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
        language = "fr";
        str1 = "";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("", results);

        //case 4
        language = "fr";
        str1 = "[FR:Produit avec Magasins]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("Produit avec Magasins", results);
        
        //case 5
        language = "";
        str1 = "[FR:Produit avec Magasins]";
        results = LocaleUtil.getLocaleValue(str1, language);
        assertEquals("[FR:Produit avec Magasins]", results);
    }
}