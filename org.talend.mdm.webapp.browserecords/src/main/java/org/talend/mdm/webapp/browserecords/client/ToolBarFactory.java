/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client;

import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

public abstract class ToolBarFactory {

    private static ToolBarFactory impl;

    protected ToolBarFactory() {

    }

    public static void initialize(ToolBarFactory factoryImpl) {
        ToolBarFactory.impl = factoryImpl;
    }

    public static ToolBarFactory getInstance() {
        if (impl == null) {
            impl = new DefaultToolBarFactoryImpl();
        }
        return impl;
    }

    public abstract ItemDetailToolBar createItemDetailToolBar(ItemsDetailPanel itemsDetailPanel);

    public abstract ItemDetailToolBar createItemDetailToolBar(ItemBean itemBean, String operation, ViewBean viewBean,
            ItemsDetailPanel itemsDetailPanel);

    public abstract ItemDetailToolBar createItemDetailToolBar(ItemBean itemBean, String operation, ViewBean viewBean,
            ItemsDetailPanel itemsDetailPanel, boolean openTab);

    public abstract ItemDetailToolBar createItemDetailToolBar(ItemBean itemBean, String operation, boolean isFkToolBar,
            ViewBean viewBean, ItemsDetailPanel itemsDetailPanel);

    public abstract ItemDetailToolBar createItemDetailToolBar(boolean isStaging, ItemsDetailPanel itemsDetailPanel);

    public abstract ItemDetailToolBar createItemDetailToolBar(boolean isStaging, ItemBean itemBean, String operation,
            ViewBean viewBean, ItemsDetailPanel itemsDetailPanel);

    public abstract ItemDetailToolBar createItemDetailToolBar(boolean isStaging, ItemBean itemBean, String operation,
            boolean isFkToolBar, ViewBean viewBean, ItemsDetailPanel itemsDetailPanel, boolean openTab);

}
