/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client;

import com.extjs.gxt.ui.client.event.EventType;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class WelcomePortalEvents {

    public static final EventType InitFrame = new EventType();

    public static final EventType RefreshPortlet = new EventType();

    public static final EventType RefreshPortal = new EventType();

    // for db storing failure started by switching to diff column number or chartsSwitherUpdated
    public static final EventType RevertRefreshPortal = new EventType();
}
