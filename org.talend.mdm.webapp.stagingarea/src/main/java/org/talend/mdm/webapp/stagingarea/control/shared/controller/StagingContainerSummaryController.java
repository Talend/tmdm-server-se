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

import org.talend.mdm.webapp.base.client.model.UserContextModel;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.stagingarea.control.shared.controller.rest.StagingRestServiceHandler;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingContainerModel;

public class StagingContainerSummaryController {

    private final StagingRestServiceHandler serviceHandler = StagingRestServiceHandler.get();

    private final StagingContainerModel     model;

    public StagingContainerSummaryController(StagingContainerModel model) {
        this.model = model;
    }

    public void refresh() {
        UserContextModel ucx = UserContextUtil.getUserContext();
        String dataModel = ucx.getDataModel();
        String dataContainer = ucx.getDataContainer();
        serviceHandler.getStagingContainerSummary(dataContainer, dataModel, model);
    }

    public native void openInvalidRecordToBrowseRecord(int state)/*-{
                                                                 if ($wnd.amalto.stagingareabrowse
                                                                 && $wnd.amalto.stagingareabrowse.StagingareaBrowse) {
                                                                 $wnd.amalto.stagingareabrowse.StagingareaBrowse.init(state);
                                                                 }
                                                                 }-*/;

}
