/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.initdb;

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

import com.amalto.core.migration.MigrationRepository;
import com.amalto.core.util.Util;

/**
 * Handle all the initdb extension jobs
 */
public class InitDbExtJobRepository {

    private static final Logger logger = Logger.getLogger(InitDbExtJobRepository.class);

    private static InitDbExtJobRepository repository = null;

    private InitDbExtJobRepository() {
    }

    public static InitDbExtJobRepository getInstance() {
        if (repository == null) {
            repository = new InitDbExtJobRepository();
        }
        return repository;
    }

    public void execute() {
        List<String> list = new ArrayList<String>();
        // look over the handlers dir, call up each handler to execute if it can
        try {
            DocumentBuilder builder = MDMXMLUtils.getDocumentBuilder().get();
            InputStream in = MigrationRepository.class.getResourceAsStream("/com/amalto/core/initdb/extjob/initdb-extjb.xml");//$NON-NLS-1$
            parseConfigList(list, builder, in);
            // get the class definition and invoke the trigger function
            for (String clazz : list) {
                try {
                    IInitDBExtJob task = (IInitDBExtJob) Class.forName(clazz).newInstance();
                    task.run();
                } catch (Exception e) {
                    logger.error(e.getLocalizedMessage(), e);
                }
            }
            //clear the cache objects, is this still need?
            Util.getXmlServerCtrlLocal().clearCache();
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    private void parseConfigList(List<String> list, DocumentBuilder builder, InputStream in) throws Exception {
        Document doc = builder.parse(in);
        NodeList nodelist = doc.getElementsByTagName("Root");//$NON-NLS-1$
        Node root = nodelist.item(0);
        for (int id = 0; id < root.getChildNodes().getLength(); id++) {
            Node node = root.getChildNodes().item(id);
            if (node instanceof Element)
                list.add(node.getNodeName());
        }
    }
}
