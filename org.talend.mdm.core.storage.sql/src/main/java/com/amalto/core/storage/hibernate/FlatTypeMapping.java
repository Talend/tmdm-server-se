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

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.amalto.core.storage.record.StorageConstants;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;

import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.DataRecordMetadata;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import com.amalto.core.util.ValidateException;

class FlatTypeMapping extends TypeMapping {

    private static final Logger LOGGER = Logger.getLogger(FlatTypeMapping.class);

    private Map<String, FieldMetadata> userToDatabase = new HashMap<String, FieldMetadata>();

    private Map<String, FieldMetadata> databaseToUser = new HashMap<String, FieldMetadata>();

    public FlatTypeMapping(ComplexTypeMetadata complexType, MappingRepository mappings) {
        super(complexType, mappings);
    }

    public FlatTypeMapping(ComplexTypeMetadata complexType, ComplexTypeMetadata database, MappingRepository mappings) {
        super(complexType, database, mappings);
    }

    @Override
    protected void map(FieldMetadata user, FieldMetadata database) {
        if (isFrozen) { // Not really expected, but as long as calling code knows what it's doing...
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Mapping for '" + user.getName() + "' is frozen"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        userToDatabase.put(getKey(user), database);
        databaseToUser.put(getKey(database), user);
    }

    private static String getKey(FieldMetadata field) {
        return field.getDeclaringType().getName() + '_' + field.getName();
    }

    @Override
    public FieldMetadata getDatabase(FieldMetadata from) {
        FieldMetadata field = userToDatabase.get(getKey(from));
        // TMDM-6802: Look for field by name (declaring type might be wrong).
        if (field == null) {
            for (FieldMetadata fieldMetadata : userToDatabase.values()) {
                if (fieldMetadata.getName().endsWith(from.getName().toLowerCase())) {
                    return fieldMetadata;
                }
            }
        }
        return field;
    }

    @Override
    public FieldMetadata getUser(FieldMetadata to) {
        return databaseToUser.get(getKey(to));
    }

    @Override
    public void freeze() {
        if (!isFrozen) {
            // Ensure mapped type are frozen.
            try {
                database.freeze();
            } catch (Exception e) {
                throw new RuntimeException("Could not process internal type '" + database.getName() + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            try {
                user.freeze();
            } catch (Exception e) {
                throw new RuntimeException("Could not process user type '" + user.getName() + "'.", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            // Freeze field mappings.
            userToDatabase = freezeFields(userToDatabase);
            databaseToUser = freezeFields(databaseToUser);
            isFrozen = true;
        }
    }

    private static Map<String, FieldMetadata> freezeFields(Map<String, FieldMetadata> fieldMap) {
        Map<String, FieldMetadata> frozen;
        frozen = new HashMap<String, FieldMetadata>();
        for (Map.Entry<String, FieldMetadata> entry : fieldMap.entrySet()) {
            FieldMetadata frozenField = entry.getValue().freeze();
            frozen.put(entry.getKey(), frozenField);
        }
        return frozen;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void setValues(Session session, DataRecord from, Wrapper to) {
        try {
            if (from == null) {
                return;
            }
            List<FieldMetadata> fields = getFields(from.getType());
            for (FieldMetadata field : fields) {
                // Note: database field might be null (would mean this field can be safely ignored in this mapping).
                FieldMetadata databaseField = getDatabase(field);
                if (databaseField == null) {
                    continue;
                }
                Object value = readValue(from, field, databaseField, session);
                // "instance of" could be replaced by visitor on field... but is a bit too much for this simple step.
                if (field instanceof SimpleTypeFieldMetadata || field instanceof EnumerationFieldMetadata) {
                    if (!field.isMany()) {
                        to.set(databaseField.getName(), value);
                    } else {
                        List list = (List) to.get(databaseField.getName());
                        if (list == null) {
                            to.set(databaseField.getName(), value);
                        } else {
                            if (value != null) {
                                resetList(list, (List) value);
                            } else {
                                list.clear();
                            }
                        }
                    }
                } else if (field instanceof ReferenceFieldMetadata) {
                    StorageClassLoader storageClassLoader = (StorageClassLoader) Thread.currentThread().getContextClassLoader();
                    if (!field.isMany()) {
                        if (value != null && value instanceof DataRecord) { //$NON-NLS-1$
                            DataRecord dataRecordValue = (DataRecord) value;
                            TypeMetadata referencedType = dataRecordValue.getType();
                            Class<?> referencedClass = storageClassLoader.findClass(referencedType.getName());
                            Object referencedObject = createReferencedObject(session, (ComplexTypeMetadata) referencedType, referencedClass, dataRecordValue);
                            Object databaseValue = to.get(databaseField.getName());
                            if (databaseValue == null || !referencedObject.equals(databaseValue)) {
                                to.set(databaseField.getName(), referencedObject);
                            }
                        } else {
                            to.set(databaseField.getName(), null);
                        }
                    } else {
                        List<Object> list = (List<Object>) to.get(databaseField.getName());
                        if (list == null) {
                            list = new LinkedList<Object>();
                            to.set(databaseField.getName(), list);
                        }
                        if (value != null) {
                            List<DataRecord> valueList = (List<DataRecord>) value;
                            List<Object> newValues = new LinkedList<Object>();
                            for (DataRecord current : valueList) {
                                TypeMetadata referencedType = current.getType();
                                Class<?> referencedClass = storageClassLoader.findClass(referencedType.getName());
                                newValues.add(createReferencedObject(session, (ComplexTypeMetadata) referencedType, referencedClass, current));
                            }
                            resetList(list, newValues);
                        } else {
                            list.clear();
                        }
                    }
                }
            }
            to.taskId(from.getRecordMetadata().getTaskId());
            to.timestamp(from.getRecordMetadata().getLastModificationTime());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<FieldMetadata> getFields(ComplexTypeMetadata type) {
        Collection<FieldMetadata> fields = type.getFields();
        List<FieldMetadata> returnedFields = new LinkedList<FieldMetadata>(fields);
        for (FieldMetadata field : fields) {
            if (field instanceof ContainedTypeFieldMetadata) {
                returnedFields.addAll(getFields(((ContainedTypeFieldMetadata) field).getContainedType()));
            }
        }
        return returnedFields;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public DataRecord setValues(Wrapper from, DataRecord to) {
        if (from == null) {
            return to;
        }
        StorageClassLoader contextClassLoader = (StorageClassLoader) Thread.currentThread().getContextClassLoader();
        ComplexTypeMetadata typeFromClass = contextClassLoader.getTypeFromClass(from.getClass());
        for (FieldMetadata field : typeFromClass.getFields()) {
            FieldMetadata userField = getUser(field);
            String fieldName = field.getName();
            Object value = readValue(from, field, userField);
            if (userField != null) {
                DataRecord previous = to;
                if (userField.getContainingType() != getUser()) {
                    Iterator<FieldMetadata> path = StorageMetadataUtils.path(getUser(), userField).iterator();
                    if (!path.hasNext()) {
                        throw new IllegalStateException("No path found from '" + getUser().getName() + "' to field '" + userField.getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    }
                    while (path.hasNext()) {
                        FieldMetadata nextField = path.next();
                        if (path.hasNext()) {
                            DataRecord containedRecord = (DataRecord) to.get(nextField);
                            if (containedRecord == null) {
                                ComplexTypeMetadata type;
                                if (nextField instanceof ContainedTypeFieldMetadata) {
                                    type = (ComplexTypeMetadata) nextField.getType();
                                } else if (nextField instanceof ReferenceFieldMetadata) {
                                    type = ((ReferenceFieldMetadata) nextField).getReferencedType();
                                } else {
                                    throw new IllegalArgumentException("Did not expect an instance of '" + nextField.getClass().getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                                containedRecord = new DataRecord(type, UnsupportedDataRecordMetadata.INSTANCE);
                                to.set(nextField, containedRecord);
                            }
                            to = containedRecord;
                        }
                    }
                }
                if (userField instanceof ContainedTypeFieldMetadata) {
                    // This mapping is not supposed to handle such cases (there's no field in type's fields mapped to a contained type).
                    throw new IllegalArgumentException("This mapping does not support contained types."); //$NON-NLS-1$
                } else if (userField instanceof ReferenceFieldMetadata) {
                    if (!userField.isMany()) {
                        if (value != null) {
                            to.set(userField, getDataRecordFromWrapper(contextClassLoader, (Wrapper) value, new HashMap<Object, DataRecord>()));
                        }
                    } else {
                        List<Wrapper> wrapperList = getFullList((List<Wrapper>) value);
                        if (wrapperList != null) {
                            for (Wrapper wrapper : wrapperList) {
                                to.set(userField, getDataRecordFromWrapper(contextClassLoader, wrapper, new HashMap<Object, DataRecord>()));
                            }
                        }
                    }
                } else {
                    if (userField.isMany()) {
                        if(value == null){
                            value = new ArrayList<>(0);
                        }
                        else {
                            value = new ArrayList<>(getFullList((List) value));
                        }
                    }
                    to.set(userField, value);
                }
                to = previous;
            } else {
                DataRecordMetadata recordMetadata = to.getRecordMetadata();
                Map<String, String> recordProperties = recordMetadata.getRecordProperties();
                if (value != null) {
                    recordProperties.put(fieldName, String.valueOf(value));
                }
            }
        }
        return to;
    }
    
    @SuppressWarnings("unchecked")
    private DataRecord getDataRecordFromWrapper(StorageClassLoader contextClassLoader, Wrapper wrapper, Map<Object, DataRecord> proceedWrappers) {
    	DataRecord dataRecord = proceedWrappers.get(wrapper);
    	if(dataRecord == null) {
	        TypeMapping mapping = mappings.getMappingFromUser(contextClassLoader.getTypeFromClass(wrapper.getClass()));
	        dataRecord = new DataRecord(mapping.getUser(), UnsupportedDataRecordMetadata.INSTANCE);
	        proceedWrappers.put(wrapper, dataRecord);
	        for (FieldMetadata field : mapping.getDatabase().getFields()) {
	            FieldMetadata fieldMetadata = mapping.getUser(field);
	            if (fieldMetadata != null) {
	                Object valueObject = wrapper.get(field.getName());
	                if (valueObject != null) {
	                    List<Object> valueList = new ArrayList<Object>();
	                    if(fieldMetadata.isMany()){
	                        valueList.addAll((List<Object>) valueObject);
	                    } else {
	                        valueList.add(valueObject);
	                    }
						for (Object value : valueList) {
                            if (fieldMetadata instanceof ReferenceFieldMetadata) {
								dataRecord.set(fieldMetadata, getDataRecordFromWrapper(contextClassLoader, (Wrapper) value, proceedWrappers));
                            } else if (fieldMetadata instanceof ContainedTypeFieldMetadata) {
                                TypeMapping newMapping = mappings.getMappingFromUser(ContainedComplexTypeMetadata.contain(contextClassLoader.getTypeFromClass(((Wrapper) value).getClass()), fieldMetadata));
                                DataRecord newdataRecord = new DataRecord(newMapping.getUser(), UnsupportedDataRecordMetadata.INSTANCE);
                                proceedWrappers.put((Wrapper) value, newdataRecord);
                                for (FieldMetadata mappingField : newMapping.getDatabase().getFields()) {
                                    FieldMetadata mappingFieldMetadata = newMapping.getUser(mappingField);
                                    if (mappingFieldMetadata != null) {
                                        Object newValueObject;
                                        if (mappingFieldMetadata instanceof ContainedTypeFieldMetadata) {
                                            for (FieldMetadata fm : ((ContainedTypeFieldMetadata)mappingFieldMetadata).getContainedType().getFields()) {
                                                if (newMapping.getDatabase(fm) != null) {
                                                    newValueObject = ((Wrapper) value).get(newMapping.getDatabase(fm).getName());
                                                    if (newValueObject != null) {
                                                        List<Object> newValueList = new ArrayList<Object>();
                                                        if(fm.isMany()){
                                                            newValueList.addAll((List<Object>) newValueObject);
                                                        } else {
                                                            newValueList.add(newValueObject);
                                                        }
                                                        for (Object newValue : newValueList) {
                                                            if (fm instanceof ReferenceFieldMetadata) {
                                                                dataRecord.set(fm, getDataRecordFromWrapper(contextClassLoader, (Wrapper) newValue, proceedWrappers));
                                                            } else {
                                                                dataRecord.set(fm, newValue);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            newValueObject = ((Wrapper) value).get(mappingFieldMetadata.getName());
                                            if (newValueObject != null) {
                                                List<Object> newValueList = new ArrayList<Object>();
                                                if(mappingFieldMetadata.isMany()){
                                                    newValueList.addAll((List<Object>) newValueObject);
                                                } else {
                                                    newValueList.add(newValueObject);
                                                }
                                                for (Object newValue : newValueList) {
                                                    if (mappingFieldMetadata instanceof ReferenceFieldMetadata) {
                                                        dataRecord.set(mappingFieldMetadata, getDataRecordFromWrapper(contextClassLoader, (Wrapper) newValue, proceedWrappers));
                                                    } else {
                                                        dataRecord.set(mappingFieldMetadata, newValue);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
	                        } else {
                                dataRecord.set(fieldMetadata, value);
                            }
	                    }
	                }
	            }
	        }
        }
        return dataRecord;
    }

    @Override
    public String getDatabaseTimestamp() {
        return StorageConstants.METADATA_TIMESTAMP;
    }

    @Override
    public String getDatabaseTaskId() {
        return StorageConstants.METADATA_TASK_ID;
    }

    private Object createReferencedObject(Session session, ComplexTypeMetadata referencedType, Class<?> referencedClass, Object referencedIdValue) throws InstantiationException, IllegalAccessException {
        if (referencedIdValue == null) {
            return null; // Means no reference (reference is null).
        }
        if (referencedIdValue instanceof Wrapper) {
            return referencedIdValue; // It's already the referenced object.
        }
        // Try to load object from current session
        if (referencedIdValue instanceof DataRecord) {
            Serializable referencedValueId;
            DataRecord idAsDataRecord = (DataRecord) referencedIdValue;
            Collection<FieldMetadata> keyFields = idAsDataRecord.getType().getKeyFields();
            if (keyFields.size() == 1) {
                referencedValueId = (Serializable) idAsDataRecord.get(keyFields.iterator().next());
            } else {
                ClassLoader storageClassLoader = Thread.currentThread().getContextClassLoader();
                List<Object> ids = new LinkedList<Object>();
                for (FieldMetadata keyField : keyFields) {
                    ids.add(idAsDataRecord.get(keyField));
                }
                referencedValueId = createCompositeId(storageClassLoader, referencedClass, ids);
            }
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
        } else {
            throw new NotImplementedException("Unexpected state."); //$NON-NLS-1$
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
    }

    @SuppressWarnings("rawtypes")
    private static Serializable createCompositeId(ClassLoader classLoader, Class<?> clazz, List<Object> compositeIdValues) {
        try {
            Class<?> idClass = classLoader.loadClass(clazz.getName() + "_ID"); //$NON-NLS-1$
            Class[] parameterClasses = new Class[compositeIdValues.size()];
            int i = 0;
            for (Object o : compositeIdValues) {
                parameterClasses[i++] = o.getClass();
            }
            Constructor<?> constructor = idClass.getConstructor(parameterClasses);
            return (Serializable) constructor.newInstance(compositeIdValues.toArray());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "FLAT (" + user.getName() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
