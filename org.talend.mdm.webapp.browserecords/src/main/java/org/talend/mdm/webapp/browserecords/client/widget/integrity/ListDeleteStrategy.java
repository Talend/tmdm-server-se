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
        action.delete(items, service, false, postDeleteAction);
    }
}
