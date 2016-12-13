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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;

/**
 *
 */
// Implementation package visibility for class is intended: no need to see this class outside of package
class SingletonDeleteStrategy implements DeleteStrategy {

    private BrowseRecordsServiceAsync service;

    /**
     * @param service Need a {@link BrowseRecordsServiceAsync} instance to display 'Relations' window if needed.
     */
    SingletonDeleteStrategy(BrowseRecordsServiceAsync service) {
        this.service = service;
    }

    /**
     * Please note that this implementation of {@link DeleteStrategy} is expected to throw an
     * {@link IllegalArgumentException} if more than one item to delete is passed in <code>items</code>.
     * 
     * @param items A {@link Map} that link each item to be deleted to the {@link FKIntegrityResult} fk integrity policy
     * to apply.
     * @param action A {@link DeleteAction} that performs the actual delete (a physical or a logical delete for
     * instance).
     * @param postDeleteAction A {@link PostDeleteAction} that wraps all post-delete action to be performed once delete
     * of items is done.
     * @throws IllegalArgumentException if <code>items</code> contains more than one item.
     */
    public void delete(final Map<ItemBean, FKIntegrityResult> items, final DeleteAction action,
            final PostDeleteAction postDeleteAction) {
        Set<Map.Entry<ItemBean, FKIntegrityResult>> entries = items.entrySet();
        Iterator<Map.Entry<ItemBean, FKIntegrityResult>> iterator = entries.iterator();

        Map.Entry<ItemBean, FKIntegrityResult> entry = iterator.next();
        if (iterator.hasNext()) {
            throw new IllegalArgumentException("Expected only one argument for delete.");
        }

        FKIntegrityResult result = entry.getValue();
        final ItemBean item = entry.getKey();

        switch (result) {
        case FORBIDDEN_OVERRIDE_ALLOWED:
            MessageBox.confirm(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                    .fk_integrity_fail_override(), new Listener<MessageBoxEvent>() {

                public void handleEvent(MessageBoxEvent be) {
                    if (Dialog.YES.equals(be.getButtonClicked().getItemId())) {
                        action.delete(Arrays.asList(item), service, true, postDeleteAction);
                    }
                }
            });
            break;
        case FORBIDDEN:
            MessageBox.confirm(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                    .fk_integrity_fail_open_relations(), new Listener<MessageBoxEvent>() {

                public void handleEvent(MessageBoxEvent be) {
                    if (Dialog.YES.equals(be.getButtonClicked().getItemId())) {
                        // Open "relation" window/tab
                        service.getLineageEntity(item.getConcept(), new SessionAwareAsyncCallback<List<String>>() {

                            public void onSuccess(List<String> list) {
                                StringBuilder entityStr = new StringBuilder();
                                if (list != null) {
                                    for (String str : list)
                                        entityStr.append(str).append(","); //$NON-NLS-1$
                                    String arrStr = entityStr.toString().substring(0, entityStr.length() - 1);
                                    String ids = item.getIds();
                                    if (ids == null || ids.trim().length() == 0)
                                        ids = ""; //$NON-NLS-1$
                                    initSearchEntityPanel(arrStr, ids, item.getConcept());
                                }
                            }
                        });

                    }
                }
            });
            // No need to call postDeleteAction.doAction() here (no delete was done).
            break;
        case ALLOWED:
            action.delete(Arrays.asList(item), service, false, postDeleteAction);
            break;
        }

    }

    private native boolean initSearchEntityPanel(String arrStr, String ids, String dataObject)/*-{
        var lineageEntities = arrStr.split(",");
        $wnd.amalto.itemsbrowser.ItemsBrowser.lineageItem(lineageEntities, ids, dataObject);
        return true;
    }-*/;
}
