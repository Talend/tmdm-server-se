/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeLayoutModel;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class ViewBean implements Serializable, IsSerializable {

    private List<String> viewableXpaths;

    private String viewPK;

    private String description;

    private String descriptionLocalized;

    private String[] viewables;

    private Map<String, String> searchables;
    
    private EntityModel bindingEntityModel;
    
    private ColumnTreeLayoutModel columnLayoutModel;

    public ColumnTreeLayoutModel getColumnLayoutModel() {
        return columnLayoutModel;
    }

    public void setColumnLayoutModel(ColumnTreeLayoutModel columnLayoutModel) {
        this.columnLayoutModel = columnLayoutModel;
    }

    public List<String> getViewableXpaths() {
        return viewableXpaths;
    }

    public void addViewableXpath(String xpath) {
        if (this.viewableXpaths == null)
            viewableXpaths = new ArrayList<String>();
        viewableXpaths.add(xpath);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getSearchables() {
        return searchables;
    }

    public void setSearchables(Map<String, String> searchables) {
        this.searchables = searchables;
    }

    public String[] getViewables() {
        return viewables;
    }

    public void setViewables(String[] viewables) {
        this.viewables = viewables;
    }

    public String getViewPK() {
        return viewPK;
    }

    public void setViewPK(String viewPK) {
        this.viewPK = viewPK;
    }

    public String getDescriptionLocalized() {
        return descriptionLocalized;
    }

    public void setDescriptionLocalized(String descriptionLocalized) {
        this.descriptionLocalized = descriptionLocalized;
    }

    
    public EntityModel getBindingEntityModel() {
        return bindingEntityModel;
    }

    
    public void setBindingEntityModel(EntityModel bindingEntityModel) {
        this.bindingEntityModel = bindingEntityModel;
    }
    

}
