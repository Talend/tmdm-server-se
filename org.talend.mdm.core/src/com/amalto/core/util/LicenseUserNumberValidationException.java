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

public class LicenseUserNumberValidationException extends Exception {

    private static final long serialVersionUID = 1669126870792259622L;

    public LicenseUserNumberValidationException() {
        super();
    }

    public LicenseUserNumberValidationException(String message) {
        super(message);
    }

    public LicenseUserNumberValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public LicenseUserNumberValidationException(Throwable cause) {
        super(cause);
    }
}
