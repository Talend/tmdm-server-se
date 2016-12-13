/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server;

import javax.servlet.ServletException;

import org.talend.mdm.webapp.base.client.i18n.BaseMessagesFactory;
import org.talend.mdm.webapp.base.server.i18n.BaseMessagesImpl;

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class AbstractService extends RemoteServiceServlet {

    private static final long serialVersionUID = 1L;

    private static final Object lock = new Object[0];

    private static boolean messagesInitialized = false;

    @Override
    public void init() throws ServletException {
        super.init();
        synchronized (lock) {
            if (!messagesInitialized) {
                BaseMessagesFactory.setMessages(new BaseMessagesImpl());
                messagesInitialized = true;
            }
        }
    }

    @Override
    public final String processCall(String payload) throws SerializationException {
        return doProcessCall(payload);
    }

    protected String doProcessCall(String payload) throws SerializationException {
        return super.processCall(payload);
    }
}
