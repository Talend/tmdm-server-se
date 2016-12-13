/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.webservice;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="WSLoadDroppedItem")
public class WSLoadDroppedItem {
    protected com.amalto.core.webservice.WSDroppedItemPK wsDroppedItemPK;
    
    public WSLoadDroppedItem() {
    }
    
    public WSLoadDroppedItem(com.amalto.core.webservice.WSDroppedItemPK wsDroppedItemPK) {
        this.wsDroppedItemPK = wsDroppedItemPK;
    }
    
    public com.amalto.core.webservice.WSDroppedItemPK getWsDroppedItemPK() {
        return wsDroppedItemPK;
    }
    
    public void setWsDroppedItemPK(com.amalto.core.webservice.WSDroppedItemPK wsDroppedItemPK) {
        this.wsDroppedItemPK = wsDroppedItemPK;
    }
}
