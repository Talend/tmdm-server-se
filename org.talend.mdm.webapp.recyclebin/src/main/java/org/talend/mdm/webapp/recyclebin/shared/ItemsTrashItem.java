/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.recyclebin.shared;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.google.gwt.user.client.rpc.IsSerializable;

public class ItemsTrashItem extends BaseModelData implements IsSerializable {

    private static final long serialVersionUID = 1L;

    private String dataClusterName;

    private String uniqueId;

    private String conceptName;

    private String dataModelName;

    private String ids;

    private String itemName;

    private String insertionUserName;

    private String insertionTime;

    private String projection;

    public ItemsTrashItem() {
        super();
    }

    public ItemsTrashItem(String conceptName, String dataModelName, String ids, String itemName, String insertionTime,
            String insertionUserName, String dataClusterName, String projection, String uniqueId) {
        this.conceptName = conceptName;
        this.dataModelName = dataModelName;
        this.ids = ids;
        this.itemName = itemName;
        this.insertionTime = insertionTime;
        this.insertionUserName = insertionUserName;
        this.dataClusterName = dataClusterName;
        this.projection = projection;
        this.uniqueId = uniqueId;
        set("conceptName", conceptName); //$NON-NLS-1$
        set("dataModelName", dataModelName); //$NON-NLS-1$
        set("ids", ids); //$NON-NLS-1$
        set("itemName", itemName); //$NON-NLS-1$
        set("insertionTime", insertionTime); //$NON-NLS-1$
        set("insertionUserName", insertionUserName); //$NON-NLS-1$
        set("dataClusterName", dataClusterName); //$NON-NLS-1$
        set("projection", projection); //$NON-NLS-1$
        set("uniqueId", uniqueId); //$NON-NLS-1$
    }

    public String getDataClusterName() {
        return dataClusterName;
    }

    public void setDataClusterName(String dataClusterName) {
        this.dataClusterName = dataClusterName;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getIds() {
        return ids;
    }

    public void setIds(String ids) {
        this.ids = ids;
    }

    public String getInsertionUserName() {
        return insertionUserName;
    }

    public void setInsertionUserName(String insertionUserName) {
        this.insertionUserName = insertionUserName;
    }

    public String getInsertionTime() {
        return insertionTime;
    }

    public void setInsertionTime(String insertionTime) {
        this.insertionTime = insertionTime;
    }

    public String getProjection() {
        return projection;
    }

    public void setProjection(String projection) {
        this.projection = projection;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getDataModelName() {
        return dataModelName;
    }

    public void setDataModelName(String dataModelName) {
        this.dataModelName = dataModelName;
    }

}
