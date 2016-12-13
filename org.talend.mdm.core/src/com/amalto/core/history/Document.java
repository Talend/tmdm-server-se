/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.history;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

/**
 * Representation of a MDM document when browsing its history.
 */
public interface Document {
    /**
     * @return Returns the document as string (only the user's document, not the MDM specific XML header).
     */
    String exportToString();

    /**
     * Transforms the document into a new one. Instance passed as parameter of {@link DocumentTransformer} is <code>this</code>,
     * so any modification done to the document in the transformer is performed (i.e. transformation isn't necessarily
     * performed on a copy).
     *
     * @param transformer A {@link DocumentTransformer} implementation.
     * @return A document transformed by the transformer.
     */
    Document transform(DocumentTransformer transformer);

    /**
     * <p>
     * "Restore" this document to the MDM database. This means the current state of this document will become the new
     * current document version.
     * </p>
     * <p>
     * <b>Note:</b>The logged user must be admin to call this method.
     * </p>
     *
     * @throws IllegalStateException If the user is not an admin user.
     */
    void restore();

    /**
     * @return The type metadata information for this Document. This method should <b>NEVER</b> return <code>null</code>.
     */
    ComplexTypeMetadata getType();

    /**
     * @return The data model name in which type information is defined. A call to {@link com.amalto.core.server.MetadataRepositoryAdmin#get(String, com.amalto.core.storage.StorageType)}
     * <b>MUST</b> return a non-null value.
     * @see com.amalto.core.server.MetadataRepositoryAdmin
     */
    String getDataModel();

    /**
     * @return The name of the data cluster for this document. If document comes from staging area it <b>MUST</b> include
     * the staging suffix.
     * @see com.amalto.core.server.StorageAdmin#STAGING_SUFFIX
     */
    String getDataCluster();

    /**
     * @return The task id (or "group id") for this record. This is used to indicate the group of records during match
     * & merge (see staging area).
     */
    String getTaskId();

    boolean isDeleted();

    DeleteType getDeleteType();
}
