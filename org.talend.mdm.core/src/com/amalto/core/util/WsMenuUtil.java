/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.amalto.core.webservice.WSMenuEntry;
import com.amalto.core.webservice.WSMenuMenuEntriesDescriptions;
import org.talend.mdm.commmon.util.core.CommonUtil;

public class WsMenuUtil {

    public static Menu wsMenu2Menu(Map<String, Menu> index, WSMenuEntry entry, Menu parent, String parentID, int position)
            throws Exception {
        try {
            Menu menu = new Menu();
            menu.setApplication(entry.getApplication());
            menu.setContext(entry.getContext());
            menu.setId(entry.getId());
            menu.setIcon(entry.getIcon());
            WSMenuMenuEntriesDescriptions[] descriptions = entry.getDescriptions();
            Map<String, String> labels = new HashMap<>();
            if (descriptions != null) {
                for (WSMenuMenuEntriesDescriptions description : descriptions) {
                    labels.put(description.getLanguage().toLowerCase(), description.getLabel());
                }
            }
            menu.setLabels(labels);
            menu.setParent(parent);
            menu.setParentID(parentID);
            menu.setPosition(position);
            // recursively add the the submenus. These ones have a parent
            WSMenuEntry[] wsSubMenus = entry.getSubMenus();
            Map<String, Menu> subMenus = new TreeMap<>();
            if (wsSubMenus != null) {
                for (int i = 0; i < wsSubMenus.length; i++) {
                    subMenus.put(Menu.TWO_DIGITS.format(i) + " - " + wsSubMenus[i].getId(), //$NON-NLS-1$
                            wsMenu2Menu(index, wsSubMenus[i], menu, menu.getParentID(), i));
                }
            }
            menu.setSubMenus(subMenus);
            index.put(menu.getId(), menu);
            return menu;
        } catch (Exception e) {
            throw new Exception(CommonUtil.getErrMsgFromException(e));
        }
    }
}
