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

package com.amalto.core.storage;

/**
 * Describes what type ({@link #MASTER}, {@link #STAGING} or {@link #SYSTEM}) a {@link com.amalto.core.storage.Storage storage} is.
 */
public enum StorageType {
    /**
     * Indicates storage stores validated data (master data).
     */
    MASTER,
    /**
     * Indicates storage stores unvalidated data.
     */
    STAGING,
    /**
     * Indicates storage stores system data (MDM internal objects).
     */
    SYSTEM
}
