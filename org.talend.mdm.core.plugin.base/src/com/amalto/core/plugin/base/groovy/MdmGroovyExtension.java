/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.groovy;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.util.Util;
import com.amalto.core.server.api.Item;

public class MdmGroovyExtension {

    public static String getItemProjection(String clusterName, String conceptName, String ids) {
        String itemProjection = "";
        if (clusterName == null || clusterName.length() == 0) {
            return itemProjection;
        }
        if (conceptName == null || conceptName.length() == 0) {
            return itemProjection;
        }
        if (ids == null) {
            return itemProjection;
        }
        try {
            Item itemCtrl2Local = Util.getItemCtrl2Local();
            //parse ids
            String[] idArray = ids.split("\\.");
            ItemPOJOPK itemPOJOPK = new ItemPOJOPK(new DataClusterPOJOPK(clusterName), conceptName, idArray);
            ItemPOJO itemPOJO = itemCtrl2Local.getItem(itemPOJOPK);
            if (itemPOJO != null) {
                itemProjection = itemPOJO.getProjectionAsString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemProjection;
    }

}
