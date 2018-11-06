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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.objects.role.RolePOJO;
import com.amalto.core.objects.role.RolePOJOPK;
import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.objects.view.ViewPOJOPK;
import com.amalto.core.save.MultiRecordsSaveException;
import com.amalto.core.server.MetadataRepositoryAdmin;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.api.Role;
import com.amalto.core.server.api.View;
import com.amalto.core.storage.exception.ConstraintViolationException;
import com.amalto.core.storage.exception.ViewNotFoundException;
import com.amalto.core.util.LocaleUtil;
import com.amalto.core.util.RoleSpecification;
import com.amalto.core.util.Util;
import com.amalto.core.util.ValidateException;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;
import com.amalto.xmlserver.interfaces.WhereOr;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@SuppressWarnings("nls")
@Path("/system/views")
@Api(value="Views management", tags="Administration")
public class SystemViews {

    private static final Logger LOGGER = Logger.getLogger(SystemViews.class);

    private static final String NODE_NAME = "name";

    private static final String NODE_DESCRIPTION = "description";

    private static final String NODE_DATA_MODEL_ID = "dataModelId";

    private static final String NODE_ROLES = "roles";

    private static final String DEFAULT_VIEW_PREFIX = "Browse_items";

    private static final String DEFAULT_VIEW_ALL = "Browse_items_.*";

    private static final String VIEW_OBJECT_NAME = "View";

    private static final String SEARCHABLE_BUSINESS_ELEMENTS = "searchableBusinessElements";

    private static final String VIEWABLE_BUSINESS_ELEMENTS = "viewableBusinessElementsJson";

    private static final String WHERE_CONDITIONS = "whereConditions";

    private static final String SORT_FIELD = "sortField";

    private static final String CUSTOM_FORM = "customForm";

    private static final String IS_ASC = "isAsc";

    private static final String IS_TRANSFORMER_ACTIVE = "isTransformerActive";

    private static final String LEFT_PATH = "leftPath";

    private static final String OPERATOR = "operator";

    private static final String RIGHT_VALUE_OR_PATH = "rightValueOrPath";

    private static final String SPELL_CHECK = "spellCheck";

    private static final String STRING_PREDICATE = "stringPredicate";

    private List<Map<String, String>> whereConditions = new ArrayList<Map<String, String>>();

    public SystemViews() {
    }

    @GET
    @ApiOperation("Get all views with roles allowed to access. Only views that correspond to deployed data models are returned.")
    public Response getViewList(@ApiParam("Optional parameter of language to get localized result.")
                                @QueryParam("lang") String locale) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Request parameter lang = " + locale);
            }
            JSONArray viewObjectArray = new JSONArray();
            View viewCtrlLocal = Util.getViewCtrlLocal();
            List<String> dataModelNames = getDataModelNames();
            Map<String, Set<String>> viewRolesMap = getViewRolesMap();
            MetadataRepositoryAdmin metadataRepositoryAdmin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
            for (ViewPOJOPK viewPOJOPK : viewCtrlLocal.getViewPKs(".*")) {
                ViewPOJO viewPOJO = ObjectPOJO.load(ViewPOJO.class, viewPOJOPK);
                String viewName = viewPOJO.getName();
                String description = getDescriptionValue(viewPOJO.getDescription(), locale);
                String entityName = getEntityNameByViewName(viewName);
                String dataModelName = getDataModelNameByEntityName(metadataRepositoryAdmin, dataModelNames, entityName);
                if (dataModelName.isEmpty()) {
                    continue;
                }
                JSONObject viewObject = new JSONObject();
                viewObject.put(NODE_NAME, viewName);
                viewObject.put(NODE_DESCRIPTION, description);
                viewObject.put(NODE_DATA_MODEL_ID, dataModelName);
                viewObject.put(NODE_ROLES, viewRolesMap.get(viewName));
                viewObjectArray.put(viewObject);
            }
            return Response.ok(viewObjectArray.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            return getErrorResponse(e, "Could not get all views.");
        }
    }

    @GET
    @Path("{id}")
    @ApiOperation("Get view detail by view name and language.")
    public Response getViewDetail(@ApiParam("a special view name") @PathParam("id") String viewPK,
            @ApiParam("Optional parameter of language to get localized result.") @QueryParam("lang") String locale) {

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Request parameter id =" + viewPK + ", lang = " + locale);
        }

        MetadataRepositoryAdmin metadataRepositoryAdmin = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin();
        View viewCtrlLocal = Util.getViewCtrlLocal();
        ViewPOJOPK currentViewPOJOPK = new ViewPOJOPK(viewPK);
        ViewPOJO viewPOJO = null;
        try {
            viewPOJO = viewCtrlLocal.getView(currentViewPOJOPK);
        } catch (Exception e) {
            return getErrorResponse(new ViewNotFoundException(Response.Status.INTERNAL_SERVER_ERROR), "Error occurred while getting a special ViewPOJO with parameter viewId = " + viewPK);
        }

        if (viewPOJO == null) {
            return getErrorResponse(new ViewNotFoundException(Response.Status.NOT_FOUND), "View ID '" + viewPK + "' is not found.");
        }

        List<String> dataModelNames;
        try {
            dataModelNames = getDataModelNames();
        } catch (Exception e) {
            return getErrorResponse(e, "Failed to get data model names.");
        }
        String entityName = getEntityNameByViewName(viewPOJO.getName());
        String dataModelName = getDataModelNameByEntityName(metadataRepositoryAdmin, dataModelNames, entityName);

        viewPOJO.setDescription(getDescriptionValue(viewPOJO.getDescription(), locale));
        try {
            JSONObject result = populatedResult(viewPOJO, dataModelName);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("The result is " + result.toString());
            }
            return Response.ok(result.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (Exception e) {
            return getErrorResponse(e, "Could not get user view.");
        }
    }

    private String getDescriptionValue(String description, String locale) {
        if (!StringUtils.isEmpty(description)) {
            description = LocaleUtil.getLocaleValue(description, locale);
        }
        return description;
    }

    private JSONObject populatedResult(ViewPOJO viewPOJO, String dataModelName) throws JSONException {
        JSONObject viewObject = new JSONObject();
        viewObject.put(NODE_NAME, viewPOJO.getName());
        viewObject.put(NODE_DESCRIPTION, viewPOJO.getDescription());
        viewObject.put(NODE_DATA_MODEL_ID, dataModelName);

        JSONArray searchableBusinessElementsJson = new JSONArray();
        for (Iterator<String> iterator = viewPOJO.getSearchableBusinessElements().getList().iterator(); iterator.hasNext();) {
            String searchableItem = iterator.next();
            searchableBusinessElementsJson.put(searchableItem);
        }
        viewObject.put(SEARCHABLE_BUSINESS_ELEMENTS, searchableBusinessElementsJson);

        JSONArray viewableBusinessElementsJson = new JSONArray();
        for (Iterator<String> iterator = viewPOJO.getViewableBusinessElements().getList().iterator(); iterator.hasNext();) {
            String viewableItem = iterator.next();
            viewableBusinessElementsJson.put(viewableItem);
        }
        viewObject.put(VIEWABLE_BUSINESS_ELEMENTS, viewableBusinessElementsJson);

        ArrayList<IWhereItem> tempList = viewPOJO.getWhereConditions().getList();
        whereConditions.clear();
        formatWhereItem(tempList);

        JSONArray whereConditionsJson = new JSONArray();
        for (Iterator<Map<String, String>> iterator = whereConditions.iterator(); iterator.hasNext();) {
            Map<String, String> whereConditionItem = iterator.next();

            JSONObject whereConditionsObject = new JSONObject();
            for (Iterator<Entry<String, String>> subIterator = whereConditionItem.entrySet().iterator(); subIterator.hasNext();) {
                Entry<String, String> map = subIterator.next();
                whereConditionsObject.put(map.getKey(), map.getValue());
            }
            whereConditionsJson.put(whereConditionsObject);
        }
        viewObject.put(WHERE_CONDITIONS, whereConditionsJson);

        viewObject.put(SORT_FIELD, viewPOJO.getSortField());
        viewObject.put(CUSTOM_FORM, viewPOJO.getCustomForm());
        viewObject.put(IS_ASC, viewPOJO.getIsAsc());
        viewObject.put(IS_TRANSFORMER_ACTIVE, viewPOJO.isTransformerActive());

        return viewObject;
    }

    private void formatWhereItem(List<IWhereItem> tempList) {
        for (IWhereItem item : tempList) {
            if (item instanceof WhereAnd) {
                List<IWhereItem> andList = ((WhereAnd) item).getItems();
                formatWhereItem(andList);
            } else if (item instanceof WhereOr) {
                List<IWhereItem> orList = ((WhereOr) item).getItems();
                formatWhereItem(orList);
            } else if (item instanceof WhereCondition) {
                WhereCondition currentWhereCon = ((WhereCondition) item);
                Map<String, String> whereItem = new LinkedHashMap<>();
                whereItem.put(LEFT_PATH, currentWhereCon.getLeftPath());
                whereItem.put(OPERATOR, currentWhereCon.getOperator());
                whereItem.put(RIGHT_VALUE_OR_PATH, currentWhereCon.getRightValueOrPath());
                whereItem.put(SPELL_CHECK, currentWhereCon.isSpellCheck() == Boolean.TRUE ? "true" : "false");
                whereItem.put(STRING_PREDICATE, currentWhereCon.getStringPredicate());

                whereConditions.add(whereItem);
            }
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
        } else if (e instanceof ViewNotFoundException) {
            return Response.status(((ViewNotFoundException) e).getStatus()).entity(responseMessage).build();
        } else {
            LOGGER.error(responseMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseMessage).build();
        }
    }

    private Map<String, Set<String>> getViewRolesMap() {
        Role roleCtrlLocal = Util.getRoleCtrlLocal();
        Map<String, Set<String>> viewRolesMap = new HashMap<String, Set<String>>();
        Set<String> roleAllViewsSet = new HashSet<>();
        try {
            for (Object rolePOJOPK : roleCtrlLocal.getRolePKs(".*")) {
                String roleName = ((RolePOJOPK) rolePOJOPK).getUniqueId();
                RolePOJO role = ObjectPOJO.load(RolePOJO.class, new RolePOJOPK(roleName));
                // get Specifications for the View Object
                RoleSpecification specification = role.getRoleSpecifications().get(VIEW_OBJECT_NAME);
                if (specification != null && !specification.isAdmin()) {
                    Set<String> viewNames = specification.getInstances().keySet();
                    for (String viewName : viewNames) {
                        if (viewName.equals(DEFAULT_VIEW_ALL)) {
                            roleAllViewsSet.add(roleName);
                            continue;
                        }
                        Set<String> rolesSet = viewRolesMap.get(viewName);
                        if (null == rolesSet) {
                            rolesSet = new HashSet<>();
                        }
                        if (!rolesSet.contains(roleName)) {
                            rolesSet.add(roleName);
                            viewRolesMap.put(viewName, rolesSet);
                        }
                    }
                }
            }
            if (roleAllViewsSet.size() > 0) {
                for (String viewName : viewRolesMap.keySet()) {
                    Set<String> rolesSet = viewRolesMap.get(viewName);
                    if (null == rolesSet) {
                        rolesSet = new HashSet<>();
                    } else {
                        rolesSet.addAll(roleAllViewsSet);
                    }
                    viewRolesMap.put(viewName, rolesSet);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get view roles map.", e);
        }
        return viewRolesMap;
    }
}