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

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.objects.storedprocedure.StoredProcedurePOJO;
import com.amalto.core.objects.storedprocedure.StoredProcedurePOJOPK;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;
import com.amalto.core.server.api.StoredProcedure;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultStoredProcedure implements StoredProcedure {

    private static final Logger LOGGER = Logger.getLogger(DefaultStoredProcedure.class);

    /**
     * Creates or updates a Stored Procedure
     */
    @Override
    public StoredProcedurePOJOPK putStoredProcedure(StoredProcedurePOJO storedProcedure) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("putStoredProcedure() ");
        }
        try {
            return new StoredProcedurePOJOPK(storedProcedure.store());
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to create/update the Stored Procedure " + storedProcedure.getName()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get item
     */
    @Override
    public StoredProcedurePOJO getStoredProcedure(StoredProcedurePOJOPK pk) throws XtentisException {
        try {
            StoredProcedurePOJO sp = ObjectPOJO.load(StoredProcedurePOJO.class, pk);
            if (sp == null) {
                String err = "The Stored Procedure " + pk.getUniqueId() + " does not exist.";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            return sp;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the StoredProcedure " + pk.toString()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get a Stored Procedure - no exception is thrown: returns null if not found
     */
    @Override
    public StoredProcedurePOJO existsStoredProcedure(StoredProcedurePOJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(StoredProcedurePOJO.class, pk);
        } catch (XtentisException e) {
            return null;
        } catch (Exception e) {
            String info = "Could not check whether this Stored Procedure exists:  " + pk.toString()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(info, e);
            }
            return null;
        }
    }

    /**
     * Remove an item
     */
    @Override
    public StoredProcedurePOJOPK removeStoredProcedure(StoredProcedurePOJOPK pk)
            throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removing " + pk.getUniqueId());
        }
        try {
            return new StoredProcedurePOJOPK(ObjectPOJO.remove(StoredProcedurePOJO.class, pk));
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the Stored Procedure " + pk.toString()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Executes the stored procedure and return the result as a Collection
     */
    @Override
    public Collection<String> execute(StoredProcedurePOJOPK sppk, DataClusterPOJOPK dcpk, String[] parameters) throws XtentisException {
        try {
            return getStoredProcedure(sppk).execute(dcpk, parameters);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Retrieve all Stored Procedure PKS
     */
    @Override
    public Collection<StoredProcedurePOJOPK> getStoredProcedurePKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> c = ObjectPOJO.findAllPKs(StoredProcedurePOJO.class, regex);
        ArrayList<StoredProcedurePOJOPK> l = new ArrayList<StoredProcedurePOJOPK>();
        for (ObjectPOJOPK objectPOJOPK : c) {
            l.add(new StoredProcedurePOJOPK(objectPOJOPK));
        }
        return l;
    }
}