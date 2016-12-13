/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.shared;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.rpc.IsSerializable;

public class AppHeader implements IsSerializable {

    private String datamodel = null;

    private String datacluster = null;

    private String masterDataCluster = null;

    private String stagingDataCluster = null;

    private int autoTextAreaLength;

    private boolean isAutoValidate = true;

    private boolean dataMigrationMultiLingualFieldAuto;

    private boolean isUseRelations = true;

    private boolean isEnterprise = false;
    
    private HashMap<String,String> userProperties;

    private int importRecordsDefaultCount;

    private int exportRecordsDefaultCount;

    public AppHeader() {

    }

    public String getDatamodel() {
        return datamodel;
    }

    public void setDatamodel(String datamodel) {
        this.datamodel = datamodel;
    }

    public String getDatacluster() {
        return datacluster;
    }

    public void setDatacluster(String datacluster) {
        this.datacluster = datacluster;
    }

    public String getMasterDataCluster() {
        return this.masterDataCluster;
    }

    public void setMasterDataCluster(String masterDataCluster) {
        this.masterDataCluster = masterDataCluster;
    }

    public String getStagingDataCluster() {
        return this.stagingDataCluster;
    }

    public void setStagingDataCluster(String stagingDataCluster) {
        this.stagingDataCluster = stagingDataCluster;
    }

    public int getAutoTextAreaLength() {
        return autoTextAreaLength;
    }

    public void setAutoTextAreaLength(int autoTextAreaLength) {
        this.autoTextAreaLength = autoTextAreaLength;
    }

    public boolean isDataMigrationMultiLingualFieldAuto() {
        return dataMigrationMultiLingualFieldAuto;
    }

    public void setDataMigrationMultiLingualFieldAuto(boolean dataMigrationMultiLingualFieldAuto) {
        this.dataMigrationMultiLingualFieldAuto = dataMigrationMultiLingualFieldAuto;
    }

    @Override
    public String toString() {
        return "AppHeader [datamodel=" + datamodel + ", datacluster=" + datacluster + "]";//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
    }

    public boolean isAutoValidate() {
        return isAutoValidate;
    }

    public void setAutoValidate(boolean isAutoValidate) {
        this.isAutoValidate = isAutoValidate;
    }

    public boolean isUseRelations() {
        return isUseRelations;
    }

    public void setUseRelations(boolean isUseRelations) {
        this.isUseRelations = isUseRelations;
    }

    public boolean isEnterprise() {
        return this.isEnterprise;
    }

    public void setEnterprise(boolean isEnterprise) {
        this.isEnterprise = isEnterprise;
    }

    public HashMap<String, String> getUserProperties() {
        return this.userProperties;
    }

    public void setUserProperties(Map<String, String> map) {
        this.userProperties = (HashMap<String, String>) map;
    }

    public int getImportRecordsDefaultCount() {
        return importRecordsDefaultCount;
    }

    public void setImportRecordsDefaultCount(int importRecordsDefaultCount) {
        this.importRecordsDefaultCount = importRecordsDefaultCount;
    }

    public int getExportRecordsDefaultCount() {
        return exportRecordsDefaultCount;
    }

    public void setExportRecordsDefaultCount(int exportRecordsDefaultCount) {
        this.exportRecordsDefaultCount = exportRecordsDefaultCount;
    }
}
