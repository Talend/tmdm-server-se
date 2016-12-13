/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server;

import org.springframework.context.ApplicationEvent;

/**
 * An application event published when all MDM storages are successfully initialized. 
 */
public class MDMInitializationCompletedEvent extends ApplicationEvent {

    private static final long serialVersionUID = -4013597339862281239L;

    public MDMInitializationCompletedEvent(Object source) {
        super(source);
    }

}
