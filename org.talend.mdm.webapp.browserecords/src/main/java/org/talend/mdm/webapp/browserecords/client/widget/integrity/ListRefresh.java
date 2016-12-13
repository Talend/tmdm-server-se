/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.integrity;

import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel.ReLoadData;

/**
 * This implementation of {@link PostDeleteAction} refreshes the content of the item browser list (can be used
 * to remove the freshly deleted items from the view).
 */
public class ListRefresh implements PostDeleteAction {

    private final PostDeleteAction next;

    private ItemDetailToolBar bar;

    /**
     * If you don't know what to pass as <code>next</code> argument, check the constant {@link NoOpPostDeleteAction#INSTANCE}.
     * @param next The next action to be called once this action has just been performed.
     */
    public ListRefresh(PostDeleteAction next) {
        this.next = next;
    }

    public ListRefresh(ItemDetailToolBar bar, PostDeleteAction next) {
        this.bar = bar;
        this.next = next;
    }

    public void doAction() {
        // TMDM-3361, Hierarchy didn't need to reload
        // TMDM-3556, it didn't need to reload when delete FK in separate tab
        if (bar == null || (!bar.isHierarchyCall() && (ItemsListPanel.getInstance().getCurrentQueryModel() != null) && ItemsListPanel.getInstance().getCurrentQueryModel().getModel().getConceptName()
                .equals(bar.getItemBean().getConcept()))) {
            // Click "Open master record" from staging browser, then delete the master record, it shoudn't reload staging browser.
            if (ItemsListPanel.getInstance().getCurrentQueryModel().getDataClusterPK().contains("#STAGING") && bar != null && !bar.isStaging()) { //$NON-NLS-1$

            } else {
                // Reload
                ItemsListPanel.getInstance().reload(new ReLoadData() {

                    public void onReLoadData() {
                        next.doAction();
                    }
                });
            }
        } else if(bar.isHierarchyCall()){
            next.doAction();
        }
    }
}
