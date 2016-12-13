/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UpdateReportPOJO {

    public final static String                SOURCE_DATA_SYNCHRONIZATION    = "dataSynchronization"; //$NON-NLS-1$

    public static final String                DATA_CLUSTER                   = "UpdateReport";       //$NON-NLS-1$

    public static final String                DATA_MODEL                     = "UpdateReport";       //$NON-NLS-1$

    public static final String                SOURCE_RESTORE                 = "restoreUI";          //$NON-NLS-1$

    public final static String                OPERATION_TYPE_CREATE          = "CREATE";             //$NON-NLS-1$

    public final static String                OPERATION_TYPE_UPDATE          = "UPDATE";             //$NON-NLS-1$

    public static final String                OPERATION_TYPE_PHYSICAL_DELETE = "PHYSICAL_DELETE";    //$NON-NLS-1$

    public static final String                OPERATION_TYPE_LOGICAL_DELETE  = "LOGIC_DELETE";       //$NON-NLS-1$

    public static final String                OPERATION_TYPE_RESTORED        = "RESTORED";           //$NON-NLS-1$

    public static final String                OPERATION_TYPE_ACTION          = "ACTION";             //$NON-NLS-1$
    
    /**
     * source value for genericUI
     */
    public static final String                GENERIC_UI_SOURCE              = "genericUI";
    
    /**
     * source value for service
     */
    public static final String                SERVICE_SOURCE                 = "service";

    private String                            source;

    private long                              timeInMillis;

    private String                            operationType;

    private String                            concept;

    private String                            key;

    private Map<String, UpdateReportItemPOJO> updateReportItemsMap;

    // additional fields
    private String                            dataCluster;

    private String                            dataModel;

    private String                            userName;

    public UpdateReportPOJO(String concept, String key, String operationType, String source, long timeInMillis) {
        super();
        this.concept = concept;
        this.key = key;
        this.operationType = operationType;
        this.source = source;
        this.timeInMillis = timeInMillis;
    }

    public UpdateReportPOJO(String concept, String key, String operationType, String source, long timeInMillis,
            Map<String, UpdateReportItemPOJO> updateReportItemsMap) {
        this(concept, key, operationType, source, timeInMillis);
        if (updateReportItemsMap == null) {
            this.updateReportItemsMap = new HashMap<String, UpdateReportItemPOJO>();
        } else {
            this.updateReportItemsMap = updateReportItemsMap;
        }
    }

    public UpdateReportPOJO(String concept, String key, String operationType, String source, long timeInMillis,
                            String dataCluster, String dataModel, String userName,
                            Map<String, UpdateReportItemPOJO> updateReportItemsMap) {
        this(concept, key, operationType, source, timeInMillis);
        this.dataCluster = dataCluster;
        this.dataModel = dataModel;
        this.userName = userName;
        if (updateReportItemsMap == null) {
            this.updateReportItemsMap = new HashMap<String, UpdateReportItemPOJO>();
        } else {
            this.updateReportItemsMap = updateReportItemsMap;
        }
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public long getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(long timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDataCluster() {
        return dataCluster;
    }

    public void setDataCluster(String dataCluster) {
        this.dataCluster = dataCluster;
    }

    public String getDataModel() {
        return dataModel;
    }

    public void setDataModel(String dataModel) {
        this.dataModel = dataModel;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Map<String, UpdateReportItemPOJO> getUpdateReportItemsMap() {
        if (updateReportItemsMap == null) {
            updateReportItemsMap = new HashMap<String, UpdateReportItemPOJO>();
        }
        return updateReportItemsMap;
    }

    public void setUpdateReportItemsMap(Map<String, UpdateReportItemPOJO> updateReportItemsMap) {
        this.updateReportItemsMap = updateReportItemsMap;
    }

    public String[] obtainIds() {

        return new String[] { this.source, this.timeInMillis + "" };
    }

    public String serialize() {
        StringBuilder log = new StringBuilder();

        log.append("<Update>\n") //$NON-NLS-1$
                .append("<UserName>").append(StringEscapeUtils.escapeXml(this.userName)).append("</UserName>\n") //$NON-NLS-1$ //$NON-NLS-2$
                .append("<Source>").append(StringEscapeUtils.escapeXml(this.source)).append("</Source>\n") //$NON-NLS-1$ //$NON-NLS-2$
                .append("<TimeInMillis>").append(this.timeInMillis).append("</TimeInMillis>\n") //$NON-NLS-1$ //$NON-NLS-2$
                .append("<OperationType>").append(StringEscapeUtils.escapeXml(this.operationType)).append("</OperationType>\n") //$NON-NLS-1$ //$NON-NLS-2$
                .append("<DataCluster>").append(StringEscapeUtils.escapeXml(this.dataCluster)).append("</DataCluster>\n") //$NON-NLS-1$ //$NON-NLS-2$
                .append("<DataModel>").append(StringEscapeUtils.escapeXml(this.dataModel)).append("</DataModel>\n") //$NON-NLS-1$ //$NON-NLS-2$
                .append("<Concept>").append(StringEscapeUtils.escapeXml(this.concept)).append("</Concept>\n") //$NON-NLS-1$ //$NON-NLS-2$
                .append("<Key>").append(StringEscapeUtils.escapeXml(this.key)).append("</Key>\n"); //$NON-NLS-1$ //$NON-NLS-2$

        if (OPERATION_TYPE_UPDATE.equals(operationType)) {
            Map<String, UpdateReportItemPOJO> map = this.updateReportItemsMap == null ? new HashMap<String, UpdateReportItemPOJO>()
                    : this.updateReportItemsMap;
            Set<Map.Entry<String, UpdateReportItemPOJO>> entries = map.entrySet();
            for (Map.Entry<String, UpdateReportItemPOJO> entry : entries) {
                log.append(entry.getValue().serialize());
            }
        }

        log.append("</Update>"); //$NON-NLS-1$

        return log.toString();
    }

}
