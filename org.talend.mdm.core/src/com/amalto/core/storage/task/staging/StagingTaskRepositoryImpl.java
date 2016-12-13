/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.task.staging;

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;
import static com.amalto.core.query.user.UserQueryBuilder.isNull;
import static com.amalto.core.query.user.UserQueryBuilder.lte;
import static com.amalto.core.query.user.UserQueryBuilder.or;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import com.amalto.core.query.user.OrderBy;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;

public class StagingTaskRepositoryImpl implements StagingTaskRepository {

    private static final Logger LOGGER = Logger.getLogger(StagingTaskRepositoryImpl.class);
    
    private final Map<String, String> currentTasksIdCache = new HashMap<String, String>();
    
    @Override
    public String getCurrentTaskId(String dataContainer) {
        final Storage staging = StagingTasksUtil.getStagingStorage(dataContainer);
        final ComplexTypeMetadata executionType = StagingTasksUtil.getTaskExecutionType(staging);
        return this.getRunningTaskId(staging, executionType);
    }
    
    @Override
    public void saveTaskAsCancelled(String dataContainer, String taskId) {
        final Storage staging = StagingTasksUtil.getStagingStorage(dataContainer);
        ComplexTypeMetadata executionType = StagingTasksUtil.getTaskExecutionType(staging);
        final UserQueryBuilder qb = from(executionType)
                .where(eq(executionType.getField("id"), taskId)); //$NON-NLS-1$
        try {
            staging.begin();
            StorageResults results = staging.fetch(qb.getSelect());
            Iterator<DataRecord> resultsIterator = results.iterator();
            if(resultsIterator.hasNext()){
                DataRecord record = resultsIterator.next();
                record.set(executionType.getField("completed"), Boolean.TRUE);
                record.set(executionType.getField("end_time"), System.currentTimeMillis());
                staging.update(record);
            }
            staging.commit();
        }
        catch(Exception e){
            staging.rollback();
        }
    }
    
    @Override
    public ExecutionStatistics getExecutionStats(String dataContainer, String executionId) {
        final Storage staging = StagingTasksUtil.getStagingStorage(dataContainer);
        ComplexTypeMetadata executionType = StagingTasksUtil.getTaskExecutionType(staging);
        UserQueryBuilder qb = from(executionType)
                .where(eq(executionType.getField("id"), executionId)); //$NON-NLS-1$
        ExecutionStatistics status = new ExecutionStatistics();
        try {
            staging.begin();
            StorageResults results = staging.fetch(qb.getSelect()); // Expects an active transaction here
            try {
                for (DataRecord result : results) {
                    status.setId(String.valueOf(result.get("id"))); //$NON-NLS-1$
                    Date start_time = new Date((Long) result.get("start_time")); //$NON-NLS-1$
                    Date end_time = new Date((Long) result.get("end_time")); //$NON-NLS-1$
                    status.setStartDate(StagingTasksUtil.formatDate(start_time));
                    status.setEndDate(StagingTasksUtil.formatDate(end_time));
                    status.setInvalidRecords(((BigDecimal) result.get("error_count")).intValue()); //$NON-NLS-1$
                    status.setRunningTime(StagingTasksUtil.formatElapsedTime(end_time.getTime() - start_time.getTime()));
                    int recordCount = ((BigDecimal) result.get("record_count")).intValue(); //$NON-NLS-1$
                    status.setProcessedRecords(recordCount);
                    status.setTotalRecords(recordCount);
                }
            } finally {
                results.close();
            }
            staging.commit();
        } catch (Exception e) {
            try {
                staging.rollback();
            } catch (Exception rollbackException) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unable to rollback transaction.", rollbackException);
                }
            }
            // TMDM-7970: Ignore all storage related errors for statistics
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not get staging storage execution statistics.", e);
            }
        }
        return status;
    }
    
    
    protected String getRunningTaskId(final Storage staging, final ComplexTypeMetadata executionType){
        synchronized(this.currentTasksIdCache){
            final String cacheKey = staging.getName();
            String cachedId = currentTasksIdCache.get(cacheKey);
            if(cachedId != null){
                if(!isCompleted(staging, executionType, cachedId)){
                    if(LOGGER.isDebugEnabled()){
                        LOGGER.debug(String.format("Retrieved running task from cache, id is %s", cachedId));
                    }
                    return cachedId;
                }
                else {
                    if(LOGGER.isDebugEnabled()){
                        LOGGER.debug("Invalidating current running task cache because it is completed");
                    }
                    currentTasksIdCache.remove(cacheKey);
                }
            }
            String taskId = fetchRunningTaskId(staging, executionType);
            if(taskId != null){
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug(String.format("Retrieved current running tasks id %s", taskId));
                }
                currentTasksIdCache.put(cacheKey, taskId);
                return taskId;
            }
            if(LOGGER.isDebugEnabled()){
                LOGGER.debug("No task currently running");
            }
            return null;
        }
    }
    
    protected String fetchRunningTaskId(final Storage staging, final ComplexTypeMetadata executionType){
        String result = null;
        UserQueryBuilder qb = from(executionType)
                .select(executionType.getField("id"))
                .where(or(
                        eq(executionType.getField("completed"), "false"),
                        isNull(executionType.getField("completed"))));
        try {
            staging.begin();
            StorageResults results = staging.fetch(qb.getSelect());
            Iterator<DataRecord> iterator = results.iterator();
            if(iterator.hasNext()){
                DataRecord record = iterator.next();
                result = (String)record.get("id");
            }
            staging.commit();
        }
        catch(Exception e){
            staging.rollback();
        }
        return result;
    }
    
    protected boolean isCompleted(final Storage staging, final ComplexTypeMetadata executionType, final String taskId){
        boolean result = true;
        final UserQueryBuilder qb = from(executionType)
                .select(executionType.getField("completed")) //$NON-NLS-1$
                .select(executionType.getField("end_time")) //$NON-NLS-1$
                .where(eq(executionType.getField("id"), taskId)); //$NON-NLS-1$
        try {
            staging.begin();
            StorageResults results = staging.fetch(qb.getSelect());
            Iterator<DataRecord> iterator = results.iterator();
            if(iterator.hasNext()){
                DataRecord record = iterator.next();
                Boolean completed = (Boolean)record.get("completed"); //$NON-NLS-1$
                Long endTime = (Long)record.get("end_time"); //$NON-NLS-1$
                if(LOGGER.isDebugEnabled()){
                    LOGGER.debug(String.format("TaskId %s : completed=%s, endTime=%s", taskId, String.valueOf(completed), String.valueOf(endTime))); //$NON-NLS-1$
                }
                if(completed == null || (completed == Boolean.FALSE && endTime == null)){
                    result = false;
                }
            }
            else {
                LOGGER.warn(String.format("Unknown task %s", taskId)); //$NON-NLS-1$
            }
            staging.commit();
        }
        catch(Exception e){
            staging.rollback();
        }
        return result;
    }

    @Override
    public void saveNewTask(String dataContainer, String taskId, long startTime) {
        final Storage stagingStorage = StagingTasksUtil.getStagingStorage(dataContainer);
        final ComplexTypeMetadata executionType = StagingTasksUtil.getTaskExecutionType(stagingStorage);  
        DataRecord execution = new DataRecord(executionType, UnsupportedDataRecordMetadata.INSTANCE);
        execution.set(executionType.getField("id"), taskId); //$NON-NLS-1$
        execution.set(executionType.getField("start_time"), startTime); //$NON-NLS-1$
        try {
            stagingStorage.begin();
            stagingStorage.update(execution);
            stagingStorage.commit();
        } catch (Exception e) {
            stagingStorage.rollback();
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void saveTaskAsCompleted(String dataContainer, String taskId, long endMatchTime, int errorCount, int recordCount){
        final Storage stagingStorage = StagingTasksUtil.getStagingStorage(dataContainer);
        final ComplexTypeMetadata executionType = StagingTasksUtil.getTaskExecutionType(stagingStorage);
        final UserQueryBuilder qb = from(executionType)
                .where(eq(executionType.getField("id"), taskId)); //$NON-NLS-1$
        try {
            stagingStorage.begin();
            StorageResults results = stagingStorage.fetch(qb.getSelect());
            Iterator<DataRecord> resultsIterator = results.iterator();
            if(resultsIterator.hasNext()){
                DataRecord record = resultsIterator.next();
                record.set(executionType.getField("end_match_time"), endMatchTime); //$NON-NLS-1$
                record.set(executionType.getField("end_time"), System.currentTimeMillis()); //$NON-NLS-1$
                record.set(executionType.getField("error_count"), new BigDecimal(errorCount)); //$NON-NLS-1$
                record.set(executionType.getField("record_count"), new BigDecimal(recordCount)); //$NON-NLS-1$
                record.set(executionType.getField("completed"), Boolean.TRUE); //$NON-NLS-1$
                stagingStorage.update(record);
            }
            stagingStorage.commit();
        }
        catch(Exception e){
            stagingStorage.rollback();
        }
    }
    
    @Override
    public List<String> listCompletedExecutions(String dataContainer, String beforeDate, int start, int size) {
        final Storage staging = StagingTasksUtil.getStagingStorage(dataContainer);
        final ComplexTypeMetadata executionType = StagingTasksUtil.getTaskExecutionType(staging);
        final UserQueryBuilder qb = from(executionType)
                .select(executionType.getField("id")) //$NON-NLS-1$
                .where(eq(executionType.getField("completed"), "true")); //$NON-NLS-1$ //$NON-NLS-2$
        if (beforeDate != null && beforeDate.trim().length() > 0) {
            try {
                long beforeTime = StagingTasksUtil.parseUserDate(beforeDate).getTime();
                qb.where(lte(executionType.getField("start_time"), String.valueOf(beforeTime))); //$NON-NLS-1$
            } catch (ParseException e) {
                throw new RuntimeException("Could not parse '" + beforeDate + "' as date.", e);
            }
        }
        if (start >= 0) {
            qb.start(start);
        }
        if (size >= 0) {
            qb.limit(size);
        }
        qb.orderBy(executionType.getField("start_time"), OrderBy.Direction.ASC); //$NON-NLS-1$
        List<String> taskIds = Collections.emptyList();
        try {
            staging.begin();
            StorageResults results = staging.fetch(qb.getSelect());
            try {
                if (size > 0) {
                    taskIds = new ArrayList<String>(size);
                } else {
                    taskIds = new LinkedList<String>();
                }
                for (DataRecord result : results) {
                    taskIds.add(String.valueOf(result.get("id"))); //$NON-NLS-1$
                }
            } finally {
                results.close();
            }
            staging.commit();
        } catch (Exception e) {
            try {
                staging.rollback();
            } catch (Exception rollbackException) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unable to rollback transaction.", rollbackException);
                }
            }
            // TMDM-7970: Ignore all storage related errors for statistics
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not get staging storage execution statistics.", e);
            }
        }
        return taskIds;
    }
}
