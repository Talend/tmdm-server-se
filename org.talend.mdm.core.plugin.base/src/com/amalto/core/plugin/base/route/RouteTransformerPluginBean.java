/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.plugin.base.route;

import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.Plugin;
import com.amalto.core.objects.transformers.util.TransformerPluginContext;
import com.amalto.core.objects.transformers.util.TransformerPluginVariableDescriptor;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.plugin.base.route.CompiledParameters;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;



/**
* <h1>Route Item Plugin</h1>
 * <h3>Plugin name: route</h3>
 * <h3>Description</h3>
 * The Route plugin submits an item to subscription engine<br/>
 * <br/>
 * This plugin is usually used after the Project Item plugin which outputs a Primary Key
 * <h3>Inputs</h3>
 * <ul>
 * <li><b>item_primary_key</b>: the primary key of the item to route as an object of type <code>application/xtentis.itempk</code></li>
 * </ul>
 * <h3>Outputs</h3>
 * <ul>
 * <li><b>item_primary_key</b>: the primary key of the item routed as an object of type <code>application/xtentis.itempk</code></li>
 * </ul>
 * <h3>Parameters</h3>
 * There are no parameters for this plugin.<br/>
 * <br/>
 *
 * @author Bruno Grieder
 *
 * @ejb.bean name="RouteTransformerPlugin"
 *           display-name="Name for RoutePlugin"
 *           description="Description for RoutePlugin"
 * 		  local-jndi-name = "amalto/local/transformer/plugin/route"
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
@Service("amalto/local/transformer/plugin/route")
public class RouteTransformerPluginBean extends Plugin {

	//private final static Pattern declarationPattern = Pattern.compile("<\\?.*?\\?>",Pattern.DOTALL);

	private static final String PARAMETERS ="com.amalto.core.plugin.route.parameters";

	private static final long serialVersionUID = 5648768989892480L;

	private static final String INPUT_PK = "item_primary_key";
	private static final String OUTPUT_PK = "item_primary_key";

    private transient boolean configurationLoaded = false;



	public RouteTransformerPluginBean() {
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
		return "amalto/local/transformer/plugin/route";
	}



    /**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public String getDescription(String twoLetterLanguageCode) throws XtentisException {
		if ("fr".matches(twoLetterLanguageCode.toLowerCase()))
			return "Soumet un item au moteur d'abonnements";
		return "Submits an item to the subscription engine";
	}




	/**
     * @throws XtentisException
     *
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
	public ArrayList<TransformerPluginVariableDescriptor> getInputVariableDescriptors(String twoLettersLanguageCode) throws XtentisException {
		 ArrayList<TransformerPluginVariableDescriptor> inputDescriptors = new ArrayList<TransformerPluginVariableDescriptor>();


		 TransformerPluginVariableDescriptor descriptor1 = new TransformerPluginVariableDescriptor();
		 descriptor1.setVariableName(INPUT_PK);
		 descriptor1.setContentTypesRegex(
				 new ArrayList<Pattern>(
						 Arrays.asList(new Pattern[]{
								 Pattern.compile("(text/xml|application/xtentis.itempk)")
						})
				)
		 );
		 HashMap<String, String> descriptions1 = new HashMap<String, String>();
		 descriptions1.put("en", "The item instance primary key.");
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
		 descriptor.setVariableName(OUTPUT_PK);
		 descriptor.setContentTypesRegex(
				 new ArrayList<Pattern>(
						 Arrays.asList(new Pattern[]{
								 Pattern.compile("application/xtentis.itempk")
						})
				)
		 );
		 HashMap<String, String> descriptions = new HashMap<String, String>();
		 descriptions.put("en", "The xmlized csv line");
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

			//parse parameters
			CompiledParameters parameters = CompiledParameters.deserialize(compiledParameters);
			context.put( PARAMETERS, parameters);

		} catch (Exception e) {
			String err = "Could not init the Route plugin:"+
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

		org.apache.log4j.Logger.getLogger(this.getClass()).debug("execute() ");

		try {

//			CompiledParameters parameters= (CompiledParameters)context.get( PARAMETERS);
			TypedContent inputPKTC = (TypedContent)context.get(INPUT_PK);

			//Get the Key
			ItemPOJOPK pk =  ItemPOJOPK.unmarshal(new String(inputPKTC.getContentBytes(),"UTF8"));

			org.apache.log4j.Logger.getLogger(this.getClass()).debug("execute() PK "+pk);

			org.apache.log4j.Logger.getLogger(this.getClass()).debug("execute() Routing "+pk.getUniqueID());

			//now perform updates
			//getRoutingEngineLocal().routeItemNow(pk);
			//Routing engine V2
			Util.getRoutingEngineV2CtrlLocal().route(pk);

			//save result to context
			context.put(OUTPUT_PK, new TypedContent(pk.marshal().getBytes("utf-8"),"application/xtentis.itempk"));
			//call the callback content is ready
			context.getPluginCallBack().contentIsReady(context);

		} catch (XtentisException xe) {
			throw (xe);
		} catch (Exception e) {
			String err = "Could not execute the Route transformer plugin "+
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
	public String getDocumentation(String twoLettersLanguageCode) throws XtentisException {
		return
		"The Route plugin sumits an item to the Subscription Engine." +
		"No parameters are required" +
		"\n" +
		"\n" +
		"Parameters\n" +
		"	None"+"\n"+
		"\n";
	}


	private String getDefaultConfiguration(){
    	return
    		"<configuration>"+
    		"	<charset>utf-8</charset>"+
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
    	    String err = "Unable to deserialize the configuration of the Route Transformer Plugin"
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
	public String getParametersSchema() throws XtentisException {
		return null;
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
    		//Element params = Util.parse(parameters).getDocumentElement();

    		return compiled.serialize();
	    } catch (Exception e) {
    	    String err = "Unable to serialize the configuration of the Route Transformer Plugin"
    	    		+": "+e.getClass().getName()+": "+e.getLocalizedMessage();
    	    org.apache.log4j.Logger.getLogger(this.getClass()).error(err,e);
    	    throw new XtentisException(err);
	    }
	}

}
