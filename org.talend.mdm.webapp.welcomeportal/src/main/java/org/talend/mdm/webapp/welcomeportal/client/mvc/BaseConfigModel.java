/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client.mvc;

public class BaseConfigModel implements ConfigModel {

    protected Boolean autoRefresh;

    public BaseConfigModel() {
        autoRefresh = false;
    }

    public BaseConfigModel(Boolean auto) {
        autoRefresh = auto;
    }

    @Override
    public Boolean isAutoRefresh() {
        return this.autoRefresh;
    }

    public void setAutoRefresh(Boolean autoRefresh) {
        this.autoRefresh = autoRefresh;
    }

    @Override
    public String getSetting() {
        return isAutoRefresh().toString();
    }

    @Override
    public Object getSettingValue() {
        return isAutoRefresh();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.autoRefresh == null) ? 0 : this.autoRefresh.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BaseConfigModel other = (BaseConfigModel) obj;
        if (this.autoRefresh == null) {
            if (other.autoRefresh != null) {
                return false;
            }
        } else if (!this.autoRefresh.equals(other.autoRefresh)) {
            return false;
        }
        return true;
    }
}
