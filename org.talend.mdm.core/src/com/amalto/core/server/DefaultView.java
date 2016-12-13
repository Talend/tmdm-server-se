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
import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.objects.view.ViewPOJOPK;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;
import com.amalto.core.server.api.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultView implements View {

    public static final Logger LOGGER = Logger.getLogger(DefaultView.class);

    /**
     * Creates or updates a View
     */
    @Override
    public ViewPOJOPK putView(ViewPOJO view) throws XtentisException {
        try {
            ObjectPOJOPK pk = view.store();
            if (pk == null)
                throw new XtentisException("Unable to create the View"); //$NON-NLS-1$
            return new ViewPOJOPK(pk);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to create/update the View '" + view.getName() + '\''; //$NON-NLS-1$
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get item
     */
    @Override
    public ViewPOJO getView(ViewPOJOPK pk) throws XtentisException {
        try {
            ViewPOJO sp = ObjectPOJO.load(ViewPOJO.class, pk);
            if (sp == null) {
                String err = "The View '" + pk.getUniqueId() + "' does not exist."; //$NON-NLS-1$ //$NON-NLS-2$
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            return sp;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the View '" + pk.toString() + '\''; //$NON-NLS-1$
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get a View - no exception is thrown: returns null if not found
     */
    @Override
    public ViewPOJO existsView(ViewPOJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(ViewPOJO.class, pk);
        } catch (XtentisException e) {
            return null;
        } catch (Exception e) {
            String info = "Could not check whether this View exists: '" + pk.getUniqueId() + '\''; //$NON-NLS-1$
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(info, e);
            }
            return null;
        }
    }

    /**
     * Remove an item
     */
    @Override
    public ViewPOJOPK removeView(ViewPOJOPK pk) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removing View '" + pk.getUniqueId() + '\''); //$NON-NLS-1$
        }
        try {
            return new ViewPOJOPK(ObjectPOJO.remove(ViewPOJO.class, pk));
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the View '" + pk.toString() + '\'';  //$NON-NLS-1$
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Retrieve all View PKS
     */
    @Override
    public Collection<ViewPOJOPK> getViewPKs(String regex) throws XtentisException {
        ArrayList<ObjectPOJOPK> c = ObjectPOJO.findAllPKs(ViewPOJO.class, regex);
        ArrayList<ViewPOJOPK> l = new ArrayList<ViewPOJOPK>();
        for (ObjectPOJOPK objectPOJOPK : c) {
            l.add(new ViewPOJOPK(objectPOJOPK));
        }
        return l;
    }

    /**
     * Retrieve all Views
     */
    @Override
    public List<ViewPOJO> getAllViews(String regex) throws XtentisException {
        ArrayList<ObjectPOJOPK> c = ObjectPOJO.findAllPKs(ViewPOJO.class, regex);
        ArrayList<ViewPOJO> l = new ArrayList<ViewPOJO>();
        for (ObjectPOJOPK objectPOJOPK : c) {
            ViewPOJO pojo = ObjectPOJO.load(ViewPOJO.class, objectPOJOPK);
            l.add(pojo);
        }
        return l;
    }
}