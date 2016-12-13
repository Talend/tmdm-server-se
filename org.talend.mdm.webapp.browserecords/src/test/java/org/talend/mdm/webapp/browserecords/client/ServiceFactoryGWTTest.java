/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client;

import org.talend.mdm.webapp.base.client.ServiceEnhancer;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class ServiceFactoryGWTTest extends GWTTestCase {

    private ServiceDefTarget browseRecordService;

    private ServiceDefTarget browseStagingRecordService;

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        browseRecordService = GWT.create(BrowseRecordsService.class);
        ServiceEnhancer.customizeService(browseRecordService);
        Registry.register(BrowseRecords.BROWSERECORDS_SERVICE, browseRecordService);

        browseStagingRecordService = GWT.create(BrowseStagingRecordsService.class);
        ServiceEnhancer.customizeService(browseStagingRecordService);
        Registry.register(BrowseRecords.BROWSESTAGINGRECORDS_SERVICE, browseStagingRecordService);

    }

    public void testServiceFatory() {
        assertEquals(browseRecordService, ServiceFactory.getInstance().getMasterService());
        assertEquals(browseStagingRecordService, ServiceFactory.getInstance().getStagingService());
    }

    @Override
    public String getModuleName() {
        // GWTTestCase Required
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }

}
