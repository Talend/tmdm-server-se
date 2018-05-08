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

package com.amalto.core.server;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.configurationinfo.ConfigurationInfoPOJO;
import com.amalto.core.objects.configurationinfo.ConfigurationInfoPOJOPK;
import com.amalto.core.server.api.ConfigurationInfo;
import com.amalto.core.util.XtentisException;

public class DefaultConfigurationInfo implements ConfigurationInfo {

    private static final Logger LOGGER = Logger.getLogger(DefaultConfigurationInfo.class);

    @Override
    public ConfigurationInfoPOJOPK putConfigurationInfo(ConfigurationInfoPOJO configurationInfo) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("putConfigurationInfo() ");
        }
        try {
            ObjectPOJOPK pk = configurationInfo.store();
            if (pk == null) {
                throw new XtentisException("Unable to create the Configuration Info. Please check the XML Server logs");
            }
            return new ConfigurationInfoPOJOPK(pk);
        } catch (XtentisException e) {
        	throw(e);
	    } catch (Exception e) {
    	    String err = "Unable to create/update the configuration info "+configurationInfo.getName()
    	    		+": "+e.getClass().getName()+": "+e.getLocalizedMessage();
    	    LOGGER.error(err,e);
    	    throw new XtentisException(err, e);
	    }
    }

    @Override
    public ConfigurationInfoPOJO getConfigurationInfo(ConfigurationInfoPOJOPK pk) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("getConfigurationInfo() ");
        }
        try {
            ConfigurationInfoPOJO configurationInfo = ObjectPOJO.load(ConfigurationInfoPOJO.class, pk);
            if (configurationInfo == null) {
                String err = "The Configuration Info " + pk.getUniqueId() + " does not exist.";
                LOGGER.error(err);
                throw new XtentisException(err);
            }
            return configurationInfo;
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to get the Configuration Info " + pk.toString()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    @Override
    public ConfigurationInfoPOJO existsConfigurationInfo(ConfigurationInfoPOJOPK pk) throws XtentisException {
        try {
            return ObjectPOJO.load(ConfigurationInfoPOJO.class, pk);
        } catch (XtentisException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("existsConfigurationInfo error.", e);
            }
            return null;
        } catch (Exception e) {
            String info = "Could not check whether this Configuration Info \"" + pk.getUniqueId() + "\" exists:  "
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(info, e);
            }
            return null;
        }
    }

    @Override
    public ConfigurationInfoPOJOPK removeConfigurationInfo(ConfigurationInfoPOJOPK pk) throws XtentisException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Removing " + pk.getUniqueId());
        }
        try {
            return new ConfigurationInfoPOJOPK(ObjectPOJO.remove(ConfigurationInfoPOJO.class, pk));
        } catch (XtentisException e) {
            throw (e);
        } catch (Exception e) {
            String err = "Unable to remove the ConfigurationInfo " + pk.toString()
                    + ": " + e.getClass().getName() + ": " + e.getLocalizedMessage();
            LOGGER.error(err, e);
            throw new XtentisException(err, e);
        }
    }

    @Override
    public Collection<ConfigurationInfoPOJOPK> getConfigurationInfoPKs(String regex) throws XtentisException {
        Collection<ObjectPOJOPK> configurations = ObjectPOJO.findAllPKs(ConfigurationInfoPOJO.class, regex);
        ArrayList<ConfigurationInfoPOJOPK> l = new ArrayList<ConfigurationInfoPOJOPK>();
        for (ObjectPOJOPK configuration : configurations) {
            l.add(new ConfigurationInfoPOJOPK(configuration));
        }
        return l;
    }
}
