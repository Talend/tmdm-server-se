/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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

    public void testLocaleValue() {
        //case 1
        String language = "en";
        String str1 = "[FR:Produit avec Magasins][EN:Product with Stores]";
        String results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("Product with Stores", results);

        //case 2
        language = "zh";
        str1 = "[FR:Produit avec Magasins][EN:Product with Stores]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[FR:Produit avec Magasins][EN:Product with Stores]", results);

        //case 3
        language = "ar";
        str1 = "[FR:Produit avec Magasins][EN:Product with Stores]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[FR:Produit avec Magasins][EN:Product with Stores]", results);

        //case 5
        language = "fr";
        str1 = "";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("", results);

        //case 6
        language = "fr";
        str1 = "[FR:Produit avec Magasins]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("Produit avec Magasins", results);
        
        //case 7
        language = "";
        str1 = "[FR:Produit avec Magasins]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[FR:Produit avec Magasins]", results);
        
        //case 8
        language = "";
        str1 = "[EN:Product with Stores]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[EN:Product with Stores]", results);

        //case 9
        language = "";
        str1 = "";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("", results);

        //case 10
        language = "zh_CN";
        str1 = "[FR:Produit avec Magasins][EN:]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[FR:Produit avec Magasins][EN:]", results);

        //case 11
        language = "zh_CN,en";
        str1 = "[FR:Produit avec Magasins][EN:Product with Stores]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[FR:Produit avec Magasins][EN:Product with Stores]", results);

        //case 12
        language = "fr_FR";
        str1 = "[FR:Produit avec Magasins][EN:Product with Stores]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[FR:Produit avec Magasins][EN:Product with Stores]", results);

        //case 13
        language = "fr_FR_en";
        str1 = "[FR:Produit avec Magasins][EN:Product with Stores]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[FR:Produit avec Magasins][EN:Product with Stores]", results);

        //case 14
        language = "_________";
        str1 = "[FR:Produit avec Magasins][EN:Product with Stores]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[FR:Produit avec Magasins][EN:Product with Stores]", results);

        //case 15
        language = "_________fr";
        str1 = "[FR:Produit avec Magasins][EN:Product with Stores]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[FR:Produit avec Magasins][EN:Product with Stores]", results);

        //case 16
        language = "fr_________";
        str1 = "[FR:Produit avec Magasins][EN:Product with Stores]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[FR:Produit avec Magasins][EN:Product with Stores]", results);
    }
}