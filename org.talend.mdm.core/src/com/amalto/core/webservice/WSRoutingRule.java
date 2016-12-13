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

@XmlType(name="WSRoutingRule")
public class WSRoutingRule {
    protected java.lang.String name;
    protected java.lang.String description;
    protected boolean synchronous;
    protected java.lang.String concept;
    protected java.lang.String serviceJNDI;
    protected java.lang.String parameters;
    protected com.amalto.core.webservice.WSRoutingRuleExpression[] wsRoutingRuleExpressions;
    protected java.lang.String condition;
    protected java.lang.Boolean deactive;
    protected int executeOrder;
    
    public WSRoutingRule() {
    }
    
    public WSRoutingRule(java.lang.String name, java.lang.String description, boolean synchronous, java.lang.String concept, java.lang.String serviceJNDI, java.lang.String parameters, com.amalto.core.webservice.WSRoutingRuleExpression[] wsRoutingRuleExpressions, java.lang.String condition, java.lang.Boolean deactive, int executeOrder) {
        this.name = name;
        this.description = description;
        this.synchronous = synchronous;
        this.concept = concept;
        this.serviceJNDI = serviceJNDI;
        this.parameters = parameters;
        this.wsRoutingRuleExpressions = wsRoutingRuleExpressions;
        this.condition = condition;
        this.deactive = deactive;
        this.executeOrder = executeOrder;
    }
    
    public java.lang.String getName() {
        return name;
    }
    
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    public java.lang.String getDescription() {
        return description;
    }
    
    public void setDescription(java.lang.String description) {
        this.description = description;
    }
    
    public boolean isSynchronous() {
        return synchronous;
    }
    
    public void setSynchronous(boolean synchronous) {
        this.synchronous = synchronous;
    }
    
    public java.lang.String getConcept() {
        return concept;
    }
    
    public void setConcept(java.lang.String concept) {
        this.concept = concept;
    }
    
    public java.lang.String getServiceJNDI() {
        return serviceJNDI;
    }
    
    public void setServiceJNDI(java.lang.String serviceJNDI) {
        this.serviceJNDI = serviceJNDI;
    }
    
    public java.lang.String getParameters() {
        return parameters;
    }
    
    public void setParameters(java.lang.String parameters) {
        this.parameters = parameters;
    }
    
    public com.amalto.core.webservice.WSRoutingRuleExpression[] getWsRoutingRuleExpressions() {
        return wsRoutingRuleExpressions;
    }
    
    public void setWsRoutingRuleExpressions(com.amalto.core.webservice.WSRoutingRuleExpression[] wsRoutingRuleExpressions) {
        this.wsRoutingRuleExpressions = wsRoutingRuleExpressions;
    }
    
    public java.lang.String getCondition() {
        return condition;
    }
    
    public void setCondition(java.lang.String condition) {
        this.condition = condition;
    }
    
    public java.lang.Boolean getDeactive() {
        return deactive;
    }
    
    public void setDeactive(java.lang.Boolean deactive) {
        this.deactive = deactive;
    }

    public int getExecuteOrder() {
        return this.executeOrder;
    }

    public void setExecuteOrder(int executeOrder) {
        this.executeOrder = executeOrder;
    }
}
