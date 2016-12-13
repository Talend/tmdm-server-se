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

import org.talend.mdm.webapp.browserecords.client.widget.ItemPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel.ForeignKeyHandler;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ForeignKeyTabModel implements Serializable, IsSerializable {

    private static final long serialVersionUID = -5519030179416660595L;
    
    private ItemNodeModel fkParentModel;

    private String fkTabTitle;

    private ItemPanel fkTabPanel;

    private ForeignKeyHandler handler;

    public ForeignKeyTabModel() {

    }

    public ForeignKeyTabModel(ItemNodeModel fkParentModel, String fkTabTitle, ItemPanel fkTabPanel, ForeignKeyHandler handler) {
        this.fkParentModel = fkParentModel;
        this.fkTabTitle = fkTabTitle;
        this.fkTabPanel = fkTabPanel;
        this.handler = handler;
    }

    public ItemNodeModel getFkParentModel() {
        return fkParentModel;
    }

    public void setFkParentModel(ItemNodeModel fkParentModel) {
        this.fkParentModel = fkParentModel;
    }

    public String getFkTabTitle() {
        return fkTabTitle;
    }

    public void setFkTabTitle(String fkTabTitle) {
        this.fkTabTitle = fkTabTitle;
    }

    public ItemPanel getFkTabPanel() {
        return fkTabPanel;
    }

    public void setFkTabPanel(ItemPanel fkTabPanel) {
        this.fkTabPanel = fkTabPanel;
    }

    public ForeignKeyHandler getHandler() {
        return handler;
    }

    public void setHandler(ForeignKeyHandler handler) {
        this.handler = handler;
    }

}
