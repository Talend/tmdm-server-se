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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.collection.internal.PersistentList;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.DataRecordMetadata;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import com.amalto.core.util.ValidateException;

/**
 * Represents type mapping between data model as specified by the user and data model as used by hibernate storage.
 */
class ScatteredTypeMapping extends TypeMapping {

    private static final Logger LOGGER = Logger.getLogger(ScatteredTypeMapping.class);

    private Map<FieldMetadata, FieldMetadata> userToDatabase = new HashMap<FieldMetadata, FieldMetadata>();

    private Map<FieldMetadata, FieldMetadata> databaseToUser = new HashMap<FieldMetadata, FieldMetadata>();

    public ScatteredTypeMapping(ComplexTypeMetadata user, MappingRepository mappings) {
        super(user, mappings);
    }

    @Override
    public void setValues(Session session, DataRecord from, Wrapper to) {
        _setValues(session, from, to);
    }

    Object _setValues(Session session, DataRecord from, Wrapper to) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        for (FieldMetadata field : from.getType().getFields()) {
            FieldMetadata mappedDatabaseField = getDatabase(field);
            if (mappedDatabaseField == null) {
                throw new IllegalStateException("Field '" + field.getName() + "' was expected to have a database mapping");
            }
            if (field instanceof ContainedTypeFieldMetadata) {
                if (!(mappedDatabaseField instanceof ReferenceFieldMetadata)) {
                    throw new IllegalStateException("Contained elements are expected to be mapped to reference.");
                }
                ReferenceFieldMetadata referenceFieldMetadata = (ReferenceFieldMetadata) mappedDatabaseField;
                StorageClassLoader classLoader = (StorageClassLoader) contextClassLoader;
                if (!field.isMany()) {
                    DataRecord referencedObject = (DataRecord) readValue(from, field, mappedDatabaseField, session);
                    Wrapper existingValue = (Wrapper) to.get(referenceFieldMetadata.getName());
                    if (referencedObject != null) {
                        // Update existing value or update existing
                        if (existingValue != null) {
                            // Check for type change
                            ComplexTypeMetadata existingType = classLoader.getTypeFromClass(existingValue.getClass());
                            TypeMapping mapping = mappings.getMappingFromDatabase(existingType);
                            if (mapping == null) {
                                throw new IllegalStateException("Type '" + existingType.getName() + "' has no mapping."); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            boolean isSameType = mapping.getUser().equals(referencedObject.getType());
                            if (isSameType) {
                                to.set(referenceFieldMetadata.getName(), _setValues(session, referencedObject, existingValue));
                            } else {
                                session.delete(existingValue);
                                Wrapper newValue = createObject(contextClassLoader, referencedObject);
                                to.set(referenceFieldMetadata.getName(), _setValues(session, referencedObject, newValue));
                            }
                        } else {
                            Wrapper object = createObject(contextClassLoader, referencedObject);
                            to.set(referenceFieldMetadata.getName(), _setValues(session, referencedObject, object));
                            session.persist(object);
                        }
                    } else {
                        to.set(referenceFieldMetadata.getName(), null);
                        if (existingValue != null) {
                            session.delete(existingValue);
                        }
                    }
                } else {
                    List<DataRecord> dataRecords = (List<DataRecord>) readValue(from, field, mappedDatabaseField, session);
                    Object value = to.get(getDatabase(field).getName());
                    List<Wrapper> existingValue = (List<Wrapper>) value;
                    if (dataRecords != null) {
                        if (existingValue != null && existingValue instanceof PersistentList) {
                            ((PersistentList) existingValue).forceInitialization();
                        }
                        List<Wrapper> objects = existingValue == null ? new ArrayList<Wrapper>(dataRecords.size())
                                : existingValue;
                        List<Wrapper> newValue = new ArrayList<Wrapper>();
                        int i = 0;
                        for (DataRecord dataRecord : dataRecords) {
                            if (i < objects.size() && objects.get(i) != null) {
                                ComplexTypeMetadata existingType = classLoader
                                        .getTypeFromClass(objects.get(i).getClass());
                                TypeMapping mapping = mappings.getMappingFromDatabase(existingType);
                                if (mapping == null) {
                                    throw new IllegalStateException("Type '" + existingType.getName() + "' has no mapping."); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                                boolean isSameType = mapping.getUser().equals(dataRecord.getType());
                                if (mapping.getUser() instanceof ContainedComplexTypeMetadata
                                        && dataRecord.getType() instanceof ContainedComplexTypeMetadata) {
                                    isSameType = ((ContainedComplexTypeMetadata) mapping.getUser()).getContainedType().equals(
                                            ((ContainedComplexTypeMetadata) dataRecord.getType()).getContainedType());
                                }
                                if (!isSameType) {
                                    Wrapper object = createObject(contextClassLoader, dataRecord);
                                    newValue.add((Wrapper) _setValues(session, dataRecord, object));
                                } else {
                                    newValue.add((Wrapper) _setValues(session, dataRecord, objects.get(i)));
                                }
                            } else {
                                Wrapper object = createObject(contextClassLoader, dataRecord);
                                newValue.add((Wrapper) _setValues(session, dataRecord, object));
                                session.persist(object);
                            }
                            i++;
                        }
                        // TMDM-7590: Remove the deleted items
                        if (objects.size() > newValue.size()) {
                            for (i = objects.size() - 1; i >= newValue.size(); i--) {
                                session.delete(objects.get(i));
                                objects.remove(i);
                            }
                        }
                        objects = newValue;
                        to.set(referenceFieldMetadata.getName(), objects);
                    } else {
                        if (value != null) {
                            List<Wrapper> objects = (List<Wrapper>) value;
                            for (Wrapper object : objects) {
                                session.delete(object);
                            }
                            ((List) value).clear();
                        }
                    }
                }
            } else if (field instanceof ReferenceFieldMetadata) {
                if (!field.isMany()) {
                    DataRecord referencedObject = (DataRecord) readValue(from, field, mappedDatabaseField, session);
                    if (referencedObject != null) {
                        Collection<FieldMetadata> keyFields = referencedObject.getType().getKeyFields();
                        Object referenceId;
                        if (keyFields.size() > 1) {
                            List<Object> referenceIdList = new LinkedList<Object>();
                            for (FieldMetadata keyField : keyFields) {
                                referenceIdList.add(readValue(referencedObject, keyField, mappedDatabaseField, session));
                            }
                            referenceId = referenceIdList;
                        } else {
                            referenceId = readValue(referencedObject, keyFields.iterator().next(), mappedDatabaseField, session);
                        }
                        to.set(mappedDatabaseField.getName(),
                                getReferencedObject(contextClassLoader, session,
                                        mappings.getMappingFromUser(referencedObject.getType()).getDatabase(), referenceId));
                    } else {
                        to.set(mappedDatabaseField.getName(), null);
                    }
                } else {
                    List<DataRecord> referencedObjectList = (List<DataRecord>) readValue(from, field, mappedDatabaseField,
                            session);
                    if (referencedObjectList != null) {
                        List<Object> wrappers = new LinkedList<Object>();
                        for (DataRecord dataRecord : referencedObjectList) {
                            Collection<FieldMetadata> keyFields = dataRecord.getType().getKeyFields();
                            Object referenceId;
                            if (keyFields.size() > 1) {
                                List<Object> referenceIdList = new LinkedList<Object>();
                                for (FieldMetadata keyField : keyFields) {
                                    referenceIdList.add(readValue(dataRecord, keyField, mappedDatabaseField, session));
                                }
                                referenceId = referenceIdList;
                            } else {
                                referenceId = readValue(dataRecord, keyFields.iterator().next(), mappedDatabaseField, session);
                            }
                            wrappers.add(getReferencedObject(contextClassLoader, session, dataRecord.getType(), referenceId));
                        }
                        to.set(mappedDatabaseField.getName(), wrappers);
                    } else {
                        Object value = to.get(mappedDatabaseField.getName());
                        if (value != null && value instanceof List) {
                            ((List) value).clear();
                        }
                    }
                }
            } else {
                if (mappedDatabaseField.isMany()) {
                    List<Object> oldValues = (List<Object>) to.get(mappedDatabaseField.getName());
                    List<Object> newValues = (List<Object>) readValue(from, field, mappedDatabaseField, session);
                    if (oldValues != null) {
                        resetList(oldValues, newValues);
                    } else {
                        to.set(mappedDatabaseField.getName(), newValues);
                    }
                } else {
                    to.set(mappedDatabaseField.getName(), readValue(from, field, mappedDatabaseField, session));
                }
            }
        }
        return to;
    }

    @Override
    public DataRecord setValues(Wrapper from, DataRecord to) {
        StorageClassLoader contextClassLoader = (StorageClassLoader) Thread.currentThread().getContextClassLoader();
        for (FieldMetadata userField : to.getType().getFields()) {
            FieldMetadata databaseField = getDatabase(userField);
            Object value = readValue(from, databaseField, userField);
            if (userField != null) {
                if (userField instanceof ContainedTypeFieldMetadata) {
                    if (!userField.isMany()) {
                        Wrapper valueAsWrapper = (Wrapper) value;
                        if (value != null) {
                            DataRecord containedDataRecord = new DataRecord(getActualContainedType(userField, valueAsWrapper),
                                    UnsupportedDataRecordMetadata.INSTANCE);
                            to.set(userField, setValues(valueAsWrapper, containedDataRecord));
                        }
                    } else {
                        List<Wrapper> wrapperList = (List<Wrapper>) value;
                        if (wrapperList != null) {
                            List<Wrapper> fullList = getFullList(wrapperList);
                            for (Wrapper wrapper : fullList) {
                                if (wrapper != null) {
                                    to.set(userField,
                                            setValues(wrapper, new DataRecord(getActualContainedType(userField, wrapper),
                                                    UnsupportedDataRecordMetadata.INSTANCE)));
                                }
                            }
                        }
                    }
                } else if (userField instanceof ReferenceFieldMetadata) {
                    if (!userField.isMany()) {
                        Wrapper wrapper = (Wrapper) value;
                        if (wrapper != null) {
                            TypeMapping mapping = mappings.getMappingFromUser(contextClassLoader.getTypeFromClass(wrapper
                                    .getClass()));
                            DataRecord referencedRecord = new DataRecord(mapping.getUser(),
                                    UnsupportedDataRecordMetadata.INSTANCE);
                            for (FieldMetadata fkField : mapping.getDatabase().getFields()) {
                                if (mapping.getUser(fkField) != null) {
                                    referencedRecord.set(mapping.getUser(fkField), wrapper.get(fkField.getName()));
                                }
                            }
                            to.set(userField, referencedRecord);
                        }
                    } else {
                        List<Wrapper> wrapperList = (List<Wrapper>) value;
                        if (wrapperList != null) {
                            List<Wrapper> fullList;
                            if (value instanceof PersistentList) {
                                fullList = getFullList((PersistentList) value);
                            } else {
                                fullList = (List<Wrapper>) value;
                            }
                            for (Wrapper wrapper : fullList) {
                                if (wrapper != null) {
                                    TypeMapping mapping = mappings.getMappingFromUser(contextClassLoader.getTypeFromClass(wrapper
                                            .getClass()));
                                    DataRecord referencedRecord = new DataRecord(mapping.getUser(),
                                            UnsupportedDataRecordMetadata.INSTANCE);
                                    for (FieldMetadata fkField : ((ReferenceFieldMetadata) databaseField).getReferencedType()
                                            .getFields()) {
                                        if (mapping.getUser(fkField) != null) {
                                            referencedRecord.set(mapping.getUser(fkField), wrapper.get(fkField.getName()));
                                        }
                                    }
                                    to.set(userField, referencedRecord);
                                }
                            }
                        }
                    }
                } else {
                    if (userField.isMany()) {
                        value = getFullList((PersistentList) value);
                    }
                    to.set(userField, value);
                }
            }
        }
        // Set database specific fields
        ComplexTypeMetadata databaseType = getDatabase();
        for (FieldMetadata field : databaseType.getFields()) {
            if (getUser(field) == null) {
                String fieldName = field.getName();
                Object value = from.get(fieldName);
                DataRecordMetadata recordMetadata = to.getRecordMetadata();
                Map<String, String> recordProperties = recordMetadata.getRecordProperties();
                if (!ScatteredMappingCreator.GENERATED_ID.equals(fieldName) && value != null) {
                    try {
                        recordProperties.put(fieldName, String.valueOf(value));
                    } catch (Exception e) {
                        throw new RuntimeException("Unexpected set error.", e);
                    }
                }
            }
        }
        return to;
    }

    @Override
    public String getDatabaseTimestamp() {
        return Storage.METADATA_TIMESTAMP;
    }

    @Override
    public String getDatabaseTaskId() {
        return Storage.METADATA_TASK_ID;
    }

    // Returns actual contained type (in case in reference to hold contained record can have sub types).
    // Not expected to be use for foreign keys, and also very specific to this mapping implementation.
    private ComplexTypeMetadata getActualContainedType(FieldMetadata userField, Wrapper value) {
        Class<?> clazz = value.getClass();
        if (clazz.getName().contains("javassist")) { //$NON-NLS-1$
            clazz = clazz.getSuperclass();
        }
        ComplexTypeMetadata typeFromClass = ((StorageClassLoader) Thread.currentThread().getContextClassLoader())
                .getTypeFromClass(clazz);
        TypeMapping mappingFromDatabase = mappings.getMappingFromDatabase(typeFromClass);
        String actualValueType;
        if (mappingFromDatabase != null) {
            actualValueType = mappingFromDatabase.getUser().getName();
        } else {
            actualValueType = clazz.getSimpleName();
        }
        if (actualValueType.equalsIgnoreCase(userField.getType().getName())) {
            return (ComplexTypeMetadata) userField.getType();
        } else {
            Collection<ComplexTypeMetadata> subTypes = ((ComplexTypeMetadata) userField.getType()).getSubTypes();
            for (ComplexTypeMetadata subType : subTypes) {
                if (subType.getName().equalsIgnoreCase(actualValueType)) {
                    return subType;
                }
            }
        }
        throw new IllegalStateException("Could not set value with class '" + String.valueOf(value) + "' to field '"
                + userField.getName() + "'.");
    }

    private Wrapper createObject(ClassLoader storageClassLoader, DataRecord record) {
        try {
            TypeMapping mappingFromUser = mappings.getMappingFromUser(record.getType());
            ComplexTypeMetadata databaseReferenceType = mappingFromUser.getDatabase();
            Class<? extends Wrapper> referencedClass = ((StorageClassLoader) storageClassLoader).getClassFromType(databaseReferenceType);
            return referencedClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create wrapper object for type '" + record.getType().getName() + "'", e);
        }
    }

    private Object getReferencedObject(ClassLoader storageClassLoader, Session session, ComplexTypeMetadata referencedType,
            Object referencedIdValue) {
        Class<?> referencedClass;
        try {
            referencedClass = ((StorageClassLoader) storageClassLoader).getClassFromType(referencedType);
        } catch (Exception e) {
            throw new RuntimeException("Could not get class for type '" + referencedType.getName() + "'", e);
        }
        try {
            if (referencedIdValue == null) {
                return null; // Means no reference (reference is null).
            }
            if (referencedIdValue instanceof Wrapper) {
                return referencedIdValue; // It's already the referenced object.
            }
            // Try to load object from current session
            if (referencedIdValue instanceof List) {
                // Handle composite id values
                Serializable result;
                try {
                    Class<?> idClass = storageClassLoader.loadClass(referencedClass.getName() + "_ID"); //$NON-NLS-1$
                    Class[] parameterClasses = new Class[((List) referencedIdValue).size()];
                    int i = 0;
                    for (Object o : (List) referencedIdValue) {
                        if (o == null) {
                            throw new IllegalStateException("Id cannot have a null value.");
                        }
                        parameterClasses[i++] = o.getClass();
                    }
                    Constructor<?> constructor = idClass.getConstructor(parameterClasses);
                    result = (Serializable) constructor.newInstance(((List) referencedIdValue).toArray());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                Serializable referencedValueId = result;
                // should actually load the object to validate the FK (for Record Validation), check by ThreadLocal won't affect performance or block function
                if (DataRecord.ValidateRecord.get()) {
                    Object sessionObject = session.get(referencedClass, referencedValueId);
                    if (sessionObject == null) {
                        throw new ValidateException("Invalid foreign key: [" + referencedClass.getName() + "#" + referencedValueId //$NON-NLS-1$ //$NON-NLS-2$
                                + "] doesn't exist."); //$NON-NLS-1$
                    } else {
                        return sessionObject;
                    }
                } else {
                    Object sessionObject = session.load(referencedClass, referencedValueId);
                    if (sessionObject != null) {
                        return sessionObject;
                    }
                }
            } else if (referencedIdValue instanceof Serializable) {
                // should actually load the object to validate the FK (for Record Validation), check by ThreadLocal won't affect performance or block function
                if (DataRecord.ValidateRecord.get()) {
                    Object sessionObject = session.get(referencedClass, (Serializable) referencedIdValue);
                    if (sessionObject == null) {
                        throw new ValidateException("Invalid foreign key: [" + referencedClass.getName() + "#" + (Serializable) referencedIdValue //$NON-NLS-1$ //$NON-NLS-2$
                                + "] doesn't exist."); //$NON-NLS-1$
                    } else {
                        return sessionObject;
                    }
                } else {
                    Object sessionObject = session.load(referencedClass, (Serializable) referencedIdValue);
                    if (sessionObject != null) {
                        return sessionObject;
                    }
                }
            } else {
                throw new NotImplementedException("Unexpected state.");
            }
            Class<?> fieldJavaType = referencedIdValue.getClass();
            // Null package might happen with proxy classes generated by Hibernate
            if (fieldJavaType.getPackage() != null && fieldJavaType.getPackage().getName().startsWith("java.")) { //$NON-NLS-1$
                Wrapper referencedObject = (Wrapper) referencedClass.newInstance();
                for (FieldMetadata fieldMetadata : referencedType.getFields()) {
                    if (fieldMetadata.isKey()) {
                        referencedObject.set(fieldMetadata.getName(), referencedIdValue);
                    }
                }
                return referencedObject;
            } else {
                return referencedIdValue;
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not create referenced object of type '" + referencedClass + "' with id '"
                    + String.valueOf(referencedIdValue) + "'", e);
        }
    }

    @Override
    public String toString() {
        return "SCATTERED (" + user.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected void map(FieldMetadata user, FieldMetadata database) {
        ComplexTypeMetadata containingType = database.getContainingType();
        TypeMetadata declaringType = database.getDeclaringType();
        if (!containingType.isInstantiable() && !containingType.equals(declaringType)) {
            return;
        }
        if (isFrozen) {
            throw new IllegalStateException("Mapping is frozen.");
        }
        String userFullPath = user.getEntityTypeName() + '/' + user.getPath();
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Map '" + userFullPath + "' to '" + database.getEntityTypeName() + '/' + database.getPath());
        }
        userToDatabase.put(user, database);
        databaseToUser.put(database, user);
    }

    @Override
    public FieldMetadata getDatabase(FieldMetadata from) {
        return userToDatabase.get(from);
    }

    @Override
    public FieldMetadata getUser(FieldMetadata to) {
        return databaseToUser.get(to);
    }

    /**
     * "Freeze" both database and internal types.
     * 
     * @see TypeMetadata#freeze()
     */
    @Override
    public void freeze() {
        if (!isFrozen) {
            // Ensure mapped type are frozen.
            database.freeze();
            user.freeze();
            // Freeze field mappings.
            Map<FieldMetadata, FieldMetadata> frozenUser = new HashMap<FieldMetadata, FieldMetadata>();
            for (Map.Entry<FieldMetadata, FieldMetadata> entry : userToDatabase.entrySet()) {
                frozenUser.put(entry.getKey(), entry.getValue().freeze());
            }
            userToDatabase = frozenUser;
            Map<FieldMetadata, FieldMetadata> frozenDatabase = new HashMap<FieldMetadata, FieldMetadata>();
            for (Map.Entry<FieldMetadata, FieldMetadata> entry : databaseToUser.entrySet()) {
                frozenDatabase.put(entry.getKey().freeze(), entry.getValue().freeze());
            }
            databaseToUser = frozenDatabase;
            isFrozen = true;
        }
    }
}
