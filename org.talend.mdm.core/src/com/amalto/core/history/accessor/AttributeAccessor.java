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

package com.amalto.core.history.accessor;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;

import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.action.FieldUpdateAction;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

/**
 *
 */
class AttributeAccessor implements DOMAccessor {

    private static final Logger logger = Logger.getLogger(AttributeAccessor.class);

    private final DOMAccessor parent;

    private final String attributeName;
    
    private final String xpath;

    private final MutableDocument document;

    public AttributeAccessor(DOMAccessor parent, String xpath, MutableDocument document) {
        this.parent = parent;
        this.xpath = StringUtils.substringBefore(xpath, "/@"); //$NON-NLS-1$
        this.attributeName = StringUtils.substringAfter(xpath, "@"); //$NON-NLS-1$
        this.document = document;
    }

    private Node getAttribute() {
        Node parentNode = parent.getNode();
        if (parentNode == null) {
            throw new IllegalStateException("Could not find a parent node in document (check if document has a root element)."); //$NON-NLS-1$
        }
        NamedNodeMap attributes = parentNode.getAttributes();
        if (attributes == null) {
            throw new IllegalStateException("Could not find attributes on parent node."); //$NON-NLS-1$
        }

        QName qName = getQName(document.asDOM());
        Node attribute = attributes.getNamedItemNS(qName.getNamespaceURI(), qName.getLocalPart());
        if (attribute == null) {
            // Look up with namespace didn't work, falls back to standard getNamedItem
            attribute = attributes.getNamedItem(qName.getLocalPart());
        }
        return attribute;
    }

    private Attr createAttribute(Node parentNode, Document domDocument) {
        // Ensure xsi prefix is declared
        if (attributeName.indexOf(':') > 0) {
            String attributePrefix = StringUtils.substringBefore(attributeName, ":"); //$NON-NLS-1$
            String namespaceURI = domDocument.lookupNamespaceURI(attributePrefix);
            if (namespaceURI == null) {
                if ("xsi".equals(attributePrefix)) { //$NON-NLS-1$
                    domDocument.getDocumentElement().setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:xsi", XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI);  //$NON-NLS-1$
                } else if ("tmdm".equals(attributePrefix)) { //$NON-NLS-1$
                    domDocument.getDocumentElement().setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:tmdm", SkipAttributeDocumentBuilder.TALEND_NAMESPACE);  //$NON-NLS-1$
                } else {
                    throw new IllegalArgumentException("Unrecognized attribute prefix: '" + attributePrefix + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        QName qName = getQName(domDocument);
        Attr newAttribute = domDocument.createAttributeNS(qName.getNamespaceURI(), qName.getLocalPart());
        String prefix = qName.getPrefix();
        newAttribute.setPrefix(prefix);
        parentNode.getAttributes().setNamedItemNS(newAttribute);
        return newAttribute;
    }

    private QName getQName(Document domDocument) {
        QName qName;
        String prefix = StringUtils.substringBefore(attributeName, ":"); //$NON-NLS-1$
        String name = StringUtils.substringAfter(attributeName, ":"); //$NON-NLS-1$
        if (name.isEmpty()) {
            // No prefix (so prefix is attribute name due to substring calls).
            String attributeNamespaceURI = domDocument.getDocumentURI();
            if (attributeNamespaceURI == null || attributeNamespaceURI.isEmpty()) {
                Node attributeNode = getAttributeNode(domDocument);
                if (attributeNode != null) {
                    attributeNamespaceURI = attributeNode.getNamespaceURI();
                }
            }
            qName = new QName(attributeNamespaceURI,prefix);
        } else {
            String attributeNamespaceURI = domDocument.lookupNamespaceURI(prefix);
            if (attributeNamespaceURI == null || attributeNamespaceURI.isEmpty()) {
                Node attributeNode = getAttributeNode(domDocument);
                if (attributeNode != null) {
                    attributeNamespaceURI = attributeNode.lookupNamespaceURI(prefix);
                }
            }
            qName = new QName(attributeNamespaceURI, name, prefix);
        }
        return qName;
    }
    
    private Node getAttributeNode(Document domDocument) {
        Node attributeNode = null;
        try {
            NodeList attributeNodeList = Util.getNodeList(domDocument, xpath);
            if (attributeNodeList.getLength() > 0) {
                attributeNode = attributeNodeList.item(0);                
            }
        } catch (XtentisException e) {
            logger.error(e);            
        }
        return attributeNode;
    }

    public void set(String value) {
        Node namedItem = getAttribute();
        if (namedItem == null) {
            throw new IllegalStateException("Attribute '" + attributeName + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (!(namedItem instanceof Attr)) {
            throw new IllegalStateException("Expected a " + Attr.class.getName() + " instance but got a " + namedItem.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Attr attribute = (Attr) namedItem;
        attribute.setValue(value);
    }

    public String get() {
        Node namedItem = getAttribute();
        if (!(namedItem instanceof Attr)) {
            throw new IllegalStateException("Expected a " + Attr.class.getName() + " instance but got a " + namedItem.getClass().getName()); //$NON-NLS-1$ //$NON-NLS-2$
        }

        Attr attribute = (Attr) namedItem;
        return attribute.getValue();
    }

    public void touch() {
        // Nothing to do.
    }

    public Node getNode() {
        return getAttribute();
    }

    public void create() {
        // Ensure everything is created in parent nodes.
        parent.create();

        // Create the attribute if it does not exist
        Document domDocument = document.asDOM();
        Node parentNode = parent.getNode();
        Node attribute = getAttribute();
        if (attribute == null) {
            createAttribute(parentNode, domDocument);
        }
    }

    public void insert() {
        create();
    }

    public void createAndSet(String value) {
        // Ensure everything is created in parent nodes.
        parent.create();

        Document domDocument = document.asDOM();
        Node parentNode = parent.getNode();
        Attr attribute = createAttribute(parentNode, domDocument);
        attribute.setValue(value);
    }

    public void delete() {
        if (exist()) {
            Node parentNode = parent.getNode();
            Node attribute = getAttribute();
            if (attribute != null) {
                parentNode.getAttributes().removeNamedItemNS(attribute.getNamespaceURI(), attribute.getLocalName());
            } else {
                logger.warn("Attempt to delete the attribute '" + attributeName + "'that does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
    }

    public boolean exist() {
        return parent.exist() && getAttribute() != null;
    }

    public void markModified(Marker marker) {
        Document domDocument = document.asDOM();
        Node parentNode = parent.getNode();
        if (parentNode != null) {
            Attr newAttribute = domDocument.createAttribute(MODIFIED_MARKER_ATTRIBUTE);
            switch(marker) {
                case ADD:
                    newAttribute.setValue(FieldUpdateAction.MODIFY_ADD_MARKER_VALUE);
                    break;
                case UPDATE:
                    newAttribute.setValue(FieldUpdateAction.MODIFY_UPDATE_MARKER_VALUE);
                    break;
                case REMOVE:
                    newAttribute.setValue(FieldUpdateAction.MODIFY_REMOVE_MARKER_VALUE);
                    break;
                default:
                    throw new IllegalArgumentException("No support for marker " + marker); //$NON-NLS-1$
            }            
            parentNode.getAttributes().setNamedItem(newAttribute);
        }
    }

    public void markUnmodified() {
        Node parentNode = parent.getNode();
        NamedNodeMap attributes = parentNode.getAttributes();
        if (attributes.getNamedItem(MODIFIED_MARKER_ATTRIBUTE) != null) {
            attributes.removeNamedItem(MODIFIED_MARKER_ATTRIBUTE);
        }
    }

    public int size() {
        if (!exist()) {
            return 0;
        }
        return 1;
    }

    public String getActualType() {
        throw new UnsupportedOperationException("Attributes can't override their type."); //$NON-NLS-1$
    }

    @Override
    public int compareTo(Accessor accessor) {
        if (exist() != accessor.exist()) {
            return -1;
        }
        if (exist()) {
            return get().equals(accessor.get()) ? 0 : -1;
        }
        return -1;
    }
}
