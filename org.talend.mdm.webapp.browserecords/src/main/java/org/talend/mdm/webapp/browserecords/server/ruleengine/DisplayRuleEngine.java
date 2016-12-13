/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.ruleengine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.QName;
import org.talend.mdm.webapp.base.shared.ExpressionUtil;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.webapp.core.util.XmlUtil;

public class DisplayRuleEngine {

    private static final Logger LOG = Logger.getLogger(DisplayRuleEngine.class);

    private final Namespace namespace = new Namespace("xsi", "http://www.w3.org/2001/XMLSchema-instance"); //$NON-NLS-1$//$NON-NLS-2$

    String concept;

    Map<String, TypeModel> metaDatas;

    public DisplayRuleEngine(Map<String, TypeModel> metaDatas, String concept) {
        this.metaDatas = metaDatas;
        this.concept = concept;
    }

    public List<TypeModel> orderDataModels(org.dom4j.Document doc4j) {
        List<TypeModel> hasBeenProcessed = new ArrayList<TypeModel>();
        Set<TypeModel> stack = new HashSet<TypeModel>();
        Iterator<String> typeIter = metaDatas.keySet().iterator();
        while (typeIter.hasNext()) {
            String typePath = typeIter.next();
            TypeModel bean = metaDatas.get(typePath);
            if (bean.getDefaultValueExpression() != null) {
                dependentDataTypeOrder(hasBeenProcessed, stack, bean, doc4j);
            }
        }
        return hasBeenProcessed;
    }

    private void dependentDataTypeOrder(List<TypeModel> hasBeenProcessed, Set<TypeModel> stack, TypeModel bean,
            org.dom4j.Document doc4j) {
        stack.add(bean);
        ExpressionUtil expUtil = new ExpressionUtil(bean.getDefaultValueExpression());
        List<String> typePathes = expUtil.getDepTypes();
        if (typePathes != null && typePathes.size() > 0) {
            for (String typePath : typePathes) {
                if (typePath.startsWith("./") || typePath.startsWith("../")) { //$NON-NLS-1$//$NON-NLS-2$
                    String xpath = CommonUtil.typePathToXpath(bean.getTypePath());
                    Node node = doc4j.selectSingleNode(xpath);
                    if (node != null) {
                        node = node.selectSingleNode(typePath);
                        if (node != null) {
                            typePath = getRealTypePath((org.dom4j.Element) node);
                        }
                    }
                }
                TypeModel depExpression = metaDatas.get(typePath);
                if (depExpression != null && !stack.contains(depExpression)) {
                    if (depExpression.getDefaultValueExpression() != null) {
                        dependentDataTypeOrder(hasBeenProcessed, stack, depExpression, doc4j);
                    }
                }
            }
        }
        if (!hasBeenProcessed.contains(bean)) {
            hasBeenProcessed.add(bean);
        }
    }

    private String getRealTypePath(org.dom4j.Element el) {
        String realXPath = ""; //$NON-NLS-1$
        org.dom4j.Element current = el;
        boolean isFirst = true;
        while (current != null) {
            String name;
            String realType = current.attributeValue(new QName("type", namespace, "xsi:type")); //$NON-NLS-1$ //$NON-NLS-2$
            if (realType != null && realType.trim().length() > 0) {
                name = current.getName() + ":" + realType; //$NON-NLS-1$
            } else {
                name = current.getName();
            }

            current = current.getParent();
            if (isFirst) {
                realXPath = name;
                isFirst = false;
                continue;
            }
            realXPath = name + "/" + realXPath; //$NON-NLS-1$
        }
        return realXPath;
    }

    public List<RuleValueItem> execDefaultValueRule(Document dom4jDoc) {

        List<TypeModel> orderTypes = orderDataModels(dom4jDoc);

        List<RuleValueItem> valueItems = new ArrayList<RuleValueItem>();
        try {
            for (TypeModel model : orderTypes) {
                if (model.getDefaultValueExpression() != null && model.getDefaultValueExpression().trim().length() > 0) {
                    String typePath = model.getTypePath();
                    String xpath = CommonUtil.typePathToXpath(typePath);
                    List nodes = dom4jDoc.selectNodes(xpath);
                    if (nodes != null) {
                        for (Object node : nodes) {
                            Element el = (Element) node;
                            String preciseXPath = getRealXPath(el);
                            String style = genDefaultValueStyle(concept, preciseXPath, model.getDefaultValueExpression(), model);
                            org.dom4j.Document transformedDocumentValue = XMLUtils.styleDocument(
                                    dom4jDoc, style);

                            int beginIndex = preciseXPath.lastIndexOf("/"); //$NON-NLS-1$
                            String matchPath = beginIndex != -1 ? preciseXPath.substring(beginIndex) : preciseXPath;
                            matchPath = matchPath.replaceAll("\\[\\d+\\]$", ""); //$NON-NLS-1$//$NON-NLS-2$
                            org.dom4j.Node valueNode = transformedDocumentValue.selectSingleNode(concept + "/" + matchPath); //$NON-NLS-1$
                            if (valueNode != null) {
                                valueItems.add(new RuleValueItem(preciseXPath, valueNode.getText()));
                                dom4jDoc.selectSingleNode(preciseXPath).setText(valueNode.getText());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return valueItems;
    }

    public List<VisibleRuleResult> execVisibleRule(Document dom4jDoc) {
        List<VisibleRuleResult> valueItems = new ArrayList<VisibleRuleResult>();
        try {
            for (TypeModel model : metaDatas.values()) {
                if (model.getVisibleExpression() != null && model.getVisibleExpression().trim().length() > 0) {
                    boolean isMultiOccurence = model.getMaxOccurs() > 1 || model.getMaxOccurs() < 0;
                    String xpath = CommonUtil.typePathToXpath(model.getTypePath());
                    List nodes = dom4jDoc.selectNodes(xpath);
                    if (nodes != null) {
                        for (Object node : nodes) {
                            Element el = (Element) node;
                            String preciseXPath = getRealXPath(el);
                            String style = genVisibleRuleStyle(concept, preciseXPath, model.getVisibleExpression(),
                                    isMultiOccurence);
                            org.dom4j.Document transformedDocumentValue = XMLUtils.styleDocument(
                                    dom4jDoc, style);
                            int beginIndex = preciseXPath.lastIndexOf("/"); //$NON-NLS-1$
                            String matchPath = beginIndex != -1 ? preciseXPath.substring(beginIndex + 1) : preciseXPath;
                            matchPath = matchPath.replaceAll("\\[\\d+\\]$", ""); //$NON-NLS-1$//$NON-NLS-2$
                            org.dom4j.Node valueNode = transformedDocumentValue.selectSingleNode(concept + "/" + matchPath); //$NON-NLS-1$
                            if (valueNode != null) {
                                org.dom4j.Element elem = (org.dom4j.Element) valueNode;
                                String value = elem.attributeValue("visible"); //$NON-NLS-1$
                                if ("false".equals(value)) { //$NON-NLS-1$
                                    valueItems.add(new VisibleRuleResult(preciseXPath, false));
                                } else if (value == null || "true".equals(value)) { //$NON-NLS-1$
                                    valueItems.add(new VisibleRuleResult(preciseXPath, true));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        return valueItems;
    }

    private String genDefaultValueStyle(String concept, String xpath, String defaultValueRule, TypeModel typeModel) {
        StringBuffer style = new StringBuffer();
        style.append("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" xmlns:t=\"http://www.talend.com/2010/MDM\" version=\"2.0\">"); //$NON-NLS-1$

        style.append("<xsl:output method=\"xml\" indent=\"yes\" omit-xml-declaration=\"yes\"/>"); //$NON-NLS-1$
        style.append("<xsl:template match=\"/" + concept + "\">"); //$NON-NLS-1$//$NON-NLS-2$
        style.append("<xsl:copy>"); //$NON-NLS-1$
        style.append("<xsl:apply-templates select=\"/" + xpath + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
        style.append("</xsl:copy>"); //$NON-NLS-1$
        style.append("</xsl:template>"); //$NON-NLS-1$

        style.append("<xsl:template match=\"/" + xpath + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        style.append("<xsl:copy>"); //$NON-NLS-1$
        style.append("<xsl:choose>"); //$NON-NLS-1$
        if ("boolean".equals(typeModel.getType().getTypeName())) { //$NON-NLS-1$
            style.append("<xsl:when test=\"not(text()) or . = 'false'\">"); //$NON-NLS-1$
        } else {
            style.append("<xsl:when test=\"not(text())\">"); //$NON-NLS-1$
        }

        style.append("<xsl:value-of select=\"" + XmlUtil.escapeXml(defaultValueRule) + "\"/>"); //$NON-NLS-1$ //$NON-NLS-2$
        style.append("</xsl:when> "); //$NON-NLS-1$
        style.append("<xsl:otherwise>"); //$NON-NLS-1$
        style.append("<xsl:value-of select=\".\"/>"); //$NON-NLS-1$
        style.append("</xsl:otherwise>"); //$NON-NLS-1$
        style.append("</xsl:choose> "); //$NON-NLS-1$
        style.append("</xsl:copy>"); //$NON-NLS-1$
        style.append("</xsl:template>"); //$NON-NLS-1$

        style.append("</xsl:stylesheet>"); //$NON-NLS-1$

        return style.toString();
    }

    @SuppressWarnings("nls")
    public static String genVisibleRuleStyle(String concept, String xpath, String visibleExpression, boolean isMultiOccurence) {
        StringBuffer style = new StringBuffer();
        style.append("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" xmlns:fn=\"http://www.w3.org/2005/xpath-functions\" xmlns:t=\"http://www.talend.com/2010/MDM\" version=\"2.0\">"); //$NON-NLS-1$
        style.append("<xsl:output method=\"xml\" indent=\"yes\" omit-xml-declaration=\"yes\"/>"); //$NON-NLS-1$
        style.append("<xsl:template match=\"/" + concept + "\">"); //$NON-NLS-1$//$NON-NLS-2$
        style.append("<xsl:copy>");//$NON-NLS-1$
        style.append("<xsl:apply-templates select=\"/" + xpath + "\"/>");//$NON-NLS-1$ //$NON-NLS-2$
        style.append("</xsl:copy>");//$NON-NLS-1$ 
        style.append("</xsl:template>");//$NON-NLS-1$ 

        style.append("<xsl:template match=\"/" + xpath + "\">");//$NON-NLS-1$ //$NON-NLS-2$
        style.append("<xsl:copy>"); //$NON-NLS-1$
        if (isMultiOccurence) {
            String pathIndex = xpath.substring(xpath.lastIndexOf('[') + 1, xpath.lastIndexOf(']'));
            String matchPath = xpath.replaceAll("\\[\\d+\\]$", "");
            style.append("<xsl:for-each select=\"/" + matchPath + "\">");
            style.append("<xsl:if test=\"position()=" + pathIndex + "\">");
        }
        style.append("<xsl:choose>"); //$NON-NLS-1$
        style.append("<xsl:when test=\"not(" + getPureValue(XmlUtil.escapeXml(visibleExpression)) + ")\">"); //$NON-NLS-1$ //$NON-NLS-2$
        style.append("<xsl:attribute name=\"t:visible\">false</xsl:attribute>"); //$NON-NLS-1$
        style.append("</xsl:when>"); //$NON-NLS-1$
        style.append("<xsl:otherwise>"); //$NON-NLS-1$
        style.append("<xsl:attribute name=\"t:visible\">true</xsl:attribute>"); //$NON-NLS-1$
        style.append("</xsl:otherwise>"); //$NON-NLS-1$
        style.append("</xsl:choose>"); //$NON-NLS-1$
        if (isMultiOccurence) {
            style.append("</xsl:if>");
            style.append("</xsl:for-each>");
        }
        style.append("</xsl:copy>"); //$NON-NLS-1$
        style.append("</xsl:template>"); //$NON-NLS-1$
        style.append("</xsl:stylesheet>"); //$NON-NLS-1$
        return style.toString();
    }

    private static String getPureValue(String displayValue) {
        return displayValue.replaceAll("\r\n", "").replaceAll("\n", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
    }

    public static String getRealXPath(Element el) {
        String realXPath = ""; //$NON-NLS-1$
        Element current = el;
        boolean isFirst = true;
        while (current != null) {
            String name = getElementNameWithIndex(current);
            current = current.getParent();
            if (isFirst) {
                realXPath = name;
                isFirst = false;
                continue;
            }
            realXPath = name + "/" + realXPath; //$NON-NLS-1$

        }
        return realXPath;
    }

    private static String getElementNameWithIndex(Element el) {
        Element parent = el.getParent();
        if (parent != null) {
            List children = parent.elements();
            int index = 0;
            if (children != null) {
                for (Object child : children) {
                    Element childEl = (Element) child;
                    if (childEl.getName().equals(el.getName())) {
                        index++;
                        if (childEl == el) {
                            return el.getName() + "[" + index + "]"; //$NON-NLS-1$//$NON-NLS-2$
                        }
                    }
                }
            }
        }
        return el.getName();
    }
}
