/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client;

import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.Window;


public class GenerateContainer {

    private static ContentPanel instance;

    public static void generateContentPanel() {
        if (instance != null) {
            instance.removeFromParent();
        }

        instance = new ContentPanel() {

            @Override
            public void onAttach() {
                monitorWindowResize = true;
                Window.enableScrolling(true);
                // setSize(Window.getClientWidth(), Window.getClientHeight());
                super.onAttach();
                GXT.hideLoadingPanel("loading");//$NON-NLS-1$
            }

            // protected void onWindowResize(int width, int height) {
            // setSize(width, height);
            // this.doLayout(true);
            // }
        };
        instance.setId(WelcomePortal.WELCOMEPORTAL_ID);
        instance.setHeaderVisible(false);
        instance.setHeading(MessagesFactory.getMessages().welcome_title());

    }

    public static ContentPanel getContentPanel() {
        return instance;
    }
}
