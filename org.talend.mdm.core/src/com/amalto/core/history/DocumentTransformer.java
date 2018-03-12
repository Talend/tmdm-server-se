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
 * Implement this interface to modify a {@link Document} returned by a {@link DocumentHistoryNavigator}.
 * @see Document#transform(DocumentTransformer) 
 */
public interface DocumentTransformer {
    /**
     * Transforms a {@link MutableDocument} instance.
     * @param document The {@link MutableDocument} instance this transformer might interact with.
     * @return A {@link Document} that represents a MDM record at a given time.
     * @see com.amalto.core.history.MutableDocument#asDOM()
     */
    Document transform(MutableDocument document);
}
