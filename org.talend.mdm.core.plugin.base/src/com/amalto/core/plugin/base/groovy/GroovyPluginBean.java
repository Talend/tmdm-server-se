/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.groovy;

import com.amalto.core.objects.Plugin;
import com.amalto.core.objects.transformers.util.TransformerPluginContext;
import com.amalto.core.objects.transformers.util.TransformerPluginVariableDescriptor;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.plugin.base.groovy.CompiledParameters;
import com.amalto.core.plugin.base.groovy.EmbedGroovy;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;
/**
 *
 * @author Starkey Shu
 *
 * @ejb.bean name="GroovyPluginBean"
 *           display-name="Name for GroovyPluginBean"
 *           description="Description for GroovyPluginBean"
 * 		     local-jndi-name = "amalto/local/transformer/plugin/groovy"
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
     
@Service("amalto/local/transformer/plugin/groovy")
public class GroovyPluginBean extends Plugin {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2468112296592499877L;
	//parameter
	public static final String PARAMETERS ="com.amalto.core.plugin.base.groovy.parameters";
	//various
	private static final String VARIABLE_INPUT ="variable_input";
	private static final String SCRIPT_OUTPUT ="script_output";
	
	private static final String booleanRegex ="(true|false)";
	
	public GroovyPluginBean() {
		super();
	}
	
	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public String getJNDIName() throws XtentisException {
		return "amalto/local/transformer/plugin/groovy";
	}
	
	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public String getDescription(String twoLettersLanguageCode)
			throws XtentisException {
		String description="";
		if(twoLettersLanguageCode.toLowerCase().equals("en")){
			description="This is a plugin which you can call the groovy script. ";
		}else{
			description="Unsupported language! ";
		}
		return description;
	}

	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public String getDocumentation(String twoLettersLanguageCode)
			throws XtentisException {
		return
		"The groovy plugin can take the advantages of the groovy script to transformer data. \n" +
		"\n" +
		"\n" +
		"Parameters\n" +
		"	autoParseXml [optional]: parsed the default variable 'variableInput' to a xml object automatically. default: 'false'"+"\n"+
		"	script [mandatory]: the content of the groovy script"+"\n"+
		"\n"+
		"\n"+
		"Example1" +"\n"+
		"	<parameters>" +"\n"+
		"		<autoParseXml>false</autoParseXml>" +"\n"+
		"		<script><![CDATA[" +"\n"+
		"		    def records = new XmlParser().parseText(variableInput);" +"\n"+
		"		    println records;//print parsedXmlObject" +"\n"+
		"		    return records.depthFirst().size();" +"\n"+
		"		]]></script>" +"\n"+
		"	</parameters>"+"\n"+
		"\n" +
		"Example2" +"\n"+
		"	<parameters>" +"\n"+
		"		<autoParseXml>true</autoParseXml>" +"\n"+
		"		<script><![CDATA[" +"\n"+
		"		    def updateReport = variableInput;" +"\n"+
		"		    def itemProjection = MdmGroovyExtension.getItemProjection(" +"\n"+
		"		          updateReport.RevisionID.text()," +"\n"+
		"		          updateReport.DataCluster.text()," +"\n"+
		"		          updateReport.Concept.text()," +"\n"+
		"		          updateReport.Key.text()" +"\n"+
		"		    );" +"\n"+
		"		    //TODO: filter itemProjection" +"\n"+
		"		    return itemProjection;" +"\n"+
		"		]]></script>" +"\n"+
		"	</parameters>"+"\n"+
		"\n" +
		"\n" +
		"Notes for Plugin Developers: " +"\n"+
		"		empty"	;
	}
	
	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public ArrayList<TransformerPluginVariableDescriptor> getInputVariableDescriptors(
			String twoLettersLanguageCode) throws XtentisException {
		 ArrayList<TransformerPluginVariableDescriptor> inputDescriptors = new ArrayList<TransformerPluginVariableDescriptor>();
		
		 TransformerPluginVariableDescriptor descriptor = new TransformerPluginVariableDescriptor();
		 descriptor.setVariableName(VARIABLE_INPUT);
		 descriptor.setContentTypesRegex(
				 new ArrayList<Pattern>(
						 Arrays.asList(new Pattern[]{
								 Pattern.compile("text/.*")
						})
				)
		 );
		 HashMap<String, String> descriptions = new HashMap<String, String>();
		 descriptions.put("en", "The input content which will be passed into the groovy");
		 descriptor.setDescriptions(descriptions);
		 descriptor.setMandatory(true);
		 descriptor.setPossibleValuesRegex(null);
		 inputDescriptors.add(descriptor);
		 return inputDescriptors;
		
	}


	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public ArrayList<TransformerPluginVariableDescriptor> getOutputVariableDescriptors(
			String twoLettersLanguageCode) throws XtentisException {
		 ArrayList<TransformerPluginVariableDescriptor> outputDescriptors = new ArrayList<TransformerPluginVariableDescriptor>();

		 //descriptor
		 TransformerPluginVariableDescriptor descriptor = new TransformerPluginVariableDescriptor();
		 descriptor.setVariableName(SCRIPT_OUTPUT);
		 descriptor.setContentTypesRegex(
				 new ArrayList<Pattern>(
						 Arrays.asList(new Pattern[]{
								 Pattern.compile("text/.*")
						})
				)
		 );
		 HashMap<String, String> descriptions = new HashMap<String, String>();
		 descriptions.put("en", "The output content which returned by the groovy script");
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
	public String getParametersSchema() throws XtentisException {
		// Is this feature in use now?
		return
		"<xsd:schema" +
		" 		elementFormDefault='unqualified'" +
		"		xmlns:xsd='http://www.w3.org/2001/XMLSchema'" +
		">" +
		"<xsd:element name='parameters'>" +
		"			<xsd:complexType >" +
		"				<xsd:sequence>" +
		"					<xsd:element minOccurs='0' maxOccurs='1' nillable='false' name='autoParseXml' type='xsd:string'/>" +
		"					<xsd:element minOccurs='1' maxOccurs='1' nillable='false' name='script' type='xsd:string'/>" +
		"				</xsd:sequence>" +
		"			</xsd:complexType>" +
		"</xsd:element>"+
		"</xsd:schema>";

	}

	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public String compileParameters(String parameters) throws XtentisException {
        try {
			
			if(parameters==null||parameters.length()==0)return "";
			CompiledParameters compiled = new CompiledParameters();
			
			Document params=Util.parse(parameters);
			
    		//script - mandatory case
			String script = Util.getFirstTextNode(params, "//script");
			if (script==null) {
				String err = "The method parameter of the Groovy Transformer Plugin cannot be empty";
				org.apache.log4j.Logger.getLogger(this.getClass()).error(err);
				throw new XtentisException(err);
			}
    		compiled.setScript(script);
    		
    		//optional case
    		boolean isAutoParseXml=false;
    		String autoParseXml = Util.getFirstTextNode(params, "//autoParseXml");
			if (autoParseXml!=null&&autoParseXml.length()>0) {
				if(!autoParseXml.trim().toLowerCase().matches(booleanRegex)){
					String err = "The format of the autoParseXml parameter of the Groovy Transformer Plugin is unavailable";
					org.apache.log4j.Logger.getLogger(this.getClass()).error(err);
					throw new XtentisException(err);
				}
				isAutoParseXml=Boolean.parseBoolean(autoParseXml.trim());
			}
    		compiled.setAutoParseXml(isAutoParseXml);

    		return compiled.serialize();
    		
    	} catch (XtentisException e) {
    		throw(e);
	    } catch (Exception e) {
    	    String err = "Unable to serialize the configuration of the groovy Plugin"
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
	public void init(TransformerPluginContext context, String compiledParameters)
			throws XtentisException {

		try {

			//parse parameters
			CompiledParameters parameters=CompiledParameters.deserialize(compiledParameters);
			context.put(PARAMETERS, parameters);
			
		}  catch (Exception e) {
			String err = "Could not init the groovy plugin:"+
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
	public void execute(TransformerPluginContext context)
			throws XtentisException {
		org.apache.log4j.Logger.getLogger(this.getClass()).trace("execute() groovy");
		
		CompiledParameters parameters= (CompiledParameters)context.get(PARAMETERS);
		TypedContent textTC = (TypedContent)context.get(VARIABLE_INPUT);
		try {

			//attempt to read charset
			String charset = Util.extractCharset(textTC.getContentType());
			String inText = new String(textTC.getContentBytes(),charset);
			
			String script=parameters.getScript();
			EmbedGroovy embedGroovy = new EmbedGroovy(inText);
			script="import com.amalto.core.plugin.base.groovy.MdmGroovyExtension\n"+script;//add default import
			if(parameters.isAutoParseXml()) {
				script="variableInput = new XmlParser().parseText(variableInput);\n"+script;
			}
			Object result = embedGroovy.runScript(script);
			
			String outText=null;
			if(result!=null) {
				outText=result.toString();
			    context.put(SCRIPT_OUTPUT, new TypedContent(outText.getBytes(),"text/xml;charset=utf-8"));
			}else {
				context.put(SCRIPT_OUTPUT, null);
			}
				
			//call the callback content is ready
			context.getPluginCallBack().contentIsReady(context);

		} catch (XtentisException xe) {
			throw (xe);
		} catch (Exception e) {
			String err = "Could not execute the groovy plugin "+
				e.getClass().getName()+": "+e.getLocalizedMessage();
			org.apache.log4j.Logger.getLogger(this.getClass()).error(err,e);
			throw new XtentisException(e);
		}
		
		org.apache.log4j.Logger.getLogger(this.getClass()).trace("execute() groovy done");
		
	}


}