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


public class FileUtil {
    
    public static String getFileType(String fileName){
        String fileType = ""; //$NON-NLS-1$
        if (fileName != null && !"".equals(fileName) && fileName.indexOf(".") != -1){ //$NON-NLS-1$ //$NON-NLS-2$
            fileType = fileName.substring(fileName.indexOf(".") + 1); //$NON-NLS-1$
        }
        return fileType;
    }
}
