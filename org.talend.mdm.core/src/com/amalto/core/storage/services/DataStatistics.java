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

import static com.amalto.core.query.user.UserQueryBuilder.alias;
import static com.amalto.core.query.user.UserQueryBuilder.count;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONWriter;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.query.user.Expression;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.record.DataRecord;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/system/stats/data")
@Api(value="Data statistics", tags="Statistics")
public class DataStatistics {

    private static final Logger LOGGER = Logger.getLogger(DataStatistics.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{container}")
    @ApiOperation(value="Provides statistics about data contained in the provided container.")
    public Response getDataStatistics(
            @ApiParam(value="Container name") @PathParam("container") String containerName, 
            @ApiParam(value="Optional language to get translated types names") @QueryParam("lang") String language,
            @ApiParam(value="Limit result to the first x data types") @QueryParam("top") Integer top) { 
        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage dataStorage = storageAdmin.get(containerName, StorageType.MASTER);
        if (dataStorage == null) {
            throw new IllegalArgumentException("Container '" + containerName + "' does not exist.");
        }
        // Build statistics
        SortedSet<TypeEntry> entries = new TreeSet<TypeEntry>(new Comparator<TypeEntry>() {

            @Override
            public int compare(TypeEntry o1, TypeEntry o2) {
                int diff = (int) (o2.count - o1.count);
                if (diff == 0) {
                    if (o1.typeName.equals(o2.typeName)) {
                        return 0;
                    } else {
                        return -1;
                    }
                }
                return diff;
            }
        });
        // Fill type counts (order by count)
        long totalCount = 0; // Need total count for percentage compute
        try {
            MetadataRepository repository = dataStorage.getMetadataRepository();
            dataStorage.begin();
            for (ComplexTypeMetadata type : repository.getUserComplexTypes()) {
                TypeEntry entry = new TypeEntry();
                Expression count = from(type).select(alias(count(), "count")).limit(1).cache().getExpression(); //$NON-NLS-1$
                StorageResults typeCount = dataStorage.fetch(count);
                long countValue = 0;
                for (DataRecord record : typeCount) {
                    countValue = (Long) record.get("count"); //$NON-NLS-1$
                }
                // Starts stats for type
                String name;
                if (language != null) {
                    name = type.getName(new Locale(language));
                } else {
                    name = type.getName();
                }
                entry.typeName = name;
                entry.count = countValue;
                totalCount += countValue;
                entries.add(entry);
            }
            dataStorage.commit();
        } catch (Exception e) {
            try {
                dataStorage.rollback();
            } catch (Exception rollbackException) {
                LOGGER.debug("Unable to rollback transaction.", e);
            }
            if (dataStorage.isClosed()) {
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
        // Write results
        try {
            if (top == null || top <= 0) {
                top = Integer.MAX_VALUE; // no top parameter or top <= 0 means 'all' types.
            }
            StringWriter stringWriter = new StringWriter();
            DecimalFormat percentageFormat = new DecimalFormat("##.##", DecimalFormatSymbols.getInstance(Locale.ENGLISH)); //$NON-NLS-1$
            JSONWriter writer = new JSONWriter(stringWriter);

            writer.object().key("data"); //$NON-NLS-1$
            {
                writer.array();
                {
                    Iterator<TypeEntry> iterator = entries.iterator();
                    for (int i = 0; i < top && iterator.hasNext(); i++) {
                        TypeEntry entry = iterator.next();
                        writer.object().key(entry.typeName);
                        {
                            writer.array();
                            {
                                writer.object().key("count").value(entry.count).endObject(); //$NON-NLS-1$
                                double percentage = totalCount > 0 ? (entry.count * 100) / totalCount : 0;
                                writer.object().key("percentage").value(percentageFormat.format(percentage)).endObject(); //$NON-NLS-1$
                            }
                            writer.endArray();
                        }
                        writer.endObject();
                    }
                }
                writer.endArray();
            }
            writer.endObject();
            return Response.ok().type(MediaType.APPLICATION_JSON_TYPE).entity(stringWriter.toString())
                    .header("Access-Control-Allow-Origin", "*").build(); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (JSONException e) {
            LOGGER.warn("Unable to send statistics.");
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Unable to send statistics due to storage exception.", e);
            }
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }

    // Object to store type statistics before building JSON output
    class TypeEntry {

        String typeName;

        double count;
    }
}
