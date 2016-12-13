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

import com.google.gwt.event.shared.HandlerRegistration;
import org.talend.mdm.webapp.stagingarea.control.shared.event.HasModelEvents;
import org.talend.mdm.webapp.stagingarea.control.shared.event.ModelEvent;
import org.talend.mdm.webapp.stagingarea.control.shared.event.ModelEventHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Abstract class to ease implementation of {@link HasModelEvents} for all model related classes.
 */
class AbstractHasModelEvents implements HasModelEvents {

    private final Set<ModelEventHandler> handlers = new HashSet<ModelEventHandler>();

    private final Map<String, Object> values = new HashMap<String, Object>();

    <T> T get(String key) {
        return (T) values.get(key);
    }

    void set(String key, Object value) {
        values.put(key, value);
    }

    public HandlerRegistration addModelEventHandler(final ModelEventHandler handler) {
        handlers.add(handler);
        return new HandlerRegistration() {

            public void removeHandler() {
                handlers.remove(handler);
            }
        };
    }

    public void notifyHandlers(ModelEvent event) {
        for (ModelEventHandler handler : handlers) {
            handler.onModelEvent(event);
        }
    }
}
