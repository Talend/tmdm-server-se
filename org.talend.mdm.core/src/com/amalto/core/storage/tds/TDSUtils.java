// ============================================================================
//
// Copyright (C) 2006-2017 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage.tds;

import static com.amalto.core.storage.tds.TDSConstants.TASK_BATCH_SIZE;
import static com.amalto.core.storage.tds.TDSConstants.TASK_STATE_RESOLVED;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Consts;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;

import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.record.DataRecord;

@SuppressWarnings("nls") public class TDSUtils {

    private static final DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private static final DateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    static {
        TIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        DATETIME_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    /**
     * Encode parameter to pass from URL
     *
     * @param param
     * @return
     * @throws IOException
     */
    public static String encode(String param) throws IOException {
        try {
            return URLEncoder.encode(param, Consts.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IOException("Parameter can't be encoded: " + param, e);
        }
    }

    /**
     * Get owned tasks resource
     *
     * @param campaignName name of a campaign
     * @return api/v1/campaigns/owned/{campaignName}/tasks
     */
    public static String getOwnedTaskResource(String coreApiRoot, String campaignName) {
        return coreApiRoot + "/campaigns/owned/" + campaignName + "/tasks";
    }

    /**
     * Get owned tasks query resource
     *
     * @param campaignName name of a campaign
     * @param taskState    state of a campaign
     * @return api/v1/campaigns/owned/{campaignName}/tasks/{taskState}
     */
    public static String getOwnedTasksQueryResource(String coreApiRoot, String campaignName, String taskState) {
        return getOwnedTaskResource(coreApiRoot, campaignName) + "/" + taskState;
    }

    /**
     * Get Resolved tasks resource
     *
     * @param campaignName name of a campaign
     * @param consumed     true or false
     * @param page         page number
     * @return api/v1/campaigns/owned/{campaignName}/tasks/Resolved?query=consumed%3D{consumed}&size=50&page={page}
     */
    public static String getResolvedTasksResource(String coreApiRoot, String campaignName, boolean consumed, int page) {
        return getOwnedTasksQueryResource(coreApiRoot, campaignName, TASK_STATE_RESOLVED) + "?query=consumed%3D" + consumed
                + "&size=" + TASK_BATCH_SIZE + "&page=" + page;
    }

    /**
     * Get delete tasks resource
     *
     * @param campaignName   name of a campaign
     * @param externalIdList external task ids list
     * @return api/v1/campaigns/owned/{campaignName}/tasks/Resolved?query=externalID%3D{externalID}
     */
    public static String getDeleteTasksResource(String coreApiRoot, String campaignName, List<String> externalIdList) {
        return getOwnedTasksQueryResource(coreApiRoot, campaignName, TASK_STATE_RESOLVED) + "?query=externalID in " + buildInTQL(
                externalIdList);
    }

    /**
     * Get consume tasks resource
     *
     * @param campaignName name of a campaign
     * @return api/v1/campaigns/owned/{campaignName}/tasks/Resolved/actions?scope=CELL
     */
    public static String getConsumeTasksResource(String coreApiRoot, String campaignName) {
        return getOwnedTasksQueryResource(coreApiRoot, campaignName, TASK_STATE_RESOLVED) + "/actions?scope=CELL";
    }

    /**
     * Get create schema resource
     *
     * @param namespace namespace
     * @return /api/v1/schemas/namespace
     */
    public static String getCreateSchemaResource(String schemaApiRoot, String namespace) {
        return schemaApiRoot + "/schemas/" + namespace;
    }

    /**
     * Get schema resource
     *
     * @param namespace namespace
     * @param name      name
     * @return /api/v1/schemas/namespace/name
     */
    public static String getSchemaResource(String schemaApiRoot, String namespace, String name) {
        return schemaApiRoot + "/schemas/" + namespace + "/" + name;
    }

    /**
     * Get owned campaigns resource
     *
     * @return api/v1/campaigns/owned
     */
    public static String getOwnedCampaignsResource(String coreApiRoot) {
        return coreApiRoot + "/campaigns/owned";
    }

    /**
     * Build "record" fragment used to create Task in TDS
     *
     * @param taskFields
     * @param dataRecord
     * @return
     */
    public static Map<String, Object> buildRecordObject(List<FieldMetadata> taskFields, DataRecord dataRecord) {
        Map<String, Object> record = new HashMap<String, Object>();
        for (FieldMetadata field : taskFields) {
            Object value = dataRecord.get(field);
            record.put(field.getName(), StorageMetadataUtils.toString(value, field));
        }
        return record;
    }

    /**
     * Convert TDS value into MDM value
     *
     * @param fieldType
     * @param tdsValue
     * @return
     */
    public static String toMdmValue(TypeMetadata fieldType, Object tdsValue) {
        Object mdmValue = StringUtils.EMPTY;
        if (tdsValue != null && !"".equals(tdsValue)) {
            if (Types.DATE.equals(fieldType.getName())) {
                mdmValue = DATE_FORMAT.format(new Date((Integer) tdsValue * TDSConstants.MS_PER_DAY));
            } else if (Types.DATETIME.equals(fieldType.getName())) {
                mdmValue = DATETIME_FORMAT.format(new Date((Long) tdsValue));
            } else if (Types.TIME.equals(fieldType.getName())) {
                mdmValue = TIME_FORMAT.format(new Date((Integer) tdsValue * 1000L));
            } else {
                mdmValue = tdsValue;
            }
        }
        return mdmValue.toString();
    }

    /**
     * Get campaign name from Storage name & Entity name
     *
     * @param storage
     * @param entity
     * @return
     */
    public static String getCampaignName(String storage, String entity) {
        return (storage + "-" + entity + "-tmdm").toLowerCase();
    }

    /**
     * Generates a description for created data model and campaign in TDS.
     *
     * @param modelName  the MDM model name
     * @param entityName the MDM entity name
     * @return
     */
    public static String getTDSModelAndCampaignDescription(String modelName, String entityName) {
        return "Created by MDM integrated matching from model [" + modelName + "] and entity [" + entityName + "].";
    }

    /**
     * Build consume tasks payload
     *
     * @param tdsTasks
     * @return
     */
    @SuppressWarnings("serial") public static Map<String, Object> buildConsumeTasksPayload(List<TDSTask> tdsTasks) {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("action", "consumeTask");
        payload.put("parameters", new HashMap<String, Object>() {

            {
                put("consumed", true);
            }
        });
        List<Map<String, Object>> taskList = new ArrayList<Map<String, Object>>();
        for (TDSTask tdsTask : tdsTasks) {
            Map<String, Object> task = new HashMap<String, Object>();
            task.put("taskId", tdsTask.id);
            task.put("taskVersion", tdsTask.version);
            taskList.add(task);
        }
        payload.put("taskList", taskList);
        return payload;
    }

    /**
     * Build "in" fragment from a list
     *
     * @param idList list of ids
     * @return fragment like ['id1','id2']
     */
    public static String buildInTQL(List<String> idList) {
        StringBuilder sb = new StringBuilder();
        for (String id : idList) {
            sb.append(",'").append(id).append("'");
        }
        String id = idList.isEmpty() ? StringUtils.EMPTY : sb.toString().substring(1);
        return "[" + id + "]";
    }
}
