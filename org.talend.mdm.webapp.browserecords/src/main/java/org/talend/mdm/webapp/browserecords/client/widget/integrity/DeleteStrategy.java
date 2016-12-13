/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.integrity;

import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult;

import java.util.Map;

/**
 * A strategy that handles deletion of items in the items browser. Strategy is chosen by {@link DeleteCallback}.
 *
 * @see DeleteCallback#onSuccess(java.util.Map)
 */
public interface DeleteStrategy {
    /**
     * Deletes all <code>items</code> using the <code>action</code> to perform the deletes. Once delete is done, strategy
     * should call <code>postDeleteAction</code> when appropriate.
     *
     * @param items            A {@link Map} that link each item to be deleted to the {@link FKIntegrityResult} fk integrity policy to
     *                         apply.
     * @param action           A {@link DeleteAction} that performs the actual delete (a physical or a logical delete for instance).
     * @param postDeleteAction A {@link PostDeleteAction} that wraps all post-delete action to be performed once delete of
     *                         items is done.
     */
    void delete(Map<ItemBean, FKIntegrityResult> items, DeleteAction action, PostDeleteAction postDeleteAction);
}
