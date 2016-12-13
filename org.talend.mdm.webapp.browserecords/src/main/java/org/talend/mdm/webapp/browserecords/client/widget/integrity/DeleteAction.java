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

import java.util.List;

import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;

/**
 * Wraps a delete action for a single item.
 *
 * <ul>
 *     <li>{@link #PHYSICAL}: for physical deletes.</li>
 *     <li>{@link LogicalDeleteAction}: for logical deletes ("move to trash" deletes).</li>
 * </ul>
 */
public interface DeleteAction {

    /**
     * The 'physical delete' operation. Please note that logical deletes requires additional parameters that makes this
     * type of delete not eligible for singleton.
     */
    DeleteAction PHYSICAL = new PhysicalDeleteAction();

    /**
     * Delete a single item. Implementations of this interface should call <code>postDeleteAction</code> actions after a
     * successful delete.
     * 
     * @param items These items to be deleted.
     * @param service The service to be used for communication with MDM server.
     * @param override <code>true</code> if user chose to override FK integrity (when applicable only! see
     * {@link org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult}), <code>false</code> otherwise.
     * @param postDeleteAction
     * @see BrowseRecordsServiceAsync#deleteItemBean(org.talend.mdm.webapp.browserecords.client.model.ItemBean, boolean,
     * String, com.google.gwt.user.client.rpc.AsyncCallback)
     * @see BrowseRecordsServiceAsync#logicalDeleteItem(org.talend.mdm.webapp.browserecords.client.model.ItemBean,
     * String, boolean, com.google.gwt.user.client.rpc.AsyncCallback)
     */
    void delete(List<ItemBean> items, BrowseRecordsServiceAsync service, boolean override, PostDeleteAction postDeleteAction);
}

