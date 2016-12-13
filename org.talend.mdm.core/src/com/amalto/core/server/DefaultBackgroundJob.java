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
import com.amalto.core.objects.backgroundjob.BackgroundJobPOJO;
import com.amalto.core.objects.backgroundjob.BackgroundJobPOJOPK;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;
import com.amalto.core.server.api.BackgroundJob;

import java.util.ArrayList;
import java.util.Collection;

public class DefaultBackgroundJob implements BackgroundJob {

    private static final Logger LOGGER = Logger.getLogger(DefaultBackgroundJob.class);

    /**
     * Creates or updates a BackgroundJob
     */
    @Override
    public BackgroundJobPOJOPK putBackgroundJob(BackgroundJobPOJO backgroundJob) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("putBackgroundJob() " + backgroundJob.getDescription());
        }
        try {
            ObjectPOJOPK pk = backgroundJob.store();
            if (pk == null) {
                throw new XtentisException("Unable to create the Background Job. Please check the XML Server logs");
            }
            return new BackgroundJobPOJOPK(pk);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to create/update the Background Job " + backgroundJob.getId()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }

    }


    /**
     * Get Background Job
     */
    @Override
    public BackgroundJobPOJO getBackgroundJob(BackgroundJobPOJOPK pk) throws XtentisException {
        try {
            BackgroundJobPOJO sp = ObjectPOJO.load(BackgroundJobPOJO.class, pk);
            if (sp == null) {
                throw new XtentisException("The Background Job id " + pk.getUniqueId() + " does not exist in storage");
            }
            return sp;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the Background Job " + pk.toString()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Get a BackgroundJob - no exception is thrown: returns null if not found
     */
    @Override
    public BackgroundJobPOJO existsBackgroundJob(BackgroundJobPOJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(BackgroundJobPOJO.class, pk);
        } catch (XtentisException e) {
            return null;
        } catch (Exception e) {
            String info = "Could not check whether this Background Job exists:  " + pk.getUniqueId()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.debug(info, e);
            return null;
        }
    }

    /**
     * Remove an  Background Job
     */
    @Override
    public BackgroundJobPOJOPK removeBackgroundJob(BackgroundJobPOJOPK pk)
            throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removing " + pk.getUniqueId());
        }
        try {
            return new BackgroundJobPOJOPK(ObjectPOJO.remove(BackgroundJobPOJO.class, pk));
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the BackgroundJob " + pk.toString()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Retrieve all BackgroundJob PKs
     */
    @Override
    public Collection<BackgroundJobPOJOPK> getBackgroundJobPKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> c = ObjectPOJO.findAllPKs(BackgroundJobPOJO.class, regex);
        ArrayList<BackgroundJobPOJOPK> l = new ArrayList<BackgroundJobPOJOPK>();
        for (ObjectPOJOPK objectPOJOPK : c) {
            l.add(new BackgroundJobPOJOPK(objectPOJOPK));
        }
        return l;
    }
}