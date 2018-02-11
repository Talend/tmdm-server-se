/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server.lifecycle.tomcat;

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.io.ByteArrayInputStream;
import java.util.*;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;

class TomcatMetadataRepositoryAdmin implements MetadataRepositoryAdmin {

    private final Map<String, MetadataRepository> metadataRepository = new HashMap<String, MetadataRepository>();

    public MetadataRepository get(String metadataRepositoryId) {
        synchronized (metadataRepository) {
            MetadataRepository repository = metadataRepository.get(metadataRepositoryId);
            if (repository == null) {
                repository = new MetadataRepository();
                Server server = ServerContext.INSTANCE.get();
                StorageAdmin storageAdmin = server.getStorageAdmin();
                Storage systemStorage = storageAdmin.get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM);
                String dataModelTypeName = ClassRepository.format("DataModelPOJO"); //$NON-NLS-1$
                ComplexTypeMetadata dataModelType = systemStorage.getMetadataRepository().getComplexType(dataModelTypeName);
                UserQueryBuilder qb = from(dataModelType).where(eq(dataModelType.getField("name"), metadataRepositoryId)); //$NON-NLS-1$

                systemStorage.begin();
                try {
                    StorageResults dataModelInstance = systemStorage.fetch(qb.getSelect());
                    Iterator<DataRecord> iterator = dataModelInstance.iterator();
                    if (iterator.hasNext()) {
                        String schema = String.valueOf(iterator.next().get("schema")); //$NON-NLS-1$
                        repository.load(new ByteArrayInputStream(schema.getBytes("UTF-8"))); //$NON-NLS-1$
                        if (iterator.hasNext()) {
                            throw new IllegalArgumentException("Found more than one data model for name '" + metadataRepositoryId + "'.");
                        }
                    } else {
                        throw new IllegalArgumentException("Data model '" + metadataRepositoryId + "' does not exist");
                    }
                    metadataRepository.put(metadataRepositoryId, repository);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Could not read data model '" + metadataRepositoryId + "'", e);
                }
            }
            return repository;
        }
    }

    public void remove(String metadataRepositoryId) {
        metadataRepository.remove(metadataRepositoryId);
    }

    public void update(String metadataRepositoryId) {
        remove(metadataRepositoryId);
        get(metadataRepositoryId);
    }

    public void close() {
    }

    public Set<Expression> getIndexedExpressions(String dataModelName) {
        return Collections.emptySet();
    }

    public boolean exist(String metadataRepositoryId) {
        return true;
    }
}
