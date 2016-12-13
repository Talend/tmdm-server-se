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
import com.amalto.core.load.context.Utils;
import org.apache.commons.lang.StringUtils;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

/**
 *
 */
public class SetId extends Characters {
    public static final State INSTANCE = new SetId();

    public void parse(StateContext context, XMLStreamReader reader) throws XMLStreamException {
        int next = reader.next();
        if (next == XMLEvent.END_ELEMENT) {
            // Means we started an ID but got no text in it, so set it to empty string
            context.getMetadata().setId(context.getCurrentIdElement(), StringUtils.EMPTY);
            // ... and move to EndElement state
            context.setCurrent(EndElement.INSTANCE);
            return;
        } else if (next != XMLEvent.CHARACTERS) {
            // Everything else (not END_ELEMENT and not CHARACTERS is error).
            throw new IllegalStateException("Expected characters but got XML event id #" + next);
        }

        // We're parsing characters so call super.parse(context, reader)...
        super.parse(context, reader);
        // ...and we're also setting id for metadata
        context.getMetadata().setId(context.getCurrentIdElement(), reader.getText());
        // If we're ready, flush document
        if (doFlush(context)) {
            Utils.doParserCallback(context, reader, context.getMetadata());
        }
    }

    private static boolean doFlush(StateContext context) {
        return !context.isFlushDone() && context.isMetadataReady();
    }

    public boolean isFinal() {
        return Constants.NON_FINAL_STATE;
    }

}
