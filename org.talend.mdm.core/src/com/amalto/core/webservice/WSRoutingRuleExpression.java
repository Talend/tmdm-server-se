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

@XmlType(name="WSRoutingRuleExpression")
public class WSRoutingRuleExpression {
    protected java.lang.String name;
    protected java.lang.String xpath;
    protected com.amalto.core.webservice.WSRoutingRuleOperator wsOperator;
    protected java.lang.String value;
    
    public WSRoutingRuleExpression() {
    }
    
    public WSRoutingRuleExpression(java.lang.String name, java.lang.String xpath, com.amalto.core.webservice.WSRoutingRuleOperator wsOperator, java.lang.String value) {
        this.name = name;
        this.xpath = xpath;
        this.wsOperator = wsOperator;
        this.value = value;
    }
    
    public java.lang.String getName() {
        return name;
    }
    
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    public java.lang.String getXpath() {
        return xpath;
    }
    
    public void setXpath(java.lang.String xpath) {
        this.xpath = xpath;
    }
    
    public com.amalto.core.webservice.WSRoutingRuleOperator getWsOperator() {
        return wsOperator;
    }
    
    public void setWsOperator(com.amalto.core.webservice.WSRoutingRuleOperator wsOperator) {
        this.wsOperator = wsOperator;
    }
    
    public java.lang.String getValue() {
        return value;
    }
    
    public void setValue(java.lang.String value) {
        this.value = value;
    }
}
