/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.task.staging;

import static com.amalto.core.query.user.UserQueryBuilder.alias;
import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;
import static com.amalto.core.query.user.UserQueryBuilder.gte;
import static com.amalto.core.query.user.UserQueryBuilder.isNull;
import static com.amalto.core.query.user.UserQueryBuilder.or;
import static com.amalto.core.query.user.UserStagingQueryBuilder.status;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.save.DefaultCommitter;
import com.amalto.core.save.context.StorageSaverSource;
import com.amalto.core.server.MDMContextAccessor;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.StagingStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.task.DefaultFilter;
import com.amalto.core.storage.task.Filter;
import com.amalto.core.storage.task.StagingConfiguration;
import com.amalto.core.storage.task.StagingConstants;
import com.amalto.core.storage.task.Task;
import com.amalto.core.storage.task.TaskFactory;
import com.amalto.core.storage.task.TaskSubmitterFactory;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

public class DefaultStagingTaskService implements StagingTaskServiceDelegate {
    
    private StagingTaskManager stagingTaskManager;
    
    public DefaultStagingTaskService(){
        this.stagingTaskManager = MDMContextAccessor.getApplicationContext().getBean(StagingTaskManager.class);
    }
    
    public StagingContainerSummary getContainerSummary() {
        String dataContainer;
        String dataModel;
        try {
            dataContainer = Util.getUserDataCluster();
            dataModel = Util.getUserDataModel();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access current session information", e);
        }
        if(StringUtils.isEmpty(dataContainer) || StringUtils.isEmpty(dataModel)){
            return null;
        }
        return getContainerSummary(dataContainer, dataModel);
    }
    
    public StagingContainerSummary getContainerSummary(String dataContainer, String dataModel) {
        final Storage storage = StagingTasksUtil.getStagingStorage(dataContainer);
        MetadataRepository repository = storage.getMetadataRepository();
        int allRecords = countAllInstancesByStatus(storage, repository);
        int newRecords = countInstancesByStatus(storage, repository, StagingConstants.NEW);
        int validRecords = countInstancesByStatus(storage, repository, true);
        int invalidRecords = countInstancesByStatus(storage, repository, false);

        StagingContainerSummary containerStagingSummary = new StagingContainerSummary();
        containerStagingSummary.setTotalRecord(allRecords);
        containerStagingSummary.setWaitingForValidation(newRecords);
        containerStagingSummary.setValidRecords(validRecords);
        containerStagingSummary.setInvalidRecords(invalidRecords);
        containerStagingSummary.setDataContainer(dataContainer);
        containerStagingSummary.setDataModel(dataModel);

        return containerStagingSummary;
    }
    
    public String startValidation() {
        String dataContainer;
        String dataModel;
        try {
            dataContainer = Util.getUserDataCluster();
            dataModel = Util.getUserDataModel();
        } catch (Exception e) {
            throw new RuntimeException("Failed to access current session information", e);
        }
        return startValidation(dataContainer, dataModel, DefaultFilter.INSTANCE);
    }

    public String startValidation(String dataContainer, String dataModel, Filter filter) {
        final Storage staging = StagingTasksUtil.getStagingStorage(dataContainer);
        final Storage user = StagingTasksUtil.getMasterStorage(dataContainer);
        String currentTaskId = this.stagingTaskManager.getCurrentTaskId(dataContainer);
        if(currentTaskId != null){
            return currentTaskId;
        }
        String userName;
        try {
            userName = LocalUser.getLocalUser().getUsername();
        } catch (XtentisException e) {
            throw new RuntimeException("Can not get current user information.", e);
        }
        StagingConfiguration stagingConfig = new StagingConfiguration(staging,
                new StorageSaverSource(userName),
                new DefaultCommitter(),
                user,
                filter);
        Task stagingTask = TaskFactory.createStagingTask(stagingConfig, this.stagingTaskManager);
        TaskSubmitterFactory.getSubmitter().submit(stagingTask);
        return stagingTask.getId();
    }

    public List<String> listCompletedExecutions(String dataContainer, String beforeDate, int start, int size) {
        return this.stagingTaskManager.listCompletedExecutions(dataContainer, beforeDate, start, size);
    }

    public ExecutionStatistics getCurrentExecutionStats(String dataContainer, String dataModel) {
        final String taskId = stagingTaskManager.getCurrentTaskId(dataContainer);
        if(taskId == null){
            return null;
        }
        return this.stagingTaskManager.getExecutionStats(dataContainer, taskId);
    }

    public void cancelCurrentExecution(String dataContainer, String dataModel) {
        final String taskId = stagingTaskManager.getCurrentTaskId(dataContainer);
        if(taskId == null){
            return;
        }
        this.stagingTaskManager.cancelTask(dataContainer, taskId);
    }
    
    public ExecutionStatistics getExecutionStats(String dataContainer, String dataModel, String executionId) {
        return this.stagingTaskManager.getExecutionStats(dataContainer, executionId);
    }
    
    private static int countAllInstancesByStatus(Storage storage, MetadataRepository repository) {
        int totalCount = 0;
        try {
            storage.begin();
            for (ComplexTypeMetadata currentType : repository.getUserComplexTypes()) {
                if (currentType.isInstantiable() && !StagingStorage.EXECUTION_LOG_TYPE.equals(currentType.getName())) {
                    UserQueryBuilder qb = from(currentType)
                            .select(alias(UserQueryBuilder.count(), "count")); //$NON-NLS-1$
                    if (hasMatchMergeConfiguration(currentType.getName())) {
                        // Don't include generated golden records in global statistics.
                        qb.where(
                                or(
                                        or(
                                                eq(status(), StagingConstants.NEW),
                                                isNull(status())
                                        ),
                                        eq(status(), StagingConstants.SUCCESS_MERGE_CLUSTERS)
                                )
                        );
                    }
                    StorageResults results = storage.fetch(qb.getSelect()); // Expects an active transaction here
                    try {
                        for (DataRecord result : results) {
                            totalCount += (Long) result.get("count"); //$NON-NLS-1$
                        }
                    } finally {
                        results.close();
                    }
                }
            }
            storage.commit();
        } catch (Exception e) {
            storage.rollback();
            throw new RuntimeException(e);
        }
        return totalCount;
    }
    
    private static int countInstancesByStatus(Storage storage, MetadataRepository repository, String status) {
        int totalCount = 0;
        try {
            storage.begin();
            for (ComplexTypeMetadata currentType : repository.getUserComplexTypes()) {
                if (currentType.isInstantiable() && !StagingStorage.EXECUTION_LOG_TYPE.equals(currentType.getName())) {
                    UserQueryBuilder qb = from(currentType)
                            .select(alias(UserQueryBuilder.count(), "count")); //$NON-NLS-1$
                    if (StagingConstants.NEW.equals(status)) {
                        qb.where(or(eq(status(), status), isNull(status())));
                    } else {
                        qb.where(eq(status(), status));
                    }
                    StorageResults results = storage.fetch(qb.getSelect());
                    try {
                        for (DataRecord result : results) {
                            totalCount += (Long) result.get("count"); //$NON-NLS-1$
                        }
                    } finally {
                        results.close();
                    }
                }
            }
            storage.commit();
        } catch (Exception e) {
            storage.rollback();
            throw new RuntimeException(e);
        }
        return totalCount;
    }

    private static int countInstancesByStatus(Storage storage, MetadataRepository repository, boolean valid) {
        int totalCount = 0;
        try {
            storage.begin();
            for (ComplexTypeMetadata currentType : repository.getUserComplexTypes()) {
                if (currentType.isInstantiable() && !StagingStorage.EXECUTION_LOG_TYPE.equals(currentType.getName())) {
                    UserQueryBuilder qb = from(currentType)
                            .select(alias(UserQueryBuilder.count(), "count")); //$NON-NLS-1$
                    if (valid) {
                        qb.where(eq(status(), StagingConstants.SUCCESS_VALIDATE));
                    } else {
                        qb.where(gte(status(), StagingConstants.FAIL));
                    }

                    StorageResults results = storage.fetch(qb.getSelect());
                    try {
                        for (DataRecord result : results) {
                            totalCount += (Long) result.get("count"); //$NON-NLS-1$
                        }
                    } finally {
                        results.close();
                    }
                }
            }
            storage.commit();
        } catch (Exception e) {
            storage.rollback();
            throw new RuntimeException(e);
        }
        return totalCount;
    }
    
    // TODO Move to a better place (this method is more a helper/util method)
    public static boolean hasMatchMergeConfiguration(String typeName) {
        // Read configuration from database
        Server server = ServerContext.INSTANCE.get();
        Storage systemStorage = server.getStorageAdmin().get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM);
        if (systemStorage == null) {
            return false;
        }
        systemStorage.begin();
        try {
            MetadataRepository metadataRepository = systemStorage.getMetadataRepository();
            String internalTypeName = ClassRepository.format("MatchRulePOJO"); //$NON-NLS-1$
            ComplexTypeMetadata matchRuleType = metadataRepository.getComplexType(internalTypeName);
            if (matchRuleType == null) {
                return false; // Might happen for CE edition.
            }
            UserQueryBuilder qb = from(matchRuleType).where(eq(matchRuleType.getField("unique-id"), typeName)); //$NON-NLS-1$
            StorageResults results = systemStorage.fetch(qb.getSelect());
            return results.getSize() != 0;
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception during configuration read.", e);
        } finally {
            systemStorage.commit();
        }
    }
    
    public StagingTaskManager getStagingTaskManager() {
        return stagingTaskManager;
    }

    
    public void setStagingTaskManager(StagingTaskManager stagingTaskManager) {
        this.stagingTaskManager = stagingTaskManager;
    }
}
