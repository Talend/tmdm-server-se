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

package com.amalto.core.load;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Implement this interface to receive callback from {@link LoadParser} during parsing.
 *
 * @see LoadParser#parse(java.io.InputStream, com.amalto.core.load.LoadParser.Configuration, LoadParserCallback)
 * @see LoadParser#parse(java.io.InputStream, com.amalto.core.load.LoadParser.Configuration, int, LoadParserCallback)
 */
public interface LoadParserCallback {
    /**
     * <p>
     * Called by {@link LoadParser} when a document is ready to be persisted in MDM. Parser provides a SAX reader
     * to the document and a input source.
     * </p>
     * <p>
     * <b>Important note :</b> Before calling {@link XMLReader#parse(org.xml.sax.InputSource)},
     * please ensure {@link XMLReader#setContentHandler(org.xml.sax.ContentHandler)} has been called.
     * </p>
     *
     * @param docReader A SAX XML reader.
     * @param input     An SAX input source.
     */
    void flushDocument(XMLReader docReader, InputSource input);
}
