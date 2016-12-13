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

public class EntityConfigModel extends BaseConfigModel implements ConfigModel {

    private String topEntities;// 5,10,all

    public EntityConfigModel() {
        super();
        this.topEntities = "all"; //$NON-NLS-1$
    }

    public EntityConfigModel(Boolean auto) {
        super(auto);
        this.topEntities = "all"; //$NON-NLS-1$
    }

    public EntityConfigModel(String topEntities) {
        super();
        this.topEntities = topEntities;
    }

    public EntityConfigModel(Boolean auto, String topEntities) {
        super(auto);
        this.topEntities = topEntities;
    }

    public String getTopEntities() {
        return this.topEntities;
    }

    public void setTopEntities(String topEntities) {
        this.topEntities = topEntities;
    }

    @Override
    public String getSetting() {
        return getTopEntities();
    }

    @Override
    public Object getSettingValue() {
        Integer configValue = 0;
        if (topEntities.equals("5")) { //$NON-NLS-1$
            configValue = 5;
        } else if (topEntities.equals("10")) { //$NON-NLS-1$
            configValue = 10;
        }

        return configValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.topEntities == null) ? 0 : this.topEntities.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        EntityConfigModel other = (EntityConfigModel) obj;
        if (this.topEntities == null) {
            if (other.topEntities != null) {
                return false;
            }
        } else if (!this.topEntities.equals(other.topEntities)) {
            return false;
        }
        return true;
    }

}
