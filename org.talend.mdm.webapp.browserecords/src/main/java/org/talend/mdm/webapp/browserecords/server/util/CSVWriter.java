/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.browserecords.shared.Constants;

public class CSVWriter extends DownloadWriter {

    private StringBuffer content = new StringBuffer();

    public CSVWriter(String concept, String viewPk, List<String> idsList, String[] headerArray, String[] xpathArray,
            String criteria, String multipleValueSeparator, String fkDisplay, boolean fkResovled, Map<String, String> colFkMap,
            Map<String, List<String>> fkMap, String language) {
        super(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator, fkDisplay, fkResovled,
                colFkMap, fkMap, language);
    }

    @Override
    public void generateFile() {

    }

    @Override
    public void writeHeader() {
        for (int i = 0; i < headerArray.length; i++) {
            content.append(headerArray[i]);
            if (i < headerArray.length - 1) {
                content.append(","); //$NON-NLS-1$
            }
        }
    }

    @Override
    void generateLine() throws Exception {
        content.append(System.getProperty("line.separator")); //$NON-NLS-1$
    }

    @Override
    public void writeValue(String value) {
        content.append(value);
        if (columnIndex < headerArray.length - 1) {
            content.append(","); //$NON-NLS-1$
        }
    }

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(content.toString().getBytes());
    }

    @Override
    public String generateFileName(String name) {
        return name + "." + Constants.FILE_TYPE_CSV; //$NON-NLS-1$
    }
}
