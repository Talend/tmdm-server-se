// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import net.sf.ehcache.Element;

public class MDMEhCacheUtil {

    public static final String MDM_CACHE_MANAGER = "mdmCacheManager"; //$NON-NLS-1$

    public static final String TRANSFORMER_CACHE_NAME = "transformer"; //$NON-NLS-1$

    public static final String TRANSFORMER_PKS_CACHE_NAME = "transformerPKs"; //$NON-NLS-1$

    public static final String ROUTING_RULE_CACHE_NAME = "routingRules"; //$NON-NLS-1$

    public static final String ROUTING_RULE_PK_CACHE_NAME = "routingRulePKs"; //$NON-NLS-1$

    public static final String DATA_CLUSTER_CACHE_NAME = "dataCluster"; //$NON-NLS-1$

    @SuppressWarnings("unchecked")
    public static <K, V> V getCache(String cacheName, K key) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        Element element = mdmEhcache.getCacheManager().getCache(cacheName).get(key);
        if (element == null) {
            return null;
        }
        return (V) element.getObjectValue();
    }

    public static void clearCache(String cacheName) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        mdmEhcache.getCache(cacheName).clear();
    }

    public static <K, V> void addCache(String cacheName, K key, V value) {
        EhCacheCacheManager mdmEhcache = MDMContextAccessor.getApplicationContext().getBean(MDM_CACHE_MANAGER,
                EhCacheCacheManager.class);
        mdmEhcache.getCache(cacheName).put(key, value);
    }
}
