/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.shared.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Represents a event fired by the model of the application.
 * 
 * @see Types
 */
public class ModelEvent extends GwtEvent<ModelEventHandler> {

    private final Type<ModelEventHandler> type;

    private final Object     model;

    public ModelEvent(Types type, Object model) {
        this.model = model;
        this.type = type.getType();
    }

    @Override
    public Type<ModelEventHandler> getAssociatedType() {
        return type;
    }

    /**
     * @return Returns the model that fired this event.
     */
    public <T> T getModel() {
        return (T) model;
    }

    @Override
    protected void dispatch(ModelEventHandler eventHandler) {
        eventHandler.onModelEvent(this);
    }

    /**
     * <p>
     * All event types fired by the model. Example of usage:
     * </p>
     * <code>
     * public void onModelEvent(ModelEvent e) {<br/>
     * &nbsp;if (e.getAssociatedType() == ModelEvent.Types.BEGIN_MODIFY_SEARCH_RESULTS.getType()) {<br/>
     * &nbsp;&nbsp;// Do something<br/>
     * &nbsp;}<br/>
     * }
     * </code>
     */
    public static enum Types {
        VALIDATION_END(new Type<ModelEventHandler>()),
        VALIDATION_CANCEL(new Type<ModelEventHandler>()),
        VALIDATION_START(new Type<ModelEventHandler>()),
        CONTAINER_MODEL_CHANGED(new Type<ModelEventHandler>()),
        VALIDATION_MODEL_CHANGED(new Type<ModelEventHandler>()),
        PREVIOUS_EXECUTION_CHANGED(new Type<ModelEventHandler>());

        private final Type<ModelEventHandler> type;

        Types(Type<ModelEventHandler> type) {
            this.type = type;
        }

        public Type<ModelEventHandler> getType() {
            return type;
        }
    }
}
