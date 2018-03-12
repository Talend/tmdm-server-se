/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.resource;

import java.io.File;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import com.amalto.core.util.Util;

/**
 * Resource which has only one representation.
 * 
 */
public class BarFileResource extends BaseResource {

    private String barFileHome;

    private String barFileName;

    public BarFileResource(Context context, Request request, Response response) {
        super(context, request, response);

        // TODO:get from core
        this.barFileHome = Util.getBarHomeDir();

        this.barFileName = getAttributeInUrl("barFileName").replace("$$", ".") + ".bar"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

    }

    @Override
    protected Representation getResourceRepresent(Variant variant) throws ResourceException {

        String filePath = barFileHome + File.separator + barFileName;
        if (new File(filePath).exists()) {
            // Generate the right representation according to its media type.
            FileRepresentation representation = null;
            representation = new FileRepresentation(filePath, MediaType.APPLICATION_OCTET_STREAM);
            return representation;
        } else {
            return null;
        }

    }

}
