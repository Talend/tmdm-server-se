/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.general.model.GroupItem;
import org.talend.mdm.webapp.general.model.LanguageBean;
import org.talend.mdm.webapp.general.model.MenuBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.core.webservice.WSPutItem;
import com.amalto.webapp.core.bean.Configuration;
import com.amalto.webapp.core.util.Menu;
import com.amalto.webapp.core.util.SystemLocale;
import com.amalto.webapp.core.util.SystemLocaleFactory;
import com.amalto.webapp.core.util.Util;

public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class);

    private static final String WELCOMECONTEXT = "welcomeportal", WELCOMEAPP = "WelcomePortal";//$NON-NLS-1$ //$NON-NLS-2$

    private static final String DEFAULT_LANG = "en"; //$NON-NLS-1$

    private static final String PROVISIONING_CONCEPT = "User"; //$NON-NLS-1$

    private static final String DATACLUSTER_PK = "PROVISIONING"; //$NON-NLS-1$

    private static final Messages MESSAGES = MessagesFactory.getMessages(
            "org.talend.mdm.webapp.general.client.i18n.GeneralMessages", Utils.class.getClassLoader()); //$NON-NLS-1$

    public static int getSubMenus(Menu menu, String language, List<MenuBean> rows, int level, int i) {
        for (String key : menu.getSubMenus().keySet()) {
            Menu subMenu = menu.getSubMenus().get(key);
            String context = subMenu.getContext();
            String application = subMenu.getApplication();
            if ("updatereport".equals(context) && "UpdateReport".equals(application)) { //$NON-NLS-1$//$NON-NLS-2$
                context = "journal"; //$NON-NLS-1$
                application = "Journal"; //$NON-NLS-1$
            }
            if (GxtFactory.getInstance().isExcluded(context, application)) {
                continue;
            }
            MenuBean item = new MenuBean();
            item.setId(i);
            item.setLevel(level);
            item.setContext(context);
            item.setIcon(subMenu.getIcon());
            String name = subMenu.getLabels().get(language);
            if (name == null) {
                name = subMenu.getLabels().get(DEFAULT_LANG); // fallback to default
            }
            item.setName(name);
            item.setApplication(application == null ? "" : application); //$NON-NLS-1$
            disabledMenuItemIf(subMenu, item, language);
            rows.add(item);
            i++;
            if (subMenu.getSubMenus().size() > 0) {
                i = getSubMenus(subMenu, language, rows, level + 1, i);
            }
        }
        return i;
    }

    private static void disabledMenuItemIf(Menu menu, MenuBean menuBean, String language) {
        if ("stagingarea".equals(menu.getContext()) && "Stagingarea".equals(menu.getApplication())) { //$NON-NLS-1$ //$NON-NLS-2$
            menuBean.setDisabledDesc(MESSAGES.getMessage(new Locale(language), "stagingarea_unavailable")); //$NON-NLS-1$
            boolean disabled = true;
            try {
                WSDataClusterPK wsDataClusterPK = new WSDataClusterPK(Configuration.getConfiguration().getCluster());
                disabled = !Util.getPort().supportStaging(wsDataClusterPK).is_true();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            menuBean.setDisabled(disabled);
        }
    }

    public static ArrayList<String> getCssImport() throws Exception {
        ArrayList<String> imports = new ArrayList<String>();
        getCssImportDetail(Menu.getRootMenu(), imports, 1, 1);
        return imports;
    }

    public static ArrayList<String> getJavascriptImport() throws Exception {
        ArrayList<String> imports = new ArrayList<String>();
        getJavascriptImportDetail(Menu.getRootMenu(), imports, 1, 1);
        completeThirdPartJS(imports);
        return imports;
    }

    private static int getCssImportDetail(Menu menu, List<String> imports, int level, int i) throws Exception {

        for (String key : menu.getSubMenus().keySet()) {
            Menu subMenu = menu.getSubMenus().get(key);

            if (subMenu.getContext() != null) {
                if (GxtFactory.getInstance().isExcluded(subMenu.getContext(), subMenu.getApplication())) {
                    continue;
                }
                String[] csses = GxtFactory.getInstance().getGxtCss(subMenu.getContext(), subMenu.getApplication());
                if (csses != null) {
                    for (String css : csses) {
                        imports.add("<link rel='stylesheet' type='text/css' href='" + css + "'/>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
                i++;
            }
            if (subMenu.getSubMenus().size() > 0) {
                i = getJavascriptImportDetail(subMenu, imports, level + 1, i);
            }
        }
        return i;
    }

    private static int getJavascriptImportDetail(Menu menu, List<String> imports, int level, int i) throws Exception {
        if (menu.getParent() == null) {
            // add welcome by default
            Menu welMenu = new Menu();
            welMenu.setApplication(WELCOMEAPP);
            welMenu.setContext(WELCOMECONTEXT);
            menu.getSubMenus().put(WELCOMECONTEXT, welMenu);
        }

        for (String key : menu.getSubMenus().keySet()) {
            Menu subMenu = menu.getSubMenus().get(key);

            if (subMenu.getContext() != null) {
                String context = subMenu.getContext();
                String application = subMenu.getApplication();
                if ("updatereport".equals(context) && "UpdateReport".equals(application)) { //$NON-NLS-1$//$NON-NLS-2$
                    context = "journal"; //$NON-NLS-1$
                    application = "Journal"; //$NON-NLS-1$
                }
                if (GxtFactory.getInstance().isExcluded(context, application)) {
                    continue;
                }
                String gxtEntryModule = GxtFactory.getInstance().getGxtEntryModule(context, application);
                if (gxtEntryModule == null) {
                    // Other Application, direct js
                    String tmp = "<script type=\"text/javascript\" src=\"secure/js/" //$NON-NLS-1$
                            + application + ".js\"></script>\n"; //$NON-NLS-1$
                    if (!imports.contains(tmp)) {
                        imports.add(tmp);
                    }
                } else {
                    String tmp = "<script type=\"text/javascript\" src=\"" + gxtEntryModule + "/" //$NON-NLS-1$ //$NON-NLS-2$ 
                            + gxtEntryModule + ".nocache.js\"></script>\n"; //$NON-NLS-1$
                    if ("browserecords".equals(context) && "browserecords".equals(gxtEntryModule)) { //$NON-NLS-1$ //$NON-NLS-2$
                        // DWR
                        imports.add("<script type=\"text/javascript\" src=\"secure/dwr/interface/ItemsBrowserInterface.js\"></script>\n"); //$NON-NLS-1$
                        imports.add("<script type=\"text/javascript\" src=\"secure/js/ImprovedDWRProxy.js\"></script>\n"); //$NON-NLS-1$
                        imports.add("<script type=\"text/javascript\" src=\"secure/js/SearchEntityPanel.js\"></script>\n"); //$NON-NLS-1$
                        imports.add("<script type=\"text/javascript\" src=\"secure/js/DataNavigatorPanel.js\"></script>\n"); //$NON-NLS-1$
                    }
                    if (!imports.contains(tmp)) {
                        imports.add(tmp);
                    }
                }
                if (context.equals("stagingarea")) { //$NON-NLS-1$
                    String tmp = "<script type=\"text/javascript\" src=\"stagingareabrowse/stagingareabrowse.nocache.js\"></script>\n"; //$NON-NLS-1$
                    imports.add(tmp);
                }
                i++;
            }
            if (subMenu.getSubMenus().size() > 0) {
                i = getJavascriptImportDetail(subMenu, imports, level + 1, i);
            }
        }
        return i;
    }

    private static void completeThirdPartJS(ArrayList<String> imports) {
        imports.add("<script language=\"javascript\" src=\"secure/gxt/resources/flash/swfobject.js\"></script>\n"); //$NON-NLS-1$
    }

    public static String getCommonImport() {
        return
        // EXT & YUI
        "<script type=\"text/javascript\" src=\"secure/yui-2.4.0/build/utilities/utilities.js\"></script>\n" //$NON-NLS-1$
                + "<script type=\"text/javascript\" src=\"secure/yui-2.4.0/build/yuiloader/yuiloader-beta.js\"></script>\n" //$NON-NLS-1$
                + "<script type=\"text/javascript\" src=\"secure/ext-2.2/adapter/yui/ext-yui-adapter.js\"></script>\n" //$NON-NLS-1$
                + "<script type=\"text/javascript\" src=\"secure/ext-2.2/ext-all.js\"></script>\n" //$NON-NLS-1$
                + "<script type=\"text/javascript\" src=\"secure/d3js-3.5.14/d3.min.js\"></script>\n" //$NON-NLS-1$
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"secure/ext-2.2/resources/css/ext-all_compatible.css\" />\n" //$NON-NLS-1$
                + // EXT-UX
                "<script type=\"text/javascript\" src=\"secure/ext.ux/editablecolumntree/ColumnNodeUI.js\"></script>\n" //$NON-NLS-1$
                + "<script type=\"text/javascript\" src=\"secure/ext.ux/editablecolumntree/treeSerializer.js\"></script>\n" //$NON-NLS-1$
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"secure/ext.ux/editablecolumntree/editable-column-tree.css\" />\n" //$NON-NLS-1$
                + "<script type=\"text/javascript\" src=\"secure/ext.ux/MultiSelectTreePanel.js\"></script>\n" //$NON-NLS-1$
                + "<script type=\"text/javascript\" src=\"secure/ext.ux/i18n/PropertyReader.js\"></script>\n" //$NON-NLS-1$
                + "<script type=\"text/javascript\" src=\"secure/ext.ux/i18n/Bundle.js\"></script>\n" //$NON-NLS-1$
                + // Firefox3 Fixes
                "<link rel=\"stylesheet\" type=\"text/css\" href=\"secure/css/firefox3-fix.css\" />\n" //$NON-NLS-1$
                + // CORE
                "<script type=\"text/javascript\" src=\"proxy_core.js\"></script>\n" //$NON-NLS-1$
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"secure/css/webapp-core.css\" />\n" //$NON-NLS-1$
                + "<link rel=\"stylesheet\" type=\"text/css\" href=\"secure/css/amalto-menus.css\" />\n" //$NON-NLS-1$
                + // Proxy DWR <-> Ext
                "<script type=\"text/javascript\" src=\"secure/ext.ux/DWRAction.js\"></script>\n" //$NON-NLS-1$
                + "<script type=\"text/javascript\" src=\"secure/ext.ux/DWRProxy.js\"></script>\n" //$NON-NLS-1$
                + // utility class
                "<script type=\"text/javascript\" src=\"secure/js/bgutil.js\"></script>\n" //$NON-NLS-1$
                + // graph class
                "<script type=\"text/javascript\" src=\"secure/js/raphael-min.js\"></script>\n" //$NON-NLS-1$
                + // DWR
                "<script language=\"javascript1.2\" type='text/javascript' src='secure/dwr/engine.js'></script>\n" //$NON-NLS-1$
                + "<script language=\"javascript1.2\" type='text/javascript' src='secure/dwr/util.js'></script>\n" //$NON-NLS-1$
                + // Simile Widget
                "<script src=\"secure/timeline/timeline_js/timeline-api.js\" type=\"text/javascript\"></script>\n" //$NON-NLS-1$
                + "<script src=\"secure/timeline/timeline_ajax/simile-ajax-api.js\" type=\"text/javascript\"></script>\n" //$NON-NLS-1$
                + "<link rel=\"stylesheet\" href=\"secure/timeline/css/default.css\" type=\"text/css\">"; //$NON-NLS-1$
    }

    public static List<LanguageBean> getLanguages(String selectedLang) throws Exception {

        List<LanguageBean> languages = new ArrayList<LanguageBean>();
        Map<String, SystemLocale> supportedLocales = SystemLocaleFactory.getInstance().getSupportedLocales();
        for (String iso : supportedLocales.keySet()) {
            SystemLocale systemLocale = supportedLocales.get(iso);
            LanguageBean lang = new LanguageBean();
            lang.setText(systemLocale.getLabel());
            lang.setValue(systemLocale.getIso());
            lang.setDateTimeFormat(systemLocale.getDateTimeFormat());
            if (lang.getValue().equals(selectedLang)) {
                lang.setSelected(true);
            }
            languages.add(lang);
        }

        if (selectedLang == null) {
            languages.get(0).setSelected(true);
        }

        return languages;
    }

    public static List<GroupItem> getGroupItems(String language) throws IOException, SAXException, ParserConfigurationException {
        InputStream is = Utils.class.getResourceAsStream("/MenuGroup.xml"); //$NON-NLS-1$
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
        Element root = doc.getDocumentElement();
        String defaultLang = root.getAttribute("defaultLang"); //$NON-NLS-1$
        NodeList nodes = root.getChildNodes();
        List<GroupItem> giList = new ArrayList<GroupItem>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.getNodeName().equals("groupitem")) { //$NON-NLS-1$ 
                    GroupItem giNew = new GroupItem();
                    Node langNode = node.getAttributes().getNamedItem(language);
                    if (langNode == null) {
                        langNode = node.getAttributes().getNamedItem(defaultLang);
                    }
                    giNew.setGroupHeader(langNode.getNodeValue());
                    NodeList items = node.getChildNodes();
                    List<String> menuItems = new ArrayList<String>();
                    for (int k = 0; k < items.getLength(); k++) {
                        Node item = items.item(k);
                        if (item.getNodeName().equals("item")) { //$NON-NLS-1$
                            menuItems.add(item.getTextContent());
                        }
                    }
                    giNew.setMenuItems(menuItems);
                    giList.add(giNew);
                }
            }
        }

        return giList;
    }

    public static String setLanguage(String xml, String language) throws Exception {
        Document doc = XMLUtils.parse(xml);
        if (doc.hasChildNodes()) {
            if (doc.getElementsByTagName("language").item(0) != null) { //$NON-NLS-1$
                doc.getElementsByTagName("language").item(0).setTextContent(language); //$NON-NLS-1$
                return XMLUtils.nodeToString(doc);
            } else {
                Element node = doc.createElement("language"); //$NON-NLS-1$
                node.setTextContent(language);
                doc.getDocumentElement().appendChild(node);
                return XMLUtils.nodeToString(doc);
            }
        }
        return xml;
    }

    public static String getDefaultLanguage() throws Exception {
        return com.amalto.webapp.core.util.Util.getDefaultLanguage();
    }

    public static Boolean setDefaultLanguage(String language) throws Exception {
        try {
            ILocalUser user = LocalUser.getLocalUser();
            if (Util.userCanWrite(user)) {
                String userName = user.getUsername();
                WSItemPK itemPK = new WSItemPK(new WSDataClusterPK(DATACLUSTER_PK), PROVISIONING_CONCEPT,
                        new String[] { userName });
                if (userName != null && userName.length() > 0) {
                    String userXml = Util.getPort().getItem(new WSGetItem(itemPK)).getContent();
                    Util.getPort().putItem(
                            new WSPutItem(new WSDataClusterPK(DATACLUSTER_PK), Utils.setLanguage(userXml, language),
                                    new WSDataModelPK(DATACLUSTER_PK), false));
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }
}
