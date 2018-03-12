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

package com.amalto.core.history.action;

import com.amalto.core.history.Action;

import java.util.List;

/**
 *
 */
public interface ActionFactory {

    /**
     * Create all the actions that were performed by users on a single document.
     *
     * @param dataClusterName The data cluster where the document is located.
     * @param dataModelName   The data model name for the document.
     * @param conceptName     The document concept name.
     * @param id              The id of the document.
     * @return The list of action(s) performed on the document. These actions can then be used to replay actions
     *         performed on the document.
     */
    List<Action> createActions(String dataClusterName, String dataModelName, String conceptName, String[] id);

}
