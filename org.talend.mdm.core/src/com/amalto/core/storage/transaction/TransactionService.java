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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
            if (transaction.isFree()) {
                transaction.commit();
            } else {
                final ExecutorService service = Executors.newSingleThreadExecutor();
                Future<Object> futureResult = null;
                try {
                    futureResult = service.submit(() -> {
                        while (transaction.isFree()) {
                            LOGGER.error("******* while commit."); //$NON-NLS-1$
                            transaction.commit();
                        }
                        return null;
                    });
                    String result = (String)futureResult.get(20, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    LOGGER.warn("Timeout during commit.", e); //$NON-NLS-1$
                } catch (Exception e) {
                    LOGGER.warn("Issue found during commit.", e); //$NON-NLS-1$
                } finally {
                    service.shutdown();
                }
            }
        }
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
            //if (((MDMTransaction)transaction).isFrees) 
            {
                LOGGER.error("******* begin rollback.");
                transaction.rollback();
            } /*else {
                final ExecutorService service = Executors.newSingleThreadExecutor();
                Future<Object> futureResult = null;
                try {
                    LOGGER.error("******* while rollback 1.");
                    futureResult = service.submit(() -> {
                        while (transaction.isFree()) {
                            LOGGER.error("******* while rollback."); //$NON-NLS-1$
                            transaction.rollback();
                        }
                        return null;
                    });
                    String result = (String)futureResult.get(20, TimeUnit.SECONDS);
                } catch (TimeoutException e) {
                    LOGGER.warn("Timeout during rollback.", e); //$NON-NLS-1$
                } catch (Exception e) {
                    LOGGER.warn("Issue found during rollback.", e); //$NON-NLS-1$
                } finally {
                    service.shutdown();
                }
            }*/
        }
    }
}
