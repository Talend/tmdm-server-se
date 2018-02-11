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

import java.util.List;

/**
 * The MDM transaction manager: provides all methods relative to MDM transactions.
 * @see Transaction
 */
public interface TransactionManager {
    /**
     * Initializes all needed resources for transaction management. Calling multiple times this method should only
     * initialize once.
     */
    void init();

    /**
     * Clean up all resources and rollbacks any pending transactions. Implementations should provide all necessary code
     * to allow this method to <b>always</b> complete (no matter what exception occurred during close).
     */
    void close();

    /**
     * @return All ids of currently active transactions (id returned by {@link com.amalto.core.storage.transaction.Transaction#getId()}).
     */
    List<String> list();

    /**
     * Creates a new {@link Transaction} with a expected life time. This method also associate the newly created
     * transaction with calling thread.
     *
     * @param lifetime The expected life time of the new transaction.
     * @return A new transaction ready to be used.
     * @see Transaction#include(com.amalto.core.storage.Storage)
     * @see #associate(Transaction)
     * @throws IllegalArgumentException If <code>lifetime</code> parameter is <code>null</code>.
     */
    Transaction create(Transaction.Lifetime lifetime);

    /**
     * <p>
     * Creates a new {@link Transaction} with a expected life time. This method also associate the newly created
     * transaction with calling thread.
     * </p>
     * <p>
     * Returned transaction <b>MUST</b> have as id the value provided in parameter <code>transactionID</code>. If a
     * transaction with <code>transactionID</code> already exists, the previously created transaction is returned.
     * </p>
     *
     * @param lifetime      The expected life time of the new transaction.
     * @param transactionID The id for the newly created transaction, or a previously created one if <code>transactionID</code>
     *                      already exists.
     * @return A new transaction ready to be used.
     * @throws IllegalArgumentException If <code>lifetime</code> parameter is <code>null</code>.
     * @see Transaction#include(com.amalto.core.storage.Storage)
     * @see #associate(Transaction)
     * @see #create(com.amalto.core.storage.transaction.Transaction.Lifetime)
     */
    Transaction create(Transaction.Lifetime lifetime, String transactionID);

    /**
     * @param transactionId A transaction id (see {@link Transaction#getId()}.
     * @return The transaction with given id, or <code>null</code> if not found, or <code>null</code> if transaction
     *         is no longer managed by this transaction manager (i.e. it was committed or rollbacked).
     * @throws IllegalArgumentException If <code>transactionId</code> parameter is <code>null</code>.
     */
    Transaction get(String transactionId);

    /**
     * Removes a transaction from the scope of this transaction manager.
     *
     * @param transaction The transaction to remove.
     * @throws IllegalArgumentException If <code>transaction</code> parameter is <code>null</code>.
     */
    void remove(Transaction transaction);

    /**
     * @return The current {@link Transaction} associated with the current thread. If no transaction is currently linked
     * to current thread, a new transaction with a life time {@link com.amalto.core.storage.transaction.Transaction.Lifetime#AD_HOC}
     * is created.
     */
    Transaction currentTransaction();

    /**
     * Links ('associate') current thread to the <code>transaction</code>. All consecutive calls to {@link #currentTransaction()}
     * will return the <code>transaction</code> parameter.
     * @param transaction A transaction to link to {@link Thread#currentThread()}
     * @return The associated transaction.
     * @throws IllegalArgumentException If <code>transaction</code> parameter is <code>null</code>.
     */
    Transaction associate(Transaction transaction);

    /**
     * <p>
     * Removes link from ('dissociate') current thread to the <code>transaction</code>. All consecutive calls to {@link #hasTransaction()}}
     * will return <code>false</code>.
     * </p>
     * <p>
     * If <code>transaction</code> is not associated with current thread, calling this method has no effect.
     * </p>
     * @param transaction A transaction linked to {@link Thread#currentThread()}
     * @throws IllegalArgumentException If <code>transaction</code> parameter is <code>null</code>.
     */
    void dissociate(Transaction transaction);

    /**
     * @return <code>true</code> if a transaction is associated with current thread, <code>false</code> otherwise.
     */
    boolean hasTransaction();
}
