/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;

import com.extjs.gxt.ui.client.widget.MessageBox;

public class MessageUtil {

    public static MessageBox generateMessageBox(ItemResult result) {
        MessageBox messageBox = new MessageBox();
        messageBox.setButtons(MessageBox.OK);
        switch (result.getStatus()) {
        case ItemResult.SUCCESS:
            messageBox.setTitle(MessagesFactory.getMessages().info_title());
            messageBox.setIcon(MessageBox.INFO);
            messageBox.setButtons(""); //$NON-NLS-1$
            break;
        case ItemResult.WARNING:
            messageBox.setTitle(MessagesFactory.getMessages().warning_title());
            messageBox.setIcon(MessageBox.WARNING);
            messageBox.setButtons(MessageBox.OKCANCEL);
            break;
        case ItemResult.FAILURE:
            messageBox.setTitle(MessagesFactory.getMessages().error_title());
            messageBox.setIcon(MessageBox.ERROR);
            break;
        default:
            break;
        }
        messageBox.setMessage(MultilanguageMessageParser.pickOutISOMessage(result.getMessage()));
        return messageBox;
    }

}
