/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.SAXParser;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.VisitorSupport;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXValidator;
import org.dom4j.io.XMLWriter;
import org.dom4j.util.XMLErrorHandler;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.server.util.callback.AttributeProcess;
import org.talend.mdm.webapp.base.server.util.callback.DocumentCreate;
import org.talend.mdm.webapp.base.server.util.callback.ElementProcess;
import org.talend.mdm.webapp.base.server.util.callback.NodeProcess;
import org.xml.sax.SAXException;

public final class XmlUtil {

    private static final Logger LOGGER = Logger.getLogger(XmlUtil.class);

    public static Document parse(URL url) throws DocumentException {
        SAXReader reader = new SAXReader();
        setProtectRule(reader);
        Document document = reader.read(url);
        return document;
    }

    public static Document parse(File file) throws DocumentException {
        SAXReader reader = new SAXReader();
        setProtectRule(reader);
        Document document = reader.read(file);
        return document;
    }

    public static Document parse(InputStream in) throws DocumentException {
        SAXReader reader = new SAXReader();
        setProtectRule(reader);
        Document document = reader.read(in);
        return document;
    }

    private static void setProtectRule(SAXReader reader) {
        try {
            reader.setFeature(MDMXMLUtils.FEATURE_DISALLOW_DOCTYPE, true);
            reader.setFeature(MDMXMLUtils.FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
            reader.setFeature(MDMXMLUtils.FEATURE_EXTERNAL_PARAM_ENTITIES, false);
        } catch (SAXException e) {
            LOGGER.error("Failed to populate feature when initializing SAXReader", e);
        }
    }

    public static Document parse(String fileName) throws DocumentException {
        InputStream is = XmlUtil.class.getResourceAsStream("/" + fileName); //$NON-NLS-1$
        Document document = parse(is);
        return document;
    }

    public static InputStream getXmlStream(String fileName) {
        return XmlUtil.class.getResourceAsStream("/" + fileName); //$NON-NLS-1$
    }

    public static org.w3c.dom.Document parseDocument(Document doc4j) throws ServiceException {
        org.dom4j.io.DOMWriter d4Writer = new org.dom4j.io.DOMWriter();
        try {
            return d4Writer.write(doc4j);
        } catch (DocumentException e) {
            throw new ServiceException("Error occurred while using DOMWriter to create a DOM document.", e);
        }
    }

    public static Document mergeDoc(Document mainDoc, Document subDoc, String contextPath) {
        org.dom4j.Element el = (org.dom4j.Element) mainDoc.selectSingleNode(contextPath);
        org.dom4j.Element root = subDoc.getRootElement();
        List children = root.elements();
        for (int i = 0; i < children.size(); i++) {
            org.dom4j.Element child = (org.dom4j.Element) children.get(i);
            root.remove(child);
            el.add(child);
        }
        return mainDoc;
    }

    /**
     * DOC HSHU Comment method "parse".
     * 
     * @throws DocumentException
     */
    public static Document parseText(String text) throws DocumentException {
        return DocumentHelper.parseText(text);
    }

    public static void iterate(Document document, ElementProcess elementProcess) {
        Element root = document.getRootElement();
        iterate(root, elementProcess);
    }

    public static void iterate(Element parentElement, ElementProcess elementProcess) {
        // iterate through child elements of element
        for (Iterator<?> i = parentElement.elementIterator(); i.hasNext();) {
            Element element = (Element) i.next();
            // do something
            elementProcess.process(element);
        }
    }

    public static void iterate(Document document, String elementName, ElementProcess elementProcess) {

        Element root = document.getRootElement();

        iterate(root, elementName, elementProcess);

    }

    public static void iterate(Element parentElement, String elementName, ElementProcess elementProcess) {

        // iterate through child elements of element with element specific element
        // name
        for (Iterator<?> i = parentElement.elementIterator(elementName); i.hasNext();) {
            Element element = (Element) i.next();
            // do something
            elementProcess.process(element);
        }

    }

    public static void iterateAttribute(Document document, AttributeProcess attributeProcess) {
        Element root = document.getRootElement();
        iterateAttribute(root, attributeProcess);
    }

    public static void iterateAttribute(Element element, AttributeProcess attributeProcess) {
        // iterate through attributes of element
        for (Iterator<?> i = element.attributeIterator(); i.hasNext();) {
            Attribute attribute = (Attribute) i.next();
            // do something
            attributeProcess.process(attribute);
        }
    }

    public static void treeWalk(Document document, NodeProcess nodeProcess) {
        treeWalk(document.getRootElement(), nodeProcess);
    }

    public static void treeWalk(Element element, NodeProcess nodeProcess) {
        for (int i = 0, size = element.nodeCount(); i < size; i++) {
            Node node = element.node(i);

            if (node instanceof Element) {
                treeWalk((Element) node, nodeProcess);
            } else {
                nodeProcess.process(element);
            }
        }
    }

    public static void visit(Document document, VisitorSupport visitor) {
        visit(document.getRootElement(), visitor);
    }

    public static void visit(Element element, VisitorSupport visitor) {
        element.accept(visitor);
    }

    public static Node queryNode(Document document, String xPath) {
        Node node = document.selectSingleNode(xPath);
        return node;
    }

    public static Node queryNode2(Document document, String xPath) {
        String[] pathSlices = xPath.split("/"); //$NON-NLS-1$
        Element current = document.getRootElement();
        for (int i = 1; i < pathSlices.length; i++) {
            String pathSlice = pathSlices[i];
            Iterator<?> children = current.elementIterator(pathSlice);
            if (children.hasNext()) {
                current = (Element) children.next();
            } else {
                return null;
            }
        }
        return current;
    }

    public static String queryNodeText(Document document, String xPath) {
        StringBuffer value = new StringBuffer();
        String[] pathSlices = xPath.split("/"); //$NON-NLS-1$
        if (pathSlices.length > 1) {
            Element current = document.getRootElement();
            Iterator<?> children = current.elementIterator(pathSlices[pathSlices.length - 1]);
            if (children.hasNext()) {
                Node node = (Node) children.next();
                value.append(node.getText());
                recursionNode(node, value);
            }
        }
        return value.toString();
    }

    public static void recursionNode(Node node, StringBuffer value) {
        Element element = (Element) node;
        int size = element.nodeCount();
        for (int i = 0; i < size; i++) {
            Node chilidNode = element.node(i);
            if (chilidNode instanceof Element) {
                if (!"".equals(value.toString())) { //$NON-NLS-1$
                    value.append(" "); //$NON-NLS-1$                     
                }
                value.append(chilidNode.getText());
                recursionNode(chilidNode, value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Node> queryList(Document document, String xPath) {
        List<Node> list = document.selectNodes(xPath);
        return list;
    }

    public static List<String> findLinks(Document document) {
        List<String> urls = new ArrayList<String>();
        List<?> list = document.selectNodes("//a/@href"); //$NON-NLS-1$
        for (Object name : list) {
            Attribute attribute = (Attribute) name;
            String url = attribute.getValue();
            urls.add(url);
        }
        return urls;
    }

    public static Document createDocument(DocumentCreate documentCreate) {
        Document document = DocumentHelper.createDocument();
        documentCreate.create(document);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("New Document has bean created"); //$NON-NLS-1$
        }

        return document;
    }

    public static void write(Document document, String filePath, String printMode, String encoding) throws IOException {
        OutputFormat format;
        if (printMode.toLowerCase().equals("pretty")) { //$NON-NLS-1$
            // Pretty print the document
            format = OutputFormat.createPrettyPrint();
        } else if (printMode.toLowerCase().equals("compact")) { //$NON-NLS-1$
            // Compact format
            format = OutputFormat.createCompactFormat();
        } else {
            format = null;
        }

        format.setEncoding(encoding);

        // lets write to a file
        XMLWriter writer = new XMLWriter(new FileOutputStream(filePath), format);
        writer.write(document);
        writer.close();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("New xml file has bean exported on " + filePath); //$NON-NLS-1$
        }
    }

    public static String toXml(Document document) {
        return document.asXML();
    }

    public static Document fromXml(String text) throws DocumentException {
        return DocumentHelper.parseText(text);
    }

    public static void print(Document document) {
        String text = toXml(document);
        LOGGER.info(text);
    }

    public static String format(Document document, OutputFormat format, String encoding) {
        StringWriter writer = new StringWriter();
        format.setEncoding(encoding);
        format.setNewLineAfterDeclaration(false);

        XMLWriter xmlwriter = new XMLWriter(writer, format);

        try {
            xmlwriter.write(document);
        } catch (Exception e) {
            LOGGER.error("Failed to write XML file.", e);
        }

        return writer.toString().replaceAll("<\\?xml.*?\\?>", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$

    }

    public static String formatPretty(String xml, String encoding) throws DocumentException {
        Document document = fromXml(xml);
        return format(document, OutputFormat.createPrettyPrint(), encoding);
    }

    public static String formatCompact(String xml, String encoding) throws DocumentException {
        Document document = fromXml(xml);
        return format(document, OutputFormat.createCompactFormat(), encoding);
    }

    public static boolean validateXMLByXSD(String inputXml, File xsdFile) {
        boolean isValidated = false;
        String xsdFileName = xsdFile.getAbsolutePath();
        try {
            XMLErrorHandler errorHandler = new XMLErrorHandler();

            SAXParser parser = MDMXMLUtils.getSAXParser();
            Document xmlDocument = DocumentHelper.parseText(inputXml);

            // [url]http://sax.sourceforge.net/?selected=get-set[/url]
            parser.setProperty(MDMXMLUtils.PROPERTY_SCHEMA_LANGUAGE, MDMXMLUtils.PROPERTY_XML_SCHEMA); //$NON-NLS-1$ //$NON-NLS-2$
            parser.setProperty(MDMXMLUtils.PROPERTY_SCHEMA_SOURCE, "file:" + xsdFileName); //$NON-NLS-1$ //$NON-NLS-2$

            SAXValidator validator = new SAXValidator(parser.getXMLReader());

            validator.setErrorHandler(errorHandler);
            validator.validate(xmlDocument);
            XMLWriter writer = new XMLWriter(OutputFormat.createPrettyPrint());

            if (errorHandler.getErrors().hasContent()) {
                isValidated = false;
                LOGGER.error("XML file validation failed! "); //$NON-NLS-1$
                writer.write(errorHandler.getErrors());
            } else {
                isValidated = true;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("XML file validation succeeded! "); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            isValidated = false;
            LOGGER.error("Failed to validate XML file through '" + xsdFileName, e); //$NON-NLS-1$
        }
        return isValidated;
    }

    public static String getTextValueFromXpath(Document doc, String xpath) {
        Element root = doc.getRootElement();
        Node node = root.selectSingleNode(xpath);
        return node == null ? "" : node.getText(); //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    public static List<Node> getValuesFromXPath(Document doc, String xpath) {
        Element root = doc.getRootElement();
        return root.selectNodes(xpath);
    }

    public static void completeXMLByXPath(Document doc, String xPath) {
        if (!xPath.startsWith("/")) {
            xPath = "/" + xPath; //$NON-NLS-1$
        }
        String[] nodeList = xPath.split("/"); //$NON-NLS-1$
        String tmpPath = nodeList[0];
        Element element = null;
        for (int i = 1; i < nodeList.length; i++) {
            tmpPath = tmpPath + "/" + nodeList[i]; //$NON-NLS-1$            

            if (doc.selectSingleNode(tmpPath) != null) {
                element = (Element) doc.selectSingleNode(tmpPath);
                continue;
            }

            Pattern pattern = Pattern.compile("(.+)(\\[.+\\])"); //$NON-NLS-1$
            Matcher matcher = pattern.matcher(nodeList[i]);
            if (!matcher.matches()) {
                element = element.addElement(nodeList[i]);
            } else {
                element = element.addElement(matcher.group(1));
            }
        }
    }
}
