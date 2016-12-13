/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.query;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.transaction.Transaction;
import com.amalto.core.storage.transaction.Transaction.Lifetime;
import com.amalto.core.storage.transaction.TransactionManager;

/**
 * Tests transaction management thread safety
 *
 */
public class LongTransactionConcurrencyTestCase extends LongTransactionAbstractTestCase {
    
    /**
     * begin
     *    + insert 
     *    + insert
     *    + insert
     *    + ...
     * commit
     * 
     * in a pool of threads
     */
    public void testSeveralLongTransactionsInSeveralThreadsJoinBeforeCommit() throws Exception {
        int nbThreads = 10;
        int nbTransactions = 4;
        int nbTasks = 40;
        int nbTasksPerTransaction = nbTasks / nbTransactions;
        String[] transactionIds = new String[nbTransactions];
        TransactionManager tm = ServerContext.INSTANCE.get().getTransactionManager();
        for(int i=0; i<nbTransactions; i++){
            Transaction longTransaction = tm.create(Lifetime.LONG);
            longTransaction.begin();
            transactionIds[i] = longTransaction.getId();
        }
        Future<?>[] tasks = new Future<?>[nbTasks];
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(nbThreads, nbThreads, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        int taskId = 0;
        for(int i=0; i<nbTransactions; i++){
            String transactionId = transactionIds[i];
            for(int j=0; j<nbTasksPerTransaction; j++){
                int id = ((i+1) * 1000) + j;
                tasks[taskId++] = threadPool.submit(new UpdateRunnable(transactionId, id));
                
            }
        }
        for(int i=0; i<nbTasks; i++){
            tasks[i].get();
        }
        for(int i=0; i<nbTransactions; i++){
            String transactionId = transactionIds[i];
            Transaction transaction = tm.get(transactionId);
            transaction.commit();
        }
        threadPool.shutdown();
        Assert.assertEquals(nbTasks, countCountries());
    }
    
    /**
     *  + begin insert insert insert ... commit
     *  + begin insert insert insert ... commit
     *  + begin insert insert insert ... commit
     *  + begin insert insert insert ... commit
     *  
     *  in a pool of threads
     */
    public void testSeveralLongTransactionsInSeveralThreadsWithCommit() throws Exception {
        int nbThreads = 5;
        int nbTransactions = 10;
        int nbTasks = 100;
        int nbTasksPerTransaction = nbTasks / nbTransactions;
        String[] transactionIds = new String[nbTransactions];
        TransactionManager tm = ServerContext.INSTANCE.get().getTransactionManager();
        for(int i=0; i<nbTransactions; i++){
            Transaction longTransaction = tm.create(Lifetime.LONG);
            longTransaction.begin();
            transactionIds[i] = longTransaction.getId();
        }
        Thread[] threads = new Thread[nbTransactions];
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(nbThreads, nbThreads, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        for(int i=0; i<nbTransactions; i++){
            String transactionId = transactionIds[i];
            LongTaskRunnable longTask = new LongTaskRunnable(threadPool);
            for(int j=0; j<nbTasksPerTransaction; j++){
                int id = ((i+1) * 1000) + j;
                longTask.addTask(new UpdateRunnable(transactionId, id));
            }
            longTask.addTask(new CommitRunnable(transactionId));
            threads[i] = new Thread(longTask);
            threads[i].start();
            
        }
        for(int i=0; i<nbTransactions; i++){
            threads[i].join();
        }
        threadPool.shutdown();
        Assert.assertEquals(nbTasks, countCountries());
    }
    
    /**
     *  + begin insert storage1 fetch storage2 insert storage1 ... commit
     *  + begin insert storage1 fetch storage2 insert storage1 ... commit
     *  + begin insert storage1 fetch storage2 insert storage1 ... commit
     *  + begin insert storage1 fetch storage2 insert storage1 ... commit
     *  
     *  in a pool of threads
     */
    public void testSeveralLongTransactionsInSeveralThreadsWithSeveralStorages() throws Exception {
        int nbThreads = 50;
        int nbTransactions = 5;
        int nbTasks = 50;
        int nbTasksPerTransaction = nbTasks / nbTransactions;
        String[] transactionIds = new String[nbTransactions];
        TransactionManager tm = ServerContext.INSTANCE.get().getTransactionManager();
        for(int i=0; i<nbTransactions; i++){
            Transaction longTransaction = tm.create(Lifetime.LONG);
            longTransaction.begin();
            transactionIds[i] = longTransaction.getId();
        }
        Thread[] threads = new Thread[nbTransactions];
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(nbThreads, nbThreads, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        for(int i=0; i<nbTransactions; i++){
            String transactionId = transactionIds[i];
            LongTaskRunnable longTask = new LongTaskRunnable(threadPool);
            for(int j=0; j<nbTasksPerTransaction; j++){
                int id = ((i+1) * 1000) + j;
                longTask.addTask(new UpdateRunnableWithFetchOnSystemStorage(transactionId, id));
            }
            longTask.addTask(new CommitRunnable(transactionId));
            threads[i] = new Thread(longTask);
            threads[i].start();
            
        }
        for(int i=0; i<nbTransactions; i++){
            threads[i].join();
        }
        threadPool.shutdown();
        Assert.assertEquals(0, tm.list().size());
        Assert.assertEquals(nbTasks, countCountries());
    }
    
    /**
     *  + begin insert storage1 insert storage2 insert storage1 ... commit
     *  + begin insert storage1 insert storage2 insert storage1 ... commit
     *  + begin insert storage1 insert storage2 insert storage1 ... commit
     *  + begin insert storage1 insert storage2 insert storage1 ... commit
     *  
     *  in a pool of threads
     */
    public void testSeveralLongTransactionsInSeveralThreadsWithSeveralStoragesWrite() throws Exception {
        int nbThreads = 50;
        int nbTransactions = 20;
        int nbTasks = 100;
        int nbTasksPerTransaction = nbTasks / nbTransactions;
        String[] transactionIds = new String[nbTransactions];
        TransactionManager tm = ServerContext.INSTANCE.get().getTransactionManager();
        for(int i=0; i<nbTransactions; i++){
            Transaction longTransaction = tm.create(Lifetime.LONG);
            longTransaction.begin();
            transactionIds[i] = longTransaction.getId();
        }
        Thread[] threads = new Thread[nbTransactions];
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(nbThreads, nbThreads, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        for(int i=0; i<nbTransactions; i++){
            String transactionId = transactionIds[i];
            LongTaskRunnable longTask = new LongTaskRunnable(threadPool);
            for(int j=0; j<nbTasksPerTransaction; j++){
                int id = ((i+1) * 1000) + j;
                longTask.addTask(new UpdateRunnableWithInsertOnSystemStorage(transactionId, id));
            }
            longTask.addTask(new CommitRunnable(transactionId));
            threads[i] = new Thread(longTask);
            threads[i].start();
            
        }
        for(int i=0; i<nbTransactions; i++){
            threads[i].join();
        }
        threadPool.shutdown();
        Assert.assertEquals(0, tm.list().size());
        Assert.assertEquals(nbTasks, countCountries());
    }

}
