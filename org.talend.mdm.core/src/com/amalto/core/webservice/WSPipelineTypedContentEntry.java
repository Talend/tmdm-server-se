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

@XmlType(name="WSPipelineTypedContentEntry")
public class WSPipelineTypedContentEntry {
    protected java.lang.String output;
    protected com.amalto.core.webservice.WSExtractedContent wsExtractedContent;
    
    public WSPipelineTypedContentEntry() {
    }
    
    public WSPipelineTypedContentEntry(java.lang.String output, com.amalto.core.webservice.WSExtractedContent wsExtractedContent) {
        this.output = output;
        this.wsExtractedContent = wsExtractedContent;
    }
    
    public java.lang.String getOutput() {
        return output;
    }
    
    public void setOutput(java.lang.String output) {
        this.output = output;
    }
    
    public com.amalto.core.webservice.WSExtractedContent getWsExtractedContent() {
        return wsExtractedContent;
    }
    
    public void setWsExtractedContent(com.amalto.core.webservice.WSExtractedContent wsExtractedContent) {
        this.wsExtractedContent = wsExtractedContent;
    }
}
