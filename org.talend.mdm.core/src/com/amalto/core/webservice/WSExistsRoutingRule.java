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

@XmlType(name="WSExistsRoutingRule")
public class WSExistsRoutingRule {
    protected com.amalto.core.webservice.WSRoutingRulePK wsRoutingRulePK;
    
    public WSExistsRoutingRule() {
    }
    
    public WSExistsRoutingRule(com.amalto.core.webservice.WSRoutingRulePK wsRoutingRulePK) {
        this.wsRoutingRulePK = wsRoutingRulePK;
    }
    
    public com.amalto.core.webservice.WSRoutingRulePK getWsRoutingRulePK() {
        return wsRoutingRulePK;
    }
    
    public void setWsRoutingRulePK(com.amalto.core.webservice.WSRoutingRulePK wsRoutingRulePK) {
        this.wsRoutingRulePK = wsRoutingRulePK;
    }
}
