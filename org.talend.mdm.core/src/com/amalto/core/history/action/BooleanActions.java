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

package com.amalto.core.history.action;

import com.amalto.core.history.Action;
import org.apache.log4j.Logger;

public class BooleanActions {

    private static final Logger LOGGER = Logger.getLogger(BooleanActions.class);

    public static Action preferTrue(FieldUpdateAction action) {
        String newValueAsString = action.getNewValue();
        String oldValueAsString = action.getOldValue();
        try {
            boolean newValue = newValueAsString.isEmpty() ? Boolean.TRUE : Boolean.parseBoolean(newValueAsString);
            boolean oldValue = oldValueAsString.isEmpty() ? Boolean.TRUE : Boolean.parseBoolean(oldValueAsString);
            if (newValue) {
                return action;
            } else if (oldValue) {
                return NoOpAction.instance();
            } else {
                return NoOpAction.instance();
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Can't use the field action PREFER TRUE on '" + action + "'.", e);
            }
            return NoOpAction.instance();
        }
    }

    public static Action preferFalse(FieldUpdateAction action) {
        String newValueAsString = action.getNewValue();
        String oldValueAsString = action.getOldValue();
        try {
            boolean newValue = newValueAsString.isEmpty() ? Boolean.FALSE : Boolean.parseBoolean(newValueAsString);
            boolean oldValue = oldValueAsString.isEmpty() ? Boolean.FALSE : Boolean.parseBoolean(oldValueAsString);
            if (!newValue) {
                return action;
            } else if (!oldValue) {
                return NoOpAction.instance();
            } else {
                return NoOpAction.instance();
            }
        } catch (Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Can't use the field action PREFER FALSE on '" + action + "'.", e);
            }
            return NoOpAction.instance();
        }
    }
}
