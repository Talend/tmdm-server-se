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

import com.amalto.core.load.LoadParserCallback;
import com.amalto.core.server.api.XmlServer;

import javax.xml.stream.XMLStreamReader;

/**
 *
 */
public interface StateContext {

    void parse(XMLStreamReader reader);

    String getPayLoadElementName();

    StateContextWriter getWriter();

    void setCurrent(com.amalto.core.load.State state);

    LoadParserCallback getCallback();

    boolean hasFinished();

    boolean hasFinishedPayload();

    com.amalto.core.load.Metadata getMetadata();

    void setWriter(StateContextWriter contextWriter);

    boolean isFlushDone();

    boolean isMetadataReady();

    void setFlushDone();

    void reset();

    void leaveElement();

    void enterElement(String elementLocalName);

    int getDepth();

    boolean isIdElement();

    String getCurrentIdElement();

    boolean skipElement();

    void close(XmlServer server);
}
