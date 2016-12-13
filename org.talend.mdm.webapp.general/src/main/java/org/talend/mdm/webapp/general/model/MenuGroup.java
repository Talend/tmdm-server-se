/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.model;

import java.io.Serializable;
import java.util.List;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MenuGroup implements Serializable, IsSerializable {

    private static final long serialVersionUID = -976509836517973550L;

    List<GroupItem> groupItem;

    List<MenuBean> menuBean;

    public boolean hasSpecifiedMenu(MenuBean mb) {
        for (GroupItem item : groupItem) {
            for (String menu : item.getMenuItems()) {
                if (menu.equals(mb.getContext() + "." + mb.getApplication())) //$NON-NLS-1$
                    return true;
            }
        }
        return false;
    }

    public List<MenuBean> getMenuBean() {
        return menuBean;
    }

    public void setMenuBean(List<MenuBean> mb) {
        this.menuBean = mb;
    }

    public MenuGroup() {

    }

    public List<GroupItem> getGroupItem() {
        return groupItem;
    }

    public void setGroupItem(List<GroupItem> gi) {
        this.groupItem = gi;
    }
}
