/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.resource;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.talend.mdm.ext.publish.model.PicturePojo;
import org.talend.mdm.ext.publish.util.DAOFactory;
import org.talend.mdm.ext.publish.util.PicturesDAO;

import talend.ext.images.server.ImageServerInfo;
import talend.ext.images.server.util.ImagePathUtil;

/**
 * Resource which has only one representation.
 * 
 */
public class PicturesResource extends BaseResource {

    private static final Logger LOGGER = Logger.getLogger(PicturesResource.class);

    private static final String picturesLocation = ImageServerInfo.getInstance().getUploadPath();

    private PicturesDAO picturesDAO = null;

    private List<PicturePojo> picturePojos = null;

    public PicturesResource(Context context, Request request, Response response) {

        super(context, request, response);

        picturesDAO = DAOFactory.getUniqueInstance().getPicturesDAO(picturesLocation);

        // get resource
        picturePojos = new ArrayList<PicturePojo>();
        try {
            String[] pks = picturesDAO.getAllPKs();
            if (pks != null && pks.length > 0) {
                for (String pk : pks) {
                    String[] parsedPK = parsePK(pk);
                    String fileName = parsedPK[1];
                    String catalog = parsedPK[0];
                    String uri = parsedPK[2];

                    picturePojos.add(new PicturePojo(pk, fileName, catalog, uri));
                }
            }
        } catch (Exception e1) {
            LOGGER.error(e1.getMessage(), e1);
        }

    }

    private String[] parsePK(String pk) {
        if (pk == null) {
            throw new IllegalArgumentException();
        }
        //PK of the form <catalog>-filename
        int index = pk.indexOf("-"); //$NON-NLS-1$
        if (index == -1) { 
            pk = "-" + pk; //$NON-NLS-1$
        }

        String catalog = pk.substring(0, index);
        String fileName = pk.substring(index + 1, pk.length());
        String uri = ImageServerInfo.getInstance().getLocateBaseUrl();
        if (!catalog.equals("") && !catalog.equals("/") && !catalog.equals("//")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            uri = uri + ImagePathUtil.encodeURL("/" + catalog); //$NON-NLS-1$
        }
        uri = uri + ImagePathUtil.encodeURL("/" + fileName); //$NON-NLS-1$

        String[] parsedPK = new String[3];
        parsedPK[0] = catalog;
        parsedPK[1] = fileName;
        parsedPK[2] = uri;
        return parsedPK;
    }

    @Override
    protected Representation getResourceRepresent(Variant variant) throws ResourceException {

        // Generate the right representation according to its media type.
        if (MediaType.TEXT_XML.equals(variant.getMediaType())) {
            return generateListRepresentation4Pictures(picturePojos);
        }
        return null;
    }

}
