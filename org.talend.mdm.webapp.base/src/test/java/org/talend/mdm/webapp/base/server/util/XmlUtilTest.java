/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server.util;

import java.io.InputStream;

import junit.framework.TestCase;

import org.dom4j.Document;
import org.dom4j.Element;

@SuppressWarnings("nls")
public class XmlUtilTest extends TestCase {

    public void testParseText() throws Exception {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream("temp_ColumnTreeLayout.xml");
        org.dom4j.Document result = XmlUtil.parse(is);
        // each qualified xml should contain this element
        Element panel = (Element) result.selectSingleNode("//mdmform:Panel");
        assertTrue(panel.asXML().indexOf("mdmform:Panel") != -1);
    }

    public void testCompleteXMLByXPath() {
        String xmlStr = "<ProductFamily><Id>5</Id><Name>Puma</Name></ProductFamily>";
        String xPath = "ProductFamily/FamilySet";
        Document doc = null;
        try {
            doc = XmlUtil.parseText(xmlStr);
        } catch (Exception e) {
            fail();
        }
        XmlUtil.completeXMLByXPath(doc, xPath);
        assertNotNull(doc);
        assertNotNull(doc.asXML());
        assertTrue(doc.asXML().contains("FamilySet"));
    }
}
