/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects;

import org.apache.commons.lang.StringEscapeUtils;

public class UpdateReportItemPOJO {

    private String path;
    private String oldValue;
    private String newValue;

    public UpdateReportItemPOJO(String path, String oldValue, String newValue) {
        super();
        this.newValue = newValue;
        this.oldValue = oldValue;
        this.path = path;
    }

    public UpdateReportItemPOJO() {
        super();
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String serialize() {
        return "<Item>\n" + //$NON-NLS-1$
                "   <path>" + StringEscapeUtils.escapeXml(this.getPath()) + "</path>\n" + //$NON-NLS-1$ //$NON-NLS-2$
                "   <oldValue>" + StringEscapeUtils.escapeXml(this.getOldValue()) + "</oldValue>\n" + //$NON-NLS-1$ //$NON-NLS-2$
                "   <newValue>" + StringEscapeUtils.escapeXml(this.getNewValue()) + "</newValue>\n" + //$NON-NLS-1$ //$NON-NLS-2$
                "</Item>\n"; //$NON-NLS-1$
    }

}
