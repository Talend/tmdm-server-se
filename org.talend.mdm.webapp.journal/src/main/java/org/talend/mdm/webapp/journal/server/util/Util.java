/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.webapp.XObjectType;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.journal.server.service.WebService;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;

import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSGetConceptsInDataCluster;
import com.amalto.core.webservice.WSGetItems;
import com.amalto.core.webservice.WSGetItemsSort;
import com.amalto.core.webservice.WSRegexDataModelPKs;
import com.amalto.core.webservice.WSStringPredicate;
import com.amalto.core.webservice.WSWhereAnd;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.core.webservice.WSWhereItem;
import com.amalto.core.webservice.WSWhereOperator;
import com.amalto.core.webservice.WSWhereOr;
import com.amalto.core.webservice.XtentisPort;


/**
 * created by talend2 on 2013-1-29
 * Detailled comment
 *
 */
public class Util {

    public static final int TIME_OF_ONE_SECOND = 1000;

    public static final String concept = "Update"; //$NON-NLS-1$   

    private static final Logger LOG = Logger.getLogger(Util.class);

    public static List<WSWhereItem> buildWhereItems(JournalSearchCriteria criteria, WebService webService)
            throws ServiceException {
        
        List<WSWhereItem> whereItemList = new ArrayList<WSWhereItem>();        

        if (criteria.getEntity() != null) {
            WSWhereCondition wc = new WSWhereCondition(
                    "Concept", criteria.isStrict() ? WSWhereOperator.EQUALS : WSWhereOperator.CONTAINS, criteria.getEntity().trim(), WSStringPredicate.NONE, false); //$NON-NLS-1$
            WSWhereItem wsWhereItem = new WSWhereItem(wc, null, null);
            whereItemList.add(wsWhereItem);
        }

        if (criteria.getKey() != null) {
            WSWhereCondition wc = new WSWhereCondition(
                    "Key", criteria.isStrict() ? WSWhereOperator.EQUALS : WSWhereOperator.CONTAINS, criteria.getKey().trim(), WSStringPredicate.NONE, false); //$NON-NLS-1$
            WSWhereItem wsWhereItem = new WSWhereItem(wc, null, null);
            whereItemList.add(wsWhereItem);
        }

        if (criteria.getSource() != null) {
            WSWhereCondition wc = new WSWhereCondition(
                    "Source", criteria.isStrict() ? WSWhereOperator.EQUALS : WSWhereOperator.CONTAINS, criteria.getSource().trim(), WSStringPredicate.NONE, false); //$NON-NLS-1$
            WSWhereItem wsWhereItem = new WSWhereItem(wc, null, null);
            whereItemList.add(wsWhereItem);
        }

        if (criteria.getOperationType() != null) {
            WSWhereCondition wc = new WSWhereCondition(
                    "OperationType", WSWhereOperator.EQUALS, criteria.getOperationType().trim(), WSStringPredicate.NONE, false); //$NON-NLS-1$
            WSWhereItem wsWhereItem = new WSWhereItem(wc, null, null);
            whereItemList.add(wsWhereItem);
        }

        if (criteria.getStartDate() != null) {
            WSWhereCondition wc = new WSWhereCondition(
                    "TimeInMillis", WSWhereOperator.GREATER_THAN_OR_EQUAL, criteria.getStartDate().getTime() + "", WSStringPredicate.NONE, false); //$NON-NLS-1$ //$NON-NLS-2$
            WSWhereItem wsWhereItem = new WSWhereItem(wc, null, null);
            whereItemList.add(wsWhereItem);
        }

        if (criteria.getEndDate() != null) {
            WSWhereCondition wc = new WSWhereCondition(
                    "TimeInMillis", WSWhereOperator.LOWER_THAN, (criteria.getEndDate().getTime() + TIME_OF_ONE_SECOND) + "", WSStringPredicate.NONE, false); //$NON-NLS-1$ //$NON-NLS-2$
            WSWhereItem wsWhereItem = new WSWhereItem(wc, null, null);
            whereItemList.add(wsWhereItem);
        }

        try {
            List<String> accessModels = new ArrayList<>();
            XtentisPort port = com.amalto.webapp.core.util.Util.getPort();
            if (criteria.getDataModel() != null) {
                WSWhereCondition wc = new WSWhereCondition(
                        "DataModel", WSWhereOperator.EQUALS, criteria.getDataModel(), WSStringPredicate.NONE, false); //$NON-NLS-1$
                WSWhereItem wsWhereItem = new WSWhereItem(wc, null, null);
                whereItemList.add(wsWhereItem);
                accessModels.add(criteria.getDataModel());
            } else {
                if (webService.isEnterpriseVersion()) {
                    // port.getDataModelPKs() can only get the models user has access
                    WSDataModelPK[] wsDataModelsPKs = port.getDataModelPKs(new WSRegexDataModelPKs("*")).getWsDataModelPKs(); //$NON-NLS-1$
                    Map<String, XSystemObjects> xDataModelsMap = XSystemObjects.getXSystemObjects(XObjectType.DATA_MODEL);
                    List<WSWhereItem> modelConditions = new ArrayList<WSWhereItem>();
                    for (WSDataModelPK wsDataModelsPK : wsDataModelsPKs) {
                        if (!XSystemObjects.isXSystemObject(xDataModelsMap, wsDataModelsPK.getPk())) {
                            accessModels.add(wsDataModelsPK.getPk());
                            WSWhereCondition wc = new WSWhereCondition(
                                    "DataModel", WSWhereOperator.EQUALS, (wsDataModelsPK.getPk()), WSStringPredicate.NONE, false); //$NON-NLS-1$ 
                            modelConditions.add(new WSWhereItem(wc, null, null));
                        }
                    }
                    if (modelConditions.size() == 0) {
                        WSWhereCondition wc = new WSWhereCondition(
                                "DataModel", WSWhereOperator.EQUALS, (""), WSStringPredicate.NONE, false); //$NON-NLS-1$ 
                        WSWhereItem modelItem = new WSWhereItem(wc, null, null);
                        whereItemList.add(modelItem);
                    } else {
                        WSWhereOr or = new WSWhereOr(modelConditions.toArray(new WSWhereItem[modelConditions.size()]));
                        WSWhereItem modelItem = new WSWhereItem(null, null, or);
                        whereItemList.add(modelItem);
                    }
                }
            }
            if (webService.isEnterpriseVersion()) {
                for (String model : accessModels) {
                    String[] entities = port.getConceptsInDataCluster(new WSGetConceptsInDataCluster(new WSDataClusterPK(model)))
                            .getStrings();
                    for (String entity : entities) {
                        boolean canReadEntity = webService.checkReadAccess(model, entity);
                        if (!canReadEntity) {
                            WSWhereCondition wc = new WSWhereCondition(
                                    "Concept", WSWhereOperator.NOT_EQUALS, entity, WSStringPredicate.NONE, false); //$NON-NLS-1$
                            WSWhereItem wsWhereItem = new WSWhereItem(wc, null, null);
                            whereItemList.add(wsWhereItem);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException("Error occurs during checking access to data model and entities: " //$NON-NLS-1$
                    + e.getLocalizedMessage());
        }        
        return whereItemList;
    }
    
    public static WSGetItems buildGetItem(List<WSWhereItem> conditions,int start, int limit) {
        
        WSWhereItem wi;
        if (conditions.size() == 0) {
            wi = null;
        } else if (conditions.size() == 1) {
            wi = conditions.get(0);
        } else {
            WSWhereAnd and = new WSWhereAnd(conditions.toArray(new WSWhereItem[conditions.size()]));
            wi = new WSWhereItem(null, and, null);
        }

        WSGetItems item = new WSGetItems();
        item.setConceptName("Update"); //$NON-NLS-1$
        item.setWhereItem(wi);
        item.setTotalCountOnFirstResult(true);
        item.setSkip(start);
        item.setMaxItems(limit);
        item.setWsDataClusterPK(new WSDataClusterPK(XSystemObjects.DC_UPDATE_PREPORT.getName()));
        return item;
    }
    
    public static WSGetItemsSort buildGetItemsSort(List<WSWhereItem> conditions,int start, int limit,String sort,String dir) {
        
        WSWhereItem wi;
        if (conditions.size() == 0) {
            wi = null;
        } else if (conditions.size() == 1) {
            wi = conditions.get(0);
        } else {
            WSWhereAnd and = new WSWhereAnd(conditions.toArray(new WSWhereItem[conditions.size()]));
            wi = new WSWhereItem(null, and, null);
        }

        WSGetItemsSort item = new WSGetItemsSort();
        item.setConceptName("Update"); //$NON-NLS-1$
        item.setWhereItem(wi);
        item.setTotalCountOnFirstResult(true);
        item.setSkip(start);
        item.setMaxItems(limit);
        item.setWsDataClusterPK(new WSDataClusterPK(XSystemObjects.DC_UPDATE_PREPORT.getName()));
        item.setSort(getOrderXPath(sort));
        item.setDir(dir);
        return item;
    }
    
    private static String getOrderXPath(String fieldName) {
        if (fieldName != null && fieldName.length() > 0) {
            String elementName;
            if ("dataContainer".equals(fieldName)) { //$NON-NLS-1$
                elementName = "DataCluster"; //$NON-NLS-1$
            } else if ("dataModel".equals(fieldName)) { //$NON-NLS-1$
                elementName =  "DataModel"; //$NON-NLS-1$
            } else if ("entity".equals(fieldName)) { //$NON-NLS-1$
                elementName =  "Concept"; //$NON-NLS-1$
            } else if ("key".equals(fieldName)) { //$NON-NLS-1$
                elementName =  "Key"; //$NON-NLS-1$
            } else if ("operationType".equals(fieldName)) { //$NON-NLS-1$
                elementName =  "OperationType"; //$NON-NLS-1$
            } else if ("operationTime".equals(fieldName)) { //$NON-NLS-1$
                elementName =  "TimeInMillis"; //$NON-NLS-1$
            } else if ("source".equals(fieldName)) { //$NON-NLS-1$
                elementName =  "Source"; //$NON-NLS-1$
            } else if ("userName".equals(fieldName)) { //$NON-NLS-1$
                elementName =  "UserName"; //$NON-NLS-1$
            } else {
                elementName = fieldName;
            }
            return concept + "/" + elementName; //$NON-NLS-1$
        } else {
            return null;
        }        
    }
}
