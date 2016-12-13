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

public class FieldLabel implements Serializable {

    private static final String X_LABEL = "X_Label_"; //$NON-NLS-1$

    private static final long serialVersionUID = 1550319079536367190L;

    private final String language;

    public FieldLabel(String language) {
        super();
        this.language = normalizeLanguage(language);
    }

    private String normalizeLanguage(String language) {
        if (language == null || language.length() == 0)
            language = "en"; //$NON-NLS-1$
        language = language.toUpperCase();
        return language;
    }

    public String getLabelAnnotation() {
        return X_LABEL + this.language;
    }

    public String getLanguage() {
        return language;
    }

}
