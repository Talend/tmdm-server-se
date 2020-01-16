/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.load.xml;

import com.amalto.core.load.State;
import com.amalto.core.load.context.StateContext;
import com.amalto.core.load.context.Utils;
import com.amalto.core.save.generator.AutoIdGenerator;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.StringTokenizer;

@SuppressWarnings("nls")
public class AutoFieldGeneration implements State {
    private final State previousState;

    private final String[] fieldPaths;

    public AutoFieldGeneration(State previousState, String[] fieldPaths) {
        this.previousState = previousState;
        this.fieldPaths = fieldPaths;
    }

    @Override
    public void parse(StateContext context, XMLStreamReader reader) throws XMLStreamException {
        try {
            AutoIdGenerator[] autoGenerator = context.getAutoFieldGenerator();
            int i = 0;
            /*
            fielldPaths store the all need to generate value the field,  if doesn't existed, it means don't generate the value.
            if the field path is 'Course/Score', it should be first write '<Course>', and next is '<Score>',
            but it inverse for the end element, first is '<Score>', first is '<Course>'
             */
            for (String idPath : fieldPaths) {
                if (idPath == null) {
                    i++;
                    continue;
                }
                if (idPath.contains("/")) {
                    StringTokenizer tokenizer = new StringTokenizer(idPath, "/");
                    while (tokenizer.hasMoreTokens()) {
                        String pathElement = tokenizer.nextToken();
                        context.getWriter().writeStartElement(pathElement);
                    }
                } else {
                    context.getWriter().writeStartElement(idPath);
                }

                context.getWriter().writeCharacters(autoGenerator[i++]
                        .generateId(context.getMetadata().getDataClusterName(), context.getMetadata().getName(), idPath.replaceAll("/", ".")));

                if (idPath.contains("/")) {
                    String[] idPathArray = idPath.split("/");
                    for (int j = idPathArray.length - 1; j >= 0; j--) {
                        context.getWriter().writeEndElement(idPathArray[j]);
                    }
                } else {
                    context.getWriter().writeEndElement(idPath);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate automatic id", e);
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
