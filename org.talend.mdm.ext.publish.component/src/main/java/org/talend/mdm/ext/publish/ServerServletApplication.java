/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.talend.mdm.ext.publish.filter.AccessControlFilter;
import org.talend.mdm.ext.publish.resource.BarFileResource;
import org.talend.mdm.ext.publish.resource.CustomTypesSetResource;
import org.talend.mdm.ext.publish.resource.CustomTypesSetsResource;
import org.talend.mdm.ext.publish.resource.DataModelResource;
import org.talend.mdm.ext.publish.resource.DataModelsResource;
import org.talend.mdm.ext.publish.resource.DataModelsTypesResource;
import org.talend.mdm.ext.publish.resource.PicturesResource;

public class ServerServletApplication extends Application {

    public static final String ROUTE_CONTEXT_PATH = "/services/pubcomponent"; ////$NON-NLS-1$
    public ServerServletApplication() {
        super();
    }

    public ServerServletApplication(Context context) {
        super(context);
    }

    @Override
    public Restlet createRoot() {

        // Create a router Restlet that routes each call to a
        // new instance of HelloWorldResource.
        Router router = new Router(getContext());

        // Defines a route for the resource "list of dataModels"
        router.attach(ROUTE_CONTEXT_PATH + '/' + ResourceType.DATAMODELS.getName(), DataModelsResource.class);
        // Defines a route for the resource "dataModel"
        router.attach("/" + ResourceType.DATAMODELS.getName() + "/{dataModelName}", DataModelResource.class); //$NON-NLS-1$ //$NON-NLS-2$

        router.attach("/" + ResourceType.DATAMODELSTYPES.getName() + "/{dataModelName}", DataModelsTypesResource.class);//$NON-NLS-1$ //$NON-NLS-2$

        router.attach("/" + ResourceType.CUSTOMTYPESSETS.getName(), CustomTypesSetsResource.class);//$NON-NLS-1$

        router.attach("/" + ResourceType.CUSTOMTYPESSETS.getName() + "/{customTypesSetName}", CustomTypesSetResource.class);//$NON-NLS-1$ //$NON-NLS-2$

        router.attach("/" + ResourceType.PICTURES.getName(), PicturesResource.class);//$NON-NLS-1$

        router.attach("/" + ResourceType.BARFILE.getName() + "/{barFileName}", BarFileResource.class); //$NON-NLS-1$ //$NON-NLS-2$

        // creates the filter and add it in front of the router
        AccessControlFilter accessControlFilter = new AccessControlFilter();
        accessControlFilter.setNext(router);

        return accessControlFilter;
    }

}
