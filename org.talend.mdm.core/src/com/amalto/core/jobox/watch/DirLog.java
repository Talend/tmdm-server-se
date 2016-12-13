/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.watch;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

class DirLog {

    private final Map<String, Long> filesModifiedTime;

    public DirLog(File dir) {
        this.filesModifiedTime = new HashMap<String, Long>();
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                long modifiedTime = file.exists() ? file.lastModified() : -1;
                filesModifiedTime.put(file.getName(), modifiedTime);
            }
        }
    }

    public Map<String, Long> getFilesModifiedTime() {
        return filesModifiedTime;
    }
}
