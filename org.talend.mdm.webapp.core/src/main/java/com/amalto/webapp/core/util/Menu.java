/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.util;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.CommonUtil;
import org.talend.mdm.commmon.util.core.ICoreConstants;

import com.amalto.core.server.security.SecurityConfig;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;
import com.amalto.core.webservice.WSBoolean;
import com.amalto.core.webservice.WSExistsMenu;
import com.amalto.core.webservice.WSGetMenu;
import com.amalto.core.webservice.WSGetRole;
import com.amalto.core.webservice.WSMenu;
import com.amalto.core.webservice.WSMenuEntry;
import com.amalto.core.webservice.WSMenuMenuEntriesDescriptions;
import com.amalto.core.webservice.WSMenuPK;
import com.amalto.core.webservice.WSRole;
import com.amalto.core.webservice.WSRolePK;
import com.amalto.core.webservice.WSRoleSpecification;
import com.amalto.core.webservice.WSRoleSpecificationInstance;

public class Menu {

    private static final Logger LOGGER = Logger.getLogger(Menu.class);

    private static final DecimalFormat twoDigits = new DecimalFormat("00"); //$NON-NLS-1$

    private static final Messages MESSAGES = MessagesFactory.getMessages(
            "com.amalto.webapp.core.util.messages", Menu.class.getClassLoader()); //$NON-NLS-1$

    private HashMap<String, String> labels = new HashMap<String, String>();

    private String id;

    private Menu parent = null; // this value is not nulmm when the menu is "linked"

    private String parentID;

    private int position;

    private String context;

    private String application;

    private String icon;

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    private TreeMap<String, Menu> subMenus = new TreeMap<String, Menu>();

    public Menu getParent() {
        return parent;
    }

    public void setParent(Menu parent) {
        this.parent = parent;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, String> getLabels() {
        return labels;
    }

    public void setLabels(HashMap<String, String> labels) {
        this.labels = labels;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public TreeMap<String, Menu> getSubMenus() {
        return subMenus;
    }

    public void setSubMenus(TreeMap<String, Menu> subMenus) {
        this.subMenus = subMenus;
    }

    public static Menu getRootMenu() throws XtentisWebappException {
        try {
            // first fetch the menu index
            HashMap<String, Menu> menuIndex = getMenuIndex();
            // create a RootMenu Holder
            Menu root = new Menu();
            // go over all the Menu Entries that have a null parent an try to put them at the appropriate location
            Set<String> ids = menuIndex.keySet();
            for (String id : ids) {
                Menu menu = menuIndex.get(id);
                if (menu.getParent() != null)
                    continue; // we are good keep going
                if ((menu.getParentID() == null) || "".equals(menu.getParentID())) { //$NON-NLS-1$
                    // attach to root
                    root.getSubMenus().put(twoDigits.format(menu.getPosition()) + " - " + menu.getId(), menu); //$NON-NLS-1$
                    // update parent with root
                    menu.setParent(root);
                    continue;// done
                }
                // try to find the entry
                Menu parentMenu = menuIndex.get(menu.getParentID());
                if (parentMenu == null) {
                    // discard
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("getRootMenu() No parent found for " + menu.getId()); //$NON-NLS-1$
                    }
                    continue;
                }
                // found - add it to parent
                parentMenu.getSubMenus().put(twoDigits.format(menu.getPosition()) + " - " + menu.getId(), menu); //$NON-NLS-1$
                // update menu with parent ref
                menu.setParent(parentMenu);
            }
            return root;
        } catch (XtentisWebappException e) {
            throw (e);
        } catch (Exception e) {
            throw new XtentisWebappException(CommonUtil.getErrMsgFromException(e));
        }
    }

    private static HashMap<String, Menu> getMenuIndex() throws XtentisWebappException {
        // The index of Menu Entries
        HashMap<String, Menu> menuIndex = new HashMap<String, Menu>();
        try {
            HashSet<String> roles = LocalUser.getLocalUser().getRoles();
            if (roles.contains(ICoreConstants.ADMIN_PERMISSION)) {
                // Add tool menus
                Menu menu = new Menu();
                menu.setApplication("H2Console"); //$NON-NLS-1$
                menu.setContext("h2console"); //$NON-NLS-1$
                menu.setId("H2Console"); //$NON-NLS-1$
                HashMap<String, String> labels = new HashMap<String, String>() {

                    private static final long serialVersionUID = 1L;

                    public String get(Object key) {
                        return MESSAGES.getMessage(new Locale(key.toString()), "menu.h2console"); //$NON-NLS-1$
                    };
                };
                menu.setLabels(labels);
                menu.setParent(null);
                menu.setParentID(""); //$NON-NLS-1$
                menu.setPosition(0);
                menuIndex.put(menu.getId(), menu);

                menu = new Menu();
                menu.setApplication("LogViewer"); //$NON-NLS-1$
                menu.setContext("logviewer"); //$NON-NLS-1$
                menu.setId("LogViewer"); //$NON-NLS-1$
                labels = new HashMap<String, String>() {

                    private static final long serialVersionUID = 1L;

                    public String get(Object key) {
                        return MESSAGES.getMessage(new Locale(key.toString()), "menu.logviewer"); //$NON-NLS-1$
                    };
                };
                menu.setLabels(labels);
                menu.setParent(null);
                menu.setParentID(""); //$NON-NLS-1$
                menu.setPosition(0);
                menuIndex.put(menu.getId(), menu);
                
                // REST API documentation
                menu = new Menu();
                menu.setApplication("RestApiDoc"); //$NON-NLS-1$
                menu.setContext("apidoc"); //$NON-NLS-1$
                menu.setId("RestApiDoc"); //$NON-NLS-1$
                labels = new HashMap<String, String>() {

                    private static final long serialVersionUID = 1L;

                    public String get(Object key) {
                        return MESSAGES.getMessage(new Locale(key.toString()), "menu.apidoc"); //$NON-NLS-1$
                    };
                };
                menu.setLabels(labels);
                menu.setParent(null);
                menu.setParentID(""); //$NON-NLS-1$
                menu.setPosition(0);
                menuIndex.put(menu.getId(), menu);
                
            }
            // not admin
            for (String role : roles) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("getMenuIndex() ROLE " + role); //$NON-NLS-1$
                }
                if (!SecurityConfig.isSecurityPermission(role)) {
                    WSRole wsRole = Util.getPort().getRole(new WSGetRole(new WSRolePK(role)));
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("getMenuIndex() WSROLE " + wsRole.getName()); //$NON-NLS-1$
                    }
                    WSRoleSpecification[] specifications = wsRole.getSpecification();
                    if (specifications != null) {
                        for (WSRoleSpecification specification : specifications) {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("getMenuIndex() OBJECT TYPE " + specification.getObjectType()); //$NON-NLS-1$
                            }
                            if ("Menu".equals(specification.getObjectType())) { //$NON-NLS-1$
                                WSRoleSpecificationInstance[] instances = specification.getInstance();
                                if (instances != null) {
                                    for (WSRoleSpecificationInstance instance : instances) {
                                        if (LOGGER.isDebugEnabled()) {
                                            LOGGER.debug("getMenuIndex() INSTANCE NAME " + instance.getInstanceName()); //$NON-NLS-1$
                                        }
                                        try {
                                            addMenuEntries(menuIndex, instance);
                                        } catch (Exception e) {
                                            LOGGER.error(e.getMessage(), e);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
            return menuIndex;
        } catch (XtentisWebappException e) {
            throw (e);
        } catch (Exception e) {
            throw new XtentisWebappException(CommonUtil.getErrMsgFromException(e));
        }
    }

    private static void addMenuEntries(HashMap<String, Menu> index, WSRoleSpecificationInstance instance)
            throws XtentisWebappException {
        String menuPK = instance.getInstanceName();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("addMenuEntries() " + menuPK); //$NON-NLS-1$
        }
        try {
            // check menu exist
            WSBoolean menuExist = Util.getPort().existsMenu(new WSExistsMenu(new WSMenuPK(menuPK)));
            if (menuExist.is_true()) {
                RoleMenuParameters params = RoleMenuParameters.unmarshalMenuParameters(instance.getParameter()[0]);
                addMenuEntry(index, new WSMenuPK(menuPK), params);
            } else {
                LOGGER.error("Menu '" + menuPK + "' does not exist."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (XtentisWebappException e) {
            throw (e);
        } catch (Exception e) {
            throw new XtentisWebappException(CommonUtil.getErrMsgFromException(e));
        }
    }

    private static void addMenuEntry(Map<String, Menu> index, WSMenuPK menuPK, RoleMenuParameters params) throws Exception {
        WSMenu wsMenu = Util.getPort().getMenu(new WSGetMenu(menuPK));
        WSMenuEntry[] wsEntries = wsMenu.getMenuEntries();
        if (wsEntries != null) {
            for (WSMenuEntry wsEntry : wsEntries) {
                index.put(wsEntry.getId(), wsMenu2Menu(index, wsEntry, null, params.getParentID(), params.getPosition()));
            }
        }
    }

    public static Menu wsMenu2Menu(Map<String, Menu> index, WSMenuEntry entry, Menu parent, String parentID, int position)
            throws XtentisWebappException {
        try {
            Menu menu = new Menu();
            menu.setApplication(entry.getApplication());
            menu.setContext(entry.getContext());
            menu.setId(entry.getId());
            menu.setIcon(entry.getIcon());
            WSMenuMenuEntriesDescriptions[] descriptions = entry.getDescriptions();
            HashMap<String, String> labels = new HashMap<String, String>();
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
            TreeMap<String, Menu> subMenus = new TreeMap<String, Menu>();
            if (wsSubMenus != null) {
                for (int i = 0; i < wsSubMenus.length; i++) {
                    subMenus.put(twoDigits.format(i) + " - " + wsSubMenus[i].getId(), //$NON-NLS-1$
                            wsMenu2Menu(index, wsSubMenus[i], menu, menu.getParentID(), i));
                }
            }
            menu.setSubMenus(subMenus);
            index.put(menu.getId(), menu);
            return menu;
        } catch (XtentisWebappException e) {
            throw (e);
        } catch (Exception e) {
            throw new XtentisWebappException(CommonUtil.getErrMsgFromException(e));
        }
    }

    public static String getMenuLabel(String language, String menuIndex) throws Exception {
        HashMap<String, Menu> menus = getMenuIndex();
        Menu menu = menus.get(menuIndex);
        if ((null == language) || (language.trim().equals(""))) { //$NON-NLS-1$
            language = "en"; //$NON-NLS-1$
        }
        return menu.getLabels().get(language);
    }
}
