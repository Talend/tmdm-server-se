/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.delegator.impl;

import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.util.XtentisException;

public class DefaultLocalUserDelegator extends ILocalUser {

    private static ILocalUser iUser = new DefaultLocalUserDelegator();

    private String userXML;

    @Override
    public ILocalUser getILocalUser() throws XtentisException {
        return iUser;
    }

    /**
     * The User in XML form as stored in the DB
     * 
     * @return The user in the DB XML form
     */
    @Override
    public String getUserXML() {
        return userXML;
    }

    @Override
    public void setUserXML(String userXML) {
        this.userXML = userXML;
    }
}
