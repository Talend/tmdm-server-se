// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.itemsbrowser2.client.i18n;

public class MessagesFactory {

    private static ItemsbrowserMessages MESSAGES;

    private MessagesFactory() {
    }

    public static synchronized void setMessages(ItemsbrowserMessages messages) {
        if (MESSAGES != null && messages != null) {
            throw new IllegalStateException();
        }
        MESSAGES = messages;
    }

    public static synchronized ItemsbrowserMessages getMessages() {
        if (MESSAGES == null) {
            throw new IllegalStateException();
        }
        return MESSAGES;
    }
}
