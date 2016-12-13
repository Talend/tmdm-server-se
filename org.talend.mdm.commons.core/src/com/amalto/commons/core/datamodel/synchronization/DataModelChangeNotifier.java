/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.commons.core.datamodel.synchronization;

import java.util.List;

public class DataModelChangeNotifier {

    private static DataModelChangeNotifier instance;

    private List<DataModelChangeListener> listeners;

    private DataModelChangeNotifier() {
    }

    public synchronized static final DataModelChangeNotifier createInstance() {
        if (instance == null) {
            instance = new DataModelChangeNotifier();
        }
        return instance;
    }

    public synchronized void notifyChange(DMUpdateEvent dmUpdateEvent) {
        if (listeners != null) {
            for (DataModelChangeListener listener : listeners) {
                listener.onChange(dmUpdateEvent);
            }
        }
    }

    public synchronized void setListeners(List<DataModelChangeListener> listeners) {
        this.listeners = listeners;
    }
}
