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

import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.objects.view.ViewPOJOPK;

public interface View {
    /**
     * Creates or updates a View
     */
    public ViewPOJOPK putView(ViewPOJO view)
            throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

    /**
     * Get item
     */
    public ViewPOJO getView(ViewPOJOPK pk)
            throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

    /**
     * Get a View - no exception is thrown: returns null if not found
     */
    public ViewPOJO existsView(ViewPOJOPK pk)
            throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

    /**
     * Remove an item
     */
    public ViewPOJOPK removeView(ViewPOJOPK pk)
            throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

    /**
     * Retrieve all View PKS
     */
    public java.util.Collection<ViewPOJOPK> getViewPKs(String regex)
            throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

    /**
     * Retrieve all Views
     */
    public java.util.List<ViewPOJO> getAllViews(String regex)
            throws com.amalto.core.util.XtentisException, java.rmi.RemoteException;

}
