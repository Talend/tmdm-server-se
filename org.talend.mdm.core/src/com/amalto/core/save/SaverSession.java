/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.save;

import com.amalto.core.ejb.ItemPOJO;
import com.amalto.core.save.context.DefaultSaverSource;
import com.amalto.core.save.context.SaverContextFactory;
import com.amalto.core.save.context.SaverSource;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SaverSession {

    private static final Map<String, SaverSource> saverSourcePerUser = new HashMap<String, SaverSource>();

    private final SaverContextFactory contextFactory;

    private final Map<String, Set<ItemPOJO>> itemsPerDataCluster = new HashMap<String, Set<ItemPOJO>>();

    private final SaverSource dataSource;

    private static SaverSource defaultSaverSource;

    private static DefaultCommitter defaultCommitter;

    private boolean hasMetAutoIncrement = false;

    private SaverSession(SaverSource dataSource) {
        this.dataSource = dataSource;
        contextFactory = new SaverContextFactory();
    }

    /**
     * @param saverSource A custom implementation of {@link SaverSource}.
     * @return A {@link SaverSession} with a custom {@link SaverSource} implementation.
     */
    public static SaverSession newSession(SaverSource saverSource) {
        return new SaverSession(saverSource);
    }

    /**
     * @return A {@link SaverSession} with default user.
     */
    public static synchronized SaverSession newSession() {
        if (defaultSaverSource == null) {
            defaultSaverSource = new DefaultSaverSource();
        }
        return new SaverSession(defaultSaverSource);
    }

    /**
     * @param userName A MDM user name
     * @return A {@link SaverSession} with the user name passed as parameter.
     */
    public static SaverSession newUserSession(String userName) {
        SaverSource saverSource = saverSourcePerUser.get(userName);
        if (saverSource == null) {
            saverSource = new DefaultSaverSource(userName);
            saverSourcePerUser.put(userName, saverSource);
        }
        SaverSource dataSource = saverSource;
        return new SaverSession(dataSource);
    }

    public SaverContextFactory getContextFactory() {
        return contextFactory;
    }

    /**
     * Start a transaction for this session on a given data cluster.
     *
     * @param dataCluster The data cluster where a transaction should be started.
     */
    public void begin(String dataCluster) {
        begin(dataCluster, getDefaultCommitter());
    }

    private DefaultCommitter getDefaultCommitter() {
        if (defaultCommitter == null) {
            defaultCommitter = new DefaultCommitter();
        }
        return defaultCommitter;
    }

    /**
     * Start a transaction for this session on a given data cluster.
     *
     * @param dataCluster The data cluster where a transaction should be started.
     * @param committer   A {@link Committer} committer for interaction between save session and underlying storage.
     */
    public void begin(String dataCluster, Committer committer) {
        committer.begin(dataCluster);
    }

    /**
     * End this session (means commit on all data clusters where a transaction was started).
     */
    public void end() {
        end(getDefaultCommitter());
    }

    /**
     * End this session (means commit on all data clusters where a transaction was started).
     *
     * @param committer A {@link Committer} committer to use when committing transactions on underlying storage.
     */
    public void end(Committer committer) {
        for (Map.Entry<String, Set<ItemPOJO>> currentTransaction : itemsPerDataCluster.entrySet()) {
            String dataCluster = currentTransaction.getKey();
            begin(dataCluster, committer);
            for (ItemPOJO currentItemToCommit : currentTransaction.getValue()) {
                committer.save(currentItemToCommit, currentItemToCommit.getDataModelRevision()); // TODO Use data model revision for revision id?
            }
            committer.commit(dataCluster);
        }

        // If any change was made to data cluster "UpdateReport", route committed update reports.
        Set<ItemPOJO> updateReport = itemsPerDataCluster.get("UpdateReport"); //$NON-NLS-1$
        if (updateReport != null) {
            SaverSource saverSource = getSaverSource();
            for (ItemPOJO updateReportPOJO : updateReport) {
                saverSource.routeItem(updateReportPOJO.getDataClusterPOJOPK().getUniqueId(), updateReportPOJO.getConceptName(), updateReportPOJO.getItemIds());
            }
        }

        // Save current state of autoincrement when save is completed.
        if (hasMetAutoIncrement) {
            // TMDM-3964 : Auto-Increment Id can not be saved immediately to DB
            String dataCluster = XSystemObjects.DC_CONF.getName();
            begin(dataCluster, committer);
            SaverSource saverSource = getSaverSource();
            saverSource.saveAutoIncrement();
            committer.commit(dataCluster);
        }
    }

    /**
     * Adds a new record to be saved in this session.
     *
     * @param dataCluster         Data cluster where the record should be saved.
     * @param itemToSave          The item to save.
     * @param hasMetAutoIncrement <code>true</code> if AUTO_INCREMENT type has been met during save of <code>item</code>,
     *                            <code>false</code> otherwise.
     */
    public void save(String dataCluster, ItemPOJO itemToSave, boolean hasMetAutoIncrement) {
        if (!this.hasMetAutoIncrement) {
            this.hasMetAutoIncrement = hasMetAutoIncrement;
        }
        Set<ItemPOJO> itemsToSave = itemsPerDataCluster.get(dataCluster);
        if (itemsToSave == null) {
            itemsToSave = new HashSet<ItemPOJO>();
            itemsPerDataCluster.put(dataCluster, itemsToSave);
        }
        itemsToSave.add(itemToSave);
    }

    /**
     * @return {@link SaverSource} to interact with MDM server.
     */
    public SaverSource getSaverSource() {
        return dataSource;
    }

    /**
     * Causes current session to forget about all changes to save.
     */
    public void clear() {
        itemsPerDataCluster.clear();
    }

    /**
     * Aborts current transaction (means rollback on all data clusters where a transaction was started).
     */
    public void abort() {
        abort(getDefaultCommitter());
    }

    /**
     * Aborts current transaction (means rollback on all data clusters where a transaction was started).
     *
     * @param committer A {@link Committer} committer for interaction between save session and underlying storage.
     */
    public void abort(Committer committer) {
        for (Map.Entry<String, Set<ItemPOJO>> currentTransaction : itemsPerDataCluster.entrySet()) {
            String dataCluster = currentTransaction.getKey();
            committer.rollback(dataCluster);
        }
    }

    /**
     * Invalidate any type cache for the data model.
     *
     * @param dataModelName A data model name.
     */
    public void invalidateTypeCache(String dataModelName) {
        dataSource.invalidateTypeCache(dataModelName);
    }

    public interface Committer {
        /**
         * Begin a transaction on a data cluster
         *
         * @param dataCluster A data cluster name.
         */
        void begin(String dataCluster);

        /**
         * Commit a transaction on a data cluster
         *
         * @param dataCluster A data cluster name
         */
        void commit(String dataCluster);

        /**
         * Saves a MDM record for a given revision.
         *
         * @param item       The item to save.
         * @param revisionId A revision id.
         */
        void save(ItemPOJO item, String revisionId);

        /**
         * Rollbacks changes done on a data cluster.
         *
         * @param dataCluster Data cluster name.
         */
        void rollback(String dataCluster);
    }

}
