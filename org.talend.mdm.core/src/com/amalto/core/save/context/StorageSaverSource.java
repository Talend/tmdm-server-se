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

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.generator.AutoIncrementGenerator;
import com.amalto.core.save.generator.AutoIncrementUtil;
import com.amalto.core.schema.validation.XmlSchemaValidator;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.server.api.DataModel;
import com.amalto.core.server.api.RoutingEngine;
import com.amalto.core.servlet.LoadServlet;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.OutputReport;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

public class StorageSaverSource implements SaverSource {

    private final DataModel dataModel;

    private final String userName;

    private final Map<String, String> schemasAsString = new HashMap<String, String>();

    public StorageSaverSource() {
        this(null);
    }

    public StorageSaverSource(String userName) {
        try {
            dataModel = Util.getDataModelCtrlLocal();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.userName = userName;
    }

    private Expression buildQueryByID(Storage storage, String typeName, String[] key) {
        ComplexTypeMetadata type = storage.getMetadataRepository().getComplexType(typeName);
        UserQueryBuilder qb = from(type);
        Collection<FieldMetadata> keyFields = type.getKeyFields();
        int i = 0;
        for (FieldMetadata keyField : keyFields) {
            qb.where(eq(keyField, key[i++]));
        }
        return qb.getExpression();
    }

    @Override
    public MutableDocument get(String dataClusterName, String dataModelName, String typeName, String[] key) {
        Storage storage = getStorage(dataClusterName);
        StorageResults results = storage.fetch(buildQueryByID(storage, typeName, key));
        try {
            Iterator<DataRecord> iterator = results.iterator();
            if (!iterator.hasNext()) {
                return null;
            }
            DataRecord record = iterator.next();
            MetadataRepository repository = storage.getMetadataRepository();
            return new StorageDocument(dataClusterName, repository, record);
        } finally {
            results.close();
        }
    }

    private Storage getStorage(String dataClusterName) {
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        return storageAdmin.get(dataClusterName, storageAdmin.getType(dataClusterName));
    }

    @Override
    public boolean exist(String dataCluster, String dataModelName, String typeName, String[] key) {
        Storage storage = getStorage(dataCluster);
        if (storage == null) {
            return false;
        }
        ComplexTypeMetadata type = storage.getMetadataRepository().getComplexType(typeName);
        if (key.length < type.getKeyFields().size()) {
            return false;
        }
        StorageResults results = storage.fetch(buildQueryByID(storage, typeName, key)); // Expect a transaction to be active
        try {
            Iterator<DataRecord> iterator = results.iterator();
            return iterator.hasNext();
        } finally {
            results.close();
        }
    }

    @Override
    public MetadataRepository getMetadataRepository(String dataModelName) {
        Storage storage = getStorage(dataModelName);
        if (storage == null) {
            throw new IllegalArgumentException("No storage available for '" + dataModelName + "'.");
        }
        return storage.getMetadataRepository();
    }

    @Override
    public InputStream getSchema(String dataModelName) {
        try {
            synchronized (schemasAsString) {
                if (schemasAsString.get(dataModelName) == null) {
                    String schemaAsString = dataModel.getDataModel(new DataModelPOJOPK(dataModelName)).getSchema();
                    schemasAsString.put(dataModelName, schemaAsString);
                }
            }
            return new ByteArrayInputStream(schemasAsString.get(dataModelName).getBytes("UTF-8")); //$NON-NLS-1$
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public OutputReport invokeBeforeSaving(DocumentSaverContext context, MutableDocument updateReportDocument) {
        try {
            return Util.beforeSaving(context.getUserDocument().getType().getName(),
                    context.getDatabaseDocument().exportToStringWithNullFields(),
                    updateReportDocument.exportToString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Set<String> getCurrentUserRoles() {
        try {
            // get user roles from current user.
            return LocalUser.getLocalUser().getRoles();
        } catch (XtentisException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUserName() {
        try {
            return LocalUser.getLocalUser().getUsername();
        } catch (XtentisException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existCluster(String dataClusterName) {
        return true;
    }

    public void resetLocalUsers() {
        try {
            LocalUser.resetLocalUsers();
        } catch (XtentisException e) {
            throw new RuntimeException(e);
        }
    }

    public void initAutoIncrement() {
        AutoIncrementGenerator.get().init();
    }

    public void routeItem(String dataCluster, String typeName, String[] id) {
        try {
            RoutingEngine ctrl = Util.getRoutingEngineV2CtrlLocal();
            DataClusterPOJOPK dataClusterPOJOPK = new DataClusterPOJOPK(dataCluster);
            ctrl.route(new ItemPOJOPK(dataClusterPOJOPK, typeName, id));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void invalidateTypeCache(String dataModelName) {
        XmlSchemaValidator.invalidateCache(dataModelName);
        synchronized (schemasAsString) {
            schemasAsString.remove(dataModelName);
        }
        synchronized (LoadServlet.typeNameToKeyDef) {
            LoadServlet.typeNameToKeyDef.clear();
        }
    }

    public void saveAutoIncrement() {
        AutoIncrementGenerator.get().saveState(Util.getXmlServerCtrlLocal());
    }

    public String nextAutoIncrementId(String dataCluster, String dataModelName, String conceptName) {
        String autoIncrementId = null;
        String concept = AutoIncrementUtil.getConceptForAutoIncrement(dataModelName, conceptName);
        if(concept != null) {
            String autoIncrementFieldName = concept;
            if (conceptName.contains(".")) { //$NON-NLS-1$
                autoIncrementFieldName = conceptName.split("\\.")[1]; //$NON-NLS-1$
            }
            autoIncrementId = AutoIncrementGenerator.get().generateId(dataCluster, concept, autoIncrementFieldName);
        }
        return autoIncrementId;
    }

    public String getLegitimateUser() {
        // web service caller
        if (userName != null) {
            return userName;
        }
        return getUserName();
    }
}
