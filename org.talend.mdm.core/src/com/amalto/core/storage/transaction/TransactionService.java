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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.amalto.core.server.MDMTransaction;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.task.staging.SerializableList;
import org.apache.log4j.Logger;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/transactions")
@Api("Transactions")
public class TransactionService {
    private static final Logger LOGGER = Logger.getLogger(TransactionService.class);
    /**
     * Lists all actives transactions ({@link Transaction.Lifetime#LONG} and {@link Transaction.Lifetime#AD_HOC}).
     * @return A space-separated list of transaction ids (as UUID).
     */
    @GET
    @Path("/")
    @ApiOperation("Lists all active transactions ids")
    public List<String> list() {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        List<String> list = transactionManager.list();
        return SerializableList.create(list, "transactions", "transaction_id"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Starts a new transaction and returns the id of the newly created transaction.
     * @return A transaction id (as UUID).
     */
    @PUT
    @Path("/")
    @ApiOperation("Begins a new transactions and returns the new transaction id")
    public String begin() {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.create(Transaction.Lifetime.LONG);
        transactionManager.dissociate(transaction);
        return transaction.getId();
    }

    /**
     * Commit the changes in transaction <code>transactionId</code>.
     * @param transactionId A valid transaction id.
     */
    @POST
    @Path("{id}/")
    @ApiOperation("Commits the transaction identified by the provided id")
    public void commit(
            @ApiParam("Transaction id") @PathParam("id") String transactionId) {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.get(transactionId);
        if (transaction != null) {
            if (((MDMTransaction)transaction).isFree.get()) {
                transaction.commit();
            } else {
                int randomNum = waitRandomSeconds();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("After sleep " + randomNum + " seconds, begin to commit for: " + transaction); //$NON-NLS-1$ //$NON-NLS-2$
                }
                transaction.commit();
            }
        }
    }

    private int waitRandomSeconds() {
        int randomNum = 0;
        try {
            // create random number between 3 and 6.
            randomNum = ThreadLocalRandom.current().nextInt(3, 7);
            TimeUnit.SECONDS.sleep(randomNum);
        } catch (InterruptedException e) {
            LOGGER.warn("Issue found during sleep.", e); //$NON-NLS-1$
        }
        return randomNum;
    }

    /**
     * Cancels (rollback) all changes done in <code>transactionId</code>.
     * @param transactionId A transaction id.
     */
    @DELETE
    @Path("{id}/")
    @ApiOperation("Rollbacks the transaction identified by the provided id")
    public void rollback(
            @ApiParam("Transaction id") @PathParam("id") String transactionId) {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction transaction = transactionManager.get(transactionId);
        if (transaction != null) {
            if (((MDMTransaction)transaction).isFree.get()) {
                transaction.rollback();
            } else {
                int randomNum = waitRandomSeconds();
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("After sleep " + randomNum + " seconds, begin to rollback for: " + transaction); //$NON-NLS-1$ //$NON-NLS-2$
                }
                transaction.rollback();
            }
        }
    }
}
