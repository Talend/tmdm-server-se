/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client.mvc;

import com.extjs.gxt.ui.client.event.EventType;

public class GeneralEvent {

    public static final EventType LoadProductInfo = new EventType();

    public static final EventType LoadUser = new EventType();

    public static final EventType InitFrame = new EventType();

    public static final EventType LoadMenus = new EventType();

    public static final EventType LoadActions = new EventType();

    public static final EventType LoadLanguages = new EventType();

    public static final EventType LoadWelcome = new EventType();

    public static final EventType SwitchClusterAndModel = new EventType();

    public static final EventType SupportStaging = new EventType();

}
