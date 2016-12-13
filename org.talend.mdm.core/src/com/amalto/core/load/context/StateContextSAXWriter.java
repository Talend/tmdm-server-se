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

import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.amalto.core.load.Constants;

/**
 *
 */
public class StateContextSAXWriter implements StateContextWriter {

    private final ContentHandler contentHandler;

    private final boolean escapeCharacters;

    public StateContextSAXWriter(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
        // Using Qizx in server mode , characters must be escaped. See TMDM-4977 & TMDM-3780
        Properties props = MDMConfiguration.getConfiguration();
        String serverClass = props.getProperty("xmlserver.class"); //$NON-NLS-1$
        if ("org.talend.mdm.qizx.xmldb.QizxWrapper".equals(serverClass)) { //$NON-NLS-1$
            String qizxType = props.getProperty("qizx.db.type"); //$NON-NLS-1$
            escapeCharacters = "server".equals(qizxType); //$NON-NLS-1$
        } else {
            escapeCharacters = false;
        }
    }

    public void writeEndDocument() throws XMLStreamException, SAXException {
        contentHandler.endDocument();
    }

    public void writeEndElement(XMLStreamReader reader) throws XMLStreamException, SAXException {
        Map<String, String> prefixToNamespace = Utils.parseNamespace(reader);
        Set<Map.Entry<String, String>> entries = prefixToNamespace.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            contentHandler.endPrefixMapping(entry.getKey());
        }

        contentHandler.endElement(getURI(reader), reader.getLocalName(), reader.getName().getLocalPart());
    }

    public void writeCharacters(XMLStreamReader reader) throws Exception {
        char[] characters = reader.getTextCharacters();
        int textStart = reader.getTextStart();
        char[] chars = ArrayUtils.subarray(characters, textStart, textStart + reader.getTextLength());
        if (escapeCharacters) {
            chars = StringEscapeUtils.escapeXml(new String(chars)).toCharArray();
        }
        contentHandler.characters(chars, 0, chars.length);
    }

    public void writeStartElement(XMLStreamReader reader) throws XMLStreamException, SAXException {
        // Namespace parsing
        Map<String, String> prefixToNamespace = Utils.parseNamespace(reader);
        Set<Map.Entry<String, String>> entries = prefixToNamespace.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            contentHandler.startPrefixMapping(entry.getKey(), entry.getValue());
        }

        // Attribute parsing
        Attributes attributes = Utils.parseAttributes(reader);

        // Start new XML element
        contentHandler.startElement(getURI(reader),
                reader.getLocalName(),
                reader.getName().getLocalPart(),
                attributes);
    }

    public void flush(ContentHandler contentHandler) throws Exception {
        // Nothing to do
    }

    public void writeStartElement(String elementLocalName) throws Exception {
        contentHandler.startElement(StringUtils.EMPTY, elementLocalName, elementLocalName, Constants.EMPTY_ATTRIBUTES);
    }

    public void writeCharacters(String characters) throws Exception {
        char[] chars;
        if (escapeCharacters) {
            chars = StringEscapeUtils.escapeXml(characters).toCharArray();
        } else {
            chars = characters.toCharArray();
        }
        contentHandler.characters(chars, 0, chars.length);
    }

    public void writeEndElement(String elementLocalName) throws Exception {
        contentHandler.endElement(StringUtils.EMPTY, elementLocalName, elementLocalName);
    }

    private static String getURI(XMLStreamReader reader) {
        String namespaceURI = reader.getNamespaceURI();
        return namespaceURI == null ? StringUtils.EMPTY : namespaceURI; // SAX consumer (such as Qizx) doesn't like null NS.
    }
}
