/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.server.actions;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class WelcomePortalActionTest extends TestCase {

    private WelcomePortalAction action = new WelcomePortalAction();

    public void testSortProcessMap() throws Exception {
        Map<String, String> processMap = new HashMap<>();

        processMap.put("Runnable#AddAndMatchProducts", "Runnable#Add new products");
        processMap.put("Runnable#AddResolvedProductsInMDM", "Load resolved products");
        processMap.put("Runnable#LoadAll", "Load all");
        processMap.put("Runnable#add", "Add Product");
        processMap.put("Runnable#CallBack", "Runnable#CallBack template process");
        processMap.put("Runnable#BacktoPrevious", "Back to Previous");

        Map<String, String> result = action.sortProcess(processMap);

        int i = 1;
        for (Map.Entry<String, String> entry : result.entrySet()) {
            if (i == 1) {
                assertEquals("Runnable#add", entry.getKey());
                assertEquals("Add Product", entry.getValue());
            } else if (i == 2) {
                assertEquals("Runnable#AddAndMatchProducts", entry.getKey());
                assertEquals("Runnable#Add new products", entry.getValue());
            } else if (i == 3) {
                assertEquals("Runnable#BacktoPrevious", entry.getKey());
                assertEquals("Back to Previous", entry.getValue());
            } else if (i == 4) {
                assertEquals("Runnable#CallBack", entry.getKey());
                assertEquals("Runnable#CallBack template process", entry.getValue());
            } else if (i == 5) {
                assertEquals("Runnable#LoadAll", entry.getKey());
                assertEquals("Load all", entry.getValue());
            } else if (i == 6) {
                assertEquals("Runnable#AddResolvedProductsInMDM", entry.getKey());
                assertEquals("Load resolved products", entry.getValue());
            }
            i++;
        }
    }
}
