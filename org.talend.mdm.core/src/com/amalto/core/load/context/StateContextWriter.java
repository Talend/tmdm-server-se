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

import org.xml.sax.ContentHandler;

import javax.xml.stream.XMLStreamReader;

/**
 *
 */
public interface StateContextWriter {
    void writeEndDocument() throws Exception;

    void writeEndElement(XMLStreamReader reader) throws Exception;

    void writeCharacters(XMLStreamReader reader) throws Exception;

    void writeStartElement(XMLStreamReader reader) throws Exception;

    void flush(ContentHandler contentHandler) throws Exception;

    void writeStartElement(String elementLocalName) throws Exception;

    void writeCharacters(String characters) throws Exception;

    void writeEndElement(String elementLocalName) throws Exception;
}
