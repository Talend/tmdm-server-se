/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.services;

import java.util.Collection;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.objects.view.ViewPOJOPK;
import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.api.View;
import com.amalto.core.util.Util;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.codehaus.jettison.json.JSONObject;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.codehaus.jettison.json.JSONArray;

@Path("/system/views")
@Api(value="Views management", tags="Administration")
public class SystemViews {

    private static final String NODE_NAME = "name"; //$NON-NLS-1$

    private static final String NODE_DESCRIPTION = "description"; //$NON-NLS-1$

    private static final String NODE_DATA_MODEL_ID = "dataModelId"; //$NON-NLS-1$

    private static final String DEFAULT_VIEW_PREFIX = "Browse_items";//$NON-NLS-1$

    public SystemViews() {
    }

    @GET
    @ApiOperation("Get all user views")
    public Response getAllUserViews() {
        try {
            JSONArray viewObjectArray = new JSONArray();
            View viewCtrlLocal = Util.getViewCtrlLocal();
            for (ViewPOJOPK viewPOJOPK : viewCtrlLocal.getViewPKs(".*")) { //$NON-NLS-1$
                ViewPOJO viewPOJO = ObjectPOJO.load(ViewPOJO.class, viewPOJOPK);
                JSONObject viewObject = new JSONObject();
                String viewName = viewPOJO.getName();
                viewObject.put(NODE_NAME, viewName);
                viewObject.put(NODE_DESCRIPTION, viewPOJO.getDescription());
                String entityName = getEntityNameByViewName(viewName);
                String dataModelName = getDataModelNameByEntityName(entityName);
                if (dataModelName.isEmpty()) {
                    continue;
                }
                viewObject.put(NODE_DATA_MODEL_ID, dataModelName);
                viewObjectArray.put(viewObject);
            }
            return Response.ok(viewObjectArray.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            throw new RuntimeException("Could not get all user views.", e); //$NON-NLS-1$
        }
    }

    private String getEntityNameByViewName(String viewName) {
        return viewName.replaceAll(DEFAULT_VIEW_PREFIX + "_", StringUtils.EMPTY).replaceAll("#.*", StringUtils.EMPTY);//$NON-NLS-1$ //$NON-NLS-2$
    }

    private String getDataModelNameByEntityName(String conceptName) {
        try {
            MetadataRepositoryAdmin metadataRepositoryAdmin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
            Collection<DataModelPOJOPK> allDataModelNames = Util.getDataModelCtrlLocal().getDataModelPKs(".*"); //$NON-NLS-1$
            Map<String, XSystemObjects> xDataClustersMap = XSystemObjects.getXSystemObjects(XObjectType.DATA_MODEL);
            for (DataModelPOJOPK dataModelPOJOPK : allDataModelNames) {
                String dataModelName = dataModelPOJOPK.getUniqueId();
                // XML Schema's schema is not aimed to be checked.
                if (!"XMLSCHEMA---".equals(dataModelName) && !xDataClustersMap.containsKey(dataModelName)) { //$NON-NLS-1$
                    MetadataRepository repository = metadataRepositoryAdmin.get(dataModelName);
                    if (null != repository.getComplexType(conceptName)) {
                        return dataModelName;
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not get all user views.", e); //$NON-NLS-1$
        }
        return StringUtils.EMPTY;
    }
}