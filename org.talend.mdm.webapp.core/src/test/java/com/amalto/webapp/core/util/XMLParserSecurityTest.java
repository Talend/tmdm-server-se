// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.webapp.core.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.io.IOUtils;
import org.dom4j.io.SAXReader;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.amalto.core.delegator.IValidation;

import junit.framework.TestCase;

/**
 * created by hwzhu on Sep 26, 2018
 *
 */
public class XMLParserSecurityTest extends TestCase {

    // assert if exist external entity in xml
    public void testIsExistExtEntity() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        factory.setValidating(false);
        InputStream is = getClass().getResourceAsStream("ExternalEntityFile.xml");// source
        assertTrue(MDMXMLUtils.isExistExtEntity(is));
    }

    // No security validation for DocumentBuilderFactory
    public void testAllowExternalRefEntityParseWithDocumentBuilderFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        factory.setValidating(false);
        factory.setExpandEntityReferences(true);
        factory.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false); //$NON-NLS-1$
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputStream is = getClass().getResourceAsStream("ExternalEntityFile.xml");// source

        InputSource source = new InputSource(is);
        Document userDomDocument = builder.parse(source);
        Element root = userDomDocument.getDocumentElement();
        String expectVal = null;
        if (root.hasChildNodes()) {
            int length = root.getChildNodes().getLength();
            outerLoop: for (int i = 0; i < length; i++) {
                Node item = root.getChildNodes().item(i);
                if ("Description".equals(item.getNodeName())) {
                    if (item.hasChildNodes()) {
                        int internalLength = item.getChildNodes().getLength();
                        for (int j = 0; j < internalLength; j++) {
                            Node subItem = item.getChildNodes().item(j);
                            if (subItem.getNodeType() == Node.TEXT_NODE) {
                                expectVal = subItem.getTextContent();
                                break outerLoop;
                            }
                        }
                    }
                }
            }
        }
        assertNotNull(expectVal);
    }

    // add security validation for DocumentBuilderFactory
    public void testDisallowExternalRefEntityParseWithDocumentBuilderFactory() throws Exception {
        DocumentBuilder builder = MDMXMLUtils.getDocumentBuilder().get();
        InputStream is = getClass().getResourceAsStream("ExternalEntityFile.xml");// source
        InputSource source = new InputSource(is);
        try {
            builder.parse(source);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("DOCTYPE is disallowed"));
        }
    }

    // no security validation for XMLInputFactory
    public void testAllowExternalRefEntityParseWithXMLInputFactory() throws Exception {
        XMLInputFactory factory = XMLInputFactory.newFactory();
        InputStream stream = getClass().getResourceAsStream("ExternalEntityFile2.xml");// source
        XMLEventReader reader = factory.createXMLEventReader(stream);
        // Skip to first record
        String expectVal = null;
        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                // Declare root element namespaces (if any)
                final StartElement startElement = event.asStartElement();
                if (startElement.getName().getLocalPart().equals("Description")) {
                    XMLEvent event1 = reader.nextEvent();
                    if (event1.isCharacters()) {
                        expectVal = event1.asCharacters().getData();
                        break;
                    }
                }
            }
        }
        assertNotNull(expectVal);
    }

    // add security validation for XMLInputFactory
    public void testDisallowExternalRefEntityParseWithXMLInputFactory() throws Exception {
        InputStream stream = getClass().getResourceAsStream("ExternalEntityFile2.xml");// source
        XMLEventReader reader = MDMXMLUtils.createXMLEventReader(stream);
        // Skip to first record
        String expectVal = null;
        while (reader.hasNext()) {
            final XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                // Declare root element namespaces (if any)
                final StartElement startElement = event.asStartElement();
                if (startElement.getName().getLocalPart().equals("Description")) {
                    try {
                        reader.nextEvent();
                    } catch (Exception e) {
                        assertTrue(e.getMessage().contains("Encountered a reference to external entity \"desc\", but stream reader has feature \"javax.xml.stream.isSupportingExternalEntities\" disabled"));
                    }
                }
            }
        }
        assertNull(expectVal);
    }

    // no security checking for XMLReader
    public void testAllowExternalRefEntityParseWithXMLReader() throws Exception {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        InputStream inputSource = getClass().getResourceAsStream("ExternalEntityFile.xml");// source
        InputSource source = new InputSource(inputSource);
        final List<String> list = new ArrayList<>();
        reader.setContentHandler(new DefaultHandler() {
            @Override       
            public void startElement(String uri, String localName, String qName,Attributes attributes) throws SAXException {           
                super.startElement(uri, localName, qName, attributes);
                if ("Description".equals(localName)) {
                    list.add(localName);
                }
            }

            @Override
            public void characters(char ch[], int start, int length) throws SAXException {
                super.characters(ch, start, length);
                if (!list.isEmpty()) {
                    String content = new String(ch, start, length);
                    assertNotNull(content);
                    list.clear();
                }
            }
        });
        reader.parse(source);
    }

    // add security checking for XMLReader
    public void testDisallowExternalRefEntityParseWithXMLReader() throws Exception {
        XMLReader reader = MDMXMLUtils.getXMLReader();
        InputStream inputSource = getClass().getResourceAsStream("ExternalEntityFile.xml");// source
        InputSource source = new InputSource(inputSource);
        try {
            reader.parse(source);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true."));
        }
    }

    // SAXReader
    public void testAllowExternalRefEntityParseWithSAXReader() throws Exception {
        SAXReader reader = new SAXReader();
        InputStream inputSource = getClass().getResourceAsStream("ExternalEntityFile.xml");// source
        org.dom4j.Document doc = reader.read(inputSource);
        org.dom4j.Element root = doc.getRootElement();
        String nameValue = root.element("Name").getText();
        assertNotNull(nameValue);
    }

    // SAXReader
    public void testDisallowExternalRefEntityParseWithSAXReader() throws Exception {
        SAXReader reader = new SAXReader();
        reader.setValidation(true);
        reader.setIncludeExternalDTDDeclarations(false);
        reader.setFeature(MDMXMLUtils.FEATURE_DISALLOW_DOCTYPE, true);
        reader.setFeature(MDMXMLUtils.FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
        reader.setFeature(MDMXMLUtils.FEATURE_EXTERNAL_PARAM_ENTITIES, false);
        InputStream inputSource = getClass().getResourceAsStream("ExternalEntityFile.xml");// source
        try {
            reader.read(inputSource);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true."));
        }
    }

    // JAXB Unmarshaller
    public void testAllowExternalRefEntityParseWithSAXParserFactory() throws Exception {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        spf.setNamespaceAware(true);
        InputStream stream = getClass().getResourceAsStream("ExternalEntityFile2.xml");// source
        Source xmlSource = new SAXSource(spf.newSAXParser().getXMLReader(),
                new InputSource(new StringReader(IOUtils.toString(stream, "utf-8"))));
        JAXBContext jc = JAXBContext.newInstance(Product.class);
        Unmarshaller um = jc.createUnmarshaller();
        Product product = (Product)um.unmarshal(xmlSource);
        assertNotNull(product.getDescription());
    }

    // Disallow SAXParser
    public void testDisallowExternalRefEntityParseWithSAXParser() throws Exception {
        try {
            InputStream stream = getClass().getResourceAsStream("ExternalEntityFile2.xml");// source
            MDMXMLUtils.getSAXParser().parse(stream, new DefaultHandler());
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true."));
        }
    }

    // JAXB Unmarshaller
    public void testDisallowExternalRefEntityParseWithSAXParserFactory() throws Exception {
        InputStream stream = getClass().getResourceAsStream("ExternalEntityFile2.xml");// source
        Source xmlSource = new SAXSource(MDMXMLUtils.getSAXParser().getXMLReader(),
                new InputSource(new StringReader(IOUtils.toString(stream, "utf-8"))));
        JAXBContext jc = JAXBContext.newInstance(Product.class);
        Unmarshaller um = jc.createUnmarshaller();
        try {
            um.unmarshal(xmlSource);
        } catch (Exception e) {
            assertTrue(e.getCause().getMessage().contains("DOCTYPE is disallowed when the feature"));
        }
    }

    // JAXB Unmarshaller
    public void testDisallowExternalRefEntityParseWithXPathExpression() throws Exception {
        DocumentBuilder builder = MDMXMLUtils.getDocumentBuilder().get();
        InputStream stream = getClass().getResourceAsStream("ExternalEntityFile2.xml");// source
        try {
            builder.parse(stream);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("DOCTYPE is disallowed when the feature"));
        }
    }

    public void testDefaultValidateWithExtEntity() throws IOException, ParserConfigurationException, SAXException {
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        df.setExpandEntityReferences(false);
        DocumentBuilder builder = df.newDocumentBuilder();
        InputStream schema = getClass().getResourceAsStream("ExternalEntityFile3.xml");// source
        try {
            Document root = builder.parse(schema);
            Element element = root.getDocumentElement();
            String result = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("ExternalEntityFile3.xml"))).lines().collect(Collectors.joining(System.lineSeparator()));
            new IValidation().validation(element, result);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("External entities are not allowed in Schema"));
        }
    }
}

@XmlRootElement(name = "Product")
@XmlAccessorType(XmlAccessType.FIELD)
class Product {

    @XmlElement(name = "Id")
    private String id;

    @XmlElement(name = "Name")
    private String name;

    @XmlElement(name = "Description")
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
