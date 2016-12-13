/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.widget;

import junit.framework.TestCase;

import org.talend.mdm.webapp.base.client.widget.Callback;
import org.talend.mdm.webapp.base.client.widget.CallbackAction;

@SuppressWarnings("nls")
public class CallbackActionTest extends TestCase {

    boolean status = false;

    final String result = "successful";

    final String concept = "Product";
    
    public void testDoAction() {
        CallbackAction.getInstance().putAction("testAcion", new Callback() {

                    @Override
                    public void doAction(String cpt, Object value, Boolean isClose) {
                        assertNull(cpt);
                        assertEquals(value, result);
                        action();
                    }
                });
        CallbackAction.getInstance().doAction("testAcion", result, false);
        assertTrue(status);
    }

    public void testDoAction2() {
        status = false;
        CallbackAction.getInstance().putAction("testAcion", new Callback() {

            @Override
            public void doAction(String cpt, Object value, Boolean isClose) {
                assertEquals(value, result);
                assertEquals(cpt, concept);
                action();
            }
        });
        
        CallbackAction.getInstance().doAction("testAcion", concept, result, false);
        assertTrue(status);
        
    }
    public void action() {
        status = true;
    }
}
