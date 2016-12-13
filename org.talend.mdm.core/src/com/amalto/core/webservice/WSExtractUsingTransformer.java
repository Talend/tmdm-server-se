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

@XmlType(name="WSExtractUsingTransformer")
public class WSExtractUsingTransformer {
    protected com.amalto.core.webservice.WSItemPK wsItemPK;
    protected com.amalto.core.webservice.WSTransformerPK wsTransformerPK;
    
    public WSExtractUsingTransformer() {
    }
    
    public WSExtractUsingTransformer(com.amalto.core.webservice.WSItemPK wsItemPK, com.amalto.core.webservice.WSTransformerPK wsTransformerPK) {
        this.wsItemPK = wsItemPK;
        this.wsTransformerPK = wsTransformerPK;
    }
    
    public com.amalto.core.webservice.WSItemPK getWsItemPK() {
        return wsItemPK;
    }
    
    public void setWsItemPK(com.amalto.core.webservice.WSItemPK wsItemPK) {
        this.wsItemPK = wsItemPK;
    }
    
    public com.amalto.core.webservice.WSTransformerPK getWsTransformerPK() {
        return wsTransformerPK;
    }
    
    public void setWsTransformerPK(com.amalto.core.webservice.WSTransformerPK wsTransformerPK) {
        this.wsTransformerPK = wsTransformerPK;
    }
}
