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

@XmlType(name="WSExecuteTransformerV2")
public class WSExecuteTransformerV2 {
    protected com.amalto.core.webservice.WSTransformerContext wsTransformerContext;
    protected com.amalto.core.webservice.WSTypedContent wsTypedContent;
    
    public WSExecuteTransformerV2() {
    }
    
    public WSExecuteTransformerV2(com.amalto.core.webservice.WSTransformerContext wsTransformerContext, com.amalto.core.webservice.WSTypedContent wsTypedContent) {
        this.wsTransformerContext = wsTransformerContext;
        this.wsTypedContent = wsTypedContent;
    }
    
    public com.amalto.core.webservice.WSTransformerContext getWsTransformerContext() {
        return wsTransformerContext;
    }
    
    public void setWsTransformerContext(com.amalto.core.webservice.WSTransformerContext wsTransformerContext) {
        this.wsTransformerContext = wsTransformerContext;
    }
    
    public com.amalto.core.webservice.WSTypedContent getWsTypedContent() {
        return wsTypedContent;
    }
    
    public void setWsTypedContent(com.amalto.core.webservice.WSTypedContent wsTypedContent) {
        this.wsTypedContent = wsTypedContent;
    }
}
