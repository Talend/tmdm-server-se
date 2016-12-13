/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package talend.core.transformer.plugin.v2.tiscall;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.core.jobox.JobContainer;
import com.amalto.core.jobox.JobInvokeConfig;
import com.amalto.core.jobox.component.MDMJobInvoker;
import com.amalto.core.objects.Plugin;
import com.amalto.core.objects.transformers.util.TransformerPluginContext;
import com.amalto.core.objects.transformers.util.TransformerPluginVariableDescriptor;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.server.api.Transformer;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

/**
 * @author Bruno Grieder
 * 
 * @ejb.bean name="TISCallTransformerPlugin" display-name="Name for TISCallPlugin"
 * description="Description for TISCallPlugin" local-jndi-name = "amalto/local/transformer/plugin/callJob"
 * type="Stateless" view-type="local"
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
@Service(TISCallTransformerPluginBean.PLUGIN_NAME)
public class TISCallTransformerPluginBean extends Plugin {

    private static final String CONTENT_TYPE = "com.amalto.core.plugin.TISCall.content.type";

    private static final String PORT = "com.amalto.core.plugin.TISCall.port";

    private static final String INVOKE_CONFIG = "com.amalto.core.plugin.TISCall.invoke.config";

    private static final String TIS_VARIABLE_NAME = "com.amalto.core.plugin.TISCall.tis.variable.name";

    private static final String LTJ_PROTOCOL = "ltj";

    private static final String HTTP_PROTOCOL = "http";

    private static final String INPUT_TEXT = "text";

    private static final String OUTPUT_TEXT = "result";

    private static final Logger LOGGER = Logger.getLogger(TISCallTransformerPluginBean.class);

    public static final String PLUGIN_NAME = "amalto/local/transformer/plugin/callJob";

    private transient boolean configurationLoaded = false;

    private CompiledParameters compiledParameters;

    /**
     * @throws XtentisException
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    @Override
    public String getJNDIName() throws XtentisException {
        return PLUGIN_NAME;
    }

    /**
     * @throws XtentisException
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    @Override
    public String getDescription(String twoLetterLanguageCode) throws XtentisException {
        if ("fr".matches(twoLetterLanguageCode.toLowerCase())) {
            return "Execute un call de TIS un texte et retourne le résultat";
        }
        return "Executes a TIS Job on a text and returns the result";
    }

    /**
     * @throws XtentisException
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    @Override
    public ArrayList<TransformerPluginVariableDescriptor> getInputVariableDescriptors(String twoLettersLanguageCode)
            throws XtentisException {
        ArrayList<TransformerPluginVariableDescriptor> inputDescriptors = new ArrayList<TransformerPluginVariableDescriptor>();

        // The csv_line descriptor
        TransformerPluginVariableDescriptor descriptor1 = new TransformerPluginVariableDescriptor();
        descriptor1.setVariableName(INPUT_TEXT);
        descriptor1.setContentTypesRegex(Arrays.asList(Pattern.compile("text.*")));
        Map<String, String> descriptions1 = new HashMap<String, String>();
        descriptions1.put("en", "The text to run the TISCall on");
        descriptor1.setDescriptions(descriptions1);
        descriptor1.setMandatory(false);
        descriptor1.setPossibleValuesRegex(Collections.<Pattern> emptyList());
        inputDescriptors.add(descriptor1);

        return inputDescriptors;
    }

    /**
     * @throws XtentisException
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    @Override
    public ArrayList<TransformerPluginVariableDescriptor> getOutputVariableDescriptors(String twoLettersLanguageCode)
            throws XtentisException {
        ArrayList<TransformerPluginVariableDescriptor> outputDescriptors = new ArrayList<TransformerPluginVariableDescriptor>();

        // The csv_line descriptor
        TransformerPluginVariableDescriptor descriptor = new TransformerPluginVariableDescriptor();
        descriptor.setVariableName(OUTPUT_TEXT);
        descriptor.setContentTypesRegex(Arrays.asList(Pattern.compile("text/xml")));
        Map<String, String> descriptions = new HashMap<String, String>();
        descriptions.put("en", "The result of the TIS call");
        descriptor.setDescriptions(descriptions);
        descriptor.setMandatory(true);
        descriptor.setPossibleValuesRegex(Collections.<Pattern> emptyList());
        outputDescriptors.add(descriptor);

        return outputDescriptors;
    }

    /**
     * @throws XtentisException
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    @Override
    public void init(TransformerPluginContext context, String parameters) throws XtentisException {
        try {
            if (!configurationLoaded) {
                loadConfiguration();
            }

            compiledParameters = CompiledParameters.deserialize(parameters);

            // set the parameters
            JobInvokeConfig jobInvokeConfig;
            URI uri = URI.create(compiledParameters.getUrl());
            String protocol = uri.getScheme();
            String jobName = uri.getHost();
            if (jobName == null) {
                jobName = uri.getAuthority();
            }
            String jobVersion = uri.getPath().substring(1);

            if (LTJ_PROTOCOL.equals(protocol)) {
                jobInvokeConfig = new JobInvokeConfig();
                jobInvokeConfig.setJobName(jobName);
                jobInvokeConfig.setJobVersion(jobVersion);
                context.put(INVOKE_CONFIG, jobInvokeConfig);
            } else if (HTTP_PROTOCOL.equalsIgnoreCase(protocol)) {
                throw new NotImplementedException(); // TODO (rewrite -> HTTP call heavily depended on JBoss classes).
            } else {
                throw new IllegalArgumentException("Protocol '" + protocol + "' is not supported.");
            }

            context.put(CONTENT_TYPE, compiledParameters.getContentType());
            context.put(TIS_VARIABLE_NAME, compiledParameters.getTisVariableName());
        } catch (XtentisException xe) {
            throw (xe);
        } catch (Exception e) {
            String err = "Could not init the TISCall plugin:" + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(e);
        }

    }

    @Override
    protected String loadConfiguration() {
        return null;
    }

    /**
     * @throws XtentisException
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    @Override
    public void execute(TransformerPluginContext context) throws XtentisException {
        String contentType = (String) context.get(CONTENT_TYPE);
        JobInvokeConfig invokeConfig = (JobInvokeConfig) context.get(INVOKE_CONFIG);
        try {
            String charset = org.apache.commons.lang.CharEncoding.UTF_8;
            // the text should be a map(key=value)
            Properties p = new Properties();
            if (compiledParameters.getTisContext() != null) {
                for (ContextParam kv : compiledParameters.getTisContext()) {
                    String value = kv.getValue();
                    // if pipeline variable name ,get from global context
                    if (kv.isPipelineVariableName()) {
                        TypedContent textTC = getGlobalContext().getFromPipeline(kv.getValue());

                        if (textTC == null) {
                            throw new XtentisException(
                                    "The variable '"
                                            + kv.getName()
                                            + "' was not found in the plug-in inputs. It is either not specified or it may be misspelled.");
                        }

                        charset = Util.extractCharset(textTC.getContentType());
                        if (textTC != null) {
                            value = new String(textTC.getContentBytes(), charset);
                        } else {
                            value = StringUtils.EMPTY;
                        }
                    }
                    p.setProperty(kv.getName(), value);
                }
            }

            Map<String, String> argsMap = new HashMap<String, String>();
            for (Object o : p.keySet()) {
                String key = (String) o;
                String value = p.getProperty(key);
                String param = "--context_param " + key + "=" + value;
                argsMap.put(key, value);
            }

            // parse concept parameters
            ConceptMappingParam conceptMappingParam = compiledParameters.getConceptMappingParam();

            // Build call parameters
            List<String[]> list = new ArrayList<String[]>();

            if (invokeConfig != null) { // Local test job invocation

                argsMap.put(MDMJobInvoker.EXCHANGE_XML_PARAMETER, new String(context
                        .getFromPipeline(Transformer.DEFAULT_VARIABLE).getContentBytes(), charset));
                String[][] result = JobContainer.getUniqueInstance()
                        .getJobInvoker(invokeConfig.getJobName(), invokeConfig.getJobVersion()).call(argsMap);
                for (String[] currentResult : result) {
                    list.add(currentResult);
                }
            } else { // Web service invocation
                // recover the port
                throw new NotImplementedException(); // TODO (rewrite -> HTTP call heavily depended on JBoss classes).
            }

            String result = buildParameters(list, conceptMappingParam);
            LOGGER.debug("execute() TIS CALL  result \n" + result);
            // save result to context
            if (result != null) {
                context.put(OUTPUT_TEXT, new TypedContent(result.getBytes("utf-8"), contentType));
            } else {
                context.put(OUTPUT_TEXT, null);
            }

            // call the callback content is ready
            context.getPluginCallBack().contentIsReady(context);

        } catch (XtentisException xe) {
            throw (xe);
        } catch (Exception e) {
            String err;
            if (invokeConfig != null) {
                err = "Could not execute the tisCall transformer plugin for job " + invokeConfig.getJobName() + " in version " + invokeConfig.getJobVersion(); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                err = "Could not execute the tisCall transformer plugin for service " + compiledParameters.getUrl(); //$NON-NLS-1$
            }
            LOGGER.error(err, e);
            throw new XtentisException(e);
        }
    }

    @SuppressWarnings("nls")
    public String buildParameters(List<String[]> list, ConceptMappingParam conceptMappingParam) throws XtentisException {
        boolean hasConcept = false;
        String conceptName = "";
        boolean hasFields = false;
        JSONObject fieldsObject = null;
        StringBuilder sb = new StringBuilder();
        try {
            if (conceptMappingParam != null) {
                if (conceptMappingParam.getConcept().length() > 0) {
                    hasConcept = true;
                    conceptName = conceptMappingParam.getConcept();
                }
                if (conceptMappingParam.getFields().length() > 0) {
                    hasFields = true;
                    fieldsObject = new JSONObject(conceptMappingParam.getFields());

                }
            }
            // FIXME use a better way ?
            if (list.size() > 0) {
                sb = sb.append("<results>\n");
                for (String[] aList : list) {
                    if (hasConcept) {
                        sb = sb.append("<").append(conceptName).append(">\n");
                    } else {
                        sb = sb.append("<item>\n");
                    }
                    for (int j = 0; j < aList.length; j++) {
                        String str = aList[j];
                        if (str != null) {
                            // remove xml header
                            str = str.replaceFirst("<\\?xml .*\\?>", "");
                            if (hasFields) {
                                if (fieldsObject.has("p" + j)) {
                                    String columnName = (String) fieldsObject.get("p" + j);
                                    if (columnName != null && columnName.contains("/")) {
                                        String[] columns = columnName.split("/");
                                        for (int i = 0; i < columns.length; i++) {
                                            sb.append("<").append(columns[i]).append(">");
                                        }
                                        sb.append(str);
                                        for (int k = columns.length - 1; k >= 0; k--) {
                                            sb.append("</").append(columns[k]).append(">");
                                        }
                                        sb.append("\n");
                                    } else {
                                        sb = sb.append("<").append(columnName).append(">").append(str).append("</")
                                                .append(columnName).append(">" + "\n");
                                    }
                                } else {
                                    // do nothing
                                }
                            } else {
                                sb = sb.append("<attr>").append(str).append("</attr>" + "\n");
                            }
                        }
                    }
                    if (hasConcept) {
                        sb = sb.append("</").append(conceptName).append(">\n");
                    } else {
                        sb = sb.append("</item>\n");
                    }
                }
                sb = sb.append("</results>" + "\n");
            } else {
                // TODO exception
            }
        } catch (Exception e) {
            LOGGER.error("Build Parameters Error: Could not execute the tisCall transformer plugin", e);
            throw new XtentisException(e);
        }
        return sb.toString();
    }

    /**
     * @throws XtentisException
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    @Override
    public String getParametersSchema() throws XtentisException {
        return null;
    }

    /**
     * @throws XtentisException
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    @Override
    public String getDocumentation(String twoLettersLanguageCode) throws XtentisException {
        return "The TIS Call plugin executes a Web Service call to TIS on a text\n" + "\n" + "\n" + "Parameters\n"
                + "	url [mandatory]: the webservice port URL to the TIS Server" + "\n"
                + "		or the local talend job URL: ltj://<jobName>/<jobVersion>/[jobMainClass]" + "\n"
                + "	contextParam   : the contextParam of the tis job" + "\n" + "		name: the name of the context param" + "\n"
                + "		value: the value of context param, the value will be viewed as a pipeline" + "\n"
                + "                   variable if the value is embraced with a brace, " + "\n"
                + "		isPipelineVariableName [optional]: true to set contextParam value as one " + "\n"
                + "                                            pipelinevariableName , this parameter will be " + "\n"
                + "                                            ignored if value is embraced with brace" + "\n"
                + "	username [optional]: the username to use for the call" + "\n"
                + "	password [optional]: the password to  use for the call" + "\n"
                + "	contentType [optional]: the contentType of the returned data. Defaults to 'text/xml'" + "\n"
                + "	conceptMapping [optional]: Directly map the result of a TIS call to a MDM Entity" + "\n"
                + "		concept: the name of the concept" + "\n" + "		fields: mapping rule with json format" + "\n" + "\n" + "\n"
                + "Example1" + "\n" + "	<configuration>" + "\n" + "		<url>http://server:port/TISService/TISPort</url>" + "\n"
                + "		<contextParam>" + "\n" + "			<name>firstname</name>" + "\n" + "			<value>jack</value>" + "\n"
                + "		</contextParam>" + "\n" + "		<contextParam>" + "\n" + "			<name>lastname</name>" + "\n"
                + "			<value>jones</value>" + "\n" + "		</contextParam>" + "\n" + "		<contextParam>" + "\n"
                + "			<name>company</name>" + "\n" + "			<value>{pipelineVariableName}</value>" + "\n" + "		</contextParam>"
                + "\n" + "		<username>john</username>" + "\n" + "		<password>doe</password>" + "\n" + "		<conceptMapping>" + "\n"
                + "			<concept>User</concept>" + "\n" + "			<fields>" + "\n" + "			  {" + "\n" + "			  p1:firstname," + "\n"
                + "			  p2:lastname" + "\n" + "			  }" + "\n" + "			</fields>" + "\n" + "		</conceptMapping>" + "\n"
                + "	</configuration>" + "\n" + "\n" + "Example2" + "\n" + "	<configuration>" + "\n"
                + "		<url>ltj://tiscall_multi_return/0.1</url>" + "\n" + "		<contextParam>" + "\n" + "			<name>nb_line</name>"
                + "\n" + "			<value>5</value>" + "\n" + "		</contextParam>" + "\n" + "	</configuration>" + "\n" + "\n";
    }

    private String getDefaultConfiguration() {
        return "<configuration>" + "	<url>http://server:port/TISService/TISPort</url>" + "	<username></username>"
                + "	<password></password>" + "	<contentType>text/xml</contentType>" + "</configuration>";
    }

    /**
     * @throws XtentisException
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
            String err = "Unable to deserialize the configuration of the TISCall Transformer Plugin" + ": "
                    + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err);
        }
    }

    /**
     * @throws XtentisException
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public void putConfiguration(String configuration) throws XtentisException {
        configurationLoaded = false;
    }

    /**
     * @throws XtentisException
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    @Override
    public String compileParameters(String parameters) throws XtentisException {
        try {
            CompiledParameters compiled = new CompiledParameters();
            Element params = Util.parse(parameters).getDocumentElement();

            // TISCall - mandatory
            String url = Util.getFirstTextNode(params, "url");
            if (url == null) {
                String err = "The url parameter of the TIS Call Transformer Plugin cannot be empty";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            compiled.setUrl(url);

            String tisVariableName = Util.getFirstTextNode(params, "tisVariableName");
            compiled.setTisVariableName(tisVariableName);
            Document parametersDoc = Util.parse(parameters);
            List<ContextParam> paramsList = new ArrayList<ContextParam>();
            NodeList paramList = Util.getNodeList(parametersDoc.getDocumentElement(), "//contextParam");
            for (int i = 0; i < paramList.getLength(); i++) {
                String paramName = Util.getFirstTextNode(paramList.item(i), "name");
                String paramValue = Util.getFirstTextNode(paramList.item(i), "value");
                String isPipelineVariableName = Util.getFirstTextNode(paramList.item(i), "isPipelineVariableName");
                if (paramValue == null) {
                    paramValue = "";
                }

                if (paramName != null) {
                    boolean isPipeline = false;
                    if (isPipelineVariableName != null) {
                        isPipeline = Boolean.valueOf(isPipelineVariableName);
                    }
                    Pattern pipeParamPattern = Pattern.compile("\\{([^\\}]*)\\}");
                    Matcher match = pipeParamPattern.matcher(paramValue);
                    if (match.matches()) {
                        paramValue = match.group(1);
                        isPipeline = true;
                    }
                    paramsList.add(new ContextParam(paramName, paramValue, isPipeline));
                }
            }
            compiled.setTisContext(paramsList);
            // content Type - defaults to "text/plain; charset=utf-8"
            String contentType = Util.getFirstTextNode(params, "contentType");
            if (contentType == null) {
                contentType = "text/xml; charset=utf-8";
            }
            compiled.setContentType(contentType);

            // username - defaults to null
            String username = Util.getFirstTextNode(params, "username");
            compiled.setUsername(username);

            // password - defaults to null
            String password = Util.getFirstTextNode(params, "password");
            compiled.setPassword(password);

            // conceptMapping
            NodeList conceptMappingList = Util.getNodeList(parametersDoc.getDocumentElement(), "//conceptMapping");
            if (conceptMappingList != null && conceptMappingList.getLength() > 0) {
                Node conceptMapping = conceptMappingList.item(0);
                String concept = Util.getFirstTextNode(conceptMapping, "concept");
                String fields = Util.getFirstTextNode(conceptMapping, "fields");
                try {
                    new JSONObject(fields);
                } catch (JSONException e) {
                    String err = "The format of fields parameter of conceptMapping is invalid";
                    LOGGER.error(err);
                    throw new XtentisException(err);
                }
                compiled.setConceptMappingParam(new ConceptMappingParam(concept, fields));
            } else {
                compiled.setConceptMappingParam(null);
            }

            return compiled.serialize();
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to serialize the configuration of the TISCall Transformer Plugin" + ": "
                    + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err);
        }
    }

}