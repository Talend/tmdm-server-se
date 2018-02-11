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

import com.amalto.core.storage.Storage;

/**
 * A MDM transaction: it composed of multiple underlying {@link StorageTransaction}, each of them handles {@link Storage}
 * specific transaction operations.
 * @see StorageTransaction
 */
public interface Transaction {

    /**
     * Describes all locking strategies to be applied to records read/written withing this transaction boundaries.
     */
    enum LockStrategy {
        /**
         * No lock: implementation of strategy is not required to perform any lock/synchronization (e.g. for concurrent
         * updates).
         */
        NO_LOCK,
        /**
         * Records read within this transaction boundaries should be considered as locked for update. Implementation
         * is responsible to decide best lock strategy.
         */
        LOCK_FOR_UPDATE
    }

    /**
     * Configures what is the transaction "life time": how long it should remain active in MDM server.
     */
    enum Lifetime {
        /**
         * A short life transaction: usually life time is bound to the HTTP request life time.
         */
        AD_HOC,
        /**
         * A "long" transaction: transaction was externally started and must remain active till commit/rollback actions
         * are called.
         */
        LONG
    }

    /**
     * <p>
     * Change the current {@link com.amalto.core.storage.transaction.Transaction.LockStrategy lock strategy} for this
     * transaction. Implementations should support change for lock strategy over this transaction lifetime.
     * </p>
     * <p>
     * Calling this method propagates the lock strategy to all contained
     * {@link com.amalto.core.storage.transaction.StorageTransaction} instances.
     * </p>
     * 
     * @param lockStrategy A {@link com.amalto.core.storage.transaction.Transaction.LockStrategy strategy}
     * @see com.amalto.core.storage.transaction.StorageTransaction#setLockStrategy(com.amalto.core.storage.transaction.Transaction.LockStrategy)
     */
    void setLockStrategy(LockStrategy lockStrategy);

    /**
     * @return The current {@link com.amalto.core.storage.transaction.Transaction.LockStrategy} for this transaction.
     */
    LockStrategy getLockStrategy();

    /**
     * @return A unique identifier for this MDM transaction.
     */
    String getId();

    /**
     * Begin the transaction: all underlying {@link StorageTransaction#begin()} get also called.
     */
    void begin();

    /**
     * Commit the transaction: all underlying {@link StorageTransaction#commit()} get also called.
     */
    void commit();

    /**
     * Rollback the transaction: all underlying {@link StorageTransaction#rollback()} get also called.
     */
    void rollback();

    /**
     * Includes a {@link Storage} inside the scope of this transaction.
     *
     * @param storage A storage implementation.
     * @return The {@link StorageTransaction} instance that handles all operations relative to the <code>storage</code>
     *         in this transaction.
     * @see #exclude(com.amalto.core.storage.Storage)
     * @throws IllegalArgumentException If <code>storage</code> parameter does not support transactions.
     * @see com.amalto.core.storage.Storage#getCapabilities()
     */
    StorageTransaction include(Storage storage);

    /**
     * Excludes a {@link Storage} inside the scope of this transaction. If transaction lifetime is {@link Lifetime#AD_HOC}
     * transaction <b>MUST</b> be removed from the active transactions in {@link TransactionManager}.
     *
     * @param storage A storage implementation.
     * @return The {@link StorageTransaction} instance that handles all operations relative to the <code>storage</code>
     *         in this transaction. If <code>storage</code> wasn't part of transaction, returns <code>null</code>.
     * @see #include(com.amalto.core.storage.Storage)
     */
    StorageTransaction exclude(Storage storage);

    /**
     * @return <code>true</code> if any of the contained storage transaction has failed. <code>false</code> otherwise.
     * @see StorageTransaction#hasFailed()
     */
    boolean hasFailed();
    
    /**
     * @return the stack-trace of the current thread when this transaction
     * was created. 
     * 
     * This feature is enabled only if the logger for
     * com.amalto.core.server.MDMTransaction is in DEBUG 
     * otherwise creation stack-traces are not recorded at transaction creation
     */
    String getCreationStackTrace();

    /**
     * @return The {@link com.amalto.core.storage.transaction.Transaction.Lifetime lifetime} of this transaction.
     */
    Lifetime getLifetime();
}
