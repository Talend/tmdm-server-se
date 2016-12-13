/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.gwt;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.talend.mdm.webapp.base.server.AbstractService;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.impl.ServerSerializationStreamReader;

public class ProxyGWTServiceImpl extends AbstractService {

    private static final long serialVersionUID = -3043769203139170342L;

    private Properties properties;

    private Map<String, Object> actions = new HashMap<String, Object>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        loadActions(config.getServletContext());
    }

    private void loadActions(ServletContext servletContext) {
        InputStream in = null;
        try {
            in = servletContext.getResourceAsStream("/WEB-INF/actions.properties"); //$NON-NLS-1$
            properties = new Properties();
            properties.load(in);
        } catch (Exception e) {
            // Ignore this error
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Ignore this error
                }
            }
        }
    }

    private synchronized Object getAction(String serviceIntfName) {
        Object action = actions.get(serviceIntfName);
        if (action == null) {
            try {
                action = Class.forName(properties.getProperty(serviceIntfName), false,
                        Thread.currentThread().getContextClassLoader()).newInstance();
                actions.put(serviceIntfName, action);
            } catch (Exception e) {
                // Ignore this error
            }
        }
        return action;
    }

    private String getServiceIntfName(String encodedRequest) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ServerSerializationStreamReader streamReader = new ServerSerializationStreamReader(classLoader, this);
        try {
            streamReader.prepareToRead(encodedRequest);
            return streamReader.readString();
        } catch (SerializationException ex) {
            throw new IncompatibleRemoteServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    protected String doProcessCall(String payload) throws SerializationException {
        try {
            String serviceIntfName = getServiceIntfName(payload);
            Object action = getAction(serviceIntfName);
            if (action == null) {
                throw new IncompatibleRemoteServiceException(serviceIntfName + " undefined in actions.properties\nPlease check"); //$NON-NLS-1$
            }

            RPCRequest rpcRequest = RPC.decodeRequest(payload, action.getClass(), this);
            onAfterRequestDeserialized(rpcRequest);
            return RPC.invokeAndEncodeResponse(action, rpcRequest.getMethod(), rpcRequest.getParameters(),
                    rpcRequest.getSerializationPolicy(), rpcRequest.getFlags());
        } catch (IncompatibleRemoteServiceException ex) {
            log("An IncompatibleRemoteServiceException was thrown while processing this call.", ex); //$NON-NLS-1$
            return RPC.encodeResponseForFailure(null, ex);
        }
    }
}
