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

package com.amalto.core.storage.hibernate;

import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.transaction.Transaction;
import com.amalto.core.storage.transaction.TransactionManager;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.context.spi.CurrentSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import java.util.HashMap;
import java.util.Map;

public class MDMTransactionSessionContext implements CurrentSessionContext {

    private final static Map<SessionFactory, HibernateStorage> declaredStorages = new HashMap<SessionFactory, HibernateStorage>();

    private static final Logger LOGGER = Logger.getLogger(MDMTransactionSessionContext.class);

    private final SessionFactoryImplementor factory;

    public MDMTransactionSessionContext(SessionFactoryImplementor factory) {
        this.factory = factory;
    }

    public static void declareStorage(HibernateStorage storage, SessionFactory factory) {
        synchronized (declaredStorages) {
            declaredStorages.put(factory, storage);
        }
    }

    @Override
    public Session currentSession() throws HibernateException {
        if (LOGGER.isTraceEnabled()) {
            long openCount = factory.getStatistics().getSessionOpenCount();
            LOGGER.trace("Currently open session count: " + openCount);
        }
        synchronized (declaredStorages) {
            TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
            Transaction transaction = transactionManager.currentTransaction();
            HibernateStorageTransaction storageTransaction = (HibernateStorageTransaction) transaction.include(declaredStorages.get(factory));
            if (storageTransaction.getInitiatorThread() != Thread.currentThread()) {
                LOGGER.error("Current thread ('" + Thread.currentThread() + "') did not initiate transaction (was '" + storageTransaction.getInitiatorThread() + "').");
            }
            return storageTransaction.getSession();
        }
    }
    
    public static void forgetStorage(SessionFactory sessionFactory){
        synchronized (declaredStorages) {
            declaredStorages.remove(sessionFactory);
        }
    }
}
