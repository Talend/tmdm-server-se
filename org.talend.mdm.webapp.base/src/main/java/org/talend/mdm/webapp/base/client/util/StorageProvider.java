/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.util;

import com.extjs.gxt.ui.client.state.Provider;
import com.google.gwt.storage.client.Storage;

public class StorageProvider extends Provider {

    private Storage storage;

    public static StorageProvider newInstanceIfSupported() {
        Storage storage = Storage.getLocalStorageIfSupported();
        if (storage != null) {
            return new StorageProvider(storage);
        }
        return null;
    }

    private StorageProvider(Storage storage) {
        this.storage = storage;
    }

    @Override
    protected void clearKey(String name) {
        storage.removeItem(name);
    }

    @Override
    protected String getValue(String name) {
        return storage.getItem(name);
    }

    @Override
    protected void setValue(String name, String value) {
        storage.setItem(name, value);
    }

}
