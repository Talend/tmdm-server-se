/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.load.context;

import com.amalto.core.load.Constants;
import com.amalto.core.load.process.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class BufferStateContextWriter implements StateContextWriter {
    private final List<PayloadProcessedElement> processedElements = new ArrayList<PayloadProcessedElement>();

    public BufferStateContextWriter() {
    }

    public void writeEndDocument() throws XMLStreamException {
        processedElements.add(new PayloadProcessedElement() {
            public void flush(ContentHandler contentHandler) throws SAXException {
                contentHandler.endDocument();
            }
        });
    }

    public void writeEndElement(XMLStreamReader reader) throws XMLStreamException {
        ProcessedEndElement endElement = new ProcessedEndElement(reader.getNamespaceURI(),
                reader.getLocalName(),
                reader.getName().getLocalPart());
        processedElements.add(endElement);

        // Namespace parsing
        Map<String, String> prefixToNamespace = Utils.parseNamespace(reader);
        Set<Map.Entry<String, String>> entries = prefixToNamespace.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            processedElements.add(new ProcessedEndNamespace(entry.getKey()));
        }
    }

    public void writeCharacters(XMLStreamReader reader) throws XMLStreamException {
        char[] characters = reader.getTextCharacters();
        int textStart = reader.getTextStart();
        char[] textCharacters = ArrayUtils.subarray(characters, textStart, textStart + reader.getTextLength());
        processedElements.add(new ProcessedCharacters(textCharacters));
    }

    public void writeStartElement(XMLStreamReader reader) throws XMLStreamException {
        // Attribute parsing
        Attributes attributes = Utils.parseAttributes(reader);

        // Namespace parsing
        Map<String, String> prefixToNamespace = Utils.parseNamespace(reader);
        Set<Map.Entry<String, String>> entries = prefixToNamespace.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            processedElements.add(new ProcessedStartNamespace(entry.getKey(), entry.getValue()));
        }

        // New start element
        ProcessedStartElement startElement = new ProcessedStartElement(reader.getNamespaceURI(),
                reader.getLocalName(),
                reader.getName().getLocalPart(),
                attributes
        );
        processedElements.add(startElement);
    }

    public void flush(ContentHandler contentHandler) throws Exception {
        for (PayloadProcessedElement processedElement : processedElements) {
            processedElement.flush(contentHandler);
        }
        processedElements.clear();
    }

    public void writeStartElement(String elementLocalName) throws Exception {
        // New start element
        ProcessedStartElement startElement = new ProcessedStartElement(StringUtils.EMPTY,
                elementLocalName,
                elementLocalName,
                Constants.EMPTY_ATTRIBUTES
        );
        processedElements.add(startElement);
    }

    public void writeCharacters(String characters) throws Exception {
        // Characters
        processedElements.add(new ProcessedCharacters(characters.toCharArray()));
    }

    public void writeEndElement(String elementLocalName) throws Exception {
        // End element
        ProcessedEndElement endElement = new ProcessedEndElement(StringUtils.EMPTY,
                elementLocalName,
                elementLocalName);
        processedElements.add(endElement);
    }

    public BufferStateContextWriter reset() {
        processedElements.clear();
        return this;
    }

}
