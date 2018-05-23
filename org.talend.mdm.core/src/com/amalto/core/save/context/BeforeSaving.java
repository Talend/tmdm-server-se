/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import java.io.StringReader;
import java.util.Collection;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.save.DOMDocument;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.save.SaverHelper;
import com.amalto.core.save.SaverSession;
import com.amalto.core.save.UserAction;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.util.BeforeSavingErrorException;
import com.amalto.core.util.BeforeSavingFormatException;
import com.amalto.core.util.OutputReport;
import com.amalto.core.util.Util;

public class BeforeSaving implements DocumentSaver {
    
    public static final String TYPE_INFO = "info"; //$NON-NLS-1$

    public static final String TYPE_WARNING = "warning"; //$NON-NLS-1$

    public static final String TYPE_WARNING_APPROVED = "approved"; //$NON-NLS-1$

    public static final String TYPE_ERROR = "error"; //$NON-NLS-1$

    private static final XPath XPATH = XPathFactory.newInstance().newXPath();

    private DocumentSaver next;

    private String message = StringUtils.EMPTY;

    private String messageType;

    BeforeSaving(DocumentSaver next) {
        this.next = next;
    }

    public void save(SaverSession session, DocumentSaverContext context) {
        // Invoke the beforeSaving
        MutableDocument updateReportDocument = context.getUpdateReportDocument();
        if (updateReportDocument == null) {
            throw new IllegalStateException("Update report is missing."); //$NON-NLS-1$
        }
        OutputReport outputreport = session.getSaverSource().invokeBeforeSaving(context, updateReportDocument);

        if (outputreport != null) { // when a process was found
            String errorCode;
            message = outputreport.getMessage();
            if (!validateFormat(message))
                throw new BeforeSavingFormatException(message);
            try {
                Document doc = Util.parse(message);
                // handle output_report
                String xpath = "//report/message"; //$NON-NLS-1$
                Node errorNode;
                synchronized (XPATH) {
                    errorNode = (Node) XPATH.evaluate(xpath, doc, XPathConstants.NODE);
                }
                errorCode = null;
                if (errorNode instanceof Element) {
                    Element errorElement = (Element) errorNode;
                    errorCode = errorElement.getAttribute("type"); //$NON-NLS-1$
                    Node child = errorElement.getFirstChild();
                    if (child instanceof org.w3c.dom.Text) {
                        message = child.getTextContent();
                    }
                    else{  
                        message = ""; //$NON-NLS-1$   
                    }
                }

                if (TYPE_INFO.equals(errorCode)) {
                    messageType = TYPE_INFO;
                } else if (TYPE_WARNING.equals(errorCode)) {
                    if (SaverHelper.WarningApprovedBeforeSave.get()) {
                        messageType = TYPE_WARNING_APPROVED;
                    } else {
                        messageType = TYPE_WARNING;
                        return;
                    }
                    SaverHelper.WarningApprovedBeforeSave.remove();
                } else if (TYPE_ERROR.equals(errorCode)) {
                    throw new BeforeSavingErrorException(message);
                }

                // handle output_item
                if (outputreport.getItem() != null) {
                    xpath = "//exchange/item"; //$NON-NLS-1$
                    SkipAttributeDocumentBuilder builder = new SkipAttributeDocumentBuilder(SaverContextFactory.DOCUMENT_BUILDER, false);
                    doc =  builder.parse(new InputSource(new StringReader(outputreport.getItem())));
                    Node item;
                    synchronized (XPATH) {
                        item = (Node) XPATH.evaluate(xpath, doc, XPathConstants.NODE);
                    }
                    if (item != null && item instanceof Element) {
                        Node node = null;
                        Node current = item.getFirstChild();
                        while (current != null) {
                            if (current instanceof Element) {
                                node = current;
                                break;
                            }
                            current = item.getNextSibling();
                        }
                        if (node != null) {
                            // set back the modified item by the process
                            DOMDocument document = new DOMDocument(node,
                                    context.getUserDocument().getType(),
                                    context.getDataCluster(),
                                    context.getDataModelName());
                            context.setUserDocument(document);
                            context.setDatabaseDocument(null);
                            Collection<FieldMetadata> keys = context.getUserDocument().getType().getKeyFields();
                            boolean isAutoIncrementKey = false;
                            for (FieldMetadata key : keys) {
                                if (EUUIDCustomType.AUTO_INCREMENT.getName().equalsIgnoreCase(key.getType().getName())
                                        || EUUIDCustomType.UUID.getName().equalsIgnoreCase(key.getType().getName())) {
                                    isAutoIncrementKey = true;
                                }
                            }
                            // Don't re-generate auto-increment id & UUID
                            if (context.getUserAction() == UserAction.CREATE && !isAutoIncrementKey) {
                                context.setId(new String[0]); // Will re-read id from document.
                            }
                            // Redo a set of actions and security checks.
                            // TMDM-4599: Adds UpdateReport phase so a new update report is generated based on latest changes.
                            next = new ID(new GenerateActions(new Security(new UpdateReport(new ApplyActions(next)))));
                        }
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException("Exception occurred during before saving phase.", e); //$NON-NLS-1$
            }
        }

        MutableDocument databaseDocument = context.getDatabaseDocument();
        if (databaseDocument != null) {
            databaseDocument.clean();
        }
        next.save(session, context);
    }

    public String[] getSavedId() {
        return next.getSavedId();
    }

    public String getSavedConceptName() {
        return next.getSavedConceptName();
    }

    public String getBeforeSavingMessage() {
        return message;
    }
    
    public String getBeforeSavingMessageType() {
        return messageType;
    }
    
    public boolean validateFormat(String msg) {
        NodeList nodeList;
        try {
            Document document = Util.parse(msg.toLowerCase());
            nodeList = Util.getNodeList(document, "//report/message"); //$NON-NLS-1$
        } catch (Exception e) {
            return false;
        }
        if (nodeList.getLength() != 1) {
            return false;
        }
        Node reportNode = nodeList.item(0);
        if (reportNode.getNodeType() != Node.ELEMENT_NODE) {
            return false;
        }
        NamedNodeMap attrMap = reportNode.getAttributes();
        Node attribute = attrMap.getNamedItem("type"); //$NON-NLS-1$
        if (attribute == null) {
            return false;
        }
        if (!isValidMessageType(attribute.getNodeValue())) {
            return false;
        }
        NodeList messageNodeList = reportNode.getChildNodes();
        if (messageNodeList.getLength() > 1) {
            return false;
        }
        if (messageNodeList.getLength() == 0) {
            return true;
        }
        Node messageNode = messageNodeList.item(0);
        return messageNode.getNodeType() == Node.TEXT_NODE;
    }
    
    private boolean isValidMessageType(String type) {
        return TYPE_INFO.equalsIgnoreCase(type) || TYPE_WARNING.equalsIgnoreCase(type) || TYPE_ERROR.equalsIgnoreCase(type);
    }
}
