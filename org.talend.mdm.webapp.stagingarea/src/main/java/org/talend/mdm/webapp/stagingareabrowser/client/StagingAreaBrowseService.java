/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingareabrowser.client;

import java.util.List;

import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.stagingareabrowser.client.model.ResultItem;
import org.talend.mdm.webapp.stagingareabrowser.client.model.SearchModel;

import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.PagingLoadResult;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("StagingAreaBrowseService")
public interface StagingAreaBrowseService extends RemoteService {

    public List<BaseModel> getConcepts(String language) throws ServiceException;

    public PagingLoadResult<ResultItem> searchStaging(SearchModel searchModel) throws ServiceException;
}
