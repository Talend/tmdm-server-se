/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.exception;

import javax.ws.rs.core.Response;

public class ViewNotFoundException extends RuntimeException {

    private static final long serialVersionUID = -4638437716474249157L;

    private Response.Status status;

    public ViewNotFoundException(Response.Status status, String message) throws IllegalArgumentException {
        super(message);
        this.status = status;
    }

    public ViewNotFoundException(Response.Status status) throws IllegalArgumentException {
        this.status = status;
    }

    public Response.Status getStatus() {
        return status;
    }

    public void setStatus(Response.Status status) {
        this.status = status;
    }
}
