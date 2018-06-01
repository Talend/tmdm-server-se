/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecordsinstaging.server.util;

import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.browserecords.server.util.ExcelWriter;
import org.talend.mdm.webapp.browserecords.shared.Constants;


public class ExcelWriterForStaging extends ExcelWriter {

    public ExcelWriterForStaging(String concept, String viewPk, List<String> idsList, String[] headerArray, String[] xpathArray,
            String criteria, String multipleValueSeparator, String fkDisplay, boolean fkResovled, Map<String, String> colFkMap,
            Map<String, List<String>> fkMap, String language) {
        super(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator, fkDisplay, fkResovled,
                colFkMap, fkMap, language);
    }

    @Override
    protected String getCurrentDataCluster() throws Exception {
        return org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataCluster(true);
    }

    @Override
    public String generateFileName(String name) {
        return name + Constants.STAGING_SUFFIX_NAME + "." + Constants.FILE_TYPE_EXCEL; //$NON-NLS-1$
    }

}
