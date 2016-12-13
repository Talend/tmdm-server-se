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

import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.query.user.UserQueryHelper;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.IWhereItem;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.server.api.DataModel;
import com.amalto.core.server.api.View;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

import static com.amalto.core.query.user.UserQueryBuilder.from;
import static com.amalto.core.query.user.UserQueryBuilder.isNull;

class MetadataRepositoryAdminImpl implements MetadataRepositoryAdmin {

    private static final Logger LOGGER = Logger.getLogger(MetadataRepositoryAdminImpl.class);

    private final Map<String, MetadataRepository> metadataRepository = new HashMap<String, MetadataRepository>();

    private final DataModel dataModelControl;

    MetadataRepositoryAdminImpl() {
        try {
            dataModelControl = Util.getDataModelCtrlLocal();
            Collection<DataModelPOJOPK> allDataModelNames = dataModelControl.getDataModelPKs(".*"); //$NON-NLS-1$
            Map<String, XSystemObjects> xDataClustersMap = XSystemObjects.getXSystemObjects(XObjectType.DATA_MODEL);
            for (DataModelPOJOPK dataModelName : allDataModelNames) {
                String id = dataModelName.getUniqueId();
                // XML Schema's schema is not aimed to be parsed.
                if (!"XMLSCHEMA---".equals(id) && !xDataClustersMap.containsKey(id)) { //$NON-NLS-1$
                    try {
                        get(id);
                    } catch (Exception e) {
                        LOGGER.error("Initialization error occurred during initialization of '" + id + "'. Storage might not be available.");
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        synchronized (metadataRepository) {
            Set<String> strings = new HashSet<>(metadataRepository.keySet());
            for (String repositoryId : strings) {
                remove(repositoryId);
            }
        }
    }

    public Set<Expression> getIndexedExpressions(String dataModelName) {
        if(XSystemObjects.DM_UPDATEREPORT.getName().equals(dataModelName)) { // Indexed expressions for UpdateReport
            MetadataRepository repository = get(dataModelName);
            // Index Concept field (used in JournalStatistics for the top N types)
            ComplexTypeMetadata updateType = repository.getComplexType("Update"); //$NON-NLS-1$
            return Collections.singleton(from(updateType).where(isNull(updateType.getField("Concept"))).getExpression()); //$NON-NLS-1$
        }
        synchronized (metadataRepository) {
            ViewPOJO view = null;
            try {
                MetadataRepository repository = get(dataModelName);
                View viewCtrlLocal = Util.getViewCtrlLocal();
                Set<Expression> indexedExpressions = new HashSet<Expression>();
                for (Object viewAsObject : viewCtrlLocal.getAllViews(".*")) { //$NON-NLS-1$
                    UserQueryBuilder qb = null;
                    view = (ViewPOJO) viewAsObject;
                    ArrayList<String> searchableElements = view.getSearchableBusinessElements().getList();
                    for (String searchableElement : searchableElements) {
                        String typeName = StringUtils.substringBefore(searchableElement, "/"); //$NON-NLS-1$
                        ComplexTypeMetadata userType = repository.getComplexType(typeName);
                        if (userType != null) {
                            if (qb == null) {
                                qb = from(userType);
                            } else {
                                qb.and(userType);
                            }
                            String fieldName = StringUtils.substringAfter(searchableElement, "/"); //$NON-NLS-1$
                            if (userType.hasField(fieldName)) {
                                qb.where(UserQueryBuilder.isEmpty(userType.getField(fieldName)));
                            }
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("View '" + view.getPK().getUniqueId() + "' does not apply to data model '" + dataModelName + "'.");
                            }
                            break; // View does not apply to model
                        }
                    }
                    if (qb != null) {
                        for (IWhereItem condition : view.getWhereConditions().getList()) {
                            qb.where(UserQueryHelper.buildCondition(qb, condition, repository));
                        }
                        indexedExpressions.add(qb.getExpression());
                    }
                }
                return indexedExpressions;
            } catch (Exception e) {
                if (view != null) {
                    throw new RuntimeException("Can not use view '" + view.getPK().getUniqueId()
                            + "' with data model '" + dataModelName
                            + "': " + e.getMessage(), e);
                } else {
                    throw new RuntimeException("Could not get indexed fields.", e);
                }
            }
        }
    }

    public boolean exist(String metadataRepositoryId) {
        assertMetadataRepositoryId(metadataRepositoryId);
        return this.get(metadataRepositoryId) != null;
    }

    public MetadataRepository get(String metadataRepositoryId) {
        assertMetadataRepositoryId(metadataRepositoryId);
        synchronized (metadataRepository) {
            MetadataRepository repository = metadataRepository.get(metadataRepositoryId);
            if (repository == null) {
                try {
                    DataModelPOJOPK pk = new DataModelPOJOPK(StringUtils.substringBeforeLast(metadataRepositoryId, "#")); //$NON-NLS-1$
                    DataModelPOJO dataModel = loadDataModel(pk);
                    if (dataModel == null) {
                        return null; // Expected per interface documentation (if not found, return null).
                    }
                    repository = createMetadataRepository(dataModel);
                    metadataRepository.put(metadataRepositoryId, repository);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return repository;
        }
    }
    
    private MetadataRepository createMetadataRepository(DataModelPOJO dataModel) throws UnsupportedEncodingException{
        String schemaAsString = dataModel.getSchema();
        MetadataRepository repository = new MetadataRepository();
        if (schemaAsString != null && !schemaAsString.isEmpty()) {
            repository.load(new ByteArrayInputStream(schemaAsString.getBytes("UTF-8"))); //$NON-NLS-1$
        }
        return repository;
    }
    
    private DataModelPOJO loadDataModel(DataModelPOJOPK pk){
        try {
            return dataModelControl.existsDataModel(pk);
        } catch (XtentisException e) {
            throw new RuntimeException(e);
        }
    }

    public void remove(String metadataRepositoryId) {
        assertMetadataRepositoryId(metadataRepositoryId);
        synchronized (metadataRepository) {
            // get(String) implicitly loads the data model, not very interesting in remove(String) case.
            MetadataRepository repository = metadataRepository.get(metadataRepositoryId);
            if (repository != null) {
                repository.close();
                metadataRepository.remove(metadataRepositoryId);
                if (!metadataRepositoryId.endsWith(StorageAdmin.STAGING_SUFFIX)) {
                    // Remove staging metadata repository.
                    remove(metadataRepositoryId + StorageAdmin.STAGING_SUFFIX);
                }
            }
        }
    }

    public void update(String metadataRepositoryId) {
        assertMetadataRepositoryId(metadataRepositoryId);
        synchronized (metadataRepository) {
            remove(metadataRepositoryId);
            get(metadataRepositoryId);
        }
    }

    private static void assertMetadataRepositoryId(String metadataRepositoryId) {
        if (metadataRepositoryId == null) {
            throw new IllegalArgumentException("Metadata repository id cannot be null");
        }
    }
}
