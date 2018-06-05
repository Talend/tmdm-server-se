/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

import javax.servlet.ServletException;

import org.talend.mdm.webapp.browserecords.server.servlet.DownloadData;
import org.talend.mdm.webapp.browserecords.server.util.CSVWriter;
import org.talend.mdm.webapp.browserecords.server.util.DownloadWriter;
import org.talend.mdm.webapp.browserecords.server.util.ExcelWriter;
import org.talend.mdm.webapp.browserecords.shared.Constants;

import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;

public class DownloadData4Staging extends DownloadData {

    private Messages messages = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages", DownloadData4Staging.class.getClassLoader()); //$NON-NLS-1$

    private static final long serialVersionUID = 6201136236958671070L;

    @Override
    protected DownloadWriter generateWriter(String concept, String viewPk, List<String> idsList, String[] headerArray,
            String[] xpathArray, String criteria, String multipleValueSeparator, String fkDisplay, boolean fkResovled,
            Map<String, String> colFkMap, Map<String, List<String>> fkMap, String language, String fileType)
            throws ServletException {
        if (Constants.FILE_TYPE_CSV.equals(fileType)) {
            return new CSVWriter(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator,
                    fkDisplay,
                    fkResovled, colFkMap, fkMap, true, language);
        } else if (Constants.FILE_TYPE_EXCEL.equals(fileType)) {
            return new ExcelWriter(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator,
                    fkDisplay,
                    fkResovled, colFkMap, fkMap, true, language);
        } else {
            throw new ServletException(messages.getMessage("unspport_file_type", fileType)); //$NON-NLS-1$
        }
    }
}
