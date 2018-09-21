/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.load.action.LoadAction;
import com.amalto.core.save.AutoCommitSaverContext;
import com.amalto.core.save.DOMDocument;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.PartialUpdateSaverContext;
import com.amalto.core.save.ReportDocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.save.UserAction;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.util.Util;
import com.amalto.core.util.XSDKey;

public class SaverContextFactory {

    public static final DocumentBuilder DOCUMENT_BUILDER;

    public static final Document EMPTY_UPDATE_REPORT;

    private static DocumentSaverExtension saverExtension;

    @SuppressWarnings("unchecked")
    static synchronized DocumentSaver invokeSaverExtension(DocumentSaver saver) {
        if (saverExtension == null) {
            try {
                Class<DocumentSaverExtension> extension = (Class<DocumentSaverExtension>) Class.forName("com.amalto.core.save.DocumentSaverExtensionImpl"); //$NON-NLS-1$
                saverExtension = extension.newInstance();
            } catch (ClassNotFoundException e) {
                Logger.getLogger(UserContext.class).warn("No extension found for save."); //$NON-NLS-1$
                saverExtension = new DocumentSaverExtension() {
                    public DocumentSaver invokeSaverExtension(DocumentSaver saver) {
                        return saver;
                    }
                };
            } catch (Exception e) {
                throw new RuntimeException("Unexpected exception occurred during saver extension lookup."); //$NON-NLS-1$
            }
        }

        return saverExtension.invokeSaverExtension(saver);
    }

    static {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        factory.setValidating(false);
        factory.setExpandEntityReferences(false);
        try {
            factory.setFeature(MDMXMLUtils.FEATURE_DISALLOW_DOCTYPE, true);
            factory.setFeature(MDMXMLUtils.FEATURE_DEFER_NODE_EXPANSION, false); //$NON-NLS-1$
            DOCUMENT_BUILDER = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Could not acquire a document builder.", e); //$NON-NLS-1$
        }
        try {
            EMPTY_UPDATE_REPORT = DOCUMENT_BUILDER.parse(SaverContextFactory.class.getResourceAsStream("updateReport.xml")); //$NON-NLS-1$
        } catch (Exception e) {
            throw new RuntimeException("Could not parse update report skeleton document.", e); //$NON-NLS-1$
        }
    }

    /**
     * Creates a {@link DocumentSaverContext} for bulk load operations.
     *
     *
     * @param dataCluster    Data container name (must exist).
     * @param dataModelName  Data model name (must exist).
     * @param keyMetadata    Key for all records contained in <code>documentStream</code>
     * @param documentStream AData model name (must exist).
     * @param loadAction     {@link com.amalto.core.load.action.LoadAction} to be used to bulk load records.
     * @param server         Abstraction of the underlying MDM database.
     * @return A context configured for bulk load.
     */
    public DocumentSaverContext createBulkLoad(String dataCluster,
                                               String dataModelName,
                                               XSDKey keyMetadata,
                                               InputStream documentStream,
                                               LoadAction loadAction,
                                               XmlServer server) {
        return new BulkLoadContext(dataCluster, dataModelName, keyMetadata, documentStream, loadAction, server);
    }

    /**
     * Creates a {@link DocumentSaverContext} to save a unique record in MDM.
     *
     * @param dataCluster    Data container name (must exist).
     * @param dataModelName  Data model name (must exist).
     * @param isReplace      <code>true</code> to replace XML document if it exists in database, <code>false</code>
     *                       otherwise. If it is a creation, this parameter is ignored.
     * @param documentStream A stream that contains one XML document.
     * @return A context configured to save a record in MDM.
     */
    public DocumentSaverContext create(String dataCluster,
                                       String dataModelName,
                                       boolean isReplace,
                                       InputStream documentStream) {
        return create(dataCluster,
                dataModelName,
                StringUtils.EMPTY,
                documentStream,
                isReplace,
                true,
                false,
                false,
                XSystemObjects.DC_PROVISIONING.getName().equals(dataCluster));
    }

    /**
     * Creates a {@link DocumentSaverContext} to save a unique record in MDM, with update report/before saving options.
     *
     * @param dataCluster        Data container name (must exist).
     * @param dataModelName      Data model name (must exist).
     * @param changeSource       Source of change (for update report). Common values includes 'genericUI'...
     * @param documentStream     A stream that contains one XML document.
     * @param isReplace          <code>true</code> to replace XML document if it exists in database, <code>false</code>
     *                           otherwise. If it is a creation, this parameter is ignored.
     * @param validate           <code>true</code> to validate XML document before saving it, <code>false</code> otherwise.
     * @param updateReport       <code>true</code> to generate an update report, <code>false</code> otherwise.
     * @param invokeBeforeSaving <code>true</code> to invoke any existing before saving process, <code>false</code> otherwise.
     * @param autoCommit         <code>true</code> to perform a call to {@link SaverSession#end()} when a record is ready for save.
     * @return A context configured to save a record in MDM.
     */
    public DocumentSaverContext create(String dataCluster,
                                       String dataModelName,
                                       String changeSource,
                                       InputStream documentStream,
                                       boolean isReplace,
                                       boolean validate,
                                       boolean updateReport,
                                       boolean invokeBeforeSaving,
                                       boolean autoCommit) {
        if (invokeBeforeSaving && !updateReport) {
            throw new IllegalArgumentException("Must generate update report to invoke before saving."); //$NON-NLS-1$
        }
        Server server = ServerContext.INSTANCE.get();
        // Parsing
        MutableDocument userDocument;
        try {
            // Don't ignore talend internal attributes when parsing this document
            DocumentBuilder documentBuilder = new SkipAttributeDocumentBuilder(DOCUMENT_BUILDER, false);
            InputSource source = new InputSource(documentStream);
            Document userDomDocument = documentBuilder.parse(source);
            final MetadataRepositoryAdmin admin = server.getMetadataRepositoryAdmin();
            String typeName = userDomDocument.getDocumentElement().getNodeName();
            MetadataRepository repository;
            if (dataModelName.startsWith("amaltoOBJECTS") || !admin.exist(dataModelName)) {
                final Storage systemStorage = server.getStorageAdmin().get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM);
                final MetadataRepository systemRepository = systemStorage.getMetadataRepository();
                if (systemRepository.getComplexType(typeName) != null) {
                    // Record to save is a system object!
                    return new DirectWriteContext(dataCluster, Util.nodeToString(userDomDocument));
                } else {
                    throw new IllegalArgumentException("Data model '" + dataModelName + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else {
                repository = admin.get(dataModelName);
            }
            ComplexTypeMetadata type = repository.getComplexType(typeName);
            if (type == null) {
                throw new IllegalArgumentException("Type '" + typeName + "' does not exist in data model '" + dataModelName + "'."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            userDocument = new DOMDocument(userDomDocument.getDocumentElement(), type, dataCluster, dataModelName);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse document to save.", e); //$NON-NLS-1$
        }
       return create(dataCluster, dataModelName, changeSource, userDocument, isReplace, validate, updateReport, invokeBeforeSaving, autoCommit);
    }

    /**
     * Creates a {@link DocumentSaverContext} to save a unique record in MDM, with update report/before saving options.
     * This method is dedicated to partial update (with the <code>overwrite</code> parameter).
     *
     * @param dataCluster    Data container name (must exist).
     * @param dataModelName  Data model name (must exist).
     * @param changeSource   Source of change (for update report). Common values includes 'genericUI'...
     * @param documentStream A stream that contains one XML document.
     * @param validate       <code>true</code> to validate XML document before saving it, <code>false</code> otherwise.
     * @param updateReport   <code>true</code> to generate an update report, <code>false</code> otherwise.
     * @param pivot          XPath to be used when iterating over a many valued element.
     * @param key            XPath to uniquely identify a element within the many valued element reachable from <code>pivot</code>.
     * @param overwrite      <code>false</code> will preserve all collections values in original document (new values
     *                       will be added at the end of the collection). <code>true</code> will overwrite all previous
     * @param delete         <code>true</code> will execute partial delete. <code>false</code> will execute normal partial update
     */
    public DocumentSaverContext createPartialUpdate(String dataCluster,
                                                    String dataModelName,
                                                    String changeSource,
                                                    InputStream documentStream,
                                                    boolean validate,
                                                    boolean updateReport,
                                                    String pivot,
                                                    String key,
                                                    int index,
                                                    boolean overwrite,
                                                    boolean delete) {
        // TODO Support before saving in case of partial update (set to "true" beforeSaving parameter to support it).
        DocumentSaverContext context = create(dataCluster,
                dataModelName,
                changeSource,
                documentStream,
                false, // Never do a "replace" when doing a partial update.
                validate,
                updateReport,
                false, XSystemObjects.DC_PROVISIONING.getName().equals(dataCluster)); // Before saving is not supported
        return PartialUpdateSaverContext.decorate(context, pivot, key, index, overwrite, delete);
    }
    
    public DocumentSaverContext createPartialUpdate(String dataCluster, 
                                                    String dataModelName, 
                                                    String changeSource,
                                                    InputStream documentStream, 
                                                    boolean validate, 
                                                    boolean updateReport, 
                                                    String pivot, 
                                                    String key, 
                                                    int index,
                                                    boolean overwrite) {
        return createPartialUpdate(dataCluster, dataModelName, changeSource, documentStream, validate, updateReport, pivot, key,
                index, overwrite, false);
    }

    /**
     * Creates a {@link DocumentSaverContext} to save a unique record in MDM, with update report/before saving options.
     *
     * @param dataCluster        Data container name (must exist).
     * @param dataModelName      Data model name (must exist).
     * @param changeSource       Source of change (for update report). Common values includes 'genericUI'...
     * @param userDocument       A document that contains the user-provided document for update.
     * @param isReplace          <code>true</code> to replace XML document if it exists in database, <code>false</code>
     *                           otherwise. If it is a creation, this parameter is ignored.
     * @param validate           <code>true</code> to validate XML document before saving it, <code>false</code> otherwise.
     * @param updateReport       <code>true</code> to generate an update report, <code>false</code> otherwise.
     * @param invokeBeforeSaving <code>true</code> to invoke any existing before saving process, <code>false</code> otherwise.
     * @param autoCommit         <code>true</code> to perform a call to {@link SaverSession#end()} when a record is ready for save.
     * @return A context configured to save a record in MDM.
     */
    public DocumentSaverContext create(String dataCluster,
                                           String dataModelName,
                                           String changeSource,
                                           MutableDocument userDocument,
                                           boolean isReplace,
                                           boolean validate,
                                           boolean updateReport,
                                           boolean invokeBeforeSaving,
                                           boolean autoCommit) {
        if (invokeBeforeSaving && !updateReport) {
            throw new IllegalArgumentException("Must generate update report to invoke before saving."); //$NON-NLS-1$
        }
        Server server = ServerContext.INSTANCE.get();
        // Choose right user action
        UserAction userAction = UserAction.UPDATE;
        if (isReplace) {
            userAction = UserAction.REPLACE;
        }
        // TMDM-5587: workflow uses 'update' for both 'update' and 'create' (so choose 'auto').
        if ("workflow".equalsIgnoreCase(changeSource)) { //$NON-NLS-1$
            userAction = UserAction.AUTO;
        }
        // Choose right context implementation
        DocumentSaverContext context;
        StorageAdmin storageAdmin = server.getStorageAdmin();
        Storage storage = storageAdmin.get(dataCluster, storageAdmin.getType(dataCluster));
        //TMDM-6316: disable update report generation & before Checking for Staging data operation: creation,update
        if (dataCluster.endsWith(StorageAdmin.STAGING_SUFFIX)) {
            invokeBeforeSaving = false;
            updateReport = false;
        }
        context = new StorageSaver(storage, dataModelName, userDocument, userAction, invokeBeforeSaving, updateReport, validate);
        // Additional options (update report, auto commit).
        if (updateReport) {
            context = ReportDocumentSaverContext.decorate(context, changeSource);
        }
        if (autoCommit) {
            context = AutoCommitSaverContext.decorate(context);
        }
        return context;
    }
    
    /**
     * Creates a {@link DocumentSaverContext} to validate a unique record in MDM.
     *
     * @param dataCluster    Data container name (must exist).
     * @param dataModelName  Data model name (must exist).
     * @param documentStream A stream that contains one XML document.
     * @return A context configured to validate a record in MDM.
     */
    public DocumentSaverContext createValidation(String dataCluster, String dataModelName, boolean invokeBeforeSaving, InputStream documentStream) {
        Server server = ServerContext.INSTANCE.get();
        // Parsing
        MutableDocument userDocument;
        try {
            // Don't ignore talend internal attributes when parsing this document
            DocumentBuilder documentBuilder = new SkipAttributeDocumentBuilder(DOCUMENT_BUILDER, false);
            InputSource source = new InputSource(documentStream);
            Document userDomDocument = documentBuilder.parse(source);
            final MetadataRepositoryAdmin admin = server.getMetadataRepositoryAdmin();
            String typeName = userDomDocument.getDocumentElement().getNodeName();
            MetadataRepository repository;
            if (!admin.exist(dataModelName)) {
                throw new IllegalArgumentException("Data model '" + dataModelName + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                repository = admin.get(dataModelName);
            }
            ComplexTypeMetadata type = repository.getComplexType(typeName);
            if (type == null) {
                throw new IllegalArgumentException("Type '" + typeName + "' does not exist in data model '" + dataModelName + "'."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            userDocument = new DOMDocument(userDomDocument.getDocumentElement(), type, dataCluster, dataModelName);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse document to save.", e); //$NON-NLS-1$
        }
        
        StorageAdmin storageAdmin = server.getStorageAdmin();
        Storage storage = storageAdmin.get(dataCluster, storageAdmin.getType(dataCluster));
        DocumentSaverContext context = new RecordValidationContext(storage, dataModelName, UserAction.REPLACE, invokeBeforeSaving, userDocument);
        return context;
    }
}
