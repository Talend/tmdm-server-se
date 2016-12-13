/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.delegator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.role.RolePOJO;
import com.amalto.core.objects.role.RolePOJOPK;
import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.objects.view.ViewPOJOPK;
import com.amalto.core.query.user.OrderBy;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.query.user.UserQueryHelper;
import com.amalto.core.query.user.metadata.StagingError;
import com.amalto.core.query.user.metadata.StagingSource;
import com.amalto.core.query.user.metadata.StagingStatus;
import com.amalto.core.query.user.metadata.TaskId;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.server.security.SecurityConfig;
import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordWriter;
import com.amalto.core.storage.record.DataRecordXmlWriter;
import com.amalto.core.storage.record.ViewSearchResultsWriter;
import com.amalto.core.util.ArrayListHolder;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.RoleSpecification;
import com.amalto.core.util.RoleWhereCondition;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;
import com.amalto.xmlserver.interfaces.WhereOr;
import com.amalto.xmlserver.interfaces.XmlServerException;

public abstract class IItemCtrlDelegator implements IBeanDelegator, IItemCtrlDelegatorService {

    private static final Logger LOGGER = Logger.getLogger(IItemCtrlDelegator.class);

    public ArrayList<String> viewSearch(DataClusterPOJOPK dataClusterPOJOPK, ViewPOJOPK viewPOJOPK, IWhereItem whereItem,
                                        String orderBy, String direction, int start, int limit) throws XtentisException {
        try {
            ViewPOJO view = getViewPOJO(viewPOJOPK);
            whereItem = Util.fixWebConditions(whereItem, getLocalUser().getUserXML());
            // Create an ItemWhere which combines the search and and view wheres
            ArrayList<IWhereItem> conditions = view.getWhereConditions().getList();
            // fix conditions: value of condition do not generate xquery.
            Util.fixConditions(conditions);
            // Set User Property conditions.
            if (Util.isContainUserProperty(conditions)) {
                Util.updateUserPropertyCondition(conditions, getLocalUser().getUserXML());
            }
            IWhereItem fullWhere = getFullWhereCondition(whereItem, conditions);
            // Add Filters from the Roles
            ILocalUser user = getLocalUser();
            HashSet<String> roleNames = user.getRoles();
            String objectType = "View"; //$NON-NLS-1$
            ArrayList<IWhereItem> roleWhereConditions = new ArrayList<IWhereItem>();
            for (String roleName : roleNames) {
                if (SecurityConfig.isSecurityPermission(roleName)) {
                    continue;
                }
                // load Role
                RolePOJO role = ObjectPOJO.load(RolePOJO.class, new RolePOJOPK(roleName));
                // get Specifications for the View Object
                RoleSpecification specification = role.getRoleSpecifications().get(objectType);
                if (specification != null) {
                    if (!specification.isAdmin()) {
                        Set<String> regexIds = specification.getInstances().keySet();
                        for (String regexId : regexIds) {
                            if (viewPOJOPK.getIds()[0].matches(regexId)) {
                                HashSet<String> parameters = specification.getInstances().get(regexId).getParameters();
                                for (String marshaledWhereCondition : parameters) {
                                    if (marshaledWhereCondition == null || marshaledWhereCondition.trim().length() == 0) {
                                        continue;
                                    }
                                    WhereCondition whereCondition = RoleWhereCondition.parse(marshaledWhereCondition)
                                            .toWhereCondition();
                                    String conditionValue = whereCondition.getRightValueOrPath();
                                    if ((conditionValue != null && conditionValue.length() > 0)
                                            || WhereCondition.EMPTY_NULL.equals(whereCondition.getOperator())) {
                                        roleWhereConditions.add(whereCondition);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // add collected additional conditions
            if (roleWhereConditions.size() > 0) {
                // Set User Property conditions.
                if (Util.isContainUserProperty(roleWhereConditions)) {
                    Util.updateUserPropertyCondition(roleWhereConditions, getLocalUser().getUserXML());
                }
                IWhereItem normalizedRolesConditions = normalizeConditions(roleWhereConditions);
                if (fullWhere == null) {
                    fullWhere = normalizedRolesConditions;
                } else {
                    WhereAnd wAnd = new WhereAnd();
                    wAnd.add(fullWhere);
                    wAnd.add(normalizedRolesConditions);
                    fullWhere = wAnd;
                }
            }
            // Find revision id for type
            String typeName = view.getSearchableBusinessElements().getList().get(0).split("/")[0]; //$NON-NLS-1$
            // Try to get storage for revision
            Server server = ServerContext.INSTANCE.get();
            String dataModelName = dataClusterPOJOPK.getUniqueId();
            StorageAdmin storageAdmin = server.getStorageAdmin();
            Storage storage = storageAdmin.get(dataModelName, storageAdmin.getType(dataModelName));
            MetadataRepository repository = storage.getMetadataRepository();
            boolean isStaging = storage.getType() == StorageType.STAGING;
            // Build query (from 'main' type)
            ComplexTypeMetadata type = repository.getComplexType(typeName);
            if (type == null) {
                throw new IllegalArgumentException("Type '" + typeName + "' does not exist in data cluster '" + dataModelName  //$NON-NLS-1$ //$NON-NLS-2$
                        + "'."); //$NON-NLS-1$
            }
            UserQueryBuilder qb = UserQueryBuilder.from(type);
            // Select fields
            ArrayListHolder<String> viewableBusinessElements = view.getViewableBusinessElements();
            for (String viewableBusinessElement : viewableBusinessElements.getList()) {
                String viewableTypeName = StringUtils.substringBefore(viewableBusinessElement, "/"); //$NON-NLS-1$
                String viewablePath = StringUtils.substringAfter(viewableBusinessElement, "/"); //$NON-NLS-1$
                if (viewablePath.isEmpty()) {
                    throw new IllegalArgumentException("View element '" + viewableBusinessElement //$NON-NLS-1$
                            + "' is invalid: no path to element."); //$NON-NLS-1$
                }
                ComplexTypeMetadata viewableType = repository.getComplexType(viewableTypeName);
                List<TypedExpression> fields = UserQueryHelper.getFields(viewableType, viewablePath);
                for (TypedExpression field : fields) {
                    if (isNeedToAddExplicitly(isStaging, field)) {
                        qb.select(field);
                    }
                }
            }
            qb.select(repository.getComplexType(typeName), "../../taskId"); //$NON-NLS-1$
            if (isStaging) {
                qb.select(repository.getComplexType(typeName), "$staging_status$"); //$NON-NLS-1$
                qb.select(repository.getComplexType(typeName), "$staging_error$"); //$NON-NLS-1$
                qb.select(repository.getComplexType(typeName), "$staging_source$"); //$NON-NLS-1$
            }
            // Condition and paging
            qb.where(UserQueryHelper.buildCondition(qb, fullWhere, repository));
            qb.start(start < 0 ? 0 : start); // UI can send negative start index
            qb.limit(limit);
            // Order by
            if (orderBy != null) {
                ComplexTypeMetadata orderByType = repository.getComplexType(StringUtils.substringBefore(orderBy, "/")); //$NON-NLS-1$
                String orderByFieldName = StringUtils.substringAfter(orderBy, "/"); //$NON-NLS-1$
                List<TypedExpression> fields = UserQueryHelper.getFields(orderByType, orderByFieldName);
                OrderBy.Direction queryDirection;
                if ("ascending".equals(direction) //$NON-NLS-1$
                        || "NUMBER:ascending".equals(direction) //$NON-NLS-1$
                        || "ASC".equals(direction)) { //$NON-NLS-1$
                    queryDirection = OrderBy.Direction.ASC;
                } else {
                    queryDirection = OrderBy.Direction.DESC;
                }
                for (TypedExpression field : fields) {
                    qb.orderBy(field, queryDirection);
                }
            }
            // Get records
            ArrayList<String> resultsAsString = new ArrayList<String>();
            try {
                storage.begin();
                StorageResults results = storage.fetch(qb.getSelect());
                resultsAsString.add("<totalCount>" + results.getCount() + "</totalCount>"); //$NON-NLS-1$ //$NON-NLS-2$
                DataRecordWriter writer = new ViewSearchResultsWriter();
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                for (DataRecord result : results) {
                    try {
                        writer.write(result, output);
                    } catch (IOException e) {
                        throw new XmlServerException(e);
                    }
                    String document = new String(output.toByteArray(), Charset.forName("UTF-8")); //$NON-NLS-1$
                    resultsAsString.add(document);
                    output.reset();
                }
                storage.commit();
            } catch (Exception e) {
                storage.rollback();
                throw new XmlServerException(e);
            }
            return resultsAsString;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to single search: " + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }
    
    private boolean isNeedToAddExplicitly(boolean isStaging, TypedExpression field) {
        boolean isNeedToAdd = true;
        if (field instanceof TaskId
                || (isStaging && (field instanceof StagingStatus || field instanceof StagingError || field instanceof StagingSource))) {
            isNeedToAdd = false;
        }
        return isNeedToAdd;
    }

    private static IWhereItem normalizeConditions(ArrayList<IWhereItem> conditions) {
        IWhereItem viewCondition = null;
        String predicate = null;
        int i = 0;
        for (IWhereItem condition : conditions) {
            if (viewCondition == null) {
                viewCondition = condition;
                if (condition instanceof WhereCondition) {
                    predicate = ((WhereCondition) condition).getStringPredicate();
                }
            } else {
                if (predicate == null) {
                    throw new IllegalArgumentException("No predicate in '" + conditions.get(i - 1) + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (condition instanceof WhereCondition) {
                    if (WhereCondition.PRE_OR.equals(predicate)) {
                        viewCondition = new WhereOr(Arrays.asList(viewCondition, condition));
                    } else if (WhereCondition.PRE_AND.equals(predicate)) {
                        viewCondition = new WhereAnd(Arrays.asList(viewCondition, condition));
                    } else if (WhereCondition.PRE_NOT.equals(predicate)) {
                        // TMDM-6888 Support for NOT (only when one condition).
                        if (conditions.size() > 1) {
                            throw new IllegalArgumentException("'Not' predicate not supported for multiple conditions (got " //$NON-NLS-1$
                                    + conditions.size() + ")"); //$NON-NLS-1$
                        }
                        viewCondition = condition; // Since there's only one condition, no need to break.
                    } else {
                        throw new IllegalArgumentException("Not supported predicate: " + predicate); //$NON-NLS-1$
                    }
                    predicate = ((WhereCondition) condition).getStringPredicate();
                }
            }
            i++;
        }
        return viewCondition;
    }

    public ItemPOJOPK putItem(ItemPOJO item, String schema, String dataModelName) throws XtentisException {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("putItem() " + item.getItemPOJOPK().getUniqueID()); //$NON-NLS-1$
        }
        try {
            // used for binding data model
            if (dataModelName != null) {
                item.setDataModelName(dataModelName);
            }
            // Store
            XmlServer xmlServerCtrlLocal = Util.getXmlServerCtrlLocal();
            String dataClusterName = item.getDataClusterPOJOPK().getUniqueId();
            ItemPOJOPK pk;
            try {
                xmlServerCtrlLocal.start(dataClusterName);
                pk = item.store();
                xmlServerCtrlLocal.commit(dataClusterName);
            } catch (XtentisException e) {
                try {
                    xmlServerCtrlLocal.rollback(dataClusterName);
                } catch (XtentisException e1) {
                    LOGGER.error("Rollback error.", e1); //$NON-NLS-1$
                }
                throw new RuntimeException(e);
            }
            if (pk == null) {
                throw new XtentisException("Could not put item " + Util.joinStrings(item.getItemIds(), ".") //$NON-NLS-1$ //$NON-NLS-2$
                        + ". Check server logs."); //$NON-NLS-1$
            }
            return pk;
        } catch (Exception e) {
            String prefix = "Unable to create/update the item " + item.getDataClusterPOJOPK().getUniqueId() + "." //$NON-NLS-1$ //$NON-NLS-2$
                    + Util.joinStrings(item.getItemIds(), ".") + ": "; //$NON-NLS-1$ //$NON-NLS-2$
            String err = prefix + e.getClass().getName() + ": " + e.getLocalizedMessage(); //$NON-NLS-1$
            LOGGER.error(err, e);
            // simplify the error message
            if ("Reporting".equalsIgnoreCase(dataModelName)) { //$NON-NLS-1$
                if (err.contains("One of '{ListOfFilters}'")) { //$NON-NLS-1$
                    err = prefix + "At least one filter must be defined"; //$NON-NLS-1$
                }
            }
            throw new XtentisException(err, e);
        }
    }

    public ArrayList<String> getItems(DataClusterPOJOPK dataClusterPOJOPK, String conceptName, IWhereItem whereItem,
            int spellThreshold, String orderBy, String direction, int start, int limit, boolean totalCountOnFirstRow)
            throws XtentisException {
        isExistDataCluster(dataClusterPOJOPK);
        Server server = ServerContext.INSTANCE.get();
        StorageAdmin storageAdmin = server.getStorageAdmin();
        String dataContainerName = dataClusterPOJOPK.getUniqueId();
        Storage storage = storageAdmin.get(dataContainerName, storageAdmin.getType(dataContainerName));
        MetadataRepository repository = storage.getMetadataRepository();
        ComplexTypeMetadata type = repository.getComplexType(conceptName);
        if (type == null) {
            throw new IllegalArgumentException("Type '" + conceptName + "' does not exist in data cluster '" //$NON-NLS-1$ //$NON-NLS-2$
                    + dataContainerName + "'."); //$NON-NLS-1$
        }
        UserQueryBuilder qb = UserQueryBuilder.from(type);
        // Condition and paging
        qb.where(UserQueryHelper.buildCondition(qb, whereItem, repository));
        qb.start(start < 0 ? 0 : start); // UI can send negative start index
        qb.limit(limit);
        // Order by
        if (orderBy != null) {
            String orderByFieldName = StringUtils.substringAfter(orderBy, "/"); //$NON-NLS-1$
            List<TypedExpression> fields = UserQueryHelper.getFields(type, orderByFieldName);
            if (fields == null) {
                throw new IllegalArgumentException("Field '" + orderBy + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
            }
            OrderBy.Direction queryDirection;
            if ("ascending".equals(direction) //$NON-NLS-1$
                    || "NUMBER:ascending".equals(direction) //$NON-NLS-1$
                    || "ASC".equals(direction)) { //$NON-NLS-1$
                queryDirection = OrderBy.Direction.ASC;
            } else {
                queryDirection = OrderBy.Direction.DESC;
            }
            for (TypedExpression typedExpression : fields) {
                qb.orderBy(typedExpression, queryDirection);
            }
        }
        // Get records
        ArrayList<String> resultsAsString = new ArrayList<String>();
        StorageResults results;
        try {
            storage.begin();
            if (totalCountOnFirstRow) {
                results = storage.fetch(qb.getSelect());
                resultsAsString.add("<totalCount>" + results.getCount() + "</totalCount>"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            results = storage.fetch(qb.getSelect());
            DataRecordWriter writer = new DataRecordXmlWriter(type);
            writer.setSecurityDelegator(SecuredStorage.getDelegator());
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (DataRecord result : results) {
                try {
                    if (totalCountOnFirstRow) {
                        output.write("<result>".getBytes()); //$NON-NLS-1$
                    }
                    writer.write(result, output);
                    if (totalCountOnFirstRow) {
                        output.write("</result>".getBytes()); //$NON-NLS-1$
                    }
                } catch (IOException e) {
                    throw new XtentisException(e);
                }
                String document = new String(output.toByteArray());
                resultsAsString.add(document);
                output.reset();
            }
            storage.commit();
        } catch (Exception e) {
            storage.rollback();
            throw new XtentisException(e);
        }
        return resultsAsString;
    }

    /**
     * get view where conditions from Role CE version return empty
     */
    protected ArrayList<IWhereItem> getViewWCFromRole(ViewPOJOPK viewPOJOPK) throws Exception {
        return null;
    }

    protected static IWhereItem getFullWhereCondition(IWhereItem whereItem, ArrayList<IWhereItem> conditions) {
        IWhereItem fullWhere;
        IWhereItem viewCondition = normalizeConditions(conditions);
        if (viewCondition == null) {
            fullWhere = whereItem;
        } else {
            if (whereItem == null) {
                fullWhere = viewCondition;
            } else {
                WhereAnd wAnd = new WhereAnd();
                wAnd.add(whereItem);
                wAnd.add(viewCondition);
                fullWhere = wAnd;
            }
        }
        return fullWhere;
    }

    /**
     * *************** test interfaces *****************************
     */

    @Override
    public ViewPOJO getViewPOJO(ViewPOJOPK viewPOJOPK) throws Exception {
        return Util.getViewCtrlLocal().getView(viewPOJOPK);
    }

    @Override
    public ILocalUser getLocalUser() throws XtentisException {
        return LocalUser.getLocalUser();
    }

    public void allowDelete(String clusterName, String concept, ComplexTypeMetadata.DeleteType deleteType) throws XtentisException {
        HashSet<String> roles;
        try {
            roles = getLocalUser().getRoles();
        } catch (XtentisException e) {
            String message = "Unable to access user current roles."; //$NON-NLS-1$
            LOGGER.error(message, e);
            throw new RuntimeException(message, e);
        }

        Server server = ServerContext.INSTANCE.get();
        StorageAdmin storageAdmin = server.getStorageAdmin();
        Storage storage = storageAdmin.get(clusterName, storageAdmin.getType(clusterName));
        MetadataRepository repository = storage.getMetadataRepository();
        ComplexTypeMetadata complexTypeMetadata = repository.getComplexType(concept);
        if (roles != null && roles.size() > 0) {
            if (ComplexTypeMetadata.DeleteType.LOGICAL.equals(deleteType)) {
                if (!Collections.disjoint(complexTypeMetadata.getDenyDelete(ComplexTypeMetadata.DeleteType.LOGICAL), roles)) {
                    throw new XtentisException("Unauthorized. User '" + LocalUser.getLocalUser().getUsername() + "' has no logical delete permission on '" + clusterName + "." + concept + "'");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
                }
            } else if (ComplexTypeMetadata.DeleteType.PHYSICAL.equals(deleteType)) {
                if (!Collections.disjoint(complexTypeMetadata.getDenyDelete(ComplexTypeMetadata.DeleteType.PHYSICAL), roles)) {
                    throw new XtentisException("Unauthorized. User '" + LocalUser.getLocalUser().getUsername() + "' has no physical delete permission on '" + clusterName + "." + concept + "'");   //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
                }
            }
        }
    }

    public void isExistDataCluster(DataClusterPOJOPK dataClusterPOJOPK) throws XtentisException {
        try {
            if (Util.getDataClusterCtrlLocal().existsDataCluster(dataClusterPOJOPK) == null) {
                throw new IllegalArgumentException("Data Cluster '" + dataClusterPOJOPK.getUniqueId() + "' does not exist."); //$NON-NLS-1$//$NON-NLS-2$
            }
        } catch (Exception e) {
            throw new XtentisException("Unable to get the Data Cluster '" + dataClusterPOJOPK.getUniqueId() + "'", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
