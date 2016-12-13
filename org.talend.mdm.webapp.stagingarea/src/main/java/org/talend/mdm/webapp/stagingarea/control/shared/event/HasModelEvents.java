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

import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Implementations of this interface can fire {@link ModelEvent}.
 */
public interface HasModelEvents {

    /**
     * @param handler A {@link ModelEventHandler} implementation.
     * @return A {@link com.google.gwt.event.shared.HandlerRegistration} implementation.
     */
    HandlerRegistration addModelEventHandler(ModelEventHandler handler);

}
