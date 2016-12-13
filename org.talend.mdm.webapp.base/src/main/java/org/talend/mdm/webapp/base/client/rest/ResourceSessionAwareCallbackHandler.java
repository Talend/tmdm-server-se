/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.rest;

import org.restlet.client.Request;
import org.restlet.client.Response;
import org.talend.mdm.webapp.base.client.i18n.BaseMessages;
import org.talend.mdm.webapp.base.client.i18n.BaseMessagesFactory;

import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;

public abstract class ResourceSessionAwareCallbackHandler implements ResourceCallbackHandler {

    protected BaseMessages messages = BaseMessagesFactory.getMessages();

    @Override
    public void process(Request request, Response response) {
        try {
            doProcess(request, response);
        } catch (Exception e) {
            alertStagingError(e);
        }
    }

    public abstract void doProcess(Request request, Response response) throws Exception;

    protected void alertStagingError(Throwable e) {
        String errorTitle = messages.error_title();
        String errorDetail;
        if (e.getMessage() == null || e.getMessage().trim().length() == 0) {
            errorDetail = messages.unknown_error();
        } else {
            errorDetail = messages.underlying_cause()
                    + "<div style='width:300px; height:80px; overflow:auto; margin-top: 5px; margin-left: 50px; border: dashed 1px #777777;'>" //$NON-NLS-1$
                    + Format.htmlEncode(e.getMessage()) + "</div>"; //$NON-NLS-1$
        }
        Dialog dialog = MessageBox.alert(errorTitle, errorDetail, null).getDialog();
        dialog.setWidth(400);
        dialog.center();
    }

}
