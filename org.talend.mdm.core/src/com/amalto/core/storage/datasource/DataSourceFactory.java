/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.datasource;

import com.amalto.core.server.api.DataSourceExtension;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.SystemPropertyUtils;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

@Component
public class DataSourceFactory implements ApplicationContextAware {

    public static final String DB_DATASOURCES = "db.datasources"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(DataSourceFactory.class);

    private static final XPath xPath = XPathFactory.newInstance().newXPath();

    private static DocumentBuilderFactory  factory = DocumentBuilderFactory.newInstance();

    private static ApplicationContext applicationContext;
    private static DataSourceFactory dataSourceFactory;

    private static boolean initialized = false;

    private static boolean updated = false;

    public static DataSourceFactory getInstance() {
        if (applicationContext != null) {
            return applicationContext.getBean(DataSourceFactory.class);
        } else {
            if (dataSourceFactory == null) {
                dataSourceFactory = new DataSourceFactory();
            }
            return dataSourceFactory;
        }
    }

    private static synchronized InputStream readDataSourcesConfiguration() {
        Properties configuration = MDMConfiguration.getConfiguration();
        String dataSourcesLocation = (String) configuration.get(DB_DATASOURCES);
        if (dataSourcesLocation == null) { // DB_DATASOURCES property is mandatory to continue.
            throw new IllegalStateException(DB_DATASOURCES + " is not defined in MDM configuration.");
        }
        String dataSourcesFileName = SystemPropertyUtils.resolvePlaceholders(dataSourcesLocation);
        InputStream configurationAsStream = null;
        // 1- Try from file (direct lookup)
        File file = new File(dataSourcesFileName);
        if (file.exists()) {
            LOGGER.info("Reading from datasource file at '" + file.getAbsolutePath() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
            try {
                configurationAsStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("Unexpected state (file exists but can't create a stream from it).", e);
            }
        }
        // 2- From class path
        if (configurationAsStream == null) {
            List<String> filePaths = Arrays.asList(dataSourcesFileName);
            Iterator<String> iterator = filePaths.iterator();

            String currentFilePath = StringUtils.EMPTY;
            while (configurationAsStream == null && iterator.hasNext()) {
                currentFilePath = iterator.next();
                configurationAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(currentFilePath);
            }
            if (configurationAsStream != null) {
                LOGGER.info("Reading from datasource file at '" + currentFilePath + "'."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        // 3- error: configuration was not found
        if (configurationAsStream == null) {
            throw new IllegalStateException("Could not find datasources configuration file '" + dataSourcesFileName + "'.");
        }
        return configurationAsStream;
    }

    private static Map<String, DataSourceDefinition> readDocument(InputStream configurationAsStream) {
        Document document;
        try {
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            document = documentBuilder.parse(configurationAsStream);
        } catch (Exception e) {
            throw new RuntimeException("Exception occurred during data sources XML configuration parsing", e);
        }
        try {
            NodeList datasources = (NodeList) evaluate(document, "/datasources/datasource", XPathConstants.NODESET);
            Map<String, DataSourceDefinition> nameToDataSources = new HashMap<String, DataSourceDefinition>();
            for (int i = 0; i < datasources.getLength(); i++) {
                Node currentDataSourceElement = datasources.item(i);
                String name = (String) evaluate(currentDataSourceElement, "@name", XPathConstants.STRING); //$NON-NLS-1$
                DataSource master = getDataSourceConfiguration(currentDataSourceElement, name, "master"); //$NON-NLS-1$
                if (master == null) {
                    throw new IllegalArgumentException("Data source '" + name + "'does not declare a master data section");
                }
                DataSource staging = getDataSourceConfiguration(currentDataSourceElement, name, "staging"); //$NON-NLS-1$
                DataSource system = getDataSourceConfiguration(currentDataSourceElement, name, "system"); //$NON-NLS-1$
                nameToDataSources.put(name, new DataSourceDefinition(master, staging, system));
            }
            return nameToDataSources;
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Invalid data sources configuration.", e);
        }
    }

    private static Object evaluate(Node node, String expression, QName returnType) throws XPathExpressionException {
        XPathExpression result;
        synchronized (xPath) {
            result = xPath.compile(expression);
        }
        return result.evaluate(node, returnType);
    }

    private static DataSource getDataSourceConfiguration(Node document, String name, String path) throws XPathExpressionException {
        Node dataSource = (Node) evaluate(document, path, XPathConstants.NODE);
        if (dataSource == null) {
            return null;
        }
        String type = (String) evaluate(dataSource, "type", XPathConstants.STRING); //$NON-NLS-1$
        // Invoke extensions for datasource extensions
        ServiceLoader<DataSourceExtension> extensions = ServiceLoader.load(DataSourceExtension.class);
        if (LOGGER.isDebugEnabled()) {
            StringBuilder extensionsAsString = new StringBuilder();
            int i = 0;
            for (DataSourceExtension extension : extensions) {
                extensionsAsString.append(extension.getClass().getName()).append(' ');
                i++;
            }
            if (i == 0) {
                LOGGER.debug("No datasource extension found");
            } else {
                LOGGER.debug("Found datasource extensions (" + i + " found): " + extensionsAsString);
            }
        }
        for (DataSourceExtension extension : extensions) {
            if (extension.accept(type)) {
                return extension.create(dataSource, name);
            } else {
                LOGGER.debug("Extension '" + extension + "' is not eligible for datasource type '" + type + "'.");
            }
        }
        throw new NotImplementedException("No support for type '" + type + "'.");
    }

    @Cacheable(value = "datasources", key = "#dataSourceName", cacheManager = "mdmCacheManager")
    public boolean hasDataSource(String dataSourceName) {
        return hasDataSource(readDataSourcesConfiguration(), dataSourceName);
    }

    @Cacheable(value = "datasources", key = "#dataSourceName", cacheManager = "mdmCacheManager")
    public boolean hasDataSource(InputStream configurationStream, String dataSourceName) {
        if (dataSourceName == null) {
            throw new IllegalArgumentException("Data source name can not be null.");
        }
        Map<String, DataSourceDefinition> dataSourceMap = readDocument(configurationStream);
        return dataSourceMap.get(dataSourceName) != null;
    }

    @Cacheable(value = "datasources", key = "#container", cacheManager = "mdmCacheManager")
    public DataSourceDefinition getDataSource(String dataSourceName, String container) {
        return getDataSource(readDataSourcesConfiguration(), dataSourceName, container);
    }

    public DataSourceDefinition getDataSource(InputStream configurationStream, String dataSourceName, String container) {
        if (dataSourceName == null) {
            throw new IllegalArgumentException("Data source name can not be null.");
        }
        if (container == null) {
            throw new IllegalArgumentException("Container name can not be null.");
        }
        Map<String, DataSourceDefinition> dataSourceMap = readDocument(configurationStream);
        DataSourceDefinition dataSource = dataSourceMap.get(dataSourceName);
        if (dataSource == null) {
            throw new IllegalArgumentException("Data source '" + dataSourceName + "' can not be found in configuration.");
        }
        return dataSource.transform(container);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
