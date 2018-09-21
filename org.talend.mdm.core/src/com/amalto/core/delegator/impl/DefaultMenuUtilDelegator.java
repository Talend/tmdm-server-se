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

    static {
        menuParametersMap = new HashMap<String, MenuParameters>();
        menuParametersMap.put("BrowseRecords", new MenuParameters(1,""));
        menuParametersMap.put("UpdateReport", new MenuParameters(4,""));
        menuParametersMap.put("WelcomePortal", new MenuParameters(0,""));
        menuParametersMap.put("RecycleBin", new MenuParameters(5,""));
    }

    @Override
    public HashMap<String, Menu> getNotAdminMenuIndex(HashMap<String, Menu> menuIndex, HashSet<String> roles) throws Exception {
        try {
            for (Map.Entry<String, MenuParameters> entry : menuParametersMap.entrySet()) {
                try {
                    addMenuEntries(menuIndex, entry);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
            return menuIndex;
        } catch (Exception e) {
            throw new Exception(CommonUtil.getErrMsgFromException(e));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void addMenuEntries(HashMap<String, Menu> index, Object entry)
            throws Exception {
        String menuPK = ((Map.Entry<String, MenuParameters>)entry).getKey();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("addMenuEntries() " + menuPK); //$NON-NLS-1$
        }
        try {
            // check menu exist
            WSBoolean menuExist = getIXtentisWSDelegator().existsMenu(new WSExistsMenu(new WSMenuPK(menuPK)));
            if (menuExist.is_true()) {
                addMenuEntry(index, new WSMenuPK(menuPK), ((Map.Entry<String, MenuParameters>)entry).getValue());
            } else {
                LOGGER.error("Menu '" + menuPK + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (Exception e) {
            throw new Exception(CommonUtil.getErrMsgFromException(e));
        }
    }

    @Override
    public void addMenuEntry(Map<String, Menu> index, WSMenuPK menuPK, Object params) throws Exception {
        WSMenu wsMenu = getIXtentisWSDelegator().getMenu(new WSGetMenu(menuPK));
        WSMenuEntry[] wsEntries = wsMenu.getMenuEntries();
        if (wsEntries != null) {
            for (WSMenuEntry wsEntry : wsEntries) {
                index.put(wsEntry.getId(), WsMenuUtil.wsMenu2Menu(index, wsEntry, null, ((MenuParameters)params).getParentID(), ((MenuParameters)params).getPosition()));
            }
        }
    }
}