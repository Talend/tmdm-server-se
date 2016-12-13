/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.bean;

import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.util.LocalUser;
import com.amalto.core.webservice.WSBoolean;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSExistsDataCluster;
import com.amalto.core.webservice.WSExistsDataModel;
import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.core.webservice.WSPutItem;
import com.amalto.webapp.core.dwr.CommonDWR;
import com.amalto.webapp.core.util.SessionContextHolder;
import com.amalto.webapp.core.util.Util;

public class Configuration {

    private static final Logger LOG = Logger.getLogger(Configuration.class);

    private static final String MDM_CONFIGURATION_ATTRIBUTE = "MDM_CONFIGURATION_ATTRIBUTE"; //$NON-NLS-1$

    private String cluster;

    private String model;

    private Configuration() {
    }

    private Configuration(String cluster, String model) {
        this.cluster = cluster;
        this.model = model;
    }

    public static synchronized Configuration getConfiguration() throws Exception {
        Configuration configuration = loadFromSession();
        if (configuration == null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Configuration instance is null, loading ..."); //$NON-NLS-1$
            }
            configuration = load();
            storeInSession(configuration);
        }
        return configuration;
    }

    public static synchronized void setConfiguration(String cluster, String model) throws Exception {
        if (cluster == null || cluster.trim().length() == 0) {
            throw new Exception("Data Container can't be empty!"); //$NON-NLS-1$
        }
        if (model == null || model.trim().length() == 0) {
            throw new Exception("Data Model can't be empty!"); //$NON-NLS-1$
        }

        store(cluster, model);
        storeInSession(new Configuration(cluster, model));
    }

    private static Configuration loadFromSession() {
        HttpSession session = SessionContextHolder.currentSession();
        if (session != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting registered configuration from session " + session.getId()); //$NON-NLS-1$
            }
            return (Configuration) session.getAttribute(MDM_CONFIGURATION_ATTRIBUTE);
        }
        return null;
    }

    private static void storeInSession(Configuration configuration) {
        HttpSession session = SessionContextHolder.currentSession();
        if (session != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Registering configuration into session " + session.getId()); //$NON-NLS-1$
            }
            session.setAttribute(MDM_CONFIGURATION_ATTRIBUTE, configuration);
        }
    }

    private static void store(String cluster, String model) throws Exception {
        if (StringUtils.isBlank(cluster)) {
            throw new Exception("nocontainer"); //$NON-NLS-1$
        } else if (StringUtils.isBlank(model)) {
            throw new Exception("nomodel"); //$NON-NLS-1$
        }

        ILocalUser user = LocalUser.getLocalUser();
        if (!Util.userCanWrite(user)) {
            return;
        }

        String xml = Util
                .getPort()
                .getItem(
                        new WSGetItem(new WSItemPK(
                                new WSDataClusterPK("PROVISIONING"), "User", new String[] { user.getUsername() }))) //$NON-NLS-1$//$NON-NLS-2$
                .getContent();
        Document d = Util.parse(xml);

        com.amalto.core.util.Util.setUserProperty(d, "cluster", cluster); //$NON-NLS-1$
        com.amalto.core.util.Util.setUserProperty(d, "model", model); //$NON-NLS-1$

        if (com.amalto.core.util.Util.isEnterprise()) {
            Util.getPort()
                    .putItem(
                            new WSPutItem(
                                    new WSDataClusterPK("PROVISIONING"), XMLUtils.nodeToString(d.getDocumentElement(), true, true).replaceAll( //$NON-NLS-1$
                                                    "<\\?xml.*?\\?>", ""), new WSDataModelPK("PROVISIONING"), false)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } else {
            Util.storeProvisioning(LocalUser.getLocalUser().getUsername(),
                    XMLUtils.nodeToString(d.getDocumentElement(), true, true).replaceAll("<\\?xml.*?\\?>", "")); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static boolean existsCluster(String cluster) throws Exception {
        if (StringUtils.isNotBlank(cluster)) {
            WSExistsDataCluster wsExistsDataCluster = new WSExistsDataCluster();
            wsExistsDataCluster.setWsDataClusterPK(new WSDataClusterPK(cluster));
            WSBoolean wsBoolean = Util.getPort().existsDataCluster(wsExistsDataCluster);
            return wsBoolean.is_true();
        } else {
            return false;
        }
    }
    
    private static boolean existsModel(String model) throws Exception {
        if (StringUtils.isNotBlank(model)) {
            WSExistsDataModel wsExistsDataModel = new WSExistsDataModel();
            wsExistsDataModel.setWsDataModelPK(new WSDataModelPK(model));
            WSBoolean wsBoolean = Util.getPort().existsDataModel(wsExistsDataModel);
            return wsBoolean.is_true();
        } else {
            return false;
        }
    }
    
    private static Configuration load() throws Exception {
        Configuration configuration = new Configuration();

        Element user = null;
        try {
            user = Util.parse(LocalUser.getLocalUser().getUserXML()).getDocumentElement();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        if (user == null) {
            String userxml = LocalUser.getLocalUser().getUserXML();
            user = (Element) com.amalto.core.util.Util.getNodeList(Util.parse(userxml), "//" + "User").item(0); //$NON-NLS-1$ //$NON-NLS-2$
        }

        NodeList nodeList = com.amalto.core.util.Util.getNodeList(user, "//property"); //$NON-NLS-1$
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if ("cluster".equals(com.amalto.core.util.Util.getFirstTextNode(node, "name"))) { //$NON-NLS-1$ //$NON-NLS-2$
                Node fchild = com.amalto.core.util.Util.getNodeList(node, "value").item(0).getFirstChild(); //$NON-NLS-1$
                if (fchild != null && existsCluster(fchild.getNodeValue())) {
                    configuration.setCluster(fchild.getNodeValue());
                }

            }
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if ("model".equals(com.amalto.core.util.Util.getFirstTextNode(node, "name"))) { //$NON-NLS-1$ //$NON-NLS-2$
                Node fchild = com.amalto.core.util.Util.getNodeList(node, "value").item(0).getFirstChild(); //$NON-NLS-1$
                if (fchild != null && existsModel(fchild.getNodeValue())) {
                    configuration.setModel(fchild.getNodeValue());
                }

            }
        }
        boolean attemptToStoreAgain = false;
        if (configuration.getCluster() == null && CommonDWR.getClusters().length > 0) {
            configuration.setCluster(CommonDWR.getClusters()[0]);
            attemptToStoreAgain = true;
        }
        if (configuration.getModel() == null && CommonDWR.getModels().length > 0) {
            configuration.setModel(CommonDWR.getModels()[0]);
            attemptToStoreAgain = true;
        }

        if (attemptToStoreAgain) {
            if (StringUtils.isBlank(configuration.getCluster()) || StringUtils.isBlank(configuration.getModel())) {
                configuration.setCluster(null);
                configuration.setModel(null);
            } else {
                try {
                    store(configuration.getCluster(), configuration.getModel());
                } catch (Exception e) {
                    LOG.error("Unable to store updated configuration", e); //$NON-NLS-1$
                }
            }
        }

        return configuration;
    }

    public String getCluster() {
        return cluster;
    }

    public void setCluster(String cluster) {
        this.cluster = cluster;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public String toString() {
        return "cluster:" + cluster + "," + "model:" + model; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}
