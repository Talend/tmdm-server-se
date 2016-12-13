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
import com.amalto.core.load.State;
import com.amalto.core.load.context.StateContext;
import com.amalto.core.load.xml.StartElement;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

/**
 *
 */
public class StartPayload implements State {
    public static final State INSTANCE = new StartPayload();

    private StartPayload() {
    }

    public void parse(StateContext context, XMLStreamReader reader) throws XMLStreamException {
        if (!reader.hasNext()) {
            context.setCurrent(End.INSTANCE);
        } else if (reader.next() == XMLEvent.START_ELEMENT) {
            if (context.getPayLoadElementName().equals(reader.getName().getLocalPart())) {
                // Reset the context since a new pay load is being read
                context.setCurrent(StartElement.INSTANCE);
            }
        }
    }

    public boolean isFinal() {
        return Constants.NON_FINAL_STATE;
    }

}
