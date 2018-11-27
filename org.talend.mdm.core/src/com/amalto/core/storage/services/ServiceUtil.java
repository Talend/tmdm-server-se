/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.services;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLStreamException;

import org.apache.log4j.Logger;

import com.amalto.core.save.MultiRecordsSaveException;
import com.amalto.core.storage.exception.ConstraintViolationException;
import com.amalto.core.util.ValidateException;
import com.ctc.wstx.exc.WstxUnexpectedCharException;

public class ServiceUtil {

    private static final Logger LOGGER = Logger.getLogger(ServiceUtil.class);

    public static Response getErrorResponse(Throwable e, String message) {
        String responseMessage = message == null ? e.getMessage() : message;
        if (e instanceof ConstraintViolationException) {
            LOGGER.warn(responseMessage, e);
            return Response.status(Response.Status.FORBIDDEN).entity(responseMessage).build();
        } else if (e instanceof XMLStreamException || e instanceof IllegalArgumentException
                || e instanceof MultiRecordsSaveException
                || (e.getCause() != null && (e.getCause() instanceof IllegalArgumentException
                        || e.getCause() instanceof IllegalStateException || e.getCause() instanceof ValidateException
                        || e.getCause() instanceof WstxUnexpectedCharException))) {
            LOGGER.warn(responseMessage, e);
            return Response.status(Response.Status.BAD_REQUEST).entity(responseMessage).build();
        } else if (e instanceof NotFoundException) {
            LOGGER.error(responseMessage, e);
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            LOGGER.error(responseMessage, e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(responseMessage).build();
        }
    }
}