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

@XmlType(name="WSRoutingOrderV2PKArray")
public class WSRoutingOrderV2PKArray {
    protected com.amalto.core.webservice.WSRoutingOrderV2PK[] wsRoutingOrder;
    
    public WSRoutingOrderV2PKArray() {
    }
    
    public WSRoutingOrderV2PKArray(com.amalto.core.webservice.WSRoutingOrderV2PK[] wsRoutingOrder) {
        this.wsRoutingOrder = wsRoutingOrder;
    }
    
    public com.amalto.core.webservice.WSRoutingOrderV2PK[] getWsRoutingOrder() {
        return wsRoutingOrder;
    }
    
    public void setWsRoutingOrder(com.amalto.core.webservice.WSRoutingOrderV2PK[] wsRoutingOrder) {
        this.wsRoutingOrder = wsRoutingOrder;
    }
}
