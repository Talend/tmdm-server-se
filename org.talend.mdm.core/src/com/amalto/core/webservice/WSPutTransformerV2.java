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

@XmlType(name="WSPutTransformerV2")
public class WSPutTransformerV2 {
    protected com.amalto.core.webservice.WSTransformerV2 wsTransformerV2;
    
    public WSPutTransformerV2() {
    }
    
    public WSPutTransformerV2(com.amalto.core.webservice.WSTransformerV2 wsTransformerV2) {
        this.wsTransformerV2 = wsTransformerV2;
    }
    
    public com.amalto.core.webservice.WSTransformerV2 getWsTransformerV2() {
        return wsTransformerV2;
    }
    
    public void setWsTransformerV2(com.amalto.core.webservice.WSTransformerV2 wsTransformerV2) {
        this.wsTransformerV2 = wsTransformerV2;
    }
}
