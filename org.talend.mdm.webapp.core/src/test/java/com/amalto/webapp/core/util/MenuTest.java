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

import java.util.HashMap;

import junit.framework.TestCase;

import com.amalto.core.webservice.WSMenuEntry;
import com.amalto.core.webservice.WSMenuMenuEntriesDescriptions;

@SuppressWarnings("nls")
public class MenuTest extends TestCase {

    private HashMap<String, Menu> menuIndex = new HashMap<String, Menu>();

    private String language = "en";

    private String language_fr = "fr";

    private String language_es = "es";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        WSMenuMenuEntriesDescriptions[] home_descriptions = new WSMenuMenuEntriesDescriptions[3];
        home_descriptions[0] = new WSMenuMenuEntriesDescriptions("EN", "home");
        home_descriptions[1] = new WSMenuMenuEntriesDescriptions("FR", "maison");
        home_descriptions[2] = new WSMenuMenuEntriesDescriptions("ES", "casa");
        WSMenuEntry[] subMenus = new WSMenuEntry[2];
        WSMenuMenuEntriesDescriptions[] welcome_descriptions = new WSMenuMenuEntriesDescriptions[3];
        welcome_descriptions[0] = new WSMenuMenuEntriesDescriptions("EN", "welcome");
        welcome_descriptions[1] = new WSMenuMenuEntriesDescriptions("FR", "accueil");
        welcome_descriptions[2] = new WSMenuMenuEntriesDescriptions("ES", "bienvenida");
        WSMenuMenuEntriesDescriptions[] browser_descriptions = new WSMenuMenuEntriesDescriptions[3];
        browser_descriptions[0] = new WSMenuMenuEntriesDescriptions("EN", "Data Browser");
        browser_descriptions[1] = new WSMenuMenuEntriesDescriptions("FR", "Accès aux données");
        browser_descriptions[2] = new WSMenuMenuEntriesDescriptions("ES", "Datos Navegador");
        subMenus[0] = new WSMenuEntry("Welcome", welcome_descriptions, "Welcome", "Welcome", null, null);
        subMenus[1] = new WSMenuEntry("Browser", browser_descriptions, "Browser", "Browser", null, null);
        WSMenuEntry menuEntry = new WSMenuEntry("Home", home_descriptions, "Home", "Home", null, subMenus);
        Menu.wsMenu2Menu(menuIndex, menuEntry, null, "", 0);
    }

    public void test_getMenuLabel() {
        assertNotNull(menuIndex);
        assertEquals(3, menuIndex.size());
        // Home Menu
        Menu menu = menuIndex.get("Home");
        assertEquals("home", menu.getLabels().get(language));
        assertEquals("maison", menu.getLabels().get(language_fr));
        assertEquals("casa", menu.getLabels().get(language_es));
        // Welcome Menu
        menu = menuIndex.get("Welcome");
        assertEquals("welcome", menu.getLabels().get(language));
        assertEquals("accueil", menu.getLabels().get(language_fr));
        assertEquals("bienvenida", menu.getLabels().get(language_es));
        // Browser Menu
        menu = menuIndex.get("Browser");
        assertEquals("Data Browser", menu.getLabels().get(language));
        assertEquals("Accès aux données", menu.getLabels().get(language_fr));
        assertEquals("Datos Navegador", menu.getLabels().get(language_es));
    }

}