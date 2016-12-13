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

@XmlType(name="WSPipeline")
public class WSPipeline {
    protected com.amalto.core.webservice.WSPipelineTypedContentEntry[] typedContentEntry;
    
    public WSPipeline() {
    }
    
    public WSPipeline(com.amalto.core.webservice.WSPipelineTypedContentEntry[] typedContentEntry) {
        this.typedContentEntry = typedContentEntry;
    }
    
    public com.amalto.core.webservice.WSPipelineTypedContentEntry[] getTypedContentEntry() {
        return typedContentEntry;
    }
    
    public void setTypedContentEntry(com.amalto.core.webservice.WSPipelineTypedContentEntry[] typedContentEntry) {
        this.typedContentEntry = typedContentEntry;
    }
}
