/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.datasource.RDBMSDataSourceBuilder;

@SuppressWarnings("UnusedDeclaration")
// Dynamically called! Do not remove!
public class DefaultStorageClassLoader extends StorageClassLoader {

    private static final Logger LOGGER = Logger.getLogger(DefaultStorageClassLoader.class);

    private static final XPath pathFactory = XPathFactory.newInstance().newXPath();

    public DefaultStorageClassLoader(ClassLoader parent,
                                     String storageName,
                                     StorageType type) {
        super(parent, storageName, type);
    }

    @Override
    public InputStream generateEhCacheConfig() {
        try {
            DocumentBuilder documentBuilder = MDMXMLUtils.getDocumentBuilderWithNamespace().get();
            Document document = documentBuilder.parse(this.getClass().getResourceAsStream(EHCACHE_XML_CONFIG));
            // <diskStore path="java.io.tmpdir"/>
            XPathExpression compile = pathFactory.compile("ehcache/diskStore"); //$NON-NLS-1$
            Node node = (Node) compile.evaluate(document, XPathConstants.NODE);
            node.getAttributes().getNamedItem("path").setNodeValue(dataSource.getCacheDirectory() + '/' + dataSource.getName()); //$NON-NLS-1$
            return toInputStream(document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public InputStream generateHibernateMapping() {
        if (resolver == null) {
            throw new IllegalStateException("Expected table resolver to be set before this method is called.");
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            documentBuilder.setEntityResolver(HibernateStorage.ENTITY_RESOLVER);
            Document document = documentBuilder.parse(this.getClass().getResourceAsStream(HIBERNATE_MAPPING_TEMPLATE));
            if (dataSource.getDialectName() == RDBMSDataSource.DataSourceDialect.MYSQL) {
                Attr propertyCatelog = document.createAttribute("catalog"); //$NON-NLS-1$
                propertyCatelog.setValue(getCatalog());
                document.getDocumentElement().getAttributes().setNamedItem(propertyCatelog);
            }
            MappingGenerator mappingGenerator = getMappingGenerator(document, resolver);
            for (Map.Entry<String, Class<? extends Wrapper>> classNameToClass : registeredClasses.entrySet()) {
                ComplexTypeMetadata typeMetadata = knownTypes.get(classNameToClass.getKey());
                if (typeMetadata != null && typeMetadata.getSuperTypes().isEmpty()) {
                    Element classElement = typeMetadata.accept(mappingGenerator);
                    if (classElement != null) { // Class element might be null if mapping is not applicable for this type
                        document.getDocumentElement().appendChild(classElement);
                    }
                }
            }
            return toInputStream(document);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected String getCatalog() {
        String catalog = StringUtils.EMPTY;
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dataSource.getConnectionURL(), dataSource.getUserName(),
                    dataSource.getPassword());
            catalog = connection.getCatalog();
        } catch (Exception e) {
            LOGGER.error("Failed to get connection of " + dataSource.getDatabaseName(), e);  //$NON-NLS-1$
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                LOGGER.error("Failed to close MySQL connection.", e); //$NON-NLS-1$
            }
        }
        return catalog;
    }

    public MappingGenerator getMappingGenerator(Document document, TableResolver resolver) {
        boolean fromMemoryStorage = false;
        try {
            UUID formatStorageName = UUID.fromString(storageName);
            fromMemoryStorage = true;
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Storage " + storageName + " is not Memory Storage", e);
            }
        }
        switch (type) {
            case MASTER:
            case SYSTEM:
                return new MappingGenerator(document, resolver, dataSource, true, fromMemoryStorage);
            case STAGING:
                return new MappingGenerator(document, resolver, dataSource, false);
            default:
                throw new NotImplementedException("No support for storage type '" + type + "'.");
        }
    }

    @Override
    public InputStream generateHibernateConfig() {
        try {
            Document document = generateHibernateConfiguration(dataSource);
            return toInputStream(document);
        } catch (Exception e) {
            throw new RuntimeException("Could not generate Hibernate configuration", e);
        }
    }

    public Document generateHibernateConfiguration(RDBMSDataSource rdbmsDataSource) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(false);

        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setEntityResolver(HibernateStorage.ENTITY_RESOLVER);
        Document document = documentBuilder.parse(DefaultStorageClassLoader.class.getResourceAsStream(HIBERNATE_CONFIG_TEMPLATE));
        String connectionUrl = rdbmsDataSource.getConnectionURL();
        String userName = rdbmsDataSource.getUserName();
        String driverClass = rdbmsDataSource.getDriverClassName();
        RDBMSDataSource.DataSourceDialect dialectType = rdbmsDataSource.getDialectName();
        String dialect = getDialect(dialectType);
        String password = rdbmsDataSource.getPassword();
        String indexBase = rdbmsDataSource.getIndexDirectory();
        int connectionPoolMinSize = rdbmsDataSource.getConnectionPoolMinSize();
        int connectionPoolMaxSize = rdbmsDataSource.getConnectionPoolMaxSize();
        if(connectionPoolMaxSize == 0){
            LOGGER.info("No value provided for property connectionPoolMaxSize of datasource " + rdbmsDataSource.getName() + ". Using default value: " + RDBMSDataSourceBuilder.CONNECTION_POOL_MAX_SIZE_DEFAULT); //$NON-NLS-1$ //$NON-NLS-2$
            connectionPoolMaxSize = RDBMSDataSourceBuilder.CONNECTION_POOL_MAX_SIZE_DEFAULT;
        }

        setPropertyValue(document, "hibernate.connection.url", connectionUrl); //$NON-NLS-1$
        setPropertyValue(document, "hibernate.connection.username", userName); //$NON-NLS-1$
        setPropertyValue(document, "hibernate.connection.driver_class", driverClass); //$NON-NLS-1$
        setPropertyValue(document, "hibernate.dialect", dialect); //$NON-NLS-1$
        setPropertyValue(document, "hibernate.connection.password", password); //$NON-NLS-1$
        // Sets up DBCP pool features
        setPropertyValue(document, "hibernate.dbcp.initialSize", String.valueOf(connectionPoolMinSize)); //$NON-NLS-1$
        setPropertyValue(document, "hibernate.dbcp.maxActive", String.valueOf(connectionPoolMaxSize)); //$NON-NLS-1$
        setPropertyValue(document, "hibernate.dbcp.maxIdle", String.valueOf(10)); //$NON-NLS-1$
        setPropertyValue(document, "hibernate.dbcp.maxTotal", String.valueOf(connectionPoolMaxSize)); //$NON-NLS-1$
        setPropertyValue(document, "hibernate.dbcp.maxWaitMillis", "60000"); //$NON-NLS-1$ //$NON-NLS-2$

        Node sessionFactoryElement = document.getElementsByTagName("session-factory").item(0); //$NON-NLS-1$
        if (rdbmsDataSource.supportFullText()) {
            /*
            <property name="hibernate.search.default.directory_provider" value="filesystem"/>
            <property name="hibernate.search.default.indexBase" value="/var/lucene/indexes"/>
             */
            addProperty(document, sessionFactoryElement, "hibernate.search.default.directory_provider", "filesystem"); //$NON-NLS-1$ //$NON-NLS-2$
            addProperty(document, sessionFactoryElement, "hibernate.search.default.indexBase", indexBase + '/' + storageName); //$NON-NLS-1$
            addProperty(document, sessionFactoryElement, "hibernate.search.default.sourceBase", indexBase + '/' + storageName); //$NON-NLS-1$
            addProperty(document, sessionFactoryElement, "hibernate.search.default.source", ""); //$NON-NLS-1$ //$NON-NLS-2$
            addProperty(document, sessionFactoryElement, "hibernate.search.default.exclusive_index_use", "false"); //$NON-NLS-1$ //$NON-NLS-2$
            addProperty(document, sessionFactoryElement, "hibernate.search.lucene_version", "LUCENE_CURRENT"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            addProperty(document, sessionFactoryElement, "hibernate.search.autoregister_listeners", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        if (dataSource.getCacheDirectory() != null && !dataSource.getCacheDirectory().isEmpty()) {
            /*
            <!-- Second level cache -->
            <property name="hibernate.cache.use_second_level_cache">true</property>
            <property name="hibernate.cache.provider_class">net.sf.ehcache.hibernate.EhCacheProvider</property>
            <property name="hibernate.cache.use_query_cache">true</property>
            <property name="net.sf.ehcache.configurationResourceName">ehcache.xml</property>
             */
            addProperty(document, sessionFactoryElement, "hibernate.cache.use_second_level_cache", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            addProperty(document, sessionFactoryElement, "hibernate.cache.provider_class", "net.sf.ehcache.hibernate.EhCacheProvider"); //$NON-NLS-1$ //$NON-NLS-2$
            addProperty(document, sessionFactoryElement, "hibernate.cache.use_query_cache", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            addProperty(document, sessionFactoryElement, "net.sf.ehcache.configurationResourceName", "ehcache.xml"); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Hibernate configuration does not define second level cache extensions due to datasource configuration."); //$NON-NLS-1$
            }
            addProperty(document, sessionFactoryElement, "hibernate.cache.use_second_level_cache", "false"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // Override default configuration with values from configuration
        Map<String, String> advancedProperties = rdbmsDataSource.getAdvancedProperties();
        for (Map.Entry<String, String> currentAdvancedProperty : advancedProperties.entrySet()) {
            setPropertyValue(document, currentAdvancedProperty.getKey(), currentAdvancedProperty.getValue());
        }
        // Order of elements highly matters and mapping shall be declared after <property/> and before <event/>.
        Element mapping = document.createElement("mapping"); //$NON-NLS-1$
        Attr resource = document.createAttribute("resource"); //$NON-NLS-1$
        resource.setValue(HIBERNATE_MAPPING);
        mapping.getAttributes().setNamedItem(resource);
        sessionFactoryElement.appendChild(mapping);

        if (rdbmsDataSource.supportFullText()) {
            addEvent(document, sessionFactoryElement, "post-update", "org.hibernate.search.event.FullTextIndexEventListener"); //$NON-NLS-1$ //$NON-NLS-2$
            addEvent(document, sessionFactoryElement, "post-insert", "org.hibernate.search.event.FullTextIndexEventListener"); //$NON-NLS-1$ //$NON-NLS-2$
            addEvent(document, sessionFactoryElement, "post-delete", "org.hibernate.search.event.FullTextIndexEventListener"); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Hibernate configuration does not define full text extensions due to datasource configuration."); //$NON-NLS-1$
        }

        return document;
    }

    protected String getDialect(RDBMSDataSource.DataSourceDialect dialectType) {
        switch (dialectType) {
            case H2:
                // Default Hibernate configuration for Hibernate forgot some JDBC type mapping.
                return H2CustomDialect.class.getName();
            case MYSQL:
                return "org.hibernate.dialect.MySQLDialect"; //$NON-NLS-1$
            default:
                throw new IllegalArgumentException("Not supported database type '" + dialectType + "'");
        }
    }

    private static void addEvent(Document document, Node sessionFactoryElement, String eventType, String listenerClass) {
        Element event = document.createElement("event"); //$NON-NLS-1$
        Attr type = document.createAttribute("type"); //$NON-NLS-1$
        type.setValue(eventType);
        event.getAttributes().setNamedItem(type);
        {
            Element listener = document.createElement("listener"); //$NON-NLS-1$
            Attr clazz = document.createAttribute("class"); //$NON-NLS-1$
            clazz.setValue(listenerClass);
            listener.getAttributes().setNamedItem(clazz);
            event.appendChild(listener);
        }
        sessionFactoryElement.appendChild(event);
    }

    private static void addProperty(Document document, Node sessionFactoryElement, String propertyName, String propertyValue) {
        Element property = document.createElement("property"); //$NON-NLS-1$
        Attr name = document.createAttribute("name"); //$NON-NLS-1$
        name.setValue(propertyName);
        property.getAttributes().setNamedItem(name);
        property.appendChild(document.createTextNode(propertyValue));
        sessionFactoryElement.appendChild(property);
    }

    private static void setPropertyValue(Document document, String propertyName, String value) throws XPathExpressionException {
        XPathExpression compile = pathFactory.compile("hibernate-configuration/session-factory/property[@name='" + propertyName + "']"); //$NON-NLS-1$ //$NON-NLS-2$
        Node node = (Node) compile.evaluate(document, XPathConstants.NODE);
        if (node != null) {
            node.setTextContent(value);
        } else {
            XPathExpression parentNodeExpression = pathFactory.compile("hibernate-configuration/session-factory"); //$NON-NLS-1$
            Node parentNode = (Node) parentNodeExpression.evaluate(document, XPathConstants.NODE);
            // Create a new property element for this datasource-specified property (TMDM-4927).
            Element property = document.createElement("property"); //$NON-NLS-1$
            Attr propertyNameAttribute = document.createAttribute("name"); //$NON-NLS-1$
            property.setAttributeNode(propertyNameAttribute);
            propertyNameAttribute.setValue(propertyName);
            property.setTextContent(value);
            parentNode.appendChild(property);
        }
    }

    protected InputStream toInputStream(Document document) throws Exception {
        StringWriter buffer = new StringWriter();
        Transformer transformer = XMLUtils.generateTransformer();
        DocumentType doctype = document.getDoctype();
        if (doctype != null) {
            if (doctype.getPublicId() != null) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype.getPublicId());
            }
            if (doctype.getSystemId() != null) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doctype.getSystemId());
            }
        }
        transformer.transform(new DOMSource(document), new StreamResult(buffer));
        String cnt = buffer.toString();
        return new ByteArrayInputStream(cnt.getBytes("UTF-8")); //$NON-NLS-1$
    }
}
