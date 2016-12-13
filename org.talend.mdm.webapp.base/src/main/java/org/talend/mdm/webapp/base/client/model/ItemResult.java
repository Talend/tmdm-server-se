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

import com.extjs.gxt.ui.client.data.BaseModel;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class ItemResult extends BaseModel implements IsSerializable, Serializable {

    private static final long serialVersionUID = -8146665156441820837L;

    public static final int SUCCESS = 0;

    public static final int FAILURE = 1;

    public static final int UNCHANGED = 2;

    private int status;

    private long insertionTime;

    private String description;

    private String returnValue;

    public ItemResult() {
    }

    public ItemResult(int status) {
        this(status, null);
    }

    public ItemResult(int status, String description) {
        setStatus(status);
        setDescription(description);
    }

    public ItemResult(int status, String description, String returnValue) {
        this(status, description);
        setReturnValue(returnValue);
    }

    public ItemResult(int status, String description, String returnValue, long insertionTime) {
        this(status, description, returnValue);
        setInsertionTime(insertionTime);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return this.status;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public long getInsertionTime() {
        return insertionTime;
    }

    public void setInsertionTime(long insertionTime) {
        this.insertionTime = insertionTime;
    }
    
    public String getMessage() {
        return get("message"); //$NON-NLS-1$
    }

    public void setMessage(String message) {
        set("message", message); //$NON-NLS-1$
    }

    public String getKey() {
        return get("key"); //$NON-NLS-1$
    }

    public void setKey(String key) {
        set("key", key); //$NON-NLS-1$
    }
}
