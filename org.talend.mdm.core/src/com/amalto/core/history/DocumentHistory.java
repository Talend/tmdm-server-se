/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.history;

/**
 *
 */
public interface DocumentHistory {
    /**
     * Creates a {@link DocumentHistoryNavigator} that allow navigation through document's versions.
     *
     * @param dataClusterName A existing data cluster name where the document is.
     * @param dataModelName Data model of the document.
     * @param conceptName Concept name of the document.
     * @param id Id of the document (typed as array to support composite keys).
     * @return A {@link DocumentHistoryNavigator} that allow navigation through document's versions.
     */
    DocumentHistoryNavigator getHistory(String dataClusterName, String dataModelName, String conceptName, String[] id);
}
