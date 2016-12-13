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

import com.amalto.core.objects.DroppedItemPOJO;
import com.amalto.core.objects.DroppedItemPOJOPK;
import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.util.XtentisException;
import org.apache.log4j.Logger;
import com.amalto.core.server.api.DroppedItem;

import java.util.List;

public class DefaultDroppedItem implements DroppedItem {

    private static final Logger LOGGER = Logger.getLogger(DefaultDroppedItem.class);

    /**
     * Recover a dropped item
     *
     * @throws com.amalto.core.util.XtentisException
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public ItemPOJOPK recoverDroppedItem(DroppedItemPOJOPK droppedItemPOJOPK) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("recovering " + droppedItemPOJOPK.getUniquePK());
        }
        try {
            return DroppedItemPOJO.recover(droppedItemPOJOPK);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to recover the dropped item " + droppedItemPOJOPK.getUniquePK()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Find all dropped items pks
     *
     * @throws com.amalto.core.util.XtentisException
     */
    public List<DroppedItemPOJOPK> findAllDroppedItemsPKs(String regex) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("finding all dropped items pks ");
        }
        try {
            return DroppedItemPOJO.findAllPKs(regex);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to find all dropped items pks  "
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Load a dropped item
     *
     * @throws com.amalto.core.util.XtentisException
     * @ejb.interface-method view-type = "both"
     * @ejb.facade-method
     */
    public DroppedItemPOJO loadDroppedItem(DroppedItemPOJOPK droppedItemPOJOPK) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("loading " + droppedItemPOJOPK.getUniquePK());
        }
        try {
            return DroppedItemPOJO.load(droppedItemPOJOPK);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to load the dropped item " + droppedItemPOJOPK.getUniquePK()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }

    /**
     * Remove a dropped item
     *
     * @throws com.amalto.core.util.XtentisException
     */
    public DroppedItemPOJOPK removeDroppedItem(DroppedItemPOJOPK droppedItemPOJOPK) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("removing " + droppedItemPOJOPK.getUniquePK());
        }
        try {
            return DroppedItemPOJO.remove(droppedItemPOJOPK);
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the dropped item " + droppedItemPOJOPK.getUniquePK()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err);
            throw new XtentisException(err, e);
        }
    }
}