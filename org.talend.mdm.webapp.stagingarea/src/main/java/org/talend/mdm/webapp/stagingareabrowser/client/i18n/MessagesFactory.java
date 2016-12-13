/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingareabrowser.client.i18n;

import com.google.gwt.core.client.GWT;

public class MessagesFactory {

    private static StagingareaBrowseMessages MESSAGES;

    private MessagesFactory() {
    }

    public static StagingareaBrowseMessages getMessages() {
        if (GWT.isClient()) {
            if (MESSAGES == null)
                MESSAGES = GWT.create(StagingareaBrowseMessages.class);
            return MESSAGES;
        }
        // Can't be called from server-side
        throw new IllegalStateException();
    }
}
