/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecordsinstaging.client.widget;

import org.talend.mdm.webapp.browserecords.client.widget.UploadFileFormPanel;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.core.client.GWT;

public class UploadFileFormPanel4Staging extends UploadFileFormPanel {

    /**
     * DOC talend2 UploadFileFormPanel4Staging constructor comment.
     * 
     * @param viewBean
     * @param window
     */
    public UploadFileFormPanel4Staging(ViewBean viewBean, Window window) {
        super(viewBean, window);
    }

    @Override
    protected String getActionUrl() {
        return GWT.getHostPageBaseURL() + "/browserecords/upload4Staging"; //$NON-NLS-1$
    }
}
