/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.util.core.ITransformerConstants;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.jobox.JobContainer;
import com.amalto.core.objects.DroppedItemPOJO;
import com.amalto.core.objects.DroppedItemPOJOPK;
import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.UpdateReportPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.util.TransformerCallBack;
import com.amalto.core.objects.transformers.util.TransformerContext;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.server.DefaultBackgroundJob;
import com.amalto.core.server.DefaultConfigurationInfo;
import com.amalto.core.server.DefaultCustomForm;
import com.amalto.core.server.DefaultDataCluster;
import com.amalto.core.server.DefaultDataModel;
import com.amalto.core.server.DefaultDroppedItem;
import com.amalto.core.server.DefaultItem;
import com.amalto.core.server.DefaultMenu;
import com.amalto.core.server.DefaultRole;
import com.amalto.core.server.DefaultRoutingOrder;
import com.amalto.core.server.DefaultRoutingRule;
import com.amalto.core.server.DefaultStoredProcedure;
import com.amalto.core.server.DefaultTransformer;
import com.amalto.core.server.DefaultView;
import com.amalto.core.server.DefaultXmlServer;
import com.amalto.core.server.ServerAccess;
import com.amalto.core.server.api.BackgroundJob;
import com.amalto.core.server.api.ConfigurationInfo;
import com.amalto.core.server.api.CustomForm;
import com.amalto.core.server.api.DataCluster;
import com.amalto.core.server.api.DataModel;
import com.amalto.core.server.api.DroppedItem;
import com.amalto.core.server.api.Item;
import com.amalto.core.server.api.Menu;
import com.amalto.core.server.api.RoutingEngine;
import com.amalto.core.server.api.RoutingOrder;
import com.amalto.core.server.api.RoutingRule;
import com.amalto.core.server.api.StoredProcedure;
import com.amalto.core.server.api.View;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.server.routing.RoutingEngineFactory;
import com.amalto.core.webservice.WSMDMJob;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereCondition;
import com.amalto.xmlserver.interfaces.WhereLogicOperator;
import com.amalto.xmlserver.interfaces.XmlServerException;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;

@SuppressWarnings({ "deprecation", "nls" })
public class Util {

    private static final Logger LOGGER = Logger.getLogger(Util.class);

    public static final String ROOT_LOCATION_PARAM = "mdmRootLocation";

    public static final String ROOT_LOCATION_KEY = "mdm.root";

    public static final String ROOT_LOCATION_URL_KEY = "mdm.root.url";

    private static final String USER_PROPERTY_PREFIX = "${user_context";

    private static final ScriptEngineManager SCRIPT_FACTORY = new ScriptEngineManager();

    private static final Pattern extractCharsetPattern = Pattern.compile(".*charset\\s*=(.+)");

    private static final String[] INVALID_ID_CHARACTERS = { "'", "\"", "*", "[", "]" };
    
    private static DocumentBuilderFactory nonValidatingDocumentBuilderFactory;

    private static XmlServer defaultXmlServer;

    private static DataModel defaultDataModel;

    private static ConfigurationInfo configurationInfo;

    private static Menu menu;

    private static com.amalto.core.server.api.Role role;

    private static DataCluster defaultDataCluster;

    private static CustomForm defaultCustomForm;

    private static BackgroundJob defaultBackgroundJob;

    private static View defaultView;

    private static StoredProcedure defaultStoredProcedure;

    private static Item defaultItem;

    private static DroppedItem defaultDroppedItem;

    private static RoutingRule defaultRoutingRule;

    private static RoutingOrder defaultRoutingOrder;

    private static com.amalto.core.server.api.Transformer defaultTransformer;

    private static synchronized DocumentBuilderFactory getDocumentBuilderFactory() {
        if (nonValidatingDocumentBuilderFactory == null) {
            nonValidatingDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
            nonValidatingDocumentBuilderFactory.setNamespaceAware(true);
            nonValidatingDocumentBuilderFactory.setValidating(false);
            nonValidatingDocumentBuilderFactory.setExpandEntityReferences(false);
        }
        return nonValidatingDocumentBuilderFactory;
    }

    public static Document parse(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory;
        factory = getDocumentBuilderFactory();
        factory.setExpandEntityReferences(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        SAXErrorHandler seh = new SAXErrorHandler();
        builder.setErrorHandler(seh);
        Document d = builder.parse(new InputSource(new StringReader(xmlString)));
        // check if document parsed correctly against the schema
        String errors = seh.getErrors();
        if (errors.length() != 0) {
            String err = "Document did not parse against schema: \n" + errors + "\n"
                    + xmlString.substring(0, Math.min(100, xmlString.length()));
            throw new SAXException(err);
        }
        return d;
    }

    public static Document validate(Element element, String schema) throws Exception {
        return BeanDelegatorContainer.getInstance().getValidationDelegator().validation(element, schema);
    }

    public static String[] getTextNodes(Node contextNode, String xPath) throws TransformerException {
        return getTextNodes(contextNode, xPath, contextNode);
    }

    private static String[] getTextNodes(Node contextNode, String xPath, final Node namespaceNode) throws TransformerException {
        String[] results;
        // test for hard-coded values
        if (xPath.startsWith("\"") && xPath.endsWith("\"")) {
            return new String[] { xPath.substring(1, xPath.length() - 1) };
        }
        // test for incomplete path (elements missing /text())
        if (!xPath.matches(".*@[^/\\]]+")) { // attribute
            if (!xPath.endsWith(")")) { // function
                xPath += "/text()";
            }
        }
        try {
            XPath path = XPathFactory.newInstance().newXPath();
            path.setNamespaceContext(new NamespaceContext() {

                @Override
                public String getNamespaceURI(String s) {
                    return namespaceNode.getNamespaceURI();
                }

                @Override
                public String getPrefix(String s) {
                    return namespaceNode.getPrefix();
                }

                @Override
                public Iterator getPrefixes(String s) {
                    return Collections.singleton(namespaceNode.getPrefix()).iterator();
                }
            });
            NodeList xo = (NodeList) path.evaluate(xPath, contextNode, XPathConstants.NODESET);
            results = new String[xo.getLength()];
            for (int i = 0; i < xo.getLength(); i++) {
                results[i] = xo.item(i).getTextContent();
            }
        } catch (Exception e) {
            String err = "Unable to get the text node(s) of " + xPath + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            throw new TransformerException(err);
        }
        return results;

    }

    private static String getFirstTextNode(Node contextNode, String xPath, Node namespaceNode) throws TransformerException {
        String[] res = getTextNodes(contextNode, xPath, namespaceNode);
        if (res.length == 0) {
            return null;
        }
        return res[0];
    }

    public static String getFirstTextNode(Node contextNode, String xPath) throws TransformerException {
        return getFirstTextNode(contextNode, xPath, contextNode);
    }

    /**
     * Get the method of a component by its name
     */
    public static Method getMethod(Object component, String methodName) {
        if (component == null) {
            return null;
        }
        Method[] methods = component.getClass().getMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }

    public static Map<String, XSElementDecl> getConceptMap(String xsd) throws Exception {
        XSOMParser reader = new XSOMParser();
        reader.setAnnotationParser(new DomAnnotationParserFactory());
        reader.setEntityResolver(new SecurityEntityResolver());
        SAXErrorHandler seh = new SAXErrorHandler();
        reader.setErrorHandler(seh);
        reader.parse(new StringReader(xsd));
        XSSchemaSet xss = reader.getResult();
        String errors = seh.getErrors();
        if (errors.length() > 0) {
            throw new SAXException("DataModel parsing error -> " + errors);
        }
        Collection xssList = xss.getSchemas();
        Map<String, XSElementDecl> mapForAll = new HashMap<>();
        Map<String, XSElementDecl> map;
        for (Object xmlSchema : xssList) {
            XSSchema schema = (XSSchema) xmlSchema;
            map = schema.getElementDecls();
            mapForAll.putAll(map);
        }
        return mapForAll;
    }

    public static Map<String, XSType> getConceptTypeMap(String xsd) throws Exception {
        XSOMParser reader = new XSOMParser();
        reader.setAnnotationParser(new DomAnnotationParserFactory());
        reader.setEntityResolver(new SecurityEntityResolver());
        SAXErrorHandler seh = new SAXErrorHandler();
        reader.setErrorHandler(seh);
        reader.parse(new StringReader(xsd));
        XSSchemaSet xss = reader.getResult();
        String errors = seh.getErrors();
        if (errors.length() > 0) {
            throw new SAXException("DataModel parsing error -> " + errors);
        }
        Collection xssList = xss.getSchemas();
        Map<String, XSType> mapForAll = new HashMap<>();
        Map<String, XSType> map;
        for (Object aXssList : xssList) {
            XSSchema schema = (XSSchema) aXssList;
            map = schema.getTypes();
            mapForAll.putAll(map);
        }
        return mapForAll;
    }

    public static String[] getKeyValuesFromItem(Element item, Collection<FieldMetadata> keyFields) throws TransformerException {
        XPath xPath = XPathFactory.newInstance().newXPath();
        String[] ids = new String[keyFields.size()];
        int i = 0;
        for (FieldMetadata keyField : keyFields) {
            try {
                ids[i++] = (String) xPath.compile(keyField.getPath()).evaluate(item, XPathConstants.STRING);
            } catch (XPathExpressionException e) {
                throw new RuntimeException("Unable to find key value in '" + keyField.getPath() + "'.", e);
            }
        }
        return ids;
    }

    /**
     * Generates an xml string from a node with or without the xml declaration (not pretty formatted)
     * 
     * @param n the node
     * @return the xml string
     * @throws TransformerException
     */
    public static String nodeToString(Node n) throws TransformerException {
        return XMLUtils.nodeToString(n, true, LOGGER.isDebugEnabled()).replaceAll("\r\n", "\n");
    }

    /**
     * Get a node list from an xPath
     * 
     * @throws XtentisException
     */
    public static NodeList getNodeList(Document d, String xPath) throws XtentisException {
        return getNodeList(d.getDocumentElement(), xPath);
    }

    /**
     * Get a node list from an xPath
     * 
     * @throws XtentisException
     */
    public static NodeList getNodeList(Node contextNode, String xPath) throws XtentisException {
        try {
            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPathParser = xPathFactory.newXPath();
            xPathParser.setNamespaceContext(new NamespaceContext() {

                @Override
                public String getNamespaceURI(String s) {
                    if ("xsd".equals(s)) {
                        return XMLConstants.W3C_XML_SCHEMA_NS_URI;
                    }
                    return null;
                }

                @Override
                public String getPrefix(String s) {
                    if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(s)) {
                        return "xsd";
                    }
                    return null;
                }

                @Override
                public Iterator getPrefixes(String s) {
                    return Collections.singletonList(s).iterator();
                }
            });
            return (NodeList) xPathParser.evaluate(xPath, contextNode, XPathConstants.NODESET);
        } catch (Exception e) {
            String err = "Unable to get the Nodes List for xpath '" + xPath + "'"
                    + ((contextNode == null) ? "" : " for Node " + contextNode.getLocalName()) + ": " + e.getLocalizedMessage();
            throw new XtentisException(err, e);
        }
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
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            res.append(i > 0 ? separator : StringUtils.EMPTY);
            res.append(strings[i] == null ? StringUtils.EMPTY : strings[i]);
        }
        return res.toString();
    }

    /**
     * @return the username/password
     */
    public static String getUsernameAndPasswordToken() {
        String token = null;
        try {
            String userName = LocalUser.getLocalUser().getUsername();
            String password = LocalUser.getLocalUser().getCredentials();
            token = userName + "/" + password;
        } catch (XtentisException e) {
            LOGGER.error(e);
        }
        return token;
    }

    public static com.amalto.core.server.api.Role getRoleCtrlLocal() {
        if (role == null) {
            role = new DefaultRole();
        }
        return role;
    }

    public static CustomForm getCustomFormCtrlLocal() {
        if (defaultCustomForm == null) {
            defaultCustomForm = new DefaultCustomForm();
        }
        return defaultCustomForm;
    }

    public static RoutingOrder getRoutingOrderV2CtrlLocal() {
        if (defaultRoutingOrder == null) {
            defaultRoutingOrder = new DefaultRoutingOrder();
        }
        return defaultRoutingOrder;
    }

    public static RoutingRule getRoutingRuleCtrlLocal() {
        if (defaultRoutingRule == null) {
            defaultRoutingRule = new DefaultRoutingRule();
        }
        return defaultRoutingRule;
    }

    public static StoredProcedure getStoredProcedureCtrlLocal() {
        if (defaultStoredProcedure == null) {
            defaultStoredProcedure = new DefaultStoredProcedure();
        }
        return defaultStoredProcedure;
    }

    public static Item getItemCtrl2Local() {
        if (defaultItem == null) {
            defaultItem = new DefaultItem();
        }
        return defaultItem;
    }

    public static DroppedItem getDroppedItemCtrlLocal() {
        if (defaultDroppedItem == null) {
            defaultDroppedItem = new DefaultDroppedItem();
        }
        return defaultDroppedItem;
    }

    public static DataModel getDataModelCtrlLocal() {
        if (defaultDataModel == null) {
            defaultDataModel = new DefaultDataModel();
        }
        return defaultDataModel;
    }

    public static XmlServer getXmlServerCtrlLocal() {
        if (defaultXmlServer == null) {
            defaultXmlServer = new DefaultXmlServer();
        }
        return defaultXmlServer;
    }

    public static DataCluster getDataClusterCtrlLocal() {
        if (defaultDataCluster == null) {
            defaultDataCluster = new DefaultDataCluster();
        }
        return defaultDataCluster;
    }

    public static View getViewCtrlLocal() {
        if (defaultView == null) {
            defaultView = new DefaultView();
        }
        return defaultView;
    }

    public static com.amalto.core.server.api.Transformer getTransformerV2CtrlLocal() {
        if (defaultTransformer == null) {
            defaultTransformer = new DefaultTransformer();
        }
        return defaultTransformer;
    }

    public static Menu getMenuCtrlLocal() {
        if (menu == null) {
            menu = new DefaultMenu();
        }
        return menu;
    }

    public static BackgroundJob getBackgroundJobCtrlLocal() {
        if (defaultBackgroundJob == null) {
            defaultBackgroundJob = new DefaultBackgroundJob();
        }
        return defaultBackgroundJob;
    }

    public static ConfigurationInfo getConfigurationInfoCtrlLocal() {
        if (configurationInfo == null) {
            configurationInfo = new DefaultConfigurationInfo();
        }
        return configurationInfo;
    }

    public static RoutingEngine getRoutingEngineV2CtrlLocal() {
        return RoutingEngineFactory.getRoutingEngine();
    }

    /**
     * Extract the charset of a content type<br>
     * e.g 'utf-8' in 'text/xml; charset="utf-8"'
     * 
     * @return the charset
     */
    public static String extractCharset(String contentType) {
        String charset = org.apache.commons.lang.CharEncoding.UTF_8;
        String properties[] = StringUtils.split(contentType, ';');
        for (String property : properties) {
            String strippedProperty = property.trim().replaceAll("\"", "").replaceAll("'", "");
            Matcher m = extractCharsetPattern.matcher(strippedProperty);
            if (m.matches()) {
                charset = m.group(1).trim().toUpperCase();
                break;
            }
        }
        return charset;
    }

    /**
     * Extract the MIME type and sub type of a content type<br>
     * e.g 'text/xml' in 'text/xml; charset="utf-8"'
     * 
     * @return the MIME Type and SubType
     */
    public static String extractTypeAndSubType(String contentType) {
        if (contentType == null) {
            return null;
        }
        return contentType.split(";")[0].trim().toLowerCase();
    }

    /**
     * Extracts a byte array from an InputStream
     * 
     * @return the byte array
     * @throws IOException
     */
    public static byte[] getBytesFromStream(InputStream is) throws IOException {
        if (is == null) {
            return null;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int b;
        while ((b = is.read()) != -1) {
            bos.write(b);
        }
        return bos.toByteArray();
    }

    /**
     * Executes a BeforeSaving process if any
     * 
     * @param concept A concept/type name.
     * @param xml The xml of the document being saved.
     * @param resultUpdateReport Update report that corresponds to the save event.
     * @return Either <code>null</code> if no process was found or a status message (0=success; <>0=failure) of the form
     * &lt;error code='1'&gt;This is a message&lt;/error>
     * @throws Exception If something went wrong
     */
    @SuppressWarnings("unchecked")
    public static OutputReport beforeSaving(String concept, String xml, String resultUpdateReport) throws Exception {
        // check before saving transformer
        boolean isBeforeSavingTransformerExist = false;
        Collection<TransformerV2POJOPK> wst = getTransformerV2CtrlLocal().getTransformerPKs("*");
        for (TransformerV2POJOPK id : wst) {
            if (id.getIds()[0].equals("beforeSaving_" + concept)) {
                isBeforeSavingTransformerExist = true;
                break;
            }
        }
        // call before saving transformer
        if (isBeforeSavingTransformerExist) {

            try {
                final String RUNNING = "XtentisWSBean.executeTransformerV2.running";
                TransformerContext context = new TransformerContext(new TransformerV2POJOPK("beforeSaving_" + concept));
                String exchangeData = mergeExchangeData(xml, resultUpdateReport);
                context.put(RUNNING, Boolean.TRUE);
                com.amalto.core.server.api.Transformer ctrl = getTransformerV2CtrlLocal();
                TypedContent wsTypedContent = new TypedContent(exchangeData.getBytes("UTF-8"), "text/xml; charset=utf-8");
                ctrl.execute(context, wsTypedContent, new TransformerCallBack() {

                    @Override
                    public void contentIsReady(TransformerContext context) throws XtentisException {
                        LOGGER.debug("XtentisWSBean.executeTransformerV2.contentIsReady() ");
                    }

                    @Override
                    public void done(TransformerContext context) throws XtentisException {
                        LOGGER.debug("XtentisWSBean.executeTransformerV2.done() ");
                        context.put(RUNNING, Boolean.FALSE);
                    }
                });
                while ((Boolean) context.get(RUNNING)) {
                    Thread.sleep(100);
                }
                // TODO process no plug-in issue
                String message = "<report><message type=\"error\"/></report> ";
                String item = null;
                // Scan the entries - in priority, aka the content of the 'output_error_message' entry,
                boolean hasOutputReport = false;
                for (Entry<String, TypedContent> entry : context.getPipelineClone().entrySet()) {
                    if (ITransformerConstants.VARIABLE_OUTPUT_OF_BEFORESAVINGTRANFORMER.equals(entry.getKey())) {
                        hasOutputReport = true;
                        message = new String(entry.getValue().getContentBytes(), "UTF-8");
                    }
                    if (ITransformerConstants.VARIABLE_OUTPUTITEM_OF_BEFORESAVINGTRANFORMER.equals(entry.getKey())) {
                        item = new String(entry.getValue().getContentBytes(), "UTF-8");
                    }
                }
                if (!hasOutputReport) {
                    throw new OutputReportMissingException("Output variable 'output_report' is missing");
                }
                return new OutputReport(message, item);
            } catch (Exception e) {
                LOGGER.error(e);
                throw e;
            }
        }
        return null;
    }

    public static boolean isEnterprise() {
        return ServerAccess.INSTANCE.isEnterpriseVersion();
    }

    /**
     * Executes a BeforeDeleting process if any
     * 
     * @param clusterName A data cluster name
     * @param concept A concept/type name
     * @param ids Id of the document being deleted
     * @throws Exception If something went wrong
     */
    @SuppressWarnings("unchecked")
    public static BeforeDeleteResult beforeDeleting(String clusterName, String concept, String[] ids, String operationType)
            throws Exception {
        // check before deleting transformer
        boolean isBeforeDeletingTransformerExist = false;
        Collection<TransformerV2POJOPK> transformers = getTransformerV2CtrlLocal().getTransformerPKs("*");
        for (TransformerV2POJOPK id : transformers) {
            if (id.getIds()[0].equals("beforeDeleting_" + concept)) {
                isBeforeDeletingTransformerExist = true;
                break;
            }
        }
        if (isBeforeDeletingTransformerExist) {
            try {
                // call before deleting transformer
                // load the item
                ItemPOJOPK itemPk = new ItemPOJOPK(new DataClusterPOJOPK(clusterName), concept, ids);
                ItemPOJO pojo = ItemPOJO.load(itemPk);
                String xml = null;
                if (pojo == null) {// load from recycle bin
                    DroppedItemPOJOPK droppedItemPk = new DroppedItemPOJOPK(itemPk, "/");
                    DroppedItemPOJO droppedItem = Util.getDroppedItemCtrlLocal().loadDroppedItem(droppedItemPk);
                    if (droppedItem != null) {
                        xml = droppedItem.getProjection();
                        Document doc = Util.parse(xml);
                        Node item = (Node) XPathFactory.newInstance().newXPath().evaluate("//ii/p", doc, XPathConstants.NODE);
                        if (item != null && item instanceof Element) {
                            NodeList list = item.getChildNodes();
                            Node node = null;
                            for (int i = 0; i < list.getLength(); i++) {
                                if (list.item(i) instanceof Element) {
                                    node = list.item(i);
                                    break;
                                }
                            }
                            if (node != null) {
                                xml = Util.nodeToString(node);
                            }
                        }
                    }
                } else {
                    xml = pojo.getProjectionAsString();
                }
                // Create before deleting update report
                String username;
                try {
                    username = LocalUser.getLocalUser().getUsername();
                } catch (Exception e1) {
                    LOGGER.error(e1);
                    throw e1;
                }
                String key = "";
                if (ids != null) {
                    for (int i = 0; i < ids.length; i++) {
                        key += ids[i];
                        if (i != ids.length - 1) {
                            key += ".";
                        }
                    }
                }
                String resultUpdateReport = "" + "<Update>" + "<UserName>" + username + "</UserName>"
                        + "<Source>" + UpdateReportPOJO.GENERIC_UI_SOURCE +"</Source>" + "<TimeInMillis>" + System.currentTimeMillis() + "</TimeInMillis>"
                        + "<OperationType>" + StringEscapeUtils.escapeXml(operationType) + "</OperationType>" + "<DataCluster>"
                        + clusterName + "</DataCluster>" + "<DataModel>" + StringUtils.EMPTY + "</DataModel>" + "<Concept>"
                        + StringEscapeUtils.escapeXml(concept) + "</Concept>" + "<Key>" + StringEscapeUtils.escapeXml(key)
                        + "</Key>";
                resultUpdateReport += "</Update>";
                // Proceed with execution
                String exchangeData = mergeExchangeData(xml, resultUpdateReport);
                final String runningKey = "XtentisWSBean.executeTransformerV2.beforeDeleting.running";
                TransformerContext context = new TransformerContext(new TransformerV2POJOPK("beforeDeleting_" + concept));
                context.put(runningKey, Boolean.TRUE);
                com.amalto.core.server.api.Transformer ctrl = getTransformerV2CtrlLocal();
                TypedContent wsTypedContent = new TypedContent(exchangeData.getBytes("UTF-8"), "text/xml; charset=utf-8");
                ctrl.execute(context, wsTypedContent, new TransformerCallBack() {

                    @Override
                    public void contentIsReady(TransformerContext context) throws XtentisException {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("XtentisWSBean.executeTransformerV2.beforeDeleting.contentIsReady() ");
                        }
                    }

                    @Override
                    public void done(TransformerContext context) throws XtentisException {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("XtentisWSBean.executeTransformerV2.beforeDeleting.done() ");
                        }
                        context.put(runningKey, Boolean.FALSE);
                    }
                });
                while ((Boolean) context.get(runningKey)) { // TODO Poor-man synchronization here
                    Thread.sleep(100);
                }
                // TODO process no plug-in issue
                String outputErrorMessage = null;
                // Scan the entries - in priority, aka the content of the 'output_error_message' entry.
                for (Entry<String, TypedContent> entry : context.getPipelineClone().entrySet()) {
                    if (ITransformerConstants.VARIABLE_OUTPUT_OF_BEFORESAVINGTRANFORMER.equals(entry.getKey())) {
                        outputErrorMessage = new String(entry.getValue().getContentBytes(), "UTF-8");
                        break;
                    }
                }
                // handle error message
                BeforeDeleteResult result = new BeforeDeleteResult();
                if (outputErrorMessage == null) {
                    LOGGER.warn("No message generated by before delete process.");
                    result.type = "info";
                    result.message = StringUtils.EMPTY;
                } else {
                    if (outputErrorMessage.length() > 0) {
                        Document doc = Util.parse(outputErrorMessage);
                        // TODO what if multiple error nodes ?
                        String xpath = "//report/message";
                        Node errorNode = (Node) XPathFactory.newInstance().newXPath().evaluate(xpath, doc, XPathConstants.NODE);
                        if (errorNode instanceof Element) {
                            Element errorElement = (Element) errorNode;
                            result.type = errorElement.getAttribute("type");
                            Node child = errorElement.getFirstChild();
                            if (child instanceof Text) {
                                result.message = child.getTextContent();
                            }
                        }
                    } else {
                        result.type = "error";
                        result.message = "<report><message type=\"error\"/></report>";
                    }
                }
                return result;
            } catch (Exception e) {
                LOGGER.error(e);
                throw e;
            }
        }
        return null;
    }

    public static String mergeExchangeData(String xml, String resultUpdateReport) {
        String exchangeData = "<exchange>\n";
        exchangeData += "<report>" + resultUpdateReport + "</report>";
        exchangeData += "\n";
        exchangeData += "<item>" + xml + "</item>";
        exchangeData += "\n</exchange>";
        return exchangeData;
    }

    private static String getAppServerDeployDir() {
        String appServerDeployDir = System.getenv("JBOSS_HOME");
        if (appServerDeployDir == null) {
            appServerDeployDir = System.getProperty("jboss.home.dir");
        }
        if (appServerDeployDir == null) {
            appServerDeployDir = System.getProperty("user.dir");
        }
        return appServerDeployDir;
    }

    public static String getBarHomeDir() {
        String mdmRootDir = System.getProperty("mdm.root");
        return mdmRootDir + File.separator + "barfiles";
    }

    private static List<File> listFiles(FileFilter filter, File folder) {
        List<File> ret = new ArrayList<>();
        File[] children = folder.listFiles(filter);
        for (File f : children) {
            if (f.isFile()) {
                ret.add(f);
            } else {
                ret.addAll(listFiles(filter, f));
            }
        }
        return ret;
    }

    private static Element getLoginProvisioningFromDB() throws Exception {
        ItemPOJOPK itemPk = new ItemPOJOPK(new DataClusterPOJOPK("PROVISIONING"), "ID",
                new String[] { LocalUser.getLocalUser().getIdentity() });
        ItemPOJO item = ItemPOJO.load(itemPk);
        if (item == null) {
            return null;
        }
        return (Element) Util.getNodeList(item.getProjection(), "//User").item(0);
    }

    public static String getUserDataModel() throws Exception {
        return getUserDataModel(getLoginProvisioningFromDB());
    }

    private static String getUserDataModel(Element item) throws Exception {
        NodeList nodeList = Util.getNodeList(item, "//property");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if ("model".equals(Util.getFirstTextNode(node, "name"))) {
                    Node firstChild = Util.getNodeList(node, "value").item(0).getFirstChild();
                    return firstChild.getNodeValue();
                }
            }
        }
        return null;
    }

    public static String getUserDataCluster() throws Exception {
        return getUserDataCluster(getLoginProvisioningFromDB());
    }

    private static String getUserDataCluster(Element item) throws Exception {
        NodeList nodeList = Util.getNodeList(item, "//property");
        if (nodeList != null) {
            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                if ("cluster".equals(Util.getFirstTextNode(node, "name"))) {
                    Node firstChild = Util.getNodeList(node, "value").item(0).getFirstChild();
                    return firstChild.getNodeValue();
                }
            }
        }
        return null;
    }
    
    public static void setUserProperty(Document user, String name, String value) throws Exception {
        if(name == null){
            return;
        }
        NodeList properties = Util.getNodeList(user, "//properties");
        Element propertiesElement = null;
        if(properties == null || properties.getLength() == 0){
            propertiesElement = user.createElement("properties");
            user.getDocumentElement().appendChild(propertiesElement);
        }
        else {
            propertiesElement = (Element)properties.item(0);
        }
        NodeList props = Util.getNodeList(propertiesElement, "//property");
        boolean propertyFound = false;
        if(props != null){
            for(int i=0; i<props.getLength(); i++){
                Node node = props.item(i);
                if (name.equals(getFirstTextNode(node, "name"))) {
                    propertyFound = true;
                    if (getFirstTextNode(node, "value") == null) {
                        getNodeList(node, "value").item(0).appendChild(user.createTextNode(value));
                    } else {
                        getNodeList(node, "value").item(0).getFirstChild().setNodeValue(value);
                    }
                }
            }
        }
        if(!propertyFound){
            Element propertyElement = user.createElement("property");
            propertiesElement.appendChild(propertyElement);
            
            Element nameElement = user.createElement("name");
            nameElement.setTextContent(name);
            propertyElement.appendChild(nameElement);
            
            Element valueElement = user.createElement("value");
            valueElement.setTextContent(value);
            propertyElement.appendChild(valueElement);
        }
    }

    public static IWhereItem fixWebConditions(IWhereItem whereItem, String userXML) throws Exception {
        if (whereItem == null) {
            return null;
        }
        if (whereItem instanceof WhereLogicOperator) {
            List<IWhereItem> subItems = ((WhereLogicOperator) whereItem).getItems();
            for (int i = subItems.size() - 1; i >= 0; i--) {
                IWhereItem item = subItems.get(i);
                item = fixWebConditions(item, userXML);
                if (item instanceof WhereLogicOperator) {
                    if (((WhereLogicOperator) item).getItems().size() == 0) {
                        subItems.remove(i);
                    }
                } else if (item instanceof WhereCondition) {
                    WhereCondition condition = (WhereCondition) item;
                    if (condition.getRightValueOrPath() != null && condition.getRightValueOrPath().length() > 0
                            && condition.getRightValueOrPath().contains(USER_PROPERTY_PREFIX)) {
                        subItems.remove(i);
                    }
                } else if (item == null) {
                    subItems.remove(i);
                }
            }
        } else if (whereItem instanceof WhereCondition) {
            WhereCondition condition = (WhereCondition) whereItem;
            if (condition.getRightValueOrPath() != null && condition.getRightValueOrPath().length() > 0
                    && condition.getRightValueOrPath().contains(USER_PROPERTY_PREFIX)) {
                // TMDM-7207: Only create the groovy script engine if needed (huge performance issues)
                // TODO Should there be some pool of ScriptEngine instances? (is reusing ok?)
                ScriptEngine scriptEngine = SCRIPT_FACTORY.getEngineByName("groovy");
                if (userXML != null && !userXML.isEmpty()) {
                    User user = User.parse(userXML);
                    scriptEngine.put("user_context", user);
                }
                String rightCondition = condition.getRightValueOrPath();
                String userExpression = rightCondition.substring(rightCondition.indexOf('{') + 1, rightCondition.indexOf('}'));
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Groovy engine evaluating " + userExpression + ".");
                    }
                    Object expressionValue = scriptEngine.eval(userExpression);
                    if (expressionValue != null) {
                        String result = String.valueOf(expressionValue);
                        if (!"".equals(result.trim())) {
                            condition.setRightValueOrPath(result);
                        }
                    }
                } catch (Exception e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("No such property " + userExpression, e);
                    }
                }
            }

            if (!condition.getOperator().equals(WhereCondition.EMPTY_NULL)) {
                whereItem = "*".equals(condition.getRightValueOrPath()) || ".*".equals(condition.getRightValueOrPath()) ? null
                        : whereItem;
            }
        } else {
            throw new XmlServerException("Unknown Where Type : " + whereItem.getClass().getName());
        }
        if (whereItem instanceof WhereLogicOperator) {
            return ((WhereLogicOperator) whereItem).getItems().size() == 0 ? null : whereItem;
        }
        return whereItem;
    }

    /**
     * fix the conditions.
     * 
     * @param conditions in workbench.
     */

    public static void fixConditions(ArrayList conditions) {
        for (int i = conditions.size() - 1; i >= 0; i--) {
            if (conditions.get(i) instanceof WhereCondition) {
                WhereCondition condition = (WhereCondition) conditions.get(i);
                String rightValueOrPath = condition.getRightValueOrPath();
                if (rightValueOrPath == null
                        || (rightValueOrPath.length() == 0 && !WhereCondition.EMPTY_NULL.equals(condition.getOperator()))) {
                    conditions.remove(i);
                }
            }
        }
    }

    public static Boolean isContainUserProperty(List conditions) {
        for (int i = conditions.size() - 1; i >= 0; i--) {
            if (conditions.get(i) instanceof WhereCondition) {
                WhereCondition condition = (WhereCondition) conditions.get(i);
                if (condition.getRightValueOrPath() != null && condition.getRightValueOrPath().length() > 0
                        && condition.getRightValueOrPath().contains(USER_PROPERTY_PREFIX)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void updateUserPropertyCondition(List conditions, String userXML) {
        for (int i = conditions.size() - 1; i >= 0; i--) {
            if (conditions.get(i) instanceof WhereCondition) {
                WhereCondition condition = (WhereCondition) conditions.get(i);
                if (condition.getRightValueOrPath() != null && condition.getRightValueOrPath().length() > 0
                        && condition.getRightValueOrPath().contains(USER_PROPERTY_PREFIX)) {
                    String rightCondition = condition.getRightValueOrPath();
                    String userExpression = rightCondition
                            .substring(rightCondition.indexOf("{") + 1, rightCondition.indexOf("}"));
                    try {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Groovy engine evaluating " + userExpression + ".");
                        }
                        ScriptEngine scriptEngine = SCRIPT_FACTORY.getEngineByName("groovy");
                        User user = User.parse(userXML);
                        scriptEngine.put("user_context", user);
                        Object expressionValue = scriptEngine.eval(userExpression);
                        if (expressionValue != null) {
                            String result = String.valueOf(expressionValue);
                            if (!"".equals(result.trim())) {
                                condition.setRightValueOrPath(result);
                            } else {
                                conditions.remove(i);
                            }
                        } else {
                            conditions.remove(i);
                        }
                    } catch (Exception e) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(e.getMessage(), e);
                        }
                        LOGGER.warn("No such property " + userExpression);
                        conditions.remove(i);
                    }
                }
            }
        }
    }

    public static WSMDMJob[] getMDMJobs() {
        // Retrieve jobs from jobox only (zip deployment)
        // TODO: Still support war deployements??
        List<WSMDMJob> jobs = new ArrayList<>();
        try {
            FileFilter filter = new FileFilter() {

                @Override
                public boolean accept(File pathName) {
                    return pathName.isDirectory() || (pathName.isFile() && pathName.getName().toLowerCase().endsWith(".zip"));
                }
            };
            List<File> zipFiles = listFiles(filter, new File(JobContainer.getUniqueInstance().getDeployDir()));

            for (File zip : zipFiles) {
                WSMDMJob job = getJobInfo(zip.getAbsolutePath());
                if (job != null) {
                    jobs.add(job);
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return jobs.toArray(new WSMDMJob[jobs.size()]);
    }

    /**
     * get the JobInfo:jobName and version & suffix
     */
    private static WSMDMJob getJobInfo(String fileName) {
        WSMDMJob jobInfo = null;
        try {
            ZipEntry z;
            try (ZipInputStream in = new ZipInputStream(new FileInputStream(fileName))) {
                String jobName = "";
                String jobVersion = "";
                // war
                if (fileName.endsWith(".war")) {
                    while ((z = in.getNextEntry()) != null) {
                        String dirName = z.getName();
                        // get job version
                        if (dirName.endsWith("undeploy.wsdd")) {
                            Pattern p = Pattern.compile(".*?_(\\d_\\d)/undeploy.wsdd");
                            Matcher m = p.matcher(dirName);
                            if (m.matches()) {
                                jobVersion = m.group(1);
                            }
                        }
                        // get job name
                        Pattern p = Pattern.compile(".*?/(.*?)\\.wsdl");
                        Matcher m = p.matcher(dirName);
                        if (m.matches()) {
                            jobName = m.group(1);
                        }
                    }
                    if (jobName.length() > 0) {
                        jobInfo = new WSMDMJob(null, null, null);
                        jobInfo.setJobName(jobName);
                        jobInfo.setJobVersion(jobVersion.replaceAll("_", "."));
                        jobInfo.setSuffix(".war");
                        return jobInfo;
                    } else {
                        return null;
                    }
                }// war
                if (fileName.endsWith(".zip")) {
                    if ((z = in.getNextEntry()) != null) {
                        String dirName = z.getName();
                        int pos = dirName.indexOf('/');
                        if (pos == -1) {
                            pos = dirName.indexOf(File.separator);
                        }
                        String dir = dirName.substring(0, pos);
                        pos = dir.lastIndexOf('_');
                        jobName = dir.substring(0, pos);
                        jobVersion = dir.substring(pos + 1);
                    }
                    if (jobName.length() > 0) {
                        jobInfo = new WSMDMJob(null, null, null);
                        jobInfo.setJobName(jobName);
                        jobInfo.setJobVersion(jobVersion);
                        jobInfo.setSuffix(".zip");
                        return jobInfo;
                    } else {
                        return null;
                    }
                }
            } catch (FileNotFoundException e) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("getJobInfo error.", e);
                }
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("getJobInfo error.", e);
            }
        }
        return jobInfo;
    }

    public static String convertAutoIncrement(Properties p) {
        StringBuilder buffer = new StringBuilder();
        buffer.append("<AutoIncrement>");
        buffer.append("<id>AutoIncrement</id>");
        if (p != null) {
            for (Entry entry : p.entrySet()) {
                buffer.append("<entry>");
                buffer.append("<key>").append(entry.getKey()).append("</key>");
                buffer.append("<value>").append(entry.getValue()).append("</value>");
                buffer.append("</entry>");
            }
        }
        buffer.append("</AutoIncrement>");
        return buffer.toString();
    }

    public static Properties convertAutoIncrement(String xml) throws Exception {
        Properties p = new Properties();
        Node n = parse(xml).getDocumentElement();
        NodeList list = getNodeList(n, "entry");
        for (int i = 0; i < list.getLength(); i++) {
            Node item = list.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                String key = getFirstTextNode(item, "key");
                String value = getFirstTextNode(item, "value");
                p.setProperty(key, value);
            }
        }
        return p;
    }

    /**
     * Escape any single quote characters that are included in the specified message string.
     * 
     * @param string The string to be escaped
     */
    private static String escape(String string) {
        if ((string == null) || (string.indexOf('\'') < 0)) {
            return string;
        }
        int n = string.length();
        StringBuilder builder = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            char ch = string.charAt(i);
            if (ch == '\'') {
                builder.append('\'');
            }
            builder.append(ch);
        }
        return builder.toString();
    }

    public static String getMessage(String value, Object... args) {
        try {
            MessageFormat format = new MessageFormat(escape(value));
            value = format.format(args);
            return value;
        } catch (MissingResourceException e) {
            return "???" + value + "???";
        }
    }

    public static <T> T getException(Throwable throwable, Class<T> cls) {
        if (cls.isInstance(throwable)) {
            return (T) throwable;
        }
        if (throwable.getCause() != null) {
            return getException(throwable.getCause(), cls);
        }
        return null;
    }

    public static String getDefaultSystemLocale() {
        return MDMConfiguration.getConfiguration().getProperty("system.locale.default");
    }

    public static class BeforeDeleteResult {

        public String type;

        public String message;
    }
    
    public static void checkIdValidation(String id) {
        if (StringUtils.isNotEmpty(id)) {
            for (String character : INVALID_ID_CHARACTERS) {
                if (id.contains(character)) {
                    // throw exception when ID contains invalid character
                    throw new IllegalStateException("ID " + id + " contains invalid character '" + character + "'"); //$NON-NLS-3$
                }
            }
        }
    }

    /**
     * remove the bracket and number between two bracket.
     * eg.  /detail/feature/actor[1]    = /detail/feature/actor
     *      /detail/feature[2]/actor    = /detail/feature/actor
     *      /detail[3]/feature//actor   = /detail/feature/actor
     *      /detail[s]/feature//actor   = /detail[s]/feature/actor
     *
     * @param path path to repalce
     * @return the path have no the bracket and number between two bracket, return <code>null</code> if path is null
     */
    public static String removeBracketWithNumber(String path) {
        if (path == null) {
            return null;
        }
        if (path.contains("[") || path.contains("]")) {
            path = path.replaceAll("\\[\\d+\\]", "");
        }
        return path;
    }
}
