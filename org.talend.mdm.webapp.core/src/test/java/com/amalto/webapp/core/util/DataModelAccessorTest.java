/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.util;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import com.amalto.commons.core.utils.XMLUtils;

/**
 * created by talend2 on 2013-6-13
 * Detailled comment
 *
 */
public class DataModelAccessorTest extends TestCase {
    
    private static final List<String> ROLES = Arrays.asList("System_Admin"); //$NON-NLS-1$

    public void testCheckRestoreAccessHelper() throws Exception {
        String modelXSD = getXSDModel("DataModelAccessorTest.xsd"); //$NON-NLS-1$
        assertFalse(DataModelAccessor.getInstance().checkRestoreAccess(modelXSD, "M26_E01", ROLES)); //$NON-NLS-1$
        assertTrue(DataModelAccessor.getInstance().checkRestoreAccess(modelXSD, "M26_E02", ROLES)); //$NON-NLS-1$
        assertFalse(DataModelAccessor.getInstance().checkRestoreAccess(modelXSD, "M26_E03", ROLES)); //$NON-NLS-1$
        assertFalse(DataModelAccessor.getInstance().checkRestoreAccess(modelXSD, "M26_E04", ROLES)); //$NON-NLS-1$
    }

    public void testCheckReadAccessHelper() throws Exception {
        String modelXSD = getXSDModel("DataModelAccessorTest.xsd"); //$NON-NLS-1$
        assertTrue(DataModelAccessor.getInstance().checkReadAccess(modelXSD, "M26_E01", ROLES)); //$NON-NLS-1$
        assertTrue(DataModelAccessor.getInstance().checkReadAccess(modelXSD, "M26_E02", ROLES)); //$NON-NLS-1$
        assertFalse(DataModelAccessor.getInstance().checkReadAccess(modelXSD, "M26_E03", ROLES)); //$NON-NLS-1$
        assertFalse(DataModelAccessor.getInstance().checkReadAccess(modelXSD, "M26_E04", ROLES)); //$NON-NLS-1$
    }

    private String getXSDModel(String filename) throws Exception {
        InputStream is = getClass().getResourceAsStream(filename);
        assertNotNull(is);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
        return XMLUtils.nodeToString(doc, true, true);
    }

}
