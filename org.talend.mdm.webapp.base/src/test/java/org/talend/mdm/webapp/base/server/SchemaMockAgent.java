/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.talend.mdm.commmon.util.datamodel.management.BusinessConcept;
import org.talend.mdm.commmon.util.datamodel.management.DataModelBean;
import org.talend.mdm.commmon.util.datamodel.management.DataModelID;
import org.talend.mdm.commmon.util.datamodel.management.ReusableType;

import com.amalto.webapp.core.dmagent.SchemaAbstractWebAgent;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class SchemaMockAgent extends SchemaAbstractWebAgent {

    private String dataModelSchema;

    private LinkedHashMap<DataModelID, DataModelBean> map;

    private DataModelID dataModelID;

    public SchemaMockAgent(String dataModelSchema, DataModelID dataModelID) {
        this.dataModelSchema = dataModelSchema;
        this.map = new LinkedHashMap<DataModelID, DataModelBean>();
        this.dataModelID = dataModelID;
    }

    @Override
    protected boolean existInPool(DataModelID dataModelID) {
        DataModelBean dataModelBean = map.get(dataModelID);
        return !(dataModelBean == null);
    }

    @Override
    protected void removeFromPool(DataModelID dataModelID) {
        map.remove(dataModelID);
    }

    @Override
    protected void addToPool(DataModelID dataModelID, DataModelBean dataModelBean) {
        map.put(dataModelID, dataModelBean);
    }

    @Override
    protected DataModelBean getFromPool(DataModelID dataModelID) throws Exception {
        if (map.get(dataModelID) != null) {
            return map.get(dataModelID);
        }

        DataModelBean dmBean = instantiateDataModelBean(dataModelSchema);
        map.put(dataModelID, dmBean);

        return dmBean;
    }

    @Override
    public List<ReusableType> getMySubtypes(String parentTypeName) throws Exception {
        return getMySubtypes(parentTypeName, false, getFromPool(dataModelID));
    }

    @Override
    public List<ReusableType> getMySubtypes(String parentTypeName, boolean deep) throws Exception {
        return getMySubtypes(parentTypeName, deep, getFromPool(dataModelID));
    }

    @Override
    public ReusableType getReusableType(String typeName) throws Exception {
        return getReusableType(typeName, dataModelID);
    }

    @Override
    public BusinessConcept getBusinessConcept(String conceptName) throws Exception {
        BusinessConcept businessConcept = getBusinessConcept(conceptName, dataModelID);
        businessConcept.load();
        return businessConcept;
    }

    @Override
    public List<BusinessConcept> getAllBusinessConcepts() throws Exception {
        return getBusinessConcepts(dataModelID);
    }

    public List<ReusableType> getMyParents(String subTypeName) throws Exception {
        DataModelBean dataModelBean = getFromPool(dataModelID);
        List<ReusableType> reusableTypes = dataModelBean.getReusableTypes();
        List<ReusableType> parentsTypes = new ArrayList<ReusableType>();
        getParents(subTypeName, reusableTypes, parentsTypes);
        return parentsTypes;
    }

    private void getParents(String subTypeName, List<ReusableType> reusableTypes, List<ReusableType> parentsTypes) {
        for (ReusableType reusableType : reusableTypes) {
            if (reusableType.getName().equals(subTypeName)) {
                String parentName = reusableType.getParentName();
                ReusableType type = findParentType(parentName, reusableTypes);
                if (type != null) {
                    parentsTypes.add(type);
                    getParents(parentName, reusableTypes, parentsTypes);
                }
            }
        }
    }

    private ReusableType findParentType(String parName, List<ReusableType> reusableTypes) {
        ReusableType reUsable = null;
        for (ReusableType reusableType : reusableTypes) {
            if (reusableType.getName().equals(parName)) {
                reUsable = reusableType;
                break;
            }
        }
        return reUsable;
    }

    public boolean isPolymorphismTypeFK(String fk) throws Exception {
        // Polymorphism FK
        boolean isPolymorphismFK = false;
        BusinessConcept businessConcept = getBusinessConcept(fk);
        if (businessConcept != null) {
            String fkReusableType = businessConcept.getCorrespondTypeName();
            if (fkReusableType != null) {
                List<ReusableType> subTypes = this.getMySubtypes(fkReusableType, true);
                List<ReusableType> parentTypes = this.getMyParents(fkReusableType);
                isPolymorphismFK = subTypes.size() > 0 || parentTypes.size() > 0;
            }
        }
        return isPolymorphismFK;
    }

}
