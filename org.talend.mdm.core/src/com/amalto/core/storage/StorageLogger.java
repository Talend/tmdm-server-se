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

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ConsoleDumpMetadataVisitor;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.compare.Compare.DiffResults;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;

import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.UserQueryDumpConsole;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.transaction.StorageTransaction;

public class StorageLogger implements Storage {

    private static final Logger LOGGER = Logger.getLogger(StorageLogger.class);

    private final Storage delegate;

    public StorageLogger(Storage delegate) {
        this.delegate = delegate;
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

    public void init(DataSourceDefinition dataSource) {
        delegate.init(dataSource);
    }

    private void handlePrepareError(MetadataRepository repository, boolean force, Exception e) {
        // Dumps types to console
        LOGGER.error("##### Error during storage preparation (force = " + force + ") #####", e);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Types:");
            repository.accept(new ConsoleDumpMetadataVisitor());
        }
        // TODO Dumps hibernate mapping (if possible).
        // TODO Dumps hibernate configuration
        // Re-throw exception
        throw new RuntimeException(e);
    }

    public void prepare(MetadataRepository repository, Set<Expression> optimizedExpressions, boolean force,
            boolean dropExistingData) {
        try {
            delegate.prepare(repository, optimizedExpressions, force, dropExistingData);
        } catch (Exception e) {
            handlePrepareError(repository, force, e);
        }
    }

    public void prepare(MetadataRepository repository, boolean dropExistingData) {
        try {
            delegate.prepare(repository, dropExistingData);
        } catch (Exception e) {
            handlePrepareError(repository, false, e);
        }
    }

    @Override
    public MetadataRepository getMetadataRepository() {
        return delegate.getMetadataRepository();
    }

    public StorageResults fetch(Expression userQuery) {
        if (LOGGER.isDebugEnabled()) {
            userQuery.accept(new UserQueryDumpConsole(LOGGER));
        }
        try {
            return delegate.fetch(userQuery);
        } catch (Exception e) {
            // Dumps query to console
            LOGGER.error("##### Error during fetch operation #####", e);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Query:");
                userQuery.accept(new UserQueryDumpConsole(LOGGER));
            }
            // Re-throw exception
            throw new RuntimeException(e);
        }
    }

    public void update(DataRecord record) {
        delegate.update(record);
    }

    public void update(Iterable<DataRecord> records) {
        delegate.update(records);
    }

    public void delete(Expression userQuery) {
        if (LOGGER.isDebugEnabled()) {
            userQuery.accept(new UserQueryDumpConsole(LOGGER));
        }
        try {
            delegate.delete(userQuery);
        } catch (Exception e) {
            // Dumps query to console
            LOGGER.error("##### Error during delete operation #####");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Query:");
                userQuery.accept(new UserQueryDumpConsole(LOGGER));
            }
            // Re-throw exception
            throw new RuntimeException(e);
        }
    }

    @Override
    public void delete(DataRecord record) {
        delegate.delete(record);
    }

    public void close() {
        delegate.close();
    }

    @Override
    public void close(boolean dropExistingData) {
        delegate.close(dropExistingData);
    }

    public void begin() {
        delegate.begin();
    }

    public void commit() {
        delegate.commit();
    }

    public void rollback() {
        delegate.rollback();
    }

    public void end() {
        delegate.end();
    }

    public void reindex() {
        delegate.reindex();
    }

    public Set<String> getFullTextSuggestion(String keyword, FullTextSuggestion mode, int suggestionSize) {
        return delegate.getFullTextSuggestion(keyword, mode, suggestionSize);
    }

    public String getName() {
        return delegate.getName();
    }

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
}
