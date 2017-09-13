/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;

/**
 * This interface provides all API to create table/columns names for the database schema (based on the data model
 * elements).
 */
interface TableResolver {

    /**
     * Returns the table name for <code>type</code>. The table name is more a 'proposition', database specific mapping
     * may decide to shorten the name (or not).
     * 
     * @param type A type from a data model.
     * @return A proposed table name to store instances of type <code>type</code>.
     */
    String get(ComplexTypeMetadata type);

    /**
     * Returns the column name for <code>field</code>. The column name is more a 'proposition', database specific
     * mapping may decide to shorten the name (or not).
     * 
     * @param field A field from a type of data model.
     * @return A proposed column name to store values of field <code>field</code>.
     */
    String get(FieldMetadata field);

    /**
     * Similar to {@link #get(org.talend.mdm.commmon.metadata.FieldMetadata)} but the additional <code>prefix</code> can
     * be used to make column name unique.
     * 
     * @param field A field from a type of data model.
     * @param prefix A prefix to add (to be use to make column name unique for instance).
     * @return A proposed column name to store values of field <code>field</code>.
     */
    String get(FieldMetadata field, String prefix);

    /**
     * @param field A field from a type of data model.
     * @return <code>true</code> if field should be indexed for fast search, <code>false</code> in other cases.
     */
    boolean isIndexed(FieldMetadata field);

    /**
     * Returns a index name valid for the database (right length and syntax).
     * 
     * @param fieldName The field's name to use for the index.
     * @param prefix A prefix to add (to be use to make index name unique for instance).
     * @return A string that can be used as index name on the database.
     */
    String getIndex(String fieldName, String prefix);

    /**
     * Returns name for a table that can store a collection of values.
     * 
     * @param field A many valued field.
     * @return A string that can be used as table name on the database.
     */
    String getCollectionTable(FieldMetadata field);

    /**
     * @param referenceField A foreign key to generate in the database
     * @return A unique identifier for the referenced field. Returns empty string if no unique name is required.
     */
    String getFkConstraintName(ReferenceFieldMetadata referenceField);

    /**
     * Modify is needed the name passed as parameter to match max length limitations.
     * @param name A database object name.
     * @return The same name if shorter than max length or same name if already shorter than max length.
     */
    String get(String name);
}
