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

package com.amalto.core.server.api;

import java.util.Collection;

import com.amalto.core.objects.configurationinfo.ConfigurationInfoPOJO;
import com.amalto.core.objects.configurationinfo.ConfigurationInfoPOJOPK;
import com.amalto.core.util.XtentisException;

/**
 *
 */
public interface ConfigurationInfo {
    /**
     * Creates or updates a configurationinfo
     *
     * @throws XtentisException
     */
    ConfigurationInfoPOJOPK putConfigurationInfo(ConfigurationInfoPOJO configurationInfo) throws XtentisException;

    /**
     * Get configurationinfo
     *
     * @throws XtentisException
     */
    ConfigurationInfoPOJO getConfigurationInfo(ConfigurationInfoPOJOPK pk) throws XtentisException;

    /**
     * Get a ConfigurationInfo - no exception is thrown: returns null if not found
     *
     * @throws XtentisException
     */
    ConfigurationInfoPOJO existsConfigurationInfo(ConfigurationInfoPOJOPK pk) throws XtentisException;

    /**
     * Remove a Configuration Info
     *
     * @throws XtentisException
     */
    ConfigurationInfoPOJOPK removeConfigurationInfo(ConfigurationInfoPOJOPK pk) throws XtentisException;

    /**
     * Retrieve all ConfigurationInfo PKs
     *
     * @throws XtentisException
     */
    Collection<ConfigurationInfoPOJOPK> getConfigurationInfoPKs(String regex) throws XtentisException;

}
