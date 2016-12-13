/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.dwr;

import java.util.*;

import com.amalto.core.server.ServerContext;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.webapp.core.util.Util;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSGetDataModel;
import com.amalto.core.webservice.WSRegexDataClusterPKs;
import com.amalto.core.webservice.WSRegexDataModelPKs;
import com.sun.xml.xsom.XSElementDecl;

/**
 * 
 * @author asaintguilhem
 *
 */

public class CommonDWR {

    private static final Logger LOGGER = Logger.getLogger(CommonDWR.class);

    public static String[] getClusters() {
        try {
            WSDataClusterPK[] wsDataClustersPK = Util.getPort().getDataClusterPKs(new WSRegexDataClusterPKs("*")).getWsDataClusterPKs();
            ArrayList<String> list = new ArrayList<String>();
            Map<String, XSystemObjects> xDataClustersMap = XSystemObjects.getXSystemObjects(XObjectType.DATA_CLUSTER);
            for (WSDataClusterPK aWsDataClustersPK : wsDataClustersPK) {
                if (!XSystemObjects.isXSystemObject(xDataClustersMap, aWsDataClustersPK.getPk())) {
                    list.add(aWsDataClustersPK.getPk());
                }
            }
            return list.toArray(new String[list.size()]);
        } catch (Exception e) {
            LOGGER.error("Unable to get clusters.", e);
            return null;
        }
    }

    public static String[] getModels() {
        try {
            WSDataModelPK[] wsDataModelsPK = Util.getPort().getDataModelPKs(new WSRegexDataModelPKs("*")).getWsDataModelPKs();
            ArrayList<String> list = new ArrayList<String>();
            Map<String, XSystemObjects> xDataModelsMap = XSystemObjects.getXSystemObjects(XObjectType.DATA_MODEL);
            for (WSDataModelPK aWsDataModelsPK : wsDataModelsPK) {
                if (!XSystemObjects.isXSystemObject(xDataModelsMap, aWsDataModelsPK.getPk())) {
                    list.add(aWsDataModelsPK.getPk());
                }
            }
            return list.toArray(new String[list.size()]);
        } catch (Exception e) {
            LOGGER.error("Unable to get models.", e);
            return null;
        }
    }

    public static String getConceptLabel(String dataModelPK, String concept, String language) throws Exception {
        MetadataRepository repository = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin().get(dataModelPK);
        ComplexTypeMetadata type = repository.getComplexType(concept);
        return type.getName(new Locale(language));
    }

    public static Map<String, XSElementDecl> getConceptMap(String dataModelPK) throws Exception {
        String xsd = Util.getPort().getDataModel(new WSGetDataModel(new WSDataModelPK(dataModelPK))).getXsdSchema();
        return com.amalto.core.util.Util.getConceptMap(xsd);
    }

}
