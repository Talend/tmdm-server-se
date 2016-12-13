/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import java.util.List;


/**
 * DOC HSHU class global comment. Detailled comment
 */
public class LabelUtil {

    private static final String RESERVED_WORD_START_FLAG = "{";//$NON-NLS-1$

    /**
     * DOC HSHU Comment method "isDynamicLabel".
     */
    public static boolean isDynamicLabel(String label) {
        if (label == null)
            return false;
        if (label.indexOf(RESERVED_WORD_START_FLAG) != -1)
            return true;
        return false;
    }

    public static String getNormalLabel(String label) {
        if (label.indexOf(RESERVED_WORD_START_FLAG) != -1) {
            return label.substring(0, label.indexOf(RESERVED_WORD_START_FLAG));
        } else {
            return label;
        }
    }
    
    public static String convertSpecialHTMLCharacter(String label) {
        label = label.replaceAll("'", "&#39;"); //$NON-NLS-1$ //$NON-NLS-2$
        label = label.replaceAll("\"", "&quot;"); //$NON-NLS-1$//$NON-NLS-2$
        return label;
    }

    public static String getFKTabLabel(String label) {
        String fkTabLabel = getNormalLabel(label);
        if (fkTabLabel.trim().endsWith(":")) //$NON-NLS-1$
            return fkTabLabel.substring(0, fkTabLabel.lastIndexOf(":")); //$NON-NLS-1$
        return fkTabLabel;
    }
    
    public static String convertList2String(List<String> list, String separator){
        StringBuilder sb = new StringBuilder();
        int i = 0;
        int count = list.size();
        for (String str : list) {
            i++;
            sb.append(str);
            if (i < count)
                sb.append(separator);
        }
        return sb.toString();
    }
    
    public static String removeBrackets(String str) {
        if (str.indexOf("[") == 0 && str.lastIndexOf("]") == str.length() - 1) { //$NON-NLS-1$ //$NON-NLS-2$
            return str.subSequence(1, str.length() - 1).toString();
        }
        return str;
    }
}
