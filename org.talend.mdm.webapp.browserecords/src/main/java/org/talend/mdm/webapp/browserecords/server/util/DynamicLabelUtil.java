/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSGetDataModel;
import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSItem;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.webapp.core.bean.Configuration;
import com.amalto.webapp.core.util.Util;
import com.amalto.webapp.core.util.XmlUtil;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSType;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class DynamicLabelUtil {

    private static final Logger LOG = Logger.getLogger(DynamicLabelUtil.class);

    /**
     * 
     * @return
     */
    public static void getDynamicLabel(Document parsedDocument, String baseXpath, ItemNodeModel itemModel,
            Map<String, TypeModel> metaDataTypes, String language) {
        try {
            String typePath = itemModel.getTypePath();
            TypeModel typeModel = metaDataTypes.get(typePath);
            String fullxpath;
            if (baseXpath == null || baseXpath.trim().length() == 0) {
                fullxpath = CommonUtil.getRealXPath(itemModel);
            } else {
                fullxpath = baseXpath + "/" + CommonUtil.getRealXPath(itemModel); //$NON-NLS-1$
            }
            String label = typeModel.getLabel(language);
            if (org.talend.mdm.webapp.base.server.util.DynamicLabelUtil.isDynamicLabel(label)) {
                label = replaceForeignPath(fullxpath, label, parsedDocument);
                String stylesheet = org.talend.mdm.webapp.base.server.util.DynamicLabelUtil.genStyle(fullxpath,
                        XmlUtil.escapeXml(label));
                String dynamicLB = org.talend.mdm.webapp.base.server.util.DynamicLabelUtil.getParsedLabel(XMLUtils.styleDocument(
                        parsedDocument, stylesheet));
                // @temp yguo, set the properties to itemmodel
                itemModel.setDynamicLabel(dynamicLB);
            }

            if (itemModel.getChildCount() == 0) {
                return;
            } else {
                for (int i = 0; i < itemModel.getChildCount(); i++) {
                    getDynamicLabel(parsedDocument, baseXpath, (ItemNodeModel) itemModel.getChild(i), metaDataTypes, language);
                }
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private static String replaceForeignPath(String basePath, String dynamicLabel, org.dom4j.Document doc) throws Exception {
        Pattern pattern = Pattern.compile("\\{.*?\\}");//$NON-NLS-1$
        Matcher matcher = pattern.matcher(dynamicLabel);
        List<String> dynamicPathes = new ArrayList<String>();
        while (matcher.find()) {
            dynamicPathes.add(matcher.group().replaceAll("^\\{", "").replaceAll("\\}$", "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

        Configuration config = Configuration.getConfiguration();
        String dataModelPK = config.getModel();
        String xsd = Util.getPort().getDataModel(new WSGetDataModel(new WSDataModelPK(dataModelPK))).getXsdSchema();
        Map<String, XSElementDecl> map = com.amalto.core.util.Util.getConceptMap(xsd);
        Map<String, XSType> typeMap = com.amalto.core.util.Util.getConceptTypeMap(xsd);
        basePath = basePath.startsWith("/") ? basePath.substring(1) : basePath; //$NON-NLS-1$
        XSElementDecl xsed = map.get(basePath.split("/")[0]); //$NON-NLS-1$

        for (String dyPath : dynamicPathes) {
            Element baseEl = (Element) doc.selectSingleNode(basePath);
            try {
                List<?> els = baseEl.selectNodes(dyPath);
                if (els == null)
                    continue;
                String multiValue = ""; //$NON-NLS-1$
                if (els.size() > 0) {

                    for (int i = 0; i < els.size(); i++) {
                        List<org.dom4j.Element> pathNodes = getPathNode((org.dom4j.Element) els.get(i));
                        String key = ((org.dom4j.Element) els.get(i)).getStringValue();
                        Object[] fkObj = getForeign(xsed, pathNodes, 0, typeMap);
                        if (fkObj != null && ((List<String>) fkObj[1]).size() > 0) {
                            String foreignkey = (String) fkObj[0];
                            List<String> fkInfos = (List<String>) fkObj[1];
                            String fkInfoStr = getFKInfo(key, foreignkey, fkInfos);
                            multiValue += fkInfoStr == null ? "" : fkInfoStr; //$NON-NLS-1$
                         } else {
                            multiValue += key == null ? "" : key; //$NON-NLS-1$
                        }
                    }

                    dynamicLabel = dynamicLabel.replace("{" + dyPath + "}", multiValue == null ? "" : multiValue); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                }
            } catch (Exception e) {
                continue;
            }
        }

        return dynamicLabel;
    }

    private static Object[] getForeign(XSElementDecl xsed, List<org.dom4j.Element> pathNodes, int pos, Map<String, XSType> typeMap) {

        XSType xsct = null;
        org.dom4j.Element el = pathNodes.get(pos);
        Attribute attr = el.attribute("xsi:type"); //$NON-NLS-1$
        String xsiType = attr == null ? null : attr.getStringValue();
        if (xsiType != null && !xsiType.equals("")) { //$NON-NLS-1$
            xsct = typeMap.get(xsiType);
        } else {
            xsct = xsed.getType();
        }

        if (pos < pathNodes.size() - 1) {
            XSParticle[] xsp = ((XSComplexType) xsct).getContentType().asParticle().getTerm().asModelGroup().getChildren();
            for (XSParticle xs : xsp) {
                List<XSElementDecl> dels = getElementDecls(xs);
                for (XSElementDecl del : dels) {
                    if (del.getName().equals(pathNodes.get(pos + 1).getName())) {
                        Object[] fkObj = getForeign(del, pathNodes, pos + 1, typeMap);
                        if (fkObj != null) {
                            return fkObj;
                        }
                    }
                }
            }
        } else {
            XSAnnotation anno = xsed.getAnnotation();
            if (anno != null) {
                org.w3c.dom.Element annotation = (org.w3c.dom.Element) anno.getAnnotation();
                if (annotation != null) {
                    NodeList annotList = annotation.getChildNodes();
                    if (annotList != null) {
                        Object[] fkObj = new Object[2];
                        String fk = null;
                        List<String> fkInfo = new ArrayList<String>();
                        for (int k = 0; k < annotList.getLength(); k++) {
                            if ("appinfo".equals(annotList.item(k).getLocalName())) { //$NON-NLS-1$ 
                                Node source = annotList.item(k).getAttributes().getNamedItem("source"); //$NON-NLS-1$
                                if (source == null)
                                    continue;
                                String appinfoSource = source.getNodeValue();
                                if ("X_ForeignKey".equals(appinfoSource)) { //$NON-NLS-1$
                                    fk = annotList.item(k).getFirstChild().getNodeValue();
                                    ;
                                } else if ("X_ForeignKeyInfo".equals(appinfoSource)) { //$NON-NLS-1$
                                    fkInfo.add(annotList.item(k).getFirstChild().getNodeValue());
                                }
                            }
                        }
                        if (fk != null) {
                            fkObj[0] = fk;
                            fkObj[1] = fkInfo;
                            return fkObj;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static List<XSElementDecl> getElementDecls(XSParticle xs) {
        List<XSElementDecl> elDecls = new ArrayList<XSElementDecl>();
        if (xs.getTerm().asModelGroup() != null) { // is complex type
            XSParticle[] xsps = xs.getTerm().asModelGroup().getChildren();
            for (int i = 0; i < xsps.length; i++) {
                elDecls.addAll(getElementDecls(xsps[i]));
            }
        }
        XSElementDecl del = xs.getTerm().asElementDecl();
        if (del != null) {
            elDecls.add(del);
        }
        return elDecls;
    }

    private static String getFKInfo(String key, String foreignkey, List<String> fkInfos) {
        try {
            if (key == null || key.trim().length() == 0)
                return null;

            List<String> ids = new ArrayList<String>();

            if (!key.matches("^\\[(.*?)\\]$")) { //$NON-NLS-1$
                ids.add(key);
            } else {
                Pattern p = Pattern.compile("\\[(.*?)\\]"); //$NON-NLS-1$
                Matcher m = p.matcher(key);
                while (m.find()) {
                    ids.add(m.group(1));
                }
            }

            // Collections.reverse(ids);
            String concept = Util.getForeignPathFromPath(foreignkey);
            concept = concept.split("/")[0]; //$NON-NLS-1$
            Configuration config = Configuration.getConfiguration();
            String dataClusterPK = config.getCluster();

            WSItemPK wsItem = new WSItemPK(new WSDataClusterPK(dataClusterPK), concept, (String[]) ids.toArray(new String[ids
                    .size()]));
            WSItem item = Util.getPort().getItem(new WSGetItem(wsItem));
            if (item != null) {
                String content = item.getContent();
                Node node = Util.parse(content).getDocumentElement();
                if (fkInfos.size() > 0) {
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < fkInfos.size(); i++) {
                        String info = fkInfos.get(i);
                        JXPathContext jxpContext = JXPathContext.newContext(node);
                        jxpContext.setLenient(true);
                        info = info.replaceFirst(concept + "/", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        String fkinfo = (String) jxpContext.getValue(info, String.class);
                        if (fkinfo != null && fkinfo.length() != 0) {
                            sb.append(fkinfo);
                        }
                        if (i < fkInfos.size() - 1 && fkInfos.size() > 1) {
                            sb.append("-"); //$NON-NLS-1$
                        }
                    }
                    return sb.toString();
                } else {
                    return key;
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            return key;
        }
        return null;
    }

    private static List<org.dom4j.Element> getPathNode(org.dom4j.Element el) {
        List<org.dom4j.Element> pathEls = new ArrayList<org.dom4j.Element>();
        org.dom4j.Element currentEl = el;
        while (currentEl != null) {
            pathEls.add(0, currentEl);
            currentEl = currentEl.getParent();
        }
        return pathEls;
    }
}
