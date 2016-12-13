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
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UpdateItemModel implements Serializable, IsSerializable {

    private String concept;

    private String ids;

    private Map<String, String> changedNodes;

    public UpdateItemModel() {

    }

    public UpdateItemModel(String concept, String ids, Map<String, String> changedNodes) {
        this.concept = concept;
        this.ids = ids;
        this.changedNodes = changedNodes;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public Map<String, String> getChangedNodes() {
        return changedNodes;
    }

    public void setChangedNodes(Map<String, String> changedNodes) {
        this.changedNodes = changedNodes;
    }
}
