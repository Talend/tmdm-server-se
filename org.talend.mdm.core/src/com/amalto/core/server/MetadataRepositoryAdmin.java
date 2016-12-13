/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;


import com.amalto.core.query.user.Expression;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import java.util.Set;

/**
 * Holds all administration tasks for handling metadata.
 */
public interface MetadataRepositoryAdmin {
    /**
     * Returns a {@link MetadataRepository} instance for the MDM data model. If data model exists but no {@link MetadataRepository}
     * has been built for this data model, this method implicitly creates a new {@link MetadataRepository}.
     *
     * @param metadataRepositoryId A non-null data model name.
     * @return The {@link MetadataRepository} instance for the data model named <code>metadataRepositoryId</code> or <code>null</code>
     *         if data model doesn't exist.
     * @throws IllegalArgumentException If data model name is null.
     */
    MetadataRepository get(String metadataRepositoryId);

    /**
     * Removes the {@link MetadataRepository} instance registered for the data model named <code>metadataRepositoryId</code>.
     * If no metadata was built for <code>metadataRepositoryId</code>, this method is a no op.
     *
     * @param metadataRepositoryId A non-null data model name.
     * @throws IllegalArgumentException If data model name is null.
     */
    void remove(String metadataRepositoryId);

    /**
     * Forces to refresh {@link MetadataRepository} associated with <code>metadataRepositoryId</code> with data model schema
     * stored in MDM.
     *
     * @param metadataRepositoryId A non-null data model name.
     * @throws IllegalArgumentException If data model name is null.
     */
    void update(String metadataRepositoryId);

    /**
     * @param metadataRepositoryId A non-null data model name.
     * @return <code>true</code> if a {@link MetadataRepository} instance has already been instantiated for the data model
     *         <code>metadataRepositoryId</code>, <code>false</code> otherwise.
     * @throws IllegalArgumentException If data model name is null.
     */
    boolean exist(String metadataRepositoryId);

    /**
     * @param dataModelName A non-null data model name.
     * @return A {@link Set} of {@link Expression} that are likely to be used for search and sort.
     */
    Set<Expression> getIndexedExpressions(String dataModelName);

    /**
     * Releases all resources used by this {@link MetadataRepositoryAdmin}.
     */
    void close();
}
