/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecordsinstaging.server.service;

import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.server.service.UploadService;

public class UploadService4Staging extends UploadService {

    public UploadService4Staging(EntityModel entityModel, String fileType, boolean isPartialUpdate, boolean headersOnFirstLine,
            Map<String, Boolean> headerVisibleMap, List<String> inheritanceNodePathList, String multipleValueSeparator,
            String seperator, String encoding, char textDelimiter, String language) {
        super(entityModel, fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap, inheritanceNodePathList, multipleValueSeparator,
                seperator, encoding, textDelimiter, language);
    }

    @Override
    protected String getCurrentDataCluster() throws Exception {
        return org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getCurrentDataCluster(true);
    }
}
