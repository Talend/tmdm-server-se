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

package com.amalto.core.history;

import org.w3c.dom.Node;

/**
 *
 */
public interface DOMMutableDocument extends MutableDocument {

    /**
     * @return Returns the last node accessed on this document.
     * @see {@link com.amalto.core.history.accessor.Accessor#touch()}
     */
    Node getLastAccessedNode();

    /**
     * Sets a new accessed node in the document.
     *
     * @param lastAccessedNode A {@link Node} in the underlying DOM document.
     * @see {@link com.amalto.core.history.accessor.Accessor#touch()}
     */
    void setLastAccessedNode(Node lastAccessedNode);

}
