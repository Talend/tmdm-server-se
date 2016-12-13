/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.client;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.talend.mdm.webapp.stagingarea.control.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.stagingarea.control.shared.model.PreviousExecutionModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaValidationModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingContainerModel;

public class GenerateContainer {

    private static final StagingAreaValidationModel validationModel;

    private static final StagingContainerModel containerModel;

    private static final PreviousExecutionModel previousExecutionModel;

    private static ContentPanel instance;

    static {
        validationModel = new StagingAreaValidationModel();
        containerModel = new StagingContainerModel();
        previousExecutionModel = new PreviousExecutionModel();
    }

    public static StagingAreaValidationModel getValidationModel() {
        return validationModel;
    }

    public static StagingContainerModel getContainerModel() {
        return containerModel;
    }

    public static PreviousExecutionModel getPreviousExecutionModel() {
        return previousExecutionModel;
    }

    public static void generateContentPanel() {
        if (instance != null) {
            instance.removeFromParent();
        }
        instance = new ContentPanel();
        instance.setLayout(new FitLayout());
        instance.setId(StagingAreaControl.STAGINGAREA_ID);
        instance.setHeading(MessagesFactory.getMessages().stagingarea_title());
        instance.setHeaderVisible(false);
        instance.setBodyBorder(false);
    }

    public static ContentPanel getContentPanel() {
        return instance;
    }
}
