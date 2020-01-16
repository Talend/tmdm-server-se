/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 *  %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.delegator;

import com.amalto.core.util.XtentisException;

import java.util.HashSet;

@SuppressWarnings("nls")
public class MockILocalUser extends ILocalUser {

    @Override
    public ILocalUser getILocalUser() throws XtentisException {
        return this;
    }

    @Override
    public HashSet<String> getRoles() {
        HashSet<String> roleSet = new HashSet<>();
        roleSet.add("Demo_Manager");
        return roleSet;
    }

    @Override
    public String getUsername() {
        return "Admin";
    }

    @Override
    public boolean isAdmin(Class<?> objectTypeClass) throws XtentisException {
        return true;
    }
}
