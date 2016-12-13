/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.bizhelpers;

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
        if (map.get(dataModelID) != null)
            return map.get(dataModelID);

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
        return getBusinessConcept(conceptName, dataModelID);
    }

    @Override
    public List<BusinessConcept> getAllBusinessConcepts() throws Exception {
        return getBusinessConcepts(dataModelID);
    }

}
