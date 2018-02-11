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

import com.amalto.core.load.context.StateContext;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Represents a internal state of the {@link LoadParser}.
 */
public interface State {
    /**
     * Parses the <code>reader</code>
     *
     * @param context The state context that stores current context as well as cross state information.
     * @param reader  A STAX XML reader.
     * @throws XMLStreamException Thrown by STAX XML reader.
     */
    void parse(StateContext context, XMLStreamReader reader) throws XMLStreamException;

    /**
     * Return true if this state is final. A final state indicates to {@link LoadParser} that its job is done.
     *
     * @return true if there is no possible new state after this one, false otherwise.
     */
    boolean isFinal();
}
