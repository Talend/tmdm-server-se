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

package com.amalto.core.save.context;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.save.DocumentSaverContext;
import com.amalto.core.util.OutputReport;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import java.io.InputStream;
import java.util.Set;

/**
 *
 */
public interface SaverSource {

    MutableDocument get(String dataClusterName, String dataModelName, String typeName, String[] key);

    boolean exist(String dataCluster, String dataModelName, String typeName, String[] key);

    MetadataRepository getMetadataRepository(String dataModelName);

    InputStream getSchema(String dataModelName);

    OutputReport invokeBeforeSaving(DocumentSaverContext context, MutableDocument updateReportDocument);

    Set<String> getCurrentUserRoles();

    String getUserName();
    
    String getLegitimateUser();

    boolean existCluster(String dataClusterName);

    void resetLocalUsers();

    void initAutoIncrement();

    void routeItem(String dataCluster, String typeName, String[] id);

    void invalidateTypeCache(String dataModelName);

    void saveAutoIncrement();

    String nextAutoIncrementId(String dataCluster, String dataModel, String conceptName);
}
