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

package com.amalto.core.storage.transaction;

import org.apache.log4j.Logger;

import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.Storage;

/**
 * Represents a transaction for a given {@link com.amalto.core.storage.Storage}. A {@link Transaction} may contain
 * several {@link StorageTransaction} instances (in case the transaction implies several storages or MDM data containers).
 * @see Transaction
 */
public abstract class StorageTransaction {
    
    private static final Logger LOG = Logger.getLogger(StorageTransaction.class);

    private final Object[] lockChange = new Object[0];

    protected boolean isAutonomous;

    // Keep it private, use getLockStrategy() in sub classes to ensure correct synchronization.
    private Transaction.LockStrategy lockStrategy = Transaction.LockStrategy.NO_LOCK;
    
    // Upper level transaction coordinating this storage transaction
    private Transaction coordinator = null;

    public StorageTransaction(){
        this.coordinator = ServerContext.INSTANCE.get().getTransactionManager().currentTransaction();
    }
    
    /**
     * @return The {@link Storage} managed by this storage transaction.
     */
    public abstract Storage getStorage();

    /**
     * Starts a transaction on the underlying storage.
     */
    public abstract void begin();

    /**
     * Commits pending changes to the underlying storage.
     * 
     * @see #autonomous()
     * @see #dependent()
     * @throws java.lang.RuntimeException Implementations are expected to throw {@link java.lang.RuntimeException} in
     * case commit can not be completed. If such event occurs, {@link #hasFailed()} is expected to return
     * <code>true</code>.
     */
    public void commit() {
        if (isAutonomous) {
            if(coordinator != null) {
                coordinator.exclude(getStorage());
            }
            else {
                LOG.warn("No coordinator found when commiting storage transaction " + this); //$NON-NLS-1$
            }
        }
    }

    /**
     * Rollbacks (revert) all pending changes on the underlying storage. After this method completes, the method
     * {@link #hasFailed()} <b>must</b> return <code>false</code>.
     * @see #autonomous()
     * @see #dependent()
     */
    public void rollback() {
        if (isAutonomous) {
            if(coordinator != null){
                coordinator.exclude(getStorage());
            }
            else {
                LOG.warn("No coordinator found when rolling back storage transaction " + this); //$NON-NLS-1$
            }
        }
    }

    /**
     * @return A {@link StorageTransaction} that may participate in a {@link Transaction} and returned storage transaction
     *         <b>DO</b> decide to commit. In fact returned transaction is expected to perform as if it is the only running
     *         transaction in MDM, thus {@link #commit()} or {@link #rollback()} <b>MUST</b> perform action when called.
     */
    public StorageTransaction autonomous() {
        isAutonomous = true;
        return this;
    }

    /**
     * @return A {@link StorageTransaction} that participates in a {@link Transaction} and returned storage transaction
     * <b>DO NOT</b> decide to commit. In fact returned transaction is expected to perform no action when {@link #commit()}
     * or {@link #rollback()} is called.
     */
    public StorageTransaction dependent() {
        isAutonomous = false;
        return this;
    }

    /**
     * @return <code>true</code> if any {@link #commit()} previously failed and no {@link #rollback()} was consequently
     * called. <code>false</code> if no commit was previously done OR if commit completed without any problem OR if
     * rollback was called to revert all commit changes.
     */
    public abstract boolean hasFailed();

    public void setLockStrategy(Transaction.LockStrategy lockStrategy) {
        synchronized (lockChange) {
            this.lockStrategy = lockStrategy;
        }
    }

    protected Transaction.LockStrategy getLockStrategy() {
        synchronized (lockChange) {
            return lockStrategy;
        }
    }
    
    /**
     * @return the application level {@link Transaction} coordinating this storage transaction
     */
    public Transaction getCoordinator(){
        return this.coordinator;
    }
}
