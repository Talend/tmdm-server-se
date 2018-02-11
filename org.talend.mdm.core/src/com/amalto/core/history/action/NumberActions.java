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

public class NumberActions {

    private static final Logger LOGGER = Logger.getLogger(NumberActions.class);

    public static Action max(FieldUpdateAction action) {
        String newValueAsString = action.getNewValue();
        String oldValueAsString = action.getOldValue();
        try {
            float newValue = newValueAsString.isEmpty() ? Long.MIN_VALUE : Float.parseFloat(newValueAsString);
            float oldValue = oldValueAsString.isEmpty() ? Long.MIN_VALUE : Float.parseFloat(oldValueAsString);
            if (newValue > oldValue) {
                return action;
            } else {
                return NoOpAction.instance();
            }
        } catch (NumberFormatException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Can't use the field action MAX on '" + action + "'.", e);
            }
            return NoOpAction.instance();
        }
    }

    public static Action min(FieldUpdateAction action) {
        try {
            String newValueAsString = action.getNewValue();
            String oldValueAsString = action.getOldValue();
            float newValue = newValueAsString.isEmpty() ? Long.MAX_VALUE : Float.parseFloat(newValueAsString);
            float oldValue = oldValueAsString.isEmpty() ? Long.MAX_VALUE : Float.parseFloat(oldValueAsString);
            if (newValue < oldValue) {
                return action;
            } else {
                return NoOpAction.instance();
            }
        } catch (NumberFormatException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Can't use the field action MIN on '" + action + "'.", e);
            }
            return NoOpAction.instance();
        }
    }
}
