/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.xslt;

import org.apache.log4j.Logger;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.util.Util;
import com.amalto.core.server.api.Item;

public class MdmExtension {

    private static final Logger LOG = Logger.getLogger(MdmExtension.class);

    //Signature kept for backward compatibility
    public static String getItemProjection(String unusedParameterForCompatibility, String clusterName, String conceptName,
            String ids) {
        return getItemProjection(clusterName, conceptName, ids);
    }

    public static String getItemProjection(String clusterName, String conceptName, String ids) {
        String itemProjection = ""; //$NON-NLS-1$
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
            // parse ids
            String[] idArray = ids.split("\\."); //$NON-NLS-1$
            ItemPOJOPK itemPOJOPK = new ItemPOJOPK(new DataClusterPOJOPK(clusterName), conceptName, idArray);
            ItemPOJO itemPOJO = itemCtrl2Local.getItem(itemPOJOPK);
            if (itemPOJO != null) {
                itemProjection = itemPOJO.getProjectionAsString();
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        return itemProjection;

    }

    public static String getItemPKXmlString(String clusterName, String conceptName, String ids) {
        StringBuilder itemPKXmlString = new StringBuilder();
        if (clusterName == null || clusterName.length() == 0) {
            return itemPKXmlString.toString();
        }
        if (conceptName == null || conceptName.length() == 0) {
            return itemPKXmlString.toString();
        }
        if (ids == null) {
            return itemPKXmlString.toString();
        }
        itemPKXmlString.append("<item-pOJOPK><concept-name>").append(conceptName).append("</concept-name><ids>").append(ids) //$NON-NLS-1$ //$NON-NLS-2$
                .append("</ids><data-cluster-pOJOPK><ids>").append(clusterName) //$NON-NLS-1$
                .append("</ids></data-cluster-pOJOPK></item-pOJOPK>"); //$NON-NLS-1$
        return itemPKXmlString.toString();
    }

}
