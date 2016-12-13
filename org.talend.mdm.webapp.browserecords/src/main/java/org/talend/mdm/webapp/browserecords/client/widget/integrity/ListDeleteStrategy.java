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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult;

import com.extjs.gxt.ui.client.widget.MessageBox;

/**
 *
 */
// Implementation package visibility for class is intended: no need to see this class outside of package
class ListDeleteStrategy implements DeleteStrategy {

    private BrowseRecordsServiceAsync service;

    ListDeleteStrategy(BrowseRecordsServiceAsync service) {
        this.service = service;
    }

    /**
     * When delete is performed on a list, all items marked as {@link FKIntegrityResult#ALLOWED} are deleted. All other items
     * are not modified, but a message is displayed to indicate all records could not be deleted.
     *
     * @param items            A {@link Map} that link each item to be deleted to the {@link FKIntegrityResult} fk integrity policy to
     *                         apply.
     * @param action           A {@link DeleteAction} that performs the actual delete (a physical or a logical delete for instance).
     * @param postDeleteAction A {@link PostDeleteAction} that wraps all post-delete action to be performed once delete of
     */
    public void delete(Map<ItemBean, FKIntegrityResult> items, DeleteAction action, PostDeleteAction postDeleteAction) {
        Set<Map.Entry<ItemBean, FKIntegrityResult>> itemsToDelete = items.entrySet();
        boolean hasMetForbiddenDeletes = false;
        List<ItemBean> itemBeans = new ArrayList<ItemBean>();
        for (Map.Entry<ItemBean, FKIntegrityResult> currentItem : itemsToDelete) {
            FKIntegrityResult integrityCheckResult = currentItem.getValue();
            switch (integrityCheckResult) {
                case FORBIDDEN_OVERRIDE_ALLOWED:
                case FORBIDDEN:
                    hasMetForbiddenDeletes = true;
                    break;
                case ALLOWED:
                    itemBeans.add(currentItem.getKey());
                    break;
            }
        }
        action.delete(itemBeans, service, false, postDeleteAction);

        if (hasMetForbiddenDeletes) {
            MessageBox.info(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                    .fk_integrity_list_partial_delete(), null);
        }
    }
}
