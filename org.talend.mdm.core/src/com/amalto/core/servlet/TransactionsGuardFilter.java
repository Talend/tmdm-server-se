/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.transaction.Transaction;
import com.amalto.core.storage.transaction.TransactionManager;

/**
 * A Servlet filter checking if the current thread has a current transaction
 * before and after executing the request.  
 */
public class TransactionsGuardFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(TransactionsGuardFilter.class);

    private static final String THROW_EXCEPTIONS_PARAM = "throw.exceptions."; //$NON-NLS-1$
    
    private Map<Phase, Boolean> throwExceptions = new HashMap<Phase, Boolean>();
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException { 
        throwExceptions.put(Phase.BEFORE, Boolean.parseBoolean(filterConfig.getInitParameter(Phase.BEFORE.getFilterConfig())));
        throwExceptions.put(Phase.AFTER, Boolean.parseBoolean(filterConfig.getInitParameter(Phase.AFTER.getFilterConfig())));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        doCheck(Phase.BEFORE);
        chain.doFilter(request, response);
        doCheck(Phase.AFTER);
    }

    @Override
    public void destroy() {
    }
    
    protected void doCheck(Phase when){
        Transaction t = getThreadTransactionIfExists();
        if(t != null){
            LOG.warn(String.format("Attached transaction intercepted %s executing a request: Thread %s is attached to transaction %s", when.toString(), Thread.currentThread().getName(), t)); //$NON-NLS-1$
            LOG.warn(t.getCreationStackTrace());
            if(throwExceptions.get(when)){
                throw new IllegalStateException("Current thread is attached to a transaction"); //$NON-NLS-1$
            }
        }
    }
    
    protected Transaction getThreadTransactionIfExists(){
        TransactionManager tm = ServerContext.INSTANCE.get().getTransactionManager();
        if(tm.hasTransaction()){
            return tm.currentTransaction();
        }
        return null;
    }
    
    private enum Phase {
        BEFORE,
        AFTER;
        
        public String getFilterConfig(){
            return THROW_EXCEPTIONS_PARAM + this.toString();
        }
    }

}
