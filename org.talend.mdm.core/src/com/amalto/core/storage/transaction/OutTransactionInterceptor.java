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

import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;

public class OutTransactionInterceptor extends TransactionInterceptor {

    public OutTransactionInterceptor() {
        super(Phase.SEND);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        try {
            final TransactionState transactionState = message.getExchange().get(TransactionState.class);
            if (transactionState != null) {
                transactionState.postRequest();
            }
        } finally {
            message.getExchange().remove(TransactionState.class);
        }
    }

    @Override
    public void handleFault(Message message) {
        try {
            final TransactionState transactionState = message.getExchange().get(TransactionState.class);
            if (transactionState != null) {
                transactionState.postRequest();
            }
        } finally {
            message.getExchange().remove(TransactionState.class);
        }
    }

}
