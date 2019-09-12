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
        String results = MultilanguageMessageParser.pickOutISOMessage(str1, "en");
        assertEquals("Product with Stores", results);

        //case 2
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "FR");
        assertEquals("Produit avec Magasins", results);

        //case 3
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "zh");
        assertEquals("Zhong Wen", results);

        //case 4
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "zh_CN");
        assertEquals("Product with Stores", results);

        //case 5
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "");
        assertEquals("Product with Stores", results);

        //case 6
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "xx");
        assertEquals("Product with Stores", results);

        //case 7
        results = MultilanguageMessageParser.pickOutISOMessage("", "en");
        assertEquals("", results);

        //case 8
        results = MultilanguageMessageParser.pickOutISOMessage("", "");
        assertEquals("", results);

        //case 9
        results = MultilanguageMessageParser.pickOutISOMessage("", "xx");
        assertEquals("", results);
    }

    public void testNoEnglishLocaleValue() {
        String str1 = "[FR:Produit avec Magasins][DE:Product with Stores][ZH:Zhong Wen]";

        //case 1
        String results = MultilanguageMessageParser.pickOutISOMessage(str1, "en");
        assertEquals(str1, results);

        //case 2
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "FR");
        assertEquals("Produit avec Magasins", results);

        //case 3
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "zh");
        assertEquals("Zhong Wen", results);

        //case 4
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "zh_CN");
        assertEquals(str1, results);

        //case 5
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "");
        assertEquals(str1, results);

        //case 6
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "xx");
        assertEquals(str1, results);
    }

    public void testLocaleValueWithSpecialCharacters() {
        String str1 = "[FR:[Org\\]Produit avec Magasins][EN:[Org\\]Product with Stores][ZH:Zhong Wen]";

        //case 1
        String results = MultilanguageMessageParser.pickOutISOMessage(str1, "EN");
        assertEquals("[Org]Product with Stores", results);

        //case 2
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "FR");
        assertEquals("[Org]Produit avec Magasins", results);

        //case 3
        results = MultilanguageMessageParser.pickOutISOMessage(str1, "zh");
        assertEquals("Zhong Wen", results);
    }
}