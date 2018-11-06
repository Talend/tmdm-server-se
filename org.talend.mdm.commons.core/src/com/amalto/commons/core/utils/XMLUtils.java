/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.commons.core.utils;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.dom4j.io.DocumentResult;
import org.dom4j.io.DocumentSource;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.org.apache.xpath.internal.objects.XObject;

/**
 * XML Manipulation routines
 * @author Bruno Grieder
 *
 */
public final class XMLUtils {

    private static final Logger logger = Logger.getLogger(XMLUtils.class);

    private static final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private static final TransformerFactory saxonTransformerFactory = new net.sf.saxon.TransformerFactoryImpl();

	/**
	 * Parsed an XML String into a {@link Document} without schema vaildation
	 * @param xmlString
	 * @return the Document
	 * @throws ParserConfigurationException,IOException, SAXException
	 */
    public static Document parse(String xmlString) throws ParserConfigurationException,IOException, SAXException{
    	return parse(xmlString,null);
    }


    /**
     * Parses an XML String into a Document<br>
     * The thrown Exception will contain validation errors when a schema is provided.
     * @param xmlString
     * @param schema - the schema XSD
     * @return The org.w3c.dom.Document
     * @throws ParserConfigurationException,IOException, SAXException
     */
    public static Document parse(String xmlString, String schema) throws ParserConfigurationException,IOException, SAXException{

		//parse
		Document d=null;
		SAXErrorHandler seh = new SAXErrorHandler();

        //initialize the sax parser which uses Xerces
    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	//Schema validation based on schemaURL
    	factory.setNamespaceAware(true);
    	factory.setValidating((schema!=null));
    	factory.setAttribute(
    			"http://java.sun.com/xml/jaxp/properties/schemaLanguage",
    			"http://www.w3.org/2001/XMLSchema");
    	if (schema != null) {
    	    factory.setAttribute(
    			"http://java.sun.com/xml/jaxp/properties/schemaSource",
    			new InputSource(new StringReader(schema))
    			);
    	}
    	DocumentBuilder builder;
    	builder = factory.newDocumentBuilder();
    	builder.setErrorHandler(seh);
    	d = builder.parse(new InputSource(new StringReader(xmlString)));


		//check if dcument parsed correctly against the schema
		if (schema != null) {
			String errors = seh.getErrors();
			if (!errors.equals("")) {
				String err = "Document  did not parse against schema: \n" + errors+"\n"
					+xmlString.substring(0, Math.min(100, xmlString.length()));
				org.apache.log4j.Logger.getLogger(XMLUtils.class).error(err);
				throw new SAXException(err);
			}
		}
		return d;
    }


    /**
     * Return the String values of the Text Nodes below an xPath
     * @param contextNode
     * @param xPath
     * @return a String Array of the text node values
     * @throws TransformerException
     */
    public static String[] getTextNodes(Node contextNode, String xPath) throws TransformerException{
    	return getTextNodes(contextNode,xPath,contextNode);
    }


    /**
     * Return the String values of the Text Nodes below an xPath<br>
     * Where the xPath contains namespace, a context node holding the namespaces definitions can be provided
     * @param contextNode
     * @param xPath
     * @param namespaceNode
     * @return a String Array of the text node values
     * @throws TransformerException
     */
    public static String[] getTextNodes(Node contextNode, String xPath,Node namespaceNode) throws TransformerException{
        String[] results=null;;

        //test for hard-coded values
        if (xPath.startsWith("\"") && xPath.endsWith("\""))
            return new String[] { xPath.substring(1, xPath.length()-1)};

        //test for incomplete path (elements missing /text())
        if (!xPath.matches(".*@[^/\\]]+"))  //attribute
        	if (! xPath.endsWith(")")) //function
        		xPath+="/text()";

        try {
	        XObject xo = XPathAPI.eval(contextNode, xPath,namespaceNode);
	        if (xo.getType() == XObject.CLASS_NODESET) {
	            NodeList l = xo.nodelist();
	            int len = l.getLength();
	            results = new String[len];
	            for (int i = 0; i < len; i++) {
	                Node n = l.item(i);
	                results[i] = n.getNodeValue();
	            }
	        } else {
	            results = new String[]{xo.toString()};
	        }
		} catch (Exception e) {
			String err = "Unable to get the text node(s) of "+xPath
					+": " + e.getClass().getName() + ": "
					+ e.getLocalizedMessage();
			org.apache.log4j.Logger.getLogger(XMLUtils.class).error(err,e);
			throw new TransformerException(err);
		}
		return results;

    }


    /**
     * The value of the first text node at the xPath<br>
     * @see #getTextNodes(Node, String, Node)
     * @param contextNode
     * @param xPath
     * @param namespaceNode
     * @return the String value
     * @throws TransformerException
     */
    public static String getFirstTextNode(Node contextNode, String xPath,Node namespaceNode) throws TransformerException{
    	String[] res = getTextNodes(contextNode,xPath,namespaceNode);
    	if (res.length == 0)
    		return null;
    	return res[0];
    }

    /**
     * The value of the first text node at the xPath<br>
     * @see #getTextNodes(Node, String)
     * @param contextNode
     * @param xPath
     * @return teh String value
     * @throws TransformerException
     */
    public static String getFirstTextNode(Node contextNode, String xPath) throws TransformerException{
    	return getFirstTextNode(contextNode,xPath,contextNode);
    }


    /**
     * The value of the first text node at the xPath which is not null<br>
     * @see #getTextNodes(Node, String)
     * @param contextNode
     * @param xPath
     * @return teh String value
     * @throws TransformerException
     */
    public static String getFirstTextNodeNotNull(Node contextNode, String xPath) throws TransformerException {
    	String val=  getFirstTextNode(contextNode,xPath,contextNode);
    	return val == null ? "" : val;
    }




    /**
	 * Returns a namespaced root element of a document
	 * Useful to create a namespace holder element
	 * @param namespace
	 * @return the root Element
	 */
	public static Element getRootElement(String elementName, String namespace, String prefix) throws TransformerException{
	 	Element rootNS=null;
    	try {
	    	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        factory.setNamespaceAware(true);
	        factory.setExpandEntityReferences(false);
	        DocumentBuilder builder = factory.newDocumentBuilder();
	    	DOMImplementation impl = builder.getDOMImplementation();
	    	Document namespaceHolder = impl.createDocument(namespace,(prefix==null?"":prefix+":")+elementName, null);
	    	rootNS = namespaceHolder.getDocumentElement();
	    	rootNS.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:"+prefix, namespace);
    	} catch (Exception e) {
    	    String err="Error creating a namespace holder document: "+e.getLocalizedMessage();
    	    throw new TransformerException(err);
    	}
    	return rootNS;
	}

	/**
	 * Get a nodelist from an xPath
	 * @throws TransformerException
	 */
	public static NodeList getNodeList(Document d, String xPath) throws TransformerException{
		return getNodeList(d.getDocumentElement(),xPath,null,null);
	}
	/**
	 * Get a nodelist from an xPath
	 * @throws TransformerException
	 */
	public static NodeList getNodeList(Node contextNode, String xPath) throws TransformerException{
		return getNodeList(contextNode,xPath,null,null);
	}

	/**
	 * Get a nodelist from an xPath
	 * @throws TransformerException
	 */
	public static NodeList getNodeList(Node contextNode, String xPath, String namespace, String prefix) throws TransformerException{
		try {
		    XObject xo = XPathAPI.eval(
	    		contextNode,
				xPath,
				(namespace == null) ? contextNode : getRootElement("nsholder",namespace,prefix)
		    );
		    if (xo.getType() != XObject.CLASS_NODESET) return null;
		    return xo.nodelist();
    	} catch (TransformerException e) {
    	    String err = "Unable to get the Nodes List for xpath '"+xPath+"'"
    	    	+((contextNode==null) ? "" : " for Node "+contextNode.getLocalName())
    			+": "+e.getLocalizedMessage();
    		throw new TransformerException(err);
    	}
	}


    /**
     * Returns the first Element of the Node List at xPath
     * @param n
     * @param xPath
     * @return
     * @throws XtentisWebappException
     */
	public static Element getFirstElement(Node n, String xPath) throws TransformerException {
			NodeList nl = getNodeList(n, xPath);
			if ((nl==null) || (nl.getLength()==0)) return null;
			return (Element)nl.item(0);
	}


	/**
	 * Validates the element against the provided XSD schema
	 * @param element
	 * @param schema
	 * @return
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws TransformerException
	 */
    public static Document validate(Element element, String schema) throws SAXException, ParserConfigurationException,
            IOException, TransformerException {

        org.apache.log4j.Logger.getLogger(XMLUtils.class).trace("validate() " + element.getLocalName());

        // parse
        Document d = null;
        SAXErrorHandler seh = new SAXErrorHandler();

        // initialize the sax parser which uses Xerces
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Schema validation based on schemaURL
        factory.setNamespaceAware(true);
        factory.setExpandEntityReferences(false);
        factory.setValidating((schema != null));
        factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
        if (schema != null) {
            factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource",
                    new InputSource(new StringReader(schema)));
        }
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        builder.setErrorHandler(seh);
        d = builder.parse(new InputSource(new StringReader(nodeToString(element))));

        // check if dcument parsed correctly against the schema
        if (schema != null) {
            String errors = seh.getErrors();
            if (!errors.equals("")) {
                String xmlString = nodeToString(element);
                String err = "The item " + element.getLocalName() + " did not validate against the model: \n" + errors + "\n"
                        + xmlString; // .substring(0, Math.min(100, xmlString.length()));
                throw new SAXException(err);
            }
        }
        return d;
    }



    public static String[] getAttributeNodeValue(Node contextNode, String xPath, Node namespaceNode) throws TransformerException{
        String[] results;

        //test for hard-coded values
        if (xPath.startsWith("\"") && xPath.endsWith("\""))
            return new String[] { xPath.substring(1, xPath.length()-1)};

        //test for incomplete path
        //if (! xPath.endsWith(")")) xPath+="/text()";

        try {
	        XObject xo = XPathAPI.eval(contextNode, xPath,namespaceNode);
	        if (xo.getType() == XObject.CLASS_NODESET) {
	            NodeList l = xo.nodelist();
	            int len = l.getLength();
	            results = new String[len];
	            for (int i = 0; i < len; i++) {
	                Node n = l.item(i);
	                results[i] = n.getNodeValue();
	            }
	        } else {
	            results = new String[]{xo.toString()};
	        }
		} catch (TransformerException e) {
			String err = "Unable to get the text node(s) of "+xPath
					+": " + e.getClass().getName() + ": "
					+ e.getLocalizedMessage();
			throw new TransformerException(err);
		}
		return results;
    }

    public static Transformer generateTransformer() throws TransformerConfigurationException {
        return transformerFactory.newTransformer();
    }

    public static Transformer generateTransformer(boolean isOmitXmlDeclaration) throws TransformerConfigurationException {
        Transformer transformer = generateTransformer();
        if (isOmitXmlDeclaration) {
            transformer.setOutputProperty("omit-xml-declaration", "yes");
        } else {
            transformer.setOutputProperty("omit-xml-declaration", "no");
        }
        return transformer;
    }

    public static Transformer generateTransformer(boolean isOmitXmlDeclaration, boolean isIndent)
            throws TransformerConfigurationException {
        Transformer transformer = generateTransformer(isOmitXmlDeclaration);
        if (isIndent) {
            transformer.setOutputProperty("indent", "yes");
        } else {
            transformer.setOutputProperty("indent", "no");
        }
        return transformer;
    }

    public static String nodeToString(Node n, boolean isOmitXmlDeclaration, boolean isIndent) throws TransformerException {
        StringWriter sw = new StringWriter();
        Transformer transformer = generateTransformer(isOmitXmlDeclaration, isIndent);
        transformer.transform(new DOMSource(n), new StreamResult(sw));
        return sw.toString();
    }

    public static String nodeToString(Node n) throws TransformerException {
        return nodeToString(n, true, true);
    }

    public static org.dom4j.Document styleDocument(org.dom4j.Document document, String stylesheet) throws Exception {
        // load the transformer using JAXP
        Transformer transformer = saxonTransformerFactory.newTransformer(new StreamSource(new StringReader(stylesheet)));

        // now lets style the given document
        DocumentSource source = new DocumentSource(document);
        DocumentResult result = new DocumentResult();
        transformer.transform(source, result);

        // return the transformed document
        org.dom4j.Document transformedDoc = result.getDocument();

        if (logger.isDebugEnabled()) {
            logger.debug("The xml file style transformed successfully ");
        }
        return transformedDoc;
    }
 }
