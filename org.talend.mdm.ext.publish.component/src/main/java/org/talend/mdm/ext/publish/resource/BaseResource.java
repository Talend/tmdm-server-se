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

import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.*;
import org.talend.mdm.ext.publish.model.PicturePojo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Base resource class that supports common behaviours or attributes shared by all resources.
 * 
 */
public abstract class BaseResource extends Resource {

    private static Logger log = Logger.getLogger(BaseResource.class);

    public BaseResource(Context context, Request request, Response response) {
        super(context, request, response);

        // This representation has only one type of representation.
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    /**
     * Returns a full representation for a given variant.
     */
    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation resourceRepresent = getResourceRepresent(variant);

        // set characterSet
        if (resourceRepresent != null)
            resourceRepresent.setCharacterSet(CharacterSet.UTF_8);
        return resourceRepresent;
    }

    protected abstract Representation getResourceRepresent(Variant variant) throws ResourceException;

    protected String getAttributeInUrl(String attributeKey) {
        return getAttributeInUrl(attributeKey, true);
    }

    protected String getAttributeInUrl(String attributeKey, boolean decode) {

        String attribute = null;
        Object getted = getRequest().getAttributes().get(attributeKey);
        if (getted != null) {
            attribute = (String) getted;
        }
        if (attribute != null && decode) {
            try {
                attribute = URLDecoder.decode(attribute, "UTF-8"); //$NON-NLS-1$
            } catch (UnsupportedEncodingException uee) {
                log.warn("Unable to decode the string with the UTF-8 character set.", uee); //$NON-NLS-1$
            }
        }

        return attribute;

    }

    /**
     * Generate an XML representation of an error response.
     * 
     * @param errorMessage the error message.
     * @param errorCode the error code.
     */
    protected void generateErrorRepresentation(String errorMessage, String errorCode, Response response) {
        generateErrorRepresentation(errorMessage, errorCode, response, Status.CLIENT_ERROR_NOT_FOUND);
    }

    protected void generateErrorRepresentation(String errorMessage, String errorCode, Response response, Status status) {
        // This is an error
        response.setStatus(status);
        // Generate the output representation
        try {
            DomRepresentation representation = new DomRepresentation(MediaType.TEXT_XML);
            // Generate a DOM document representing the list of
            Document d = representation.getDocument();

            Element eltError = d.createElement("error"); //$NON-NLS-1$
            d.appendChild(eltError);

            Element eltCode = d.createElement("code"); //$NON-NLS-1$
            eltCode.appendChild(d.createTextNode(errorCode));
            eltError.appendChild(eltCode);

            Element eltMessage = d.createElement("message"); //$NON-NLS-1$
            eltMessage.appendChild(d.createTextNode(errorMessage));
            eltError.appendChild(eltMessage);

            response.setEntity(representation);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    protected Representation generateListRepresentation(List<String> inputList) {

        DomRepresentation representation = null;

        try {
            representation = new DomRepresentation(MediaType.TEXT_XML);
            Document d = representation.getDocument();

            Element listElement = d.createElement("list"); //$NON-NLS-1$
            d.appendChild(listElement);

            if (inputList != null) {
                for (Iterator<String> iterator = inputList.iterator(); iterator.hasNext();) {
                    String entry = iterator.next();
                    if (entry != null) {
                        Element entryElement = d.createElement("entry"); //$NON-NLS-1$
                        entryElement.appendChild(d.createTextNode(entry));
                        listElement.appendChild(entryElement);
                    }
                }
            }

            d.normalize();

        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }

        return representation;
    }

    protected Representation generateListRepresentation4Pictures(List<PicturePojo> inputList) {

        DomRepresentation representation = null;

        try {
            representation = new DomRepresentation(MediaType.TEXT_XML);
            Document d = representation.getDocument();

            Element listElement = d.createElement("list"); //$NON-NLS-1$
            d.appendChild(listElement);

            if (inputList != null) {
                for (Iterator<PicturePojo> iterator = inputList.iterator(); iterator.hasNext();) {
                    PicturePojo entry = iterator.next();
                    if (entry != null) {

                        Element entryElement = d.createElement("entry"); //$NON-NLS-1$
                        listElement.appendChild(entryElement);

                        Element entryNameElement = d.createElement("name"); //$NON-NLS-1$
                        entryNameElement.appendChild(d.createTextNode(entry.getName()));
                        entryElement.appendChild(entryNameElement);

                        Element entryImageNameElement = d.createElement("imageName"); //$NON-NLS-1$
                        entryImageNameElement.appendChild(d.createTextNode(entry.getFileName()));
                        entryElement.appendChild(entryImageNameElement);

                        Element entryCategoryElement = d.createElement("catalog"); //$NON-NLS-1$
                        entryCategoryElement.appendChild(d.createTextNode(entry.getCatalog()));
                        entryElement.appendChild(entryCategoryElement);

                        Element entryUriElement = d.createElement("uri"); //$NON-NLS-1$
                        entryUriElement.appendChild(d.createTextNode(entry.getUri()));
                        entryElement.appendChild(entryUriElement);
                    }
                }
            }

            d.normalize();

        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }

        return representation;
    }

    protected Representation generateMapRepresentation(Map<String, String> inputMap) {

        DomRepresentation representation = null;

        try {
            representation = new DomRepresentation(MediaType.TEXT_XML);
            Document d = representation.getDocument();

            Element listElement = d.createElement("list"); //$NON-NLS-1$
            d.appendChild(listElement);

            if (inputMap != null) {

                for (Iterator<String> iterator = inputMap.keySet().iterator(); iterator.hasNext();) {
                    String entryName = iterator.next();

                    Element entryElement = d.createElement("entry"); //$NON-NLS-1$
                    listElement.appendChild(entryElement);

                    Element entryNameElement = d.createElement("name"); //$NON-NLS-1$
                    entryNameElement.appendChild(d.createTextNode(entryName));
                    entryElement.appendChild(entryNameElement);

                    Element entryUriElement = d.createElement("uri"); //$NON-NLS-1$
                    entryUriElement.appendChild(d.createTextNode(inputMap.get(entryName)));
                    entryElement.appendChild(entryUriElement);
                }
            }

            d.normalize();

        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }

        return representation;
    }

    /**
     * @param dataModelName
     * @return
     */
    protected DataModelPOJO getDataModel(String dataModelName) {
        DataModelPOJO dataModelPOJO = null;
        try {
            // use local bean without security check
            dataModelPOJO = Util.getDataModelCtrlLocal().getDataModel(new DataModelPOJOPK(dataModelName));
        } catch (XtentisException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return dataModelPOJO;
    }

}
