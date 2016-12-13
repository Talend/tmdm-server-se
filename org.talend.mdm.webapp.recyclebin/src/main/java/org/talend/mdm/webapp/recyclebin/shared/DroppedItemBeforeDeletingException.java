/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.recyclebin.shared;

import org.talend.mdm.webapp.base.client.exception.ServiceException;

import com.google.gwt.user.client.rpc.IsSerializable;


public class DroppedItemBeforeDeletingException extends ServiceException implements IsSerializable {

    private static final long serialVersionUID = -4036164497494523333L;

    private String messageType;    
    
    public DroppedItemBeforeDeletingException() {
    }
    
    public DroppedItemBeforeDeletingException(String message) {
        super(message);
    }
    public DroppedItemBeforeDeletingException(String messageType, String message) {
        super(message);
        this.messageType = messageType;
    }

    public String getMessageType() {
        return this.messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }}
