/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.migration.tasks;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.migration.AbstractMigrationTask;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.util.Util;

/**
 * convert old Item to concept Item
 * @author achen
 *
 */
public class AutoIncrementTask4 extends AbstractMigrationTask {

    @Override
    protected Boolean execute() {
        try {
            String xml = Util.getXmlServerCtrlLocal().getDocumentAsString(XSystemObjects.DC_CONF.getName(), "Auto_Increment"); //$NON-NLS-1$
            if (xml == null) {
                return true;
            }
            DataClusterPOJOPK DC = new DataClusterPOJOPK(XSystemObjects.DC_CONF.getName());
            String[] IDS = new String[]{"AutoIncrement"}; //$NON-NLS-1$
            String CONCEPT = "AutoIncrement"; //$NON-NLS-1$
            String doctype = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<!DOCTYPE properties SYSTEM \"http://java.sun.com/dtd/properties.dtd\">"; //$NON-NLS-1$
            xml = xml.replaceFirst("(<\\?xml.*\\?>)", doctype); //$NON-NLS-1$
            if (!xml.startsWith(doctype)) {
                xml = doctype + xml;
            }
            byte[] buf = xml.getBytes();
            ByteArrayInputStream bio = new ByteArrayInputStream(buf);
            Properties p = new Properties();
            p.loadFromXML(bio);
            bio.close();
            String xmlString = Util.convertAutoIncrement(p);
            ItemPOJO pojo = new ItemPOJO(
                    DC, //cluster
                    CONCEPT, //concept name
                    IDS,
                    System.currentTimeMillis(), //insertion time
                    xmlString //actual data
            );
            pojo.setDataModelName(XSystemObjects.DM_CONF.getName());
            pojo.store();
            //delete the original file
            Util.getXmlServerCtrlLocal().deleteDocument(XSystemObjects.DC_CONF.getName(), "Auto_Increment"); //$NON-NLS-1$
        } catch (Exception e) {
            Logger.getLogger(AutoIncrementTask4.class).error("Autoincrement exception.", e);
        }
        return true;
    }

}
