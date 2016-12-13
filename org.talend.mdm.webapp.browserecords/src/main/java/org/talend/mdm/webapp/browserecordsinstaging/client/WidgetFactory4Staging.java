/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecordsinstaging.client;

import org.talend.mdm.webapp.browserecords.client.WidgetFactory;
import org.talend.mdm.webapp.browserecords.client.model.QueryModel;
import org.talend.mdm.webapp.browserecords.client.widget.DownloadFilePanel;
import org.talend.mdm.webapp.browserecords.client.widget.UploadFileFormPanel;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecordsinstaging.client.widget.DownloadFilePanel4Staging;
import org.talend.mdm.webapp.browserecordsinstaging.client.widget.UploadFileFormPanel4Staging;

import com.extjs.gxt.ui.client.widget.Window;

public class WidgetFactory4Staging extends WidgetFactory {

    @Override
    public UploadFileFormPanel createUploadFileFormPanel(ViewBean viewBean, Window window) {
        return new UploadFileFormPanel4Staging(viewBean, window);
    }

    @Override
    public DownloadFilePanel createDownloadFilePanel(ViewBean viewBean, QueryModel queryModel, Window window) {
        return new DownloadFilePanel4Staging(viewBean, queryModel, window);
    }

}
