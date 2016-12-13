/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.commmon.util.datamodel.management;

import java.util.LinkedHashMap;

public class SchemaAgentTestMock extends SchemaManager {

    private String dataModelSchema;

    private LinkedHashMap<DataModelID, DataModelBean> map;

    public SchemaAgentTestMock() {

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


}
