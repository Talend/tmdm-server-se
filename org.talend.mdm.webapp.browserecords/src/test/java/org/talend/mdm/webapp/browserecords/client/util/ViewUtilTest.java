/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class ViewUtilTest extends TestCase {

    public void testConvertCSS4ValueStyle() {
        String css = null;
        assertNull(ViewUtil.convertCSS4ValueStyle(css));
        
        css = "width: 192px;";
        assertNotNull(css);
        assertEquals(css, ViewUtil.convertCSS4ValueStyle(css));
        
        css = "background-color: red; width: 192px;";
        assertNotNull(css);
        assertEquals("background-image:none; " + css, ViewUtil.convertCSS4ValueStyle(css));
    }
}
