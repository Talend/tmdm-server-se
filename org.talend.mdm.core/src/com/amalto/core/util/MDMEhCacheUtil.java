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

package com.amalto.core.util;

import org.springframework.cache.ehcache.EhCacheCacheManager;

import com.amalto.core.server.MDMContextAccessor;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

public class MDMEhCacheUtil {
    
    private static final String MDM_CACHE_MANAGER = "mdmCacheManager";

    // Sets the time to idle for an element before it expires.
    private static final int DEFAULT_WAIT_TIME_TO_IDLE_SECONDS = 60;
    
    // Sets the time to live since cache creation before it expires, if have clustered MDM, when data changes, both
    // servers will expire these caches & sync with db in maximum 120s
    private static final int DEFAULT_WAIT_TIME_TO_LIVE_SECONDS = 120;

    private static final int MAX_ENTRIES_LOCAL_HEAP = 10000;
    
    public static void regisitCache(String cacheName) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);

        if (mdmEhcache.getCache(cacheName) == null) {
            // Create a Cache specifying its configuration.
            Cache cache = new Cache(new CacheConfiguration(cacheName, MAX_ENTRIES_LOCAL_HEAP)
                    .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU).eternal(false)
                    .timeToLiveSeconds(DEFAULT_WAIT_TIME_TO_LIVE_SECONDS).timeToIdleSeconds(DEFAULT_WAIT_TIME_TO_IDLE_SECONDS)
                    .diskExpiryThreadIntervalSeconds(0));
            mdmEhcache.getCacheManager().addCache(cache);
        }
    }

    public static Object getCache(String cacheName, Object key) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        regisitCache(cacheName);
        Element element = mdmEhcache.getCacheManager().getCache(cacheName).get(key);
        if (element == null) {
            return null;
        }
        return element.getObjectValue();
    }

    public static void clearCache(String cacheName) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        regisitCache(cacheName);
        mdmEhcache.getCache(cacheName).clear();
    }

    public static void addCache(String cacheName, Object key, Object value) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        regisitCache(cacheName);
        mdmEhcache.getCache(cacheName).put(key, value);
    }
}
