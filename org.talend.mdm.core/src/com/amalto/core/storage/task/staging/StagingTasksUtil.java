/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.task.staging;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.StagingStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;

/**
 * Utility class for {@link StagingTaskManager}
 */
public class StagingTasksUtil {
    
    // SimpleDateFormat is not thread-safe
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS"); //$NON-NLS-1$

    // SimpleDateFormat is not thread-safe
    private static final DateFormat userDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //$NON-NLS-1$

    static {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
    }
    
    public static String formatDate(Date startDate){
        synchronized (dateFormat) {
            return dateFormat.format(startDate);
        }
    }
    
    public static Date parseUserDate(String date) throws ParseException{
        synchronized(userDateFormat){
            return userDateFormat.parse(date);
        }
    }
    
    public static String formatElapsedTime(long elapsedTime) {
        return String.format("%d:%02d:%02d", elapsedTime / 3600, (elapsedTime % 3600) / 60, (elapsedTime % 60)); //$NON-NLS-1$
    }
    
    public static Storage getStagingStorage(final String dataContainer){
        return getStorage(dataContainer, StorageType.STAGING);
    }
    
    public static Storage getMasterStorage(final String dataContainer){
        return getStorage(dataContainer, StorageType.MASTER);
    }
    
    private static Storage getStorage(final String dataContainer, StorageType type){
        Server server = ServerContext.INSTANCE.get();
        Storage storage = server.getStorageAdmin().get(dataContainer, type);
        if (storage == null) {
            throw new IllegalStateException("No storage available for container '" + dataContainer + "' and type " + type);
        }
        return storage;
    }

    public static ComplexTypeMetadata getTaskExecutionType(final Storage stagingStorage){
        ComplexTypeMetadata result = stagingStorage.getMetadataRepository().getComplexType(StagingStorage.EXECUTION_LOG_TYPE);
        if(result == null){
            throw new IllegalStateException("No type " + StagingStorage.EXECUTION_LOG_TYPE + " found in staging storage");
        }
        return result;
    }

}
