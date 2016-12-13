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

import org.talend.mdm.webapp.stagingarea.control.client.GenerateContainer;

public class Controllers {

    private static final StagingController                 stagingController;

    private static final StagingContainerSummaryController stagingContainerSummaryController;

    private static final ValidationController              validationController;

    private static final PreviousExecutionController       previousExecutionController;

    private static final Controllers                       instance = new Controllers();

    static {
        stagingController = new StagingController();
        stagingContainerSummaryController = new StagingContainerSummaryController(GenerateContainer.getContainerModel());
        validationController = new ValidationController(GenerateContainer.getValidationModel());
        previousExecutionController = new PreviousExecutionController(GenerateContainer.getPreviousExecutionModel());
    }

    private Controllers() {
    }

    public static Controllers get() {
        return instance;
    }

    public StagingController getStagingController() {
        return stagingController;
    }

    public StagingContainerSummaryController getSummaryController() {
        return stagingContainerSummaryController;
    }

    public ValidationController getValidationController() {
        return validationController;
    }

    public PreviousExecutionController getPreviousExecutionController() {
        return previousExecutionController;
    }
}
