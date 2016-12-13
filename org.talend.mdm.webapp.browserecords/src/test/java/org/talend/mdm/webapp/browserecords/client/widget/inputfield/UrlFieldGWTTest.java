/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield;

import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.ui.RootPanel;


public class UrlFieldGWTTest extends GWTTestCase  {

    public void testUrlField() {
        UrlField url = new UrlField();
        url.setAllowBlank(false);
        url.setEnabled(false);
        assertFalse(url.editImage.isVisible());

        int inputIndex = url.wrap.getChildIndex(url.input.dom);
        int handlerIndex = url.wrap.getChildIndex(url.handler);
        boolean hasInvalidStyle = url.wrap.hasStyleName("x-form-invalid"); //$NON-NLS-1$

        assertEquals(-1, inputIndex);
        assertEquals(-1, handlerIndex);
        assertEquals(false, url.isRendered());
        assertEquals(false, hasInvalidStyle);

        RootPanel.get().add(url);

        inputIndex = url.wrap.getChildIndex(url.input.dom);
        handlerIndex = url.wrap.getChildIndex(url.handler);

        assertEquals(0, inputIndex);
        assertEquals(1, handlerIndex);

        assertEquals(true, url.isRendered());

        hasInvalidStyle = url.wrap.hasStyleName("x-form-invalid"); //$NON-NLS-1$
        assertEquals(true, hasInvalidStyle);

        url.setValue("talend@@http://www.talend.com"); //$NON-NLS-1$

        hasInvalidStyle = url.wrap.hasStyleName("x-form-invalid"); //$NON-NLS-1$
        assertEquals(false, hasInvalidStyle);
        assertEquals("talend", url.input.dom.getInnerText()); //$NON-NLS-1$
        assertEquals("http://www.talend.com", url.input.dom.getAttribute("href")); //$NON-NLS-1$ //$NON-NLS-2$

        url.setValue(null);

        hasInvalidStyle = url.wrap.hasStyleName("x-form-invalid"); //$NON-NLS-1$
        assertEquals(true, hasInvalidStyle);
        assertEquals("", url.input.dom.getInnerText()); //$NON-NLS-1$
        assertEquals(false, url.input.dom.hasAttribute("href")); //$NON-NLS-1$
    }

    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }
}