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

@XmlType(name="WSRoutingEngineV2Action")
public class WSRoutingEngineV2Action {
    protected com.amalto.core.webservice.WSRoutingEngineV2ActionCode wsAction;
    
    public WSRoutingEngineV2Action() {
    }
    
    public WSRoutingEngineV2Action(com.amalto.core.webservice.WSRoutingEngineV2ActionCode wsAction) {
        this.wsAction = wsAction;
    }
    
    public com.amalto.core.webservice.WSRoutingEngineV2ActionCode getWsAction() {
        return wsAction;
    }
    
    public void setWsAction(com.amalto.core.webservice.WSRoutingEngineV2ActionCode wsAction) {
        this.wsAction = wsAction;
    }
}
