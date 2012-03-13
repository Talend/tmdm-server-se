// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.journal.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DOC Administrator  class global comment. Detailled comment
 */
public class JournalParameters implements Serializable, IsSerializable {

    private static final long serialVersionUID = 1L;

    private String ids;

    private String[] id;
    
    private String dataClusterName;
    
    private String dataModelName;
    
    private String conceptName;
        
    private String revisionId;

    private long date;

    private String action;

    public JournalParameters() {

    }

    public JournalParameters(String ids, String[] id, String dataClusterName, String dataModelName, String conceptName,
            String revisionId, long date, String action) {
        this.ids = ids;
        this.id = id;
        this.dataClusterName = dataClusterName;
        this.dataModelName = dataModelName;
        this.conceptName = conceptName;
        this.revisionId = revisionId;
        this.date = date;
        this.action = action;
    }

    public String getIds() {
        return ids;
    }
    
    public void setIds(String ids) {
        this.ids = ids;
    }
    
    public String[] getId() {
        return id;
    }

    public void setId(String[] id) {
        this.id = id;
    }
    
    public String getDataClusterName() {
        return dataClusterName;
    }

    public void setDataClusterName(String dataClusterName) {
        this.dataClusterName = dataClusterName;
    }
    
    public String getDataModelName() {
        return dataModelName;
    }

    public void setDataModelName(String dataModelName) {
        this.dataModelName = dataModelName;
    }
    
    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getRevisionId() {
        return revisionId;
    }

    public void setRevisionId(String revisionId) {
        this.revisionId = revisionId;
    }
    
    public long getDate() {
        return date;
    }
    
    public void setDate(long date) {
        this.date = date;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
}