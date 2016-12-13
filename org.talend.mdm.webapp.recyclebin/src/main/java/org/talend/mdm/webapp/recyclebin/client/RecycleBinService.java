/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.recyclebin.client;

import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.recyclebin.shared.ItemsTrashItem;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("RecycleBinService")
public interface RecycleBinService extends RemoteService {

    ItemBasePageLoadResult<ItemsTrashItem> getTrashItems(String regex, BasePagingLoadConfigImpl load) throws ServiceException;

    String removeDroppedItem(String clusterName, String modelName, String conceptName, String ids, String language)
            throws ServiceException;

    void recoverDroppedItem(String clusterName, String modelName, String conceptName, String ids) throws ServiceException;

    boolean isEntityPhysicalDeletable(String conceptName) throws ServiceException;

    boolean checkConflict(String clusterName, String conceptName, String id) throws ServiceException;
}
