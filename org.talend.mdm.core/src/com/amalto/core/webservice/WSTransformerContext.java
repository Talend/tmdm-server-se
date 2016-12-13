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

@XmlType(name="WSTransformerContext")
public class WSTransformerContext {
    protected com.amalto.core.webservice.WSTransformerV2PK wsTransformerPK;
    protected com.amalto.core.webservice.WSTransformerContextPipeline pipeline;
    protected com.amalto.core.webservice.WSTransformerContextProjectedItemPKs projectedItemPKs;
    
    public WSTransformerContext() {
    }
    
    public WSTransformerContext(com.amalto.core.webservice.WSTransformerV2PK wsTransformerPK, com.amalto.core.webservice.WSTransformerContextPipeline pipeline, com.amalto.core.webservice.WSTransformerContextProjectedItemPKs projectedItemPKs) {
        this.wsTransformerPK = wsTransformerPK;
        this.pipeline = pipeline;
        this.projectedItemPKs = projectedItemPKs;
    }
    
    public com.amalto.core.webservice.WSTransformerV2PK getWsTransformerPK() {
        return wsTransformerPK;
    }
    
    public void setWsTransformerPK(com.amalto.core.webservice.WSTransformerV2PK wsTransformerPK) {
        this.wsTransformerPK = wsTransformerPK;
    }
    
    public com.amalto.core.webservice.WSTransformerContextPipeline getPipeline() {
        return pipeline;
    }
    
    public void setPipeline(com.amalto.core.webservice.WSTransformerContextPipeline pipeline) {
        this.pipeline = pipeline;
    }
    
    public com.amalto.core.webservice.WSTransformerContextProjectedItemPKs getProjectedItemPKs() {
        return projectedItemPKs;
    }
    
    public void setProjectedItemPKs(com.amalto.core.webservice.WSTransformerContextProjectedItemPKs projectedItemPKs) {
        this.projectedItemPKs = projectedItemPKs;
    }
}
