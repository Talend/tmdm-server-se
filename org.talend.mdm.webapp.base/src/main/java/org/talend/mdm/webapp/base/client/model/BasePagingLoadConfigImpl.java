/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.model;

import com.extjs.gxt.ui.client.data.PagingLoadConfig;

public class BasePagingLoadConfigImpl extends ItemBaseModel {

    private static final long serialVersionUID = 2670845271716853735L;

    private int limit;

    private int offset;

    private String sortDir;

    private String sortField;

    public BasePagingLoadConfigImpl() {

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

    public static BasePagingLoadConfigImpl copyPagingLoad(PagingLoadConfig pconfig) {
        BasePagingLoadConfigImpl rpConfig = new BasePagingLoadConfigImpl();
        rpConfig.setLimit(pconfig.getLimit());
        rpConfig.setOffset(pconfig.getOffset());
        rpConfig.setSortDir(pconfig.getSortDir() == null ? "NONE" : pconfig.getSortDir().toString()); //$NON-NLS-1$
        rpConfig.setSortField(pconfig.getSortField());
        return rpConfig;
    }

}
