/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects.storedprocedure;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.metadata.LongString;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;

import java.util.Collection;


public class StoredProcedurePOJO extends ObjectPOJO {

    private final Logger LOGGER = Logger.getLogger(StoredProcedurePOJO.class);

    private String name;

    private String description;

    private String procedure;

    private boolean refreshCache;

    public StoredProcedurePOJO() {
    }

    public StoredProcedurePOJO(String name, String query) {
        this.name = name;
        this.procedure = query;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isRefreshCache() {
        return refreshCache;
    }

    public void setRefreshCache(boolean refreshCache) {
        this.refreshCache = refreshCache;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @LongString
    public String getProcedure() {
        return procedure;
    }

    public void setProcedure(String procedure) {
        this.procedure = procedure;
    }

    public Collection<String> execute(DataClusterPOJOPK dataClusterPOJOPK, String[] parameters) throws XtentisException {
        if (getProcedure() == null) {
            return null;
        }
        try {
            XmlServer server = Util.getXmlServerCtrlLocal();
            String cluster = null;
            if (dataClusterPOJOPK != null) {
                cluster = dataClusterPOJOPK.getUniqueId();
            }
            return server.runQuery(cluster, getProcedure(), parameters, true);
        } catch (Exception e) {
            String err = "Unable to execute the Stored Procedure " + getPK().getUniqueId()
                    + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new RuntimeException(err, e);
        }
    }

    @Override
    public ObjectPOJOPK getPK() {
        if (getName() == null) {
            return null;
        }
        return new ObjectPOJOPK(new String[]{name});
    }
}
