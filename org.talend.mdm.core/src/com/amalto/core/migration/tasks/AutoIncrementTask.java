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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import com.amalto.core.server.api.XmlServer;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.migration.AbstractMigrationTask;
import com.amalto.core.util.Util;

public class AutoIncrementTask extends AbstractMigrationTask {

    @Override
    protected Boolean execute() {
        File file = new File("auto_increment.conf"); //$NON-NLS-1$
        if (file.exists()) {
            try {
                FileInputStream reader = new FileInputStream(file);
                Properties p = new Properties();
                p.load(reader);
                //save to
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                p.storeToXML(bos, "", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
                String xmlString = bos.toString("UTF-8"); //$NON-NLS-1$
                XmlServer xmlServerCtrlLocal = Util.getXmlServerCtrlLocal();
                xmlServerCtrlLocal.start(XSystemObjects.DC_CONF.getName());
                xmlServerCtrlLocal.putDocumentFromString(xmlString, "Auto_Increment", XSystemObjects.DC_CONF.getName()); //$NON-NLS-1$
                xmlServerCtrlLocal.commit(XSystemObjects.DC_CONF.getName());
                //read from xml file
                bos.close();
                reader.close();
                //delete the auto_increment.conf
                boolean delete = file.delete();
                if(!delete) {
                    Logger.getLogger(AutoIncrementTask.class).warn("Could not successfully delete '" + file + "'.");
                }
            } catch (Exception e) {
                Logger.getLogger(AutoIncrementTask.class).error("Auto increment task exception.", e);
            }
        }
        return true;
    }

}
