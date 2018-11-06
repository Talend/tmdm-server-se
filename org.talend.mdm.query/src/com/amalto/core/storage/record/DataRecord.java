/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.record;

import com.amalto.core.storage.record.metadata.DataRecordMetadata;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;

import java.util.*;

public class DataRecord {

    private final Map<FieldMetadata, Object> fieldToValue = new LinkedHashMap<FieldMetadata, Object>();

    private final DataRecordMetadata recordMetadata;

    private ComplexTypeMetadata type;

    /**
     * @param type Type as {@link ComplexTypeMetadata} of the data record
     * @param recordMetadata Record specific metadata (e.g. last modification timestamp...)
     */
    public DataRecord(ComplexTypeMetadata type, DataRecordMetadata recordMetadata) {
        this.type = type;
        this.recordMetadata = recordMetadata;
    }

    /**
     * @param record The {@link com.amalto.core.storage.record.DataRecord record} to copy.
     * @return A deep copy of the <code>record</code>, fields are also copied.
     */
    public static DataRecord copy(DataRecord record) {
        DataRecord copy = new DataRecord(record.getType(), record.getRecordMetadata().copy());
        for (Map.Entry<FieldMetadata, Object> entry : record.fieldToValue.entrySet()) {
            Object value = record.get(entry.getKey());
            if (value instanceof DataRecord) {
                value = DataRecord.copy((DataRecord) value);
            } else if (value instanceof List) {
                value = new ArrayList<Object>((Collection<?>) value);
            }
            copy.set(entry.getKey(), value);
        }
        return copy;
    }

    /**
     * Returns a serialized version of the {@link com.amalto.core.storage.record.DataRecord record} id.
     * 
     * @param dataRecord The data record to serialize id from.
     * @return An id serialized in the following format [id0] (for single key) [id0][...][idN] (for composite keys) or
     * empty string if no key is defined.
     */
    public static String getId(DataRecord dataRecord) {
        if (dataRecord == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (FieldMetadata keyField : dataRecord.getType().getKeyFields()) {
            builder.append('[').append(dataRecord.get(keyField)).append(']');
        }
        return builder.toString();
    }

    /**
     * Returns a typed version of the provided <code>id</code> based on <code>type</code>.
     * @param type A {@link org.talend.mdm.commmon.metadata.ComplexTypeMetadata type}.
     * @param id A serialized id (in the form of [id0]...[idN]).
     * @return A typed version of the id (each item of the return is typed based on key field type).
     */
    public static Object[] parseId(ComplexTypeMetadata type, String id) {
        FieldMetadata[] keyFields = type.getKeyFields().toArray(new FieldMetadata[type.getKeyFields().size()]);
        Object[] keyValues = new Object[keyFields.length];
        StringTokenizer tokenizer = new StringTokenizer(id, "]"); //$NON-NLS-1$
        int i = 0;
        while (tokenizer.hasMoreTokens()) {
            String currentKeyValue = tokenizer.nextToken().substring(1);
            keyValues[i] = CommonStorageMetaDataUtils.convert(currentKeyValue, keyFields[i]);
            i++;
        }
        return keyValues;
    }

    public ComplexTypeMetadata getType() {
        return type;
    }

    /**
     * @param type The new type of this data record. New type <b>MUST</b> be a sub type of current type.
     */
    public void setType(ComplexTypeMetadata type) {
        // TODO Check if type is eligible
        this.type = type;
    }

    public DataRecordMetadata getRecordMetadata() {
        return recordMetadata;
    }

    /**
     * <p>
     * Get <b>a</b> value for a <code>field</code>. This method the first value to be found, so in case the field is present many
     * different times inside record (for example if field is contained in repeatable elements) this method does not aggregate
     * all values.
     * </p>
     * <p>
     * For a method that aggregates all values, please look at {@link #find(org.talend.mdm.commmon.metadata.FieldMetadata)};
     * </p>
     *
     * @param field A {@link org.talend.mdm.commmon.metadata.FieldMetadata field} contained in record.
     * @return The value of the field in this record or <code>null</code> if no value is set for field.
     */
    public Object get(FieldMetadata field) {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null.");
        }
        ComplexTypeMetadata containingType = field.getContainingType();
        if (containingType != this.getType() && !this.getType().isAssignableFrom(containingType)) {
            if (fieldToValue.containsKey(field)) {
                return fieldToValue.get(field);
            } else if (recordMetadata.getRecordProperties().containsKey(field.getName())) { // Try to read from metadata
                return recordMetadata.getRecordProperties().get(field.getName());
            }
            Iterator<FieldMetadata> path = CommonStorageMetaDataUtils.path(type, field, false).iterator();
            if (!path.hasNext()) {
                Object value = get(field.getName());
                if (value != null) { // Support explicit projection type fields
                    return value;
                }
                throw new IllegalArgumentException("Field '" + field.getName() + "' isn't reachable from type '" + type.getName()
                        + "'");
            }
            DataRecord current = this;
            while (path.hasNext()) {
                FieldMetadata nextField = path.next();
                Object nextObject = current.fieldToValue.get(nextField);
                if (nextObject == null) {
                    return null;
                }
                if (path.hasNext()) {
                    if (!(nextObject instanceof DataRecord)) {
                        if (!path.hasNext()) {
                            return nextObject;
                        } else if (nextObject instanceof List) {
                            // TODO This is maybe (surely?) not what user expect, but there's no way to select the nth
                            // instance of a collection in query API.
                            nextObject = ((List) nextObject).get(0);
                        } else {
                            return nextObject;
                        }
                    }
                    current = (DataRecord) nextObject;
                } else {
                    return nextObject;
                }
            }
            return null; // Not found.
        } else {
            if (fieldToValue.containsKey(field)) {
                return fieldToValue.get(field);
            } else if (recordMetadata.getRecordProperties().containsKey(field.getName())) { // Try to read from metadata
                return recordMetadata.getRecordProperties().get(field.getName());
            }
            // Last chance for finding value: reused field might not be hashCode-equals so does a by-name lookup.
            for (Map.Entry<FieldMetadata, Object> entry : fieldToValue.entrySet()) {
                if (field.getName().equals(entry.getKey().getName())) {
                    return entry.getValue();
                }
            }
            return null;
        }
    }

    public Object get(String fieldName) {
        StringTokenizer tokenizer = new StringTokenizer(fieldName, "/"); //$NON-NLS-1$
        DataRecord current = this;
        Object currentValue = null;
        while (tokenizer.hasMoreTokens()) {
            String currentPathElement = tokenizer.nextToken();
            FieldMetadata field = current.getType().getField(currentPathElement);
            currentValue = current.get(field);
            if (tokenizer.hasMoreTokens()) {
                if (currentValue == null) {
                    // Means record does not own field last call to "tokenizer.nextToken()" returned.
                    return null;
                } else if (!field.isMany()) {
                    current = (DataRecord) currentValue;
                } else { // Repeatable element
                    current = (DataRecord) ((List) currentValue).get(0); // TODO No way to specify index in API.
                }
            }
        }
        return currentValue;
    }

    public List<Object> find(FieldMetadata field) {
        // Build path elements
        String path = field.getPath();
        StringTokenizer tokenizer = new StringTokenizer(path, "/"); //$NON-NLS-1$
        List<String> pathElements = new ArrayList<String>(tokenizer.countTokens());
        while (tokenizer.hasMoreTokens()) {
            pathElements.add(tokenizer.nextToken());
        }
        List<Object> values = new LinkedList<Object>();
        _find(values, this, pathElements);
        return values;
    }

    private void _find(List<Object> values, DataRecord current, List<String> pathElements) {
        String fieldName = pathElements.get(0);
        if (!current.getType().hasField(fieldName)) {
            return; // TODO Error?
        }
        FieldMetadata fieldMetadata = current.getType().getField(fieldName);
        Object fieldValue = current.fieldToValue.get(fieldMetadata);
        if (pathElements.size() == 1) {
            if (fieldMetadata.isMany()) {
                values.addAll((Collection<?>) fieldValue);
            } else {
                values.add(fieldValue);
            }
        } else {
            if (fieldValue == null) {
                return;
            }
            List<DataRecord> subRecords;
            if (fieldMetadata.isMany()) {
                subRecords = (List<DataRecord>) fieldValue;
            } else {
                subRecords = Collections.singletonList((DataRecord) fieldValue);
            }
            List<String> subPathElements = pathElements.subList(1, pathElements.size());
            for (DataRecord record : subRecords) {
                _find(values, record, subPathElements);
            }
        }
    }

    public void set(FieldMetadata field, Object o) {
        if (field == null) {
            throw new IllegalArgumentException("Field can not be null.");
        }
        if (!field.isMany()) {
            fieldToValue.put(field, o);
        } else {
            List list = (List) fieldToValue.get(field);
            // fields that may contain data records.
            if (field instanceof ReferenceFieldMetadata || field instanceof ContainedTypeFieldMetadata) {
                if (o != null) {
                    if (list == null) {
                        list = new LinkedList();
                        fieldToValue.put(field, list);
                    }
                    if (o instanceof Collection) {
                        list.addAll((Collection) o);
                    } else {
                        list.add(o);
                    }
                } else {
                    fieldToValue.put(field, o);
                }
            } else {
                if (list == null && o instanceof List) {
                    fieldToValue.put(field, o);
                } else {
                    if (list == null) {
                        list = new LinkedList();
                        fieldToValue.put(field, list);
                    }
                    list.add(o);
                }
            }
        }
    }

    public Set<FieldMetadata> getSetFields() {
        return fieldToValue.keySet();
    }

    public boolean isEmpty() {
        for (Map.Entry<FieldMetadata, Object> entry : fieldToValue.entrySet()) {
            if (entry.getValue() != null) {
                if (entry.getKey() instanceof ContainedTypeFieldMetadata) {
                    if (entry.getKey().isMany()) {
                        List<DataRecord> list = (List<DataRecord>) entry.getValue();
                        for (DataRecord o : list) {
                            if (!o.isEmpty()) {
                                return false;
                            }
                        }
                    } else {
                        if (!((DataRecord) entry.getValue()).isEmpty()) {
                            return false;
                        }
                    }
                } else {
                    if (entry.getKey().isMany()) {
                        List list = (List) entry.getValue();
                        boolean containsOnlyNulls = true;
                        for (Object o : list) {
                            containsOnlyNulls &= (o == null);
                        }
                        if (!list.isEmpty() && !containsOnlyNulls) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataRecord)) {
            return false;
        }
        DataRecord that = (DataRecord) o;
        for (FieldMetadata key : fieldToValue.keySet()) {
            if (key.isKey()) {
                if (!fieldToValue.get(key).equals(that.fieldToValue.get(key))) {
                    return false;
                }
            }
        }
        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        for (FieldMetadata key : fieldToValue.keySet()) {
            if (key.isKey()) {
                result += fieldToValue.get(key).hashCode();
            }
        }
        return result;
    }

    public void remove(FieldMetadata field) {
        fieldToValue.remove(field);
    }

    @Override
    public String toString() {
        StringBuilder keyValue = new StringBuilder();
        for (FieldMetadata keyField : type.getKeyFields()) {
            keyValue.append('[').append(fieldToValue.get(keyField)).append(']');
        }
        return "Record {" + "type=" + type.getName() + ",key=" + keyValue.toString() + '}';
    }
    
    /**
     * Used to control if check existence from DB when update a record (like: bulkload won't check to avoid executing too many SELECT
     * statements)
     * 
     */
    public static class CheckExistence {

        private static ThreadLocal<Boolean> threadLocal = new ThreadLocal<Boolean>() {

            public Boolean initialValue() {
                return Boolean.TRUE;
            }
        };

        private CheckExistence() {
        }

        public static void set(boolean value) {
            threadLocal.set(value);
        }

        public static boolean get() {
            return threadLocal.get();
        }

        public static void remove() {
            threadLocal.remove();
        }
    }

    /**
     * Used to control if only validate a record but not save it.
     *
     */
    public static class ValidateRecord {

        private static ThreadLocal<Boolean> threadLocal = new ThreadLocal<Boolean>() {

            public Boolean initialValue() {
                return Boolean.FALSE;
            }
        };

        private ValidateRecord() {
        }

        public static void set(boolean value) {
            threadLocal.set(value);
        }

        public static boolean get() {
            return threadLocal.get();
        }

        public static void remove() {
            threadLocal.remove();
        }
    }
}
