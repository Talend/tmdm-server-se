/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.objects;

import com.amalto.core.util.XtentisException;

import java.io.Serializable;
import java.util.HashMap;

public abstract class Service {

    /**
     * Returns the description of the service. Can be null if not available.
     */
    public abstract String getDescription(String twoLettersLanguageCode);

    public abstract String getDocumentation(String twoLettersLanguageCode);

    /**
     * Starts the service.
     */
    public abstract void start() throws XtentisException;

    /**
     * Stops if needed the service.
     */
    public abstract void stop() throws XtentisException;

    /**
     * Returns a status of the service Can be null
     */
    public abstract String getStatus();

    /**
     * Runs the service. The object received in an HashMap made of -username - String -password -
     * String -contentType - String -charset - String -bytes - bytes[] -paramameters - HashMap
     * @return Serializable - a serializable Object to be passed backed to the connector
     */
    public abstract Serializable receiveFromOutbound(HashMap<String, Serializable> map) throws XtentisException;

    /**
     * Runs the service. The item received in an XML String
     * 
     * @param itemPK - the item that triggered a Routing Rule <hich created the Active Routing Order
     * @param routingOrderID - the routing Order ID of the routing rule that called - From 2.19.0, the Routing Order is
     * an ActiveRoutingOrderPOJO
     * @param parameters - the routing rules parameters
     * @return this value is appended at the end of the message field of the Routing Order
     */
    public abstract String receiveFromInbound(ItemPOJOPK itemPK, String routingOrderID, String parameters) throws XtentisException;

    /**
     * To request and get the response from other applications
     * 
     * @param command - used to call different pull method in service Object
     * @param parameters - incoming parameters, may be in xml format
     * @param schedulePlanID - the ID of schedule plan, if in schedule mode
     * @return Serializable - a serializable Object to be passed backed to the system
     */
    public abstract Serializable fetchFromOutbound(String command, String parameters, String schedulePlanID) throws XtentisException;

    protected String loadConfiguration() {
        return null;
    }

    /**
     * Configuration received from outbound, typically a portlet The default implementation stores the configuration
     * string "as is".
     */
    public abstract void putConfiguration(String configuration);

    /**
     * Retrieves the configuration The default implementation renders the configuration string "as stored" and ignore
     * the optional parameter
     */
    public abstract String getConfiguration(String optionalParameter) throws XtentisException;

    /**
     * Return default the configuration.
     */
    public abstract String getDefaultConfiguration();

}
