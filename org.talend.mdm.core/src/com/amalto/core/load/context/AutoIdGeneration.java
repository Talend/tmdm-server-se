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

package com.amalto.core.load.context;

import com.amalto.core.load.State;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 */
class AutoIdGeneration implements State {
    private final State previousState;

    private final String[] idPaths;

    public AutoIdGeneration(State previousState, String[] idPaths) {
        this.previousState = previousState;
        this.idPaths = idPaths;
    }

    public void parse(StateContext context, XMLStreamReader reader) throws XMLStreamException {
        try {
            String[] id = context.getMetadata().getId();
            int index = 0;
            assert (id.length == idPaths.length);
            for (String idPath : idPaths) {
                context.getWriter().writeStartElement(idPath);
                context.getWriter().writeCharacters(id[index++]);
                context.getWriter().writeEndElement(idPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate automatic id");
        }

        context.setCurrent(previousState);

        if (!context.isFlushDone()) {
            Utils.doParserCallback(context, reader, context.getMetadata());
        }
    }

    public boolean isFinal() {
        return false;
    }
}
