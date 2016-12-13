/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.load.xml;

import com.amalto.core.load.Constants;
import com.amalto.core.load.State;
import com.amalto.core.load.context.StateContext;
import com.amalto.core.load.payload.End;
import com.amalto.core.load.payload.EndPayload;
import org.apache.commons.lang.NotImplementedException;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

/**
 *
 */
public class Selector implements State {
    public static final State INSTANCE = new Selector();

    private Selector() {
    }

    public void parse(StateContext context, XMLStreamReader reader) throws XMLStreamException {
        int next = reader.next();

        switch (next) {
            case XMLEvent.START_ELEMENT:
                context.setCurrent(StartElement.INSTANCE);
                break;
            case XMLEvent.END_ELEMENT:
                /*
                 * Switch to EndPayload only if we're at the level of begin payload.
                 * If the document has the following structure:
                 *
                 * <root>
                 *    <root>...</root>
                 * </root>
                 *
                 * We want to end the payload for the top level <root> element, hence the depth == 1 condition.
                 */
                if (context.getDepth() == 1 && context.getPayLoadElementName().equals(reader.getName().getLocalPart())) {
                    context.setCurrent(EndPayload.INSTANCE);
                } else {
                    context.setCurrent(EndElement.INSTANCE);
                }
                break;
            case XMLEvent.CHARACTERS:
                context.setCurrent(Characters.INSTANCE);
                break;
            case XMLEvent.END_DOCUMENT:
                context.setCurrent(End.INSTANCE);
                break;
            case XMLEvent.COMMENT:
                // Ignore comment storage in MDM
                break;
            default:
                // Nothing to do?
                throw new NotImplementedException("Support for event id #" + next);
        }
    }

    public boolean isFinal() {
        return Constants.NON_FINAL_STATE;
    }
}
