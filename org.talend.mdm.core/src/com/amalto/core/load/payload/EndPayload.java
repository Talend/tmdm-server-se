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
import com.amalto.core.load.context.Utils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 */
public class EndPayload implements State {
    public static final State INSTANCE = new EndPayload();

    private EndPayload() {
    }

    public void parse(StateContext context, XMLStreamReader reader) throws XMLStreamException {
        try {
            context.getWriter().writeEndElement(reader);
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }

        if (!context.isFlushDone()) {
            if (!context.isMetadataReady()) {
                throw new IllegalStateException("End of XML payload for type '" + context.getPayLoadElementName() + "' detected but metadata isn't ready.");
            }
            Utils.doParserCallback(context, reader, context.getMetadata());
        }

        context.leaveElement();
        // After payload is over, go to next payload start and buffer next elements until id is reached.
        context.setCurrent(StartPayload.INSTANCE);
    }

    public boolean isFinal() {
        return Constants.NON_FINAL_STATE;
    }

}
