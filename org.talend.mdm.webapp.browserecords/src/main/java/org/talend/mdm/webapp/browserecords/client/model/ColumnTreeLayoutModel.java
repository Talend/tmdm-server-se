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
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ColumnTreeLayoutModel implements Serializable, IsSerializable {

    private static final long serialVersionUID = 1L;

    private List<ColumnTreeModel> columnTreeModels;

    public ColumnTreeLayoutModel() {

    }

    public List<ColumnTreeModel> getColumnTreeModels() {
        return columnTreeModels != null ? columnTreeModels : new ArrayList<ColumnTreeModel>();
    }

    public void setColumnTreeModels(List<ColumnTreeModel> columnTreeModels) {
        this.columnTreeModels = columnTreeModels;
    }

}
