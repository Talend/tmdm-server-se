/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.rest;

import org.restlet.client.Response;
import org.restlet.client.data.MediaType;
import org.restlet.client.data.Method;
import org.restlet.client.ext.json.JsonRepresentation;
import org.talend.mdm.webapp.base.client.rest.ClientResourceWrapper;
import org.talend.mdm.webapp.base.client.rest.ResourceCallbackHandler;

import com.google.gwt.json.client.JSONParser;

public class ClientResourceMockWrapper extends ClientResourceWrapper {

    private ResourceCallbackHandler callbackHandler;

    public ClientResourceMockWrapper() {
        super();
    }

    @Override
    public void setCallback(ResourceCallbackHandler callbackHandler) {
        this.callbackHandler = callbackHandler;
    }

    @Override
    public void request(MediaType mediaType) {
        request();
    }

    @Override
    public void request() {

        Response response = new Response(null);
        if (method.equals(Method.GET) && uri.matches("^.+/services/rest/tasks/matching/explain/Product/groups/\\?.+$")) { //$NON-NLS-1$
            String jsonString = "{\"groups\":[{\"group\":[{\"result\":[{\"id\":\"10aacd8a-5600-4ae8-afa3-2cd3c870ed7d\"},{\"confidence\":1},{\"related_ids\":[\"Test1\"]},{\"values\":[{\"value\":[{\"field\":\"Name\"},{\"value\":\"1\"}]},{\"value\":[{\"field\":\"Description\"},{\"value\":\"1\"}]}]}]},{\"details\":[{\"detail\":[{\"id\":\"Test1\"},{\"match\":[{\"is_match\":true},{\"score\":1},{\"field_scores\":[{\"field_score\":[{\"pair_id\":\"10aacd8a-5600-4ae8-afa3-2cd3c870ed7d\"},{\"field\":\"Name\"},{\"fieldValue\":\"1\"},{\"value\":1},{\"algorithm\":\"Exact\"},{\"threshold\":1}]},{\"field_score\":[{\"pair_id\":\"10aacd8a-5600-4ae8-afa3-2cd3c870ed7d\"},{\"field\":\"Description\"},{\"fieldValue\":\"1\"},{\"value\":1},{\"algorithm\":\"Exact\"},{\"threshold\":1}]}]}]},{\"values\":[{\"value\":[{\"field\":\"Name\"},{\"value\":\"1\"}]},{\"value\":[{\"field\":\"Description\"},{\"value\":\"1\"}]}]}]}]}]}]}"; //$NON-NLS-1$
            JsonRepresentation representation = new JsonRepresentation(MediaType.TEXT_XML, JSONParser.parse(jsonString));
            response.setEntity(representation);
        } else if (method.equals(Method.POST)
                && uri.matches("^.+/services/rest/tasks/matching/explain/Product/records/\\?.+$")) { //$NON-NLS-1$
            String jsonString = "{\"groups\":[{\"group\":[{\"result\":[{\"id\":\"10aacd8a-5600-4ae8-afa3-2cd3c870ed7d\"},{\"confidence\":1},{\"related_ids\":[\"Test1\"]},{\"values\":[{\"value\":[{\"field\":\"Name\"},{\"value\":\"1\"}]},{\"value\":[{\"field\":\"Description\"},{\"value\":\"1\"}]}]}]},{\"details\":[{\"detail\":[{\"id\":\"Test1\"},{\"match\":[{\"is_match\":true},{\"score\":1},{\"field_scores\":[{\"field_score\":[{\"pair_id\":\"10aacd8a-5600-4ae8-afa3-2cd3c870ed7d\"},{\"field\":\"Name\"},{\"fieldValue\":\"1\"},{\"value\":1},{\"algorithm\":\"Exact\"},{\"threshold\":1}]},{\"field_score\":[{\"pair_id\":\"10aacd8a-5600-4ae8-afa3-2cd3c870ed7d\"},{\"field\":\"Description\"},{\"fieldValue\":\"1\"},{\"value\":1},{\"algorithm\":\"Exact\"},{\"threshold\":1}]}]}]},{\"values\":[{\"value\":[{\"field\":\"Name\"},{\"value\":\"1\"}]},{\"value\":[{\"field\":\"Description\"},{\"value\":\"1\"}]}]}]}]}]}]}"; //$NON-NLS-1$
            JsonRepresentation representation = new JsonRepresentation(MediaType.TEXT_XML, JSONParser.parse(jsonString));
            response.setEntity(representation);
        }
        this.callbackHandler.process(null, response);
    }
}
