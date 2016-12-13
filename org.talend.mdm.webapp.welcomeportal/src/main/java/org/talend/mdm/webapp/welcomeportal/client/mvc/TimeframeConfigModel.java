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

public class TimeframeConfigModel extends BaseConfigModel implements ConfigModel {

    private String timeFrame;

    public TimeframeConfigModel() {
        super();
        this.timeFrame = "all"; //$NON-NLS-1$
    }

    public TimeframeConfigModel(Boolean auto) {
        super(auto);
        this.timeFrame = "all"; //$NON-NLS-1$
    }

    public TimeframeConfigModel(String timeFrame) {
        super();
        this.timeFrame = timeFrame;
    }

    public TimeframeConfigModel(Boolean auto, String timeFrame) {
        super(auto);
        this.timeFrame = timeFrame;
    }

    public String getTimeFrame() {
        return this.timeFrame;
    }

    public void setTimeFrame(String timeFrame) {
        this.timeFrame = timeFrame;
    }

    @Override
    public String getSetting() {
        return getTimeFrame();
    }

    @Override
    public Object getSettingValue() {
        Long configValue = 0L;
        if (timeFrame.equals("week")) { //$NON-NLS-1$
            configValue = 7 * 24 * 60 * 60L;
        } else if (timeFrame.equals("day")) { //$NON-NLS-1$
            configValue = 24 * 60 * 60L;
        }

        return configValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.timeFrame == null) ? 0 : this.timeFrame.hashCode());
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
        TimeframeConfigModel other = (TimeframeConfigModel) obj;
        if (this.timeFrame == null) {
            if (other.timeFrame != null) {
                return false;
            }
        } else if (!this.timeFrame.equals(other.timeFrame)) {
            return false;
        }
        return true;
    }

}
