/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.i18n;

import com.google.gwt.core.client.GWT;

public class BaseMessagesFactory {

    private static BaseMessages MESSAGES;

    static {
        if (GWT.isClient()) {
            setMessages((BaseMessages) GWT.create(BaseMessages.class));
        }
    }

    private BaseMessagesFactory() {
    }

    /**
     * For internal use only. 
     */
    public static synchronized void setMessages(BaseMessages messages) {
        if (MESSAGES != null && messages != null) {
            throw new IllegalStateException();
        }
        MESSAGES = messages;
    }

    public static synchronized BaseMessages getMessages() {
        if (MESSAGES == null) {
            throw new IllegalStateException();
        }
        return MESSAGES;
    }
}
