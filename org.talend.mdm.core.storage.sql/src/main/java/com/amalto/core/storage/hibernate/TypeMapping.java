/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Clob;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.io.IOUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.collection.internal.PersistentList;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.storage.record.DataRecord;

/**
 * Represents type mapping between data model as specified by the user and data model as used by hibernate storage.
 */
public abstract class TypeMapping {

    public static final String SQL_TYPE = "SQL_TYPE"; //$NON-NLS-1$

    public static final String SQL_TYPE_CLOB = "clob"; //$NON-NLS-1$
    
    public static final String SQL_TYPE_TEXT = "text"; //$NON-NLS-1$

    public static final String SQL_TYPE_BOOLEAN = "boolean"; //$NON-NLS-1$

    /**
     * Used to hold how many times a reusable type is reused within data model (may help to decide whether constrains
     * should be generated).
     */
    public static final String USAGE_NUMBER = "USAGES"; //$NON-NLS-1$

    protected final ComplexTypeMetadata user;

    protected final ComplexTypeMetadata database;

    protected final MappingRepository mappings;

    protected boolean isFrozen;

    protected TypeMapping(ComplexTypeMetadata user, MappingRepository mappings) {
        this.user = user;
        this.database = (ComplexTypeMetadata) user.copyShallow();
        this.mappings = mappings;
        // Make sure internal type (database) does not have any '-' that blocks Java classes generation.
        database.setName(database.getName().replace('-', '_'));
    }

    protected TypeMapping(ComplexTypeMetadata user, ComplexTypeMetadata database, MappingRepository mappings) {
        this.user = user;
        this.database = database;
        this.mappings = mappings;
    }

    protected static <T> void resetList(List<T> oldValues, List<T> newValues) {
        if (newValues == null) {
            if (oldValues != null) {
                oldValues.clear();
            }
            return;
        }
        Iterator<T> iterator = newValues.iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            T nextNew = iterator.next();
            if (nextNew != null) {
                if (i < oldValues.size() && !nextNew.equals(oldValues.get(i))) {
                    oldValues.set(i, nextNew);
                } else if (i >= oldValues.size()) {
                    oldValues.add(i, nextNew);
                }
            }
        }
        while (oldValues.size() > newValues.size()) {
            oldValues.remove(oldValues.size() - 1);
        }
    }

    public static Object readValue(DataRecord from, FieldMetadata sourceField, FieldMetadata targetField, Session session) {
        Object value = from.get(sourceField);
        return _serializeValue(value, sourceField, targetField, session);
    }

    public static Object readValue(Wrapper from, FieldMetadata sourceField, FieldMetadata targetField) {
        Object value = from.get(sourceField.getName());
        return _deserializeValue(value, sourceField, targetField);
    }

    private static Object _serializeValue(Object value, FieldMetadata sourceField, FieldMetadata targetField, Session session) {
        if (targetField == null) {
            return value;
        }
        if (!targetField.isMany()) {
            Boolean targetZipped = targetField.<Boolean> getData(MetadataRepository.DATA_ZIPPED);
            Boolean sourceZipped = sourceField.<Boolean> getData(MetadataRepository.DATA_ZIPPED);
            if (sourceZipped == null && Boolean.TRUE.equals(targetZipped)) {
                try {
                    ByteArrayOutputStream characters = new ByteArrayOutputStream();
                    OutputStream bos = new Base64OutputStream(characters);
                    ZipOutputStream zos = new ZipOutputStream(bos);
                    ZipEntry zipEntry = new ZipEntry("content"); //$NON-NLS-1$
                    zos.putNextEntry(zipEntry);
                    zos.write(String.valueOf(value).getBytes("UTF-8")); //$NON-NLS-1$
                    zos.closeEntry();
                    zos.flush();
                    zos.close();
                    return new String(characters.toByteArray());
                } catch (IOException e) {
                    throw new RuntimeException("Unexpected compression exception", e); //$NON-NLS-1$
                }
            }
            String targetSQLType = targetField.getType().getData(TypeMapping.SQL_TYPE);
            if (targetSQLType != null && SQL_TYPE_CLOB.equals(targetSQLType)) {
                if (value != null) {
                    return Hibernate.getLobCreator(session).createClob(String.valueOf(value));
                } else {
                    return null;
                }
            }
        }
        return value;
    }

    private static Object _deserializeValue(Object value, FieldMetadata sourceField, FieldMetadata targetField) {
        if (targetField == null) {
            return value;
        }
        if (!targetField.isMany()) {
            Boolean targetZipped = targetField.<Boolean> getData(MetadataRepository.DATA_ZIPPED);
            Boolean sourceZipped = sourceField.<Boolean> getData(MetadataRepository.DATA_ZIPPED);
            if (Boolean.TRUE.equals(sourceZipped) && targetZipped == null) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(String.valueOf(value).getBytes("UTF-8")); //$NON-NLS-1$
                    ZipInputStream zis = new ZipInputStream(new Base64InputStream(bis));
                    byte[] buffer = new byte[1024];
                    int read;
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    while (zis.getNextEntry() != null) {
                        while ((read = zis.read(buffer, 0, buffer.length)) > -1) {
                            bos.write(buffer, 0, read);
                        }
                    }
                    return new String(bos.toByteArray(), "UTF-8"); //$NON-NLS-1$
                } catch (IOException e) {
                    throw new RuntimeException("Unexpected deflate exception", e); //$NON-NLS-1$
                }
            }
            String targetSQLType = sourceField.getType().getData(TypeMapping.SQL_TYPE);
            if (value != null && targetSQLType != null && SQL_TYPE_CLOB.equals(targetSQLType)) {
                try {
                    Reader characterStream = ((Clob) value).getCharacterStream();
                    return new String(IOUtils.toCharArray(characterStream)); // No need to close (Hibernate seems to
                                                                             // handle this).
                } catch (Exception e) {
                    throw new RuntimeException("Unexpected read from clob exception", e); //$NON-NLS-1$
                }
            }
        }
        return value;
    }

    /*
     * See TMDM-5524: Hibernate sometimes "hides" values of a collection when condition is on contained value. This
     * piece of code forces load. Relates also to TMDM-7452.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected static <T> List<T> getFullList(List valueList) {
        if (valueList == null) {
            return null;
        }
        if (!(valueList instanceof PersistentList)) {
            return valueList;
        }
        PersistentList list = (PersistentList) valueList;
        List<T> fullList = new LinkedList<T>();
        SessionImplementor session = list.getSession();
        if (!session.isConnected()) {
            throw new IllegalStateException("Session is not connected: impossible to read values from database."); //$NON-NLS-1$
        }
        CollectionEntry entry = session.getPersistenceContext().getCollectionEntry(list);
        CollectionPersister persister = entry.getLoadedPersister();
        if (persister != null) {
            int databaseSize = persister.getSize(entry.getKey(), session);
            if (list.size() == databaseSize && !list.contains(null)) {
                // No need to reload a list (no omission in list and size() corresponds to size read from database).
                return list;
            }
            for (int i = 0; i < databaseSize; i++) {
                T wrapper = (T) persister.getElementByIndex(entry.getLoadedKey(), i, session, list.getOwner());
                fullList.add(wrapper);
            }
        }
        // Returns a unmodifiable list -> returned list is *not* a persistent list so change tracking is not possible,
        // returning a unmodifiable list is a safety for code using returned list.
        return Collections.unmodifiableList(fullList);
    }

    public ComplexTypeMetadata getDatabase() {
        return database;
    }

    public ComplexTypeMetadata getUser() {
        return user;
    }

    protected abstract void map(FieldMetadata user, FieldMetadata database);

    protected abstract void freeze();

    public abstract FieldMetadata getDatabase(FieldMetadata from);

    public abstract FieldMetadata getUser(FieldMetadata to);

    public String getName() {
        return database.getName();
    }

    /**
     * Set values <b>from</b> MDM representation <b>to</b> a Hibernate representation (i.e. POJOs).
     * 
     * @param session A valid (opened) Hibernate session that might be used to resolve FK values.
     * @param from A value from MDM (usually got with
     * {@link com.amalto.core.storage.Storage#fetch(com.amalto.core.query.user.Expression)}
     * @param to The Hibernate POJO where values should be set. Object is expected to implement {@link Wrapper}
     * interface.
     */
    public abstract void setValues(Session session, DataRecord from, Wrapper to);

    /**
     * Set values <b>from</b> Hibernate representation <b>to</b> a MDM representation.
     * 
     * @param from A Hibernate object that represents a MDM entity instance.
     * @param to A MDM internal representation of the MDM record.
     * @return The modified version of <code>to</code>. In fact, return can be ignored for callers, this is a
     * convenience for recursion.
     */
    public abstract DataRecord setValues(Wrapper from, DataRecord to);

    /**
     * @return A name for the timestamp in storage. The name is database dependent and represent name of a field in
     * underlying storage (column name, XPath...). Returns <code>null</code> if mapping has no database field for this.
     */
    public abstract String getDatabaseTimestamp();

    /**
     * @return A name for the task id in storage. The name is database dependent and represent name of a field in
     * underlying storage (column name, XPath...). Returns <code>null</code> if mapping has no database field for this.
     */
    public abstract String getDatabaseTaskId();
}
