/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.delegator.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.CommonUtil;
import org.talend.mdm.commmon.util.webapp.XObjectType;

import com.amalto.core.webservice.WSBoolean;
import com.amalto.core.webservice.WSExistsMenu;
import com.amalto.core.webservice.WSGetMenu;
import com.amalto.core.webservice.WSMenu;
import com.amalto.core.webservice.WSMenuEntry;
import com.amalto.core.webservice.WSMenuPK;
import com.amalto.core.delegator.BaseMenu;
import com.amalto.core.util.Menu;
import com.amalto.core.util.WsMenuUtil;

public class DefaultMenuUtilDelegator extends BaseMenu {

    private static final Logger LOGGER = Logger.getLogger(DefaultMenuUtilDelegator.class);

    private static class MenuParameters {
        private int position;
        private String parentID;
        public MenuParameters(int position, String parentID) {
            this.position = position;
            this.parentID = parentID;
        }
        
        public int getPosition() {
            return position;
        }

        public String getParentID() {
            return parentID;
        }
    }

    private static Map<String, MenuParameters> menuParametersMap;

    private static enum MenuItem {
        BROWSERECORDS("BrowseRecords"), 
        UPDATEREPORT("UpdateReport"), 
        WELCOMEPORTAL("WelcomePortal"), 
        RECYCLEBIN("RecycleBin");

        private String label;

        MenuItem(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }
    }

    static {
        menuParametersMap = new HashMap<String, MenuParameters>();
        menuParametersMap.put(MenuItem.BROWSERECORDS.getLabel(), new MenuParameters(1,""));
        menuParametersMap.put(MenuItem.UPDATEREPORT.getLabel(), new MenuParameters(4,""));
        menuParametersMap.put(MenuItem.WELCOMEPORTAL.getLabel(), new MenuParameters(0,""));
        menuParametersMap.put(MenuItem.RECYCLEBIN.getLabel(), new MenuParameters(5,""));
    }

    @Override
    public Map<String, Menu> getNotAdminMenuIndex(Map<String, Menu> menuIndex, HashSet<String> roles) throws Exception {
        try {
            for (Map.Entry<String, MenuParameters> entry : menuParametersMap.entrySet()) {
                try {
                    addMenuEntries(menuIndex, entry.getKey(), entry.getValue().getParentID(), entry.getValue().getPosition());
                } catch (Exception e) {
                    throw new Exception(CommonUtil.getErrMsgFromException(e));
                }
            }
            return menuIndex;
        } catch (Exception e) {
            throw new Exception(CommonUtil.getErrMsgFromException(e));
        }
    }

    @Override
    protected void addMenuEntries(Map<String, Menu> index, String menuPK, String menuParentID, int menuPosition)
            throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("addMenuEntries() " + menuPK); //$NON-NLS-1$
        }
        try {
            // check menu exist
            WSBoolean menuExist = getIXtentisWSDelegator().existsMenu(new WSExistsMenu(new WSMenuPK(menuPK)));
            if (menuExist.is_true()) {
                addMenuEntry(index, new WSMenuPK(menuPK), menuParentID, menuPosition);
            } else {
                LOGGER.error("Menu '" + menuPK + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (Exception e) {
            throw new Exception(CommonUtil.getErrMsgFromException(e));
        }
    }

    @Override
    protected void addMenuEntry(Map<String, Menu> index, WSMenuPK menuPK, String menuParentID, int menuPosition) throws Exception {
        WSMenu wsMenu = getIXtentisWSDelegator().getMenu(new WSGetMenu(menuPK));
        WSMenuEntry[] wsEntries = wsMenu.getMenuEntries();
        if (wsEntries != null) {
            for (WSMenuEntry wsEntry : wsEntries) {
                index.put(wsEntry.getId(), WsMenuUtil.wsMenu2Menu(index, wsEntry, null, menuParentID, menuPosition));
            }
        }
    }
}