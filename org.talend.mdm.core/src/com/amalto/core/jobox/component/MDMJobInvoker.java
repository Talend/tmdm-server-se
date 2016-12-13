/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.component;

import com.amalto.core.jobox.util.JoboxException;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Implementation of {@link JobInvoker} for jobs that implements 'routines.system.api.TalendMDMJob' (i.e. Jobs that
 * contain a tMDMTriggerInput or tMDMTriggerOutput  component).
 */
public class MDMJobInvoker extends JobInvoker {

    public static final String EXCHANGE_XML_PARAMETER = "__talend__exchangeXML__"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(MDMJobInvoker.class);

    private static final String SET_MDM_INPUT_MESSAGE_METHOD = "setMDMInputMessage"; //$NON-NLS-1$

    private static final String GET_MDM_OUTPUT_MESSAGE_METHOD = "getMDMOutputMessage"; //$NON-NLS-1$

    private static final String GET_ROOT_ELEMENT_METHOD = "getRootElement"; //$NON-NLS-1$

    private static final String AS_XML_METHOD = "asXML"; //$NON-NLS-1$

    public MDMJobInvoker(String jobName, String version) {
        super(jobName, version);
    }

    @Override
    protected String[][] getReturn(Method runJobMethod, Object jobInstance, Object parameter) throws JoboxException {
        try {
            Object invokeResult = runJobMethod.invoke(jobInstance, (Object) parameter);

            Method getMDMOutputMethod = jobInstance.getClass().getMethod(GET_MDM_OUTPUT_MESSAGE_METHOD);
            Object o = getMDMOutputMethod.invoke(jobInstance);
            if (o == null) {
                // If job didn't return anything for getMDMOutputMessage, returns job invocation result.
                return (String[][]) invokeResult;
            } else {
                // Be careful here: return object is a instance of Document but can't use it because of class cast
                // issue (caller has different class loader than executor), so only use classes from the bootstrap class
                // loader.
                Class documentClass = o.getClass();
                // TMDM-nnnn: Call asXML on the root element of the document so XML processing content is skipped.
                Method getRootElementMethod = documentClass.getMethod(GET_ROOT_ELEMENT_METHOD);
                Object rootElement = getRootElementMethod.invoke(o);
                Class nodeClass = rootElement.getClass();
                String[] resultAsString = {nodeClass.getMethod(AS_XML_METHOD).invoke(rootElement).toString()};
                return new String[][]{resultAsString};
            }
        } catch (Exception e) {
            throw new JoboxException(e);
        }
    }

    @Override
    protected void prepareJobInstance(Object jobInstance, Map<String, String> parameters) throws JoboxException {
        try {
            Method setMDMInputMethod = jobInstance.getClass().getMethod(SET_MDM_INPUT_MESSAGE_METHOD, String.class);
            if (parameters.containsKey(EXCHANGE_XML_PARAMETER)) {
                setMDMInputMethod.invoke(jobInstance, parameters.get(EXCHANGE_XML_PARAMETER));
            } else {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exchange parameter not found. No value will be passed to " + SET_MDM_INPUT_MESSAGE_METHOD + "() method."); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        } catch (Exception e) {
            throw new JoboxException(e);
        }
    }
}
