/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class ForeignKeyDrawer implements Serializable, IsSerializable {

    private static final long serialVersionUID = -5495796745371373787L;

    private String xpathForeignKey;

    private String xpathInfoForeignKey;

    private String fkFilter;

    public String getXpathForeignKey() {
        return xpathForeignKey;
    }

    public void setXpathForeignKey(String xpathForeignKey) {
        this.xpathForeignKey = xpathForeignKey;
    }

    public String getXpathInfoForeignKey() {
        return xpathInfoForeignKey;
    }

    public void setXpathInfoForeignKey(String xpathInfoForeignKey) {
        this.xpathInfoForeignKey = xpathInfoForeignKey;
    }

    public String getFkFilter() {
        return fkFilter;
    }

    public void setFkFilter(String fkFilter) {
        this.fkFilter = fkFilter;
    }

}
