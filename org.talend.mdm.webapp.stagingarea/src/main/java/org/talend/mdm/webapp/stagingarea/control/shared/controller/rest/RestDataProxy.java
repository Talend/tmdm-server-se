/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.stagingarea.control.shared.controller.rest;

import com.extjs.gxt.ui.client.data.DataProxy;
import com.extjs.gxt.ui.client.data.DataReader;
import com.google.gwt.user.client.rpc.AsyncCallback;

public abstract class RestDataProxy<D> implements DataProxy<D> {

    public void load(final DataReader<D> reader, final Object loadConfig, final AsyncCallback<D> callback) {
        load(loadConfig, new AsyncCallback<D>() {

            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }

            @SuppressWarnings("unchecked")
            public void onSuccess(Object result) {
                try {
                    D data;
                    if (reader != null) {
                        data = reader.read(loadConfig, result);
                    } else {
                        data = (D) result;
                    }
                    callback.onSuccess(data);
                } catch (Exception e) {
                    callback.onFailure(e);
                }
            }
        });
    }

    protected abstract void load(Object loadConfig, AsyncCallback<D> callback);
}
