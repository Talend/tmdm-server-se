/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save;


public class MultiRecordsSaveException extends RuntimeException {
    
    private String keyInfo;
    private final int rowCount;
    
    public MultiRecordsSaveException(String message, Throwable cause, int count) {
        super(message, cause);
        rowCount = count;
    }
    
    public MultiRecordsSaveException(String message, Throwable cause, String keys, int count) {
        super(message, cause);
        rowCount = count;
        keyInfo = keys;
    }

    public int getRowCount() {
        return rowCount;
    }
    
    public String getKeyInfo() {
        return keyInfo;
    }
}
