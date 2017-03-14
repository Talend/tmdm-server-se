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

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;

import com.amalto.core.storage.tds.setup.TDSDataModel;
import com.amalto.core.storage.tds.setup.TDSMergingCampaignWithParticipants;

/**
 * TDS service.
 */
@SuppressWarnings("nls")
public interface TDSClient {

    /**
     * Initialize TDS connection with given user & password
     *
     * @param user
     * @param password
     * @return
     */
    TDSClient initAuthentication(String user, String password);

    /**
     * Initialize TDS connection with technical user & password set in mdm.conf
     *
     * @return
     */
    TDSClient initAuthentication();

    /**
     * Bulk create tasks
     *
     * @param campaignName
     * @param tasksJson
     * @return
     */
    List<Map<String, Object>> createTasks(String campaignName, String tasksJson);

    /**
     * Bulk consume tasks
     *
     * @param campaignName
     * @param taskList
     * @return
     */
    List<Map<String, Object>> consumeTasks(String campaignName, List<TDSTask> taskList);

    /**
     * Bulk delete tasks
     *
     * @param campaignName
     * @param externalIdList
     * @return
     */
    List<Map<String, Object>> deleteTasks(String campaignName, List<String> externalIdList);

    /**
     * Get tasks to consume (state=Resolved, consumed=false)
     *
     * @param campaignName
     * @param page
     * @return
     */
    List<TDSTask> getTasksToConsume(String campaignName, int page);

    /**
     * Gets a data model given its name and namespace.
     *
     * @param namespace
     * @param name
     * @return the fetched data model, if found.
     */
    TDSDataModel getDataModel(String namespace, String name) throws IOException;

    /**
     * Creates a data model.
     *
     * @param dataModel
     * @return the summary of the created data model.
     */
    TDSDataModel createDataModel(TDSDataModel dataModel) throws IOException;

    /**
     * Creates a TDS merging campaign.
     *
     * @param campaignWithParticipants
     * @return the fetched data model, if found.
     */
    void createCampaign(TDSMergingCampaignWithParticipants campaignWithParticipants) throws IOException;

    /**
     * Check existence of a task
     * 
     * @param externalId
     * @return
     */
    boolean existTask(String externalId);

    /**
     * Gets current user.
     *
     * @return the current user for this client.
     */
    String getCurrentUser();

    /**
     * Logs the current user in.
     *
     * @return the {@link HttpResponse}
     * @throws IOException
     */
    HttpResponse login() throws IOException;

    /**
     * Logs the current user out.
     *
     * @return the {@link HttpResponse}
     */
    HttpResponse logout();
}