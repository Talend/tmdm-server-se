/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.dumpandgo;

import com.amalto.core.objects.Plugin;
import com.amalto.core.objects.transformers.util.TransformerPluginContext;
import com.amalto.core.objects.transformers.util.TransformerPluginVariableDescriptor;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
/**
 *
 * @author Starkey Shu
 *
 * @ejb.bean name="DumpAndGoPlugin"
 *           display-name="Name for DumpAndGoPlugin"
 *           description="Description for DumpAndGoPlugin"
 * 		  local-jndi-name = "amalto/local/transformer/plugin/dumpandgo"
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
     
@Service("amalto/local/transformer/plugin/dumpandgo")
public class DumpAndGoPluginBean extends Plugin {

	private static final long serialVersionUID = 6927070668734342292L;

	//parameter
	public static final String PARAMETERS ="com.amalto.core.plugin.base.dumpandgo.parameters";
	//various
	private static final String INPUT_TEXT ="in_text";
	private static final String OUTPUT_TEXT ="out_text";
	
	public DumpAndGoPluginBean() {
		super();
	}
	
	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	@Override
    public String getJNDIName() throws XtentisException {
		return "amalto/local/transformer/plugin/dumpandgo";
	}
	
	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	@Override
    public String getDescription(String twoLettersLanguageCode)
			throws XtentisException {
		String description="";
		if(twoLettersLanguageCode.toLowerCase().equals("en")){
			description="This is a plugin used for dump text and pass it. ";
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
	@Override
    public String getDocumentation(String twoLettersLanguageCode)
			throws XtentisException {
		return
		"The DumpAndGo plugin can dump your input text to console and pass it to the next step. \n" +
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
	@Override
    public ArrayList<TransformerPluginVariableDescriptor> getInputVariableDescriptors(
            String twoLettersLanguageCode) throws XtentisException {
		 ArrayList<TransformerPluginVariableDescriptor> inputDescriptors = new ArrayList<TransformerPluginVariableDescriptor>();
		
		 TransformerPluginVariableDescriptor descriptor = new TransformerPluginVariableDescriptor();
		 descriptor.setVariableName(INPUT_TEXT);
		 descriptor.setContentTypesRegex(
				 new ArrayList<Pattern>(
						 Arrays.asList(new Pattern[]{
								 Pattern.compile("text/.*")
						})
				)
		 );
		 HashMap<String, String> descriptions = new HashMap<String, String>();
		 descriptions.put("en", "The text content to enter");
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
	@Override
    public ArrayList<TransformerPluginVariableDescriptor> getOutputVariableDescriptors(
            String twoLettersLanguageCode) throws XtentisException {
		 ArrayList<TransformerPluginVariableDescriptor> outputDescriptors = new ArrayList<TransformerPluginVariableDescriptor>();

		 //descriptor
		 TransformerPluginVariableDescriptor descriptor = new TransformerPluginVariableDescriptor();
		 descriptor.setVariableName(OUTPUT_TEXT);
		 descriptor.setContentTypesRegex(
				 new ArrayList<Pattern>(
						 Arrays.asList(new Pattern[]{
								 Pattern.compile("text/.*")
						})
				)
		 );
		 HashMap<String, String> descriptions = new HashMap<String, String>();
		 descriptions.put("en", "The output text");
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
	@Override
    public String getParametersSchema() throws XtentisException {
		// Is this feature in use now?
		return null;
	}

	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	@Override
    public String compileParameters(String parameters) throws XtentisException {
		return parameters;
	}

	

	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	@Override
    public void init(TransformerPluginContext context, String compiledParameters)
			throws XtentisException {

		try {

			context.put(PARAMETERS, compiledParameters);
			
		}  catch (Exception e) {
			String err = "Could not init the DumpAndGo plugin:"+
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
	@Override
    public void execute(TransformerPluginContext context)
			throws XtentisException {
		org.apache.log4j.Logger.getLogger(this.getClass()).trace("execute() DumpAndGo");
		
		TypedContent textTC = (TypedContent)context.get(INPUT_TEXT);
		try {

			//attempt to read charset
			String charset = Util.extractCharset(textTC.getContentType());
			String inText = new String(textTC.getContentBytes(),charset);
			
			org.apache.log4j.Logger.getLogger(this.getClass()).info("[Dump]:\n"+inText);
			
			String outText=inText;			
			
			context.put(OUTPUT_TEXT, new TypedContent(outText.getBytes(),"text/xml;charset=utf-8"));
			//call the callback content is ready
			context.getPluginCallBack().contentIsReady(context);

		} catch (XtentisException xe) {
			throw (xe);
		} catch (Exception e) {
			String err = "Could not execute the dumpAndGo plugin "+
				e.getClass().getName()+": "+e.getLocalizedMessage();
			org.apache.log4j.Logger.getLogger(this.getClass()).error(err,e);
			throw new XtentisException(e);
		}
		
		org.apache.log4j.Logger.getLogger(this.getClass()).trace("execute() DumpAndGo done");
		
	}


}