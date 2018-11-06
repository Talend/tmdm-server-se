/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.talend.mdm.commmon.util.datamodel.management.BusinessConcept;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.view.ViewPOJO;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.XtentisException;
import com.amalto.core.webservice.WSConceptKey;
import com.amalto.core.webservice.WSCount;
import com.amalto.core.webservice.WSDataClusterPK;
import com.amalto.core.webservice.WSDataModelPK;
import com.amalto.core.webservice.WSGetBusinessConceptKey;
import com.amalto.core.webservice.WSGetItem;
import com.amalto.core.webservice.WSItemPK;
import com.amalto.core.webservice.WSString;
import com.amalto.core.webservice.WSStringPredicate;
import com.amalto.core.webservice.WSWhereAnd;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.core.webservice.WSWhereItem;
import com.amalto.core.webservice.WSWhereOperator;
import com.amalto.core.webservice.WSWhereOr;
import com.amalto.core.webservice.XtentisPort;
import com.amalto.webapp.core.bean.Configuration;
import com.amalto.webapp.core.dmagent.SchemaWebAgent;
import com.amalto.xmlserver.interfaces.WhereCondition;
import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSElementDecl;

public abstract class Util {

    public static int _AUTO_ = 0;

    public static int _FORCE_RMI_ = 1;

    public static int _FORCE_WEB_SERVICE_ = 2;

    public static String DATE_FORMAT_PREFIX = "%t"; //$NON-NLS-1$  

    private static final String PROVISIONING_CONCEPT = "User"; //$NON-NLS-1$

    public static final String DATACLUSTER_PK = "PROVISIONING"; //$NON-NLS-1$

    public static final String DEFAULT_LANGUAGE = "en"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(Util.class);

    /*********************************************************************
     * WEB SERVICES
     *********************************************************************/

    public static XtentisPort getPort() throws XtentisWebappException {
        return BeanDelegatorContainer.getInstance().getXtentisWSDelegator();
    }

    public static String getXML(Class<?> c, String filename) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(c.getResourceAsStream(filename)));
        String xml = ""; //$NON-NLS-1$
        String line;
        while ((line = in.readLine()) != null) {
            xml += line + "\n"; //$NON-NLS-1$
        }
        return xml;
    }

    /**
     * Join an array of strings into a single string using a separator
     * 
     * @return a single string or null
     */
    public static String joinStrings(String[] strings, String separator) {
        if (strings == null) {
            return null;
        }
        String res = ""; //$NON-NLS-1$
        for (int i = 0; i < strings.length; i++) {
            res += (i > 0) ? separator : ""; //$NON-NLS-1$
            res += strings[i];
        }
        return res;
    }

    /**
     * Returns the first part - eg. the concept - from the path
     * 
     * @return the Concept Name
     */
    public static String getConceptFromPath(String path) {
        if (path == null || path.trim().length() == 0) {
            return null;
        }
        if (path.startsWith("/")) { //$NON-NLS-1$
            path = path.substring(1);
        }

        Pattern p = Pattern.compile("(.*?)[\\[|/].*"); //$NON-NLS-1$
        if (!path.endsWith("/")) { //$NON-NLS-1$
            path += "/"; //$NON-NLS-1$
        }
        Matcher m = p.matcher(path);
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }

    public static String getForeignPathFromPath(String path) {
        int pos = path.indexOf("["); //$NON-NLS-1$
        if (pos != -1) {
            return path.substring(0, pos);
        }
        return path;

    }

    public static WSWhereCondition getConditionFromPath(String path) {
        Pattern p = Pattern.compile("(.*?)\\[(.*?)(&=|!=|>=|<=|>|<|=)(.*?)\\].*"); //$NON-NLS-1$
        if (!path.endsWith("/")) { //$NON-NLS-1$
            path += "/"; //$NON-NLS-1$
        }
        Matcher m = p.matcher(path);
        if (m.matches()) {
            WSWhereCondition wc = new WSWhereCondition();
            wc.setLeftPath(m.group(2).trim());
            com.amalto.core.webservice.WSWhereOperator operator = changeToWSOperator(m.group(3));
            wc.setOperator(operator);
            wc.setRightValueOrPath(m.group(4).trim().replaceAll("'|\"", "")); //$NON-NLS-1$ //$NON-NLS-2$
            wc.setSpellCheck(true);
            wc.setStringPredicate(WSStringPredicate.NONE);
            return wc;
        }
        return null;
    }

    public static WSWhereItem getConditionFromFKFilter(String foreignKey, String foreignKeyInfo, String fkFilter) {
        return getConditionFromFKFilter(foreignKey, foreignKeyInfo, fkFilter, true);
    }

    public static WSWhereItem getConditionFromFKFilter(String foreignKey, String foreignKeyInfo, String fkFilter,
            boolean formatFkValue) {
        if (fkFilter == null || fkFilter.length() == 0) {
            return null;
        }
        if (fkFilter.equals("null")) { //$NON-NLS-1$
            return null;
        }
        int additionalInfo = fkFilter.indexOf("-", fkFilter.lastIndexOf("#") > -1 ? fkFilter.lastIndexOf("#") + 1 : 0); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        String additionalValue = null;
        if (additionalInfo != -1) {
            additionalValue = fkFilter.substring(additionalInfo + 1);
            fkFilter = fkFilter.substring(0, additionalInfo);
        }
        String[] criterias = fkFilter.split("#"); //$NON-NLS-1$
        ArrayList<WSWhereItem> condition = new ArrayList<WSWhereItem>();
        for (String cria : criterias) {
            String[] values = cria.split("\\$\\$"); //$NON-NLS-1$
            WSWhereCondition wc = Util.convertLine(values);
            if (wc != null) {
                if (formatFkValue) {
                    if (isFkPath(values[0])) {
                        wc.setRightValueOrPath(wrapFkValue(wc.getRightValueOrPath()));
                    } else {
                        wc.setRightValueOrPath(unwrapFkValue(wc.getRightValueOrPath()));
                    }
                }
                condition.add(new WSWhereItem(wc, null, null));
            }
        }
        if (additionalInfo != -1) {
            String[] keyInfos = (foreignKeyInfo != null && !foreignKeyInfo.trim().isEmpty()) ? foreignKeyInfo.split(",") //$NON-NLS-1$
                    : foreignKey.split(","); //$NON-NLS-1$
            for (String keyInfo : keyInfos) {
                String[] values = new String[] { keyInfo, "Contains", additionalValue.equals(".*") ? "." : additionalValue }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                WSWhereCondition wc = Util.convertLine(values);
                if (wc != null) {
                    condition.add(new WSWhereItem(wc, null, null));
                }
            }
        }
        if (condition.size() > 0) {
            return makeWhereItem(condition);
        } else {
            return null;
        }
    }

    public static WSWhereItem makeWhereItem(List<WSWhereItem> conditions) {
        List<Object> conds = new ArrayList<Object>();
        for (int i = 0; i < conditions.size(); i++) {
            WSWhereItem item = conditions.get(i);
            conds.add(item);
            if (i < conditions.size() - 1) {
                WSStringPredicate predicate = item.getWhereCondition().getStringPredicate();
                switch (predicate) {
                case NOT:
                case EXACTLY:
                case STRICTAND:
                case NONE:
                    predicate = WSStringPredicate.AND;
                    break;
                }
                conds.add(predicate);
            }
        }
        Stack<WSStringPredicate> stackOp = new Stack<WSStringPredicate>();
        List<Object> rpn = new ArrayList<Object>();
        for (Object item : conds) {
            if (item instanceof WSWhereItem) {
                rpn.add(item);
            } else {
                WSStringPredicate predicate = (WSStringPredicate) item;
                while (!stackOp.isEmpty()) {
                    rpn.add(stackOp.pop());
                }
                stackOp.push(predicate);
            }
        }
        while (!stackOp.isEmpty()) {
            rpn.add(stackOp.pop());
        }
        Stack<WSWhereItem> whereStack = new Stack<WSWhereItem>();
        for (Object o : rpn) {
            if (o instanceof WSWhereItem) {
                whereStack.push((WSWhereItem) o);
            } else if (o instanceof WSStringPredicate) {
                if (WSStringPredicate.OR.equals(o)) {
                    WSWhereItem item1 = whereStack.pop();
                    WSWhereItem item2 = whereStack.pop();
                    WSWhereOr or = new WSWhereOr(new WSWhereItem[] { item2, item1 });
                    whereStack.push(new WSWhereItem(null, null, or));
                } else if (WSStringPredicate.AND.equals(o)) {
                    WSWhereItem item1 = whereStack.pop();
                    WSWhereItem item2 = whereStack.pop();
                    WSWhereAnd and = new WSWhereAnd(new WSWhereItem[] { item2, item1 });
                    whereStack.push(new WSWhereItem(null, and, null));
                }
            }
        }
        return whereStack.pop();
    }

    @SuppressWarnings("nls")
    public static WSWhereCondition convertLine(String[] values) {
        WSWhereCondition wc = new WSWhereCondition();
        if (values.length < 3) {
            if (values.length == 2 && WhereCondition.EMPTY_NULL.equals(values[1])) {
                wc.setOperator(WSWhereOperator.EMPTY_NULL);
            } else {
                return null;
            }
        }

        wc.setLeftPath(values[0]);
        if (values.length >= 3) {
            WSWhereOperator operator = null;
            if (values[1].equals("Contains")) {
                operator = WSWhereOperator.CONTAINS;
            } else if (values[1].equals("Contains Text Of")) {
                operator = WSWhereOperator.JOIN;
            } else if (values[1].equals("=")) {
                operator = WSWhereOperator.EQUALS;
            } else if (values[1].equals(">")) {
                operator = WSWhereOperator.GREATER_THAN;
            } else if (values[1].equals(">=")) {
                operator = WSWhereOperator.GREATER_THAN_OR_EQUAL;
            } else if (values[1].equals("<")) {
                operator = WSWhereOperator.LOWER_THAN;
            } else if (values[1].equals("<=")) {
                operator = WSWhereOperator.LOWER_THAN_OR_EQUAL;
            } else if (values[1].equals("!=")) {
                operator = WSWhereOperator.NOT_EQUALS;
            } else if (values[1].equals("Starts With")) {
                operator = WSWhereOperator.STARTSWITH;
            } else if (values[1].equals(WhereCondition.EMPTY_NULL)) {
                operator = WSWhereOperator.EMPTY_NULL;
            }
            wc.setOperator(operator);
            if (values[2] != null && values[2].matches("^\".*\"$")) {
                values[2] = values[2].substring(1, values[2].length() - 1);
            }
            wc.setRightValueOrPath(values[2]);
        }
        if (values.length >= 4) {
            WSStringPredicate predicate = null;
            if (values[3].equals("")) {
                predicate = WSStringPredicate.NONE;
            } else if (values[3].equals("Or")) {
                predicate = WSStringPredicate.OR;
            }
            if (values[3].equals("And")) {
                predicate = WSStringPredicate.AND;
            }
            if (values[3].equals("Strict And")) {
                predicate = WSStringPredicate.STRICTAND;
            }
            if (values[3].equals("Exactly")) {
                predicate = WSStringPredicate.EXACTLY;
            }
            if (values[3].equals("Not")) {
                predicate = WSStringPredicate.NOT;
            }
            wc.setStringPredicate(predicate);
        } else {
            wc.setStringPredicate(WSStringPredicate.NONE);
        }
        return wc;
    }

    public static String wrapFkValue(String value) {
        if (value.startsWith("[") && value.endsWith("]")) { //$NON-NLS-1$//$NON-NLS-2$
            return value;
        }
        return "[" + value + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String unwrapFkValue(String value) {
        if (value.startsWith("[") && value.endsWith("]")) { //$NON-NLS-1$ //$NON-NLS-2$
            if (value.contains("][")) { //$NON-NLS-1$
                return value;
            } else {
                return value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

    public static boolean isFkPath(String fkPath) {
        String concept = fkPath.split("/")[0]; //$NON-NLS-1$
        try {
            BusinessConcept businessConcept = SchemaWebAgent.getInstance().getBusinessConcept(concept);
            businessConcept.load();
            Map<String, String> fkMap = businessConcept.getForeignKeyMap();
            if (fkMap != null && fkMap.containsKey("/" + fkPath)) { //$NON-NLS-1$
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @SuppressWarnings("nls")
    public static com.amalto.core.webservice.WSWhereOperator changeToWSOperator(String operator) {
        if ("=".equals(operator)) {
            return com.amalto.core.webservice.WSWhereOperator.EQUALS;
        }
        if ("!=".equals(operator)) {
            return com.amalto.core.webservice.WSWhereOperator.NOT_EQUALS;
        }
        if ("<".equals(operator)) {
            return com.amalto.core.webservice.WSWhereOperator.LOWER_THAN;
        }
        if ("<=".equals(operator)) {
            return com.amalto.core.webservice.WSWhereOperator.LOWER_THAN_OR_EQUAL;
        }
        if (">".equals(operator)) {
            return com.amalto.core.webservice.WSWhereOperator.GREATER_THAN;
        }
        if (">=".equals(operator)) {
            return com.amalto.core.webservice.WSWhereOperator.GREATER_THAN_OR_EQUAL;
        }
        if ("&=".equals(operator)) {
            return com.amalto.core.webservice.WSWhereOperator.CONTAINS;
        }
        return null;
    }

    /**
     * Returns a namespaced root element of a document Useful to create a namespace holder element
     * 
     * @return the root Element
     */
    public static Element getRootElement(String elementName, String namespace, String prefix) throws Exception {
        try {
            DocumentBuilder builder = MDMXMLUtils.getDocumentBuilderWithNamespace().get();
            DOMImplementation impl = builder.getDOMImplementation();
            Document namespaceHolder = impl.createDocument(namespace, (prefix == null ? "" : prefix + ":") + elementName, null); //$NON-NLS-1$ //$NON-NLS-2$
            Element rootNS = namespaceHolder.getDocumentElement();
            rootNS.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:" + prefix, namespace); //$NON-NLS-1$ //$NON-NLS-2$
            return rootNS;
        } catch (Exception e) {
            String err = "Error creating a namespace holder document: " + e.getLocalizedMessage(); //$NON-NLS-1$
            throw new Exception(err);
        }
    }

    public static Document parse(String xmlString) throws Exception {
        return parse(xmlString, null);
    }

    public static Document parse(String xmlString, String schema) throws Exception {
        // parse
        Document d;
        SAXErrorHandler seh = new SAXErrorHandler();
        try {
            // initialize the sax parser which uses Xerces
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Schema validation based on schemaURL
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            factory.setValidating((schema != null));
            factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$ //$NON-NLS-2$
            if (schema != null) {
                factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", new InputSource(new StringReader( //$NON-NLS-1$
                        schema)));
            }
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            builder.setErrorHandler(seh);
            d = builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            String err = "Unable to parse the document" + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage() + "\n " //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                    + xmlString;
            throw new Exception(err);
        }
        // check if document parsed correctly against the schema
        if (schema != null) {
            String errors = seh.getErrors();
            if (!errors.equals("")) { //$NON-NLS-1$
                String err = "Document  did not parse against schema: \n" + errors + "\n" + xmlString; //$NON-NLS-1$//$NON-NLS-2$
                throw new Exception(err);
            }
        }
        return d;
    }

    public static String[] getTextNodes(Node contextNode, String xPath) throws XtentisWebappException {
        // test for hard-coded values
        if (xPath.startsWith("\"") && xPath.endsWith("\"")) { //$NON-NLS-1$ //$NON-NLS-2$
            return new String[] { xPath.substring(1, xPath.length() - 1) };
        }
        // test for incomplete path (elements missing /text())
        if (!xPath.matches(".*@[^/\\]]+")) { //$NON-NLS-1$
            if (!xPath.endsWith(")")) { //$NON-NLS-1$
                xPath += "/text()"; //$NON-NLS-1$
            }
        }
        try {
            NodeList nodeList = com.amalto.core.util.Util.getNodeList(contextNode, xPath);
            String[] results = new String[nodeList.getLength()];
            for (int i = 0; i < nodeList.getLength(); i++) {
                results[i] = nodeList.item(i).getNodeValue();
            }
            return results;
        } catch (Exception e) {
            String err = "Unable to get the text node(s) of " + xPath + ": " + e.getClass().getName() + ": " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + e.getLocalizedMessage();
            throw new XtentisWebappException(err, e);
        }
    }

    /**
     * Computes an md5 hash of a string.
     * 
     * @param text the hashed string
     * @return the string hash
     * @exception NullPointerException if text is null
     */
    public static byte[] md5(String text, String charset) {
        // arguments check
        if (text == null) {
            throw new NullPointerException("null text"); //$NON-NLS-1$
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
            md.update(text.getBytes(charset));
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot find MD5 algorithm"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("No such encoding: " + charset); //$NON-NLS-1$
        }
    }

    /**
     * Computes an md5 hash and returns the result as a string in hexadecimal format.
     * 
     * @param text the hashed string
     * @return the string hash
     * @exception NullPointerException if text is null
     */
    public static String md5AsHexString(String text, String charset) {
        return toHexString(md5(text, charset));
    }

    /**
     * Returns a string in the hexadecimal format.
     * 
     * @param bytes the converted bytes
     * @return the hexadecimal string representing the bytes data
     * @throws IllegalArgumentException if the byte array is null
     */
    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("byte array must not be null"); //$NON-NLS-1$
        }
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            hex.append(Character.forDigit((aByte & 0XF0) >> 4, 16));
            hex.append(Character.forDigit((aByte & 0X0F), 16));
        }
        return hex.toString();
    }

    /**
     * store the info of datacluster and datamodel to PROVISIONING.
     */
    public static void storeProvisioning(String username, String xmlString) throws Exception {
        XmlServer server = com.amalto.core.util.Util.getXmlServerCtrlLocal();
        server.start("PROVISIONING"); //$NON-NLS-1$
        server.putDocumentFromString(xmlString, "PROVISIONING" + "." + "User" + "." + username, "PROVISIONING"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        server.commit("PROVISIONING"); //$NON-NLS-1$
    }

    /**
     * @return gives the operator associated to the string 'option'
     */
    public static WSWhereOperator getOperator(String option) {
        WSWhereOperator res = WSWhereOperator.valueOf(option);
        return res;
    }

    public static WSWhereItem buildWhereItems(String criteria) {
        Pattern p = Pattern.compile("\\((.*)\\)"); //$NON-NLS-1$
        Matcher m = p.matcher(criteria);
        if (m.matches()) {
            criteria = m.group(1);
        }
        String[] criterias = criteria.split("[\\s]+OR[\\s]+"); //$NON-NLS-1$
        ArrayList<WSWhereItem> conditions = new ArrayList<WSWhereItem>();
        for (String cria : criterias) {
            ArrayList<WSWhereItem> condition = new ArrayList<WSWhereItem>();
            m = p.matcher(cria);
            if (m.matches()) {
                cria = m.group(1);
            }
            String[] subCriterias = cria.split("[\\s]+AND[\\s]+"); //$NON-NLS-1$
            // add by ymli; fix the bug: 0011974. remove "(" at the left and ")" at the right
            for (String subCria : subCriterias) {
                if (subCria.startsWith("(")) { //$NON-NLS-1$
                    subCria = subCria.substring(1);
                }
                if (subCria.endsWith(")")) { //$NON-NLS-1$
                    subCria = subCria.substring(0, subCria.length() - 1);
                }
                WSWhereItem whereItem = buildWhereItem(subCria);
                if (whereItem != null) {
                    condition.add(whereItem);
                }
            }
            if (condition.size() > 0) {
                WSWhereAnd and = new WSWhereAnd(condition.toArray(new WSWhereItem[condition.size()]));
                WSWhereItem whand = new WSWhereItem(null, and, null);
                conditions.add(whand);
            }
        }
        WSWhereOr or = new WSWhereOr(conditions.toArray(new WSWhereItem[conditions.size()]));
        return new WSWhereItem(null, null, or);
    }

    public static WSWhereItem buildWhereItem(String criteria) {
        WSWhereItem wi;
        String[] filters = criteria.split(" "); //$NON-NLS-1$
        String filterXpaths, filterOperators, filterValues;
        filterXpaths = filters[0];
        filterOperators = filters[1];
        if (filters.length <= 2) {
            filterValues = " "; //$NON-NLS-1$
        } else if (filters.length == 3) {
            filterValues = filters[2];
        } else {// more than 3 mean filterValues contains whitespace
            StringBuilder sb = new StringBuilder();
            for (int i = 2; i < filters.length; i++) {
                sb.append(filters[i]);
                if (i < filters.length - 1) {
                    sb.append(" ");//$NON-NLS-1$
                }
            }
            filterValues = sb.toString();
        }
        if (filterXpaths == null || filterXpaths.trim().isEmpty()) {
            return null;
        }
        WSWhereCondition wc = new WSWhereCondition(filterXpaths, Util.getOperator(filterOperators), filterValues,
                WSStringPredicate.NONE, false);
        ArrayList<WSWhereItem> conditions = new ArrayList<WSWhereItem>();
        WSWhereItem item = new WSWhereItem(wc, null, null);
        conditions.add(item);
        if (conditions.size() == 0) {
            wi = null;
        } else {
            WSWhereAnd and = new WSWhereAnd(conditions.toArray(new WSWhereItem[conditions.size()]));
            wi = new WSWhereItem(null, and, null);
        }
        return wi;
    }

    // added by lzhang, for 22001
    // explicit replaced current element with root element
    public static String getFormatedFKInfo(String info, String conceptName) {
        info = info.substring(info.startsWith("/") ? 1 : 0); //$NON-NLS-1$
        String formatedInfo = info;
        if (info.startsWith("./")) { //$NON-NLS-1$
            formatedInfo = info.replaceFirst("./", conceptName + "/"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return formatedInfo;
    }

    public static String getInjectedXpath(String fkFilter) {
        return fkFilter.substring(6);
    }

    public static boolean isCustomFilter(String fkFilter) {
        boolean isCustom = false;
        if (fkFilter != null && fkFilter.startsWith("$CFFP:")) { //$NON-NLS-1$
            isCustom = true;
        }
        return isCustom;
    }

    public static boolean isTransformerExist(String transformerPK) {
        try {
            boolean isBeforeSavingTransformerExist = false;
            Collection<TransformerV2POJOPK> wst = com.amalto.core.util.Util.getTransformerV2CtrlLocal().getTransformerPKs("*"); //$NON-NLS-1$
            for (TransformerV2POJOPK id : wst) {
                if (id.getIds()[0].equals(transformerPK)) {
                    isBeforeSavingTransformerExist = true;
                    break;
                }
            }
            return isBeforeSavingTransformerExist;
        } catch (XtentisException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e);
            }
        }
        return false;
    }

    public static boolean checkDCAndDM(String dataContainer, String dataModel) {
        Configuration config;
        try {
            config = Configuration.getConfiguration();
            return config.getCluster().equals(dataContainer) && config.getModel().equals(dataModel);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            return false;
        }
    }

    @Deprecated
    public static String formatDate(String formats, Calendar date) {
        String formatValue = formats;
        if (formats.startsWith(DATE_FORMAT_PREFIX)) {
            while (true) {
                if (!formatValue.contains(DATE_FORMAT_PREFIX)) {
                    break;
                }
                String format = formatValue.substring(formatValue.indexOf(DATE_FORMAT_PREFIX),
                        formatValue.indexOf(DATE_FORMAT_PREFIX) + 3);
                String valueStr = String.format(java.util.Locale.ENGLISH, format, date);
                formatValue = formatValue.replace(format, valueStr);
            }
        } else {
            formatValue = String.format(java.util.Locale.ENGLISH, formats, date);
        }

        return formatValue;
    }

    public static String convertAbsolutePath(String currentPath, String xpath) {
        StringBuilder sb = new StringBuilder();
        String[] ops = xpath.split("/"); //$NON-NLS-1$
        String[] eles = currentPath.split("/"); //$NON-NLS-1$
        int num = 0;
        if (xpath.startsWith("..")) { //$NON-NLS-1$
            for (String op : ops) {
                if (op.equals("..")) { //$NON-NLS-1$
                    num += 1;
                }
            }
            for (int i = 0; i < eles.length - num; i++) {
                sb.append(eles[i]);
                sb.append("/"); //$NON-NLS-1$
            }
        } else if (xpath.startsWith(".")) { //$NON-NLS-1$
            sb.append(eles[0]);
            sb.append("/"); //$NON-NLS-1$
        }
        sb.append(ops[ops.length - 1]);
        return sb.toString();
    }

    public static String[] getBusinessConceptKeys(String concept) throws Exception {
        Configuration config = Configuration.getConfiguration();
        String model = config.getModel();

        WSConceptKey key = getPort().getBusinessConceptKey(new WSGetBusinessConceptKey(new WSDataModelPK(model), concept));
        WSConceptKey copyKey = new WSConceptKey();
        copyKey.setFields((String[]) ArrayUtils.clone(key.getFields()));
        copyKey.setSelector(key.getSelector());

        String[] keys = copyKey.getFields();
        for (int i = 0; i < keys.length; i++) {
            if (".".equals(key.getSelector())) { //$NON-NLS-1$
                keys[i] = concept + "/" + keys[i]; //$NON-NLS-1$ 
            } else {
                keys[i] = key.getSelector() + keys[i];
            }
        }
        return keys;
    }

    public static boolean causeIs(Throwable throwable, Class<?> cls) {
        Throwable currentCause = throwable;
        while (currentCause != null) {
            if (cls.isInstance(currentCause)) {
                return true;
            }
            currentCause = currentCause.getCause();
        }
        return false;
    }

    public static <T> T cause(Throwable throwable, Class<T> cls) {
        Throwable currentCause = throwable;
        while (currentCause != null) {
            if (cls.isInstance(currentCause)) {
                return (T) currentCause;
            }
            currentCause = currentCause.getCause();
        }
        return null;
    }

    @Deprecated
    public static String getExceptionMessage(String message, String language) {
        if (message == null || message.contains("<msg/>")) { //$NON-NLS-1$
            return ""; //$NON-NLS-1$
        }
        // Message tip : "<msg>[EN:validate error][FR:validate error]</msg>"
        if (message.contains("<msg>")) { //$NON-NLS-1$
            int index = message.indexOf(language.toUpperCase() + ":"); //$NON-NLS-1$
            if (index != -1) {
                return message.substring(index + language.length() + 1, index + message.substring(index).indexOf("]")); //$NON-NLS-1$
            } else {
                return message.substring(message.indexOf("<msg>"), message.indexOf("</msg>") + 6); //$NON-NLS-1$//$NON-NLS-2$
            }
        }
        return message;
    }

    // Arrrrg bummer
    public static boolean isElementHiddenForCurrentUser(XSElementDecl decl) throws Exception {
        Set<String> roleSet = new HashSet<String>();
        XSAnnotation xsa = decl.getAnnotation();
        if (xsa == null) {
            return false;
        }
        Element el = (Element) xsa.getAnnotation();
        NodeList annotList = el.getChildNodes();
        for (int k = 0; k < annotList.getLength(); k++) {
            if ("appinfo".equals(annotList.item(k).getLocalName())) { //$NON-NLS-1$
                Node source1 = annotList.item(k).getAttributes().getNamedItem("source"); //$NON-NLS-1$         
                if (source1 == null) {
                    continue;
                }
                String appinfoSource = annotList.item(k).getAttributes().getNamedItem("source").getNodeValue(); //$NON-NLS-1$

                if ("X_Hide".equals(appinfoSource)) { //$NON-NLS-1$
                    roleSet.add(annotList.item(k).getFirstChild().getNodeValue());
                }
            }
        }
        Collection<String> roleArr = LocalUser.getLocalUser().getRoles();
        for (String role : roleArr) {
            if (roleSet.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public static void filterAuthViews(Map<String, String> viewMap) throws Exception {
        if (viewMap == null || viewMap.size() == 0) {
            return;
        }
        if (Webapp.INSTANCE.isEnterpriseVersion()) {
            for (String viewInstanceId : viewMap.keySet()) {
                ILocalUser localUser = LocalUser.getLocalUser();
                if (!localUser.userCanWrite(ViewPOJO.class, viewInstanceId)
                        && !localUser.userCanRead(ViewPOJO.class, viewInstanceId)) {
                    viewMap.remove(viewInstanceId);
                }
            }
        }
    }

    public static String getDefaultLanguage() throws Exception {
        String defaultLanguage = ""; //$NON-NLS-1$
        String identity = LocalUser.getLocalUser().getIdentity();
        if (com.amalto.core.util.Util.isEnterprise()) {
            WSWhereItem wi = new WSWhereItem();
            String criteria = "User/id EQUALS " + identity; //$NON-NLS-1$
            wi = Util.buildWhereItems(criteria);
            WSDataClusterPK wsDataClusterPK = new WSDataClusterPK(DATACLUSTER_PK);
            WSString countResult = Util.getPort().count(new WSCount(wsDataClusterPK, PROVISIONING_CONCEPT, wi, -1));
            int totalLength = countResult.getValue() == null ? 0 : Integer.valueOf(countResult.getValue());
            if (totalLength == 0) {
                return defaultLanguage;
            }
        }
        
        WSItemPK itemPK = new WSItemPK(new WSDataClusterPK(DATACLUSTER_PK), PROVISIONING_CONCEPT, new String[] { identity });
        if (identity != null && identity.length() > 0) {
            Document doc = XMLUtils.parse(Util.getPort().getItem(new WSGetItem(itemPK)).getContent());
            if (doc.getElementsByTagName("language") != null) { //$NON-NLS-1$
                Node language = doc.getElementsByTagName("language").item(0); //$NON-NLS-1$
                if (language != null) {
                    return language.getTextContent();
                }
            }
        }
        return defaultLanguage;
    }
}
