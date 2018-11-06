/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.delegator;

import java.util.HashSet;
import java.util.Map;

import com.amalto.core.util.Menu;
import com.amalto.core.webservice.WSMenuPK;

public abstract class BaseMenu {

    public Map<String, Menu> getNotAdminMenuIndex(Map<String, Menu> menuIndex, HashSet<String> roles) 
            throws Exception {
        return null;
    }

    protected void addMenuEntries(Map<String, Menu> index, String menuPK, String menuParentID, int menuPosition)
            throws Exception {
    }

    protected void addMenuEntry(Map<String, Menu> index, WSMenuPK menuPK, String menuParentID, int menuPosition)
            throws Exception {
    }

    protected static IXtentisWSDelegator getIXtentisWSDelegator() {
        return BeanDelegatorContainer.getInstance().getXtentisWSDelegator();
    }
}
