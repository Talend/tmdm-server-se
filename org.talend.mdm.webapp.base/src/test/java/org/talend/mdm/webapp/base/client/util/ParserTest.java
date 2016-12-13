/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.util;

import org.talend.mdm.webapp.base.client.model.Criteria;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class ParserTest extends TestCase {

    public void testParseString() throws Exception {
        Criteria result = null;
        String testString = "((Test/Id EQUALS *) AND (Test/Id EQUALS *) AND (Test/Id EQUALS *))";

        result = Parser.parse(testString);
        assertEquals(testString, result.toString());
    }
}
