/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.model;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class MultiLanguageModelTest extends TestCase {

    private MultiLanguageModel model;

    private String multiLanguageString = "[EN:456][FR:789][ZH:123&amp;#92;&amp;#91;&amp;#93;]";

    protected void setUp() throws Exception {
        model = new MultiLanguageModel(multiLanguageString);
    }

    public void testGetValueByLanguage() {
        assertEquals("123\\[]", model.getValueByLanguage("ZH"));
        assertEquals("456", model.getValueByLanguage("EN"));
        assertEquals("789", model.getValueByLanguage("FR"));
        assertEquals("", model.getValueByLanguage("SP"));
    }

    public void testSetValue() {
        // modify chinese value
        assertEquals("123\\[]", model.getValueByLanguage("ZH"));
        model.setValueByLanguage("ZH", "123456\\[]");
        assertEquals("123456\\[]", model.getValueByLanguage("ZH"));
        // modify english value
        assertEquals("456", model.getValueByLanguage("EN"));
        model.setValueByLanguage("EN", "456789");
        assertEquals("456789", model.getValueByLanguage("EN"));
        // modify french value
        assertEquals("789", model.getValueByLanguage("FR"));
        model.setValueByLanguage("FR", "789012");
        assertEquals("789012", model.getValueByLanguage("FR"));
        // add spanish value
        assertEquals("", model.getValueByLanguage("SP"));
        model.setValueByLanguage("SP", "ok");
        assertEquals("ok", model.getValueByLanguage("SP"));
        assertTrue(model.toString().equals("[EN:456789][FR:789012][SP:ok][ZH:123456&amp;#92;&amp;#91;&amp;#93;]"));
        // remove spanish value
        model.setValueByLanguage("SP", "");
        assertTrue(model.toString().equals("[EN:456789][FR:789012][ZH:123456&amp;#92;&amp;#91;&amp;#93;]"));
        // multiLanguageString = null
        model = new MultiLanguageModel(null);
        model.setValueByLanguage("ZH", "123");
        assertEquals("[ZH:123]", model.toString());
        // multiLanguageString == currentLanguageValue
        model = new MultiLanguageModel("Talend");
        assertEquals("Talend", model.getValueByLanguage("EN"));
        // modify value
        model.setValueByLanguage("EN", "Talend Company");
        assertEquals("[EN:Talend Company]", model.toString());
        // set value is empty
        model.setValueByLanguage("EN", "");
        assertTrue(model.toString().isEmpty());
    }

}