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

import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A generic {@link AsyncCallback} implementation to handle all deletes in items browser and all specific actions to
 * performed regarding deletes and FK integrity checks.
 */
public class DeleteCallback extends SessionAwareAsyncCallback<Map<ItemBean, FKIntegrityResult>> {

    private final DeleteAction action;

    private final PostDeleteAction postDeleteAction;

    private final BrowseRecordsServiceAsync service;

    /**
     * @param action The {@link DeleteAction} to perform.
     * @param postDeleteAction The {@link PostDeleteAction} to perform.
     * @param service A {@link BrowseRecordsServiceAsync} instance to be used for communication with MDM server.
     */
    public DeleteCallback(DeleteAction action, PostDeleteAction postDeleteAction, BrowseRecordsServiceAsync service) {
        this.action = action;
        this.postDeleteAction = postDeleteAction;
        this.service = service;
    }

    /**
     * @param result A {@link Map} of items to be deleted with information on what FK integrity policy should be
     * applied.
     */
    @Override
    public void onSuccess(Map<ItemBean, FKIntegrityResult> result) {
        if (!result.isEmpty()) { // If empty, do nothing
            DeleteStrategy strategy;
            if (result.size() > 1) {
                strategy = new ListDeleteStrategy(service);
            } else {
                strategy = new SingletonDeleteStrategy(service);
            }

            strategy.delete(result, action, postDeleteAction);
        }
    }

}
