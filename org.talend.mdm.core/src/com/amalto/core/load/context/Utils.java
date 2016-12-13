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

import com.amalto.core.load.Metadata;
import com.amalto.core.load.exception.ParserCallbackException;
import com.amalto.core.load.payload.FlushXMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.stream.XMLStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class Utils {

    private Utils() {
    }

    public static void doParserCallback(StateContext context, XMLStreamReader reader, Metadata metadata) {
        FlushXMLReader xmlReader = new FlushXMLReader(reader, context);
        InputSource input = new InputSource();
        input.setPublicId(generatePublicId(metadata));
        context.setFlushDone();

        try {
            context.getCallback().flushDocument(xmlReader, input);
        } catch (Throwable throwable) {
            throw new ParserCallbackException(throwable);
        }

        // Once flush is done, reset context
        context.reset();
    }

    private static String generatePublicId(Metadata metadata) {
        String publicId = metadata.getContainer() + '.' + metadata.getName();
        String[] metadataId = metadata.getId();
        for (String id : metadataId) {
            publicId += '.' + id;
        }
        return publicId;
    }

    public static Attributes parseAttributes(XMLStreamReader reader) {
        AttributesImpl attributes = new AttributesImpl();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String namespaceURI = reader.getAttributeNamespace(i);
            String localName = reader.getAttributeLocalName(i);
            String type = reader.getAttributeType(i);
            String value = reader.getAttributeValue(i);
            attributes.addAttribute(namespaceURI, localName, namespaceURI + '#' + localName, type, value);
        }
        return attributes;
    }

    static Map<String, String> parseNamespace(XMLStreamReader reader) {
        if (reader.getNamespaceCount() == 0) {
            return Collections.emptyMap();
        }

        Map<String, String> prefixToNamespace = new HashMap<String, String>(reader.getNamespaceCount());
        int namespaceCount = reader.getNamespaceCount();
        for (int i = 0; i < namespaceCount; i++) {
            String namespacePrefix = reader.getNamespacePrefix(i);
            String namespaceURI = reader.getNamespaceURI(namespacePrefix);
            prefixToNamespace.put(namespacePrefix, namespaceURI);
        }

        return prefixToNamespace;
    }
}
