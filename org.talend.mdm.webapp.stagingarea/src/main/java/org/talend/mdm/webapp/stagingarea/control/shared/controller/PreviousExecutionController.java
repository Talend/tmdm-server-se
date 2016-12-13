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

import org.talend.mdm.webapp.stagingarea.control.shared.event.ModelEvent;
import org.talend.mdm.webapp.stagingarea.control.shared.model.PreviousExecutionModel;

import java.util.Date;

public class PreviousExecutionController {

    private final PreviousExecutionModel model;

    public PreviousExecutionController(PreviousExecutionModel model) {
        this.model = model;
    }

    public void setBeforeDate(Date beforeDate) {
        model.setBeforeDate(beforeDate);
    }

    public void refresh() {
        model.getStore().getLoader().load();
        model.notifyHandlers(new ModelEvent(ModelEvent.Types.PREVIOUS_EXECUTION_CHANGED, model));
    }
}
