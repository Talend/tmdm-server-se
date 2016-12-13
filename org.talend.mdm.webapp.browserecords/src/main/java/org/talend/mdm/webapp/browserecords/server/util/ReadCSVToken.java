/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.util.ArrayList;
import java.util.List;


public class ReadCSVToken {

    public static List<String> readToken(String line, char separator, char textDelimiter) {
        boolean inString = false;
        List<String> tokens = new ArrayList<String>();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == textDelimiter) {
                inString = !inString;
            }
            if (c == separator && !inString) {
                tokens.add(sb.toString().replaceAll("^" + textDelimiter + "|" + textDelimiter + "$", "").replace(String.valueOf(textDelimiter) + textDelimiter, String.valueOf(textDelimiter))); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                sb.delete(0, sb.length());
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().replaceAll("^" + textDelimiter + "|" + textDelimiter + "$", "").replace(String.valueOf(textDelimiter) + textDelimiter, String.valueOf(textDelimiter))); //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return tokens;
    }

}
