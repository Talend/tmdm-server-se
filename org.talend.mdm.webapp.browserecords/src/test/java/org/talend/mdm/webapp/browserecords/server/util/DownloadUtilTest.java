/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.talend.mdm.webapp.base.server.util.XmlUtil;

@SuppressWarnings("nls")
public class DownloadUtilTest extends TestCase {

    private static final Logger LOG = Logger.getLogger(DownloadUtilTest.class);

    public void testAssembleFkMap() throws Exception {
        Map<String, String> colFkMap = new HashMap<String, String>();
        Map<String, List<String>> fkMap = new HashMap<String, List<String>>();
        String fkColXPath = "<fkColXPath><item>Person/Shop,Store/Id</item></fkColXPath>";
        String fkInfo = "<fkInfo><item>Store/Name</item></fkInfo>";
        DownloadUtil.assembleFkMap(colFkMap, fkMap, fkColXPath, fkInfo);

        assertEquals(1, colFkMap.size());
        assertEquals(1, fkMap.size());
        assertEquals("Person/Shop", colFkMap.keySet().iterator().next());
        assertEquals("Person/Shop", fkMap.keySet().iterator().next());
        assertEquals("Store/Id", colFkMap.get(colFkMap.keySet().iterator().next()));
        assertEquals("Store/Name", fkMap.get(fkMap.keySet().iterator().next()).get(0));

        colFkMap.clear();
        fkMap.clear();
        String fk1 = "Person/Shop";
        String fk2 = "Product/Famliy";
        fkColXPath = "<fkColXPath><item>" + fk1 + ",Store/Id</item><item>" + fk2 + ",ProductFamliy/Id</item></fkColXPath>";
        fkInfo = "<fkInfo><item>Store/Name</item><item>ProductFamliy/name,ProductFamliy/Code</item></fkInfo>";
        DownloadUtil.assembleFkMap(colFkMap, fkMap, fkColXPath, fkInfo);

        assertEquals(2, colFkMap.size());
        assertEquals(2, fkMap.size());
        assertEquals("Store/Id", colFkMap.get(fk1));
        assertEquals("ProductFamliy/Id", colFkMap.get(fk2));
        assertEquals("Store/Name", fkMap.get(fk1).get(0));
        assertEquals(2, fkMap.get(fk2).size());
        assertEquals("ProductFamliy/name", fkMap.get(fk2).get(0));
        assertEquals("ProductFamliy/Code", fkMap.get(fk2).get(1));
    }

    public void testConvertXml2Array() throws Exception {
        String xml = "<header><item>Id</item><item>Name</item><item>Family</item><item>Price</item><item>Availability</item></header>";
        String[] resultArray = DownloadUtil.convertXml2Array(xml, "item");
        assertEquals(5, resultArray.length);
        assertEquals("Family", resultArray[2]);
    }

    public void testIsJoinField() {
        String xPath = "Product/Id";
        String concept = "Product";
        boolean result = DownloadUtil.isJoinField(xPath, concept);
        assertTrue(result);

        xPath = "Product/Name";
        concept = "ProductFamily";
        result = DownloadUtil.isJoinField(xPath, concept);
        assertFalse(result);

        xPath = "ProductFamily/Name";
        concept = "productfamily";
        result = DownloadUtil.isJoinField(xPath, concept);
        assertTrue(result);
    }

    public void testGetJoinFieldValue() {
        InputStream is = DownloadUtilTest.class.getResourceAsStream("result.xml");
        Document doc = null;
        try {
            doc = XmlUtil.parse(is);
        } catch (DocumentException e) {
            LOG.error(e);
        }

        String value = DownloadUtil.getJoinFieldValue(doc, "ProductFamily/Name", 5);
        assertEquals("Mugs", value);
    }
}
