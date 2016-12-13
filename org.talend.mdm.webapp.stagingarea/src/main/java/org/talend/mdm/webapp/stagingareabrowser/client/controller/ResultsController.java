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

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.stagingareabrowser.client.StagingareaBrowse;
import org.talend.mdm.webapp.stagingareabrowser.client.model.ResultItem;
import org.talend.mdm.webapp.stagingareabrowser.client.model.SearchModel;
import org.talend.mdm.webapp.stagingareabrowser.client.view.ResultsView;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelKeyProvider;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ResultsController extends AbstractController {

    private static RpcProxy<PagingLoadResult<ResultItem>> proxy;

    private static BasePagingLoader<PagingLoadResult<ResultItem>> loader;

    private static ListStore<ResultItem> store;

    static {
        proxy = new RpcProxy<PagingLoadResult<ResultItem>>() {

            @Override
            protected void load(Object loadConfig, final AsyncCallback<PagingLoadResult<ResultItem>> callback) {
                final PagingLoadConfig config = (PagingLoadConfig) loadConfig;
                searchModel.setOffset(config.getOffset());
                searchModel.setLimit(config.getLimit());
                searchModel.setSortField(config.getSortField());
                String sortDir = null;
                if (config.getSortDir() == SortDir.ASC) {
                    sortDir = "ascending"; //$NON-NLS-1$
                }
                if (config.getSortDir() == SortDir.DESC) {
                    sortDir = "descending"; //$NON-NLS-1$
                }
                searchModel.setSortDir(sortDir);
                StagingareaBrowse.service.searchStaging(searchModel,
                        new SessionAwareAsyncCallback<PagingLoadResult<ResultItem>>() {

                            public void onSuccess(PagingLoadResult<ResultItem> result) {
                                callback.onSuccess(result);
                            }

                            @Override
                            public void doOnFailure(Throwable caught) {
                                alertStagingError(caught);
                            }
                        });
            }
        };
        loader = new BasePagingLoader<PagingLoadResult<ResultItem>>(proxy);
        store = new ListStore<ResultItem>(loader);
        loader.setRemoteSort(true);
    }

    private static SearchModel searchModel;

    public ResultsController(ResultsView view) {
        setBindingView(view);
    }

    public static BasePagingLoader<PagingLoadResult<ResultItem>> getLoader() {
        return loader;
    }

    public static ListStore<ResultItem> getClearStore() {
        store = new ListStore<ResultItem>(loader);
        store.setKeyProvider(new ModelKeyProvider<ResultItem>() {

            public String getKey(ResultItem model) {
                return model.getIds();
            }
        });
        return store;
    }

    public void searchResult(SearchModel searchModel) {
        ResultsController.searchModel = searchModel;
        loader.load(0, ResultsView.PAGE_SIZE);
    }
}
