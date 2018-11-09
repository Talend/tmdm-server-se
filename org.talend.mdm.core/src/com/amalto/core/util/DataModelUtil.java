/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.server.MetadataRepositoryAdmin;

public class DataModelUtil {

    private static final Logger LOGGER = Logger.getLogger(DataModelUtil.class);

    public static String getDataModelNameByEntityName(MetadataRepositoryAdmin metadataRepositoryAdmin,
            List<String> dataModelNames, String entityName) {
        try {
            for (String dataModelName : dataModelNames) {
                MetadataRepository repository = metadataRepositoryAdmin.get(dataModelName);
                if (null != repository.getComplexType(entityName)) {
                    return dataModelName;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get data model name by entity name.", e); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }

    public static List<String> getDataModelNames() {
        List<String> validDataModelNames = new ArrayList<>();
        try {
            Collection<DataModelPOJOPK> allDataModelPOJOPKs = Util.getDataModelCtrlLocal().getDataModelPKs(".*"); //$NON-NLS-1$
            Map<String, XSystemObjects> xDataClustersMap = XSystemObjects.getXSystemObjects(XObjectType.DATA_MODEL);
            for (DataModelPOJOPK dataModelPOJOPK : allDataModelPOJOPKs) {
                String dataModelName = dataModelPOJOPK.getUniqueId();
                // XML Schema's schema is not aimed to be checked.
                if (!"XMLSCHEMA---".equals(dataModelName) && !xDataClustersMap.containsKey(dataModelName)) { //$NON-NLS-1$
                    validDataModelNames.add(dataModelName);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data model names.", e); //$NON-NLS-1$
        }
        return validDataModelNames;
    }
}
