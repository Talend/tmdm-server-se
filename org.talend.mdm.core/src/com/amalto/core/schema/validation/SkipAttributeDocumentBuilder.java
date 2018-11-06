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

package com.amalto.core.schema.validation;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.amalto.core.load.io.ResettableStringWriter;

public class SkipAttributeDocumentBuilder extends DocumentBuilder {

    public static final String TALEND_NAMESPACE = "http://www.talend.com/mdm"; //$NON-NLS-1$

    private static final Map<Thread, SAXParser> parserCache = new HashMap<Thread, SAXParser>();

    private final DocumentBuilder documentBuilder;

    private final boolean ignoreTalendNamespace;

    public SkipAttributeDocumentBuilder(DocumentBuilder documentBuilder, boolean ignoreTalendNamespace) {
        this.documentBuilder = documentBuilder;
        this.ignoreTalendNamespace = ignoreTalendNamespace;
    }

    @Override
    public Document parse(InputSource is) throws SAXException, IOException {
        SAXParser parser;
        try {
            parser = getSaxParser();
        } catch (ParserConfigurationException e) {
            throw new SAXException(e);
        }
        try {
            Document document = newDocument();
            parser.parse(is, new SkipAttributeHandler(document, ignoreTalendNamespace));
            return document;
        }  finally {
            parser.reset();
        }
    }

    private static SAXParser getSaxParser() throws ParserConfigurationException, SAXException {
        synchronized (parserCache) {
            SAXParser parser = parserCache.get(Thread.currentThread());
            if (parser == null) {
                parser = MDMXMLUtils.getSAXParser();
                parserCache.put(Thread.currentThread(), parser);
            }
            return parser;
        }
    }

    @Override
    public boolean isNamespaceAware() {
        return documentBuilder.isNamespaceAware();
    }

    @Override
    public boolean isValidating() {
        return documentBuilder.isValidating();
    }

    @Override
    public void setEntityResolver(EntityResolver er) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setErrorHandler(ErrorHandler eh) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Document newDocument() {
        return documentBuilder.newDocument();
    }

    @Override
    public DOMImplementation getDOMImplementation() {
        throw new UnsupportedOperationException();
    }

    private static class SkipAttributeHandler extends DefaultHandler {

        private final Document document;

        private final Stack<Element> elementStack = new Stack<Element>();

        private final boolean ignoreTalendNamespace;

        private final Set<String> declaredNamespaces = new HashSet<String>();

        private final ResettableStringWriter textValue = new ResettableStringWriter();

        public SkipAttributeHandler(Document document, boolean ignoreTalendNamespace) {
            this.document = document;
            this.ignoreTalendNamespace = ignoreTalendNamespace;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            Element element = document.createElementNS(uri, qName);
            for (int i = 0; i < attributes.getLength(); i++) {
                String value = attributes.getValue(i);
                String qualifiedName = attributes.getQName(i);
                String namespaceURI = attributes.getURI(i);
                if (namespaceURI != null) { //$NON-NLS-1$
                    // Ignore everything else (e.g. 'tmdm'...) if we have to.
                    if (TALEND_NAMESPACE.equals(namespaceURI) && !ignoreTalendNamespace) {
                        Attr attribute = document.createAttributeNS(namespaceURI, qualifiedName);
                        attribute.setValue(value);
                        element.setAttributeNodeNS(attribute);
                    } else if(!TALEND_NAMESPACE.equals(namespaceURI)) {
                        Attr attribute = document.createAttributeNS(namespaceURI, qualifiedName);
                        attribute.setValue(value);
                        element.setAttributeNodeNS(attribute);
                    }
                    if (!declaredNamespaces.contains(namespaceURI)) {
                        // Add a xmlns:attribute to document element
                        Attr attribute = document.createAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns:" + StringUtils.substringBefore(qualifiedName, ":"));
                        attribute.setValue(namespaceURI);
                        Element documentElement = element.getOwnerDocument().getDocumentElement();
                        if (documentElement != null) {
                            documentElement.setAttributeNodeNS(attribute);
                            declaredNamespaces.add(namespaceURI);
                        }
                    }
                }
            }
            if (elementStack.empty()) {
                document.appendChild(element);
            }
            elementStack.push(element);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            Element newElement = elementStack.pop();
            if (!elementStack.empty()) {
                Element parent = elementStack.peek();
                parent.appendChild(newElement);
                if (textValue.getBuffer().length() > 0) {
                    Text textNode = document.createTextNode(textValue.reset());
                    newElement.appendChild(textNode);
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            String value = new String(ch, start, length);
            // Ignore empty strings (incl. those with line feeds).
            if (!value.trim().isEmpty() || textValue.getBuffer().length() > 0) {
                textValue.append(value);
            }
        }
    }
}
