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

import org.apache.log4j.Logger;

import com.amalto.core.server.ServerContext;

public class ImplicitTransactionState implements TransactionState {

    public static final TransactionState INSTANCE = new ImplicitTransactionState();
    
    private static final Logger LOGGER = Logger.getLogger(ImplicitTransactionState.class);

    private ImplicitTransactionState() {
    }

    @Override
    public void preRequest() {
    }

    @Override
    public void postRequest() {
        this.checkNoCurrentTransaction();
    }

    @Override
    public void cancelRequest() {
        this.checkNoCurrentTransaction();
    }
    
    private void checkNoCurrentTransaction(){
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        if (transactionManager.hasTransaction()) {
            String msg = "A non-transactional (auto-commit) operation has an active " + //$NON-NLS-1$
                    "transaction after operation completion."; //$NON-NLS-1$
            LOGGER.error(msg);
            Transaction t = transactionManager.currentTransaction();
            if(t != null){
                LOGGER.error("Transaction creation stackTrace : \n" + t.getCreationStackTrace()); //$NON-NLS-1$
            }
            throw new IllegalStateException(msg);
        }
        
    }
}
