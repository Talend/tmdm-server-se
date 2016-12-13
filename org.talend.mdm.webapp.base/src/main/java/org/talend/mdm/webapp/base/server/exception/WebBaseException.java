/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server.exception;


/**
 * created by talend2 on 2013-8-1
 * Detailled comment
 *
 */
public class WebBaseException extends Exception {
    
    private static final long serialVersionUID = -7606775304360127841L;

    private String message;
    
    private Object[] args;
    
    public WebBaseException(String message,Object... args){
        this.message = message;
        this.args = args;
    }

    
    /**
     * Getter for message.
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }

    
    /**
     * Getter for args.
     * @return the args
     */
    public Object[] getArgs() {
        return this.args;
    }
    
    
}
