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

package com.amalto.core.storage.hibernate;

/**
 * This interface exposes operation commonly used by MDM when creating/updating/reading objects from Hibernate.
 * Generated classes by MDM implements this interface.
 *
 * @see ClassCreator
 */
public interface Wrapper {

    /**
     * Standard getter interface.
     *
     * @param fieldName A field name, object that implement this interface should have code to call the appropriate getter.
     * @return The value associated with <code>fieldName</code> or <code>null</code> if field name does not exist.
     */
    Object get(String fieldName);

    /**
     * Standard setter interface
     *
     * @param fieldName A field name, object that implement this interface should have code to call the appropriate setter.
     * @param value     The value to be set. Implementation of method may cast the <code>value</code> to the appropriate setter's type.
     */
    void set(String fieldName, Object value);

    /**
     * @return Returns MDM timestamp for the object (i.e. date of the last modification).
     */
    long timestamp();

    /**
     * Sets a value for the MDM timestamp.
     *
     * @param value A long value (e.g. System.currentTimeMillis).
     * @see System#currentTimeMillis()
     */
    void timestamp(long value);

    /**
     * @return Returns MDM task id for the object.
     */
    String taskId();

    /**
     * Set the MDM task id for the object.
     *
     * @param value A task id or <code>null</code> to remove task id.
     */
    void taskId(String value);

}
