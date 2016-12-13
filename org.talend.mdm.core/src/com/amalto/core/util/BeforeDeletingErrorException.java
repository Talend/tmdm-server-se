/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

public class BeforeDeletingErrorException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private String messageType;    
    
    public BeforeDeletingErrorException(String message) {
        super(message);
    }
    
    public BeforeDeletingErrorException(String messageType, String message) {
        super(message);
        this.messageType = messageType;
    }
    
    public String getMessageType() {
        return this.messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }
    
}
