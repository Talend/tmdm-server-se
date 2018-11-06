/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.recyclebin.server.actions;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;

import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Document;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class UtilTest extends TestCase {

    private String getXSDModel(String filename) throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream(filename);
        assertNotNull(is);
        DocumentBuilder builder = MDMXMLUtils.getDocumentBuilder().get();
        Document doc = builder.parse(is);
        String XSDModel = com.amalto.core.util.Util.nodeToString(doc);
        return XSDModel;
    }

    public void testGetItemNameByProjection() throws Exception {
        String conceptName = "Product";
        InputStream xmlStream = getClass().getClassLoader().getResourceAsStream("ProductData.xml");
        String projection = getStringFromInputStream(xmlStream);
        String modelXSD = getXSDModel("ProductOne.xsd");
        InputStream is = new ByteArrayInputStream(modelXSD.getBytes("UTF-8"));
        MetadataRepository repository = new MetadataRepository();
        repository.load(is);
        String language = "en";
        // 1. the firstPrimaryKeyInfo is Product/Name(String type)
        String[] values = Util.getItemNameByProjection(conceptName, projection, repository, language);
        assertNotNull(values);
        assertEquals("Talend Golf Shirt", values[0]);
        assertEquals("Product", values[1]);
        // 2. the firstPrimaryKeyInfo is Product/Description(MultiLingual type)
        modelXSD = getXSDModel("ProductTwo.xsd");
        is = new ByteArrayInputStream(modelXSD.getBytes("UTF-8"));
        repository = new MetadataRepository();
        repository.load(is);
        values = Util.getItemNameByProjection(conceptName, projection, repository, language);
        assertNotNull(values);
        assertEquals("Talend Shirt", values[0]);
        assertEquals("Product", values[1]);
    }

    private String getStringFromInputStream(InputStream in) throws IOException {
        int total = in.available();
        byte[] buf = new byte[total];
        in.read(buf);
        return new String(buf);
    }
}
