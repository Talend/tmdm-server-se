/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.exception;

import com.google.gwt.user.client.rpc.IsSerializable;

public class LicenseUserNumberValidationException extends ServiceException implements IsSerializable {

    private static final long serialVersionUID = -782929850319585683L;

    public LicenseUserNumberValidationException() {
        super();
    }

    public LicenseUserNumberValidationException(String message) {
        super(message);
    }

}
