/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.util.webapp.XSystemObjects;

import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.server.api.DataCluster;
import com.amalto.core.server.api.XmlServer;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

public class DefaultDataCluster implements DataCluster {

    private static final Logger LOGGER = Logger.getLogger(DefaultDataCluster.class);

    public static Map<String, DataClusterPOJO> EXISTED_DATA_CLUSTERS = new HashMap<String, DataClusterPOJO>();

    /**
     * Creates or updates a data cluster
     */
    @Override
    public DataClusterPOJOPK putDataCluster(DataClusterPOJO dataCluster) throws XtentisException {
        try {
            ObjectPOJOPK pk = dataCluster.store();
            if (pk == null) {
                throw new XtentisException("Unable to create the Data Cluster. Please check the XML Server logs");
            }
            // create the actual physical cluster
            try {
                // get the xml server wrapper
                XmlServer server;
                try {
                    server = Util.getXmlServerCtrlLocal();
                } catch (Exception e) {
                    String err = "Error creating cluster '" + dataCluster.getName()
                            + "' : unable to access the XML Server wrapper";
                    LOGGER.error(err, e);
                    throw new XtentisException(err, e);
                }
                boolean exist = server.existCluster(pk.getUniqueId());
                if (!exist) {
                    server.createCluster(pk.getUniqueId());
                }
            } catch (Exception e) {
                String err = "Unable to physically create the data cluster " + pk.getUniqueId() + ": " + e.getClass().getName()
                        + ": " + e.getLocalizedMessage();
                try {
                    ObjectPOJO.remove(DataClusterPOJO.class, new ObjectPOJOPK(pk.getUniqueId()));
                } catch (Exception x) {
                    LOGGER.error(x.getMessage(), x);
                }
                throw new XtentisException(err, e);
            }
            EXISTED_DATA_CLUSTERS.put(pk.getUniqueId(), dataCluster);
            return new DataClusterPOJOPK(pk);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to create/update the datacluster " + dataCluster.getName() + ": " + e.getClass().getName()
                    + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get datacluster
     */
    @Override
    public DataClusterPOJO getDataCluster(DataClusterPOJOPK pk) throws XtentisException {
        try {
            if (pk.getUniqueId() == null) {
                throw new XtentisException("The Data Cluster should not be null!");
            }
            if (pk.getUniqueId().endsWith(StorageAdmin.STAGING_SUFFIX)) {
                pk = new DataClusterPOJOPK(StringUtils.substringBeforeLast(pk.getUniqueId(), "#"));
            }
            if (EXISTED_DATA_CLUSTERS.containsKey(pk.getUniqueId())) {
                return EXISTED_DATA_CLUSTERS.get(pk.getUniqueId());
            }
            DataClusterPOJO dataCluster = ObjectPOJO.load(DataClusterPOJO.class, pk);
            if (dataCluster == null) {
                String err = "The Data Cluster " + pk.getUniqueId() + " does not exist.";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            EXISTED_DATA_CLUSTERS.put(pk.getUniqueId(), dataCluster);
            return dataCluster;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the Data Cluster " + pk.toString() + ": " + e.getClass().getName() + ": "
                    + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err);
        }
    }

    /**
     * Get a DataCluster - no exception is thrown: returns null if not found
     */
    @Override
    public DataClusterPOJO existsDataCluster(DataClusterPOJOPK pk) throws XtentisException {
        if (pk == null) {
            throw new IllegalArgumentException("The Data Cluster should not be null!");
        }
        if (pk.getUniqueId() == null) {
            throw new XtentisException("The Data cluster PK cannot be null.");
        }
        // Staging DataCluster should be changed to the Master DataCluster
        if (pk.getUniqueId().endsWith(StorageAdmin.STAGING_SUFFIX)) {
            String cluster = StringUtils.substringBeforeLast(pk.getUniqueId(), "#");
            if (!Util.getXmlServerCtrlLocal().supportStaging(cluster)) {
                throw new XtentisException("Cluster '" + pk.getUniqueId() + "' (revision: 'null') does not exist"); //$NON-NLS-1$//$NON-NLS-2$
            }
            pk = new DataClusterPOJOPK(cluster);
        }
        try {
            ILocalUser user = LocalUser.getLocalUser();
            if (user != null && !user.userCanRead(DataClusterPOJO.class, pk.getUniqueId())) {
                String err = "Unauthorized read access by " + "user '" + user.getUsername() + "' on object "
                        + ObjectPOJO.getObjectName(DataClusterPOJO.class) + " [" + pk.getUniqueId() + "] ";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            if (EXISTED_DATA_CLUSTERS.containsKey(pk.getUniqueId())) {
                return EXISTED_DATA_CLUSTERS.get(pk.getUniqueId());
            }
            DataClusterPOJO dataCluster = ObjectPOJO.load(DataClusterPOJO.class, pk);
            EXISTED_DATA_CLUSTERS.put(pk.getUniqueId(), dataCluster);
            return dataCluster;
        } catch (XtentisException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exist data cluster check exception.", e);
            }
            return null;
        } catch (Exception e) {
            String info = "Could not check whether this Data Cluster \"" + pk.getUniqueId() + "\" exists:  " + ": "
                    + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.debug("existsDataCluster() " + info, e);
            return null;
        }
    }

    /**
     * Remove a Data Cluster The physical remove is performed on a separate Thred
     */
    @Override
    public DataClusterPOJOPK removeDataCluster(DataClusterPOJOPK pk) throws XtentisException {
        // remove the actual physical cluster - do it asynchronously
        try {
            String dataClusterName = pk.getUniqueId();
            // get the xml server wrapper
            XmlServer server = Util.getXmlServerCtrlLocal();
            server.deleteCluster(dataClusterName);
        } catch (Exception e) {
            String err = "Unable to physically delete the data cluster " + pk.getUniqueId() + ": " + e.getClass().getName()
                    + ": " + e.getLocalizedMessage();
            try {
                ObjectPOJO.remove(DataClusterPOJO.class, new ObjectPOJOPK(pk.getUniqueId()));
            } catch (Exception x) {
                LOGGER.error("Could not remove data cluster object.", x);
            }
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
        ObjectPOJOPK objectPOJOPK = ObjectPOJO.remove(DataClusterPOJO.class, pk);
        if (EXISTED_DATA_CLUSTERS.containsKey(pk.getUniqueId())) {
            EXISTED_DATA_CLUSTERS.remove(pk.getUniqueId());
        }
        return new DataClusterPOJOPK(objectPOJOPK);
    }

    /**
     * Retrieve all DataCluster PKs
     */
    @Override
    public Collection<DataClusterPOJOPK> getDataClusterPKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> dataClusterPKs = ObjectPOJO.findAllPKs(DataClusterPOJO.class, regex);
        List<DataClusterPOJOPK> l = new ArrayList<DataClusterPOJOPK>();
        for (ObjectPOJOPK dataClusterPK : dataClusterPKs) {
            l.add(new DataClusterPOJOPK(dataClusterPK));
        }
        return l;
    }

    /**
     * Add this string words to the vocabulary - ignore xml tags
     */
    @Override
    public int addToVocabulary(DataClusterPOJOPK pk, String string) throws XtentisException {
        return 0;
    }

    /**
     * Spell checks a sentence and return possible spellings
     */
    @Override
    public Collection<String> spellCheck(DataClusterPOJOPK dcpk, String sentence, int treshold, boolean ignoreNonExistantWords)
            throws XtentisException {
        return Collections.emptyList();
    }
}