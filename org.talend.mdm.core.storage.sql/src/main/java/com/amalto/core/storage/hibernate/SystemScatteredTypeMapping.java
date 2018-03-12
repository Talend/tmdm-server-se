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

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.DataRecordMetadata;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import org.apache.commons.lang.NotImplementedException;
import org.hibernate.Session;
import org.hibernate.collection.internal.PersistentList;
import org.talend.mdm.commmon.metadata.*;

import java.io.*;
import java.lang.reflect.Constructor;
import java.util.*;

/**
 * Represents type mapping between data model as specified by the user and data model as used by hibernate storage.
 */
class SystemScatteredTypeMapping extends ScatteredTypeMapping {

    private Map<String, FieldMetadata> userToDatabase = new HashMap<String, FieldMetadata>();

    private Map<String, FieldMetadata> databaseToUser = new HashMap<String, FieldMetadata>();

    public SystemScatteredTypeMapping(ComplexTypeMetadata user, MappingRepository mappings) {
        super(user, mappings);
    }

    public void setValues(Session session, DataRecord from, Wrapper to) {
        _setValues(session, from, to);
    }

    Object _setValues(Session session, DataRecord from, Wrapper to) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        TypeMapping mapping = mappings.getMappingFromUser(from.getType());
        Collection<FieldMetadata> fields;
        if (mapping != null) {
            fields = mapping.getUser().getFields();
        } else {
            fields = from.getType().getFields();
        }
        for (FieldMetadata field : fields) {
            FieldMetadata mappedDatabaseField;
            if (mapping != null) {
                mappedDatabaseField = mapping.getDatabase(field);
            } else {
                mappedDatabaseField = getDatabase(field);
            }
            if (mappedDatabaseField == null) {
                continue;
                // throw new IllegalStateException("Field '" + field.getName() + "' was expected to have a database mapping");
            }
            if (field instanceof ContainedTypeFieldMetadata) {
                if (!(mappedDatabaseField instanceof ReferenceFieldMetadata)) {
                    throw new IllegalStateException("Contained elements are expected to be mapped to reference.");
                }
                ReferenceFieldMetadata referenceFieldMetadata = (ReferenceFieldMetadata) mappedDatabaseField;
                if (!field.isMany()) {
                    DataRecord referencedObject = (DataRecord) readValue(from, field, mappedDatabaseField, session);
                    if (referencedObject != null) {
                        TypeMapping mappingFromUser = mappings.getMappingFromUser(referencedObject.getType());
                        ComplexTypeMetadata referencedType = mappingFromUser != null ? mappingFromUser.getDatabase() : referencedObject.getType();
                        Wrapper existingValue = (Wrapper) to.get(referenceFieldMetadata.getName());
                        boolean needCreate = existingValue == null;
                        if (!needCreate) {
                            ComplexTypeMetadata existingType = ((StorageClassLoader) contextClassLoader).getTypeFromClass(existingValue.getClass());
                            needCreate = !existingType.equals(referencedType);
                        }
                        Wrapper object = needCreate ? createObject(contextClassLoader, referencedType) : existingValue;
                        to.set(referenceFieldMetadata.getName(), _setValues(session, referencedObject, object));
                        if (needCreate) {
                            session.persist(object);
                        }
                    } else {
                        to.set(referenceFieldMetadata.getName(), null);
                    }
                } else {
                    List<DataRecord> dataRecords = (List<DataRecord>) readValue(from, field, mappedDatabaseField, session);
                    Object value;
                    if (mapping != null) {
                        value = to.get(mapping.getDatabase(field).getName());
                    } else {
                        value = to.get(getDatabase(field).getName());
                    }
                    if (dataRecords != null) {
                        List<Wrapper> existingValue = (List<Wrapper>) value;
                        if (existingValue != null) {
                            ((PersistentList) existingValue).forceInitialization();
                        }
                        List<Wrapper> objects = existingValue == null ? new ArrayList<Wrapper>(dataRecords.size()) : existingValue;
                        int i = 0;
                        for (DataRecord dataRecord : dataRecords) {
                            if (i < objects.size() && objects.get(i) != null) {
                                objects.set(i, (Wrapper) _setValues(session, dataRecord, objects.get(i)));
                            } else {
                                Wrapper object = createObject(contextClassLoader, dataRecord.getType());
                                objects.add((Wrapper) _setValues(session, dataRecord, object));
                                session.persist(object);
                            }
                            i++;
                        }
                        // TMDM-5257: Remove the deleted items
                        while (objects.size() > dataRecords.size()) {
                            objects.remove(objects.size() - 1);
                        }
                        to.set(referenceFieldMetadata.getName(), objects);
                    } else {
                        if (value != null && value instanceof List) {
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
                        to.set(mappedDatabaseField.getName(), getReferencedObject(contextClassLoader, session, mappings.getMappingFromUser(referencedObject.getType()).getDatabase(), referenceId));
                    } else {
                        to.set(mappedDatabaseField.getName(), null);
                    }
                } else {
                    List<DataRecord> referencedObjectList = (List<DataRecord>) readValue(from, field, mappedDatabaseField, session);
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

    public DataRecord setValues(Wrapper from, DataRecord to) {
        StorageClassLoader contextClassLoader = (StorageClassLoader) Thread.currentThread().getContextClassLoader();
        ComplexTypeMetadata typeFromClass = contextClassLoader.getTypeFromClass(from.getClass());
        for (FieldMetadata field : typeFromClass.getFields()) {
            FieldMetadata userField = getUser(field);
            String fieldName = field.getName();
            Object value = readValue(from, field, userField);
            if (userField != null) {
                if (userField instanceof ContainedTypeFieldMetadata) {
                    if (!userField.isMany()) {
                        Wrapper valueAsWrapper = (Wrapper) value;
                        if (value != null) {
                            DataRecord containedDataRecord = new DataRecord(getActualContainedType(userField, valueAsWrapper), UnsupportedDataRecordMetadata.INSTANCE);
                            to.set(userField, setValues(valueAsWrapper, containedDataRecord));
                        }
                    } else {
                        List<Wrapper> wrapperList = (List<Wrapper>) value;
                        if (wrapperList != null) {
                            List<Wrapper> fullList = getFullList((PersistentList) value);
                            for (Wrapper wrapper : fullList) {
                                if (wrapper != null) {
                                    to.set(userField, setValues(wrapper, new DataRecord(getActualContainedType(userField, wrapper), UnsupportedDataRecordMetadata.INSTANCE)));
                                }
                            }
                        }
                    }
                } else if (userField instanceof ReferenceFieldMetadata) {
                    if (!userField.isMany()) {
                        Wrapper wrapper = (Wrapper) value;
                        if (wrapper != null) {
                            TypeMapping mapping = mappings.getMappingFromUser(contextClassLoader.getTypeFromClass(wrapper.getClass()));
                            DataRecord referencedRecord = new DataRecord(mapping.getUser(), UnsupportedDataRecordMetadata.INSTANCE);
                            for (FieldMetadata fkField : ((ReferenceFieldMetadata) field).getReferencedType().getFields()) {
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
                                    TypeMapping mapping = mappings.getMappingFromUser(contextClassLoader.getTypeFromClass(wrapper.getClass()));
                                    DataRecord referencedRecord = new DataRecord(mapping.getUser(), UnsupportedDataRecordMetadata.INSTANCE);
                                    for (FieldMetadata fkField : ((ReferenceFieldMetadata) field).getReferencedType().getFields()) {
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
            } else {
                DataRecordMetadata recordMetadata = to.getRecordMetadata();
                Map<String,String> recordProperties = recordMetadata.getRecordProperties();
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
        ComplexTypeMetadata typeFromClass = ((StorageClassLoader) Thread.currentThread().getContextClassLoader()).getTypeFromClass(clazz);
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
        throw new IllegalStateException("Could not set value with class '" + String.valueOf(value) + "' to field '" + userField.getName() + "'.");
    }

    private Wrapper createObject(ClassLoader storageClassLoader, ComplexTypeMetadata referencedType) {
        try {
            TypeMapping mappingFromUser = mappings.getMappingFromUser(referencedType);
            Class<? extends Wrapper> referencedClass;
            if (mappingFromUser != null) {
                ComplexTypeMetadata databaseReferenceType = mappingFromUser.getDatabase();
                referencedClass = ((StorageClassLoader) storageClassLoader).getClassFromType(databaseReferenceType);
            } else {
                referencedClass = ((StorageClassLoader) storageClassLoader).getClassFromType(referencedType);
            }
            return referencedClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Could not create wrapper object for type '" + referencedType.getName() + "'", e);
        }
    }

    private Object getReferencedObject(ClassLoader storageClassLoader, Session session, ComplexTypeMetadata referencedType, Object referencedIdValue) {
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

                Object sessionObject = session.load(referencedClass, referencedValueId);
                if (sessionObject != null) {
                    return sessionObject;
                }
            } else if (referencedIdValue instanceof Serializable) {
                Object sessionObject = session.load(referencedClass, (Serializable) referencedIdValue);
                if (sessionObject != null) {
                    return sessionObject;
                }
            } else {
                throw new NotImplementedException("Unexpected state.");
            }
            Class<?> fieldJavaType = referencedIdValue.getClass();
            // Null package might happen with proxy classes generated by Hibernate
            if (fieldJavaType.getPackage() != null && fieldJavaType.getPackage().getName().startsWith("java.")) {  //$NON-NLS-1$
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
            throw new RuntimeException("Could not create referenced object of type '" + referencedClass + "' with id '" + String.valueOf(referencedIdValue) + "'", e);
        }
    }

    @Override
    public String toString() {
        return "SYSTEM SCATTERED (" + user.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected void map(FieldMetadata user, FieldMetadata database) {
        if (isFrozen) {
            throw new IllegalStateException("Mapping is frozen.");
        }
        userToDatabase.put(user.getContainingType().getName() + '_' + user.getName(), database);
        databaseToUser.put(database.getContainingType().getName() + '_' + database.getName(), user);
    }

    public FieldMetadata getDatabase(FieldMetadata from) {
        return userToDatabase.get(from.getContainingType().getName() + '_' + from.getName());
    }

    public FieldMetadata getUser(FieldMetadata to) {
        return databaseToUser.get(to.getContainingType().getName() + '_' + to.getName());
    }

    /**
     * "Freeze" both database and internal types.
     *
     * @see TypeMetadata#freeze()
     */
    public void freeze() {
        if (!isFrozen) {
            // Ensure mapped type are frozen.
            database.freeze();
            user.freeze();
            // Freeze field mappings.
            Map<String, FieldMetadata> frozen = new HashMap<String, FieldMetadata>();
            for (Map.Entry<String, FieldMetadata> entry : userToDatabase.entrySet()) {
                frozen.put(entry.getKey(), entry.getValue().freeze());
            }
            userToDatabase = frozen;
            frozen = new HashMap<String, FieldMetadata>();
            for (Map.Entry<String, FieldMetadata> entry : databaseToUser.entrySet()) {
                frozen.put(entry.getKey(), entry.getValue().freeze());
            }
            databaseToUser = frozen;
            isFrozen = true;
        }
    }
}