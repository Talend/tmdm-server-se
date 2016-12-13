/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.datamodel;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.amalto.commons.core.datamodel.synchronization.DataModelChangeNotifier;
import com.amalto.core.metadata.LongString;
import com.amalto.core.query.user.Expression;
import com.amalto.core.storage.StorageType;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.compare.Change;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.commons.core.datamodel.synchronization.DMUpdateEvent;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.util.XtentisException;

public class DataModelPOJO extends ObjectPOJO{

    private static final Logger LOGGER = Logger.getLogger(DataModelPOJO.class);

    private static final Map<String, XSystemObjects> SYSTEM_OBJECTS = XSystemObjects.getXSystemObjects(XObjectType.DATA_MODEL);

    private static final boolean ENABLE_MODEL_UPDATE_CHECKS = false; // TODO Temp: aimed to be enabled.

    private String name;

    private String description;

    private String schema;

    /**
     * 
     */
    public DataModelPOJO() {
    }    
	public DataModelPOJO(String name) {
		super();
		this.name = name;
	}
	public DataModelPOJO(String name, String desc,String schema){
		this.name=name;
		this.description=desc;
		this.schema=schema;
	}

	/**
	 * @return Returns the Name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	
	/**
	 * @return Returns the Description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return the xsd Schema
	 */
    @LongString
	public String getSchema() {
		return schema;		
	}
	
	public void setSchema(String schema) {
		this.schema = schema;
	}
	
	
	@Override
	public ObjectPOJOPK getPK() {
		if (getName()==null) return null;
		return new ObjectPOJOPK(new String[] {name});
	}
	

    @Override
    public ObjectPOJOPK store() throws XtentisException {
        String previousSchema = getSchema();
        ObjectPOJOPK objectPK = super.store();
        // TMDM-4621: Update operation has to be synchronous
        String updatedDataModelName = getPK().getUniqueId();
        if (isUserDataModel(updatedDataModelName)) { // Do not update system data clusters
            Server server = ServerContext.INSTANCE.get();
            MetadataRepositoryAdmin metadataRepositoryAdmin = server.getMetadataRepositoryAdmin();
            metadataRepositoryAdmin.remove(updatedDataModelName);
            StorageAdmin storageAdmin = server.getStorageAdmin();
            Storage storage = storageAdmin.get(updatedDataModelName, storageAdmin.getType(updatedDataModelName));
            // Storage already exists so update it.
            MetadataRepository repository = metadataRepositoryAdmin.get(updatedDataModelName);
            // Check if data model update is possible without issues
            if (ENABLE_MODEL_UPDATE_CHECKS) {
                Compare.DiffResults diffResults = Compare.compare(storage.getMetadataRepository(), repository);
                ImpactAnalyzer impactAnalyzer = storage.getImpactAnalyzer();
                Map<ImpactAnalyzer.Impact, List<Change>> impacts = impactAnalyzer.analyzeImpacts(diffResults);
                List<Change> changes = impacts.get(ImpactAnalyzer.Impact.HIGH);
                if (!changes.isEmpty()) {
                    // Restore previous version
                    setSchema(previousSchema);
                    super.store();
                    throw new IllegalArgumentException("Can not update '" + updatedDataModelName
                            + "': " + changes.size()
                            + " changes cause storage issues.");
                }
            }
            Set<Expression> indexedExpressions = metadataRepositoryAdmin.getIndexedExpressions(updatedDataModelName);
            storage.prepare(repository, indexedExpressions, true, false);

            if (storageAdmin.supportStaging(updatedDataModelName)) {
                Storage stagingStorage = storageAdmin.get(updatedDataModelName, StorageType.STAGING);
                if (stagingStorage != null) {
                    // Storage already exists so update it.
                    MetadataRepository stagingRepository = metadataRepositoryAdmin.get(updatedDataModelName);
                    stagingStorage.prepare(stagingRepository, indexedExpressions, true, false);
                } else {
                    LOGGER.warn("No SQL staging storage defined for data model '" + updatedDataModelName //$NON-NLS-1$
                            + "'. No SQL staging storage to update."); //$NON-NLS-1$
                }
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Storage '" + updatedDataModelName + "' does not support staging (forbidden by storage admin).");
                }
            }
        }
        // synchronize with outer agents
        DataModelChangeNotifier dmUpdateEventNotifier = DataModelChangeNotifier.createInstance();
        dmUpdateEventNotifier.notifyChange(new DMUpdateEvent(getPK().getUniqueId()));
        return objectPK;
    }

    public static boolean isUserDataModel(String updatedDataModelName) {
        return !SYSTEM_OBJECTS.containsKey(updatedDataModelName)
                || XSystemObjects.DC_UPDATE_PREPORT.getName().equals(updatedDataModelName)
                || XSystemObjects.DC_CROSSREFERENCING.getName().equals(updatedDataModelName);
    }
}
