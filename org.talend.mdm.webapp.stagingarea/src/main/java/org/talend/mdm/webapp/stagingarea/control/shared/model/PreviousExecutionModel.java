/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.shared.model;

import com.extjs.gxt.ui.client.data.*;
import com.extjs.gxt.ui.client.store.ListStore;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.stagingarea.control.shared.controller.rest.RestDataProxy;
import org.talend.mdm.webapp.stagingarea.control.shared.controller.rest.StagingRestServiceHandler;

import java.util.Date;
import java.util.List;

public class PreviousExecutionModel extends AbstractHasModelEvents {

    private final ListStore<StagingAreaExecutionModel> store;

    private Date beforeDate;

    public PreviousExecutionModel() {
        DataProxy<PagingLoadResult<StagingAreaExecutionModel>> proxy = new RestDataProxy<PagingLoadResult<StagingAreaExecutionModel>>() {

            @Override
            protected void load(Object loadConfig, final AsyncCallback<PagingLoadResult<StagingAreaExecutionModel>> callback) {
                final String dataContainer = UserContextUtil.getDataContainer();
                final PagingLoadConfig config = (PagingLoadConfig) loadConfig;
                StagingAreaExecutionModel criteria = new StagingAreaExecutionModel();
                criteria.setStartDate(getBeforeDate());
                StagingRestServiceHandler.get().countStagingAreaExecutions(dataContainer, criteria,
                        new SessionAwareAsyncCallback<Integer>() {

                            @Override
                            public void onSuccess(final Integer total) {
                                StagingRestServiceHandler.get().getStagingAreaExecutionsWithPaging(dataContainer,
                                        config.getOffset(), config.getLimit(), getBeforeDate(),
                                        new SessionAwareAsyncCallback<List<StagingAreaExecutionModel>>() {

                                            @Override
                                            public void onSuccess(List<StagingAreaExecutionModel> result) {
                                                BasePagingLoadResult<StagingAreaExecutionModel> pagingResult = new BasePagingLoadResult<StagingAreaExecutionModel>(
                                                        result, config.getOffset(), total);
                                                callback.onSuccess(pagingResult);
                                            }
                                        });
                            }
                        });
            }
        };
        BasePagingLoader loader = new BasePagingLoader(proxy);
        this.store = new ListStore<StagingAreaExecutionModel>(loader);
        this.store.setKeyProvider(new ModelKeyProvider<StagingAreaExecutionModel>() {

            @Override
            public String getKey(StagingAreaExecutionModel model) {
                return model.getId();
            }
        });
    }

    public Date getBeforeDate() {
        return beforeDate;
    }

    public void setBeforeDate(Date beforeDate) {
        this.beforeDate = beforeDate;
        store.getLoader().load();
    }

    public ListStore<StagingAreaExecutionModel> getStore() {
        return store;
    }
}
