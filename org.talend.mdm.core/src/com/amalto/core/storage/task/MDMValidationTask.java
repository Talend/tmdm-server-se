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

import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.Select;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverSession;
import com.amalto.core.save.UserAction;
import com.amalto.core.save.context.DocumentSaver;
import com.amalto.core.save.context.SaverSource;
import com.amalto.core.save.context.StorageDocument;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.util.User;
import com.amalto.core.util.UserHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;
import org.talend.mdm.commmon.util.core.ICoreConstants;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.isNull;
import static com.amalto.core.query.user.UserQueryBuilder.or;
import static com.amalto.core.query.user.UserStagingQueryBuilder.status;

public class MDMValidationTask extends MetadataRepositoryTask {

    private static final boolean GENERATE_UPDATE_REPORT;

    private static final int CONSUMER_POOL_SIZE;

    private static final Logger LOGGER = Logger.getLogger(MDMValidationTask.class);

    private static final int COMMIT_SIZE;        

    private final SaverSource source;

    private final SaverSession.Committer committer;

    private final Storage destinationStorage;

    private int recordsCount;

    private SecurityContext context;       

    static {
        // staging.validation.updatereport tells whether validation should generate update reports
        String value = MDMConfiguration.getConfiguration().getProperty("staging.validation.updatereport"); //$NON-NLS-1$
        GENERATE_UPDATE_REPORT = value == null ? true : Boolean.valueOf(value);
        // staging.validation.pool tells how many threads do staging validation
        value = MDMConfiguration.getConfiguration().getProperty("staging.validation.pool"); //$NON-NLS-1$
        CONSUMER_POOL_SIZE = value == null ? 2 : Integer.valueOf(value);
        // staging.validation.commit tells when validation should perform intermediate commits.
        value = MDMConfiguration.getConfiguration().getProperty("staging.validation.commit"); //$NON-NLS-1$
        COMMIT_SIZE = value == null ? 1000 : Integer.valueOf(value);
    }

    public MDMValidationTask(Storage storage,
                             Storage destinationStorage,
                             MetadataRepository repository,
                             SaverSource source,
                             SaverSession.Committer committer,
                             ClosureExecutionStats stats,
                             Filter filter) {
        super(storage, repository, stats, filter);
        this.source = source;
        this.committer = committer;
        this.destinationStorage = destinationStorage;
    }

    @Override
    public String toString() {
        return "CLUSTERS VALIDATION"; //$NON-NLS-1$
    }

    @Override
    protected Task createTypeTask(ComplexTypeMetadata type) {
        Closure closure = new MDMValidationTask.MDMValidationClosure(source, committer, destinationStorage);
        Select select = filter.doFilter(this, type);
        try {
            storage.begin();
            StorageResults records = storage.fetch(select); // Expects an active transaction here
            try {
                recordsCount += records.getCount();
            } finally {
                records.close();
                storage.commit();
            }
        } catch (Exception e) {
            storage.rollback();
            throw new RuntimeException(e);
        }
        // Build task needed for type (might include AutoIncrement update)
        List<Task> tasks = new LinkedList<>();
        tasks.add(new MultiThreadedTask(type.getName(),
                storage,
                select,
                CONSUMER_POOL_SIZE,
                closure,
                stats,
                context));
        for (FieldMetadata keyField : type.getKeyFields()) {
            // Only adds a AutoIncrement update if the type contains at least one AutoIncrement field
            if (EUUIDCustomType.AUTO_INCREMENT.getName().equals(keyField.getType().getName())) {
                tasks.add(new AutoIncrementUpdateTask(storage, destinationStorage, type, context));
                break;
            }
        }
        return new SequentialTasks(tasks.toArray(new Task[tasks.size()]));
    }

    @Override
    public int getRecordCount() {
        return recordsCount;
    }

    @Override
    public Condition getDefaultFilter() {
        return or(
                eq(status(), StagingConstants.TASK_RESOLVED_RECORD),
                or(
                        eq(status(), StagingConstants.SUCCESS_MERGED_RECORD),
                        or(
                                eq(status(), StagingConstants.NEW),
                                or(
                                        isNull(status()),
                                        or(
                                                eq(status(), StagingConstants.FAIL_VALIDATE_CONSTRAINTS),
                                                eq(status(), StagingConstants.FAIL_VALIDATE_VALIDATION)
                                        )
                                )
                        )
                )
        );
    }

    @Override
    public boolean hasFailed() {
        return false;
    }

    @Override
    public void setSecurityContext(SecurityContext context) {
        this.context = context;
    }

    private class MDMValidationClosure implements Closure {

        private final SaverSource source;

        private final SaverSession.Committer committer;

        private final Storage destinationStorage;

        private SaverSession session;

        private int commitCount;
        
        private List<DataRecord> stagingRecords = new ArrayList<DataRecord>();
        
        private boolean isResolve;

        public MDMValidationClosure(SaverSource source, SaverSession.Committer committer, Storage destinationStorage) {
            this.source = source;
            this.committer = committer;
            this.destinationStorage = destinationStorage;
        }

        public synchronized void begin() {            
            session = SaverSession.newSession(source);
            session.begin(destinationStorage.getName(), committer);
            storage.begin();
        }

        public void execute(DataRecord stagingRecord, ClosureExecutionStats stats) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(Thread.currentThread() + " is validating record: " + stagingRecord);
            }
            StorageDocument document = new StorageDocument(destinationStorage.getName(),
                    destinationStorage.getMetadataRepository(),
                    stagingRecord);
            DocumentSaverContext context = session.getContextFactory().create(destinationStorage.getName(),
                    destinationStorage.getName(),
                    "Staging", //$NON-NLS-1$
                    document,
                    true,
                    true,
                    GENERATE_UPDATE_REPORT,
                    false,
                    false);
            context.setTaskId(stagingRecord.getRecordMetadata().getTaskId());
            context.setUserAction(UserAction.AUTO_STRICT);
            DocumentSaver saver = context.createSaver();
            Map<String, String> recordProperties = stagingRecord.getRecordMetadata().getRecordProperties();
            try {
                if (!isAllowedAccess(stagingRecord, source.getLegitimateUser())) {
                    throw new Exception("User '" + source.getLegitimateUser() + "' is not allowed to write '" + stagingRecord.getType().getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                saver.save(session, context);
                commitCount++;
                recordProperties.put(Storage.METADATA_STAGING_STATUS, StagingConstants.SUCCESS_VALIDATE);
                recordProperties.put(Storage.METADATA_STAGING_ERROR, StringUtils.EMPTY);
                storage.update(stagingRecord);
                if(isResolve) {
                    session.end(committer);
                } else {
                    stagingRecords.add(stagingRecord);
                    if ((commitCount % COMMIT_SIZE == 0)) {
                        end(stats);
                        begin();
                    }
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Validation success: record id #"
                            + stagingRecord.get(stagingRecord.getType().getKeyFields().iterator().next())
                            + " (" + stagingRecord.getType().getName() + ")");
                }
                stats.reportSuccess();
            } catch (Exception e) {
                recordProperties.put(Storage.METADATA_STAGING_STATUS, StagingConstants.FAIL_VALIDATE_VALIDATION);
                StringWriter exceptionMessages = new StringWriter();
                Throwable current = e;
                while (current != null) {
                    exceptionMessages.append(current.getMessage());
                    current = current.getCause();
                    if (current != null) {
                        exceptionMessages.append('\n');
                    }
                }
                recordProperties.put(Storage.METADATA_STAGING_ERROR, exceptionMessages.toString());                
                storage.update(stagingRecord);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Validation failed: record id #"
                            + stagingRecord.get(stagingRecord.getType().getKeyFields().iterator().next())
                            + " (" + stagingRecord.getType().getName() + ")", e);
                }
                if(isResolve) {
                    session.abort(committer);
                } else {
                    // if a batch has 403 records due to FK constraint
                    // allow other 403 records (failed validation) to be resolved
                    stagingRecords.add(stagingRecord);
                }
                stats.reportError();
            }
        }

        private boolean isAllowedAccess(DataRecord stagingRecord, String userName) {
            User user = new User();
            user.setUserName(userName);
            Collection<String> currentRoles = UserHelper.getInstance().getOriginalRole(user);
            if (currentRoles != null && currentRoles.contains(ICoreConstants.ADMIN_PERMISSION)) {
                return true;
            }
            List<String> writeUsers = stagingRecord.getType().getWriteUsers();
            if (writeUsers != null) {
                List<String> hideUserRoles = stagingRecord.getType().getHideUsers();
                for (String writeUser : writeUsers) {
                    if (hideUserRoles.contains(writeUser)) {
                        continue;
                    }
                    if (currentRoles != null && currentRoles.contains(writeUser)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        @Override
        public void cancel() {
        }
        
        private void resolve(ClosureExecutionStats stats) {
            isResolve = true;
            for(DataRecord stagingRecord : stagingRecords) {
                try {
                    session = SaverSession.newSession(source);
                    session.begin(destinationStorage.getName(), committer);
                    storage.begin();
                    execute(stagingRecord, stats);
                    storage.commit();
                } catch (Exception e) {
                    storage.rollback();
                    // unexpected error when commit into staging
                    LOGGER.error("Could not commit changes into staging storage.", e); //$NON-NLS-1$
                }
            }
            stagingRecords.clear();
            isResolve = false;
        }

        public synchronized void end(ClosureExecutionStats stats) {
            try {
                session.end(committer);
                storage.commit();
            } catch (Exception e) {
                storage.rollback();
                // something prevent committing the whole batch, try again one by one
                if(COMMIT_SIZE > 1 && stagingRecords.size() > 0) {
                    session.abort(committer);
                    resolve(stats);
                } else {                    
                    // This is unexpected (session should only contain records that won't fail commit).
                    LOGGER.error("Could not commit changes.", e); //$NON-NLS-1$
                    session.abort(committer);                    
                }
            }
        }

        public Closure copy() {
            return new MDMValidationClosure(source, committer, destinationStorage);
        }
    }

}


