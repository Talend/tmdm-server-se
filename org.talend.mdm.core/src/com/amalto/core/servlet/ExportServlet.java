/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.servlet;

import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.amalto.core.server.api.XmlServer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ExportServlet extends HttpServlet {

    private static final String PARAMETER_CLUSTER = "cluster";

    private static final String PARAMETER_START = "start";

    private static final String PARAMETER_END = "end";

    private static final Logger LOGGER = Logger.getLogger(ExportServlet.class);

    private static final String PARAMETER_INCLUDE_METADATA = "includeMetadata";

    public ExportServlet() {
        super();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse resp) throws ServletException {
        XmlServer server = Util.getXmlServerCtrlLocal();
        String clusterName = getParameter(request, PARAMETER_CLUSTER, true);
        int start = Integer.parseInt(getParameter(request, PARAMETER_START, true));
        int end = Integer.parseInt(getParameter(request, PARAMETER_END, true));
        boolean includeMetadata = Boolean.parseBoolean(getParameter(request, PARAMETER_INCLUDE_METADATA, false, "false"));  //$NON-NLS-1$

        ServletOutputStream outputStream;
        try {
            outputStream = resp.getOutputStream();
        } catch (IOException e) {
            throw new ServletException(e);
        }

        try {
            resp.setContentType("text/xml");
            server.exportDocuments(clusterName, start, end, includeMetadata, outputStream);
        } catch (XtentisException e) {
            throw new ServletException(e);
        } finally {
            try {
                outputStream.flush();
            } catch (IOException e) {
                LOGGER.error("Error during flush", e);
            }
        }
    }

    private static String getParameter(HttpServletRequest request, String parameter, boolean isMandatory) {
        return getParameter(request, parameter, isMandatory, StringUtils.EMPTY);
    }

    private static String getParameter(HttpServletRequest request, String parameter, boolean isMandatory, String defaultValue) {
        String value = request.getParameter(parameter);
        if (value == null && isMandatory) {
            throw new IllegalArgumentException("Parameter '" + parameter + "' is mandatory and not set.");
        }
        return value == null ? defaultValue : value;
    }
}

