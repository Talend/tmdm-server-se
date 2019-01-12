/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;

/**
 * The following class that is registered and used to get the derive object from the
 * {@link java.util.ServiceLoader#load()}, the concreted functionality is to synchronize the <b> UpdateReport </b> table
 * structure when the server running mode switch from Standalone to Cluster mode.
 * 
 * created by hwzhu on Jan 11, 2019
 *
 */
public interface PersistenceExtension {

    /**
     * Check if which derive class be premised to return from the ServiceLoader#load method. just only the condition
     * that the Server is Cluster mode and corresponding DB be initialized is true, the service class will be used.
     * 
     * @param server
     * @return
     */
    boolean accept(Server server);

    /**
     * If there is related service class returned from above accept, the <b>Update</b> method will be invoked to change
     * the UpdateReport table schema structure using liquibase library. The mainly implementation is to switch the
     * <b>UpdateReport</b> table's Primary Key to UUID type in Cluster mode.
     */
    void update();
}