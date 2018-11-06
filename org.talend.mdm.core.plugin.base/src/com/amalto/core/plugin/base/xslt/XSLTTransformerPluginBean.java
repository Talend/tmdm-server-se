/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.xslt;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.core.objects.Plugin;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.transformers.util.TransformerPluginContext;
import com.amalto.core.objects.transformers.util.TransformerPluginVariableDescriptor;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;

import net.sf.saxon.FeatureKeys;

/**
 * <h1>XSLT Plugin</h1> <h3>Plugin name: xslt</h3> <h3>Description</h3> The XSLT plugin executes an XSLT on an input XML
 * document.<br/>
 * <br/>
 * The XSLT plugin supports XSLT 2.0 and provides and extension to perform cross-referencing on the fly when the output
 * method of the xslt is set to 'xml' or to 'xhtml'.<br/>
 * 
 * <br/>
 * Cross-referencing is carried out AFTER the xslt is processed on ALL elements with the following attributes:
 * 
 * <pre>
 * &lt;MyElement
 *     xrefCluster='CLUSTER'
 *     xrefIn='TEST1, ..., TESTN'
 *     xrefOut='XPATH_IN_ITEM'
 *     xrefIgnore='true|false'
 *     xrefDefault='DEFAULT_VALUE'
 * &gt;OLD_VALUE&lt;/MyElement&gt;
 * </pre>
 * 
 * Where
 * <ul>
 * <li><b>xrefCluster</b>: the cluster where the Items used for cross-referencing are stored</li>
 * <li><b>xrefIn</b>: a series of XPaths tests to match this item content with a remote Item</li>
 * <li><b>xrefOut</b>: the XPath in the remote item, starting with the concept name, of the content that will replace
 * the content of this item</li>
 * <li><b>xrefIgnore</b>: optional, defaults to <code>false</code>. If set to <code>true</code>, the cross referencing
 * will not fail if not Item is found and the <code>xrefDefault</code> value will be inserted.</li>
 * <li><b>xrefDefault</b>: if <code>xrefIgnore</code> is set to <code>true</code> and the cross-referencing is failing,
 * this value will be used instead.</li>
 * </ul>
 * <h3>Inputs</h3>
 * <ul>
 * <li><b>xml</b>: the xml on which to apply the XSLT</li>
 * <li><b>parameters</b>: optional input parameters to the XSLT in the form of
 * 
 * <pre>
 * &lt;Parameters&gt;
 *     &lt;Parameter&gt;
 *         &lt;Name&gt;PARAMETER_NAME&lt;/Name&gt;
 *         &lt;Value&gt;PARAMETER_VALUE&lt;/Value&gt;
 *     &lt;/Parameter&gt;
 * &lt;/Parameters&gt;
 * </pre>
 * 
 * </li>
 * </ul>
 * <h3>Outputs</h3>
 * <ul>
 * <li><b>text</b>: the result of the XSLT</li>
 * </ul>
 * <h3>Parameters</h3> The XSLT - see the description. <h3>Example</h3> The following example parameters will loop over
 * all the <code>LineItem</code>s of the input XML and send them to the rest of the transformer as XML fragments
 * 
 * <pre>
 * &lt;Country
 *     xrefCluster='MYCLUSTER'
 *     xrefIn='.=Country/Codes/ISO2, ../Customer/Name=[ACME]'
 *     xrefOut='Country/Name/FR'
 * &gt;&lt;xsl:value-of select='State/CountryCode'/&gt;&lt;/Country&gt;
 * </pre>
 * 
 * The example above does the following:
 * <ul>
 * <li>1-the xslt generates a &lt;Country&gt; element in the target document</li>
 * <li>2-the content of State/CountryCode of the source document is inserted as the value of the <Country> element</li>
 * <li>3-the rest of the xsl transformations complete</li>
 * <li>4-the system queries the Country data in cluster MYCLUSTER where
 * <ul>
 * <li>Codes/ISO2Code is equal to State/CountryCode (the current value of the Country element)</li>
 * <li>and ../Customer/Name in the target document is equal to hard coded value ACME</li>
 * </ul>
 * <li>5-the matching Country document is returned and the value in Name/FR is extracted</li>
 * <li>6-the value in Country of the target document is replaced with the extracted value</li>
 * </ul>
 * 
 * @author Bruno Grieder
 * 
 * @ejb.bean name="XSLTTransformerPlugin" display-name="Name for XSLTPlugin" description="Description for XSLTPlugin"
 * local-jndi-name = "amalto/local/transformer/plugin/xslt" type="Stateless" view-type="local"
 * local-business-interface="com.amalto.core.objects.transformers.v2.util.TransformerPluginV2LocalInterface"
 * 
 * @ejb.remote-facade
 * 
 * @ejb.permission view-type = "remote" role-name = "administration"
 * @ejb.permission view-type = "local" unchecked = "true"
 * 
 * 
 * 
 */
@Service(XSLTTransformerPluginBean.JNDI_NAME)
public class XSLTTransformerPluginBean extends Plugin {

    public static final String JNDI_NAME = "amalto/local/transformer/plugin/xslt"; //$NON-NLS-1$

    private static final Logger LOG = Logger.getLogger(XSLTTransformerPluginBean.class);

    private static final String OUTPUT_METHOD = "com.amalto.core.plugin.xpath.outputMethod"; //$NON-NLS-1$

    private static final String TRANSFORMER = "com.amalto.core.plugin.xpath.transformer"; //$NON-NLS-1$

    private static final String INPUT_XML = "xml"; //$NON-NLS-1$

    private static final String INPUT_PARAMETERS = "parameters"; //$NON-NLS-1$

    private static final String OUTPUT_TEXT = "text"; //$NON-NLS-1$

    /********************************************************************************************
     * Compilation - decompilation of parameters
     ********************************************************************************************/

    // XSLT ouput determination
    private static Pattern XLST_OUTPUT_PATTERN = Pattern.compile(".+?<[a-z]+?:output[^>]*? method\\s*=\\s*[\"|'](.*?)[\"|'].*", //$NON-NLS-1$
            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

    /** A transformation error */
    String transformatioError = null;

    /** A transformation warning */
    String transformationeWarning = null;

    private boolean configurationLoaded = false;

    private String compilationErrors = ""; //$NON-NLS-1$

    public XSLTTransformerPluginBean() {
        super();
        try {
            getConfiguration(null);
        } catch (Exception e) {
        }
    }

    /**
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public String getJNDIName() throws XtentisException {
        return JNDI_NAME;
    }

    /**
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public String getDescription(String twoLetterLanguageCode) throws XtentisException {
        if ("fr".matches(twoLetterLanguageCode.toLowerCase()))
            return "Transforme un XML en utilisant une XSLT";
        return "Transform an XML using an XSLT";
    }

    /**
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public ArrayList<TransformerPluginVariableDescriptor> getInputVariableDescriptors(String twoLettersLanguageCode)
            throws XtentisException {
        ArrayList<TransformerPluginVariableDescriptor> inputDescriptors = new ArrayList<TransformerPluginVariableDescriptor>();

        // The csv_line descriptor
        TransformerPluginVariableDescriptor descriptor1 = new TransformerPluginVariableDescriptor();
        descriptor1.setVariableName(INPUT_XML);
        descriptor1.setContentTypesRegex(new ArrayList<Pattern>(Arrays.asList(new Pattern[] { Pattern.compile("text/xml") })));
        HashMap<String, String> descriptions1 = new HashMap<String, String>();
        descriptions1.put("en", "The item instance to project");
        descriptor1.setDescriptions(descriptions1);
        descriptor1.setMandatory(true);
        descriptor1.setPossibleValuesRegex(null);
        inputDescriptors.add(descriptor1);

        // The xslt parameters descriptor
        TransformerPluginVariableDescriptor descriptor2 = new TransformerPluginVariableDescriptor();
        descriptor2.setVariableName(INPUT_PARAMETERS);
        descriptor2.setContentTypesRegex(new ArrayList<Pattern>(Arrays.asList(new Pattern[] { Pattern.compile("text/xml") })));
        HashMap<String, String> descriptions2 = new HashMap<String, String>();
        descriptions2.put("en", "An xml file with parameters to pass to the xlst transformer");
        descriptions2.put("fr", "Un fichier XML contenant les paramètres à passer au processeur xslt");
        descriptor2.setDescriptions(descriptions2);
        descriptor2.setMandatory(false);
        descriptor2.setPossibleValuesRegex(null);
        inputDescriptors.add(descriptor2);

        return inputDescriptors;
    }

    /**
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public ArrayList<TransformerPluginVariableDescriptor> getOutputVariableDescriptors(String twoLettersLanguageCode)
            throws XtentisException {
        ArrayList<TransformerPluginVariableDescriptor> outputDescriptors = new ArrayList<TransformerPluginVariableDescriptor>();

        // The csv_line descriptor
        TransformerPluginVariableDescriptor descriptor = new TransformerPluginVariableDescriptor();
        descriptor.setVariableName(OUTPUT_TEXT);
        descriptor.setContentTypesRegex(new ArrayList<Pattern>(Arrays.asList(new Pattern[] { Pattern.compile("text/.*") })));
        HashMap<String, String> descriptions = new HashMap<String, String>();
        descriptions.put("en", "The result of the xml transformation");
        descriptor.setDescriptions(descriptions);
        descriptor.setMandatory(true);
        descriptor.setPossibleValuesRegex(null);
        outputDescriptors.add(descriptor);

        return outputDescriptors;
    }

    /**
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public void init(TransformerPluginContext context, String compiledParameters) throws XtentisException {
        try {
            if (!configurationLoaded) {
                loadConfiguration();
            }
            // fetech the parameters
            CompiledParameters parameters = CompiledParameters.deserialize(compiledParameters);

            // get the xslt compiled style sheet and the Transformer
            /**
             * Unfortunately this does not work for the moment PreparedStylesheet preparedStyleSheet
             * =parameters.getPreparedStyleSheet(); Transformer transformer = preparedStyleSheet.newTransformer();
             **/
            // As a replacement in the meantime
            setCompilationErrors(""); //$NON-NLS-1$
            
            // USE SAXON for XSLT 2.0 Support
            TransformerFactory transFactory = new net.sf.saxon.TransformerFactoryImpl();
            transFactory.setErrorListener(new ErrorListener() {

                public void error(TransformerException exception) throws TransformerException {
                    add2CompilationErrors(exception.getLocalizedMessage());
                }

                public void fatalError(TransformerException exception) throws TransformerException {
                    add2CompilationErrors(exception.getLocalizedMessage());
                }

                public void warning(TransformerException exception) throws TransformerException {
                    String err = "XSLT Plugin: Warning during the compilation of the XSLT"; //$NON-NLS-1$
                    LOG.warn(err, exception);
                }
            });
            transFactory.setAttribute(FeatureKeys.VERSION_WARNING, Boolean.valueOf(false));
            if (!"".equals(getCompilationErrors())) { //$NON-NLS-1$
                String err = "XSLT Plugin: Errors occurred during the compilation of the XSLT:" + getCompilationErrors(); //$NON-NLS-1$
                LOG.error(err);
                throw new XtentisException(err);
            }
            Transformer transformer = transFactory.newTransformer(new StreamSource(new StringReader(parameters.getXslt())));

            // Pass Parameters to the XSLT processor
            String username = LocalUser.getLocalUser().getUsername();
            transformer.setParameter("USERNAME", username); //$NON-NLS-1$
            transformer.setErrorListener(new ErrorListener() {

                public void error(TransformerException exception) throws TransformerException {
                    String err = "XSLT Plugin: An error occured during the XSLT transformation"; //$NON-NLS-1$
                    LOG.error(err, exception);
                    throw new TransformerException(err);
                }

                public void fatalError(TransformerException exception) throws TransformerException {
                    String err = "XSLT Plugin: A fatal error occured during the XSLT transformation"; //$NON-NLS-1$
                    LOG.error(err, exception);
                    throw new TransformerException(err);
                }

                public void warning(TransformerException exception) throws TransformerException {
                    String err = "XSLT Plugin: Warning during the XSLT transformation"; //$NON-NLS-1$
                    LOG.warn(err, exception);
                }
            });

            // Insert all this in the context
            context.put(TRANSFORMER, transformer);
            context.put(OUTPUT_METHOD, parameters.getOutputMethod());

        } catch (Exception e) {
            String err = compilationErrors;
            if (err == null || err.length() == 0) {
                err = "Could not init the XSLT Plugin"; //$NON-NLS-1$
            }
            LOG.error(err, e);
            throw new XtentisException(err);
        }

    }

    @Override
    protected String loadConfiguration() {
        return null;
    }

    /**
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public void execute(TransformerPluginContext context) throws XtentisException {
        try {
            // fetch data from context
            Transformer transformer = (Transformer) context.get(TRANSFORMER);
            String outputMethod = (String) context.get(OUTPUT_METHOD);
            TypedContent xmlTC = (TypedContent) context.get(INPUT_XML);

            // get the charset
            String charset = Util.extractCharset(xmlTC.getContentType());

            // get the xml String
            String xml = new String(xmlTC.getContentBytes(), charset);

            // get the xml parameters
            TypedContent parametersTC = (TypedContent) context.get(INPUT_PARAMETERS);
            if (parametersTC != null) {
                String parametersCharset = Util.extractCharset(parametersTC.getContentType());

                byte[] parametersBytes = parametersTC.getContentBytes();
                if (parametersBytes != null) {
                    String parameters = new String(parametersBytes, parametersCharset);

                    Document parametersDoc = Util.parse(parameters);
                    NodeList paramList = Util.getNodeList(parametersDoc.getDocumentElement(), "//Parameter"); //$NON-NLS-1$
                    for (int i = 0; i < paramList.getLength(); i++) {
                        String paramName = Util.getFirstTextNode(paramList.item(i), "Name"); //$NON-NLS-1$
                        String paramValue = Util.getFirstTextNode(paramList.item(i), "Value"); //$NON-NLS-1$
                        if (paramValue == null)
                            paramValue = ""; //$NON-NLS-1$

                        if (paramName != null) {
                            transformer.setParameter(paramName, paramValue);
                        }
                    }
                }
            }

            // FIXME: ARRRRGHHHHHH - How do you prevent the Transformer to process doctype instructions?

            // Sets the current time
            transformer.setParameter("TIMESTAMP", getTimestamp()); //$NON-NLS-1$
            transformer.setErrorListener(new ErrorListener() {

                public void error(TransformerException exception) throws TransformerException {
                    transformatioError = exception.getMessage();
                }

                public void fatalError(TransformerException exception) throws TransformerException {
                    transformatioError = exception.getMessage();
                }

                public void warning(TransformerException exception) throws TransformerException {
                    transformationeWarning = exception.getMessage();

                }
            });
            transformatioError = null;
            transformationeWarning = null;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            if ("xml".equals(outputMethod) || "xhtml".equals(outputMethod)) { //$NON-NLS-1$ //$NON-NLS-2$
                DocumentBuilder builder = MDMXMLUtils.getDocumentBuilder().get();
                Document document = builder.newDocument();
                DOMResult domResult = new DOMResult(document);

                transformer.transform(new StreamSource(new StringReader(xml)), domResult);

                if (transformationeWarning != null) {
                    String err = "Warning processing the XSLT: " + transformationeWarning; //$NON-NLS-1$
                    LOG.warn(err);
                }
                if (transformatioError != null) {
                    String err = "Error processing the XSLT: " + transformatioError; //$NON-NLS-1$
                    LOG.error(err);
                    throw new XtentisException(err);
                }
                Node rootNode = document.getDocumentElement();
                // process the cross-referencings
                NodeList xrefl = Util.getNodeList(rootNode.getOwnerDocument(), "//*[@xrefCluster]"); //$NON-NLS-1$
                int len = xrefl.getLength();
                for (int i = 0; i < len; i++) {
                    Element xrefe = (Element) xrefl.item(i);
                    xrefe = processMappings(xrefe);
                }
                TransformerFactory transFactory = new net.sf.saxon.TransformerFactoryImpl();
                Transformer serializer = transFactory.newTransformer();
                serializer.setOutputProperty(OutputKeys.METHOD, outputMethod);
                serializer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
                serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); //$NON-NLS-1$
                serializer.transform(new DOMSource(rootNode), new StreamResult(baos));
            } else {
                if (transformationeWarning != null) {
                    String err = "Warning processing the XSLT: " + transformationeWarning; //$NON-NLS-1$
                    LOG.warn(err);
                }
                if (transformatioError != null) {
                    String err = "Error processing the XSLT: " + transformatioError; //$NON-NLS-1$
                    LOG.error(err);
                    throw new XtentisException(err);
                }
                // transform the item
                transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(baos));
            }
            // Call Back
            byte[] bytes = baos.toByteArray();
            if (LOG.isDebugEnabled()) {
                LOG.debug("execute() Inserting XSL Result in '" + OUTPUT_TEXT + "'\n" + new String(bytes, "utf-8")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            context.put(OUTPUT_TEXT, new TypedContent(bytes, ("xhtml".equals(outputMethod) ? "application/xhtml+xml" : "text/" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    + outputMethod) + "; charset=utf-8")); //$NON-NLS-1$
            context.getPluginCallBack().contentIsReady(context);
        } catch (Exception e) {
            String err = "Could not start the XSLT plugin"; //$NON-NLS-1$
            LOG.error(err, e);
            throw new XtentisException(err);
        }
    }

    /**
     * Process the mappings after xsl transformation
     * 
     * @param xrefElement
     * @return the processed Element
     */
    private Element processMappings(Element xrefElement) throws XtentisException {

        try {

            String xrefcluster = xrefElement.getAttribute("xrefCluster"); //$NON-NLS-1$

            String xrefIn = xrefElement.getAttribute("xrefIn"); //$NON-NLS-1$
            String xrefOut = xrefElement.getAttribute("xrefOut"); //$NON-NLS-1$
            String xrefIgnore = xrefElement.getAttribute("xrefIgnore"); //$NON-NLS-1$
            String xrefDefault = xrefElement.getAttribute("xrefDefault"); //$NON-NLS-1$

            Logger.getLogger(XSLTTransformerPluginBean.class).debug(
                    "\n xrefIgnore=" + xrefIgnore + "\n xrefDefault=" + xrefDefault); //$NON-NLS-1$ //$NON-NLS-2$

            // parse xrefbein dockey1:xrefkey1,dockey2:xrefkey2
            String[] mappings = xrefIn.split(","); //$NON-NLS-1$
            HashMap<String, String> itemvals = new HashMap<String, String>();
            for (int j = 0; j < mappings.length; j++) {
                String[] relations = mappings[j].split("="); //$NON-NLS-1$
                String docpath = relations[0];
                String xrefpath = relations[1];
                String itemval = ""; //$NON-NLS-1$
                try {
                    if (docpath.startsWith("[")) // hardcoded value //$NON-NLS-1$
                        itemval = docpath.substring(1, docpath.length() - 1);
                    else
                        itemval = Util.getFirstTextNode(xrefElement, docpath);
                } catch (Exception x) {
                    throw new XtentisException("Value for business element '" + xrefElement.getNodeName() + '/' + docpath //$NON-NLS-1$
                            + "' cannot be found!"); //$NON-NLS-1$
                }
                if (itemval == null)
                    itemval = ""; //$NON-NLS-1$
                String content = stripeOuterBracket(itemval);
                if (content.split(",").length >= mappings.length) //$NON-NLS-1$
                    itemvals.put(xrefpath, content.split(",")[j]); //$NON-NLS-1$
                else
                    itemvals.put(xrefpath, ""); //$NON-NLS-1$
            }

            WhereAnd wAnd = new WhereAnd();

            Collection<Map.Entry<String, String>> c = itemvals.entrySet();
            int i = 0;
            for (Iterator<Map.Entry<String, String>> iter = c.iterator(); iter.hasNext();) {
                i++;
                Map.Entry<String, String> entry = iter.next();
                wAnd.add(new WhereCondition(entry.getKey(), WhereCondition.EQUALS, entry.getValue(), WhereCondition.PRE_AND,
                        false));
            }

            ArrayList<String> resList = Util.getItemCtrl2Local().xPathsSearch(new DataClusterPOJOPK(xrefcluster), null,
                    new ArrayList<String>(Arrays.asList(new String[] { xrefOut })), wAnd, -1, // spell
                    0, // start
                    1, // limit
                    false);

            String val = ""; //$NON-NLS-1$

            if ((resList == null) || (resList.size() == 0)) {
                if (xrefIgnore.equals("true") || xrefIgnore.equals("1")) { //$NON-NLS-1$ //$NON-NLS-2$
                    if (xrefDefault != null)
                        val = xrefDefault;
                    else
                        val = ""; //$NON-NLS-1$
                } else {
                    String ks = ""; //$NON-NLS-1$
                    c = itemvals.entrySet();
                    for (Iterator<Map.Entry<String, String>> iter = c.iterator(); iter.hasNext();) {
                        Map.Entry<String, String> entry = iter.next();
                        ks += " " + entry.getKey() + "=\"" + entry.getValue() + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    throw new XtentisException("Foreign keys values not found for: " + ks); //$NON-NLS-1$
                }
            } else {
                // read result
                Pattern p = Pattern.compile("<.*?>(.*?)</.*>", Pattern.DOTALL); //$NON-NLS-1$
                Matcher m = p.matcher(resList.iterator().next());

                if (m.matches())
                    val = StringEscapeUtils.unescapeXml(m.group(1));
                else {
                    Pattern p2 = Pattern.compile("<.*?/>", Pattern.DOTALL); //$NON-NLS-1$
                    Matcher m2 = p2.matcher(resList.iterator().next());
                    if (!m2.matches() && !(xrefIgnore.equals("true") || xrefIgnore.equals("1"))) { //$NON-NLS-1$ //$NON-NLS-2$
                        throw new XtentisException("Result values were not understood for crossref element"); //$NON-NLS-1$
                    }
                }
            }

            NodeList l = xrefElement.getChildNodes();
            for (int j = 0; j < l.getLength(); j++) {
                switch (l.item(j).getNodeType()) {
                case Node.TEXT_NODE:
                    l.item(j).setNodeValue(val);
                    break;
                case Node.ELEMENT_NODE:
                    xrefElement.removeChild(l.item(j));
                    break;
                default:

                }
            }

            xrefElement.removeAttribute("xrefCluster"); //$NON-NLS-1$
            xrefElement.removeAttribute("xrefIgnore"); //$NON-NLS-1$
            xrefElement.removeAttribute("xrefDefault"); //$NON-NLS-1$
            xrefElement.removeAttribute("xrefIn"); //$NON-NLS-1$
            xrefElement.removeAttribute("xrefOut"); //$NON-NLS-1$

            return xrefElement;
        } catch (Exception e) {
            String err = "Unable to process the mappings for the element '" + xrefElement.getLocalName() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
            LOG.error(err, e);
            throw new XtentisException(err);
        }
    }

    /**
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public String getParametersSchema() throws XtentisException {
        return null;

    }

    /**
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    @SuppressWarnings("nls")
    public String getDocumentation(String twoLettersLanguageCode) throws XtentisException {
        return "Simply drop your xslt in the parameters box."
                + "\n"
                + ""
                + "\n"
                + "To xslt can do cross referencing on the fly if the output method is set to 'xml' or 'xhtml'"
                + "\n"
                + ""
                + "\n"
                + "Cross-referencing is carried out AFTER the xlst is processed on ALL elements with the following attributes:"
                + "\n"
                + "	<Country  "
                + "\n"
                + "			xrefCluster='MYCLUSTER' "
                + "\n"
                + "			xrefIn='.=Country/Codes/ISO2, ../Customer/Name=[ACME]' "
                + "\n"
                + "			xrefOut='Country/Name/FR'"
                + "\n"
                + "	>"
                + "\n"
                + "			<xsl:value-of select='State/CountryCode'/>"
                + "\n"
                + "	</Country>"
                + "\n"
                + ""
                + "\n"
                + "where"
                + "\n"
                + "	xrefCluster is the cluster holmding the Country (concept/table) data"
                + "\n"
                + "	xrefIn is a list of comma separated match expressions. "
                + "\n"
                + "			The left part specifies an xPath relative to the current context of the *target* document"
                + "\n"
                + "			or a hard coded value between brackets"
                + "\n"
                + "			The right part specifies an xPath in the cluster"
                + "\n"
                + "	xrefOut is an xPath in the Cluster holding the final value that will be inserted in the *target* document"
                + "\n"
                + ""
                + "\n"
                + "	if xrefIgnore is 'true' or '1', no exception will be thrown if there is no entry in the transcodification table."
                + "\n"
                + "	xrefDefault is the value that's used if there is no entry in the transcodification table and xrefIgnore is true."
                + "\n" + "" + "\n" + "The example above does the following:" + "\n"
                + "	1-the xslt generates a <Country> element in the target document" + "\n"
                + "	2-the value of State/CountryCode of the source document is inserted as the value of the <Country> element"
                + "\n" + "	3-the rest of the xsl transformations complete" + "\n"
                + "	4-the system queries the Country data in cluster MYCLUSTER where " + "\n"
                + "			Codes/ISO2Code is equal to  State/CountryCode (the current value of the Country element)" + "\n"
                + "			and ../Customer/Name in the target document is equal to hard coded value ACME." + "\n"
                + "	5-the matching Country document is returned and the value in Name/FR is extracted" + "\n"
                + "	6- the value in Cuuntry of the traget document is replaced with the extracted value";

    }

    private String getDefaultConfiguration() {
        return "<configuration><charset>utf-8</charset></configuration>"; //$NON-NLS-1$
    }

    /**
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public String getConfiguration(String optionalParameters) throws XtentisException {
        try {
            String configuration = loadConfiguration();
            if (configuration == null) {
                configuration = getDefaultConfiguration();
            }
            configurationLoaded = true;
            return configuration;
        } catch (Exception e) {
            String err = "Unable to deserialize the configuration of the XSLT Transformer Plugin"; //$NON-NLS-1$
            LOG.error(err, e);
            throw new XtentisException(err);
        }
    }

    /**
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public void putConfiguration(String configuration) throws XtentisException {
        configurationLoaded = false;
    }

    /**
     * @throws XtentisException
     * 
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public String compileParameters(String parameters) throws XtentisException {
        try {
            String xslt = parameters;
            CompiledParameters compiled = new CompiledParameters();

            // Determine the ouput of the xslt
            String output = "xml"; // default //$NON-NLS-1$
            Matcher mxslt = XLST_OUTPUT_PATTERN.matcher(xslt);
            if (mxslt.matches()) {
                output = mxslt.group(1).toLowerCase().trim();
            }
            compiled.setOutputMethod(output);

            // compile the sytlesheet
            setCompilationErrors(""); //$NON-NLS-1$
            compiled.setXslt(xslt);

            return compiled.serialize();
        } catch (Exception e) {
            String err = "Unable to serialize the parameters of the XSLT Transformer Plugin"; //$NON-NLS-1$
            LOG.error(err, e);
            throw new XtentisException(err);
        }
    }

    public String getCompilationErrors() {
        return compilationErrors;
    }

    public void setCompilationErrors(String compilationError) {
        this.compilationErrors = compilationError;
    }

    protected synchronized void add2CompilationErrors(String err) {
        compilationErrors += "\n" + err; //$NON-NLS-1$
    }

    public static String getTimestamp() {
        return getTimestamp(new Date());
    }

    private static String getTimestamp(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss.SSS' 'z"); //$NON-NLS-1$
        return sdf.format(d);
    }

    public static String stripeOuterBracket(String rowData) {
        ArrayList<String> result = new ArrayList<>();
        int aggregate = 0;
        int cordon = 0;
        for (int i = 0; i < rowData.length(); i++) {
            char ch = rowData.charAt(i);
            if (ch == '[') {
                aggregate++;
                if (aggregate == 1) {
                    cordon = i;
                }
            } else if (ch == ']') {
                aggregate--;
                if (aggregate == 0) {
                    result.add(rowData.substring(cordon + 1, i));
                }
            } else if (aggregate == 0) {
                result.add(ch + ""); //$NON-NLS-1$
            }
        }
        return StringUtils.join(result.toArray(), ","); //$NON-NLS-1$
    }
}
