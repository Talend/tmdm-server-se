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

package com.amalto.core.storage.task.staging;

import java.io.StringReader;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.amalto.core.storage.task.ConfigurableFilter;
import com.amalto.core.storage.task.DefaultFilter;
import com.amalto.core.storage.task.Filter;
import com.amalto.core.util.Util;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path(StagingTaskService.TASKS)
@Api(value = "Staging area management")
public class StagingTaskService {

    private static final Logger LOGGER = Logger.getLogger(StagingTaskService.class);
    
    public static final String TASKS = "/tasks/staging"; //$NON-NLS-1$

    private final StagingTaskServiceDelegate delegate = new DefaultStagingTaskService();

    @GET
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @ApiOperation(value="Provides staging area statistics for the user's current container and model", response=StagingContainerSummary.class)
    public StagingContainerSummary getContainerSummary(@Context final HttpServletResponse response) {
        StagingContainerSummary result = delegate.getContainerSummary();
        if(result == null){
            response.setStatus(HttpStatus.NO_CONTENT.value());
            return null;
        }
        return result;
    }

    @POST
    @Path("/")
    @ApiOperation(value="Starts a new validation task and returns the validation task id for the user's current container and model")
    public String startValidation() {
        return delegate.startValidation();
    }

    @GET
    @Path("{container}/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    @ApiOperation(value="Provides staging area statistics for provided container and model", response=StagingContainerSummary.class)
    public StagingContainerSummary getContainerSummary(@ApiParam(value="Container name") @PathParam("container") String dataContainer,
                                                       @ApiParam(value="Model name") @QueryParam("model") String dataModel) {
        return delegate.getContainerSummary(dataContainer, dataModel);
    }

    @POST
    @Path("{container}/")
    @Consumes(MediaType.APPLICATION_XML)
    @ApiOperation(value="start a new validation on provided container and model and returns the validation task id")
    public String startValidation(@ApiParam(value="Container name") @PathParam("container") String dataContainer,
                                  @ApiParam(value="Model name") @QueryParam("model") String dataModel,
                                  @Context HttpServletRequest request) {
        Filter filter;
        try {
            DocumentBuilder builder = MDMXMLUtils.getDocumentBuilder().get();
            String content = IOUtils.toString(request.getInputStream());
            if (!content.isEmpty()) {
                Document doc = builder.parse(new InputSource(new StringReader(content)));
                filter = new ConfigurableFilter(doc);
            } else {
                filter = DefaultFilter.INSTANCE;
            }
        } catch (Exception e) {
            filter = DefaultFilter.INSTANCE;
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ignored parse error for staging filter: ", e);
            }
        }
        return delegate.startValidation(dataContainer, dataModel, filter);
    }

    @GET
    @Path("{container}/execs")
    @ApiOperation(value="Lists all completed validation tasks ids")
    public List<String> listCompletedTaskExecutions(@ApiParam(value="Container name") @PathParam("container") String dataContainer,
                                                    @ApiParam(value="Limit search to tasks started before this date (yyyy-MM-ddTHH:mm:ss)") @QueryParam("before") String beforeDate,
                                                    @ApiParam(value="Pagination start offset") @DefaultValue("1") @QueryParam("start") int start,
                                                    @ApiParam(value="Pagination size") @DefaultValue("-1") @QueryParam("size") int size) {
        return SerializableList.create(delegate.listCompletedExecutions(dataContainer, beforeDate, start, size), "executions", "execution");
    }

    @GET
    @Path("{container}/execs/count")
    @ApiOperation(value="Count all completed validation tasks ids")
    public int countCompletedTaskExecutions(@ApiParam(value="Container name") @PathParam("container") String dataContainer,
                                            @ApiParam(value="Limit search to tasks started before this date (yyyy-MM-ddTHH:mm:ss)")@QueryParam("before") String beforeDate) {
        return delegate.listCompletedExecutions(dataContainer, beforeDate, 1, -1).size();
    }

    @GET
    @Path("{container}/execs/current/")
    @ApiOperation(value="Returns statistics of the current validation execution for the provided container and model")
    public ExecutionStatistics getCurrentExecutionStats(@ApiParam(value="Container name") @PathParam("container") String dataContainer,
                                                        @ApiParam(value="Model name") @QueryParam("model") String dataModel) {
        return delegate.getCurrentExecutionStats(dataContainer, dataModel);
    }

    @DELETE
    @Path("{container}/execs/current/")
    @ApiOperation(value="Cancels the current validation execution for the provided container and model."
            + "If no current execution, as no effect.")
    public void cancelCurrentExecution(@ApiParam(value="Container name") @PathParam("container") String dataContainer,
                                       @ApiParam(value="Model name") @QueryParam("model") String dataModel) {
        delegate.cancelCurrentExecution(dataContainer, dataModel);
    }

    @GET
    @Path("{container}/execs/{executionId}/")
    @ApiOperation(value="Returns execution statistics for the validation task with the provided id.")
    public ExecutionStatistics getExecutionStats(@ApiParam(value="Container name") @PathParam("container") String dataContainer,
                                                 @ApiParam(value="Model name") @QueryParam("model") String dataModel,
                                                 @ApiParam(value="Execution id") @PathParam("executionId") String executionId) {
        return delegate.getExecutionStats(dataContainer, dataModel, executionId);
    }
    
    @GET
    @Path("{container}/hasStaging")
    @ApiOperation(value="Returns true of ths container has a staging area, false otherwise.")
    public String isSupportStaging(@ApiParam(value="Container name") @PathParam("container")
            String dataContainer) {
        try {
            return String.valueOf(Util.getXmlServerCtrlLocal().supportStaging(dataContainer));
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Could not confirm staging support.", e); //$NON-NLS-1$
            }
            throw new RuntimeException("Could not confirm staging support.", e); //$NON-NLS-1$
        }
    }
}
