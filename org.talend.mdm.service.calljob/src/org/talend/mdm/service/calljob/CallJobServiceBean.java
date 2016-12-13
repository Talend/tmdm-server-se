/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.service.calljob;

import java.io.Serializable;
import java.net.URI;
import java.util.*;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.w3c.dom.Element;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.Service;
import com.amalto.core.jobox.JobContainer;
import com.amalto.core.jobox.JobInvokeConfig;
import com.amalto.core.jobox.component.MDMJobInvoker;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.api.Item;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

@org.springframework.stereotype.Service("amalto/local/service/callJob")
public class CallJobServiceBean extends Service {

    public static final String JNDI_NAME = "amalto/local/service/callJob";

    public static final String HTTP_PROTOCOL = "http";

    private static final String LTJ_PROTOCOL = "ltj";

    private static final Logger LOGGER = Logger.getLogger(CallJobServiceBean.class);

    private static String createExchangeXML(ItemPOJOPK itemPK) throws XtentisException {
        // get item string from itempojopk
        String value;
        try {
            Item itemCtrl2Local = Util.getItemCtrl2Local();
            ItemPOJO pojo = itemCtrl2Local.getItem(itemPK);
            String updateReportXml = pojo.getProjectionAsString();

            Element root = Util.parse(updateReportXml).getDocumentElement();
            String concept = Util.getFirstTextNode(root, "Concept");//$NON-NLS-1$
            String dataCluster = Util.getFirstTextNode(root, "DataCluster");//$NON-NLS-1$
            String key = Util.getFirstTextNode(root, "Key");//$NON-NLS-1$ 
            ComplexTypeMetadata type = ServerContext.INSTANCE.get().getMetadataRepositoryAdmin().get(dataCluster)
                    .getComplexType(concept);
            Collection<FieldMetadata> keyFields = type.getKeyFields();
            String[] ids;
            int dotNum = countDots(key);
            int keyNum = keyFields.size();

            if (keyNum == 1) {
                ids = new String[] { key };
            } else if (keyNum == dotNum + 1) {
                ids = key.split("\\.");//$NON-NLS-1$
            } else {
                String[] idsTemp = key.split("\\.");//$NON-NLS-1$
                String[] compositeKeyPart = Arrays.copyOfRange(idsTemp, 0, keyNum - 1);
                ids = Arrays.copyOf(compositeKeyPart, keyNum);
                String lastCompositeKey = compositeKeyPart[compositeKeyPart.length - 1];
                ids[keyNum - 1] = key.substring(key.lastIndexOf(compositeKeyPart[compositeKeyPart.length - 1])
                        + lastCompositeKey.length() + 1);
            }

            String clusterPK = Util.getFirstTextNode(root, "DataCluster");//$NON-NLS-1$ 
            ItemPOJOPK itemPk = new ItemPOJOPK(new DataClusterPOJOPK(clusterPK), concept, ids);

            String itemXml = "";//$NON-NLS-1$ 
            if (itemCtrl2Local.existsItem(itemPk) != null) {
                ItemPOJO itempojo = itemCtrl2Local.getItem(itemPk);
                itemXml = itempojo.getProjectionAsString();
            }

            value = Util.mergeExchangeData(itemXml, updateReportXml);
        } catch (Exception e) {
            throw new XtentisException(e);
        }
        return value;
    }

    private static int countDots(String str) {
        int counter = 0;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == '.') {
                counter++;
            }
        }
        return counter;
    }

    /**
     * @ejb.interface-method view-type = "local"
     * @ejb.facade-method
     */
    public Serializable fetchFromOutbound(String command, String parameters, String schedulePlanID) throws XtentisException {
        throw new XtentisException("The callJob service is not meant to interact with adapters");
    }

    @Override
    public void putConfiguration(String configuration) {
    }

    public String getDescription(String twoLettersLanguageCode) {
        // TODO Missing i18n like thing
        return "The service call job";
    }

    public String getDocumentation(String twoLettersLanguageCode) {
        return "CallJob Service\n"
                + "\n"
                + "Parameters\n"
                + "	url [mandatory]: the webservice port URL to the TIS Server"
                + "\n"
                + "		or the local talend job URL: ltj://<jobName>/<jobVersion>/[jobMainClass]"
                + "\n"
                + "	contextParam   : the contextParam of the tis job"
                + "\n"
                + "		name: the name of the context param"
                + "\n"
                + "		value: the value of context param, the value will be viewed as a priple"
                + "\n"
                + "              variable if the value is embraced with a brace, its content will be like: "
                + "\n"
                + "              <exchange><report>{update report here}</report><item>{item pointed to by Update/Key}</item></exchange>\n"
                + "	username [optional]: the username to use for the call" + "\n"
                + "	password [optional]: the password to  use for the call" + "\n"
                + "	contentType [optional]: the contentType of the returned data. Defaults to 'text/xml'" + "\n"
                + "	conceptMapping [optional]: Directly map the result of a TIS call to a MDM entity" + "\n"
                + "		concept: the name of the concept" + "\n" + "		fields: mapping rule with json format" + "\n" + "\n"
                + "Example1" + "\n" + "	<configuration>" + "\n" + "		<url>http://server:port/TISService/TISPort</url>" + "\n"
                + "		<contextParam>" + "\n" + "			<name>firstname</name>" + "\n" + "			<value>jack</value>" + "\n"
                + "		</contextParam>" + "\n" + "		<contextParam>" + "\n" + "			<name>lastname</name>" + "\n"
                + "			<value>jones</value>" + "\n" + "		</contextParam>" + "\n" + "		<contextParam>" + "\n"
                + "			<name>xmlInput</name>" + "\n" + "			<value>{}</value>" + "\n" + "		</contextParam>" + "\n"
                + "		<username>john</username>" + "\n" + "		<password>doe</password>" + "\n" + "		<conceptMapping>" + "\n"
                + "			<concept>User</concept>" + "\n" + "			<fields>" + "\n" + "			  {" + "\n" + "			  p1:firstname," + "\n"
                + "			  p2:lastname" + "\n" + "			  }" + "\n" + "			</fields>" + "\n" + "		</conceptMapping>" + "\n"
                + "	</configuration>" + "\n" + "Example2" + "\n" + "	<configuration>" + "\n"
                + "		<url>ltj://tiscall_multi_return/0.1</url>" + "\n" + "		<contextParam>" + "\n" + "			<name>nb_line</name>"
                + "\n" + "			<value>5</value>" + "\n" + "		</contextParam>" + "\n" + "	</configuration>" + "\n" + "\n";
    }

    public String getStatus() {
        return "OK"; //$NON-NLS-1$
    }

    public String getConfiguration(String optionalParameters) throws XtentisException {
        return getDefaultConfiguration();
    }

    @Override
    public String getDefaultConfiguration() {
        return null;
    }

    public String receiveFromInbound(ItemPOJOPK itemPK, String routingOrderID, String compiledParameters) throws XtentisException {
        JobInvokeConfig jobInvokeConfig = null;
        CompiledParameters parameters;
        try {
            parameters = CompiledParameters.deserialize(compiledParameters);
        } catch (Exception e) {
            LOGGER.error("Could not read parameters", e);
            throw new XtentisException(e);
        }
        try {
            // set the parameters
            URI uri = URI.create(parameters.getUrl());
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
            } else if (HTTP_PROTOCOL.equalsIgnoreCase(protocol)) {
                throw new NotImplementedException(); // TODO Too much dependency on JBoss class (to rewrite)
            } else {
                throw new IllegalArgumentException("Protocol '" + protocol + "' is not supported.");
            }
            // the text should be a map(key=value)
            String exchangeXML = StringUtils.EMPTY;
            Properties p = new Properties();
            if (parameters.getTisContext() != null) {
                for (ContextParam kv : parameters.getTisContext()) {
                    String value = kv.getValue();
                    if (kv.isItemXML()) {
                        value = createExchangeXML(itemPK);
                        // TMDM-2633 Always include exchange XML message in parameters (save computed value for later)
                        exchangeXML = value;
                    }
                    p.setProperty(kv.getName(), value);
                }
            }
            Map<String, String> argsMap = new HashMap<String, String>();
            for (Object o : p.keySet()) {
                String key = (String) o;
                String value = p.getProperty(key);
                argsMap.put(key, value);
            }
            // TMDM-2633: Always include exchange XML message in parameters
            if (exchangeXML.isEmpty()) { // (don't compute it twice)
                exchangeXML = createExchangeXML(itemPK);
            }
            argsMap.put(MDMJobInvoker.EXCHANGE_XML_PARAMETER, exchangeXML);
            JobContainer.getUniqueInstance().getJobInvoker(jobName, jobVersion).call(argsMap);
            return "callJob Service successfully executed!'";
        } catch (XtentisException xe) {
            throw xe;
        } catch (Exception e) {
            String err;
            if (jobInvokeConfig != null) {
                err = "Could not execute callJob service for job " + jobInvokeConfig.getJobName() + " in version " + jobInvokeConfig.getJobVersion(); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                err = "Could not execute callJob service for service " + parameters.getUrl(); //$NON-NLS-1$
            }
            LOGGER.error(err, e);
            throw new XtentisException(e);
        }
    }

    public Serializable receiveFromOutbound(HashMap<String, Serializable> map) throws XtentisException {
        return null;
    }

    public void start() throws XtentisException {
    }

    public void stop() throws XtentisException {
    }
}
