/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.shared;

public class XpathUtil {

    public static String convertAbsolutePath(String currentPath, String xpath) {
        StringBuffer sb = new StringBuffer();
        String[] ops = xpath.split("/"); //$NON-NLS-1$
        String[] eles = currentPath.split("/"); //$NON-NLS-1$
        int num = 0;

        if (xpath.startsWith("..")) { //$NON-NLS-1$
            for (int i = 0; i < ops.length; i++) {
                if (ops[i].equals("..")) { //$NON-NLS-1$
                    num += 1;
                }
            }

            for (int i = 0; i < eles.length - num; i++) {
                sb.append(eles[i]);
                sb.append("/"); //$NON-NLS-1$
            }
        } else if (xpath.startsWith(".")) { //$NON-NLS-1$
            sb.append(eles[0]);
            sb.append("/"); //$NON-NLS-1$
        }

        sb.append(ops[ops.length - 1]);

        return sb.toString();
    }
}
