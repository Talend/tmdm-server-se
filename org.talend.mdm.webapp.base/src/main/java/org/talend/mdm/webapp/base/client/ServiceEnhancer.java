/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client;

import org.talend.mdm.webapp.base.client.util.UrlUtil;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.user.client.rpc.RpcRequestBuilder;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class ServiceEnhancer {

    private static final String LANGUAGE_HEADER = "X-MDM-Language"; //$NON-NLS-1$

    public static void customizeService(ServiceDefTarget service) {

        service.setRpcRequestBuilder(new RpcRequestBuilder() {

            @Override
            protected void doFinish(RequestBuilder rb) {
                super.doFinish(rb);
                Log.info(UrlUtil.getLanguage());
                rb.setHeader(LANGUAGE_HEADER, UrlUtil.getLanguage());
            }
        });
    }
}
