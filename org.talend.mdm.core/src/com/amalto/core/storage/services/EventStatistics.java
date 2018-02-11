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

import static com.amalto.core.query.user.UserQueryBuilder.*;

import java.io.StringWriter;
import java.util.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.amalto.core.query.user.UserQueryBuilder;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONWriter;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/system/stats/events")
@Api(value="Events statistics", tags="Statistics")
public class EventStatistics {

    private static final Logger LOGGER = Logger.getLogger(EventStatistics.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value="Lists number of executed triggers grouped by status (failed and completed) and name.")
    public Response getEventStatistics(
            @ApiParam(value="Limit result to triggers executed less than x seconds from now") @QueryParam("timeframe") Long timeFrame, 
            @ApiParam(value="Limit result to the first x entries") @QueryParam("top") Integer top) {
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage system = storageAdmin.get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM);
        // Build statistics
        StringWriter stringWriter = new StringWriter();
        JSONWriter writer = new JSONWriter(stringWriter);
        MetadataRepository repository = system.getMetadataRepository();
        try {
            system.begin();
            // Get defined triggers
            ComplexTypeMetadata triggerDefinition = repository.getComplexType("routing-rule-pOJO"); //$NON-NLS-1$
            FieldMetadata triggerName = triggerDefinition.getField("name"); //$NON-NLS-1$
            FieldMetadata triggerParameters = triggerDefinition.getField("parameters"); //$NON-NLS-1$
            UserQueryBuilder qb = from(triggerDefinition).select(triggerName).select(triggerParameters);
            StorageResults results = system.fetch(qb.getSelect());
            Map<String, String> triggerNameToParameter;
            try {
                triggerNameToParameter = new HashMap<String, String>(results.getCount());
                for (DataRecord result : results) {
                    String trigger = String.valueOf(result.get(triggerName));
                    String parameters = String.valueOf(result.get(triggerParameters));
                    triggerNameToParameter.put(trigger, parameters);
                }
            } finally {
                results.close();
            }
            // Get event count from failed and completed queues
            writer.object().key("events"); //$NON-NLS-1$
            {
                writer.array();
                {
                    // Failed events
                    ComplexTypeMetadata failedRoutingOrder = repository.getComplexType("failed-routing-order-v2-pOJO"); //$NON-NLS-1$
                    writeTo(system, triggerNameToParameter, failedRoutingOrder, writer, timeFrame, "failed", top); //$NON-NLS-1$
                    // Completed events
                    ComplexTypeMetadata completedRoutingOrder = repository.getComplexType("completed-routing-order-v2-pOJO"); //$NON-NLS-1$
                    writeTo(system, triggerNameToParameter, completedRoutingOrder, writer, timeFrame,  "completed", top); //$NON-NLS-1$
                }
                writer.endArray();
            }
            writer.endObject();
            system.commit();
            return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(stringWriter.toString())
                    .header("Access-Control-Allow-Origin", "*").build(); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (Exception e) {
            try {
                system.rollback();
            } catch (Exception rollbackException) {
                LOGGER.debug("Unable to rollback transaction.", e);
            }
            if (system.isClosed()) {
                // TMDM-7749: Ignore errors when storage is closed.
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception occurred due to closed storage.", e);
                }
            } else {
                // TMDM-7970: Ignore all storage related errors.
                LOGGER.warn("Unable to compute statistics.");
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Unable to compute statistics due to storage exception.", e);
                }
            }
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    private void writeTo(Storage system, Map<String, String> triggerNameToParameter, ComplexTypeMetadata routingOrderType,
                         JSONWriter writer, Long timeFrame, String categoryName, Integer top) throws JSONException {
        FieldMetadata parameters = routingOrderType.getField("service-parameters"); //$NON-NLS-1$
        writer.object().key(categoryName);
        {
            writer.array();
            {
                try {
                    // Build statistics
                    SortedSet<EventEntry> entries = new TreeSet<EventEntry>(new Comparator<EventEntry>() {

                        @Override
                        public int compare(EventEntry o1, EventEntry o2) {
                            int diff = (int) (o2.count - o1.count);
                            if (diff == 0) {
                                if (o1.eventName.equals(o2.eventName)) {
                                    return 0;
                                } else {
                                    return -1;
                                }
                            }
                            return diff;
                        }
                    });
                    for (Map.Entry<String, String> entry : triggerNameToParameter.entrySet()) {
                        String key = entry.getKey();
                        UserQueryBuilder qb = from(routingOrderType).select(count()).where(eq(parameters, entry.getValue()))
                                .limit(1).cache();
                        if (timeFrame != null && timeFrame > 0) {
                            qb.where(gte(routingOrderType.getField("time-last-run-completed"), //$NON-NLS-1$
                                    String.valueOf(System.currentTimeMillis() - (timeFrame * 1000))));
                        }
                        StorageResults results = system.fetch(qb.getSelect());
                        try {
                            EventEntry eventEntry = new EventEntry();
                            eventEntry.eventName = key;
                            eventEntry.count = (Long) results.iterator().next().get("count"); //$NON-NLS-1
                            entries.add(eventEntry);
                        } finally {
                            results.close();
                        }
                    }
                    // Returns all events or the n top one (query parameter).
                    Iterator<EventEntry> iterator = entries.iterator();
                    int limit = top == null || top <= 0 ? Integer.MAX_VALUE : top;
                    for (int i = 0; i < limit && iterator.hasNext(); i++) {
                        EventEntry entry = iterator.next();
                        writer.object().key(entry.eventName).value(entry.count).endObject();
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Could not build event statistics for '" + categoryName + "' events", e);
                }
            }
            writer.endArray();
        }
        writer.endObject();
    }

    // Object to store type statistics before building JSON output
    class EventEntry {

        String eventName;

        long count;
    }
}
