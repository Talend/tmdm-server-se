package com.amalto.core.storage.tds;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpResponse;

import com.amalto.core.storage.tds.setup.TDSDataModel;
import com.amalto.core.storage.tds.setup.TDSMergingCampaignWithParticipants;

public class TDSClientImpl implements TDSClient {

    @Override
    public TDSClient initAuthentication(String user, String password) {
        throw new NotImplementedException();
    }

    @Override
    public TDSClient initAuthentication() {
        throw new NotImplementedException();
    }

    @Override
    public List<Map<String, Object>> createTasks(String campaignName, String tasksJson) {
        throw new NotImplementedException();
    }

    @Override
    public List<Map<String, Object>> consumeTasks(String campaignName, List<TDSTask> taskList) {
        throw new NotImplementedException();
    }

    @Override
    public List<Map<String, Object>> deleteTasks(String campaignName, List<String> externalIdList) {
        throw new NotImplementedException();
    }

    @Override
    public List<TDSTask> getTasksToConsume(String campaignName, int page) {
        throw new NotImplementedException();
    }

    @Override
    public TDSDataModel getDataModel(String namespace, String name) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public TDSDataModel createDataModel(TDSDataModel dataModel) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public void createCampaign(TDSMergingCampaignWithParticipants campaignWithParticipants) throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public boolean existTask(String externalId) {
        return false;
    }

    @Override
    public String getCurrentUser() {
        throw new NotImplementedException();
    }

    @Override
    public HttpResponse login() throws IOException {
        throw new NotImplementedException();
    }

    @Override
    public HttpResponse logout() {
        throw new NotImplementedException();
    }
}
