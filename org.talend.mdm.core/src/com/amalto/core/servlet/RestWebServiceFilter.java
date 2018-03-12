/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.servlet;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RestWebServiceFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // do nothing
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        
        //positionning http response cache control to no
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        response.setHeader("Cache-Control", "no-cache"); //$NON-NLS-1$//$NON-NLS-2$
        response.setDateHeader("Expires", 0); //$NON-NLS-1$
        response.setHeader("Pragma", "No-cache"); //$NON-NLS-1$ //$NON-NLS-2$

        //continue to next filter
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
