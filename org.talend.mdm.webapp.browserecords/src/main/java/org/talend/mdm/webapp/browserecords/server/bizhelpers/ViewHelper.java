/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.bizhelpers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.server.util.DynamicLabelUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ColumnElement;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeLayoutModel;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeModel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.core.util.Messages;
import com.amalto.core.util.MessagesFactory;
import com.amalto.core.webservice.WSView;

/**
 * DOC HSHU class global comment. Detailled comment TODO In the further, we can migrate helper classes to spring beans
 */
public class ViewHelper {

    private static final Logger logger = Logger.getLogger(ViewHelper.class);

    public static final String DEFAULT_VIEW_PREFIX = "Browse_items";//$NON-NLS-1$

    /**
     * DOC HSHU Comment method "getConceptFromDefaultViewName".
     */
    public static String getConceptFromDefaultViewName(String viewName) {

        String concept = viewName.replaceAll(ViewHelper.DEFAULT_VIEW_PREFIX + "_", "").replaceAll("#.*", "");//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return concept;

    }

    /**
     * DOC HSHU Comment method "getViewLabel".
     * 
     * @param language
     * @param wsview
     * @return
     */
    public static String getViewLabel(String language, WSView wsview) {

        String viewDesc = MultilanguageMessageParser.pickOutISOMessage(
                !wsview.getDescription().equals("") ? wsview.getDescription() : wsview.getName(), language); //$NON-NLS-1$
        viewDesc = viewDesc.equals("") ? wsview.getName() : viewDesc; //$NON-NLS-1$ 
        return viewDesc;

    }

    /**
     * DOC HSHU Comment method "getViewables".
     * 
     * @param wsView
     * @return
     */
    public static String[] getViewables(WSView wsView) {
        return wsView.getViewableBusinessElements();
    }

    /**
     * DOC HSHU Comment method "getSearchables".
     * 
     * @param wsView
     * @param dataModel
     * @param language
     * @return
     */
    public static Map<String, String> getSearchables(WSView wsView, String dataModel, String language, EntityModel entityModel) {
        try {
            String[] searchables = wsView.getSearchableBusinessElements();
            Map<String, String> labelSearchables = new LinkedHashMap<String, String>();

            if (wsView.getName().contains(DEFAULT_VIEW_PREFIX + "_")) { //$NON-NLS-1$
                Map<String, TypeModel> labelMapSrc = entityModel.getMetaDataTypes();
                for (int i = 0; i < searchables.length; i++) {
                    String searchableLabel;
                    // add feature TMDM-2679:FT search in web UI,it should be displaying the "whole content" but not the
                    // entity name in the second drop-down.
                    if (searchables[i].equals(entityModel.getConceptName())) {
                        Messages message = MessagesFactory.getMessages(
                                "org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages",//$NON-NLS-1$
                                ViewHelper.class.getClassLoader());
                        searchableLabel = message.getMessage(new Locale(language), "entity_display_name"); //$NON-NLS-1$
                    } else {
                        searchableLabel = labelMapSrc.get(searchables[i]) == null ? searchables[i] : labelMapSrc.get(
                                searchables[i]).getLabel(language);
                        if (searchableLabel == null) {
                            searchableLabel = labelMapSrc.get(searchables[i]).getName();
                        } else {
                            if (DynamicLabelUtil.isDynamicLabel(searchableLabel))
                                searchableLabel = labelMapSrc.get(searchables[i]).getName();
                        }

                    }
                    labelSearchables.put(searchables[i], searchableLabel);
                }
            }

            return labelSearchables;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    public static ColumnTreeLayoutModel builderLayout(Element el) {
        ColumnTreeLayoutModel columnModel = new ColumnTreeLayoutModel();
        NodeList children = el.getChildNodes();
        if (children != null && children.getLength() > 0) {
            List<ColumnTreeModel> columnTreeModels = new ArrayList<ColumnTreeModel>();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if ("mdmform:Panel".equals(node.getNodeName())) { //$NON-NLS-1$
                    Element child = (Element) node;
                    columnTreeModels.add(builderColumnTreeModel(child));
                }
            }
            columnModel.setColumnTreeModels(columnTreeModels);
        }
        return columnModel;
    }

    private static ColumnTreeModel builderColumnTreeModel(Element el) {
        ColumnTreeModel columnTreeModel = new ColumnTreeModel();
        columnTreeModel.setStyle(el.getAttribute("style")); //$NON-NLS-1$
        NodeList children = el.getChildNodes();
        if (children != null && children.getLength() > 0) {
            List<ColumnElement> childrenEls = new ArrayList<ColumnElement>();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if ("children".equals(node.getNodeName())) { //$NON-NLS-1$
                    Element child = (Element) node;
                    childrenEls.add(builderColumnElement(child));
                }
            }
            columnTreeModel.setColumnElements(childrenEls);
        }
        return columnTreeModel;
    }

    private static ColumnElement builderColumnElement(Element el) {
        ColumnElement columnEl = new ColumnElement();
        columnEl.setLabel(el.getAttribute("label")); //$NON-NLS-1$
        columnEl.setxPath(el.getAttribute("xpath")); //$NON-NLS-1$
        columnEl.setParent(el.getAttribute("parent")); //$NON-NLS-1$
        columnEl.setStyle(el.getAttribute("style")); //$NON-NLS-1$
        columnEl.setLabelStyle(el.getAttribute("labelStyle")); //$NON-NLS-1$
        columnEl.setValueStyle(el.getAttribute("valueStyle")); //$NON-NLS-1$
        columnEl.setHtmlSnippet(el.getAttribute("htmlSnippet")); //$NON-NLS-1$
        NodeList children = el.getChildNodes();
        if (children != null && children.getLength() > 0) {
            List<ColumnElement> childrenEls = new ArrayList<ColumnElement>();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if ("children".equals(node.getNodeName())) { //$NON-NLS-1$
                    Element child = (Element) node;
                    childrenEls.add(builderColumnElement(child));
                }
            }
            columnEl.setChildren(childrenEls);
        }

        return columnEl;
    }
}
