/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.migration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.amalto.core.util.Util;

public class MigrationRepository {

    private static final Logger LOGGER = Logger.getLogger(MigrationRepository.class);

    private static boolean isExecuted = false;

    private static MigrationRepository repository = null;

    private MigrationRepository() {
    }

    synchronized public static MigrationRepository getInstance() {
        if (repository == null) {
            repository = new MigrationRepository();
        }
        return repository;
    }

    public void execute(boolean force) {
        if (!isExecuted || force) {
            
            List<String> list = new ArrayList<String>();
            // look over the handlers dir, call up each handler to execute if it can
            try {
                DocumentBuilder builder = MDMXMLUtils.getDocumentBuilder().get();
                InputStream in = MigrationRepository.class.getResourceAsStream("/com/amalto/core/migration/migration.xml"); //$NON-NLS-1$
                InputStream extIn = MigrationRepository.class
                        .getResourceAsStream("/com/amalto/core/migration/migration-extension.xml"); //$NON-NLS-1$
                parseConfigList(list, builder, in);
                if (extIn != null) {
                    parseConfigList(list, builder, extIn);
                }
                // get the class definition and invoke the trigger function
                for (String clazz : list) {
                    try {
                        AbstractMigrationTask task = (AbstractMigrationTask) Class.forName(clazz).newInstance();
                        task.setForceExe(force);
                        task.start();
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                }
                // clear the cache objects, is this still need?
                Util.getXmlServerCtrlLocal().clearCache();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                return;
            }
        }
        isExecuted = true;
    }

    private void parseConfigList(List<String> list, DocumentBuilder builder, InputStream in) throws SAXException, IOException {
        Document doc = builder.parse(in);
        NodeList nodelist = doc.getElementsByTagName("Root"); //$NON-NLS-1$
        Node root = nodelist.item(0);
        for (int id = 0; id < root.getChildNodes().getLength(); id++) {
            Node node = root.getChildNodes().item(id);
            if (node instanceof Element)
                list.add(node.getNodeName());
        }
    }

    public void connect(boolean forceUpgrade) {
        execute(forceUpgrade);
    }

}
