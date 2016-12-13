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

@XmlType(name="WSExecuteTransformerV2AsJob")
public class WSExecuteTransformerV2AsJob {
    protected com.amalto.core.webservice.WSTransformerContext wsTransformerContext;
    
    public WSExecuteTransformerV2AsJob() {
    }
    
    public WSExecuteTransformerV2AsJob(com.amalto.core.webservice.WSTransformerContext wsTransformerContext) {
        this.wsTransformerContext = wsTransformerContext;
    }
    
    public com.amalto.core.webservice.WSTransformerContext getWsTransformerContext() {
        return wsTransformerContext;
    }
    
    public void setWsTransformerContext(com.amalto.core.webservice.WSTransformerContext wsTransformerContext) {
        this.wsTransformerContext = wsTransformerContext;
    }
}
