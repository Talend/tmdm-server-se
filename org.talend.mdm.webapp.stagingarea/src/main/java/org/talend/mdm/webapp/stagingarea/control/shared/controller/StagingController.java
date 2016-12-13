/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.shared.controller;

import com.google.gwt.user.client.Timer;
import org.talend.mdm.webapp.stagingarea.control.client.StagingAreaControl;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaConfiguration;

public class StagingController {

    private final Timer timer;

    private boolean     autoRefresh;

    public StagingController() {
        timer = new Timer() {

            @Override
            public void run() {
                Controllers.get().getSummaryController().refresh();
                Controllers.get().getValidationController().refresh();
            }
        };
    }

    public void autoRefresh(boolean auto) {
        if (this.autoRefresh != auto) {
            this.autoRefresh = auto;
            if (auto) {
                StagingAreaConfiguration stagingAreaConfig = StagingAreaControl.getStagingAreaConfig();
                timer.scheduleRepeating(stagingAreaConfig.getRefreshIntervals());
            } else {
                timer.cancel();
            }
        }
    }
}
