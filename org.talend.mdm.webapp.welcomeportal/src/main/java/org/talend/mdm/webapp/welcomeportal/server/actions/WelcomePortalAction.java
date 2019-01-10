/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.server.actions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.shared.AppHeader;
import org.talend.mdm.webapp.base.shared.Constants;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortal;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortalService;
import org.talend.mdm.webapp.welcomeportal.client.mvc.PortalProperties;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;
import com.amalto.core.util.User;
import com.amalto.core.webservice.WSByteArray;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSExecuteTransformerV2;
import com.amalto.core.webservice.WSGetTransformerV2;
import com.amalto.core.webservice.WSGetTransformerV2PKs;
import com.amalto.core.webservice.WSPutItem;
import com.amalto.core.webservice.WSTransformerContext;
import com.amalto.core.webservice.WSTransformerContextPipelinePipelineItem;
import com.amalto.core.webservice.WSTransformerV2;
import com.amalto.core.webservice.WSTransformerV2PK;
import com.amalto.core.webservice.WSTypedContent;
import com.amalto.webapp.core.bean.Configuration;
import com.amalto.core.util.Menu;
import com.amalto.webapp.core.util.Util;
import com.amalto.webapp.core.util.Webapp;

/**
 * The server side implementation of the RPC service.
 */
public class WelcomePortalAction implements WelcomePortalService {

    private static final Logger LOG = Logger.getLogger(WelcomePortalAction.class);

    private static final String STANDALONE_PROCESS_PREFIX = "Runnable#"; //$NON-NLS-1$
    
    private static final Messages MESSAGES = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.welcomeportal.client.i18n.WelcomePortalMessages", WelcomePortalAction.class.getClassLoader()); //$NON-NLS-1$

    private PortalProperties portalConfig;

    /**
     * check if is show workflow task link.
     * 
     * @return
     */
    @Override
    public boolean isHiddenWorkFlowTask() throws ServiceException {
        return isHiddenMenu(WelcomePortal.WORKFLOW_TASKAPP);
    }

    /**
     * check if is show dsc task link.
     * 
     * @return
     */
    @Override
    public boolean isHiddenTDSTask() throws ServiceException {
        return isHiddenMenu(Constants.TDS_APP);
    }

    /**
     * check if is it standalong process.
     */
    public boolean isStandaloneProcess(String wstransformerpk) {
        return wstransformerpk.startsWith(STANDALONE_PROCESS_PREFIX);
    }

    /**
     * check if is show specify menu.
     * 
     * @param menu
     * @return
     */
    private boolean isHiddenMenu(String menu) throws ServiceException {
        try {
            Map<String, Menu> subMenus = Menu.getRootMenu().getSubMenus();
            for (String key : subMenus.keySet()) {
                Menu subMenu = subMenus.get(key);

                if (menu.equals(subMenu.getApplication())) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }

    /**
     * get workflow task informations.
     * 
     * @return
     */
    @Override
    public int getWorkflowTaskMsg() {
        return Webapp.INSTANCE.getWorkflowTasksCount();
    }

    @Override
    public Map<String, String> getStandaloneProcess(String language) throws ServiceException {
        try {
            Map<String, String> processMap = new HashMap<String, String>();
            WSTransformerV2PK[] wst = Util.getPort().getTransformerV2PKs(new WSGetTransformerV2PKs("*")) //$NON-NLS-1$
                    .getWsTransformerV2PK();
            for (WSTransformerV2PK wstransformerpk : wst) {
                if (isStandaloneProcess(wstransformerpk.getPk())) {
                    WSTransformerV2 wsTransformer = Util.getPort().getTransformerV2(new WSGetTransformerV2(wstransformerpk));
                    processMap.put(wstransformerpk.getPk(),
                            MultilanguageMessageParser.pickOutISOMessage(wsTransformer.getDescription(), language));
                }
            }
            return sortProcess(processMap);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }

    protected Map<String, String> sortProcess(Map<String, String> processMap) {
        Map<String, String> linkedHashMap = new LinkedHashMap<>();
        Map<String, String> processMapTmp = new HashMap<String, String>(processMap);

        processMapTmp.entrySet().stream().peek(entry -> {
            if (isStandaloneProcess(entry.getValue())) {
                entry.setValue(entry.getValue().replace(STANDALONE_PROCESS_PREFIX, StringUtils.EMPTY));
            }

        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)).entrySet().stream()
                .sorted(Map.Entry.<String, String>comparingByValue())
                .forEachOrdered(x -> linkedHashMap.put(x.getKey(), processMap.get(x.getKey())));

        return linkedHashMap;
    }

    /**
     * run the standalone process.
     * 
     * @param transformerPK
     * @return
     */
    @Override
    public String runProcess(String transformerPK) throws ServiceException {
        try {
            WSTransformerContext wsTransformerContext = new WSTransformerContext(new WSTransformerV2PK(transformerPK), null, null);

            StringBuilder result = new StringBuilder();
            // yguo, plugin input parameters
            String content = "<root/>"; //$NON-NLS-1$
            WSTypedContent typedContent = new WSTypedContent(null, new WSByteArray(content.getBytes("UTF-8")),//$NON-NLS-1$
                    "text/xml; charset=UTF-8");//$NON-NLS-1$
            WSExecuteTransformerV2 wsExecuteTransformerV2 = new WSExecuteTransformerV2(wsTransformerContext, typedContent);
            WSTransformerContextPipelinePipelineItem[] entries = Util.getPort().executeTransformerV2(wsExecuteTransformerV2)
                    .getPipeline().getPipelineItem();
            if (entries.length > 0) {
                WSTransformerContextPipelinePipelineItem item = entries[entries.length - 1];
                if (item.getVariable().equals("output_url")) {//$NON-NLS-1$
                    byte[] bytes = item.getWsTypedContent().getWsBytes().getBytes();
                    String urlcontent = new String(bytes);
                    Document resultDoc = Util.parse(urlcontent);
                    NodeList attrList = com.amalto.core.util.Util.getNodeList(resultDoc, "//attr");//$NON-NLS-1$
                    if (attrList != null && attrList.getLength() > 0) {
                        String downloadUrl = attrList.item(0).getTextContent();
                        result.append(downloadUrl);
                    }
                }
            }
            return result.toString();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }

    }

    @Override
    public boolean isEnterpriseVersion() throws ServiceException {
        return Webapp.INSTANCE.isEnterpriseVersion();
    }

    @Override
    public String getMenuLabel(String language, String id) throws Exception {
        return Menu.getMenuLabel(language, id);
    }

    @Override
    public String getCurrentDataContainer() throws ServiceException {
        try {
            Configuration config = Configuration.getConfiguration();
            return config.getCluster();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }

    @Override
    public Map<Boolean, Integer> getWelcomePortletConfig() throws Exception {
        return Webapp.INSTANCE.getWelcomePortletConfig();
    }

    @Override
    public PortalProperties getPortalConfig() throws ServiceException {

        try {
            ILocalUser user = LocalUser.getLocalUser();
            User parse = User.parse(user.getUserXML());
            portalConfig = new PortalProperties(getPortalPreferences(parse.getProperties()));
            return portalConfig;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(MESSAGES.getMessage("retrieve_portal_config_failed")); //$NON-NLS-1$
        }
    }

    private Map<String, String> getPortalPreferences(Map<String, String> props) {
        Map<String, String> portalConfigs = new HashMap<String, String>();
        for (String key : props.keySet()) {
            if (key.startsWith(Constants.PORTAL_CONFIG_PREFIX)) {
                portalConfigs.put(key, props.get(key));
            }
        }
        return portalConfigs;
    }

    @Override
    public void savePortalConfig(PortalProperties config) throws ServiceException {
        try {
            ILocalUser user = LocalUser.getLocalUser();
            if (!user.userCanWrite()) {
                return;
            }
            User parsedUser = user.parseWithoutSystemRoles();
            Map<String, String> properties = parsedUser.getProperties();
            String value;
            for (String name : config.getKeys()) {
                value = config.get(name);
                properties.put(name, value);
            }
            Util.getPort()
                    .putItem(
                            new WSPutItem(
                                    new WSDataClusterPK("PROVISIONING"), parsedUser.serialize(true), new WSDataModelPK("PROVISIONING"), false)); //$NON-NLS-1$ //$NON-NLS-2$
            user.setUserXML(parsedUser.serialize(false));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(MESSAGES.getMessage("save_portal_config_failed")); //$NON-NLS-1$
        }
    }

    @Override
    public void savePortalConfig(String key, String value) throws ServiceException {
        try {
            ILocalUser user = LocalUser.getLocalUser();
            if (!user.userCanWrite()) {
                return;
            }
            User parsedUser = user.parseWithoutSystemRoles();
            Map<String, String> properties = parsedUser.getProperties();

            properties.put(key, value);
            Util.getPort()
                    .putItem(
                            new WSPutItem(
                                    new WSDataClusterPK("PROVISIONING"), parsedUser.serialize(true), //$NON-NLS-1$
                                    new WSDataModelPK("PROVISIONING"), false)); //$NON-NLS-1$

            user.setUserXML(parsedUser.serialize(false));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(MESSAGES.getMessage("save_portal_config_failed")); //$NON-NLS-1$
        }
    }

    @Override
    public void savePortalConfig(String key, String portletName, String value) throws ServiceException {
        try {
            ILocalUser user = LocalUser.getLocalUser();
            if (!user.userCanWrite()) {
                return;
            }
            User parsedUser = user.parseWithoutSystemRoles();
            Map<String, String> properties = parsedUser.getProperties();

            portalConfig = new PortalProperties(getPortalPreferences(parsedUser.getProperties()));

            portalConfig.add(key, portletName, value);

            properties.put(key, portalConfig.get(key));
            Util.getPort()
                    .putItem(
                            new WSPutItem(
                                    new WSDataClusterPK("PROVISIONING"), parsedUser.serialize(true), //$NON-NLS-1$
                                    new WSDataModelPK("PROVISIONING"), false)); //$NON-NLS-1$

            user.setUserXML(parsedUser.serialize(false));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(MESSAGES.getMessage("save_portal_config_failed")); //$NON-NLS-1$
        }
    }

    @Override
    public void savePortalConfigAutoAndSetting(String portletName, List<String> configs) throws ServiceException {
        assert configs.size() == 2;
        try {
            ILocalUser user = LocalUser.getLocalUser();
            if (!user.userCanWrite()) {
                return;
            }
            User parsedUser = user.parseWithoutSystemRoles();
            Map<String, String> properties = parsedUser.getProperties();

            portalConfig = new PortalProperties(getPortalPreferences(parsedUser.getProperties()));

            portalConfig.add(PortalProperties.KEY_AUTO_ONOFFS, portletName, configs.get(0));
            portalConfig.add(PortalProperties.KEY_CHART_SETTINGS, portletName, configs.get(1));

            properties.put(PortalProperties.KEY_AUTO_ONOFFS, portalConfig.get(PortalProperties.KEY_AUTO_ONOFFS));
            properties.put(PortalProperties.KEY_CHART_SETTINGS, portalConfig.get(PortalProperties.KEY_CHART_SETTINGS));
            Util.getPort()
                    .putItem(
                            new WSPutItem(
                                    new WSDataClusterPK("PROVISIONING"), parsedUser.serialize(true), //$NON-NLS-1$
                                    new WSDataModelPK("PROVISIONING"), false)); //$NON-NLS-1$

            user.setUserXML(parsedUser.serialize(false));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(MESSAGES.getMessage("save_portal_config_failed")); //$NON-NLS-1$
        }
    }

    @Override
    public AppHeader getAppHeader() throws ServiceException {
        AppHeader header = new AppHeader();
        header.setTdsBaseUrl(MDMConfiguration.getTdsRootUrl());
        return header;
    }

    @Override
    public String getCurrentDataModel() throws ServiceException {
        Configuration config;
        try {
            config = Configuration.getConfiguration();
            return config.getModel();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }
}
