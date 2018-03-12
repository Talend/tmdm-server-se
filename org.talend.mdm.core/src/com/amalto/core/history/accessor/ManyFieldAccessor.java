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

import com.amalto.core.history.DOMMutableDocument;
import com.amalto.core.history.action.FieldUpdateAction;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.*;

import javax.xml.XMLConstants;

/**
 *
 */
class ManyFieldAccessor implements DOMAccessor {

    private final DOMAccessor parent;

    private final String fieldName;

    private final int index;

    private final DOMMutableDocument document;

    private Element cachedCollectionItemNode;

    public ManyFieldAccessor(DOMAccessor parent, String fieldName, int index, DOMMutableDocument document) {
        this.parent = parent;
        this.fieldName = fieldName;
        this.index = index;
        this.document = document;
    }

    private Element getCollectionItemNode() {
        if (cachedCollectionItemNode != null) {
            return cachedCollectionItemNode;
        }
        Element collectionItemNode = null;
        Node node = parent.getNode();
        if (node == null) {
            throw new IllegalStateException("Could not find parent node in document.");
        }
        NodeList children = node.getChildNodes();
        if (index > children.getLength()) {
            return null;
        }
        int currentIndex = 0;
        Node current = node.getFirstChild();
        while (current != null) {
            if (fieldName.equals(current.getNodeName())) {
                if (index == currentIndex) {
                    collectionItemNode = (Element) current;
                    break;
                } else {
                    currentIndex++;
                }
            }
            current = current.getNextSibling();
        }
        cachedCollectionItemNode = collectionItemNode;
        return collectionItemNode;
    }

    public void set(String value) {
        Node collectionItemNode = getCollectionItemNode();
        collectionItemNode.setTextContent(value);
    }

    public String get() {
        Node collectionItemNode = getCollectionItemNode();
        Node firstChild = collectionItemNode.getFirstChild();
        if (firstChild != null) {
            if (firstChild instanceof Text)
                return collectionItemNode.getTextContent();// get node value can not handle bracket well
            else
                return firstChild.getNodeValue();
        } else {
            return StringUtils.EMPTY;
        }
    }

    public void touch() {
        document.setLastAccessedNode(getCollectionItemNode());
    }

    public Node getNode() {
        return getCollectionItemNode();
    }

    public void create() {
        parent.create();

        // TODO Refactor this
        Document domDocument = document.asDOM();
        Node node = getCollectionItemNode();
        if (node == null) {
            Element parentNode = (Element) parent.getNode();
            NodeList children = parentNode.getElementsByTagName(fieldName);
            int currentCollectionSize = children.getLength();
            if (currentCollectionSize > 0) {
                Node refNode = children.item(currentCollectionSize - 1).getNextSibling();
                while (currentCollectionSize <= index) {
                    node = domDocument.createElementNS(domDocument.getNamespaceURI(), fieldName);
                    parentNode.insertBefore(node, refNode);
                    currentCollectionSize++;
                }
            } else {
                // Collection is not present at all, append at the end of parent element.
                Node lastAccessedNode = document.getLastAccessedNode();
                if (lastAccessedNode != null) {
                    Node refNode = lastAccessedNode.getNextSibling();
                    while (refNode != null && !(refNode instanceof Element)) {
                        refNode = refNode.getNextSibling();
                    }
                    while (currentCollectionSize <= index) {
                        node = domDocument.createElementNS(domDocument.getNamespaceURI(), fieldName);
                        if (lastAccessedNode == parentNode) {
                            if(lastAccessedNode == document.asDOM().getDocumentElement() 
                                    && lastAccessedNode.getChildNodes().getLength() > 0)
                                parentNode.insertBefore(node, parentNode.getFirstChild());
                            else
                                parentNode.appendChild(node);
                        } else if (refNode != null && refNode.getParentNode() == parentNode) {
                            parentNode.insertBefore(node, refNode);
                        } else {
                            parentNode.appendChild(node);
                        }
                        currentCollectionSize++;
                    }
                } else {
                    while (currentCollectionSize <= index) {
                        node = domDocument.createElementNS(domDocument.getNamespaceURI(), fieldName);
                        parentNode.appendChild(node);
                        currentCollectionSize++;
                    }
                }
            }
            document.setLastAccessedNode(node);
        } else if (node.getChildNodes().getLength() == 0) {
            // This accessor creates (n-1) empty elements when accessing first collection element at index n.
            // This setLastAccessedNode call allows all (n-1) elements to find their parent.
            if (document.getLastAccessedNode() == null || (document.getLastAccessedNode() != null && !(node.getLocalName().equals(document.getLastAccessedNode().getLocalName()) && document.getLastAccessedNode()
                    .getParentNode() == node.getParentNode()))) {
                // if last accessed node is parallel with this node, can't modify last accessed node. eg, last accessed
                // node=/feature/vendor[2], this node=/feature/vendor[1], the last accessed is still /feature/vendor[2]
                document.setLastAccessedNode(node);
            }
        }
    }

    public void insert() {
        Document domDocument = document.asDOM();
        Element parentNode = (Element) parent.getNode();
        NodeList children = parentNode.getElementsByTagName(fieldName);
        Node refNode = children.item(index);
        Node node = domDocument.createElementNS(domDocument.getNamespaceURI(), fieldName);
        parentNode.insertBefore(node, refNode);
        cachedCollectionItemNode = (Element) node;
    }

    public void createAndSet(String value) {
        create();
        set(value);
    }

    public void delete() {
        if (exist()) {
            Node node = getCollectionItemNode();
            if (node == null) {
                return; // Node has already been deleted.
            }
            node.getParentNode().removeChild(node);
        }

        // if the parent is exist and have no the child, will remove.
        if (parent.exist() && isEmptyChildForXSIType()) {
            Node parentNode = parent.getNode();
            parentNode.getParentNode().removeChild(parentNode);
        }
    }

    public boolean exist() {
        return parent.exist() && getCollectionItemNode() != null;
    }

    public boolean isEmptyChildForXSIType() {
        Node parentNode = parent.getNode();
        NodeList childs = parentNode.getChildNodes();
        return childs.getLength() == 0;
    }

    public void markModified(Marker marker) {
        Document domDocument = document.asDOM();
        Node collectionItemNode = getCollectionItemNode();
        if (collectionItemNode != null) {
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
            collectionItemNode.getAttributes().setNamedItem(newAttribute);
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
        Node parentNode = getCollectionItemNode().getParentNode();
        if (parentNode instanceof Element) {
            return ((Element) parentNode).getElementsByTagName(fieldName).getLength();
        } else {
            return 0;
        }
    }

    public String getActualType() {
        Attr type = getCollectionItemNode().getAttributeNodeNS(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI, "type"); //$NON-NLS-1$
        if (type == null) {
            return StringUtils.EMPTY;
        } else {
            return type.getValue();
        }
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
