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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.talend.mdm.ext.publish.util.DAOFactory;
import org.talend.mdm.ext.publish.util.DomainObjectsDAO;

import com.amalto.core.util.XtentisException;

/**
 * Resource which has only one representation.
 * 
 */
public class CustomTypesSetsResource extends BaseResource {

    private static Logger log = Logger.getLogger(CustomTypesSetResource.class);

    DomainObjectsDAO domainObjectsDAO = null;

    List<String> namesList = null;

    public CustomTypesSetsResource(Context context, Request request, Response response) {

        super(context, request, response);
        // Allow modifications of this resource via POST requests.
        setModifiable(true);

        domainObjectsDAO = DAOFactory.getUniqueInstance().getDomainObjectDAO();

        // get resource
        namesList = new ArrayList<String>();
        try {
            String[] names = domainObjectsDAO.getAllPKs();
            if (names != null && names.length > 0)
                namesList = Arrays.asList(names);
        } catch (XtentisException e1) {
            log.error(e1.getLocalizedMessage(), e1);
        }

    }

    /**
     * Handle POST requests: create a new item.
     */
    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {

        Form form = new Form(entity);
        String domainObjectName = form.getFirstValue("domainObjectName");//$NON-NLS-1$
        String domainObjectContent = form.getFirstValue("domainObjectContent");//$NON-NLS-1$// TODO CHANGE TO FILE

        // Check that the domainObject is not already registered.
        if (namesList.contains(domainObjectName)) {
            generateErrorRepresentation("The Domain Object name " + domainObjectName + " already exists.", "1", getResponse());
        } else {
            // Register the new domainObject
            domainObjectsDAO.putResource(domainObjectName, domainObjectContent);

            // Set the response's status and entity
            getResponse().setStatus(Status.SUCCESS_CREATED);
            Representation rep = new StringRepresentation("Domain Object created", MediaType.TEXT_PLAIN);
            // Indicates where is located the new resource.
            rep.setIdentifier(getRequest().getResourceRef().getIdentifier() + "/" + domainObjectName);
            getResponse().setEntity(rep);
        }
    }

    @Override
    protected Representation getResourceRepresent(Variant variant) throws ResourceException {

        // Generate the right representation according to its media type.
        if (MediaType.TEXT_XML.equals(variant.getMediaType())) {
            return generateListRepresentation(namesList);
        }
        return null;
    }

}
