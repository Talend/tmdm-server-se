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

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ItemBasePageLoadResult<Data> implements Serializable, IsSerializable {

    private static final long serialVersionUID = 1L;

    private List<Data> data;

    private int offset;

    private int totalLength;

    private boolean isPagingAccurate;

    public ItemBasePageLoadResult() {

    }

    public ItemBasePageLoadResult(List<Data> data, int offset, int totalLength) {
        this.data = data;
        this.offset = offset;
        this.totalLength = totalLength;

    }

    public ItemBasePageLoadResult(List<Data> data, int offset, int totalLength, boolean isPagingAccurate) {
        this(data, offset, totalLength);
        this.isPagingAccurate = isPagingAccurate;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    public boolean isPagingAccurate() {
        return isPagingAccurate;
    }

    public void setPagingAccurate(boolean isPagingAccurate) {
        this.isPagingAccurate = isPagingAccurate;
    }
}
