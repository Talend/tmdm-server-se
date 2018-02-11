/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.save;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.history.DOMMutableDocument;
import com.amalto.core.history.DeleteType;
import com.amalto.core.history.Document;
import com.amalto.core.history.DocumentTransformer;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.history.accessor.DOMAccessorFactory;
import com.amalto.core.save.context.SaverContextFactory;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;

public class DOMDocument implements DOMMutableDocument {

    private org.w3c.dom.Document domDocument;

    private Node lastAccessedNode;

    private String rootElementName;

    private final ComplexTypeMetadata type;

    private final String dataModelName;

    private final String dataCluster;

    private String taskId;

    private boolean isDeleted = false;

    public DOMDocument(org.w3c.dom.Document domDocument, ComplexTypeMetadata type, String dataCluster, String dataModelName) {
        this.type = type;
        this.dataCluster = dataCluster;
        this.dataModelName = dataModelName;
        init(domDocument);
    }

    public DOMDocument(Node node, ComplexTypeMetadata type, String dataCluster, String dataModelName) {
        this.type = type;
        this.dataCluster = dataCluster;
        this.dataModelName = dataModelName;
        DocumentBuilder documentBuilder = SaverContextFactory.DOCUMENT_BUILDER;
        org.w3c.dom.Document document = documentBuilder.newDocument();
        document.adoptNode(node);
        document.appendChild(node);
        init(document);
    }

    private void init(org.w3c.dom.Document domDocument) {
        this.domDocument = domDocument;
        Element documentElement = domDocument.getDocumentElement();

        if (documentElement != null) {
            rootElementName = documentElement.getTagName();
        } else {
            rootElementName = null;
        }
    }

    @Override
    public Node getLastAccessedNode() {
        return lastAccessedNode;
    }

    @Override
    public void setLastAccessedNode(Node lastAccessedNode) {
        this.lastAccessedNode = lastAccessedNode;
    }

    public MutableDocument copy() {
        return new DOMDocument((org.w3c.dom.Document) domDocument.cloneNode(true), type, dataCluster, dataModelName);
    }

    @Override
    public void clean() {
        clean(domDocument.getDocumentElement(), EmptyElementCleaner.INSTANCE, false);
    }

    @Override
    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    private void clean(Element element, Cleaner cleaner, boolean removeTalendAttributes) {
        if (element == null) {
            return;
        }
        if (removeTalendAttributes) {
            element.removeAttributeNS(SkipAttributeDocumentBuilder.TALEND_NAMESPACE, "type"); //$NON-NLS-1$
        }
        Node current = element.getLastChild();
        while (current != null) {
            Node next = current.getPreviousSibling();
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                Element currentElement = (Element) current;
                clean(currentElement, cleaner, removeTalendAttributes);
            }
            current = next;
        }
        if (cleaner.clean(element)) {
            element.getParentNode().removeChild(element);
        }
    }

    interface Cleaner {
        /**
         * Indicates to the caller whether <code>element</code> should be deleted or not.<br/>
         *
         * @param element An element to clean
         * @return <code>true</code> if element should be removed by caller, <code>false</code> otherwise.
         */
        boolean clean(Element element);
    }

    private static class EmptyElementCleaner implements Cleaner {

        static final Cleaner INSTANCE = new EmptyElementCleaner();

        public boolean clean(Element element) {
            return element == null || !element.hasChildNodes();
        }
    }

    public String exportToString() {
        try {
            Element documentElement = domDocument.getDocumentElement();
            // TMDM-6900 Ensure the xsi prefix is declared in exported document when save uses a DOM document.
            documentElement.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI,
                    "xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI); //$NON-NLS-1$
            return XMLUtils.nodeToString(documentElement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String exportToStringWithNullFields() {
        return exportToString();
    }

    public Document transform(DocumentTransformer transformer) {
        if (transformer == null) {
            throw new IllegalArgumentException("Transformer argument cannot be null");
        }
        return transformer.transform(this);
    }

    public void restore() {
        throw new UnsupportedOperationException("Restore not supported.");
    }

    @Override
    public ComplexTypeMetadata getType() {
        return type;
    }

    @Override
    public String getDataModel() {
        return dataModelName;
    }

    @Override
    public String getDataCluster() {
        return dataCluster;
    }

    @Override
    public String getTaskId() {
        return taskId;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public DeleteType getDeleteType() {
        return null;
    }

    public Accessor createAccessor(String path) {
        if (rootElementName != null) {
            return DOMAccessorFactory.createAccessor(rootElementName + '/' + path, this);
        } else {
            return DOMAccessorFactory.createAccessor(path, this);
        }
    }

    public org.w3c.dom.Document asDOM() {
        return domDocument;
    }

    @Override
    public org.w3c.dom.Document asValidationDOM() {
        org.w3c.dom.Document validationDOM = (org.w3c.dom.Document) domDocument.cloneNode(true);
        clean(validationDOM.getDocumentElement(), EmptyElementCleaner.INSTANCE, true);
        return validationDOM;
    }

    public MutableDocument create(MutableDocument content) {
        init(content.asDOM());
        return this;
    }

    public MutableDocument setContent(MutableDocument content) {
        init(content.asDOM());
        return this;
    }

    public MutableDocument delete(DeleteType deleteType) {
        isDeleted = true;
        return this;
    }

    public MutableDocument recover(DeleteType deleteType) {
        return this;
    }

    public Document applyChanges() {
        return this;
    }

    @Override
    public boolean considerMissingElementsAsEmpty() {
        return false;
    }

}
