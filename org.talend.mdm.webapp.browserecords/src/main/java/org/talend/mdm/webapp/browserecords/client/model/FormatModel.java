/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class FormatModel implements IsSerializable {

    private String format;

    private Serializable object;

    private String language;

    private boolean isDate;

    private boolean isDateTime = false;

    public FormatModel() {

    }

    public FormatModel(String format, Serializable object, String language) {
        this.format = format;
        this.object = object;
        this.language = language;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public Serializable getObject() {
        return object;
    }

    public void setObject(Serializable object) {
        this.object = object;
    }

    public String getLanguage() {
        return language == null ? "en" : language; //$NON-NLS-1$
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public boolean isDate() {
        return isDate;
    }

    public void setDate(boolean isDate) {
        this.isDate = isDate;
    }

    public boolean isDateTime() {
        return isDateTime;
    }

    public void setDateTime(boolean isDateTime) {
        this.isDateTime = isDateTime;
    }
}
