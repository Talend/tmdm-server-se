/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.resource;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

/**
 * Resource which has only one representation.
 * 
 */
public class DataModelsResource extends BaseResource {

    public DataModelsResource(Context context, Request request, Response response) {
        super(context, request, response);
    }

    @Override
    protected Representation getResourceRepresent(Variant variant) throws ResourceException {
        return null;
    }

}
