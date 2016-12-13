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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 *
 */
public class End implements State {
    public static final State INSTANCE = new End();

    private End() {
    }

    public void parse(StateContext context, XMLStreamReader reader) throws XMLStreamException {
        try {
            context.getWriter().writeEndDocument();
        } catch (Exception e) {
            throw new XMLStreamException(e);
        }
    }

    public boolean isFinal() {
        return Constants.FINAL_STATE;
    }

}
