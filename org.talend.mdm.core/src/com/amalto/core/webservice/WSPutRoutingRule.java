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

@XmlType(name="WSPutRoutingRule")
public class WSPutRoutingRule {
    protected com.amalto.core.webservice.WSRoutingRule wsRoutingRule;
    
    public WSPutRoutingRule() {
    }
    
    public WSPutRoutingRule(com.amalto.core.webservice.WSRoutingRule wsRoutingRule) {
        this.wsRoutingRule = wsRoutingRule;
    }
    
    public com.amalto.core.webservice.WSRoutingRule getWsRoutingRule() {
        return wsRoutingRule;
    }
    
    public void setWsRoutingRule(com.amalto.core.webservice.WSRoutingRule wsRoutingRule) {
        this.wsRoutingRule = wsRoutingRule;
    }
}
