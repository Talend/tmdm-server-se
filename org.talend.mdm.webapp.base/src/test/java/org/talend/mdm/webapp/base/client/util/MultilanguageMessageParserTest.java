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

import junit.framework.TestCase;

public class MultilanguageMessageParserTest extends TestCase {

    public void testPickOutISOMessage() {
        // Sanity check
        String s = "[fr:f][en:e][zh:c]"; //$NON-NLS-1$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "en").equals("e")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "fr").equals("f")); //$NON-NLS-1$//$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "zh").equals("c")); //$NON-NLS-1$ //$NON-NLS-2$

        // Case-insensitive check
        s = "[FR:f][en:e][ZH:c]"; //$NON-NLS-1$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "EN").equals("e")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "fr").equals("f")); //$NON-NLS-1$//$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "ZH").equals("c")); //$NON-NLS-1$ //$NON-NLS-2$

        // Test backslash escaped ] and \ characters
        s = "[fr:f\\]f][en:e\\\\e][zh:c\\]c\\]]"; //$NON-NLS-1$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "en").equals("e\\e")); //$NON-NLS-1$//$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "fr").equals("f]f")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "zh").equals("c]c]")); //$NON-NLS-1$//$NON-NLS-2$

        // Test default to English if language code not present and english is
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "sp").equals("e\\e")); //$NON-NLS-1$ //$NON-NLS-2$

        // Test default to whole string when no English
        s = "[fr:f\\]f][zh:c\\]c\\]]"; //$NON-NLS-1$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "sp").equals("[fr:f\\]f][zh:c\\]c\\]]")); //$NON-NLS-1$ //$NON-NLS-2$

        // Testing being able to pick out language strings
        s = "dddd[fr:f\\]f]dddd[en:e\\\\e]ddd[zh:c\\]c\\]]dddd"; //$NON-NLS-1$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "en").equals("e\\e")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "fr").equals("f]f")); //$NON-NLS-1$//$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "zh").equals("c]c]")); //$NON-NLS-1$ //$NON-NLS-2$

        // Testing being able to skip malformed country codes
        s = "dddd[french:f\\]f]dddd[en:e\\\\e]ddd[zh:c\\]c\\]]dddd"; //$NON-NLS-1$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "en").equals("e\\e")); //$NON-NLS-1$//$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "fr").equals("e\\e")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "zh").equals("c]c]")); //$NON-NLS-1$//$NON-NLS-2$

        // Testing special characters outside of language specific messages
        s = "dd\\\\dd[fr:f\\]f]dd\\[ddd[en:e\\\\e]dd[[d[zh:c\\]c\\]]dddd"; //$NON-NLS-1$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "en").equals("e\\e")); //$NON-NLS-1$ //$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "fr").equals("f]f")); //$NON-NLS-1$//$NON-NLS-2$
        assertTrue(MultilanguageMessageParser.pickOutISOMessage(s, "zh").equals("c]c]")); //$NON-NLS-1$ //$NON-NLS-2$
        
        //Testing null
        s = null;
        assertNull(MultilanguageMessageParser.pickOutISOMessage(s, "en")); //$NON-NLS-1$
        
        //Testing empty string
        s = ""; //$NON-NLS-1$
        assertNotNull(MultilanguageMessageParser.pickOutISOMessage(s, "en")); //$NON-NLS-1$
        assertEquals(s, MultilanguageMessageParser.pickOutISOMessage(s, "en")); //$NON-NLS-1$
        
        //Testing messy string
        s= "arhrnltkdzngds"; //$NON-NLS-1$
        assertNotNull(MultilanguageMessageParser.pickOutISOMessage(s, "en")); //$NON-NLS-1$
        assertEquals(s, MultilanguageMessageParser.pickOutISOMessage(s, "en")); //$NON-NLS-1$
    }

    @SuppressWarnings("nls")
    public void testGetMultiLanguageValueByLanguage() {
        // no [en:+][fr:+]... format
        String multiLanguageString = "Talend";
        assertTrue(MultilanguageMessageParser.getValueByLanguage(multiLanguageString, "EN").equals(multiLanguageString));
        assertFalse(MultilanguageMessageParser.isExistMultiLanguageFormat(multiLanguageString));
        // normal format lowercase language
        multiLanguageString = "[ZH:拓蓝][EN:Tal&amp;#92;end&amp;#93;&amp;#92;][FR:Talend Company]";
        assertTrue(MultilanguageMessageParser.getValueByLanguage(multiLanguageString, "ZH").equals("拓蓝"));
        assertTrue(MultilanguageMessageParser.getValueByLanguage(multiLanguageString, "EN").equals("Tal\\end]\\"));
        assertTrue(MultilanguageMessageParser.getValueByLanguage(multiLanguageString, "FR").equals("Talend Company"));
        assertTrue(MultilanguageMessageParser.isExistMultiLanguageFormat(multiLanguageString));
        // test null
        multiLanguageString = "[ZH:拓蓝][FR:Talend Company&amp;#93;]";
        assertNull(MultilanguageMessageParser.getValueByLanguage(multiLanguageString, "EN"));
        assertTrue(MultilanguageMessageParser.isExistMultiLanguageFormat(multiLanguageString));
        assertTrue(MultilanguageMessageParser.getValueByLanguage(multiLanguageString, "FR").equals("Talend Company]"));

    }

    @SuppressWarnings("nls")
    public void testGetFormatValueByDefaultLanguage() {
        String defaultLanguage = "en";
        String value = "Talend";
        assertEquals("[EN:Talend]", MultilanguageMessageParser.getFormatValueByDefaultLanguage(value, defaultLanguage));
        defaultLanguage = "fr";
        assertEquals("[FR:Talend]", MultilanguageMessageParser.getFormatValueByDefaultLanguage(value, defaultLanguage));
        value = "Talend\\[]";
        assertEquals("[FR:Talend&amp;#92;&amp;#91;&amp;#93;]",
                MultilanguageMessageParser.getFormatValueByDefaultLanguage(value, defaultLanguage));
    }

}