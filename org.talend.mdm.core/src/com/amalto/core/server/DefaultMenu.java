/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.menu.MenuPOJO;
import com.amalto.core.objects.menu.MenuPOJOPK;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;
import com.amalto.core.server.api.Menu;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultMenu implements Menu {

    private static final Logger LOGGER = Logger.getLogger(DefaultMenu.class);

    @Override
    public MenuPOJOPK putMenu(MenuPOJO menu) throws XtentisException {
        LOGGER.debug("createMenu()");
        try {
            ObjectPOJOPK pk = menu.store();
            if (pk == null) {
                throw new XtentisException("Unable to create the Menu. Please check the XML Server logs");
            }
            return new MenuPOJOPK(pk);
        } catch (Exception e) {
            String err = "Unable to create/update the menu " + menu.getName() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    @Override
    public MenuPOJO getMenu(MenuPOJOPK pk) throws XtentisException {
        LOGGER.debug("getMenu() ");
        try {
            MenuPOJO menu = ObjectPOJO.load(MenuPOJO.class, pk);
            if (menu == null) {
                String err = "The Menu " + pk.getUniqueId() + " does not exist.";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            return menu;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the Menu " + pk.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    @Override
    public MenuPOJO existsMenu(MenuPOJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(MenuPOJO.class, pk);
        } catch (XtentisException e) {
            return null;
        } catch (Exception e) {
            String info = "Could not check whether this Menu \"" + pk.getUniqueId() + "\" exists:  " + ": "
                    + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.debug(info, e);
            return null;
        }
    }

    @Override
    public MenuPOJOPK removeMenu(MenuPOJOPK pk) throws XtentisException {
        LOGGER.debug("Removing " + pk.getUniqueId());
        try {
            return new MenuPOJOPK(ObjectPOJO.remove(MenuPOJO.class, pk));
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the Menu " + pk.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    @Override
    public Collection<MenuPOJOPK> getMenuPKs(String regex) throws XtentisException {
        try {
            Collection<ObjectPOJOPK> c = ObjectPOJO.findAllPKs(MenuPOJO.class, regex);
            ArrayList<MenuPOJOPK> l = new ArrayList<MenuPOJOPK>();
            for (ObjectPOJOPK entry : c) {
                l.add(new MenuPOJOPK(entry));
            }
            return l;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the Menu PKs for regular expression \"" + regex + "\"" + ": " + e.getClass().getName()
                    + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }
}
