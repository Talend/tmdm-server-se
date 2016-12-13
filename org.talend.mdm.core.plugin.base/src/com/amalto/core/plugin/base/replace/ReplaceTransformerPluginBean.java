/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.replace;

import com.amalto.core.objects.Plugin;
import com.amalto.core.objects.transformers.util.TransformerPluginContext;
import com.amalto.core.objects.transformers.util.TransformerPluginVariableDescriptor;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.plugin.base.replace.CompiledParameters;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

import org.springframework.stereotype.Service;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <h1>Replace Plugin</h1>
 * <h3>Plugin name: replace</h3>
 * <h3>Description</h3>
 * The Replace plugin executes a regular expression on an input text, and replace the text by the result.
 * The result may include references to matching groups of the regular expression<br/>
 * <br/>
 * This plugin is usually used in front of the CSV Parser Plugin to fix incorrectly formatted CSVs
 * <h3>Inputs</h3>
 * <ul>
 * <li><b>text</b>: the text to apply the regular expression to</li>
 * </ul>
 * <h3>Outputs</h3>
 * <ul>
 * <li><b>replaced</b>: the result of the replace</li>
 * </ul>
 * <h3>Parameters</h3>
 * The parameters are specified as an XML <pre>
    &lt;parameters&gt;
      &lt;regexp&gt;REGULAR_EXPRESSION&lt;/regexp&gt;
      &lt;replacement&gt;&lt;![CDATA[
        &lt;myXML&gt;$1&lt;/myXML&gt;
      ]]&gt;&lt;/replacement&gt;
      &lt;contentType&gt;RESULT_CONTENT_TYPE&lt;/contentType&gt;
    &lt;/parameters&gt;
 * </pre>
 * <ul>
 * <li><b>regexp</b>: The regular expression</li>
 * <li><b>replacement</b>: The expected result. The numbers following the dollar signs are replaced
 * by the matching group of the regular expression: <code>$n</code> will insert the result of group n</li>
 * <li><b>contentType</b>: The content/type of the result that will be affected to the output: text/xml for an XML,
 * text/html for an html, text/plain for a plain text, etc...</li>
 *</ul>
 * <h3>Example</h3>
 * The following example parameters will read a CSV line and replace the commas separating the columns
 * with the word ###AMALTO###
 * <pre>
    &lt;parameters&gt;
      &lt;regexp&gt;(\"[^\",]*)(,)([^\",]*\")&lt;/regexp&gt;
      &lt;replacement&gt;$1##AMALTO##$3&lt;/replacement&gt;
      &lt;contentType&gt;text/plain&lt;/contentType&gt;
    &lt;/parameters&gt;
 * </pre>
 *
 * @author Bruno Grieder, David Le Niniven
 *
 * @ejb.bean name="ReplaceTransformerPlugin"
 *           display-name="Name for ReplacePlugin"
 *           description="Description for ReplacePlugin"
 * 		  local-jndi-name = "amalto/local/transformer/plugin/replace"
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
@Service("amalto/local/transformer/plugin/replace")
public class ReplaceTransformerPluginBean extends Plugin {

	private static final String PARAMETERS = "com.amalto.core.plugin.replace.parameters";

	private static final String INPUT_TEXT = "text";

	private static final String OUTPUT_TEXT = "replaced";

	private static final long serialVersionUID = 1L;

    private transient boolean configurationLoaded = false;


	public ReplaceTransformerPluginBean() {
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
		return "amalto/local/transformer/plugin/replace";
	}



    /**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public String getDescription(String twoLetterLanguageCode) throws XtentisException {
		if ("fr".matches(twoLetterLanguageCode.toLowerCase()))
			return "Execute un regexp sur un text, remplace par la valeur en paramètre et retourne le résultat";
		return "Executes a regexp on a text, replaces with parameter value and returns the result";
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
		 descriptor1.setVariableName(INPUT_TEXT);
		 descriptor1.setContentTypesRegex(
				 new ArrayList<Pattern>(
						 Arrays.asList(new Pattern[]{
								 Pattern.compile("text.*")
						})
				)
		 );
		 HashMap<String, String> descriptions1 = new HashMap<String, String>();
		 descriptions1.put("en", "The text to run the Replace on");
		 descriptor1.setDescriptions(descriptions1);
		 descriptor1.setMandatory(true);
		 descriptor1.setPossibleValuesRegex(null);
		 inputDescriptors.add(descriptor1);

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
		 descriptions.put("en", "The result of the Replace");
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
			String err = "Could not init the Replace plugin:"+
				e.getClass().getName()+": "+e.getLocalizedMessage();
			org.apache.log4j.Logger.getLogger(this.getClass()).error(err,e);
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

		CompiledParameters parameters= (CompiledParameters)context.get(PARAMETERS);
		TypedContent textTC = (TypedContent)context.get(INPUT_TEXT);
		Pattern p = parameters.getRegexp();
		String replacement = parameters.getReplacement();

		try {

			//attempt to read charset
			String charset = Util.extractCharset(textTC.getContentType());
			String text = new String(textTC.getContentBytes(),charset);

			org.apache.log4j.Logger.getLogger(this.getClass()).trace("execute() Pattern:'"+p.toString()+"' text \n"+text);

			Matcher m = p.matcher(text);
			String result = m.replaceAll(replacement);

			String ct = parameters.getResultingContentType()+"; charset=utf-8";
			byte[] bytes = (result == null ? null : result.getBytes("utf-8"));
			//save result to context
			context.put(OUTPUT_TEXT, new TypedContent(bytes,ct));
			//call the callback content is ready
			context.getPluginCallBack().contentIsReady(context);

		} catch (XtentisException xe) {
			throw (xe);
		} catch (Exception e) {
			String err = "Could not execute the regexp transformer plugin "+
				e.getClass().getName()+": "+e.getLocalizedMessage();
			org.apache.log4j.Logger.getLogger(this.getClass()).error(err,e);
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
		"The replace plugin execute substitution on a text\n" +
		"The plugin will replace every subsequence of the input sequence that matches the pattern with the given replacement string\n" +
		"\n" +
		"\n" +
		"Parameters\n" +
		"	regexp [mandatory]: the regexp expression to run"+"\n"+
		"	contentType [optional]: the content type of the result of the replace"+"\n"+
		"	replacement [mandatory]: the replacement string " +"\n"+
		"\n"+
		"\n"+
		"Example" +"\n"+
		"	<parameters>" +"\n"+
		"		<regexp>(\"[^\",]*)(,)([^\",]*\")</regexp>" +"\n"+
		"		<contentType>text/plain</contentType>" +"\n"+
		"		<replacement>$1##AMALTO##$3</replacement>" +"\n"+
		"	</parameters>"+"\n"+
		"\n"+
		"\n";
	}


	private String getDefaultConfiguration(){
    	return
		"<configuration>"+
		"	<regexp>.*</regexp>"+
		"	<contentType>text/xml</contentType>"+
		"	<replacement></replacement>"+
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
    	    String err = "Unable to deserialize the configuration of the Replace Transformer Plugin"
    	    		+": "+e.getClass().getName()+": "+e.getLocalizedMessage();
    	    org.apache.log4j.Logger.getLogger(this.getClass()).error(err,e);
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

    		//replacement - mandatory
    		String replacement = Util.getFirstTextNode(params, "replacement");
    		if (replacement == null) replacement="";
    		compiled.setReplacement(replacement);

    		//Regexp - mandatory
			String regexp = Util.getFirstTextNode(params, "regexp");
			if (regexp==null) {
				String err = "The regexp parameter of the Replace Transformer Plugin cannot be empty";
				org.apache.log4j.Logger.getLogger(this.getClass()).error(err);
				throw new XtentisException(err);
			}
    		compiled.setRegexp(Pattern.compile(regexp, Pattern.DOTALL));

    		return compiled.serialize();
    	} catch (XtentisException e) {
    		throw(e);
	    } catch (Exception e) {
    	    String err = "Unable to serialize the configuration of the Replace Transformer Plugin"
    	    		+": "+e.getClass().getName()+": "+e.getLocalizedMessage();
    	    org.apache.log4j.Logger.getLogger(this.getClass()).error(err,e);
    	    throw new XtentisException(err);
	    }
	}
}
