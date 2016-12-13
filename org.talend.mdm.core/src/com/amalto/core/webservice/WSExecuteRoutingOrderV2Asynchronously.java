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

@XmlType(name="WSExecuteRoutingOrderV2Asynchronously")
public class WSExecuteRoutingOrderV2Asynchronously {
    protected com.amalto.core.webservice.WSRoutingOrderV2PK routingOrderV2PK;
    
    public WSExecuteRoutingOrderV2Asynchronously() {
    }
    
    public WSExecuteRoutingOrderV2Asynchronously(com.amalto.core.webservice.WSRoutingOrderV2PK routingOrderV2PK) {
        this.routingOrderV2PK = routingOrderV2PK;
    }
    
    public com.amalto.core.webservice.WSRoutingOrderV2PK getRoutingOrderV2PK() {
        return routingOrderV2PK;
    }
    
    public void setRoutingOrderV2PK(com.amalto.core.webservice.WSRoutingOrderV2PK routingOrderV2PK) {
        this.routingOrderV2PK = routingOrderV2PK;
    }
}
