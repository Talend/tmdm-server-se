/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.history;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class UniqueIdTransformerTest extends TestCase {

    public void testAddIds() throws Exception {        
        String beforeXML = file2String(this.getClass().getResourceAsStream("before.xml"));
        Document beforeDoc = Util.parse(beforeXML);
        assertNotNull(beforeDoc);
        
        String afterXML = file2String(this.getClass().getResourceAsStream("after.xml"));
        Document afterDoc = Util.parse(afterXML);
        assertNotNull(afterDoc);
        
        UniqueIdTransformer utf = new UniqueIdTransformer();
        utf.addIds(beforeDoc);
        assertNotNull(beforeDoc);

        utf.addIds(afterDoc);
        assertNotNull(afterDoc);
        
        testIdByXpath(beforeDoc, afterDoc, "/ii/p/Product");
        
        testIdByXpath(beforeDoc, afterDoc, "/ii/p/Product/Features");
        
        testIdByXpath(beforeDoc, afterDoc, "/ii/p/Product/Features/Sizes");
        
        testIdByXpath(beforeDoc, afterDoc, "/ii/p/Product/Features/Colors");
        
    }
    
    private void testIdByXpath(Document beforeDoc, Document afterDoc, String xpath){
        NodeList beforeList = null;
        try {
            beforeList = Util.getNodeList(beforeDoc, xpath);
        } catch (XtentisException e) {
            fail();
        }
        assertNotNull(beforeList);
        assertEquals(1, beforeList.getLength());
        
        NodeList afterList = null;
        try {
            afterList = Util.getNodeList(afterDoc, xpath);
        } catch (XtentisException e) {
            fail();
        }
        assertNotNull(afterList);
        assertEquals(1, afterList.getLength());
        
        assertEquals(beforeList.item(0).getAttributes().getNamedItem("id").getNodeValue(), afterList.item(0).getAttributes().getNamedItem("id").getNodeValue());
    }
    
    private String file2String(InputStream is) {
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