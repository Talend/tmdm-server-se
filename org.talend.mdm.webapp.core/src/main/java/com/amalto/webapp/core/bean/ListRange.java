/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.bean;

import java.io.Serializable;

/*
 * generic bean for Ext.2.0 datastore
 * must be used with dwrproxy
 */

public class ListRange implements Serializable {

	private static final long serialVersionUID = 6239586023660639700L;

	private Object[] data;

    private int totalSize;

    private boolean isPagingAccurate;

    public Object[] getData() {
        return data;
    }

    public void setData(Object[] data) {
        this.data = data;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(int totalSize) {
        this.totalSize = totalSize;
    }

    public boolean isPagingAccurate() {
        return isPagingAccurate;
    }

    public void setPagingAccurate(boolean isPagingAccurate) {
        this.isPagingAccurate = isPagingAccurate;
    }
}