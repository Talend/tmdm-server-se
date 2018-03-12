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

import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.task.staging.SerializableList;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import javax.ws.rs.*;

import java.util.List;

@Path("/transactions")
@Api("Transactions")
public class TransactionService {

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
            transaction.commit();
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
            transaction.rollback();
        }
    }
}
