/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 *  %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.webapp.base.client.util;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.MessageBox;
import org.talend.mdm.webapp.base.client.i18n.BaseMessages;
import org.talend.mdm.webapp.base.client.i18n.BaseMessagesFactory;

public class ErrorMessageUtil {

    private static final BaseMessages MESSAGES = BaseMessagesFactory.getMessages();

    public static void showDetailErrorMessage(Throwable caught) {
        String errorMsg = caught.getLocalizedMessage();
        if (errorMsg == null) {
            if (Log.isDebugEnabled()) {
                errorMsg = caught.toString(); // for debugging purpose
            } else {
                errorMsg = MESSAGES.unknown_error();
            }
        }
        MessageBox.alert(MESSAGES.error_title(), MultilanguageMessageParser.pickOutISOMessage(errorMsg), null);
    }

}
