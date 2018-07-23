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

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.webapp.browserecords.shared.Constants;

@SuppressWarnings("nls")
public class CSVWriter extends DownloadWriter {

    private StringBuffer content = new StringBuffer();

    private String[] lineDataArray;

    public CSVWriter(String concept, String viewPk, List<String> idsList, String[] headerArray, String[] xpathArray,
            String criteria, String multipleValueSeparator, String fkDisplay, boolean fkResovled, Map<String, String> colFkMap,
            Map<String, List<String>> fkMap, boolean isStaging, String language) {
        super(concept, viewPk, idsList, headerArray, xpathArray, criteria, multipleValueSeparator, fkDisplay, fkResovled,
                colFkMap, fkMap, isStaging, language);
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
        lineDataArray = new String[headerArray.length];
        content.append(System.getProperty("line.separator")); //$NON-NLS-1$
    }

    @Override
    public void writeValue(String value) {
        lineDataArray[columnIndex] = encodeValue(value);
        if (columnIndex == lineDataArray.length - 1) {
            writeLine();
        }
    }

    private void writeLine() {
        for (int i = 0; i < lineDataArray.length; i++) {
            String value = lineDataArray[i];
            content.append(value == null ? StringUtils.EMPTY : value);
            if (i < lineDataArray.length - 1) {
                content.append(","); //$NON-NLS-1$
            }
        }
    }

    @Override
    public void write(OutputStream out) throws IOException {
        out.write(content.toString().getBytes());
    }

    @Override
    public String generateFileName(String name) {
        return super.generateFileName(name) + "." + Constants.FILE_TYPE_CSV;
    }

    private String encodeValue(String value) {
        return value.replace(",", "&#44;").replace("\"", "&#34;"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }
}
