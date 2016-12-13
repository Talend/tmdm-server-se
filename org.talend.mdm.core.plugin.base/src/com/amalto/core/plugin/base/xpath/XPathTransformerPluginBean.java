/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.xpath;

import com.amalto.core.objects.Plugin;
import com.amalto.core.objects.transformers.util.TransformerPluginContext;
import com.amalto.core.objects.transformers.util.TransformerPluginVariableDescriptor;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.plugin.base.xpath.CompiledParameters;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

import org.springframework.stereotype.Service;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;



/**
 * <h1>XPath Plugin</h1>
 * <h3>Plugin name: xpath</h3>
 * <h3>Description</h3>
 * The XPath plugin executes an XPath on an xml document.<br/>
 * <br/>
 * The result of the xPath may be <ul>
 * <li>either an XML fragment (content type of text/xml)</li>
 * <li>or a text fragment (content type text/plain)</li>
 * <li> or set of nodes in which case the plugin will loop over the results.</li>
 * </ul>
 * This plugin is a "looping" plugin. When the result is a set of nodes (xmls), the first node is sent for processing
 * by the rest of the transformer then the next node and so forth until the last node.<br/>
 * <br/>
 * This plugin is usually used in front of the Fixed Length Parser Plugin or the CSV Parser Plugin.
 * <h3>Inputs</h3>
 * <ul>
 * <li><b>xml</b>: the xml on which to apply the XPath</li>
 * </ul>
 * <h3>Outputs</h3>
 * <ul>
 * <li><b>text</b>: the result of the XPath</li>
 * </ul>
 * <h3>Parameters</h3>
 * The parameters are specified as an XML <pre>
    &lt;parameters&gt;
      &lt;xPath&gt;XPATH_V2&lt;/xPath&gt;
      &lt;contentType&gt;text/plain|text/xml&lt;/contentType&gt;
    &lt;/parameters&gt;
 * </pre>
 * <ul>
 * <li><b>xPath</b>: an XPath v2.0 to apply to the input XML. The XPath may return a text node, an element or a set of nodes</li>
 * <li><b>contentType</b>: the content type of the result. If the result of the XPath is an element (or a set of elements)
 * and the content type is<ul>
 * 		<li>text/xml: an xml is returned with the element name as root element name</li>
 * 		<li>text/plain: the text content of the element is returned</li>
 * </ul>
 *</ul>
 * <h3>Example</h3>
 * The following example parameters will loop over all the <code>LineItem</code>s of the input XML
 * and send them to the rest of the transformer as XML fragments
 * <pre>
    &lt;parameters&gt;
      &lt;xPath&gt;//LineItem[@category='Office Supplies']&lt;/xPath&gt;
      &lt;contentType&gt;text/xml&lt;/contentType&gt;
    &lt;/parameters&gt;
 * </pre>
 *
 * @author Bruno Grieder
 *
 * @ejb.bean name="xPathTransformerPlugin"
 *           display-name="Name for xPathPlugin"
 *           description="Description for xPathPlugin"
 * 		  local-jndi-name = "amalto/local/transformer/plugin/xpath"
 *           type="Stateless"
 *           view-type="local"
 *           local-business-interface="com.amalto.core.objects.transformers.v2.util.TransformerPluginV2LocalInterface"
 *
 * @ejb.remote-facade
 *
 * @ejb.permission
 * 	view-type = "remote"
 * 	role-name = "administration"
 * @ejb.permission
 * 	view-type = "local"
 * 	unchecked = "true"
 *
 *
 *
 */
@Service("amalto/local/transformer/plugin/xpath")
public class XPathTransformerPluginBean extends Plugin {

	private static final String PARAMETERS = "com.amalto.core.plugin.xpath.parameters";

	private static final String INPUT_XML = "xml";
	//private static final String INPUT_RESULTING_CONTENT_TYPE = "output_content_type";
	private static final String OUTPUT_TEXT = "text";

	private static final long serialVersionUID = 11487098995680L;

    private transient boolean configurationLoaded = false;


	public XPathTransformerPluginBean() {
		super();
		try {
			getConfiguration(null);
		} catch (Exception e) {}
	}



    /**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public String getJNDIName() throws XtentisException {
		return "amalto/local/transformer/plugin/xpath";
	}



    /**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public String getDescription(String twoLetterLanguageCode) throws XtentisException {
		if ("fr".matches(twoLetterLanguageCode.toLowerCase()))
			return "Execute un xpath sur un xml et retourne le résultat";
		return "Executes an XPath on an XML document and returns the result";
	}



	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public ArrayList<TransformerPluginVariableDescriptor> getInputVariableDescriptors(String twoLettersLanguageCode) throws XtentisException {
		 ArrayList<TransformerPluginVariableDescriptor> inputDescriptors = new ArrayList<TransformerPluginVariableDescriptor>();

		 //The csv_line descriptor
		 TransformerPluginVariableDescriptor descriptor1 = new TransformerPluginVariableDescriptor();
		 descriptor1.setVariableName(INPUT_XML);
		 descriptor1.setContentTypesRegex(
				 new ArrayList<Pattern>(
						 Arrays.asList(new Pattern[]{
								 Pattern.compile("text/xml")
						})
				)
		 );
		 HashMap<String, String> descriptions1 = new HashMap<String, String>();
		 descriptions1.put("en", "The xml to run the xPath on");
		 descriptor1.setDescriptions(descriptions1);
		 descriptor1.setMandatory(true);
		 descriptor1.setPossibleValuesRegex(null);
		 inputDescriptors.add(descriptor1);

		 /* To be implemented when the xPath is passed as variable
		 TransformerPluginVariableDescriptor descriptor2 = new TransformerPluginVariableDescriptor();
		 descriptor2.setVariableName(INPUT_RESULTING_CONTENT_TYPE);
		 descriptor2.setContentTypesRegex(
				 new ArrayList<Pattern>(
						 Arrays.asList(new Pattern[]{
								 Pattern.compile("text/plain")
						})
				)
		 );
		 HashMap<String, String> descriptions2 = new HashMap<String, String>();
		 descriptions2.put("en", "The reculting content-type after running the Xpath. Non Mandatory. Default: text/plain; charset=utf-8");
		 descriptor2.setDescriptions(descriptions2);
		 descriptor2.setMandatory(false);
		 descriptor2.setPossibleValuesRegex(null);
		 inputDescriptors.add(descriptor2);
		 */

		 return inputDescriptors;
	}



	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public ArrayList<TransformerPluginVariableDescriptor> getOutputVariableDescriptors(String twoLettersLanguageCode) throws XtentisException {
		ArrayList<TransformerPluginVariableDescriptor> outputDescriptors = new ArrayList<TransformerPluginVariableDescriptor>();

		 //The csv_line descriptor
		 TransformerPluginVariableDescriptor descriptor = new TransformerPluginVariableDescriptor();
		 descriptor.setVariableName(OUTPUT_TEXT);
		 descriptor.setContentTypesRegex(
				 new ArrayList<Pattern>(
						 Arrays.asList(new Pattern[]{
								 Pattern.compile("text.*")
						})
				)
		 );
		 HashMap<String, String> descriptions = new HashMap<String, String>();
		 descriptions.put("en", "The result of the xPath");
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
	public void init(
			TransformerPluginContext context,
			String compiledParameters
			) throws XtentisException {
		try {

			if (!configurationLoaded) loadConfiguration();

			CompiledParameters parameters = CompiledParameters.deserialize(compiledParameters);
			context.put( PARAMETERS, parameters);

		} catch (Exception e) {
			String err = "Could not init the xPath plugin:"+
				e.getClass().getName()+": "+e.getLocalizedMessage();
			org.apache.log4j.Logger.getLogger(this.getClass()).error(err);
			throw new XtentisException(e);
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

		org.apache.log4j.Logger.getLogger(this.getClass()).debug("execute() ");
		CompiledParameters parameters= (CompiledParameters)context.get( PARAMETERS);
		TypedContent xmlTC = (TypedContent)context.get(INPUT_XML);

		try {

			//attempt to read charset
			String charset = Util.extractCharset(xmlTC.getContentType());
			String xml = new String(xmlTC.getContentBytes(),charset);

			//read the bytes from Bytes in priority,
			//If non available, attempt to read from the stream and insert the bytes into the TypedContent
			NodeList nodes = Util.getNodeList(Util.parse(xml), parameters.getXPath());
			for (int i = 0; i < nodes.getLength(); i++) {
				Node n = nodes.item(i);
				String res = null;
				if (parameters.getResultingContentType().startsWith("text/plain")) {
					res = Util.getFirstTextNode(n, ".");
				} else if (parameters.getResultingContentType().startsWith("text/xml")){
					res = Util.nodeToString(n);
				} else {
					String err = "xPath Plugin: Could not parse the result of the xPath with content type "+parameters.getResultingContentType()
										+"\n The xpath is "+parameters.getXPath()+".The xml is\n"+xml;
					org.apache.log4j.Logger.getLogger(this.getClass()).error(err);
					throw new XtentisException(err);
				}
				TypedContent tc = new TypedContent(res.getBytes("UTF-8"),parameters.getResultingContentType()+";charset=utf-8");
				//save result to context
				context.put(OUTPUT_TEXT, tc);
				//call the callback content is ready
				context.getPluginCallBack().contentIsReady(context);
			}
			org.apache.log4j.Logger.getLogger(this.getClass()).debug("execute() XPath done");
		} catch (XtentisException xe) {
			throw (xe);
		} catch (Exception e) {
			e.printStackTrace();
			String err = "Could not execute the xPath transformer plugin "+
				e.getClass().getName()+": "+e.getLocalizedMessage();
			org.apache.log4j.Logger.getLogger(this.getClass()).error(err);
			throw new XtentisException(e);
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
	public String getDocumentation(String twoLettersLanguageCode) throws XtentisException {
		return
		"The XPath plugin executes an XPath on an xml document\n" +
		"The result of the xPath may be an XML fragment (content type of text/xml) or\n" +
		"A text fragment (content type text/plain) or\n" +
		" A nodes set in which case the plugin will loop over the results\n"+
		"\n" +
		"\n" +
		"Parameters\n" +
		"	the xPath expression [mandatory]: the xpath expression to run"+"\n"+
		"	contentType [optional]: the content type of the result of the x path fragment"+"\n"+
		"\n"+
		"\n"+
		"Example" +"\n"+
		"	<parameters>" +"\n"+
		"		<xPath>//Buyer/Name[../City='Paris']</xPath>" +"\n"+
		"		<contentType>text/plain</contentType>" +"\n"+
		"	</parameters>"+"\n"+
		"\n"+
		"\n"+
		"Notes for Plugin Developers: " +"\n"+
		"	None";
	}


	private String getDefaultConfiguration(){
    	return
    		"<configuration>"+
    		"	<xPath>.</xPath>"+
    		"	<contentType>text/xml</contentType>"+
    		"</configuration>";
    }




    /**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public String getConfiguration(String optionalParameters) throws XtentisException{
    	try {
    		String configuration = loadConfiguration();
    		if (configuration == null) {
    			configuration = getDefaultConfiguration();
    		}
    		configurationLoaded = true;
    		return configuration;
        } catch (Exception e) {
    	    String err = "Unable to deserialize the configuration of the XPath Transformer Plugin"
    	    		+": "+e.getClass().getName()+": "+e.getLocalizedMessage();
    	    org.apache.log4j.Logger.getLogger(this.getClass()).error(err);
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
    		CompiledParameters compiled = new CompiledParameters();
    		Element params = Util.parse(parameters).getDocumentElement();
    		//content Type - defaults to "text/plain; charset=utf-8"
    		String contentType = Util.getFirstTextNode(params, "contentType");
    		if (contentType == null) contentType = "text/plain; charset=utf-8";
    		compiled.setResultingContentType(contentType);
    		//xPath - mandatory
			String xPath = Util.getFirstTextNode(params, "xPath");
			if (xPath==null) {
				String err = "The xPath Parameter of the xPath Transformer Plugin cannot be empty";
				org.apache.log4j.Logger.getLogger(this.getClass()).error(err);
				throw new XtentisException(err);
			}
    		compiled.setXPath(xPath);

    		return compiled.serialize();
    	} catch (XtentisException e) {
    		throw(e);
	    } catch (Exception e) {
    	    String err = "Unable to serialize the configuration of the xPath Transformer Plugin"
    	    		+": "+e.getClass().getName()+": "+e.getLocalizedMessage();
    	    org.apache.log4j.Logger.getLogger(this.getClass()).error(err);
    	    throw new XtentisException(err);
	    }
	}





}