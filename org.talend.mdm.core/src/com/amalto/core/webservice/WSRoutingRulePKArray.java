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

@XmlType(name="WSRoutingRulePKArray")
public class WSRoutingRulePKArray {
    protected com.amalto.core.webservice.WSRoutingRulePK[] wsRoutingRulePKs;
    
    public WSRoutingRulePKArray() {
    }
    
    public WSRoutingRulePKArray(com.amalto.core.webservice.WSRoutingRulePK[] wsRoutingRulePKs) {
        this.wsRoutingRulePKs = wsRoutingRulePKs;
    }
    
    public com.amalto.core.webservice.WSRoutingRulePK[] getWsRoutingRulePKs() {
        return wsRoutingRulePKs;
    }
    
    public void setWsRoutingRulePKs(com.amalto.core.webservice.WSRoutingRulePK[] wsRoutingRulePKs) {
        this.wsRoutingRulePKs = wsRoutingRulePKs;
    }
}
