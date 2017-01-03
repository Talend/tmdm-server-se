/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage;

import static com.amalto.core.query.user.UserQueryBuilder.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataVisitor;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.core.storage.record.DataRecordXmlWriter;
import com.amalto.core.storage.record.SystemDataRecordXmlWriter;
import com.amalto.core.storage.record.XmlDOMDataRecordReader;
import com.amalto.core.storage.record.XmlSAXDataRecordReader;
import com.amalto.xmlserver.interfaces.XmlServerException;

public class SystemStorageWrapper extends StorageWrapper {

    private static final DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();

    private static final String SYSTEM_PREFIX = "amaltoOBJECTS"; //$NON-NLS-1$

    private static final String CUSTOM_FORM_TYPE = "custom-form-pOJO"; //$NON-NLS-1$

    private static final String DROPPED_ITEM_TYPE = "dropped-item-pOJO"; //$NON-NLS-1$

    private static final String COMPLETED_ROUTING_ORDER = "completed-routing-order-v2-pOJO"; //$NON-NLS-1$

    private static final String FAILED_ROUTING_ORDER = "failed-routing-order-v2-pOJO"; //$NON-NLS-1$

    private static final String ACTIVE_ROUTING_ORDER = "active-routing-order-v2-pOJO"; //$NON-NLS-1$

    private static final String PROVISIONING_PREFIX_INFO = "PROVISIONING.User."; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(SystemStorageWrapper.class);

    public SystemStorageWrapper() {
        DOCUMENT_BUILDER_FACTORY.setNamespaceAware(true);
        // Create "system" storage
        Server server = ServerContext.INSTANCE.get();
        StorageAdmin admin = server.getStorageAdmin();
        String datasource = admin.getDatasource(StorageAdmin.SYSTEM_STORAGE);
        admin.create(StorageAdmin.SYSTEM_STORAGE, StorageAdmin.SYSTEM_STORAGE, datasource, null);
    }

    private ComplexTypeMetadata getType(String clusterName, Storage storage, String uniqueId) {
        MetadataRepository repository = storage.getMetadataRepository();
        if (uniqueId != null && uniqueId.startsWith("amalto_local_service_")) { //$NON-NLS-1$
            return repository.getComplexType("service-bMP"); //$NON-NLS-1$
        }
        if (clusterName.startsWith(SYSTEM_PREFIX) || clusterName.startsWith("amalto")) { //$NON-NLS-1$) {
            if (!"amaltoOBJECTSservices".equals(clusterName)) { //$NON-NLS-1$
                return repository.getComplexType(ClassRepository.format(clusterName.substring(SYSTEM_PREFIX.length()) + "POJO")); //$NON-NLS-1$
            } else {
                return repository.getComplexType(ClassRepository.format(clusterName.substring(SYSTEM_PREFIX.length())));
            }
        }
        if (XSystemObjects.DC_MDMITEMSTRASH.getName().equals(clusterName)) {
            return repository.getComplexType(DROPPED_ITEM_TYPE);
        } else if ("MDMDomainObjects".equals(clusterName) || "MDMItemImages".equals(clusterName) || "FailedAutoCommitSvnMessage".equals(clusterName)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return null; // Documents for these clusters don't have a predefined structure.
        }
        // No id, so no type to be read.
        if (uniqueId == null) {
            return null;
        }
        // MIGRATION.completed.record
        return repository.getComplexType(getTypeName(uniqueId));
    }

    @Override
    protected Storage getStorage(String dataClusterName) {
        return storageAdmin.get(StorageAdmin.SYSTEM_STORAGE, null);
    }

    @Override
    protected Storage getStorage(String dataClusterName, String revisionId) {
        return storageAdmin.get(StorageAdmin.SYSTEM_STORAGE, null);
    }

    @Override
    public long deleteCluster(String revisionID, String clusterName) throws XmlServerException {
        return 0;
    }

    @Override
    public String[] getAllClusters(String revisionID) throws XmlServerException {
        Set<String> internalClusterNames = DispatchWrapper.getInternalClusterNames();
        return internalClusterNames.toArray(new String[internalClusterNames.size()]);
    }

    @Override
    public long deleteAllClusters(String revisionID) throws XmlServerException {
        return 0;
    }

    @Override
    public long createCluster(String revisionID, String clusterName) throws XmlServerException {
        return 0;
    }

    @Override
    public boolean existCluster(String revision, String cluster) throws XmlServerException {
        return true;
    }

    @Override
    protected Collection<ComplexTypeMetadata> getClusterTypes(String clusterName, String revisionID) {
        Storage storage = getStorage(clusterName, revisionID);
        MetadataRepository repository = storage.getMetadataRepository();
        return filter(repository, clusterName);
    }

    public static Collection<ComplexTypeMetadata> filter(MetadataRepository repository, String clusterName) {
        if (XSystemObjects.DC_CONF.getName().equals(clusterName)) {
            return filter(repository, "Conf", "AutoIncrement"); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (XSystemObjects.DC_CROSSREFERENCING.getName().equals(clusterName)) {
            return Collections.emptyList(); // TODO Support crossreferencing
        } else if (XSystemObjects.DC_PROVISIONING.getName().equals(clusterName)) {
            return filter(repository, "User", "Role"); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (XSystemObjects.DC_XTENTIS_COMMON_REPORTING.getName().equals(clusterName)) {
            return filter(repository, "Reporting", "hierarchical-report"); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (XSystemObjects.DC_SEARCHTEMPLATE.getName().equals(clusterName)) {
            return filter(repository, "BrowseItem", "HierarchySearchItem"); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (XSystemObjects.DC_JCAADAPTERS.getName().equals(clusterName)) {
            // Not supported
            return Collections.emptyList();
        } else if (XSystemObjects.DC_INBOX.getName().equals(clusterName)) {
            // Not supported
            return Collections.emptyList();
        } else {
            return repository.getUserComplexTypes();
        }
    }

    private static Collection<ComplexTypeMetadata> filter(MetadataRepository repository, String... typeNames) {
        final Set<ComplexTypeMetadata> filteredTypes = new HashSet<ComplexTypeMetadata>();
        MetadataVisitor<Void> transitiveTypeClosure = new DefaultMetadataVisitor<Void>() {

            @Override
            public Void visit(ComplexTypeMetadata complexType) {
                if (complexType.isInstantiable()) {
                    filteredTypes.add(complexType);
                }
                return super.visit(complexType);
            }

            @Override
            public Void visit(ContainedComplexTypeMetadata containedType) {
                if (containedType.isInstantiable()) {
                    filteredTypes.add(containedType);
                }
                return super.visit(containedType);
            }
        };
        for (String typeName : typeNames) {
            ComplexTypeMetadata type = repository.getComplexType(typeName);
            if (type != null) {
                type.accept(transitiveTypeClosure);
            }
        }
        return filteredTypes;
    }

    @Override
    public String[] getAllDocumentsUniqueID(String revisionID, String clusterName) throws XmlServerException {
        Storage storage = getStorage(clusterName, revisionID);
        ComplexTypeMetadata type = getType(clusterName, storage, null);
        if (type != null) {
            FieldMetadata keyField = type.getKeyFields().iterator().next();
            UserQueryBuilder qb = from(type).select(keyField);
            StorageResults results = storage.fetch(qb.getSelect());
            try {
                String[] ids = new String[results.getCount()];
                int i = 0;
                for (DataRecord result : results) {
                    ids[i++] = String.valueOf(result.get(keyField));
                }
                return ids;
            } finally {
                results.close();
            }
        } else {
            return new String[0];
        }
    }

    @Override
    public long putDocumentFromDOM(Element root, String uniqueID, String clusterName, String revisionID)
            throws XmlServerException {
        long start = System.currentTimeMillis();
        {
            DataRecordReader<Element> reader = new XmlDOMDataRecordReader();
            Storage storage = getStorage(clusterName, revisionID);
            ComplexTypeMetadata type = getType(clusterName, storage, uniqueID);
            if (type == null) {
                return -1; // TODO
            }
            if (DROPPED_ITEM_TYPE.equals(type.getName())) {
                // head.Product.Product.0-
                uniqueID = uniqueID.substring(0, uniqueID.length() - 1);
            }
            MetadataRepository repository = storage.getMetadataRepository();
            DataRecord record = reader.read(revisionID, repository, type, root);
            for (FieldMetadata keyField : type.getKeyFields()) {
                if (record.get(keyField) == null) {
                    LOGGER.warn("Ignoring update for record '" + uniqueID + "' (does not provide key information).");
                    return 0;
                }
            }
            storage.update(record);
        }
        return System.currentTimeMillis() - start;
    }

    @Override
    public long putDocumentFromSAX(String dataClusterName, XMLReader docReader, InputSource input, String revisionId)
            throws XmlServerException {
        long start = System.currentTimeMillis();
        {
            Storage storage = getStorage(dataClusterName);
            ComplexTypeMetadata type = getType(dataClusterName, storage, input.getPublicId());
            if (type == null) {
                return -1; // TODO
            }
            DataRecordReader<XmlSAXDataRecordReader.Input> reader = new XmlSAXDataRecordReader();
            XmlSAXDataRecordReader.Input readerInput = new XmlSAXDataRecordReader.Input(docReader, input);
            DataRecord record = reader.read(revisionId, storage.getMetadataRepository(), type, readerInput);
            storage.update(record);
        }
        return System.currentTimeMillis() - start;
    }

    @Override
    public long putDocumentFromString(String xmlString, String uniqueID, String clusterName, String revisionID)
            throws XmlServerException {
        return putDocumentFromString(xmlString, uniqueID, clusterName, revisionID, null);
    }

    @Override
    public long putDocumentFromString(String xmlString, String uniqueID, String clusterName, String revisionID,
            String documentType) throws XmlServerException {
        try {
            InputSource source = new InputSource(new StringReader(xmlString));
            Document document = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder().parse(source);
            return putDocumentFromDOM(document.getDocumentElement(), uniqueID, clusterName, revisionID);
        } catch (Exception e) {
            throw new XmlServerException(e);
        }
    }

    @Override
    public String getDocumentAsString(String revisionID, String clusterName, String uniqueID) throws XmlServerException {
        return getDocumentAsString(revisionID, clusterName, uniqueID, "UTF-8");
    }

    @Override
    public String getDocumentAsString(String revisionID, String clusterName, String uniqueID, String encoding)
            throws XmlServerException {
        if (encoding == null) {
            encoding = "UTF-8"; //$NON-NLS-1$
        }
        Storage storage = getStorage(clusterName);
        ComplexTypeMetadata type = getType(clusterName, storage, uniqueID);
        if (type == null) {
            return null; // TODO
        }
        UserQueryBuilder qb;
        boolean isUserFormat;
        if (DROPPED_ITEM_TYPE.equals(type.getName())) {
            isUserFormat = false;
            // head.Product.Product.0-
            uniqueID = uniqueID.substring(0, uniqueID.length() - 1);
            // TODO Filter by revision
            // String revisionId = StringUtils.substringBefore(uniqueID, ".");
            String documentUniqueId = StringUtils.substringAfter(uniqueID, "."); //$NON-NLS-1$
            qb = from(type).where(eq(type.getKeyFields().iterator().next(), documentUniqueId));
        } else if (COMPLETED_ROUTING_ORDER.equals(type.getName()) || FAILED_ROUTING_ORDER.equals(type.getName())
                || ACTIVE_ROUTING_ORDER.equals(type.getName())) {
            isUserFormat = false;
            qb = from(type).where(eq(type.getKeyFields().iterator().next(), uniqueID));
        } else {
            // TMDM-5513 custom form layout pk contains double dot .. to split, but it's a system definition object
            // like this Product..Product..product_layout
            isUserFormat = !uniqueID.contains("..") && uniqueID.indexOf('.') > 0;
            String documentUniqueId = uniqueID;
            if (uniqueID.startsWith(PROVISIONING_PREFIX_INFO)) {
                documentUniqueId = StringUtils.substringAfter(uniqueID, PROVISIONING_PREFIX_INFO);
            } else if (isUserFormat) {
                documentUniqueId = StringUtils.substringAfterLast(uniqueID, "."); //$NON-NLS-1$
            }
            qb = from(type).where(eq(type.getKeyFields().iterator().next(), documentUniqueId));
        }
        StorageResults records = storage.fetch(qb.getSelect());
        ByteArrayOutputStream output = new ByteArrayOutputStream(1024);
        try {
            Iterator<DataRecord> iterator = records.iterator();
            // Enforce root element name in case query returned instance of a subtype.
            DataRecordWriter dataRecordXmlWriter = isUserFormat ? new DataRecordXmlWriter(type) : new SystemDataRecordXmlWriter(
                    (ClassRepository) storage.getMetadataRepository(), type);
            if (iterator.hasNext()) {
                DataRecord result = iterator.next();
                if (isUserFormat) {
                    String identifier = uniqueID.startsWith(PROVISIONING_PREFIX_INFO) ? StringUtils.substringAfter(uniqueID,
                            PROVISIONING_PREFIX_INFO) : uniqueID.split("\\.")[2]; //$NON-NLS-1$
                    long timestamp = result.getRecordMetadata().getLastModificationTime();
                    String taskId = result.getRecordMetadata().getTaskId();
                    byte[] start = ("<ii><c>" + clusterName + "</c><dmn>" + clusterName + "</dmn><dmr/><sp/><t>" + timestamp + "</t><taskId>" + taskId + "</taskId><i>" + identifier + "</i><p>").getBytes(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                    output.write(start);
                }
                dataRecordXmlWriter.write(result, output);
                if (iterator.hasNext()) {
                    int recordsLeft = 1;
                    while (iterator.hasNext()) { // TMDM-6712: Consumes all results in iterator
                        iterator.next();
                        if (recordsLeft % 10 == 0) {
                            LOGGER.warn("Processing system query lead to unexpected number of results (" + recordsLeft
                                    + " so far).");
                        }
                        recordsLeft++;
                    }
                    throw new IllegalStateException("Expected only 1 result.");
                }
                if (isUserFormat) {
                    byte[] end = ("</p></ii>").getBytes(); //$NON-NLS-1$
                    output.write(end);
                }
                output.flush();
                return new String(output.toByteArray(), encoding);
            } else {
                return null;
            }
        } catch (IOException e) {
            throw new XmlServerException(e);
        } finally {
            records.close();
        }
    }

    @Override
    public long deleteDocument(String revisionID, String clusterName, String uniqueID, String documentType)
            throws XmlServerException {
        Storage storage = getStorage(clusterName);
        ComplexTypeMetadata type = getType(clusterName, storage, uniqueID);
        if (type == null) {
            return -1;
        }
        if (DROPPED_ITEM_TYPE.equals(type.getName())) {
            // head.Product.Product.0-
            uniqueID = uniqueID.substring(0, uniqueID.length() - 1);
            // TODO Filter by revision
            // String revisionId = StringUtils.substringBefore(uniqueID, ".");
            uniqueID = StringUtils.substringAfter(uniqueID, "."); //$NON-NLS-1$
        } else if (!COMPLETED_ROUTING_ORDER.equals(type.getName()) && !FAILED_ROUTING_ORDER.equals(type.getName())
                && !ACTIVE_ROUTING_ORDER.equals(type.getName()) && !CUSTOM_FORM_TYPE.equals(type.getName())) {
            if (uniqueID.startsWith(PROVISIONING_PREFIX_INFO)) {
                uniqueID = StringUtils.substringAfter(uniqueID, PROVISIONING_PREFIX_INFO);
            } else if (uniqueID.contains(".")) { //$NON-NLS-1$
                uniqueID = StringUtils.substringAfterLast(uniqueID, "."); //$NON-NLS-1$
            }
        }
        long start = System.currentTimeMillis();
        {
            UserQueryBuilder qb = from(type).where(eq(type.getKeyFields().iterator().next(), uniqueID));
            try {
                storage.begin();
                Select select = qb.getSelect();
                StorageResults results = storage.fetch(select);
                if (results.getCount() == 0) {
                    throw new IllegalArgumentException("Could not find document to delete."); //$NON-NLS-1$
                }
                storage.delete(select);
                storage.commit();
            } catch (Exception e) {
                storage.rollback();
                throw new XmlServerException(e);
            } finally {
                storage.end();
            }
        }
        return System.currentTimeMillis() - start;
    }
}
