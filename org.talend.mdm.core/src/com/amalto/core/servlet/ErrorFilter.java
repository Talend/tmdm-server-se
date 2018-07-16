/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.servlet;

import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;

import java.io.IOException;

/**
 * A {@link Filter} implementation that prints out the full stack to the servlet response in case of {@link Throwable}.
 */
public class ErrorFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(ErrorFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
        // Nothing to do.
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Throwable e) {
            if (servletResponse instanceof HttpServletResponse) {
                HttpServletResponse httpServletResponse = ((HttpServletResponse) servletResponse);
                if (httpServletResponse.getStatus() != Response.Status.FORBIDDEN.getStatusCode()) {
                    // If response is an HTTP one, set it to 500 (Internal Error).
                    ((HttpServletResponse) servletResponse).setStatus(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());

                }
            }
            e.printStackTrace(servletResponse.getWriter());

            // Still print the error for the server log.
            LOG.error("Error during servlet invocation", e);
        }
    }

    public void destroy() {
        // Nothing to do.
    }
}
