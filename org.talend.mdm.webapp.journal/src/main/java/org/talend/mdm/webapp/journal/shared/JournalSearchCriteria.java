/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ===========================================================================
package org.talend.mdm.webapp.journal.shared;

import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DOC Administrator  class global comment. Detailled comment
 */
public class JournalSearchCriteria implements IsSerializable {
    
    private String dataModel;

    private String entity;

    private String key;

    private String source;

    private String operationType;

    private Date startDate;

    private Date endDate;
    
    private boolean isStrict;

    public JournalSearchCriteria() {

    }

    public String getDataModel() {
        return dataModel;
    }

    public void setDataModel(String dataModel) {
        this.dataModel = dataModel;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean isStrict() {
        return this.isStrict;
    }

    public void setStrict(boolean isStrict) {
        this.isStrict = isStrict;
    }
    
    public String toString() {
        StringBuilder criteraStr= new StringBuilder();
        
        if (entity != null) criteraStr.append(entity);
        if (key != null) criteraStr.append(key);
        if (source != null) criteraStr.append(source);
        if (operationType != null) criteraStr.append(operationType);
        if (startDate != null) criteraStr.append(startDate);
        if (endDate != null) criteraStr.append(endDate);
        criteraStr.append(isStrict);   
        
        return criteraStr.toString();
    }
}