/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.publiclistener;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class StatusObserver {

    private static StatusObserver instance;

    private List<SavedListener> savedListeners;

    private List<UpdatedListener> updatedListeners;

    private List<DeletedListener> deletedListeners;

    private StatusObserver() {
    }

    public static StatusObserver getInstance() {
        if (instance == null) {
            instance = new StatusObserver();
        }
        return instance;
    }

    public void addSavedListener(SavedListener savedListener) {
        if (savedListeners == null) {
            savedListeners = new ArrayList<SavedListener>();
        }
        savedListeners.add(savedListener);
    }

    public void removeSavedListener(SavedListener savedListener) {
        if (savedListeners != null) {
            savedListeners.remove(savedListener);
        }
    }

    public void removeAllSavedListener() {
        if (savedListeners != null) {
            savedListeners.clear();
            savedListeners = null;
        }
    }
    
    public void notifySaved(ItemNodeModel itemBean, boolean isClose) {
        if (savedListeners != null){
            for (SavedListener savedListener : savedListeners){
                savedListener.onSaved(itemBean, isClose);
            }
        }
    }

    public void addUpdatedListener(UpdatedListener updatedListener) {
        if (updatedListeners == null) {
            updatedListeners = new ArrayList<UpdatedListener>();
        }
        updatedListeners.add(updatedListener);
    }

    public void removeUpdatedListener(UpdatedListener updatedListener) {
        if (updatedListeners != null) {
            updatedListeners.remove(updatedListener);
        }
    }

    public void removeAllUpdatedListener() {
        if (updatedListeners != null) {
            updatedListeners.clear();
            updatedListeners = null;
        }
    }

    public void notifyUpdated(ItemNodeModel itemBean, boolean isClose) {
        if (updatedListeners != null) {
            for (UpdatedListener updatedListener : updatedListeners) {
                updatedListener.onUpdated(itemBean, isClose);
            }
        }
    }

    public void addDeletedListener(DeletedListener deletedListener) {
        if (deletedListeners == null) {
            deletedListeners = new ArrayList<DeletedListener>();
        }
        deletedListeners.add(deletedListener);
    }

    public void removeDeletedListener(DeletedListener deletedListener) {
        if (deletedListeners != null) {
            deletedListeners.remove(deletedListener);
        }
    }

    public void removeAllDeletedListener() {
        if (deletedListeners != null) {
            deletedListeners.clear();
            deletedListeners = null;
        }
    }

    public void notifyDeleted(ItemBean itemBean, String path, boolean override) {
        if (deletedListeners != null) {
            for (DeletedListener deletedListener : deletedListeners) {
                deletedListener.onDeleted(itemBean, path, override);
            }
        }
    }
}
