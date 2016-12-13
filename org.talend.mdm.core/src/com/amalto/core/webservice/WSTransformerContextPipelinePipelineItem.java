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

@XmlType(name="WSTransformerContextPipelinePipelineItem")
public class WSTransformerContextPipelinePipelineItem {
    protected java.lang.String variable;
    protected com.amalto.core.webservice.WSTypedContent wsTypedContent;
    
    public WSTransformerContextPipelinePipelineItem() {
    }
    
    public WSTransformerContextPipelinePipelineItem(java.lang.String variable, com.amalto.core.webservice.WSTypedContent wsTypedContent) {
        this.variable = variable;
        this.wsTypedContent = wsTypedContent;
    }
    
    public java.lang.String getVariable() {
        return variable;
    }
    
    public void setVariable(java.lang.String variable) {
        this.variable = variable;
    }
    
    public com.amalto.core.webservice.WSTypedContent getWsTypedContent() {
        return wsTypedContent;
    }
    
    public void setWsTypedContent(com.amalto.core.webservice.WSTypedContent wsTypedContent) {
        this.wsTypedContent = wsTypedContent;
    }
}
