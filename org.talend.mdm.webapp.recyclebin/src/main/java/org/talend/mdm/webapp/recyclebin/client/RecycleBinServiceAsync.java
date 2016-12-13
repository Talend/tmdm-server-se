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

import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.recyclebin.shared.ItemsTrashItem;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>RecycleBinService</code>.
 */
public interface RecycleBinServiceAsync {

    void getTrashItems(String regex, BasePagingLoadConfigImpl load, AsyncCallback<ItemBasePageLoadResult<ItemsTrashItem>> callback);

    void removeDroppedItem(String clusterName, String modelName, String conceptName, String ids, String language,
            AsyncCallback<String> callback);

    void recoverDroppedItem(String clusterName, String modelName, String conceptName, String ids, AsyncCallback<Void> callback);

    void isEntityPhysicalDeletable(String conceptName, AsyncCallback<Boolean> callback);

    void checkConflict(String clusterName, String conceptName, String id, AsyncCallback<Boolean> callback);
    
}
