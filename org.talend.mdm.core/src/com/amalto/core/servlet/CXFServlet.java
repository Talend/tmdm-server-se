/*
 * Copyright (C) 2006-2013 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.servlet;

import org.apache.cxf.common.classloader.ClassLoaderUtils;
import org.apache.cxf.jaxrs.servlet.CXFNonSpringJaxrsServlet;
import org.apache.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CXFServlet extends CXFNonSpringJaxrsServlet {

    private static final String SERVICE_CLASSES_PARAM = "jaxrs.serviceClasses"; //$NON-NLS-1$

    private static final Logger LOGGER = Logger.getLogger(CXFServlet.class);

    // Similar to CXFNonSpringJaxrsServlet but doesn't fail if a service class isn't available.
    @Override
    protected List<Class> getServiceClasses(ServletConfig servletConfig, boolean modelAvailable) throws ServletException {
        String serviceBeans = servletConfig.getInitParameter(SERVICE_CLASSES_PARAM);
        if (serviceBeans == null) {
            if (modelAvailable) {
                return Collections.emptyList();
            }
            throw new ServletException("At least one resource class should be specified");
        }
        String[] classNames = serviceBeans.split(" "); //$NON-NLS-1$
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Resources classes: " + Arrays.toString(classNames));
        }
        List<Class> resourceClasses = new ArrayList<Class>();
        for (String cName : classNames) {
            String theName = cName.trim();
            if (theName.length() != 0) {
                try {
                    Class cls = ClassLoaderUtils.loadClass(cName, CXFServlet.class);
                    resourceClasses.add(cls);
                } catch (ClassNotFoundException e) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Service class '" + cName + "' is disabled because it isn't available.");
                    }
                }
            }
        }
        if (resourceClasses.isEmpty()) {
            LOGGER.warn("No resource class was found in current environment (enable DEBUG for more details).");
        }
        return resourceClasses;
    }
}
