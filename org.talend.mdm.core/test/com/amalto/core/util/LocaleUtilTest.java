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
public class LocaleUtilTest extends TestCase {

    public void testNormalLocaleValue() {
        String str1 = "[FR:Produit avec Magasins][EN:Product with Stores][ZH:Zhong Wen]";

        //case 1
        String results = LocaleUtil.getLocaleValue(str1, "en");
        assertEquals("Product with Stores", results);

        //case 2
        results = LocaleUtil.getLocaleValue(str1, "FR");
        assertEquals("Produit avec Magasins", results);

        //case 3
        results = LocaleUtil.getLocaleValue(str1, "zh");
        assertEquals("Zhong Wen", results);

        //case 4
        results = LocaleUtil.getLocaleValue(str1, "zh_CN");
        assertEquals(str1, results);

        //case 5
        results = LocaleUtil.getLocaleValue(str1, "");
        assertEquals(str1, results);

        results = LocaleUtil.getLocaleValue(str1, null);
        assertEquals(str1, results);

        //case 6
        results = LocaleUtil.getLocaleValue(str1, "xx");
        assertEquals(str1, results);

        //case 7
        results = LocaleUtil.getLocaleValue("", "en");
        assertEquals("", results);

        //case 8
        results = LocaleUtil.getLocaleValue("", "");
        assertEquals("", results);

        //case 9
        results = LocaleUtil.getLocaleValue("", "xx");
        assertEquals("", results);
    }

    public void testLocaleValueWithSpecialCharacters() {
        String str1 = "[FR:[Org\\]Produit avec Magasins][EN:[Org\\]Product with Stores][ZH:Zhong Wen]";

        //case 1
        String results = LocaleUtil.getLocaleValue(str1, "EN");
        assertEquals("[Org]Product with Stores", results);

        //case 2
        results = LocaleUtil.getLocaleValue(str1, "FR");
        assertEquals("[Org]Produit avec Magasins", results);

        //case 3
        results = LocaleUtil.getLocaleValue(str1, "zh");
        assertEquals("Zhong Wen", results);
    }
}