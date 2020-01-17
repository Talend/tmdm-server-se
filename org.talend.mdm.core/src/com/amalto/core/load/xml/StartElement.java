/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import com.google.common.base.Joiner;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.util.Iterator;

/**
 *
 */
public class StartElement implements State {
    public static final State INSTANCE = new StartElement();

    private StartElement() {
    }

    public void parse(StateContext context, XMLStreamReader reader) throws XMLStreamException {
        String elementLocalName = reader.getName().getLocalPart();
        context.enterElement(elementLocalName);
        context.getReadElementPath().push(elementLocalName);
        /*
        add the field path into the stack from xml content.
        eg:
            <Student>
              <Id>2</Id>
              <Name>Mike</Name>
              <Age>25</Age>
              <Account>8bf3d65e-8350-4a49-a651-c4cb5fdaf91f</Account>
              <Site>6</Site>
              <Course>
                <Id>Chinese</Id>
                <Score>6</Score>
                <Like>1efa8311-3be5-4c42-a015-216780c46ab4</Like>
              </Course>
            </Student>
            stack result like below:
                Id
                Name
                Age
                Account
                Site
                Course/Id
                Course/Score
                Course/Like
         */
        Iterator<String> iterator = context.getReadElementPath().iterator();
        if (iterator.hasNext()) {
            iterator.next();
        }
        String name = Joiner.on("/").join(iterator);
        context.getNormalFieldInXML().add(name);

        if (context.skipElement()) {
            while (reader.next() != XMLEvent.END_ELEMENT) {
                // Skip element's content until we meet end element
            }
            context.leaveElement();
            context.setCurrent(Selector.INSTANCE);
        } else {
            try {
                context.getWriter().writeStartElement(reader);
            } catch (Exception e) {
                throw new XMLStreamException(e);
            }
            if (!context.getPayLoadElementName().equals(elementLocalName) && context.isIdElement()) {
                context.setCurrent(SetId.INSTANCE);
            } else {
                context.setCurrent(Selector.INSTANCE);
            }
        }
    }

    public boolean isFinal() {
        return Constants.NON_FINAL_STATE;
    }

}
