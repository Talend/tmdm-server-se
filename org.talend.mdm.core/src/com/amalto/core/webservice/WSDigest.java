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

@XmlType(name="WSDigest")
public class WSDigest {
    
    protected WSDigestKey wsDigestKey; 

    protected String digestValue;

    protected long timeStamp;
    
    public WSDigest() {
        
    }

    public WSDigest(WSDigestKey wsDigestKey,String digestValue, long timeStamp) {
        super();
        this.wsDigestKey = wsDigestKey;
        this.digestValue = digestValue;
        this.timeStamp = timeStamp;
    }

    public WSDigestKey getWsDigestKey() {
        return this.wsDigestKey;
    }

    public void setWsDigestKey(WSDigestKey wsDigestKey) {
        this.wsDigestKey = wsDigestKey;
    }

    public String getDigestValue() {
        return this.digestValue;
    }

    public void setDigestValue(String digestValue) {
        this.digestValue = digestValue;
    }

    public long getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
