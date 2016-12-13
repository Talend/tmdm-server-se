/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class StagingAreaConfiguration implements Serializable, IsSerializable {

    private static final int DEFAULT_REFRESH_INTERVALS = 1000;

    private int              refreshIntervals          = DEFAULT_REFRESH_INTERVALS;

    public StagingAreaConfiguration() {
    }

    public int getRefreshIntervals() {
        return refreshIntervals;
    }

    public void setRefreshIntervals(int refreshIntervals) {
        this.refreshIntervals = refreshIntervals;
    }
}
