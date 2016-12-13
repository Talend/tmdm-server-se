/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.BrowseStagingRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;

import com.extjs.gxt.ui.client.Registry;

/**
 * created by talend2 on 2014-1-29 Detailled comment
 * 
 */
public class StagingItemTreeDetail extends TreeDetail {

    public StagingItemTreeDetail(ItemsDetailPanel itemsDetailPanel) {
        super(itemsDetailPanel);

    }

    @Override
    protected BrowseRecordsServiceAsync getItemService() {
        BrowseStagingRecordsServiceAsync service = (BrowseStagingRecordsServiceAsync) Registry
                .get(BrowseRecords.BROWSESTAGINGRECORDS_SERVICE);
        return service;
    }

}
