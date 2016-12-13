/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage;

/**
 * A special DispatchWrapper: this wrapper always uses RDBMS for both system and user master data.
 */
// Dynamically called! Don't remove!
public class SQLWrapper extends DispatchWrapper {

    // MDM instantiates several times this class, keep constructor parameters as constant limits new instances.
    private static final SystemStorageWrapper INTERNAL = new SystemStorageWrapper();

    // MDM instantiates several times this class, keep constructor parameters as constant limits new instances.
    private static final StorageWrapper USER = new StorageWrapper();

    public SQLWrapper() {
        super(INTERNAL, USER);
    }
}
