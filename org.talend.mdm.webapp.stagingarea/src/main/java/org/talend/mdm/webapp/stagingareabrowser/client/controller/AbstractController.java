/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingareabrowser.client.controller;

import org.talend.mdm.webapp.stagingareabrowser.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.stagingareabrowser.client.i18n.StagingareaBrowseMessages;
import org.talend.mdm.webapp.stagingareabrowser.client.view.AbstractView;

import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;

public abstract class AbstractController {

    protected AbstractView bindingView;

    public AbstractView getBindingView() {
        return bindingView;
    }

    public void setBindingView(AbstractView bindingView) {
        this.bindingView = bindingView;
    }

    protected static void alertStagingError(Throwable e) {
        StagingareaBrowseMessages messages = MessagesFactory.getMessages();
        String errorTitle = messages.staging_area_error();
        String errorDetail;
        if (e.getMessage() == null || e.getMessage().trim().length() == 0) {
            errorDetail = messages.staging_area_exception();
        } else {
            errorDetail = messages.staging_area_exception() + "</br>" + messages.underlying_cause() //$NON-NLS-1$
                    + "<div style='width:300px; height:80px; overflow:auto; margin-top: 5px; margin-left: 50px; border: dashed 1px #777777;'>" //$NON-NLS-1$
                    + Format.htmlEncode(e.getMessage()) + "</div>"; //$NON-NLS-1$
        }
        Dialog dialog = MessageBox.alert(errorTitle, errorDetail, null).getDialog();
        dialog.setWidth(400);
        dialog.center();
    }
}
