// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package com.amalto.core.server;

import org.springframework.beans.BeansException;
import org.springframework.cache.ehcache.EhCacheCacheManager;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class MDMEhCacheUtil {
    
    private static final String MDM_CACHE_MANAGER = "mdmCacheManager";

    private static final int DEFAULT_WAIT_TIME_TO_IDLE_SECONDS = 60;
    
    private static final int DEFAULT_WAIT_TIME_TO_LIVE_SECONDS = 120;

    private static final int MAX_ENTRIES_LOCAL_HEAP = 10000;
    
    private static MDMEhCacheUtil instance;
    
    private MDMEhCacheUtil() {
    }

    public static MDMEhCacheUtil getInstance() {
        if (instance == null) {
            instance = new MDMEhCacheUtil();
        }
        return instance;
    }

    private void doRegisitCache(String cacheName) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);

        // Create a Cache specifying its configuration.
        Cache cache = new Cache(new CacheConfiguration(cacheName, MAX_ENTRIES_LOCAL_HEAP)
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU).eternal(false)
                .timeToLiveSeconds(DEFAULT_WAIT_TIME_TO_LIVE_SECONDS).timeToIdleSeconds(DEFAULT_WAIT_TIME_TO_IDLE_SECONDS)
                .diskExpiryThreadIntervalSeconds(0).persistence(new PersistenceConfiguration().strategy(Strategy.LOCALTEMPSWAP)));
        mdmEhcache.getCacheManager().addCache(cache);
    }

    private void decideRegistCache(String cacheName, EhCacheCacheManager mdmEhcache) {
        if (mdmEhcache.getCache(cacheName) == null) {
            doRegisitCache(cacheName);
        }
    }

    public Object getCache(String cacheName, Object key) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        decideRegistCache(cacheName, mdmEhcache);
        Element element = mdmEhcache.getCacheManager().getCache(cacheName).get(key);
        if (element == null) {
            return null;
        }
        return element.getObjectValue();
    }

    public void clearCache(String cacheName) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        decideRegistCache(cacheName, mdmEhcache);
        mdmEhcache.getCache(cacheName).clear();
    }

    public void addCache(String cacheName, Object key, Object value) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        decideRegistCache(cacheName, mdmEhcache);
        mdmEhcache.getCache(cacheName).put(key, value);
    }
}
