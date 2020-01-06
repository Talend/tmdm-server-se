/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.schema.validation.SkipAttributeDocumentBuilder;
import com.amalto.core.util.Util;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class DOMDocumentTest extends TestCase {

    public void testExportToString() throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(DOMDocumentTest.class.getResourceAsStream("test1.xml")));
        String line;
        String xml = "";
        while ((line = in.readLine()) != null) {
            xml += line;
        }
        DOMDocument doc = new DOMDocument(Util.parse(xml), null, StringUtils.EMPTY, StringUtils.EMPTY);
        assertNotNull(doc);
        assertNotNull(doc.exportToString());
        assertFalse(doc.exportToString().contains("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
    }

    public void testIncludeXSINamespace() throws Exception {
        String lineSeparator = System.getProperty("line.separator");
        StringBuilder xmlBuilder = new StringBuilder("<Organisation xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">");
        xmlBuilder.append(lineSeparator);
        xmlBuilder.append("<IdOrganisation xsi:type=\"xsd:string\">5797</IdOrganisation>");
        xmlBuilder.append(lineSeparator);
        xmlBuilder.append("</Organisation>");
        xmlBuilder.append(lineSeparator);
        String xml = xmlBuilder.toString();
        InputStream documentStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
        // Parsing
        MutableDocument userDocument;
        try {
            // Don't ignore talend internal attributes when parsing this document
            DocumentBuilder documentBuilder = new SkipAttributeDocumentBuilder(MDMXMLUtils.getDocumentBuilderWithNamespace().get(), false);
            InputSource source = new InputSource(documentStream);
            Document userDomDocument = documentBuilder.parse(source);
            userDocument = new DOMDocument(userDomDocument, null, StringUtils.EMPTY, StringUtils.EMPTY);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse document to save.", e);
        }
        assertNotNull(userDocument);
        String result = userDocument.exportToString();
        assertEquals(xml, result);
    }

}
