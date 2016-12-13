/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client;

import java.util.List;

import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalParameters;
import org.talend.mdm.webapp.journal.shared.JournalSearchCriteria;
import org.talend.mdm.webapp.journal.shared.JournalTreeModel;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface JournalServiceAsync {

    void getJournalList(JournalSearchCriteria criteria, BasePagingLoadConfigImpl load,
            AsyncCallback<ItemBasePageLoadResult<JournalGridModel>> callback);    

    void getDetailTreeModel(String ids, AsyncCallback<JournalTreeModel> callback);

    void getComparisionTree(JournalParameters parameter, String language, AsyncCallback<JournalTreeModel> callback);

    void isEnterpriseVersion(AsyncCallback<Boolean> callback);

    void restoreRecord(JournalParameters parameter, String language, AsyncCallback<Void> callback);

    void checkDCAndDM(String dataContainer, String dataModel, AsyncCallback<Boolean> callback);

    void getReportString(int start, int limit, String sort, String field, String language, String dataModel, String entity,
            String key, String source, String operationType, String startDate, String endDate, boolean isStrict,
            AsyncCallback<String> callback);

    void isAdmin(AsyncCallback<Boolean> callback);
    
    void isJournalHistoryExist(JournalParameters parameter,AsyncCallback<Boolean> callback);
    
    void checkConflict(String itemPk, String conceptName, String id,AsyncCallback<Boolean> callback);

    void getDataModels(AsyncCallback<List<ItemBaseModel>> callback);
}