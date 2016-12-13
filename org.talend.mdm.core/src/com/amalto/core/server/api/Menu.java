/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server.api;

import com.amalto.core.objects.menu.MenuPOJO;
import com.amalto.core.objects.menu.MenuPOJOPK;
import com.amalto.core.util.XtentisException;

import java.util.Collection;

/**
 *
 */
public interface Menu {
    /**
     * Creates or updates a menu
     * @throwsXtentisxception
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    MenuPOJOPK putMenu(MenuPOJO menu) throws XtentisException;

    /**
     * Get menu
     * @throws com.amalto.core.util.XtentisException
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    MenuPOJO getMenu(MenuPOJOPK pk)
    throws XtentisException;

    /**
     * Get a Menu - no exception is thrown: returns null if not found
     * @throws com.amalto.core.util.XtentisException
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    MenuPOJO existsMenu(MenuPOJOPK pk) throws XtentisException;

    /**
     * Remove a Menu
     * @throws com.amalto.core.util.XtentisException
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    MenuPOJOPK removeMenu(MenuPOJOPK pk)
    throws XtentisException;

    /**
	 * Retrieve all Menu PKs
	 *
	 * @throws com.amalto.core.util.XtentisException
     *
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    Collection<MenuPOJOPK> getMenuPKs(String regex) throws XtentisException;
}
