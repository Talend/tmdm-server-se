/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client.resources.icon;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public interface Icons extends ClientBundle {

    Icons INSTANCE = GWT.create(Icons.class);

    @Source("alert-icon.png")
    ImageResource alert();

    @Source("start-icon.png")
    ImageResource start();

    @Source("task-list-icon.png")
    ImageResource task();

    @Source("transformer.png")
    ImageResource transformer();

    @Source("launch_process.png")
    ImageResource launch();

    @Source("find.png")
    ImageResource find();

    @Source("chart.png")
    ImageResource chart();
}
