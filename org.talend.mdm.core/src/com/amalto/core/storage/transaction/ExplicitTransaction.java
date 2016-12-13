/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.transaction;

import com.amalto.core.server.ServerContext;

public class ExplicitTransaction implements TransactionState {

    private final String transactionID;

    public ExplicitTransaction(String transactionID) {
        this.transactionID = transactionID;
    }

    @Override
    public void preRequest() {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.get(transactionID);
        if (transaction == null) {
            transaction = transactionManager.create(Transaction.Lifetime.LONG, transactionID);
            transaction.begin();
        }
        transactionManager.associate(transaction);
    }

    @Override
    public void postRequest() {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.get(transactionID);
        if (transaction == null) {
            throw new IllegalStateException("Transaction '" + transactionID + "' no longer exists.");
        }
        transactionManager.dissociate(transaction);
    }

    @Override
    public void cancelRequest() {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.get(transactionID);
        if (transaction == null) {
            throw new IllegalStateException("Transaction '" + transactionID + "' no longer exists.");
        }
        transactionManager.dissociate(transaction);
    }
}
