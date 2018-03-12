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

/**
 * A {@link DocumentTransformer} implementation to mark document with modification marks.
 * @see Action#addModificationMark(MutableDocument)
 */
public class ModificationMarker implements DocumentTransformer {
    private final Action modificationMarkersAction;

    public ModificationMarker(Action modificationMarkersAction) {
        this.modificationMarkersAction = modificationMarkersAction;
    }

    public Document transform(MutableDocument document) {
        return modificationMarkersAction.addModificationMark(document);
    }
}
