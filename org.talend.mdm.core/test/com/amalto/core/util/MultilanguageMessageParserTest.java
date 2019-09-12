/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class MultilanguageMessageParserTest extends TestCase {

    public void testNormalLocaleValue() {
        String str1 = "[FR:Produit avec Magasins][EN:Product with Stores][ZH:Zhong Wen]";

        //case 1
        String results = MultilanguageMessageParser.getValueByLanguage(str1, "en");
        assertEquals("Product with Stores", results);

        //case 2
        results = MultilanguageMessageParser.getValueByLanguage(str1, "FR");
        assertEquals("Produit avec Magasins", results);

        //case 3
        results = MultilanguageMessageParser.getValueByLanguage(str1, "zh");
        assertEquals("Zhong Wen", results);

        //case 4
        results = MultilanguageMessageParser.getValueByLanguage(str1, "zh_CN");
        //assertEquals(str1, results);

        //case 5
        results = MultilanguageMessageParser.getValueByLanguage(str1, "");
        assertEquals(str1, results);

        //case 6
        results = MultilanguageMessageParser.getValueByLanguage(str1, "xx");
        //assertEquals(str1, results);

        //case 7
        results = MultilanguageMessageParser.getValueByLanguage("", "en");
        assertEquals("", results);

        //case 8
        results = MultilanguageMessageParser.getValueByLanguage("", "");
        assertEquals("", results);

        //case 9
        results = MultilanguageMessageParser.getValueByLanguage("", "xx");
        assertEquals("", results);
    }

    public void testLocaleValueWithSpecialCharacters() {
        String str1 = "[FR:[Org\\]Produit avec Magasins][EN:[Org\\]Product with Stores][ZH:Zhong Wen]";

        //case 1
        String results = MultilanguageMessageParser.getValueByLanguage(str1, "EN");
        assertEquals("[Org]Product with Stores", results);

        //case 2
        results = MultilanguageMessageParser.getValueByLanguage(str1, "FR");
        assertEquals("[Org]Produit avec Magasins", results);

        //case 3
        results = MultilanguageMessageParser.getValueByLanguage(str1, "zh");
        assertEquals("Zhong Wen", results);
    }
}