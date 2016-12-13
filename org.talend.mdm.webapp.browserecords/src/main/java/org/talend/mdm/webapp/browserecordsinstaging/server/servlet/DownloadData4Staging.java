/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecordsinstaging.server.servlet;

import org.talend.mdm.webapp.browserecords.server.servlet.DownloadData;

public class DownloadData4Staging extends DownloadData {

    private static final long serialVersionUID = 6201136236958671070L;

    private final String STAGING_SUFFIX_NAME = "-Staging"; //$NON-NLS-1$

    @Override
    protected void generateFileName(String name) {
        fileName = name + STAGING_SUFFIX_NAME + DOWNLOADFILE_EXTEND_NAME;
    }

    @Override
    protected String getCurrentDataCluster() throws Exception {
        return org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataCluster(true);
    }
}
