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

import java.util.List;

import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.recyclebin.shared.ItemsTrashItem;

import com.google.gwt.user.client.rpc.AsyncCallback;

@SuppressWarnings("nls")
public class RecycleBinServiceAsyncMock implements RecycleBinServiceAsync {

    @Override
    public void checkConflict(String itemPk, String conceptName, String id, AsyncCallback<Boolean> callback) {
        callback.onSuccess(true);
    }

    @Override
    public void getTrashItems(String regex, BasePagingLoadConfigImpl load,
            AsyncCallback<ItemBasePageLoadResult<ItemsTrashItem>> callback) {
        List<ItemsTrashItem> items = FakeData.getItems();
        ItemBasePageLoadResult<ItemsTrashItem> result = new ItemBasePageLoadResult<ItemsTrashItem>(items, 0, items.size());
        callback.onSuccess(result);
    }

    @Override
    public void isEntityPhysicalDeletable(String conceptName, AsyncCallback<Boolean> callback) {
        callback.onSuccess(true);
    }

    @Override
    public void recoverDroppedItem(String clusterName, String modelName, String conceptName, String ids,
            AsyncCallback<Void> callback) {
        // do nothing
    }

    @Override
    public void removeDroppedItem(String clusterName, String modelName, String conceptName, String ids, String language,
            AsyncCallback<String> callback) {
        FakeData.remvoeItem(clusterName, conceptName, ids);
        callback.onSuccess("[EN:del succ][FR:del succ]");
    }

}
