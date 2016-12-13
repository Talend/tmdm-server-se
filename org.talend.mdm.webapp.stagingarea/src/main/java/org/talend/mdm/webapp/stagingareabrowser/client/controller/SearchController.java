/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingareabrowser.client.controller;

import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.stagingareabrowser.client.StagingareaBrowse;
import org.talend.mdm.webapp.stagingareabrowser.client.view.SearchView;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.store.ListStore;

public class SearchController extends AbstractController {

    private static ListStore<BaseModel> store;

    private SearchView view;

    public SearchController(SearchView view) {
        setBindingView(view);
        this.view = (SearchView) bindingView;
    }

    static {
        store = new ListStore<BaseModel>();
    }

    public static void loadConcepts(final SessionAwareAsyncCallback<List<BaseModel>> conceptCallback) {
        StagingareaBrowse.service.getConcepts(UrlUtil.getLanguage(), new SessionAwareAsyncCallback<List<BaseModel>>() {

            public void onSuccess(List<BaseModel> result) {
                store.removeAll();
                store.add(result);
                if (conceptCallback != null) {
                    conceptCallback.onSuccess(result);
                }
            }

            @Override
            public void doOnFailure(Throwable caught) {
                alertStagingError(caught);
            }
        });
    }

    public static ListStore<BaseModel> getStore() {
        return store;
    }

    public void defaultDoSearch(int defaultState) {
        view.defaultDoSearch(defaultState);
    }
}
