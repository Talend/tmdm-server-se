/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class UserContextModel implements Serializable, IsSerializable {

    private static final long serialVersionUID = -3689563019277748075L;

    private String dataContainer;

    private String dataModel;

    private String language;

    private String dateTimeFormat;

    public UserContextModel() {

    }

    public String getDataContainer() {
        return dataContainer;
    }

    public void setDataContainer(String dataContainer) {
        this.dataContainer = dataContainer;
    }

    public String getDataModel() {
        return dataModel;
    }

    public void setDataModel(String dataModel) {
        this.dataModel = dataModel;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getDateTimeFormat() {
        return dateTimeFormat;
    }

    public void setDateTimeFormat(String dateTimeFormat) {
        this.dateTimeFormat = dateTimeFormat;
    }

}
