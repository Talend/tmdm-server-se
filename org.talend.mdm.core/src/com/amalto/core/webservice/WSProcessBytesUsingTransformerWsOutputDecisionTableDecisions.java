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

@XmlType(name="WSProcessBytesUsingTransformerWsOutputDecisionTableDecisions")
public class WSProcessBytesUsingTransformerWsOutputDecisionTableDecisions {
    protected java.lang.String outputVariableName;
    protected java.lang.String decision;
    
    public WSProcessBytesUsingTransformerWsOutputDecisionTableDecisions() {
    }
    
    public WSProcessBytesUsingTransformerWsOutputDecisionTableDecisions(java.lang.String outputVariableName, java.lang.String decision) {
        this.outputVariableName = outputVariableName;
        this.decision = decision;
    }
    
    public java.lang.String getOutputVariableName() {
        return outputVariableName;
    }
    
    public void setOutputVariableName(java.lang.String outputVariableName) {
        this.outputVariableName = outputVariableName;
    }
    
    public java.lang.String getDecision() {
        return decision;
    }
    
    public void setDecision(java.lang.String decision) {
        this.decision = decision;
    }
}
