/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.bizhelpers;

import java.io.BufferedReader;
//============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeLayoutModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.amalto.webapp.core.util.Util;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class CustomLayoutTest extends TestCase {

    public void testBuilderLayout() throws Exception {
        String xml = inputStream2String(this.getClass().getResourceAsStream("temp_ColumnTreeLayout.xml"));
        Document doc = Util.parse(xml);
        Element root = doc.getDocumentElement();
        ColumnTreeLayoutModel result = ViewHelper.builderLayout(root);
        assertNotNull(result);
        assertEquals(2, result.getColumnTreeModels().size());
        assertTrue(result.getColumnTreeModels().get(0).getStyle().equals(""));
        assertTrue(result.getColumnTreeModels().get(1).getStyle().equals(""));
        assertTrue(result.getColumnTreeModels().get(0).getStyle().equals(""));
        assertNotNull(result.getColumnTreeModels().get(0).getColumnElements());
        assertNotNull(result.getColumnTreeModels().get(1).getColumnElements());
        assertEquals(3, result.getColumnTreeModels().get(0).getColumnElements().size());
        assertEquals(2, result.getColumnTreeModels().get(1).getColumnElements().size());
        assertEquals("/1", result.getColumnTreeModels().get(0).getColumnElements().get(0).getParent());
        assertNull(result.getColumnTreeModels().get(0).getColumnElements().get(0).getChildren());
        assertEquals("/Cusomer/Name", result.getColumnTreeModels().get(0).getColumnElements().get(1).getxPath());
        assertEquals("Name", result.getColumnTreeModels().get(0).getColumnElements().get(1).getLabel());
        assertEquals("/1", result.getColumnTreeModels().get(0).getColumnElements().get(2).getParent());
        assertNotNull(result.getColumnTreeModels().get(0).getColumnElements().get(2).getChildren());
        assertEquals(2, result.getColumnTreeModels().get(0).getColumnElements().get(2).getChildren().size());
        assertEquals("/1/@children.2", result.getColumnTreeModels().get(0).getColumnElements().get(2).getChildren().get(0).getParent());
        assertEquals("/Customer/Address/zipCode", result.getColumnTreeModels().get(0).getColumnElements().get(2).getChildren().get(0).getxPath());
        assertEquals("zipCode", result.getColumnTreeModels().get(0).getColumnElements().get(2).getChildren().get(0).getLabel());
        assertTrue(result.getColumnTreeModels().get(0).getColumnElements().get(2).getChildren().get(1).getHtmlSnippet().equals(""));
        assertTrue(result.getColumnTreeModels().get(0).getColumnElements().get(2).getChildren().get(1).getLabelStyle().equals(""));
        assertTrue(result.getColumnTreeModels().get(0).getColumnElements().get(2).getChildren().get(1).getStyle().equals(""));
        assertTrue(result.getColumnTreeModels().get(0).getColumnElements().get(2).getChildren().get(1).getValueStyle().equals(""));
        assertEquals(2, result.getColumnTreeModels().get(1).getColumnElements().size());
        assertEquals("/2", result.getColumnTreeModels().get(1).getColumnElements().get(0).getParent());
        assertEquals("/Customer/picture", result.getColumnTreeModels().get(1).getColumnElements().get(0).getxPath());
        assertEquals("/2", result.getColumnTreeModels().get(1).getColumnElements().get(1).getParent());
        assertEquals("/Customer/country", result.getColumnTreeModels().get(1).getColumnElements().get(1).getxPath());
        
        xml = inputStream2String(this.getClass().getResourceAsStream("productform.xml"));
        doc = Util.parse(xml);
        root = doc.getDocumentElement();
        result = ViewHelper.builderLayout(root);
        assertNotNull(result);
        assertEquals(2, result.getColumnTreeModels().size());
        assertEquals(8, result.getColumnTreeModels().get(0).getColumnElements().size());
        assertEquals(3, result.getColumnTreeModels().get(1).getColumnElements().size());
        assertEquals("_4DuYgGKFEeG_l-qIWXZr4g", result.getColumnTreeModels().get(0).getColumnElements().get(0).getParent());
        assertNotNull(result.getColumnTreeModels().get(0).getColumnElements().get(3).getChildren());
        assertEquals("/Product/Features", result.getColumnTreeModels().get(0).getColumnElements().get(3).getxPath());
        assertEquals("a", result.getColumnTreeModels().get(1).getColumnElements().get(0).getLabelStyle());
        assertEquals("b", result.getColumnTreeModels().get(1).getColumnElements().get(0).getValueStyle());
        assertEquals("d", result.getColumnTreeModels().get(1).getColumnElements().get(1).getStyle());
        assertEquals("_4EnJUmKFEeG_l-qIWXZr4g", result.getColumnTreeModels().get(1).getColumnElements().get(1).getParent());
        assertNotNull(result.getColumnTreeModels().get(1).getColumnElements().get(2).getHtmlSnippet());
        assertNotSame(0, result.getColumnTreeModels().get(1).getColumnElements().get(2).getHtmlSnippet().trim().length());
        
        xml = inputStream2String(this.getClass().getResourceAsStream("Store.xml"));
        doc = Util.parse(xml);
        root = doc.getDocumentElement();
        result = ViewHelper.builderLayout(root);
        assertNotNull(result);
        assertEquals(2, result.getColumnTreeModels().size());
        assertEquals(5, result.getColumnTreeModels().get(0).getColumnElements().size());
        assertNull(result.getColumnTreeModels().get(1).getColumnElements());
        
        xml = inputStream2String(this.getClass().getResourceAsStream("MyTest.xml"));
        doc = Util.parse(xml);
        root = doc.getDocumentElement();
        result = ViewHelper.builderLayout(root);
        assertNotNull(result);
        assertEquals(3, result.getColumnTreeModels().size());
        assertNotNull(result.getColumnTreeModels().get(0).getColumnElements());
        assertNull(result.getColumnTreeModels().get(1).getColumnElements());
        assertNull(result.getColumnTreeModels().get(2).getColumnElements());

    }

    private String inputStream2String(InputStream is) {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            fail();
        }
        return buffer.toString();
    }
}
