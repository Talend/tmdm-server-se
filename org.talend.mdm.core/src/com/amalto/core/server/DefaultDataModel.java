/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.talend.mdm.commmon.util.exception.XmlBeanDefinitionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.save.SaverSession;
import com.amalto.core.server.api.DataModel;
import com.amalto.core.util.XtentisException;

public class DefaultDataModel implements DataModel {

    private static final Logger LOGGER = Logger.getLogger(DefaultDataModel.class);

    private static final XPath X_PATH = XPathFactory.newInstance().newXPath();

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    private static final String EMPTY_SCHEMA = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + //$NON-NLS-1$
            "<xsd:schema " + //$NON-NLS-1$
            "	elementFormDefault=\"qualified\"" + //$NON-NLS-1$
            "	xml:lang=\"EN\"" + //$NON-NLS-1$
            "	xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">" + //$NON-NLS-1$
            "</xsd:schema>";

    static {
        try {
            DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
            DOCUMENT_BUILDER_FACTORY.setExpandEntityReferences(false);
            DOCUMENT_BUILDER_FACTORY.setFeature(MDMXMLUtils.FEATURE_DISALLOW_DOCTYPE, true);
        } catch (Exception e) {
            throw new XmlBeanDefinitionException("Error occurred to initialize DocumentBuilderFactory", e);
        }
        X_PATH.setNamespaceContext(XSDNamespaceContext.INSTANCE);
    }

    @Override
    public DataModelPOJOPK putDataModel(DataModelPOJO dataModel) throws XtentisException {
        try {
            if ((dataModel.getSchema() == null) || "".equals(dataModel.getSchema())) { //$NON-NLS-1$
                // put an empty schema
                dataModel.setSchema(EMPTY_SCHEMA);
            }
            ObjectPOJOPK pk = dataModel.store();
            if (pk == null) {
                throw new XtentisException("Unable to create the Data Model"); //$NON-NLS-1$
            }
            return new DataModelPOJOPK(pk);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to create/update the Data Model '" + dataModel.getName() + '\''; //$NON-NLS-1$
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }

    }

    @Override
    public DataModelPOJO getDataModel(DataModelPOJOPK pk) throws XtentisException {
        if (pk == null || pk.getUniqueId() == null) {
            throw new XtentisException("The Data Model can't be empty!"); //$NON-NLS-1$
        }
        try {
            DataModelPOJO sp = ObjectPOJO.load(DataModelPOJO.class, pk);
            if (sp == null && pk.getUniqueId() != null && !"null".equals(pk.getUniqueId())) { //$NON-NLS-1$
                String err = "The Data Model '" + pk.getUniqueId() + "' does not exist."; //$NON-NLS-1$ //$NON-NLS-2$
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            return sp;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the Data Model '" + pk.toString() + '\''; //$NON-NLS-1$;
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    @Override
    public DataModelPOJO existsDataModel(DataModelPOJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(DataModelPOJO.class, pk);
        } catch (XtentisException e) {
            return null;
        } catch (Exception e) {
            String err = "Could not check whether this Data Model exists: '" + pk.getUniqueId() + '\''; //$NON-NLS-1$
            LOGGER.error(err, e);
            return null;
        }
    }

    @Override
    public DataModelPOJOPK removeDataModel(DataModelPOJOPK pk) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removing " + pk.getUniqueId()); //$NON-NLS-1$
        }

        try {
            ObjectPOJOPK objectPOJOPK = ObjectPOJO.remove(DataModelPOJO.class, pk);
            ServerContext.INSTANCE.get().getMetadataRepositoryAdmin().remove(objectPOJOPK.getUniqueId());
            return new DataModelPOJOPK(objectPOJOPK);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the DataModel '" + pk.toString() + '\''; //$NON-NLS-1$
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }

    }

    @Override
    public Collection<DataModelPOJOPK> getDataModelPKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> dataModelPKs = ObjectPOJO.findAllPKs(DataModelPOJO.class, regex);
        List<DataModelPOJOPK> l = new ArrayList<DataModelPOJOPK>();
        for (ObjectPOJOPK dataModelPK : dataModelPKs) {
            l.add(new DataModelPOJOPK(dataModelPK));
        }
        return l;
    }

    @Override
    public String checkSchema(String schema) throws XtentisException {
        return schema;
    }

    @Override
    public String putBusinessConceptSchema(DataModelPOJOPK pk, String conceptSchemaString) throws XtentisException {
        try {
            DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            DataModelPOJO dataModel = getDataModel(pk);
            String schema = dataModel.getSchema();
            if (schema == null || schema.isEmpty()) {
                schema = EMPTY_SCHEMA;
            }
            Document schemaAsDOM = builder.parse(new InputSource(new StringReader(schema)));
            Document newConceptAsDOM = builder.parse(new InputSource(new StringReader(conceptSchemaString)));
            String conceptName;
            Node existingNode;
            synchronized (X_PATH) {
                conceptName = (String) X_PATH.evaluate("/xsd:element/@name", newConceptAsDOM, XPathConstants.STRING); //$NON-NLS-1$
                existingNode = (Node) X_PATH.evaluate(
                        "/xsd:schema/xsd:element[@name='" + conceptName + "']", schemaAsDOM, XPathConstants.NODE); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (existingNode != null) {
                existingNode.getParentNode().removeChild(existingNode);
            }
            schemaAsDOM.getDocumentElement().appendChild(schemaAsDOM.importNode(newConceptAsDOM.getDocumentElement(), true));
            dataModel.setSchema(XMLUtils.nodeToString(schemaAsDOM, true, false).replaceAll("\r\n", "\n"));
            dataModel.store();
            invalidateConceptSession(dataModel.getName());
            return conceptName;
        } catch (Exception e) {
            throw new XtentisException(e);
        }
    }

    private static void invalidateConceptSession(String businessConceptName) {
        SaverSession session = SaverSession.newSession();
        session.getSaverSource().invalidateTypeCache(businessConceptName);
    }

    @Override
    public String deleteBusinessConcept(DataModelPOJOPK pk, String businessConceptName) throws XtentisException {
        try {
            DocumentBuilder builder = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            DataModelPOJO dataModel = getDataModel(pk);
            String schema = dataModel.getSchema();
            if (schema == null || schema.isEmpty()) {
                return businessConceptName;
            }
            Document schemaAsDOM = builder.parse(new InputSource(new StringReader(schema)));
            Node existingNode;
            synchronized (X_PATH) {
                existingNode = (Node) X_PATH.evaluate(
                        "/xsd:schema/xsd:element[@name='" + businessConceptName + "']", schemaAsDOM, XPathConstants.NODE); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (existingNode != null) {
                existingNode.getParentNode().removeChild(existingNode);
            }
            dataModel.setSchema(XMLUtils.nodeToString(schemaAsDOM, true, false).replaceAll("\r\n", "\n"));
            dataModel.store();
            invalidateConceptSession(dataModel.getName());
            return businessConceptName;
        } catch (Exception e) {
            throw new XtentisException(e);
        }
    }

    @Override
    public String[] getAllBusinessConceptsNames(DataModelPOJOPK pk) throws XtentisException {
        MetadataRepositoryAdmin metadataRepositoryAdmin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
        MetadataRepository repository = metadataRepositoryAdmin.get(pk.getUniqueId());
        Collection<ComplexTypeMetadata> userComplexTypes = repository.getUserComplexTypes();
        String[] businessConceptNames = new String[userComplexTypes.size()];
        int i = 0;
        for (ComplexTypeMetadata currentType : userComplexTypes) {
            businessConceptNames[i++] = currentType.getName();
        }
        return businessConceptNames;
    }

    private static class XSDNamespaceContext implements NamespaceContext {

        private static NamespaceContext INSTANCE = new XSDNamespaceContext();

        @Override
        public String getNamespaceURI(String prefix) {
            if ("xsd".equals(prefix)) { //$NON-NLS-1$
                return XMLConstants.W3C_XML_SCHEMA_NS_URI;
            }
            return null;
        }

        @Override
        public String getPrefix(String namespaceURI) {
            if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespaceURI)) {
                return "xsd"; //$NON-NLS-1$
            }
            return null;
        }

        @Override
        public Iterator getPrefixes(String namespaceURI) {
            if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(namespaceURI)) {
                return Collections.singleton("xsd").iterator(); //$NON-NLS-1$
            }
            return Collections.emptyList().iterator();
        }
    }
}
