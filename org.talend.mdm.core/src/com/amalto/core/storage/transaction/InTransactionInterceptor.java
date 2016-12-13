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

public class InTransactionInterceptor extends TransactionInterceptor {

    public InTransactionInterceptor() {
        super(Phase.PRE_INVOKE);
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        final TransactionState state = getState(message);
        try {
            state.preRequest();
        } finally {
            message.getExchange().put(TransactionState.class, state);
        }
    }

    @Override
    public void handleFault(Message message) {
        final TransactionState state = getState(message);
        try {
            state.cancelRequest();
        } finally {
            message.getExchange().put(TransactionState.class, state);
        }
    }
}
