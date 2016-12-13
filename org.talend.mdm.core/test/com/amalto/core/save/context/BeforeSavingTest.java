/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save.context;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class BeforeSavingTest extends TestCase {

    public void testValidateFormat() throws Exception {
        BeforeSaving bs = new BeforeSaving(null);

        String message = "<report type=\"info\">[EN:1111][FR:222]</report1>";
        assertFalse(bs.validateFormat(message));

        message = "<report type=\"info\">[EN:1111][FR:222]</report>";
        assertFalse(bs.validateFormat(message));

        message = "<report><message>[EN:1111][FR:222]</message></report>";
        assertFalse(bs.validateFormat(message));

        message = "<report><message type=\"warning\">[EN:1111][FR:222]</message></report>";
        assertFalse(bs.validateFormat(message));

        message = "<report><message type=\"info\"><aaa/></message></report>";
        assertFalse(bs.validateFormat(message));

        message = "<report><message type=\"info\"/></report>";
        assertTrue(bs.validateFormat(message));

        message = "<report><MessagE type=\"info\">[EN:1111][FR:222]</message></report>";
        assertTrue(bs.validateFormat(message));

        message = "<results><item><attr><report><message type=\"info\">[FR:Produit validé][EN:Product validation OK]</message></report></attr></item></results>";
        assertTrue(bs.validateFormat(message));

        message = "<report aaa=\"bbb\"><message type=\"info\">[FR:Produit validé][EN:Product validation OK]</message></report>";
        assertTrue(bs.validateFormat(message));
    }
}
