/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.shared;

import java.util.ArrayList;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class JournalGridModel extends BaseModel implements IsSerializable {

    private static final long serialVersionUID = 1L;

    private String ids;

    private String dataContainer;

    private String dataModel;

    private String entity;

    private String key;

    private String operationType;

    private String operationTime;

    private String source;

    private String userName;

    private String operationDate;
    
    private List<String> changeNodeList;

    public JournalGridModel() {
        changeNodeList = new ArrayList<String>();
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        set("ids", ids); //$NON-NLS-1$
        this.ids = ids;
    }

    public String getDataContainer() {
        return dataContainer;
    }

    public void setDataContainer(String dataContainer) {
        set("dataContainer", dataContainer); //$NON-NLS-1$
        this.dataContainer = dataContainer;
    }

    public String getDataModel() {
        return dataModel;
    }

    public void setDataModel(String dataModel) {
        set("dataModel", dataModel); //$NON-NLS-1$
        this.dataModel = dataModel;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        set("entity", entity); //$NON-NLS-1$
        this.entity = entity;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        set("key", key); //$NON-NLS-1$
        this.key = key;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        set("operationType", operationType); //$NON-NLS-1$
        this.operationType = operationType;
    }

    public String getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(String operationTime) {
        this.operationTime = operationTime;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        set("source", source); //$NON-NLS-1$
        this.source = source;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        set("userName", userName); //$NON-NLS-1$
        this.userName = userName;
    }

    public String getOperationDate() {
        return operationDate;
    }

    public void setOperationDate(String operationDate) {
        set("operationTime", operationDate); //$NON-NLS-1$
        this.operationDate = operationDate;
    }

    public List<String> getChangeNodeList() {
        return this.changeNodeList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ids).append(","); //$NON-NLS-1$
        sb.append(dataContainer).append(","); //$NON-NLS-1$
        sb.append(dataModel).append(","); //$NON-NLS-1$
        sb.append(entity).append(","); //$NON-NLS-1$
        sb.append(key).append(","); //$NON-NLS-1$
        sb.append(operationType).append(","); //$NON-NLS-1$
        sb.append(operationTime).append(","); //$NON-NLS-1$
        sb.append(source).append(","); //$NON-NLS-1$
        sb.append(userName);

        return sb.toString();
    }
}