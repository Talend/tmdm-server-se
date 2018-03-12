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

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.talend.mdm.ext.publish.util.DAOFactory;
import org.talend.mdm.ext.publish.util.DomainObjectsDAO;
import org.xml.sax.SAXException;

import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

public class CustomTypesSetResource extends BaseResource {

    private static Logger log = Logger.getLogger(CustomTypesSetResource.class);

    String customTypesSetName;

    DomainObjectsDAO domainObjectsDAO = null;

    public CustomTypesSetResource(Context context, Request request, Response response) {
        super(context, request, response);

        this.customTypesSetName = getAttributeInUrl("customTypesSetName");//$NON-NLS-1$
        this.domainObjectsDAO = DAOFactory.getUniqueInstance().getDomainObjectDAO();

    }

    @Override
    protected Representation getResourceRepresent(Variant variant) throws ResourceException {
        // Generate the right representation according to its media type.
        if (MediaType.TEXT_XML.equals(variant.getMediaType())) {
            DomRepresentation representation = null;
            try {
                representation = new DomRepresentation(MediaType.TEXT_XML, Util.parse(domainObjectsDAO
                        .getResource(customTypesSetName)));
                representation.getDocument().normalize();
            } catch (ParserConfigurationException e) {
                log.error(e.getLocalizedMessage(), e);
            } catch (IOException e) {
                log.error(e.getLocalizedMessage(), e);
            } catch (SAXException e) {
                log.error(e.getLocalizedMessage(), e);
            } catch (XtentisException e) {
                log.error(e.getLocalizedMessage(), e);
            }

            return representation;
        }
        return null;
    }

}
