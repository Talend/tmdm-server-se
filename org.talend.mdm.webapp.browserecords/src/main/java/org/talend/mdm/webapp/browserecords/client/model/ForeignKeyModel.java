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

import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

public class ForeignKeyModel extends ItemBaseModel {

    private static final long serialVersionUID = 1L;

    private ViewBean viewBean;

    private ItemBean itemBean;

    private ItemNodeModel nodeModel;

    public ForeignKeyModel() {
    }

    public ForeignKeyModel(ViewBean _v, ItemBean _i, ItemNodeModel _m) {
        this.viewBean = _v;
        this.itemBean = _i;
        this.nodeModel = _m;
    }

    public ViewBean getViewBean() {
        return viewBean;
    }

    public void setViewBean(ViewBean viewBean) {
        this.viewBean = viewBean;
    }

    public ItemBean getItemBean() {
        return itemBean;
    }

    public void setItemBean(ItemBean itemBean) {
        this.itemBean = itemBean;
    }

    public ItemNodeModel getNodeModel() {
        return nodeModel;
    }

    public void setNodeModel(ItemNodeModel nodeModel) {
        this.nodeModel = nodeModel;
    };

}
