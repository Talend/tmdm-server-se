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

@XmlType(name="WSMDMConfig")
public class WSMDMConfig {
    protected java.lang.String serverName;
    protected java.lang.String serverPort;
    protected java.lang.String userName;
    protected java.lang.String password;
    protected java.lang.String xdbDriver;
    protected java.lang.String xdbID;
    protected java.lang.String xdbUrl;
    protected java.lang.String isupurl;
    
    public WSMDMConfig() {
    }
    
    public WSMDMConfig(java.lang.String serverName, java.lang.String serverPort, java.lang.String userName, java.lang.String password, java.lang.String xdbDriver, java.lang.String xdbID, java.lang.String xdbUrl, java.lang.String isupurl) {
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.userName = userName;
        this.password = password;
        this.xdbDriver = xdbDriver;
        this.xdbID = xdbID;
        this.xdbUrl = xdbUrl;
        this.isupurl = isupurl;
    }
    
    public java.lang.String getServerName() {
        return serverName;
    }
    
    public void setServerName(java.lang.String serverName) {
        this.serverName = serverName;
    }
    
    public java.lang.String getServerPort() {
        return serverPort;
    }
    
    public void setServerPort(java.lang.String serverPort) {
        this.serverPort = serverPort;
    }
    
    public java.lang.String getUserName() {
        return userName;
    }
    
    public void setUserName(java.lang.String userName) {
        this.userName = userName;
    }
    
    public java.lang.String getPassword() {
        return password;
    }
    
    public void setPassword(java.lang.String password) {
        this.password = password;
    }
    
    public java.lang.String getXdbDriver() {
        return xdbDriver;
    }
    
    public void setXdbDriver(java.lang.String xdbDriver) {
        this.xdbDriver = xdbDriver;
    }
    
    public java.lang.String getXdbID() {
        return xdbID;
    }
    
    public void setXdbID(java.lang.String xdbID) {
        this.xdbID = xdbID;
    }
    
    public java.lang.String getXdbUrl() {
        return xdbUrl;
    }
    
    public void setXdbUrl(java.lang.String xdbUrl) {
        this.xdbUrl = xdbUrl;
    }
    
    public java.lang.String getIsupurl() {
        return isupurl;
    }
    
    public void setIsupurl(java.lang.String isupurl) {
        this.isupurl = isupurl;
    }
}
