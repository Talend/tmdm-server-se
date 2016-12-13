/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.delegator;

import java.util.Map;

public class BeanDelegatorContainer {

    private Map<String, Object> delegatorInstancePool;

    private static final String LOCAL_USER = "LocalUser"; //$NON-NLS-1$

    private static final String VALIDATION = "Validation"; //$NON-NLS-1$

    private static final String ITEM_CTRL = "ItemCtrl"; //$NON-NLS-1$

    private static final String XTENTIS_WS = "XtentisWS"; //$NON-NLS-1$

    private static BeanDelegatorContainer instance;

    private BeanDelegatorContainer() {
    }
   
    public void setDelegatorInstancePool(Map<String, Object> delegatorInstancePool) {
        this.delegatorInstancePool = delegatorInstancePool;
    }

    public static synchronized BeanDelegatorContainer createInstance() {
        if (instance != null) {
            throw new IllegalStateException();
        }
        instance = new BeanDelegatorContainer();
        return instance;
    }

    public static synchronized BeanDelegatorContainer getInstance() {
        if (instance == null) {
            throw new IllegalStateException();
        }
        return instance;
    }

    public ILocalUser getLocalUserDelegator() {
        synchronized (delegatorInstancePool) {
            return (ILocalUser) delegatorInstancePool.get(LOCAL_USER);
        }
    }

    public IValidation getValidationDelegator() {
        synchronized (delegatorInstancePool) {
            return (IValidation) delegatorInstancePool.get(VALIDATION);
        }
    }

    public IItemCtrlDelegator getItemCtrlDelegator() {
        synchronized (delegatorInstancePool) {
            return (IItemCtrlDelegator) delegatorInstancePool.get(ITEM_CTRL);
        }
    }

    public IXtentisWSDelegator getXtentisWSDelegator() {
        synchronized (delegatorInstancePool) {
            return (IXtentisWSDelegator) delegatorInstancePool.get(XTENTIS_WS);
        }
    }
}
