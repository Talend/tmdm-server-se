/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.shared.controller.rest;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import org.restlet.client.Response;
import org.restlet.client.data.Status;
import org.restlet.client.ext.xml.DomRepresentation;
import org.talend.mdm.webapp.base.client.rest.RestServiceHelper;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaExecutionModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingAreaValidationModel;
import org.talend.mdm.webapp.stagingarea.control.shared.model.StagingContainerModel;

import java.io.IOException;

class StagingModelConverter {

    public static void set(Response response, StagingContainerModel model) throws IOException {
        DomRepresentation domRepresentation = RestServiceHelper.getDomRepresentationFromResponse(response);
        if (domRepresentation != null) {
            NodeList nodeList = domRepresentation.getDocument().getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeValue = node.getFirstChild().getNodeValue();
                    if (node.getNodeName().equals("data_container")) { //$NON-NLS-1$
                        model.setDataContainer(nodeValue);
                    } else if (node.getNodeName().equals("data_model")) { //$NON-NLS-1$
                        model.setDataModel(nodeValue);
                    } else if (node.getNodeName().equals("invalid_records")) { //$NON-NLS-1$
                        model.setInvalidRecords(nodeValue == null ? 0 : Integer.parseInt(nodeValue));
                    } else if (node.getNodeName().equals("total_records")) { //$NON-NLS-1$
                        model.setTotalRecords(nodeValue == null ? 0 : Integer.parseInt(nodeValue));
                    } else if (node.getNodeName().equals("valid_records")) { //$NON-NLS-1$
                        model.setValidRecords(nodeValue == null ? 0 : Integer.parseInt(nodeValue));
                    } else if (node.getNodeName().equals("waiting_validation_records")) { //$NON-NLS-1$
                        model.setWaitingValidationRecords(nodeValue == null ? 0 : Integer.parseInt(nodeValue));
                    }
                }
            }
        }
    }

    public static void set(Response response, StagingAreaValidationModel model) throws IOException {
        if (response.getStatus().getCode() == Status.SUCCESS_NO_CONTENT.getCode()) { // No current execution
            model.setId(null);
            return;
        }
        DomRepresentation domRepresentation = RestServiceHelper.getDomRepresentationFromResponse(response);
        if (domRepresentation != null) {
            // Loop on the nodes to retrieve the node names and text content.
            NodeList nodeList = domRepresentation.getDocument().getDocumentElement().getChildNodes();
            String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSz"; //$NON-NLS-1$
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeValue = node.getFirstChild().getNodeValue();
                    if (node.getNodeName().equals("id")) { //$NON-NLS-1$
                        model.setId(nodeValue);
                    } else if (node.getNodeName().equals("invalid_records")) { //$NON-NLS-1$
                        model.setInvalidRecords(nodeValue == null ? 0 : Integer.parseInt(nodeValue));
                    } else if (node.getNodeName().equals("processed_records")) { //$NON-NLS-1$
                        model.setProcessedRecords(nodeValue == null ? 0 : (int) Double
                                .parseDouble(nodeValue));
                    } else if (node.getNodeName().equals("start_date")) { //$NON-NLS-1$
                        model.setStartDate(nodeValue == null ? null : DateTimeFormat.getFormat(pattern)
                                .parse(nodeValue));
                    } else if (node.getNodeName().equals("total_record")) { //$NON-NLS-1$
                        model.setTotalRecord(nodeValue == null ? 0 : Integer.parseInt(nodeValue));
                    }
                }
            }
        }
    }


    public static StagingAreaExecutionModel response2StagingAreaExecutionModel(Response response) throws IOException {
        StagingAreaExecutionModel stagingAreaExecutionModel = null;
        DomRepresentation domRepresentation = RestServiceHelper.getDomRepresentationFromResponse(response);
        if (domRepresentation != null) {
            stagingAreaExecutionModel = new StagingAreaExecutionModel();
            // Loop on the nodes to retrieve the node names and text content.
            NodeList nodeList = domRepresentation.getDocument().getDocumentElement().getChildNodes();
            String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSz"; //$NON-NLS-1$
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    String nodeValue = node.getFirstChild().getNodeValue();
                    if (node.getNodeName().equals("end_date")) { //$NON-NLS-1$
                        stagingAreaExecutionModel.setEndDate(nodeValue == null ? null : DateTimeFormat.getFormat(pattern).parse(
                                nodeValue));
                    } else if (node.getNodeName().equals("id")) { //$NON-NLS-1$
                        stagingAreaExecutionModel.setId(nodeValue);
                    } else if (node.getNodeName().equals("invalid_records")) { //$NON-NLS-1$
                        stagingAreaExecutionModel.setInvalidRecords(nodeValue == null ? 0 : Integer.parseInt(nodeValue));
                    } else if (node.getNodeName().equals("processed_records")) { //$NON-NLS-1$
                        stagingAreaExecutionModel
                                .setProcessedRecords(nodeValue == null ? 0 : (int) Double.parseDouble(nodeValue));
                    } else if (node.getNodeName().equals("start_date")) { //$NON-NLS-1$
                        stagingAreaExecutionModel.setStartDate(nodeValue == null ? null : DateTimeFormat.getFormat(pattern)
                                .parse(nodeValue));
                    } else if (node.getNodeName().equals("total_record")) { //$NON-NLS-1$
                        stagingAreaExecutionModel.setTotalRecord(nodeValue == null ? 0 : Integer.parseInt(nodeValue));
                    }
                }
            }
        }
        return stagingAreaExecutionModel;
    }
}