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

import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class BrowseRecordsGWTTest extends GWTTestCase {

    protected BrowseRecords browseRecords;

    @Override
    protected void gwtSetUp() throws Exception {
        browseRecords = new BrowseRecords();
    }

    public void testOnModuleLoad() {
        // test the entry method without any exception
        prepareEnv();
        browseRecords.onModuleLoad();
    }

    private native void prepareEnv()/*-{
		$wnd.amalto = {};
		$wnd.amalto.browserecords = {};
    }-*/;

    @Override
    public String getModuleName() {
        // GWTTestCase Required
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }

}
