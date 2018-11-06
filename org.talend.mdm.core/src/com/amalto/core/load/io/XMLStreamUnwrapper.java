/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.load.io;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;

/**
 * A implementation of {@link Enumeration} that allows one to separately return XML fragments at a given level.
 * For example, the stream: <br/>
 * <p>
 * <code>
 * &lt;wrapper&gt;&lt;root&gt;&lt;/root&gt;&lt;root&gt;&lt;/root&gt;&lt;/wrapper&gt;
 * </code>
 * </p>
 * Will give the following 2 String instances : <br/>
 * <p>
 * <code>
 * &lt;root&gt;&lt;/root&gt;
 * </code> <br/>
 * <code>
 * &lt;root&gt;&lt;/root&gt;
 * </code>
 * </p>
 */
public class XMLStreamUnwrapper implements Enumeration<String> {

    // Means this class returns XML fragments for elements on level = 1 (may allow configuration for this later on).
    private static final int RECORD_LEVEL = 1;

    private final XMLEventReader reader;

    /**
     * Contains the next record to be returned by {@link #nextElement()}.
     */
    private final ResettableStringWriter stringWriter = new ResettableStringWriter();

    private final XMLOutputFactory xmlOutputFactory;
    
    private List<Namespace> rootNamespaceList = new ArrayList<Namespace>();

    private int level = 0;

    public XMLStreamUnwrapper(InputStream stream) {
        try {
            reader = MDMXMLUtils.createXMLEventReader(stream);
            // Skip to first record
            while (reader.hasNext() && level < RECORD_LEVEL) {
                final XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    // Declare root element namespaces (if any)
                    final StartElement startElement = event.asStartElement();
                    Iterator namespaces = startElement.getNamespaces();
                    while (namespaces.hasNext()) {
                        rootNamespaceList.add((Namespace) namespaces.next());
                    }
                    level++;
                }
            }
            xmlOutputFactory = XMLOutputFactory.newFactory();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Unexpected parsing configuration error.", e);
        }
    }

    @Override
    public boolean hasMoreElements() {
        moveToNext();
        return stringWriter.getBuffer().length() > 0;
    }

    @Override
    public String nextElement() {
        return stringWriter.reset();
    }

    /**
     * Moves to next record in stream and stores it in {@link #stringWriter}.
     */
    private void moveToNext() {
        try {
            XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(stringWriter);
            boolean hasMadeChanges;
            do {
                if (!reader.hasNext()) {
                    break;
                }
                hasMadeChanges = false; // Keep a state to skip line feeds
                final XMLEvent event = reader.nextEvent();
                if (event.isEndElement()) {
                    level--;
                } else if (event.isStartElement()) {
                    level++;
                } else if (event.isEndDocument()) {
                    level--;
                }
                if (level >= RECORD_LEVEL) {
                    if (event.isEndElement()) {
                        writer.writeEndElement();
                        hasMadeChanges = true;
                    } else if (event.isStartElement()) {
                        final StartElement startElement = event.asStartElement();
                        final QName name = startElement.getName();
                        writer.writeStartElement(name.getNamespaceURI(), name.getLocalPart());
                        boolean isRecordRootElement = (RECORD_LEVEL == level - 1);
                        if (isRecordRootElement) {
                            for (int i = 0; i < rootNamespaceList.size(); i++) {
                                Namespace namespace = rootNamespaceList.get(i);
                                writer.writeNamespace(namespace.getPrefix(), namespace.getNamespaceURI());
                            }
                        }
                        // Declare namespaces (if any)
                        final Iterator elementNamespaces = startElement.getNamespaces();
                        while (elementNamespaces.hasNext()) {
                            Namespace elementNamespace = (Namespace) elementNamespaces.next();
                            if (isRecordRootElement) {
                                if (rootNamespaceList.size() > 0) {
                                    for (int i = 0; i < rootNamespaceList.size(); i++) {
                                        Namespace namespace = rootNamespaceList.get(i);
                                        if (!namespace.getPrefix().equals(elementNamespace.getPrefix())
                                                || !namespace.getNamespaceURI().equals(elementNamespace.getNamespaceURI())) {
                                            writer.writeNamespace(elementNamespace.getPrefix(),
                                                    elementNamespace.getNamespaceURI());
                                        }
                                    }
                                } else {
                                    writer.writeNamespace(elementNamespace.getPrefix(), elementNamespace.getNamespaceURI());
                                }
                            } else {
                                writer.writeNamespace(elementNamespace.getPrefix(), elementNamespace.getNamespaceURI());
                            }
                        }
                        // Write attributes
                        final Iterator attributes = startElement.getAttributes();
                        while (attributes.hasNext()) {
                            Attribute attribute = (Attribute) attributes.next();
                            QName attributeName = attribute.getName();
                            String value = attribute.getValue();
                            if (StringUtils.isEmpty(attributeName.getNamespaceURI())) {
                                writer.writeAttribute(attributeName.getLocalPart(), value);
                            } else {
                                writer.writeAttribute(attributeName.getNamespaceURI(), attributeName.getLocalPart(), value);
                            }
                        }
                        hasMadeChanges = true;
                    } else if (event.isCharacters()) {
                        final String text = event.asCharacters().getData().trim();
                        if (!text.isEmpty()) {
                            writer.writeCharacters(text);
                            hasMadeChanges = true;
                        }
                    }
                }
            } while (level > RECORD_LEVEL || !hasMadeChanges);
            writer.flush();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Unexpected parsing exception.", e);
        }
    }
}
