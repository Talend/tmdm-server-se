/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.widget;

import org.talend.mdm.webapp.base.shared.Constants;

import com.google.gwt.junit.client.GWTTestCase;

public class PagingToolBarExGWTTest extends GWTTestCase {

    public void testPagingToolBarEx() {
        PagingToolBarEx bar = new PagingToolBarEx(-1);
        assertEquals(Constants.PAGE_SIZE, bar.getPageSize());

        bar = new PagingToolBarEx(0);
        assertEquals(Constants.PAGE_SIZE, bar.getPageSize());

        bar = new PagingToolBarEx(5);
        assertEquals(5, bar.getPageSize());
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.base.TestBase"; //$NON-NLS-1$
    }

}
