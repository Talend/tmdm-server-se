/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client;

import junit.framework.TestCase;

import org.talend.mdm.webapp.base.client.exception.ServiceException;

@SuppressWarnings("nls")
public class SessionAwareAsyncCallbackTest extends TestCase {

    public void testOnFailure() {
        final String flag = "failed";
        new SessionAwareAsyncCallback<Object>() {

            @Override
            public void onSuccess(Object arg0) {
                               
            }

            @Override
            protected void doOnFailure(Throwable caught) {
                assertNotNull(caught);
                assertEquals(flag, caught.getMessage());
            }
            
        }.doOnFailure(new ServiceException(flag));
    }
    
 
}
