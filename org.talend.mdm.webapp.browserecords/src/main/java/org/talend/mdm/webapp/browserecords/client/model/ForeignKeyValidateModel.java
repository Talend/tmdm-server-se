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


public class ForeignKeyValidateModel extends ItemBaseModel {

    private static final long serialVersionUID = -2070259885223573368L;

    private boolean isHaveNodeValue;

    private boolean isNodeValid;

    public ForeignKeyValidateModel() {

    }

    public ForeignKeyValidateModel(boolean isHaveNodeValue, boolean isNodeValid) {
        this.isHaveNodeValue = isHaveNodeValue;
        this.isNodeValid = isNodeValid;
    }

    public boolean isHaveNodeValue() {
        return isHaveNodeValue;
    }

    public void setHaveNodeValue(boolean isHaveNodeValue) {
        this.isHaveNodeValue = isHaveNodeValue;
    }

    public boolean isNodeValid() {
        return isNodeValid;
    }

    public void setNodeValid(boolean isNodeValid) {
        this.isNodeValid = isNodeValid;
    }

}
