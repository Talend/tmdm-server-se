/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server;

import junit.framework.TestCase;

import org.talend.mdm.webapp.base.shared.XpathUtil;

public class XPathUtilTest extends TestCase {

    public void test_convertAbsolutePath() {
        String foreignKey = "ProductFamily/Id"; //$NON-NLS-1$
        String xpath = "../Name"; //$NON-NLS-1$
        String realXPath = "ProductFamily/Name"; //$NON-NLS-1$

        assertEquals(realXPath, XpathUtil.convertAbsolutePath(foreignKey, xpath));

        xpath = "./Name"; //$NON-NLS-1$
        assertEquals(realXPath, XpathUtil.convertAbsolutePath(foreignKey, xpath));
    }

}
