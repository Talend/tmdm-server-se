/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server.api;

import com.amalto.core.objects.DroppedItemPOJO;
import com.amalto.core.objects.DroppedItemPOJOPK;
import com.amalto.core.objects.ItemPOJOPK;

/**
 *
 */
public interface DroppedItem {
    /**
     * Recover a dropped item
     *
     * @throws com.amalto.core.util.XtentisException
     */
    ItemPOJOPK recoverDroppedItem(DroppedItemPOJOPK droppedItemPOJOPK) throws com.amalto.core.util.XtentisException;

    /**
     * Find all dropped items pks
     *
     * @throws com.amalto.core.util.XtentisException
     */
    java.util.List findAllDroppedItemsPKs(String regex) throws com.amalto.core.util.XtentisException;

    /**
     * Load a dropped item
     *
     * @throws com.amalto.core.util.XtentisException
     */
    DroppedItemPOJO loadDroppedItem(DroppedItemPOJOPK droppedItemPOJOPK) throws com.amalto.core.util.XtentisException;

    /**
     * Remove a dropped item
     *
     * @throws com.amalto.core.util.XtentisException
     */
    DroppedItemPOJOPK removeDroppedItem(DroppedItemPOJOPK droppedItemPOJOPK) throws com.amalto.core.util.XtentisException;
}
