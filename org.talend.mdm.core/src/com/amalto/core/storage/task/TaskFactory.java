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

package com.amalto.core.storage.task;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContextHolder;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.save.SaverSession;
import com.amalto.core.save.context.SaverSource;
import com.amalto.core.storage.Storage;

public class TaskFactory {

    private static final Logger LOGGER = Logger.getLogger(TaskFactory.class);

    public static Task createStagingTask(StagingConfiguration config, StagingTaskExecutionListener listener){
        Storage stagingStorage = config.getOrigin();
        MetadataRepository userRepository = config.getUserRepository();
        SaverSource source = config.getSource();
        SaverSession.Committer committer = config.getCommitter();
        Storage destinationStorage = config.getDestination();
        Filter filter = config.getFilter();
        ClosureExecutionStats stats = new ClosureExecutionStats();
        List<Task> tasks = new ArrayList<Task>();
        // Adds match & merge (if available)
        try {
            Class<?> clazz = Class.forName("com.amalto.core.storage.task.MatchMergeTask"); //$NON-NLS-1$
            Constructor<?> constructor = clazz.getConstructor(Storage.class,
                    Storage.class,
                    MetadataRepository.class,
                    ClosureExecutionStats.class,
                    Filter.class,
                    String.class);
            Object task = constructor.newInstance(stagingStorage,
                    destinationStorage,
                    userRepository,
                    stats,
                    filter,
                    source.getUserName());
            tasks.add((Task) task);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Could not find match & merge extension, feature will be disabled.");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not find match & merge extension: exception occurred.", e);
            }
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Expected a constructor but could not find it.", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Expected a constructor but could not invoke it.", e);
        } catch (InstantiationException e) {
            throw new RuntimeException("Expected a constructor but could not instantiate it.", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Expected a constructor but could not access it.", e);
        } catch (Exception e) {
            LOGGER.error("Unexpected error when building a match & merge task.", e);
        }
        // Adds MDM validation task
        tasks.add(new MDMValidationTask(stagingStorage,
                destinationStorage,
                destinationStorage.getMetadataRepository(),
                source,
                committer,
                stats,
                filter));
        StagingTask stagingTask = new StagingTask(TaskSubmitterFactory.getSubmitter(),
                stagingStorage,
                tasks,
                stats,
                listener);
        stagingTask.setSecurityContext(SecurityContextHolder.getContext());
        return stagingTask;
    }
}
