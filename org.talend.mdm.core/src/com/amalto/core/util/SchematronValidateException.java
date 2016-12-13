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

/**
 * created by yjli on 2013-12-31 Detailled comment
 * 
 */
public class SchematronValidateException extends RuntimeException {

    private static final long serialVersionUID = 5773714701320676637L;

    public SchematronValidateException() {
        super();
    }

    /**
     * @param message
     */
    public SchematronValidateException(String message) {
        super(message);
    }

}
