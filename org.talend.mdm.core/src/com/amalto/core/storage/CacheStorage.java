/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.compare.Compare.DiffResults;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;

import com.amalto.core.query.user.Expression;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.transaction.StorageTransaction;

/**
 * <p>
 * An implementation of {@link com.amalto.core.storage.Storage} that caches
 * {@link com.amalto.core.storage.Storage#fetch(com.amalto.core.query.user.Expression) fetch} results. Cache key is the
 * hashCode of the {@link com.amalto.core.query.user.Expression} used to read records.
 * </p>
 * <p>
 * Cache results are evicted on the following conditions:
 * <ul>
 * <li>Cache result hasn't been used for a long time.</li>
 * <li>Cache result was used too many times.</li>
 * </ul>
 * When at least one of the condition is met, a new result is computed.
 * </p>
 */
public class CacheStorage implements Storage {

    private static final int DEFAULT_MAX_CACHE_LIFETIME = 60 * 1000;

    private static final int DEFAULT_MAX_CAPACITY = 30;

    private static final Logger LOGGER = Logger.getLogger(CacheStorage.class);

    private static final int MAX_TOKEN = 10;

    private final Map<Expression, CacheValue> cache = Collections.synchronizedMap(new LRUMap(DEFAULT_MAX_CAPACITY));

    private final Storage delegate;

    private int maxCacheEntryLifetime = DEFAULT_MAX_CACHE_LIFETIME;

    private int cachedResultMaxSize = 50;

    public CacheStorage(Storage delegate) {
        this.delegate = delegate;
    }

    public boolean hasCache(Expression expression) {
        CacheValue cacheValue = cache.get(expression);
        boolean timeValid = cacheValue != null && cacheValue.lastAccessTime + maxCacheEntryLifetime > System.currentTimeMillis();
        boolean usageValid = cacheValue != null && cacheValue.tokenCount.get() > 0;
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Cache: has entry -> " + (cacheValue != null) + " / time valid -> " + timeValid + " / usage valid -> "
                    + usageValid);
        }
        return cacheValue != null && timeValid && usageValid;
    }

    public int getCacheEntryUsage(Expression expression) {
        CacheValue cacheValue = cache.get(expression);
        return cacheValue == null ? 0 : cacheValue.tokenCount.get();
    }

    public int getMaxCacheEntryUsage() {
        return MAX_TOKEN;
    }

    public int getMaxCacheEntryLifetime() {
        return maxCacheEntryLifetime;
    }

    public void setMaxCacheEntryLifetime(int maxCacheEntryLifetime) {
        this.maxCacheEntryLifetime = maxCacheEntryLifetime;
    }

    public int getMaxCacheCapacity() {
        return cache.size();
    }

    /**
     * Indicates the maximum allowed size for a cached result. A size of 0 (or lower) disables the cache as storage is
     * not allowed to store a result where size > <code>cachedResultMaxSize</code>.
     * 
     * @param cachedResultMaxSize The new maximum size for a cached result. Any number <= 0 disables cache.
     */
    public void setCachedResultMaxSize(int cachedResultMaxSize) {
        this.cachedResultMaxSize = cachedResultMaxSize;
    }

    @Override
    public Storage asInternal() {
        return delegate.asInternal();
    }

    @Override
    public int getCapabilities() {
        return delegate.getCapabilities();
    }

    @Override
    public StorageTransaction newStorageTransaction() {
        return delegate.newStorageTransaction();
    }

    @Override
    public void init(DataSourceDefinition dataSource) {
        delegate.init(dataSource);
    }

    @Override
    public void prepare(MetadataRepository repository, Set<Expression> optimizedExpressions, boolean force,
            boolean dropExistingData) {
        delegate.prepare(repository, optimizedExpressions, force, dropExistingData);
    }

    @Override
    public void prepare(MetadataRepository repository, boolean dropExistingData) {
        delegate.prepare(repository, dropExistingData);
    }

    @Override
    public MetadataRepository getMetadataRepository() {
        return delegate.getMetadataRepository();
    }

    @Override
    public StorageResults fetch(Expression userQuery) {
        if (userQuery == null) {
            throw new IllegalArgumentException("Query cannot be null.");
        }
        if (cachedResultMaxSize <= 0) {
            // cachedResultMaxSize == 0 means no cache (not allowed to have a result list >= 0).
            return delegate.fetch(userQuery);
        }
        if (userQuery.cache()) {
            CacheValue cacheValue = cache.get(userQuery);
            if (cacheValue != null) {
                cacheValue.tokenCount.decrementAndGet();
                long timeSpentInCache = System.currentTimeMillis() - cacheValue.lastAccessTime;
                if (cacheValue.tokenCount.get() > 0 && timeSpentInCache < maxCacheEntryLifetime) {
                    // Cache hit!
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Cache hit! Using value for '" + userQuery + "' (token left: " + cacheValue.tokenCount.get()
                                + ").");
                    }
                    cacheValue.lastAccessTime = System.currentTimeMillis();
                    return new CachedResults(cacheValue.results);
                } else {
                    // Cache hit! (but value expired in cache)
                    if (LOGGER.isDebugEnabled()) {
                        if (cacheValue.tokenCount.get() < 0) {
                            LOGGER.debug("Cache hit! Expired value for '" + userQuery + "' (results used too many times).");
                        } else {
                            LOGGER.debug("Cache hit! Expired value for '" + userQuery + "' (results too old).");
                        }
                    }
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Cache miss! for query '" + userQuery + "'.");
                }
            }
            // Test for result size before adding to cache
            StorageResults results = delegate.fetch(userQuery);
            int count = results.getCount();
            if (count > cachedResultMaxSize) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Query is yielding more results than cache is allowed to keep, bypassing cache for query.");
                }
                return results;
            }
            // New value in cache
            cacheValue = new CacheValue();
            List<DataRecord> records = new ArrayList<>(count);
            for (DataRecord result : results) {
                records.add(result);
            }
            cacheValue.results = records;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("New cache value for '" + userQuery + "'.");
            }
            cacheValue.lastAccessTime = System.currentTimeMillis();
            cache.put(userQuery, cacheValue);
            return new CachedResults(records);
        } else {
            // TMDM-8035: Invalidate cache if query was already cached
            cache.remove(userQuery);
            return delegate.fetch(userQuery);
        }
    }

    @Override
    public void update(DataRecord record) {
        delegate.update(record);
    }

    @Override
    public void update(Iterable<DataRecord> records) {
        delegate.update(records);
    }

    @Override
    public void delete(Expression userQuery) {
        cache.remove(userQuery);
        delegate.delete(userQuery);
    }

    @Override
    public void delete(DataRecord record) {
        delegate.delete(record);
    }

    @Override
    public void close() {
        delegate.close();
    }

    @Override
    public void close(boolean dropExistingData) {
        delegate.close(dropExistingData);
    }

    @Override
    public void begin() {
        delegate.begin();
    }

    @Override
    public void commit() {
        delegate.commit();
    }

    @Override
    public void rollback() {
        delegate.rollback();
    }

    @Override
    public void end() {
        delegate.end();
    }

    @Override
    public void reindex() {
        delegate.reindex();
    }

    @Override
    public Set<String> getFullTextSuggestion(String keyword, FullTextSuggestion mode, int suggestionSize) {
        return delegate.getFullTextSuggestion(keyword, mode, suggestionSize);
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public DataSource getDataSource() {
        return delegate.getDataSource();
    }

    @Override
    public StorageType getType() {
        return delegate.getType();
    }

    @Override
    public ImpactAnalyzer getImpactAnalyzer() {
        return delegate.getImpactAnalyzer();
    }

    @Override
    public void adapt(MetadataRepository newRepository, boolean force) {
        delegate.adapt(newRepository, force);
    }

    @Override
    public boolean isClosed() {
        return delegate.isClosed();
    }

    @Override
    public List<ComplexTypeMetadata> findSortedTypesToDrop(DiffResults diffResults, boolean force) {
        return delegate.findSortedTypesToDrop(diffResults, force);
    }

    @Override
    public Set<String> findTablesToDrop(List<ComplexTypeMetadata> sortedTypesToDrop) {
        return delegate.findTablesToDrop(sortedTypesToDrop);
    }

    static class CacheValue {

        final AtomicInteger tokenCount = new AtomicInteger(MAX_TOKEN);

        long lastAccessTime = System.currentTimeMillis();

        List<DataRecord> results = Collections.emptyList();

    }
}
