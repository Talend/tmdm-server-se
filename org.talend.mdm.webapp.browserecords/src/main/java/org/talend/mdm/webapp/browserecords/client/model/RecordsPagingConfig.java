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

public class RecordsPagingConfig implements IsSerializable, Serializable {

    private static final long serialVersionUID = -5336430937639916542L;

    private int limit;

    private int offset;

    private String sortDir;

    private String sortField;

    public RecordsPagingConfig() {

    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getSortDir() {
        return sortDir;
    }

    public String getSortField() {
        return sortField;
    }
    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

}
