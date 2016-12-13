/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client.widget.options;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.googlecode.gflot.client.Tick;

public class AxeTicks extends JsArray<Tick> {

    // Overlay types always have protected, zero-arg ctors
    protected AxeTicks() {
    }

    public static final AxeTicks create() {
        return JavaScriptObject.createArray().cast();
    }
}
