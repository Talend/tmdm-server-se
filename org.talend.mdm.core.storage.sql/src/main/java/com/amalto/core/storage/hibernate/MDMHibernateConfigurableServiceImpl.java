// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage.hibernate;

import java.util.Map;

import org.hibernate.service.Service;
import org.hibernate.service.spi.Configurable;

/**
 * created by hwzhu on Aug 18, 2020 Detailled comment
 *
 */
public class MDMHibernateConfigurableServiceImpl implements Service, Configurable {

    // Access to this field requires synchronization on -this-
    private Map configurationValues;

    @Override
    public void configure(Map configurationValues) {
        this.configurationValues = configurationValues;
    }

    /**
     * Get a property value by name
     *
     * @param propertyName The name of the property
     *
     * @return The value currently associated with that property name; may be null.
     */
    public String getProperty(String propertyName) {
        Object o = configurationValues.get(propertyName);
        return o instanceof String ? (String) o : null;
    }
    
    /**
     * Set a property value by name
     *
     * @param propertyName The name of the property to set
     * @param value The new property value
     *
     * @return this for method chaining
     */
    public Configurable setProperty(String propertyName, String value) {
        configurationValues.put(propertyName, value);
        return this;
    }
}
