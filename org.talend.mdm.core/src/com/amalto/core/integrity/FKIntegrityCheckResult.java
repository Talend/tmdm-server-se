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

package com.amalto.core.integrity;

/**
 * Types of FK integrity policy:
 *
 * <ul>
 *     <li>{@link #FORBIDDEN}</li>
 *     <li>{@link #FORBIDDEN_OVERRIDE_ALLOWED}</li>
 *     <li>{@link #ALLOWED}</li>
 * </ul>
 */
public enum FKIntegrityCheckResult {
    /**
     * Delete of the item is forbidden (at least an other instance points to the item) and <b>can not</b> be overridden
     * based on data model information.
     */
    FORBIDDEN,
    /**
     * Delete of the item is forbidden (at least an other instance points to the item) but user <b>can</b> override this
     * behavior based on data model information.
     */
    FORBIDDEN_OVERRIDE_ALLOWED,
    /**
     * Delete of the item is allowed whether other instance(s) point(s) to the item or not.
     */
    ALLOWED
}
