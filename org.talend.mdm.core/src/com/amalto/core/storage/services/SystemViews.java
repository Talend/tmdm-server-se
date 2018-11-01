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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.objects.view.ViewPOJOPK;
import com.amalto.core.save.MultiRecordsSaveException;
import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.api.View;
import com.amalto.core.storage.exception.ConstraintViolationException;
import com.amalto.core.util.Util;
import com.amalto.core.util.ValidateException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.codehaus.jettison.json.JSONObject;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.codehaus.jettison.json.JSONArray;

@SuppressWarnings("nls")
@Path("/system/views")
@Api(value="Views management", tags="Administration")
public class SystemViews {

    private static final Logger LOGGER = Logger.getLogger(SystemViews.class);

    private static final String NODE_NAME = "name";

    private static final String NODE_DESCRIPTION = "description";

    private static final String NODE_DATA_MODEL_ID = "dataModelId";

    private static final String DEFAULT_VIEW_PREFIX = "Browse_items";

    public SystemViews() {
    }

    @GET
    @ApiOperation("Get all user views")
    public Response getAllUserViews() {
        try {
            JSONArray viewObjectArray = new JSONArray();
            View viewCtrlLocal = Util.getViewCtrlLocal();
            List<String> dataModelNames = getDataModelNames();
            MetadataRepositoryAdmin metadataRepositoryAdmin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
            for (ViewPOJOPK viewPOJOPK : viewCtrlLocal.getViewPKs(".*")) {
                ViewPOJO viewPOJO = ObjectPOJO.load(ViewPOJO.class, viewPOJOPK);
                String viewName = viewPOJO.getName();
                String entityName = getEntityNameByViewName(viewName);
                String dataModelName = getDataModelNameByEntityName(metadataRepositoryAdmin, dataModelNames, entityName);
                if (dataModelName.isEmpty()) {
                    continue;
                }
                JSONObject viewObject = new JSONObject();
                viewObject.put(NODE_NAME, viewName);
                viewObject.put(NODE_DESCRIPTION, viewPOJO.getDescription());
                viewObject.put(NODE_DATA_MODEL_ID, dataModelName);
                viewObjectArray.put(viewObject);
            }
            return Response.ok(viewObjectArray.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            return getErrorResponse(e, "Could not get all user views.");
        }
    }

    private String getEntityNameByViewName(String viewName) {
        return viewName.replaceAll(DEFAULT_VIEW_PREFIX + "_", StringUtils.EMPTY).replaceAll("#.*", StringUtils.EMPTY);
    }

    private String getDataModelNameByEntityName(MetadataRepositoryAdmin metadataRepositoryAdmin, List<String> dataModelNames, String entityName) {
        try {
            for (String dataModelName : dataModelNames) {
                MetadataRepository repository = metadataRepositoryAdmin.get(dataModelName);
                if (null != repository.getComplexType(entityName)) {
                    return dataModelName;
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to get data model name by entity name.", e);
        }
        return StringUtils.EMPTY;
    }

    private List<String> getDataModelNames() {
        List<String> validDataModelNames = new ArrayList<>();
        try {
            Collection<DataModelPOJOPK> allDataModelPOJOPKs = Util.getDataModelCtrlLocal().getDataModelPKs(".*");
            Map<String, XSystemObjects> xDataClustersMap = XSystemObjects.getXSystemObjects(XObjectType.DATA_MODEL);
            for (DataModelPOJOPK dataModelPOJOPK : allDataModelPOJOPKs) {
                String dataModelName = dataModelPOJOPK.getUniqueId();
                // XML Schema's schema is not aimed to be checked.
                if (!"XMLSCHEMA---".equals(dataModelName) && !xDataClustersMap.containsKey(dataModelName)) {
                    validDataModelNames.add(dataModelName);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get data model names.", e);
        }
        return validDataModelNames;
    }

    private Response getErrorResponse(Throwable e, String message) {
        String responseMessage = message == null ? e.getMessage() : message;
        if (e instanceof ConstraintViolationException) {
            LOGGER.warn(responseMessage, e);
            return Response.status(Response.Status.FORBIDDEN).entity(responseMessage).build();
        } else if (e instanceof XMLStreamException
                || e instanceof IllegalArgumentException || e instanceof MultiRecordsSaveException
                || (e.getCause() != null && (e.getCause() instanceof IllegalArgumentException
                || e.getCause() instanceof IllegalStateException || e.getCause() instanceof ValidateException))) {
            LOGGER.warn(responseMessage, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(responseMessage).build();
        } else {
            LOGGER.error(responseMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseMessage).build();
        }
    }
}