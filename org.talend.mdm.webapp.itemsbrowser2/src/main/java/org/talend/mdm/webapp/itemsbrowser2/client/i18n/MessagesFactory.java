// ============================================================================
//
// Copyright (C) 2006-2010 Talend Inc. - www.talend.com
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


import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;


public class MessagesFactory {

    private static ItemsbrowserMessages MESSAGES;
    
    private MessagesFactory(){}
    
    public static ItemsbrowserMessages getMessages()
    {
        if(MESSAGES == null && GWT.isClient())
           MESSAGES = GWT.create(ItemsbrowserMessages.class);
        return MESSAGES;
    }
}
