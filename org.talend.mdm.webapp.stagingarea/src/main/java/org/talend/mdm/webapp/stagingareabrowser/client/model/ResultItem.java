/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingareabrowser.client.model;

import java.io.Serializable;
import java.util.Date;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.google.gwt.user.client.rpc.IsSerializable;

@SuppressWarnings("serial")
public class ResultItem extends BaseModel implements IsSerializable, Serializable {

    public ResultItem() {

    }

    public String getIds() {
        return get("ids"); //$NON-NLS-1$
    }

    public void setIds(String ids) {
        set("ids", ids); //$NON-NLS-1$
    }

    public String getEntity() {
        return get("entity"); //$NON-NLS-1$
    }

    public void setEntity(String entity) {
        set("entity", entity); //$NON-NLS-1$
    }

    public String getKey() {
        return get("key"); //$NON-NLS-1$
    }

    public void setKey(String key) {
        set("key", key); //$NON-NLS-1$
    }

    public Date getDateTime() {
        return get("dateTime"); //$NON-NLS-1$
    }

    public void setDateTime(Date dateTime) {
        set("dateTime", dateTime); //$NON-NLS-1$
    }

    
    public String getSource() {
        return get("source"); //$NON-NLS-1$
    }

    
    public void setSource(String source) {
        set("source", source); //$NON-NLS-1$
    }

    
    public String getGroup() {
        return get("group"); //$NON-NLS-1$
    }

    
    public void setGroup(String group) {
        set("group", group); //$NON-NLS-1$
    }

    
    public Integer getStatus() {
        return get("status"); //$NON-NLS-1$
    }

    
    public void setStatus(Integer status) {
        set("status", status); //$NON-NLS-1$
    }

    
    public String getError() {
        return get("error"); //$NON-NLS-1$
    }

    
    public void setError(String error) {
        set("error", error); //$NON-NLS-1$
    }

}
