/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

public class VetoException extends Exception {

    private static final long serialVersionUID = 6057799391813209245L;

    public VetoException() {
        super();
    }

    public VetoException(String message) {
        super(message);
    }

    public VetoException(String message, Throwable cause) {
        super(message, cause);
    }

    public VetoException(Throwable cause) {
        super(cause);
    }
}