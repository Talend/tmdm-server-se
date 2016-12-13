/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.services;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.SQLWrapper;
import com.amalto.core.storage.StorageType;
import com.amalto.core.util.XtentisException;
import com.amalto.xmlserver.interfaces.XmlServerException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/system/containers")
@Api(value="Containers management", tags="Administration")
public class SystemContainers {
    
    private static final Logger LOGGER = Logger.getLogger(SystemContainers.class);

    public SystemContainers() {
    }

    @POST
    @ApiOperation("Creates a new data container with the provided name")
    public void createContainer(@ApiParam("The new container name") @QueryParam("containerName") String containerName) {
        try {
            DataClusterPOJO dataClusterPOJO = new DataClusterPOJO(containerName);
            dataClusterPOJO.store();
        } catch (XtentisException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not store new data container.", e); //$NON-NLS-1$
            }
            throw new RuntimeException("Could not store new data container.", e); //$NON-NLS-1$
        }
    }
    
    @GET
    @ApiOperation("Get all data container names as array")
    public Response getAllContainers() {
        try{
            String[] output = new SQLWrapper().getAllClusters();
            return Response.ok(output).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (XmlServerException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not get all containers.", e); //$NON-NLS-1$
            }
            throw new RuntimeException("Could not get all containers.", e); //$NON-NLS-1$
        }
    }
    
    @GET
    @Path("{containerName}/hasStaging")
    @ApiOperation(value="Returns true of this container has a staging area, false otherwise.")
    public Response isSupportStaging(@ApiParam(value="Container name") @PathParam("containerName") String containerName) {
        boolean output = ServerContext.INSTANCE.get().getStorageAdmin().supportStaging(containerName);
        return Response.ok(output).type(MediaType.APPLICATION_JSON_TYPE).build();
    }
    
    @GET
    @Path("{containerName}/isExisting")
    @ApiOperation(value="Returns true of this container exits, false otherwise.")
    public Response isExistContainer(@ApiParam(value="Container name") @PathParam("containerName") String containerName) {
        try{
            boolean output = new SQLWrapper().existCluster(containerName);
            return Response.ok(output).type(MediaType.APPLICATION_JSON_TYPE).build();
        } catch (XmlServerException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not check existence of '"+ containerName +"'.", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            throw new RuntimeException("Could not check existence of '"+ containerName +"'.", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

}
