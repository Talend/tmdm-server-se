/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client;

import org.talend.mdm.webapp.journal.client.i18n.MessagesFactory;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.Window;

/**
 * DOC Administrator  class global comment. Detailled comment
 */
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
                super.onAttach();
                GXT.hideLoadingPanel("loading");//$NON-NLS-1$
            }
        };

        instance.setId(Journal.JOURNAL_ID);
        instance.setHeading(MessagesFactory.getMessages().journal_title());
    }
    
    public static ContentPanel getContentPanel() {
        return instance;
    }
}
