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

package com.amalto.core.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import org.apache.log4j.Logger;

import com.amalto.core.storage.transaction.Transaction;
import com.amalto.core.storage.transaction.TransactionManager;

public class MDMTransactionManager implements TransactionManager {

    private static final Logger LOGGER = Logger.getLogger(MDMTransactionManager.class);

    private static final Map<Thread, Stack<Transaction>> currentTransactions = new HashMap<Thread, Stack<Transaction>>();

    private static final Map<String, Transaction> activeTransactions = new HashMap<String, Transaction>();

    private boolean isInitialized = false;

    @Override
    public List<String> list() {
        synchronized (activeTransactions) {
            List<String> activeTransactionIds = new ArrayList<String>(activeTransactions.size());
            activeTransactionIds.addAll(activeTransactions.keySet());
            return activeTransactionIds;
        }
    }

    @Override
    public Transaction create(Transaction.Lifetime lifetime) {
        return create(lifetime, UUID.randomUUID().toString());
    }

    @Override
    public Transaction create(Transaction.Lifetime lifetime, String transactionID) {
        if (lifetime == null) {
            throw new IllegalArgumentException("Life time argument cannot be null.");
        }
        Transaction transaction;
        synchronized (activeTransactions) {
            transaction = activeTransactions.get(transactionID);
            if (transaction == null) {
                transaction = new MDMTransaction(lifetime, transactionID);
            }
            activeTransactions.put(transaction.getId(), transaction);
        }
        synchronized (currentTransactions) {
            if(!currentTransactionsContains(transaction)){
                associate(transaction);
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("New transaction: " + transaction.toString());
        }
        return transaction;
    }

    @Override
    public Transaction get(String transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction id cannot be null.");
        }
        synchronized (activeTransactions) {
            return activeTransactions.get(transactionId);
        }
    }

    @Override
    public void remove(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null.");
        }
        if (transaction.hasFailed()) {
            LOGGER.error("Transaction " + transaction.getId() + " should not be removed from current transactions (it has failed).");
            transaction.rollback();
        }
        synchronized (currentTransactions) {
            for (Iterator<Thread> it = currentTransactions.keySet().iterator(); it.hasNext();) {
                Thread thread = it.next();
                Stack<Transaction> stack = getTransactionStack(thread);
                Iterator<Transaction> transIt = stack.iterator();
                while(transIt.hasNext()){
                    Transaction t = transIt.next();
                    if(t!=null && transaction.getId().equals(t.getId())){
                        transIt.remove();
                    }
                }
            }
        }
        // remove all of transactions by transaction id
        synchronized (activeTransactions) {
            Set<Entry<String, Transaction>> entries = activeTransactions.entrySet();
            Iterator<Entry<String, Transaction>>  iterator = entries.iterator();
            while(iterator.hasNext()){
                Entry<String, Transaction> t = iterator.next();
                if(t.getKey().equals(transaction.getId())){
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void init() {
        if (!isInitialized) {
            if (LOGGER.isDebugEnabled()) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            while (true) {
                                StringBuilder builder = new StringBuilder();
                                builder.append("Active transaction(s) (").append(activeTransactions.size()).append(")");
                                for (Map.Entry<String, Transaction> currentTransaction : activeTransactions.entrySet()) {
                                    builder.append(currentTransaction.getKey()).append('\n');
                                }
                                LOGGER.debug(builder.toString());
                                Thread.sleep(20000);
                            }
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
                t.start();
            }
            isInitialized = true;
        }
    }

    @Override
    public void close() {
        synchronized (activeTransactions) {
            Collection<Transaction> values = new ArrayList<Transaction>(activeTransactions.values());
            for (Transaction transaction : values) {
                try {
                    transaction.rollback();
                } catch (Throwable t) {
                    LOGGER.error("Unable to rollback active transaction #" + transaction.getId());
                    if(LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Unable to rollback transaction due to exception.", t);
                    }
                }
            }
        }
    }

    @Override
    public Transaction currentTransaction() {
        synchronized (currentTransactions) {
            Stack<Transaction> stack = this.getTransactionStack();
            if(stack.isEmpty()){
                return associate(create(Transaction.Lifetime.AD_HOC));
            }
            return stack.lastElement();
        }
    }

    @Override
    public Transaction associate(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null.");
        }
        synchronized (currentTransactions) {
            Stack<Transaction> transactionStack = getTransactionStack();
            if(!transactionStack.contains(transaction)){
                getTransactionStack().push(transaction);
            }
             
        }
        return transaction;
    }

    @Override
    public void dissociate(Transaction transaction) {
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction cannot be null.");
        }
        synchronized (currentTransactions) {
            Stack<Transaction> stack = getTransactionStack();
            if (stack.size() > 0 && stack.firstElement() == transaction) {
                stack.pop();
            }
        }
    }

    @Override
    public boolean hasTransaction() {
        return !getTransactionStack().isEmpty();
    }
    
    private Stack<Transaction> getTransactionStack(){
        return getTransactionStack(Thread.currentThread());
    }
    
    private Stack<Transaction> getTransactionStack(Thread t){
        Stack<Transaction> currentStack = currentTransactions.get(t);
        if(currentStack == null){
            currentStack = new Stack<Transaction>();
            currentTransactions.put(t, currentStack);
        }
        return currentStack;
    }
    
    private boolean currentTransactionsContains(Transaction t){
        return getTransactionStack().contains(t);
    }
}
