/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.util;

import java.io.Serializable;
import java.util.Map;

public abstract class SystemLocaleInitializable implements Serializable {

    private static final long serialVersionUID = -2581037266617176489L;

    protected Map<String, SystemLocale> supportedLocales;

    public void setSupportedLocales(Map<String, SystemLocale> supportedLocales) {
        this.supportedLocales = supportedLocales;
    }

    public abstract void doInit() throws Exception;

}
