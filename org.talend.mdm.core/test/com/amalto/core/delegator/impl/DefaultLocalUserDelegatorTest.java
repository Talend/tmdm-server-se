// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.delegator.impl;

import junit.framework.TestCase;

import com.amalto.core.delegator.IBeanDelegator;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.util.XtentisException;


public class DefaultLocalUserDelegatorTest extends TestCase{
    public void testLocalUserDelegator() throws XtentisException {
        try {
            ILocalUser localUser = new DefaultLocalUserDelegator();
            assertEquals(IBeanDelegator.CE_USER, localUser.getUserType());
        } catch (Exception e) {
            fail("get user type fail."); //$NON-NLS-1$
        }
    }
}
