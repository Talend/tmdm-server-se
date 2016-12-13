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


public class ValidateException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ValidateException() {
        super();
    }

    /**
     * @param message
     */
    public ValidateException(String message) {
        super(message);
    }

    /**
     * @param message
     * @param cause
     */
    public ValidateException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause
     */
    public ValidateException(Throwable cause) {
        super(cause);
    }
}
