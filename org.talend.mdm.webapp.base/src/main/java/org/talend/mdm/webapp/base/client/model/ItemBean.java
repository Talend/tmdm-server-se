/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ItemBean extends ItemBaseModel {

    private static final long serialVersionUID = 1L;

    private String concept;

    private String ids;

    private String itemXml;

    private Map<String, ForeignKeyBean> foreignkeyDesc = new HashMap<String, ForeignKeyBean>();

    private Map<String, Object> originalMap;
    
    private Map<String, String> formateMap;
    
    @Deprecated
    public ItemBean() {
    }

    public ItemBean(String concept, String ids, String itemXml) {
        super();
        this.concept = concept;
        this.ids = ids;
        this.itemXml = itemXml;
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

    public String getItemXml() {
        return itemXml;
    }

    public void setItemXml(String itemXml) {
        this.itemXml = itemXml;
    }

    public String getPk() {
        String pk = concept;

        if (ids != null) {
            pk += " " + ids;//$NON-NLS-1$ 
        }

        return pk;
    }

    public void setForeignkeyDesc(String fkValue, ForeignKeyBean desc) {
        foreignkeyDesc.put(fkValue, desc);
    }

    public ForeignKeyBean getForeignkeyDesc(String fkValue) {
        return foreignkeyDesc.get(fkValue);
    }

    @Override
    public String toString() {
        return "ItemBean [concept=" + concept + ", ids=" + ids + ", itemXml=" + itemXml + "]";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$  
    }

    public void copy(ItemBean itemBean) {
        // FIXME is this ugly?
        setConcept(itemBean.getConcept());
        setIds(itemBean.getIds());
        this.foreignkeyDesc = itemBean.foreignkeyDesc;
        setItemXml(itemBean.getItemXml());
        Collection<String> names = itemBean.getPropertyNames();
        setOriginalMap(itemBean.getOriginalMap());
        setFormateMap(itemBean.getFormateMap());
        for (String name : names) {
            this.set(name, itemBean.get(name));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof ItemBean))
            return false;

        ItemBean item = (ItemBean) obj;
        if (item.ids != null) {
            return item.ids.equals(this.ids);
        }
        return item.ids == this.ids;
    }
   
    public Map<String, Object> getOriginalMap() {
        return originalMap;
    }
    
    public void setOriginalMap(Map<String, Object> originalMap) {
        this.originalMap = originalMap;
    }

    
    public Map<String, String> getFormateMap() {
        return formateMap;
    }

    
    public void setFormateMap(Map<String, String> formateMap) {
        this.formateMap = formateMap;
    }
}
