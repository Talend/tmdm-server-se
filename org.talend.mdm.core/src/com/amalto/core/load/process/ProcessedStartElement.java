/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.load.process;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 *
 */
public class ProcessedStartElement implements PayloadProcessedElement {
    private final Attributes attributes;
    private final String uri;
    private final String localName;
    private final String qName;

    public ProcessedStartElement(String uri, String localName, String qName, Attributes attributes) {
        this.attributes = attributes;
        this.uri = uri == null ? StringUtils.EMPTY : uri;  // SAX consumer (such as Qizx) doesn't like null NS.
        this.localName = localName;
        this.qName = qName;
    }

    public void flush(ContentHandler contentHandler) throws SAXException {
        contentHandler.startElement(uri, localName, qName, attributes);
    }
}
