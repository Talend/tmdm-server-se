/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.server;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.query.user.Expression;

public class MockMetadataRepositoryAdmin implements MetadataRepositoryAdmin {

    public static MockMetadataRepositoryAdmin INSTANCE = new MockMetadataRepositoryAdmin();

    private static final Logger LOGGER = Logger.getLogger(MockMetadataRepositoryAdmin.class);

    private final Map<String, MetadataRepository> metadataRepository = new HashMap<String, MetadataRepository>();

    protected MockMetadataRepositoryAdmin() {
    }
    
    public void register(String metadataRepositoryId, MetadataRepository repository) {
        synchronized (metadataRepository) {
            metadataRepository.put(metadataRepositoryId, repository);
        }
    }

    public MetadataRepository get(String metadataRepositoryId) {
        synchronized (metadataRepository) {
            MetadataRepository repository = metadataRepository.get(metadataRepositoryId);

            if (repository == null) {
                repository = new MetadataRepository();
                InputStream resourceAsStream = this.getClass().getResourceAsStream("../query/metadata.xsd");
                if (resourceAsStream == null) {
                    String base = this.getClass().getResource(".").toString();
                    String fullFileName = base + "../query/metadata.xsd";
                    LOGGER.info("File " + fullFileName + " can not be found.");
                    return repository;
                }
                repository.load(resourceAsStream);
                metadataRepository.put(metadataRepositoryId, repository);
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
        synchronized (metadataRepository) {
            metadataRepository.clear();
        }
    }

    public Set<Expression> getIndexedExpressions(String dataModelName) {
        return Collections.emptySet();
    }

    public boolean exist(String metadataRepositoryId) {
        return true;
    }

    public Map<String, MetadataRepository> getMetadataRepository() {
        return metadataRepository;
    }
}
