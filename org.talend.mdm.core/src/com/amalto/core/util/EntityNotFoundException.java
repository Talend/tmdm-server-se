/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

import org.apache.commons.lang.StringUtils;

import com.amalto.core.objects.ItemPOJOPK;
import com.amalto.core.objects.ObjectPOJOPK;

public class EntityNotFoundException extends XtentisException {

    private static final long serialVersionUID = -5541352366654335876L;

    private ItemPOJOPK itemPK;

    public EntityNotFoundException(ItemPOJOPK pk) {
        this.itemPK = pk;
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    @Override
    public String getMessage() {
        if (itemPK != null) {
            if (StringUtils.isEmpty(itemPK.getDataClusterPOJOPK().getUniqueId())) {
                return "No container specified in id '" + itemPK.getUniqueID() + "'.";
            }
            if (StringUtils.isEmpty(itemPK.getConceptName())) {
                return "No data model specified in '" + itemPK.getUniqueID() + "'.";
            } else {
                return "Could not find item '" + itemPK.getUniqueID() + "'.";
            }
        } else {
            return "Could not find item."; // Generic error message.
        }
    }
}
