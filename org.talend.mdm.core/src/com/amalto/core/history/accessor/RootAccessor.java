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

package com.amalto.core.history.accessor;

import com.amalto.core.history.MutableDocument;
import org.apache.commons.lang.NotImplementedException;
import org.w3c.dom.Node;

/**
 *
 */
class RootAccessor implements DOMAccessor {

    private final MutableDocument document;

    RootAccessor(MutableDocument document) {
        this.document = document;
    }

    public void set(String value) {
        throw new IllegalStateException("Cannot set value");
    }

    public String get() {
        throw new IllegalStateException("Cannot get value");
    }

    public void touch() {
        // Nothing to do.
    }

    public Node getNode() {
        return document.asDOM();
    }

    public void create() {
        // Nothing to do (won't recreate the document).
    }

    public void insert() {
        // Nothing to do (won't insert a new document inside an existing one).
    }

    public void createAndSet(String value) {
        // Nothing to do (won't recreate the document).
    }

    public void delete() {
        // Nothing to do (won't delete the document).
    }

    public boolean exist() {
        return getNode() != null;
    }

    public void markModified(Marker marker) {
        throw new IllegalStateException("Cannot mark this as modified. No value to be set in this accessor");
    }

    public void markUnmodified() {
        throw new IllegalStateException("Cannot mark this as unmodified. No value to be set in this accessor");
    }

    public int size() {
        if (!exist()) {
            return 0;
        }
        return getNode().getChildNodes().getLength();
    }

    public String getActualType() {
        throw new NotImplementedException("Override document type at root element.");
    }

    @Override
    public int compareTo(Accessor accessor) {
        if (exist() != accessor.exist()) {
            return -1;
        }
        if (exist() && (accessor instanceof RootAccessor)) {
            return getNode().equals(((RootAccessor) accessor).getNode()) ? 0 : -1;
        }
        return -1;
    }
}
