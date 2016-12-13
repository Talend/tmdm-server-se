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

import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;
import org.restlet.client.data.MediaType;
import org.restlet.client.ext.xml.DomRepresentation;
import org.restlet.client.representation.Representation;
import org.talend.mdm.webapp.base.client.model.UserContextModel;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.stagingarea.control.client.GenerateContainer;
import org.talend.mdm.webapp.stagingarea.control.shared.controller.rest.StagingRestServiceHandler;
import org.talend.mdm.webapp.stagingarea.control.shared.event.ModelEvent;
import org.talend.mdm.webapp.stagingarea.control.shared.model.FilterModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaValidationModel;

public class ValidationController {

    private final StagingRestServiceHandler  serviceHandler = StagingRestServiceHandler.get();

    private final StagingAreaValidationModel model;

    public ValidationController(StagingAreaValidationModel model) {
        this.model = model;
    }

    public void refresh() {
        UserContextModel ucx = UserContextUtil.getUserContext();
        String dataContainer = ucx.getDataContainer();
        StagingRestServiceHandler.get().getValidationTaskStatus(dataContainer, model);
    }

    public void cancel() {
        UserContextModel ucx = UserContextUtil.getUserContext();
        StagingRestServiceHandler.get().cancelValidationTask(ucx.getDataContainer());
    }

    private Document buildFilterDocument(FilterModel filterModel) {
        Document filterDoc = XMLParser.createDocument();
        Element root = filterDoc.createElement("config"); //$NON-NLS-1$
        Element startDate = filterDoc.createElement("start-date"); //$NON-NLS-1$
        if (filterModel.getStartDate() != null) {
            startDate.appendChild(filterDoc.createTextNode(String.valueOf(filterModel.getStartDate().getTime())));
        }
        Element endDate = filterDoc.createElement("end-date"); //$NON-NLS-1$
        if (filterModel.getEndDate() != null) {
            endDate.appendChild(filterDoc.createTextNode(String.valueOf(filterModel.getEndDate().getTime())));
        }
        root.appendChild(startDate);
        root.appendChild(endDate);

        Element statusCodes = filterDoc.createElement("status-codes"); //$NON-NLS-1$
        for (String statusCode : filterModel.getStatusCodes()) {
            Element codeEl = filterDoc.createElement("code"); //$NON-NLS-1$
            codeEl.appendChild(filterDoc.createTextNode(statusCode));
            statusCodes.appendChild(codeEl);
        }

        Element concepts = filterDoc.createElement("concepts"); //$NON-NLS-1$
        for (String concept : filterModel.getConcepts()) {
            Element conceptEl = filterDoc.createElement("concept"); //$NON-NLS-1$
            conceptEl.appendChild(filterDoc.createTextNode(concept));
            concepts.appendChild(conceptEl);
        }
        root.appendChild(statusCodes);
        root.appendChild(concepts);
        filterDoc.appendChild(root);
        return filterDoc;
    }

    public void startValidation() {
        startValidation(null);
    }

    public void startValidation(FilterModel filterModel) {
        UserContextModel ucx = UserContextUtil.getUserContext();
        Representation entity = null;
        if (filterModel != null) {
            entity = new DomRepresentation(MediaType.APPLICATION_XML, buildFilterDocument(filterModel));
        }
        serviceHandler.runValidationTask(ucx.getDataContainer(), ucx.getDataModel(), entity);
        GenerateContainer.getValidationModel().notifyHandlers(
                new ModelEvent(ModelEvent.Types.VALIDATION_START, GenerateContainer.getValidationModel()));
    }
}
