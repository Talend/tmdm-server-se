/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;

public class MockStorageAdmin extends StorageAdminImpl {

    private Set<Storage> storages = new HashSet<Storage>();

    @Override
    public Storage get(String storageName, StorageType type) {
        storageName = storageName.replace("#STAGING", StringUtils.EMPTY);
        for (Storage s : storages) {
            if (s.getName().equals(storageName) && s.getType() == type) {
                return s;
            }
        }
        return super.get(storageName, type);
    }

    public void register(Storage storage) {
        storages.add(storage);
    }

    public void forget(Storage storage) {
        storages.remove(storage);
    }

    @Override
    public boolean exist(String storageName, StorageType storageType) {
        return true;
    }

}
