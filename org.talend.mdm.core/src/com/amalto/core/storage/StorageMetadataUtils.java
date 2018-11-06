/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage;

import com.amalto.core.query.user.ConstantExpression;
import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.TimeConstant;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.*;

/**
 * Similar to {@link org.talend.mdm.commmon.metadata.MetadataUtils} but with utility methods for use of metadata
 * information in {@link com.amalto.core.storage.Storage} API.
 */
public class StorageMetadataUtils extends CommonStorageMetaDataUtils {

    private static final Logger LOGGER = Logger.getLogger(StorageMetadataUtils.class);

    private StorageMetadataUtils() {
    }

    /**
     * Similar to
     * {@link #path(org.talend.mdm.commmon.metadata.ComplexTypeMetadata, org.talend.mdm.commmon.metadata.FieldMetadata, boolean)}
     * but will remain in entity boundaries (won't follow FK to other MDM entities).
     *
     * @param origin Point of entry in the metadata graph.
     * @param target Field to look for as end of path.
     * @return A path <b>within</b> type <code>origin</code> to field <code>target</code>. Returns empty stack if no
     * path could be found.
     * @throws IllegalArgumentException If either <code>origin</code> or <code>path</code> is null.
     */
    public static List<FieldMetadata> path(ComplexTypeMetadata origin, FieldMetadata target) {
        return path(origin, target, true);
    }

    /**
     * <p>
     * Find <b>all</b> paths from type <code>origin</code> to field <code>target</code>.
     * </p>
     * <p>
     * This is a rather expensive operation, so use this method only when needed. When you need only <b>a</b> path to
     * field <code>target</code>, prefer usage of
     * {@link #path(org.talend.mdm.commmon.metadata.ComplexTypeMetadata, org.talend.mdm.commmon.metadata.FieldMetadata)}
     * .
     * </p>
     * <p>
     * This method follows references to other type <b>only</b> when type is not instantiable (see
     * {@link org.talend.mdm.commmon.metadata.TypeMetadata#isInstantiable()}).
     * </p>
     * 
     * @param type Point of entry in the metadata graph.
     * @param target Field to look for as end of path.
     * @return A path from type <code>origin</code> to field <code>target</code>. Returns empty list if no path could be
     * found.
     * @throws IllegalArgumentException If either <code>origin</code> or <code>path</code> is null.
     * @see #path(org.talend.mdm.commmon.metadata.ComplexTypeMetadata, org.talend.mdm.commmon.metadata.FieldMetadata)
     */
    public static Set<List<FieldMetadata>> paths(ComplexTypeMetadata type, FieldMetadata target) {
        Stack<FieldMetadata> path = new Stack<FieldMetadata>();
        HashSet<List<FieldMetadata>> foundPaths = new HashSet<List<FieldMetadata>>();
        _paths(type, target, path, foundPaths, new HashSet<TypeMetadata>());
        return foundPaths;
    }

    private static void _paths(ComplexTypeMetadata type, FieldMetadata target, Stack<FieldMetadata> currentPath,
            Set<List<FieldMetadata>> foundPaths, Set<TypeMetadata> processedTypes) {
        // Prevent infinite loop (in case of recursive relations)
        if (!processedTypes.add(type)) {
            return;
        }
        // Various optimizations for very simple cases
        if (type == null) {
            throw new IllegalArgumentException("Origin can not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target field can not be null");
        }
        if (target instanceof CompoundFieldMetadata) {
             FieldMetadata[] fields = ((CompoundFieldMetadata) target).getFields();
             for (FieldMetadata fieldMetadata : fields) {
             __paths(type, fieldMetadata, currentPath, foundPaths, processedTypes);
             }
        } else {
             __paths(type, target, currentPath, foundPaths, processedTypes);
        }
     }
            
     private static void __paths(ComplexTypeMetadata type, FieldMetadata target, Stack<FieldMetadata> currentPath,
             Set<List<FieldMetadata>> foundPaths, Set<TypeMetadata> processedTypes) {
        if (Storage.PROJECTION_TYPE.equals(type.getName()) && type.hasField(target.getName())) {
            currentPath.push(type.getField(target.getName()));
        }
        Collection<FieldMetadata> fields = type.getFields();
        for (FieldMetadata current : fields) {
            currentPath.push(current);
            if (current.equals(target)) {
                foundPaths.add(new ArrayList<FieldMetadata>(currentPath));
            }
            if (current instanceof ContainedTypeFieldMetadata) {
                ComplexTypeMetadata containedType = ((ContainedTypeFieldMetadata) current).getContainedType();
                _paths(containedType, target, currentPath, foundPaths, processedTypes);
                for (ComplexTypeMetadata subType : containedType.getSubTypes()) {
                    for (FieldMetadata field : subType.getFields()) {
                        if (field.getDeclaringType().equals(subType)) {
                            _paths(subType, target, currentPath, foundPaths, processedTypes);
                        }
                    }
                }
            } else if (current instanceof ReferenceFieldMetadata) {
                ComplexTypeMetadata referencedType = ((ReferenceFieldMetadata) current).getReferencedType();
                if (!referencedType.isInstantiable()) {
                    if (processedTypes.contains(referencedType)) {
                        Collection<FieldMetadata> tempFields = referencedType.getFields();
                        for (FieldMetadata tempCurrent : tempFields) {
                            if (tempCurrent.equals(target)) {
                                currentPath.push(tempCurrent);
                                foundPaths.add(new ArrayList<FieldMetadata>(currentPath));
                                currentPath.pop();
                            }
                        }
                    }
                    _paths(referencedType, target, currentPath, foundPaths, processedTypes);
                    for (ComplexTypeMetadata subType : referencedType.getSubTypes()) {
                        for (FieldMetadata field : subType.getFields()) {
                            if (field.getDeclaringType() == subType) {
                                _paths(subType, target, currentPath, foundPaths, processedTypes);
                            }
                        }
                    }
                }
            }
            currentPath.pop();
        }
    }
    
    @SuppressWarnings("rawtypes")
    public static boolean isValueAssignable(List value, String typeName) {
        try {
            convert(value, typeName);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether <code>value</code> is valid for full text search.
     * 
     * @param value The value to check.
     * @param field The field to receive the value.
     * @return <code>true</code> if the field can be searched by lucene, <code>false</code> otherwise.
     */
    public static boolean isValueSearchable(final String value, FieldMetadata field) {
        // As we decided to index all user defined field with value String.valueOf(fieldValue)
        // All values should be searchable.
        // TODO: improve non-text field indexing by using the correct FieldBridge
        return true;
    }

    /**
     * Creates a value from <code>dataAsString</code>. Type and/or format of the returned value depends on
     * <code>field</code>. For instance, calling this method with {@link String} with value "0" and a field typed as
     * integer returns {@link Integer} instance with value 0.
     *
     * @param dataAsString A {@link String} containing content to initialize a value.
     * @param field A {@link FieldMetadata} that describes type information about the field.
     * @return A {@link Object} value that has correct type according to <code>field</code>. Returns <code>null</code>
     * if field is instance of {@link org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata} (this type of field
     * isn't expected to have values). Also returns <code>null</code> is parameter <code>dataAsString</code> is null
     * <b>OR</b> if <code>dataAsString</code> is empty string.
     * @throws RuntimeException Throws sub classes of {@link RuntimeException} if <code>dataAsString</code> format does
     * not match field's type.
     */
    public static Object convert(String dataAsString, FieldMetadata field) {
        return convert(dataAsString, field.getType());
    }
    
    public static Object convert(List<String> dataAsList, FieldMetadata field) {
        return convert(dataAsList, field.getType());
    }

    public static Object convert(String dataAsString, FieldMetadata field, TypeMetadata actualType) {
        if (actualType == null) {
            // Use field's declared type if no actual type (TMDM-6898)
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Type is null, replacing type with field's declared type"); //$NON-NLS-1$
            }
            actualType = field.getType();
            if (actualType == null) {
                throw new IllegalArgumentException("Actual type for field '" + field.getName() + "' cannot be null."); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        if (field instanceof ReferenceFieldMetadata) {
            if (dataAsString == null || dataAsString.trim().isEmpty()) {
                return null;
            }
            List<String> ids = getIds(dataAsString);
            if (ids.isEmpty()) {
                throw new IllegalArgumentException("Id '" + dataAsString + "' does not match expected format (no id found).");  //$NON-NLS-1$//$NON-NLS-2$
            }
            if (!(actualType instanceof ComplexTypeMetadata)) {
                throw new IllegalArgumentException("Type '" + actualType.getName() + "' was expected to be an entity type.");  //$NON-NLS-1$//$NON-NLS-2$
            }
            ComplexTypeMetadata actualComplexType = (ComplexTypeMetadata) actualType;
            DataRecord referencedRecord = new DataRecord(actualComplexType, UnsupportedDataRecordMetadata.INSTANCE);
            Collection<FieldMetadata> keyFields = actualComplexType.getKeyFields();
            if (ids.size() != keyFields.size()) {
                throw new IllegalStateException("Type '" + actualType.getName() + "' expects " + keyFields.size()  //$NON-NLS-1$//$NON-NLS-2$
                        + " keys values, but got " + ids.size() + ".");  //$NON-NLS-1$//$NON-NLS-2$
            }
            Iterator<FieldMetadata> keyIterator = keyFields.iterator();
            for (String id : ids) {
                FieldMetadata nextKey = keyIterator.next();
                referencedRecord.set(nextKey, convert(id, nextKey));
            }
            return referencedRecord;
        } else {
            if (dataAsString == null) {
                return null;
            }
            if (dataAsString.trim().isEmpty()) { // Empty string is considered as null value
                return null;
            }
            TypeMetadata type = field.getType();
            if (!(field instanceof ContainedTypeFieldMetadata)) { // Contained (anonymous types) values can't have
                // values
                try {
                    return convert(dataAsString, type);
                } catch (Exception e) {
                    throw new RuntimeException("Could not convert value for field '" + field.getName() + "'", e);
                }
            } else {
                return null;
            }
        }
    }

    public static Object convert(ConstantExpression<Date> constant) {
        if (constant.isExpressionList()) {
            CollectionUtils.transform(constant.getValueList(), new Transformer() {
                public java.lang.Object transform(java.lang.Object input) {
                        return new Timestamp(((Date)input).getTime());
                    }
                });
            return constant.getValueList();
        } else {
            return new Timestamp(constant.getValue().getTime());
        }
    }

    public static String toString(Object value) {
        return toString(value, true);
    }

    public static String toString(Object value, boolean escapeXml) {
        if (value == null) {
            return StringUtils.EMPTY;
        }
        if (value instanceof DataRecord) {
            DataRecord record = (DataRecord) value;
            StringBuilder builder = new StringBuilder();
            Collection<FieldMetadata> keyFields = record.getType().getKeyFields();
            for (FieldMetadata keyField : keyFields) {
                String keyFieldValue = StorageMetadataUtils.toString(record.get(keyField), keyField);
                TypeMetadata type = org.talend.mdm.commmon.metadata.MetadataUtils.getSuperConcreteType(keyField.getType());
                if (Types.STRING.equals(type.getName()) && escapeXml) {
                    keyFieldValue = StringEscapeUtils.escapeXml(keyFieldValue);
                }
                builder.append('[').append(keyFieldValue).append(']');
            }
            return builder.toString();
        }
        if (escapeXml) {
            return StringEscapeUtils.escapeXml(String.valueOf(value));
        } else {
            return String.valueOf(value);
        }
    }

    public static String toString(Object o, FieldMetadata field) {
        if (o == null) {
            return StringUtils.EMPTY;
        }
        if (field instanceof ReferenceFieldMetadata) {
            return toString(o);
        }
        TypeMetadata type = MetadataUtils.getSuperConcreteType(field.getType());
        String typeName = type.getName();
        if (Types.DATE.equals(typeName)) {
            synchronized (DateConstant.DATE_FORMAT) {
                try {
                    DateFormat dateFormat = DateConstant.DATE_FORMAT;
                    return dateFormat.format(o);
                } catch (Exception e) {
                    throw new RuntimeException("Could not parse date '" + o + "'.", e);
                }
            }
        } else if (Types.DATETIME.equals(typeName)) {
            synchronized (DateTimeConstant.DATE_FORMAT) {
                try {
                    DateFormat dateFormat = DateTimeConstant.DATE_FORMAT;
                    return dateFormat.format(o);
                } catch (Exception e) {
                    throw new RuntimeException("Could not parse date time '" + o + "'.", e);
                }
            }
        } else if (Types.TIME.equals(typeName)) {
            synchronized (TimeConstant.TIME_FORMAT) {
                try {
                    DateFormat dateFormat = TimeConstant.TIME_FORMAT;
                    return dateFormat.format(o);
                } catch (Exception e) {
                    throw new RuntimeException("Could not parse time '" + o + "'.", e);
                }
            }
        } else if (Types.FLOAT.equals(typeName) || Types.DOUBLE.equals(typeName) || Types.DECIMAL.equals(typeName)) {
            return getNumberValue(typeName, o);
        } else {
            return String.valueOf(o);
        }
    }
    

    public static List<String> getIds(String dataAsString) {
        List<String> ids = new LinkedList<String>();
        if (dataAsString.startsWith("[")) { //$NON-NLS-1$
            char[] chars = dataAsString.toCharArray();
            StringBuilder builder = null;
            for (char currentChar : chars) {
                switch (currentChar) {
                case '[':
                    builder = new StringBuilder();
                    break;
                case ']':
                    if (builder != null) {
                        ids.add(builder.toString());
                    }
                    break;
                default:
                    if (builder != null) {
                        builder.append(currentChar);
                    }
                    break;
                }
            }
        } else {
            ids.add(dataAsString);
        }
        return ids;
    }

    public static String formatFranctionValue(String value) {
        if (value != null && value.contains(".")) { //$NON-NLS-1$
            String[] numberArray = value.trim().split("\\."); //$NON-NLS-1$
            String decimalValue = numberArray[1];
            int fractionDigits = decimalValue.length();

            for (int i = 0; i < decimalValue.length(); i++) {
                if (!"0".equals(String.valueOf(decimalValue.charAt(i)))) { //$NON-NLS-1$
                    fractionDigits = i + 1;
                }
            }
            return numberArray[0] + "." + decimalValue.substring(0, fractionDigits); //$NON-NLS-1$
        } else {
            return value;
        }
    }

    public static String getNumberValue(String type, Object value) {
        String valueString = new BigDecimal(String.valueOf(value)).toPlainString();
        if (Types.FLOAT.equals(type) || Types.DOUBLE.equals(type)) {
            valueString = formatFranctionValue(valueString);
        }
        return valueString;
    }
}
