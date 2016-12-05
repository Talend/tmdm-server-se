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

    public static final String MDM_CACHE_MANAGER = "mdmCacheManager";

    public static Object getCache(String cacheName, Object key) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        Element element = mdmEhcache.getCacheManager().getCache(cacheName).get(key);
        if (element == null) {
            return null;
        }
        return element.getObjectValue();
    }

    public static void clearCache(String cacheName) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        mdmEhcache.getCache(cacheName).clear();
    }

    public static void addCache(String cacheName, Object key, Object value) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        mdmEhcache.getCache(cacheName).put(key, value);
    }
}
