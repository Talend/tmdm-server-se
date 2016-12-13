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

import junit.framework.Test;
import junit.framework.TestCase;

import com.google.gwt.junit.tools.GWTTestSuite;

@SuppressWarnings("nls")
public class ClientGWTTestSuite extends TestCase /* note this is TestCase and not TestSuite */
{

    public static Test suite() {
        GWTTestSuite suite = new GWTTestSuite("All Gwt Tests go in here");
        suite.addTestSuite(org.talend.mdm.webapp.base.client.util.ImageUitlGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.base.client.widget.MultiLanguageFieldGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.base.client.widget.PagingToolBarExGWTTest.class);
        return suite;
    }
}
