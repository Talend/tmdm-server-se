/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.recyclebin.client;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.recyclebin.shared.ItemsTrashItem;

@SuppressWarnings("nls")
public class FakeData {

    private static List<ItemsTrashItem> items = new ArrayList<ItemsTrashItem>();
    static {
        items.add(new ItemsTrashItem("Product", "Product", "000", "", "", "", "000",  "", ""));
        items.add(new ItemsTrashItem("Product", "Product", "111", "", "", "", "111",  "", ""));
        items.add(new ItemsTrashItem("Product", "Product", "222", "", "", "", "222",  "", ""));
        items.add(new ItemsTrashItem("Product", "Product", "333", "", "", "", "333",  "", ""));
        items.add(new ItemsTrashItem("Product", "Product", "444", "", "", "", "444",  "", ""));
        items.add(new ItemsTrashItem("Product", "Product", "555", "", "", "", "555",  "", ""));
        items.add(new ItemsTrashItem("Product", "Product", "666", "", "", "", "666",  "", ""));
        items.add(new ItemsTrashItem("Product", "Product", "777", "", "", "", "777",  "", ""));
        items.add(new ItemsTrashItem("Product", "Product", "888", "", "", "", "888",  "", ""));
        items.add(new ItemsTrashItem("Product", "Product", "999", "", "", "", "999",  "", ""));
    }

    public static List<ItemsTrashItem> getItems() {
        return items;
    }

    public static void remvoeItem(String clusterName, String concept, String ids) {
        for (ItemsTrashItem item : items) {
            if (clusterName.equals(item.getDataClusterName()) && concept.equals(item.getConceptName())
                    && ids.equals(item.getIds())) {
                items.remove(item);
                return;
            }
        }
    }

    public static ItemsTrashItem getItemByIds(String ids) {
        for (ItemsTrashItem item : items) {
            if (ids.equals(item.getIds())) {
                return item;
            }
        }
        return null;
    }
}
