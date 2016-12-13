/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONObject;
import org.talend.mdm.commmon.util.datamodel.management.BusinessConcept;
import org.talend.mdm.commmon.util.datamodel.management.ReusableType;

import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSGetItemPKsByCriteria;
import com.amalto.core.webservice.WSGetItemPKsByFullCriteria;
import com.amalto.core.webservice.WSItemPKsByCriteriaResponse;
import com.amalto.webapp.core.bean.Configuration;
import com.amalto.webapp.core.bean.ListRange;
import com.amalto.webapp.core.dmagent.SchemaWebAgent;
import com.amalto.webapp.core.util.Util;

/**
 * cluster
 * 
 * 
 * @author asaintguilhem
 * 
 */

public class ItemsBrowserDWR {

    public ListRange getItems(int start, int limit, String sort, String dir, String regex) throws Exception {
        ListRange listRange = new ListRange();
        WSDataClusterPK wsDataClusterPK = new WSDataClusterPK();
        String entity = null;
        String contentWords = null;
        String keys = null;
        Long fromDate = new Long(-1);
        Long toDate = new Long(-1);
        String fkvalue = null;
        String dataObject = null;

        if (regex != null && regex.length() > 0) {
            JSONObject criteria = new JSONObject(regex);

            Configuration configuration = Configuration.getConfiguration();
            wsDataClusterPK.setPk(configuration.getCluster());
            entity = !criteria.isNull("entity") ? (String) criteria.get("entity") : "";//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
            keys = !criteria.isNull("key") && !"*".equals(criteria.get("key")) ? (String) criteria.get("key") : "";//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
            fkvalue = !criteria.isNull("fkvalue") && !"*".equals(criteria.get("fkvalue")) ? (String) criteria.get("fkvalue") : "";//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
            dataObject = !criteria.isNull("dataObject") && !"*".equals(criteria.get("dataObject")) ? (String) criteria//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                    .get("dataObject") : "";//$NON-NLS-1$//$NON-NLS-2$
            contentWords = !criteria.isNull("keyWords") && !"*".equals(criteria.get("keyWords")) ? (String) criteria//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
                    .get("keyWords") : "";//$NON-NLS-1$//$NON-NLS-2$

            if (!criteria.isNull("fromDate")) {//$NON-NLS-1$
                String startDate = (String) criteria.get("fromDate");//$NON-NLS-1$
                SimpleDateFormat dataFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//$NON-NLS-1$
                java.util.Date date = dataFmt.parse(startDate);
                fromDate = date.getTime();
            }

            if (!criteria.isNull("toDate")) {//$NON-NLS-1$
                String endDate = (String) criteria.get("toDate");//$NON-NLS-1$
                SimpleDateFormat dataFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//$NON-NLS-1$
                java.util.Date date = dataFmt.parse(endDate);
                toDate = date.getTime();
            }
        }

        // @temp yguo , xpath and value
        BusinessConcept businessConcept = SchemaWebAgent.getInstance().getBusinessConcept(entity);
        businessConcept.load();
        Map<String, String> foreignKeyMap = businessConcept.getForeignKeyMap();
        Map<String, String> inheritanceForeignKeyMap = businessConcept.getInheritanceForeignKeyMap();

        Set<String> foreignKeyXpath = foreignKeyMap.keySet();
        Set<String> xpathes = new HashSet<String>();

        for (String path : foreignKeyXpath) {
            String dataObjectPath = foreignKeyMap.get(path);
            String entityName = SchemaWebAgent.getInstance().getEntityNameFromXPath(dataObjectPath);
            if (SchemaWebAgent.getInstance().equalOrInheritanceEntities(entityName, dataObject)) {
                xpathes.add(path.substring(1));
            }
        }

        List<String> types = SchemaWebAgent.getInstance().getBindingType(businessConcept.getE());
        for (String type : types) {
            List<ReusableType> subTypes = SchemaWebAgent.getInstance().getMySubtypes(type);
            for (ReusableType reusableType : subTypes) {
                Map<String, String> fks = SchemaWebAgent.getInstance().getReferenceEntities(reusableType, dataObject);
                Collection<String> fkPaths = fks != null ? fks.keySet() : null;
                for (String fkpath : fkPaths) {
                    if (fks.get(fkpath).indexOf(dataObject) != -1
                            && !resolvedInInheritanceMapping(fkpath, fks.get(fkpath), inheritanceForeignKeyMap)) {
                        xpathes.add(fkpath);
                    }
                }
            }
        }

        // process foreign key obtained through inheritance
        if (inheritanceForeignKeyMap.size() > 0) {
            Set<String> keySet = inheritanceForeignKeyMap.keySet();
            String dataObjectPath = null;
            for (String path : keySet) {
                dataObjectPath = inheritanceForeignKeyMap.get(path);
                if (dataObjectPath.indexOf(dataObject) != -1) {
                    xpathes.add(path.substring(1));
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("$");//$NON-NLS-1$
        sb.append(joinSet(xpathes, ","));//$NON-NLS-1$
        sb.append("$");//$NON-NLS-1$
        sb.append(fkvalue);

        WSItemPKsByCriteriaResponse results = Util.getPort().getItemPKsByFullCriteria(
                new WSGetItemPKsByFullCriteria(new WSGetItemPKsByCriteria(wsDataClusterPK, entity, contentWords, sb.toString(),
                        keys, fromDate, toDate, start, limit), false));

        Map<String, Object>[] data = new Map[results.getResults().length - 1];
        int totalSize = 0;
        for (int i = 0; i < results.getResults().length; i++) {
            if (i == 0) {
                totalSize = Integer.parseInt(Util.parse(results.getResults()[i].getWsItemPK().getConceptName())
                        .getDocumentElement().getTextContent());
                continue;
            }

            Map<String, Object> record = new HashMap<String, Object>();
            record.put("date", new java.util.Date(results.getResults()[i].getDate()).toString());//$NON-NLS-1$
            record.put("entity", results.getResults()[i].getWsItemPK().getConceptName());//$NON-NLS-1$
            record.put("key", results.getResults()[i].getWsItemPK().getIds());//$NON-NLS-1$
            data[i - 1] = record;
        }

        listRange.setTotalSize(totalSize);
        listRange.setData(data);

        return listRange;
    }

    private boolean resolvedInInheritanceMapping(String fkpath, String target, Map<String, String> inheritanceForeignKeyMap) {
        if (inheritanceForeignKeyMap.isEmpty() || !inheritanceForeignKeyMap.containsValue(target)) {
            return false;
        }
        for (Entry<String, String> current : inheritanceForeignKeyMap.entrySet()) {
            String fkName = StringUtils.substringAfterLast(fkpath, "/"); //$NON-NLS-1$
            if (target.equals(current.getValue()) && current.getKey().endsWith(fkName)) {
                return true;
            }
        }
        return false;
    }

    private String joinSet(Set<String> set, String decollator) {
        if (set == null) {
            return ""; //$NON-NLS-1$
        }
        StringBuffer sb = new StringBuffer();
        boolean isFirst = true;
        for (String str : set) {
            if (isFirst) {
                sb.append(str);
                isFirst = false;
                continue;
            }
            sb.append(decollator + str);
        }
        return sb.toString();
    }
}
