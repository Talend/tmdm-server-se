/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.amalto.core.objects.Plugin;
import com.amalto.core.objects.Service;

public class PluginRegistry {

    private static final Logger LOGGER = Logger.getLogger(PluginRegistry.class);

    public static final String PLUGIN_PREFIX = "amalto/local/transformer/plugin"; //$NON-NLS-1$

    public static final String SERVICE_PREFIX = "amalto/local/service/"; //$NON-NLS-1$

    private static PluginRegistry instance;

    private PluginFactory pluginFactory;

    @Autowired
    private ListableBeanFactory listableBeanFactory;

    private PluginRegistry() {
    }

    public static synchronized PluginRegistry createInstance() {
        if (instance != null) {
            throw new IllegalStateException();
        }
        instance = new PluginRegistry();
        return instance;
    }

    public static synchronized PluginRegistry getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public Plugin getPlugin(String pluginName) {
        try {
            assert (pluginName.startsWith(PLUGIN_PREFIX));
            return pluginFactory.getPlugin(pluginName);
        } catch (NoSuchBeanDefinitionException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No such plugin '" + pluginName + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return null;
        }
    }

    public Service getService(String serviceName) {
        try {
            assert (serviceName.startsWith(SERVICE_PREFIX));
            return pluginFactory.getService(serviceName);
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No such service '" + serviceName + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return null;
        }
    }

    public Map<String, Plugin> getPlugins() {
        return getList(Plugin.class);
    }

    public Map<String, Service> getServices() {
        return getList(Service.class);
    }

    public boolean existsPlugin(String pluginName) {
        return getPlugin(pluginName) != null;
    }

    public boolean existsService(String serviceName) {
        return getService(serviceName) != null;
    }

    private <T> Map<String, T> getList(Class<T> clazz) {
        return listableBeanFactory.getBeansOfType(clazz);
    }

    public void setPluginFactory(PluginFactory pluginFactory) {
        this.pluginFactory = pluginFactory;
    }
}
