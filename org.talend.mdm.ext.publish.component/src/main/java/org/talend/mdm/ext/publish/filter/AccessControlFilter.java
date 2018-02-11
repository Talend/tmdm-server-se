/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.talend.mdm.ext.publish.ResourceType;
import org.talend.mdm.ext.publish.ServerServletApplication;

public class AccessControlFilter extends Filter {

    private static Logger log = Logger.getLogger(AccessControlFilter.class);

    protected int beforeHandle(Request request, Response response) {

        try {
            String inputPath = request.getResourceRef().getPath();
            Pattern pattern = Pattern.compile(ServerServletApplication.ROUTE_CONTEXT_PATH + "/(.*)");//$NON-NLS-1$
            Matcher matcher = pattern.matcher(inputPath);
            while (matcher.find())

            {
                List<String> parts = new ArrayList<String>();
                String part = matcher.group(1);
                if (part != null && part.length() > 0) {
                    String[] tmp = part.split("/");//$NON-NLS-1$
                    if (tmp != null && tmp.length > 0) {
                        parts = Arrays.asList(tmp);
                    }
                }

                if (parts.size() > 0) {
                    String resourceType = parts.get(0);
                    parts = parts.subList(1, parts.size());

                    AccessController accessController = null;
                    AccessControlPropertiesReader propertiesReader = AccessControlPropertiesReader.getInstance();
                    if (resourceType.equals(ResourceType.DATAMODELS.getName())) {
                        accessController = new DataModelsAccessControl();
                    } else if (resourceType.equals(ResourceType.DATAMODELSTYPES.getName())) {
                        accessController = new DataModelsTypesAccessControl();
                    } else {
                        // TODO support more types
                    }

                    if (accessController != null && !accessController.validate(parts, propertiesReader)) {
                        return Filter.STOP;
                    }
                }

            }
        } catch (Exception e) {
            log.error(e.getLocalizedMessage(), e);
        }
        return Filter.CONTINUE;

    }

}
