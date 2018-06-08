/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.services;

public interface BulkUpdate {

    public static final String FAILURE = "failure"; //$NON-NLS-1$

    public static final String SUCCESS = "success"; //$NON-NLS-1$

    String bulkUpdate(String storageName, String typeName, String storageType, boolean createUpdateReport, String content);
}
