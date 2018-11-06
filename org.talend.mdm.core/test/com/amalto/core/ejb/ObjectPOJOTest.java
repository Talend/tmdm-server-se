/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.ejb;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;

import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class ObjectPOJOTest extends TestCase {

    public void testParsingTotalCount() throws Exception {
        DocumentBuilder builder = MDMXMLUtils.getDocumentBuilder().get();
        String string = "<totalCount>100</totalCount>";
        // rebuild IDs
        Document doc = builder.parse(new InputSource(new StringReader(string)));
        assertEquals("100", doc.getDocumentElement().getTextContent());
    }

}
