/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.load.payload;

import com.amalto.core.load.Constants;
import com.amalto.core.load.Metadata;
import com.amalto.core.load.context.StateContext;
import com.amalto.core.load.context.StateContextSAXWriter;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.*;

import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class FlushXMLReader implements XMLReader {
    private final XMLStreamReader reader;
    private final StateContext context;
    private final Map<String, Boolean> features = new HashMap<String, Boolean>();
    private final Map<String, Object> properties = new HashMap<String, Object>();

    private EntityResolver resolver;
    private DTDHandler handler;
    private ContentHandler contentHandler;
    private ErrorHandler errorHandler;

    public FlushXMLReader(XMLStreamReader reader, StateContext context) {
        this.reader = reader;
        this.context = context;
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return features.get(name) != null ? features.get(name) : false;
    }

    public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        features.put(name, value);
    }

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return properties.get(name);
    }

    public void setProperty(String name, Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        properties.put(name, value);
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.resolver = resolver;
    }

    public EntityResolver getEntityResolver() {
        return resolver;
    }

    public void setDTDHandler(DTDHandler handler) {
        this.handler = handler;
    }

    public DTDHandler getDTDHandler() {
        return handler;
    }

    public void setContentHandler(ContentHandler handler) {
        contentHandler = handler;
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    public void setErrorHandler(ErrorHandler handler) {
        errorHandler = handler;
    }

    public ErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void parse(InputSource input) throws IOException, SAXException {
        if (contentHandler == null) {
            throw new IllegalStateException("Content handler has not been set.");
        }

        contentHandler.startDocument();
        contentHandler.startElement(StringUtils.EMPTY, Constants.WRAPPER_ELEMENT, Constants.WRAPPER_ELEMENT, Constants.EMPTY_ATTRIBUTES);

        // Prolog
        Metadata metadata = context.getMetadata();
        writePrologElement(Constants.CONTAINER_ELEMENT, metadata.getContainer());
        writePrologElement(Constants.NAME_ELEMENT, metadata.getName());
        writePrologElement(Constants.DMN_ELEMENT, metadata.getDMN());
        writePrologElement(Constants.DMR_ELEMENT, metadata.getDMR());
        writePrologElement(Constants.SP_ELEMENT, metadata.getSP());
        writePrologElement(Constants.TIMESTAMP_ELEMENT, metadata.getVersion());
        writePrologElement(Constants.TASK_ID_ELEMENT, metadata.getTaskId());
        writePrologElement(Constants.ID_ELEMENT, metadata.getId());

        // Payload
        contentHandler.startElement(StringUtils.EMPTY, Constants.PAYLOAD_ELEMENT, Constants.PAYLOAD_ELEMENT, Constants.EMPTY_ATTRIBUTES);
        try {
            context.getWriter().flush(contentHandler);
            context.setWriter(new StateContextSAXWriter(contentHandler));
            while (!context.hasFinishedPayload()) {
                context.parse(reader);
            }
            if (context.getDepth() == 1) {
                context.parse(reader);
            }
        } catch (Exception e) {
            throw new SAXException(e);
        }

        contentHandler.endElement(StringUtils.EMPTY, Constants.PAYLOAD_ELEMENT, Constants.PAYLOAD_ELEMENT);

        // End prolog
        contentHandler.endElement(StringUtils.EMPTY, Constants.WRAPPER_ELEMENT, Constants.WRAPPER_ELEMENT);

        contentHandler.endDocument();
    }

    private void writePrologElement(String elementName, String... elementText) throws SAXException {
        for (String currentElementText : elementText) {
            contentHandler.startElement(StringUtils.EMPTY, elementName, elementName, Constants.EMPTY_ATTRIBUTES);
            {
                char[] elementTextChars = String.valueOf(currentElementText).toCharArray();
                contentHandler.characters(elementTextChars, 0, elementTextChars.length);
            }
            contentHandler.endElement(StringUtils.EMPTY, elementName, elementName);
        }
    }

    public void parse(String systemId) throws IOException, SAXException {
        throw new SAXException("Not supported");
    }
}
