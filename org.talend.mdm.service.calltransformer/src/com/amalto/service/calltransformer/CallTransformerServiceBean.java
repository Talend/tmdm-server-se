/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.service.calltransformer;

import com.amalto.core.objects.ItemPOJO;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.Service;
import com.amalto.core.objects.routing.AbstractRoutingOrderV2POJO;
import com.amalto.core.objects.transformers.TransformerV2POJOPK;
import com.amalto.core.objects.transformers.util.TransformerContext;
import com.amalto.core.objects.transformers.util.TransformerGlobalContext;
import com.amalto.core.objects.transformers.util.TypedContent;
import com.amalto.core.server.api.Item;
import com.amalto.core.server.api.Transformer;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import sun.misc.BASE64Decoder;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@org.springframework.stereotype.Service("amalto/local/service/callprocess")
public class CallTransformerServiceBean extends Service {

    public static final Logger         LOGGER                 = Logger.getLogger(CallTransformerServiceBean.class);

    private static final String        Param_Transformer_Name = "process"; //$NON-NLS-1$

    private AbstractRoutingOrderV2POJO routingOrderPOJO       = null;

    public String getDescription(String twoLetterLanguageCode) {
        if ("fr".matches(twoLetterLanguageCode.toLowerCase())) {
            return "Service qui appelle des processus";
        }
        return "The service call process";
    }

    public String getDocumentation(String twoLettersLanguageCode) {
        return "This service takes a single parameter: \n" + "process: the name of the process. \n\n"
                + "The process should expect to receive the content of the Item sent to the process in the DEFAULT variable \n"
                + "with a content-type of text/xml. \n\n" + "Example: " + Param_Transformer_Name + "=tiscall_test";
    }

    public String getStatus() {
        return "OK";
    }

    public void start() {
    }

    public void stop() {
    }

    public Serializable receiveFromOutbound(HashMap<String, Serializable> map) throws XtentisException {
        throw new XtentisException("The Call Transformer service is not meant to interact with adapters");
    }

    public String receiveFromInbound(ItemPOJOPK itemPK, String routingOrderID, String parameters)
            throws com.amalto.core.util.XtentisException {
        try {
            String transformer = null;
            if (parameters != null) {
                String kvs[] = parameters.split("&");
                for (String kv1 : kvs) {
                    String[] kv = kv1.split("=");
                    String key = kv[0].trim().toLowerCase();
                    if ((Param_Transformer_Name.equals(key)) && (kv.length == 2)) {
                        transformer = kv[1].trim();
                    }
                }
                if (transformer == null || "".equals(transformer)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Service CallTransformer - mandatory parameter transformer name is missing");
                    }
                    throw new XtentisException("Service CallTransformer - mandatory parameter transformer name is missing");
                }
                Transformer tctrl = Util.getTransformerV2CtrlLocal();
                if (tctrl.existsTransformer(new TransformerV2POJOPK(transformer)) == null) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Service CallTransformer is unable to call transformer " + transformer
                                + " - transformer doesn't exist");
                    }
                    throw new XtentisException("Unable to find the transformer " + transformer);
                }
                Item ictrl = Util.getItemCtrl2Local();
                ItemPOJO pojo = ictrl.getItem(itemPK);
                TransformerContext context = new TransformerContext(new TransformerV2POJOPK(transformer));
                context.putInPipeline(Transformer.DEFAULT_VARIABLE, new TypedContent(pojo.getProjectionAsString().getBytes(),
                        "text/xml"));
                AbstractRoutingOrderV2POJO routingOrder = getRoutingOrderPOJO();
                String userToken = null;
                if (routingOrder != null) {
                    try {
                        userToken = new String((new BASE64Decoder()).decodeBuffer(routingOrder.getBindingUserToken()), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        LOGGER.error("Unable to read user token.", e);
                    }
                }
                TransformerGlobalContext globalContext = new TransformerGlobalContext(context);
                globalContext.setUserToken(userToken);
                tctrl.executeUntilDone(globalContext);
            }
            return "CallTransformer Service successfully executed transformer '" + transformer + "'";
        } catch (Exception e) {
            String err = (new SimpleDateFormat("yyyyMMdd'T'HH:mm:ss, SSS")).format(new Date(System.currentTimeMillis()))
                    + ": ERROR routing to Call Transformer Service " + ": " + e.getLocalizedMessage();
            if (e instanceof XtentisException) {
                throw new XtentisException(e);
            } else {
                LOGGER.error(err + " (" + e.getClass().getName() + ")", e);
                throw new XtentisException(e);
            }
        }

    }

    public String getDefaultConfiguration() {
        return "<configuration/>";
    }

    public String getConfiguration(String optionalParameters) throws XtentisException {
        try {
            String configuration = loadConfiguration();
            if (configuration == null) {
                configuration = getDefaultConfiguration();
            }
            return configuration;
        } catch (Exception e) {
            String err = "Unable to deserialize the configuration of the Call Transformer Service" + ": "
                    + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err);
        }
    }

    public void putConfiguration(String configuration) {
        LOGGER.debug("putConfiguration() " + configuration);
    }

    public Serializable fetchFromOutbound(String command, String parameters, String schedulePlanID) throws XtentisException {
        try {
            // parse input parameter
            if (parameters == null || parameters.length() == 0) {
                throw new XtentisException("Parameters can not be empty! ");
            }
            Document paramDoc = Util.parse(parameters);
            String transformerName = Util.getFirstTextNode(paramDoc, "//" + Param_Transformer_Name);
            String typedContentType = Util.getFirstTextNode(paramDoc, "//typedContent/type");
            String typedContentValue = Util.getFirstTextNode(paramDoc, "//typedContent/value");
            typedContentValue = StringEscapeUtils.unescapeXml(typedContentValue);
            // execute main process
            if (transformerName == null || "".equals(transformerName)) {
                LOGGER.debug("Service CallTransformer - mandatory parameter transformer name is missing");
                throw new XtentisException("Service CallTransformer - mandatory parameter transformer name is missing");
            }
            Transformer tctrl = Util.getTransformerV2CtrlLocal();
            if (tctrl.existsTransformer(new TransformerV2POJOPK(transformerName)) == null) {
                LOGGER.debug("Service CallTransformer is unable to call transformer " + transformerName
                        + " - transformer doesn't exist");
                throw new XtentisException("Unable to find the transformer " + transformerName);
            }
            TransformerContext context = new TransformerContext(new TransformerV2POJOPK(transformerName));
            context.putInPipeline(Transformer.DEFAULT_VARIABLE, new TypedContent(typedContentValue.getBytes(), typedContentType));
            tctrl.executeUntilDone(context);
            return "OK";
        } catch (Exception e) {
            String err = "Unable to fetchFromOutbound of the Call Transformer Service" + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    public AbstractRoutingOrderV2POJO getRoutingOrderPOJO() {
        return routingOrderPOJO;
    }

}