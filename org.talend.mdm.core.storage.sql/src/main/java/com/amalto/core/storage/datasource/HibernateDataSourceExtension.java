/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.datasource;

import com.amalto.core.server.api.DataSourceExtension;

import org.talend.mdm.commmon.util.core.Crypt;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.HashMap;
import java.util.Map;

public class HibernateDataSourceExtension implements DataSourceExtension {

    private static final XPath xPath = XPathFactory.newInstance().newXPath();

    private static Object evaluate(Node node, String expression, QName returnType) throws XPathExpressionException {
        XPathExpression result;
        synchronized (xPath) {
            result = xPath.compile(expression);
        }
        return result.evaluate(node, returnType);
    }

    @Override
    public boolean accept(String type) {
        return "RDBMS".equals(type); //$NON-NLS-1
    }

    @Override
    public DataSource create(Node dataSource, String name) {
        try {
            String dialectName = (String) evaluate(dataSource, "rdbms-configuration/dialect", XPathConstants.STRING); //$NON-NLS-1$
            String driverClassName = (String) evaluate(dataSource,
                    "rdbms-configuration/connection-driver-class", XPathConstants.STRING); //$NON-NLS-1$
            String connectionURL = (String) evaluate(dataSource, "rdbms-configuration/connection-url", XPathConstants.STRING); //$NON-NLS-1$
            String userName = (String) evaluate(dataSource, "rdbms-configuration/connection-username", XPathConstants.STRING); //$NON-NLS-1$
            String password = (String) evaluate(dataSource, "rdbms-configuration/connection-password", XPathConstants.STRING); //$NON-NLS-1$
            int connectionPoolMinSize = ((Double) evaluate(dataSource,
                    "rdbms-configuration/connection-pool-minsize", XPathConstants.NUMBER)).intValue(); //$NON-NLS-1$
            int connectionPoolMaxSize = ((Double) evaluate(dataSource,
                    "rdbms-configuration/connection-pool-maxsize", XPathConstants.NUMBER)).intValue(); //$NON-NLS-1$
            String indexDirectory = (String) evaluate(dataSource,
                    "rdbms-configuration/fulltext-index-directory", XPathConstants.STRING); //$NON-NLS-1$
            String cacheDirectory = (String) evaluate(dataSource, "rdbms-configuration/cache-directory", XPathConstants.STRING); //$NON-NLS-1$
            String caseSensitivity = (String) evaluate(dataSource,
                    "rdbms-configuration/case-sensitive-search", XPathConstants.STRING); //$NON-NLS-1$
            Boolean caseSensitiveSearch = caseSensitivity == null || caseSensitivity.isEmpty()
                    || Boolean.parseBoolean(caseSensitivity);
            String schemaGeneration = (String) evaluate(dataSource,
                    "rdbms-configuration/schema-generation", XPathConstants.STRING); //$NON-NLS-1$
            if (schemaGeneration == null || schemaGeneration.isEmpty()) {
                // Default value is "update".
                schemaGeneration = "update"; //$NON-NLS-1$
            }
            String generateTechnicalFKAsString = (String) evaluate(dataSource,
                    "rdbms-configuration/schema-technical-fk", XPathConstants.STRING); //$NON-NLS-1$
            Boolean generateTechnicalFK;
            if (generateTechnicalFKAsString == null || generateTechnicalFKAsString.isEmpty()) {
                // Default value is "true" (enforce FK for technical FKs).
                generateTechnicalFK = Boolean.TRUE;
            } else {
                generateTechnicalFK = Boolean.parseBoolean(generateTechnicalFKAsString);
            }
            Map<String, String> advancedProperties = new HashMap<String, String>();
            NodeList properties = (NodeList) evaluate(dataSource,
                    "rdbms-configuration/properties/property", XPathConstants.NODESET); //$NON-NLS-1$
            for (int i = 0; i < properties.getLength(); i++) {
                Node item = properties.item(i);
                String propertyName = item.getAttributes().getNamedItem("name").getNodeValue(); //$NON-NLS-1$
                String propertyValue = item.getTextContent();
                advancedProperties.put(propertyName, propertyValue);
            }
            // Contains optimization
            String containsOptimizationAsString = (String) evaluate(dataSource,
                    "rdbms-configuration/contains-optimization", XPathConstants.STRING); //$NON-NLS-1$
            RDBMSDataSource.ContainsOptimization containsOptimization = RDBMSDataSource.ContainsOptimization.FULL_TEXT;
            if ("fulltext".equals(containsOptimizationAsString)) { //$NON-NLS-1$
                containsOptimization = RDBMSDataSource.ContainsOptimization.FULL_TEXT;
            } else if ("disabled".equals(containsOptimizationAsString)) { //$NON-NLS-1$
                containsOptimization = RDBMSDataSource.ContainsOptimization.DISABLED;
            } else if ("like".equals(containsOptimizationAsString)) { //$NON-NLS-1$
                containsOptimization = RDBMSDataSource.ContainsOptimization.LIKE;
            }
            String initConnectionURL = (String) evaluate(dataSource,
                    "rdbms-configuration/init/connection-url", XPathConstants.STRING); //$NON-NLS-1$
            String initUserName = (String) evaluate(dataSource,
                    "rdbms-configuration/init/connection-username", XPathConstants.STRING); //$NON-NLS-1$
            String initPassword = (String) evaluate(dataSource,
                    "rdbms-configuration/init/connection-password", XPathConstants.STRING); //$NON-NLS-1$
            String databaseName = (String) evaluate(dataSource, "rdbms-configuration/init/database-name", XPathConstants.STRING); //$NON-NLS-1$
            return new RDBMSDataSource(name, dialectName, driverClassName, userName, Crypt.decrypt(password), connectionPoolMinSize,
                    connectionPoolMaxSize, indexDirectory, cacheDirectory, caseSensitiveSearch, schemaGeneration,
                    generateTechnicalFK, advancedProperties, connectionURL, databaseName, containsOptimization, Crypt.decrypt(initPassword),
                    initUserName, initConnectionURL, true);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Unable to parse datasource configuration.", e);
        } catch (Exception e) {
            throw new RuntimeException("Unable to encrypt datasource passwords.", e);
        }
    }
}
