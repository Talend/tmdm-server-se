/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amalto.core.storage.transaction.Transaction;
import com.amalto.core.storage.transaction.Transaction.Lifetime;


public class MDMTransactionManagerTestCase {
    
    private MDMTransactionManager transactionManager;
    
    @Before
    public void setup() throws Exception {
        transactionManager = new MDMTransactionManager();
    }
    
    @After
    public void teardown() throws Exception {
        if(transactionManager != null){
            transactionManager.close();
        }
    }
    
    @Test
    public void testCreateAdHocTransaction() throws Exception {
        Transaction transaction = transactionManager.create(Lifetime.AD_HOC);
        Assert.assertNotNull(transaction);
        Assert.assertEquals(Lifetime.AD_HOC, transaction.getLifetime());
        Assert.assertNotNull(transaction.getId());
        
        Transaction transaction2 = transactionManager.get(transaction.getId());
        Assert.assertSame(transaction, transaction2);
        
        Transaction transaction3 = transactionManager.currentTransaction();
        Assert.assertSame(transaction, transaction3);
        
        Assert.assertEquals(1, transactionManager.list().size());
    }
    
    @Test
    public void testNoCurrentTransaction() throws Exception {
        Transaction transaction = transactionManager.currentTransaction();
        Assert.assertNotNull(transaction);
        Assert.assertEquals(Lifetime.AD_HOC, transaction.getLifetime());
        Assert.assertNotNull(transaction.getId());
        
        Transaction transaction2 = transactionManager.currentTransaction();
        Assert.assertSame(transaction, transaction2);
    }
    
    @Test
    public void testAssociateSameTransactionToMultipleThreads() throws Exception {
        Assert.assertFalse(transactionManager.hasTransaction());
        final Transaction transaction = transactionManager.create(Lifetime.AD_HOC);
        Assert.assertTrue(transactionManager.hasTransaction());
        Thread t = new Thread(new Runnable(){
            @Override
            public void run() {
                Assert.assertFalse(transactionManager.hasTransaction());
                transactionManager.associate(transaction);
                Assert.assertTrue(transactionManager.hasTransaction());
                Transaction transaction2 = transactionManager.currentTransaction();
                Assert.assertSame(transaction, transaction2);
                transactionManager.dissociate(transaction);
                Assert.assertFalse(transactionManager.hasTransaction());
            }
        });
        t.start();
        t.join();
        Transaction transaction2 = transactionManager.currentTransaction();
        Assert.assertSame(transaction, transaction2);
        Assert.assertEquals(1, transactionManager.list().size());
    }
    
    @Test
    public void testSeveralAdHocTransactions() throws Exception {
        final Transaction transaction = transactionManager.create(Lifetime.AD_HOC);
        Assert.assertEquals(transaction, transactionManager.currentTransaction());
        
        final Transaction transaction2 = transactionManager.create(Lifetime.AD_HOC);
        Assert.assertNotSame(transaction, transaction2);
        Assert.assertEquals(transaction2, transactionManager.currentTransaction());
        
        Assert.assertEquals(2, transactionManager.list().size());
    }
    
    @Test
    public void testRemoveTransactionsAssociatedToSeveralThreads() throws Exception {
        final Transaction transaction = transactionManager.create(Lifetime.AD_HOC);
        for(int i=0; i<10; i++){
            Thread t = new Thread(new Runnable(){
                @Override
                public void run() {
                    transactionManager.associate(transaction);
                }
            });
            t.start();
            t.join();
        }
        transactionManager.remove(transaction);
        Assert.assertEquals(0, transactionManager.list().size());
        Assert.assertFalse(transactionManager.hasTransaction());
    }
    
    @Test
    public void testLongTransactions() throws Exception {
        Transaction longTransaction = transactionManager.create(Lifetime.LONG);
        Assert.assertTrue(transactionManager.hasTransaction());
        transactionManager.dissociate(longTransaction);
        Assert.assertFalse(transactionManager.hasTransaction());
        transactionManager.associate(longTransaction);
        Assert.assertTrue(transactionManager.hasTransaction());
        transactionManager.remove(longTransaction);
        Assert.assertFalse(transactionManager.hasTransaction());
    }

    @Test
    public void testCreateLongThenAdhocTransaction() throws Exception {
        Transaction longTransaction = transactionManager.create(Lifetime.LONG);
        Assert.assertTrue(transactionManager.hasTransaction());
        
        Transaction adhocTransaction = transactionManager.create(Lifetime.AD_HOC);
        Assert.assertNotSame(longTransaction, adhocTransaction);
        Assert.assertTrue(transactionManager.hasTransaction());
        
        Assert.assertEquals(adhocTransaction, transactionManager.currentTransaction());
        
        transactionManager.remove(adhocTransaction);
        Assert.assertTrue(transactionManager.hasTransaction());
        Assert.assertEquals(longTransaction, transactionManager.currentTransaction());
    }
    
    @Test
    public void testCreateAdhocThenLongTransaction() throws Exception {
        Transaction adhocTransaction = transactionManager.create(Lifetime.AD_HOC);
        Assert.assertTrue(transactionManager.hasTransaction());
        
        Transaction longTransaction = transactionManager.create(Lifetime.LONG);
        Assert.assertNotSame(longTransaction, adhocTransaction);
        Assert.assertTrue(transactionManager.hasTransaction());
        
        Assert.assertEquals(longTransaction, transactionManager.currentTransaction());
    }

    /**
     * TMDM-9242 Error while running demo job 'LoadProductFamilies' when using transaction and Validate, throw
     * java.util.NoSuchElementException when transactionManager has no transaction
     * 
     */
    @Test
    public void testDissociate() throws Exception {
        Transaction transaction = transactionManager.create(Lifetime.AD_HOC);
        Assert.assertTrue(transactionManager.hasTransaction());

        transactionManager.dissociate(transaction);
        Assert.assertFalse(transactionManager.hasTransaction());

        transactionManager.dissociate(transaction);
    }

}
