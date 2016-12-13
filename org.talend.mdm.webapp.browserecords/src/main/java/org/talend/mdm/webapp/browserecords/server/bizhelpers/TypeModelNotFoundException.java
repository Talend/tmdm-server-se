/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.webapp.browserecords.server.bizhelpers;

public class TypeModelNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    private String xpathNotFound;

    public TypeModelNotFoundException(String xpathNotFound) {
        this.xpathNotFound = xpathNotFound;
    }

    public String getXpathNotFound() {
        return xpathNotFound;
    }
}
