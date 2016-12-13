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

import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.browserecords.server.service.UploadService;
import org.talend.mdm.webapp.browserecords.server.servlet.UploadData;
import org.talend.mdm.webapp.browserecordsinstaging.server.service.UploadService4Staging;

public class UploadData4Staging extends UploadData {

    private static final long serialVersionUID = -4815767737453028749L;

    @Override
    protected UploadService generateUploadService(String concept, String fileType, boolean isPartialUpdate, boolean headersOnFirstLine,
            Map<String, Boolean> headerVisibleMap, List<String> inheritanceNodePathList, String multipleValueSeparator,
            String seperator, String encoding, char textDelimiter, String language) throws Exception {
        return new UploadService4Staging(getEntityModel(concept), fileType, isPartialUpdate, headersOnFirstLine, headerVisibleMap,
                inheritanceNodePathList, multipleValueSeparator, seperator, encoding, textDelimiter, language);
    }

}
