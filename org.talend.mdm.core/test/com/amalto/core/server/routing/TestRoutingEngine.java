/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server.routing;

import javax.jms.Message;

import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.routing.RoutingRulePOJOPK;
import com.amalto.core.server.api.RoutingEngine;
import com.amalto.core.util.XtentisException;

public class TestRoutingEngine implements RoutingEngine {

    private final RoutingEngine delegate;

    private int consumeCallCount = 0;

    public TestRoutingEngine(RoutingEngine delegate) {
        this.delegate = delegate;
    }

    public int getConsumeCallCount() {
        return consumeCallCount;
    }

    @Override
    public RoutingRulePOJOPK[] route(ItemPOJOPK itemPOJOPK) throws XtentisException {
        return delegate.route(itemPOJOPK);
    }

    @Override
    public void consume(Message message) {
        consumeCallCount++;
        delegate.consume(message);
    }

    @Override
    public void start() throws XtentisException {
        delegate.start();
    }

    @Override
    public void stop() throws XtentisException {
        delegate.stop();
    }

    @Override
    public void suspend(boolean suspend) throws XtentisException {
        delegate.suspend(suspend);
    }

    @Override
    public int getStatus() throws XtentisException {
        return delegate.getStatus();
    }
}
