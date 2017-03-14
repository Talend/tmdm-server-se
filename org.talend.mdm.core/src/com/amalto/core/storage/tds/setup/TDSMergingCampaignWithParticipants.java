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
package com.amalto.core.storage.tds.setup;

import java.util.*;
import java.util.stream.Collectors;

import com.amalto.core.storage.tds.TDSConstants;
import com.amalto.core.storage.tds.TDSUtils;
import com.google.common.collect.ImmutableMap;

/**
 * The JSON payload that represents a merging campaign TDS
 */

public class TDSMergingCampaignWithParticipants {

    private TDSMergingCampaign campaign;

    private Map<String, List<String>> participants;

    public TDSMergingCampaignWithParticipants(TDSDataModel dataModel, String mdmModelName, String mdmEntityName,
            String currentUser) {
        this.participants = ImmutableMap.of(TDSConstants.STEWARD_ROLE, Collections.singletonList(currentUser));
        this.campaign = new TDSMergingCampaign(dataModel.getName());
        this.campaign.setDescription(TDSUtils.getTDSModelAndCampaignDescription(mdmModelName, mdmEntityName));
        this.campaign.setOwners(Collections.singletonList(currentUser));
        this.campaign.setSchemaRef(new TDSSchemaRef(dataModel.getNamespace(), dataModel.getName(), 1));
        this.campaign.setFieldsAccess(dataModel.getFields().stream().collect(Collectors.toMap(f -> f.getName(),
                f -> ImmutableMap.of(TDSConstants.STEWARD_ROLE, TDSConstants.ACCESS_RIGHT_READ_ONLY))));
    }

    public TDSMergingCampaign getCampaign() {
        return campaign;
    }

    public void setCampaign(TDSMergingCampaign campaign) {
        this.campaign = campaign;
    }

    public Map<String, List<String>> getParticipants() {
        return participants;
    }

    public void setParticipants(Map<String, List<String>> participants) {
        this.participants = participants;
    }

    public static class TDSMergingCampaign {

        private String label;

        private String description;

        private String taskType;

        private TDSWorkflow workflow;

        private List<String> owners = new ArrayList<>();

        private TDSSchemaRef schemaRef;

        private Map<String, Map<String, String>> fieldsAccess;

        public <T> TDSMergingCampaign(String label) {
            this.label = label;
            this.taskType = TDSConstants.TASK_TYPE_MERGING;
            // Default workflow
            TDSState newState = new TDSState(TDSConstants.TASK_STATE_NEW);
            TDSTransition transition = new TDSTransition("resolve", TDSConstants.TASK_STATE_RESOLVED,
                    Collections.singletonList(TDSConstants.STEWARD_ROLE));
            newState.setTransitions(Collections.singletonList(transition));
            newState.setAllowedRoles(Collections.singletonList(TDSConstants.STEWARD_ROLE));
            TDSState resolvedState = new TDSState(TDSConstants.TASK_STATE_RESOLVED);
            this.workflow = new TDSWorkflow("Simple", Arrays.asList(newState, resolvedState));
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getTaskType() {
            return taskType;
        }

        public void setTaskType(String taskType) {
            this.taskType = taskType;
        }

        public TDSWorkflow getWorkflow() {
            return workflow;
        }

        public void setWorkflow(TDSWorkflow workflow) {
            this.workflow = workflow;
        }

        public List<String> getOwners() {
            return owners;
        }

        public void setOwners(List<String> owners) {
            this.owners = owners;
        }

        public TDSSchemaRef getSchemaRef() {
            return schemaRef;
        }

        public void setSchemaRef(TDSSchemaRef schemaRef) {
            this.schemaRef = schemaRef;
        }

        public Map<String, Map<String, String>> getFieldsAccess() {
            return fieldsAccess;
        }

        public void setFieldsAccess(Map<String, Map<String, String>> fieldsAccess) {
            this.fieldsAccess = fieldsAccess;
        }
    }

    public static class TDSSchemaRef {

        private String namespace;

        private String name;

        private Integer version;

        public TDSSchemaRef(String namespace, String name, Integer version) {
            this.namespace = namespace;
            this.name = name;
            this.version = version;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }
    }

    public static class TDSWorkflow {

        private String name;

        private List<TDSState> states;

        public TDSWorkflow(String name, List<TDSState> states) {
            this.name = name;
            this.states = states;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<TDSState> getStates() {
            return states;
        }

        public void setStates(List<TDSState> states) {
            this.states = states;
        }
    }

    public static class TDSState {

        private String name;

        private List<TDSTransition> transitions;

        private List<String> allowedRoles;

        public TDSState(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<TDSTransition> getTransitions() {
            return transitions;
        }

        public void setTransitions(List<TDSTransition> transitions) {
            this.transitions = transitions;
        }

        public List<String> getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(List<String> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }

    }

    public static class TDSTransition {

        private String name;

        private String targetStateName;

        private List<String> allowedRoles;

        public TDSTransition(String name, String targetStateName, List<String> allowedRoles) {
            this.name = name;
            this.targetStateName = targetStateName;
            this.allowedRoles = allowedRoles;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getTargetStateName() {
            return targetStateName;
        }

        public void setTargetStateName(String targetStateName) {
            this.targetStateName = targetStateName;
        }

        public List<String> getAllowedRoles() {
            return allowedRoles;
        }

        public void setAllowedRoles(List<String> allowedRoles) {
            this.allowedRoles = allowedRoles;
        }

    }
}