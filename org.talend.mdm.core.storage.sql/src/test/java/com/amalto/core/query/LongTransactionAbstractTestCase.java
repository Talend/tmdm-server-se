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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;
import com.amalto.core.storage.transaction.Transaction;
import com.amalto.core.storage.transaction.TransactionManager;


/**
 * Abstract test case for all LongTransaction*TestCase.
 * 
 * Contains helpers and minimal setup / tearDown  
 */
public class LongTransactionAbstractTestCase extends StorageTestCase {
    
    @Override
    public void setUp() throws Exception {
        // override to avoid storage.begin in superclass setup
    }
    
    @Override
    public void tearDown() throws Exception{
        storage.begin();
        storage.delete(UserQueryBuilder.from(country).getSelect());
        storage.commit();
        storage.end();
    }
    
    protected static int countCountries() {
        FetchRunnable fetch = new FetchRunnable();
        fetch.run();
        return fetch.getResultSize();
    }
    
    
    protected static class LongTaskRunnable implements Runnable {

        private final List<Runnable> subTasks = new ArrayList<Runnable>();
        
        private final ThreadPoolExecutor threadPool;
        
        public LongTaskRunnable(ThreadPoolExecutor threadPool){
            this.threadPool = threadPool;
        }
        
        @Override
        public void run() {
            for(Runnable subTask : subTasks){
                Future<?> future = threadPool.submit(subTask);
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        
        public void addTask(Runnable r){
            this.subTasks.add(r);
        }
        
    }
    
    
    protected static class UpdateRunnable implements Runnable {
        
        private int id;
        private String transactionId;
        
        public UpdateRunnable(String transactionId, int id){
            this.transactionId = transactionId;
            this.id = id;
        }
        
        @Override
        public void run() {
            TransactionManager tm = ServerContext.INSTANCE.get().getTransactionManager();
            Transaction transaction = tm.get(this.transactionId);
            tm.associate(transaction);
            storage.begin();
            storage.update(createDataRecord(id));
            storage.commit();
            tm.dissociate(transaction);
        }
    }
    
    protected static class UpdateRunnableWithFetchOnSystemStorage implements Runnable {
        
        private int id;
        private String transactionId;
        
        public UpdateRunnableWithFetchOnSystemStorage(String transactionId, int id){
            this.transactionId = transactionId;
            this.id = id;
        }
        
        @Override
        public void run() {
            TransactionManager tm = ServerContext.INSTANCE.get().getTransactionManager();
            Transaction transaction = tm.get(this.transactionId);
            tm.associate(transaction);

            // a simple fetch before ...
            systemStorage.begin();
            ComplexTypeMetadata type = systemRepository.getComplexType("menu-pOJO");
            systemStorage.fetch(UserQueryBuilder.from(type).select(type.getField("unique-id")).getSelect());
            systemStorage.commit();
            
            storage.begin();
            storage.update(createDataRecord(id));            
            storage.commit();
            
            tm.dissociate(transaction);
        }
    }
    
    protected static class UpdateRunnableWithInsertOnSystemStorage implements Runnable {
        
        private int id;
        private String transactionId;
        
        public UpdateRunnableWithInsertOnSystemStorage(String transactionId, int id){
            this.transactionId = transactionId;
            this.id = id;
        }
        
        @Override
        public void run() {
            TransactionManager tm = ServerContext.INSTANCE.get().getTransactionManager();
            Transaction transaction = tm.get(this.transactionId);
            tm.associate(transaction);

            systemStorage.begin();
            systemStorage.update(createRole());
            systemStorage.commit();
            
            storage.begin();
            storage.update(createDataRecord(id));            
            storage.commit();
            
            tm.dissociate(transaction);
        }
    }
    
    protected static class CommitRunnable implements Runnable {
        
        private String transactionId;
        
        public CommitRunnable(String transactionId){
            this.transactionId = transactionId;
        }
        
        @Override
        public void run() {
            TransactionManager tm = ServerContext.INSTANCE.get().getTransactionManager();
            Transaction transaction = tm.get(this.transactionId);
            tm.associate(transaction);
            transaction.commit();
        }
    }
    
    protected static class FetchRunnable implements Runnable{
        
        private int resultSize;
        
        @Override
        public void run() {
            storage.begin();
            StorageResults result = storage.fetch(UserQueryBuilder.from(country).select(country.getField("id")).getSelect());
            this.resultSize = result.getCount();
            storage.commit();
            storage.end();
        }
        
        public int getResultSize(){
            return this.resultSize;
        }
    }
    
    protected static DataRecord createDataRecord(int id){
        DataRecord result = new DataRecord(country, new DataRecordMetadataImpl(System.currentTimeMillis(), "T1")); //$NON-NLS-1$
        result.set(country.getField("id"), id); //$NON-NLS-1$
        result.set(country.getField("name"), UUID.randomUUID().toString()); //$NON-NLS-1$
        result.set(country.getField("creationTime"), new Timestamp(new Date().getTime())); //$NON-NLS-1$
        result.set(country.getField("creationDate"), new Timestamp(new Date().getTime())); //$NON-NLS-1$
        return result;
    }
    
    protected static DataRecord createRole(){
        ComplexTypeMetadata type = systemRepository.getComplexType("role-pOJO");
        DataRecord result = new DataRecord(type, new DataRecordMetadataImpl(System.currentTimeMillis(), "T1")); //$NON-NLS-1$
        result.set(type.getField("unique-id"), UUID.randomUUID().toString()); //$NON-NLS-1$
        result.set(type.getField("description"), UUID.randomUUID().toString()); //$NON-NLS-1$
        result.set(type.getField("digest"), UUID.randomUUID().toString()); //$NON-NLS-1$
        result.set(type.getField("name"), UUID.randomUUID().toString()); //$NON-NLS-1$
        return result;
    }
    
    protected static int countCountriesInAnotherThread() throws Exception {
        FetchRunnable fetch = new FetchRunnable();
        Thread t = new Thread(fetch);
        t.start();
        t.join();
        return fetch.getResultSize();
    }

}
