/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.CompoundFieldMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;

import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.TimeConstant;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;

/**
 * Similar to {@link org.talend.mdm.commmon.metadata.MetadataUtils} but with utility methods for use of metadata
 * information in {@link com.amalto.core.storage.Storage} API.
 */
public class StorageMetadataUtils {

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
     * Find <b>a</b> path (<b>not necessarily the shortest</b>) from type <code>origin</code> to field
     * <code>target</code>.
     * </p>
     * <p>
     * Method is expected to run in linear time, depending on:
     * <ul>
     * <li>Number of fields in <code>origin</code>.</li>
     * <li>Number of references fields accessible from <code>origin</code>.</li>
     * </ul>
     * </p>
     *
     * @param type Point of entry in the metadata graph.
     * @param target Field to look for as end of path.
     * @return A path from type <code>origin</code> to field <code>target</code>. Returns empty list if no path could be
     * found.
     * @throws IllegalArgumentException If either <code>origin</code> or <code>path</code> is null.
     */
    public static List<FieldMetadata> path(ComplexTypeMetadata type, FieldMetadata target, boolean includeReferences) {
        Stack<FieldMetadata> path = new Stack<FieldMetadata>();
        _path(type, target, path, new HashSet<ComplexTypeMetadata>(), includeReferences);
        return path;
    }

    private static void _path(ComplexTypeMetadata type, FieldMetadata target, Stack<FieldMetadata> path,
            Set<ComplexTypeMetadata> processedTypes, boolean includeReferences) {
        // Various optimizations for very simple cases
        if (type == null) {
            throw new IllegalArgumentException("Origin can not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target field can not be null");
        }
        if (Storage.PROJECTION_TYPE.equals(type.getName()) && type.hasField(target.getName())) {
            path.push(type.getField(target.getName()));
        }
        if (target.getContainingType() instanceof ContainedComplexTypeMetadata) {
            String targetPath = target.getPath();
            if (type.hasField(targetPath)) {
                StringTokenizer tokenizer = new StringTokenizer(targetPath, "/"); //$NON-NLS-1$
                StringBuilder currentPath = new StringBuilder();
                while (tokenizer.hasMoreTokens()) {
                    currentPath.append(tokenizer.nextToken()).append('/');
                    path.add(type.getField(currentPath.toString()));
                }
                return;
            }
        }
        //
        if (processedTypes.contains(type)) {
            return;
        }
        processedTypes.add(type);
        Collection<FieldMetadata> fields = type.getFields();
        for (FieldMetadata current : fields) {
            path.push(current);
            if (current.equals(target)) {
                return;
            }
            if (current instanceof ContainedTypeFieldMetadata) {
                ComplexTypeMetadata containedType = ((ContainedTypeFieldMetadata) current).getContainedType();
                _path(containedType, target, path, processedTypes, includeReferences);
                if (path.peek().equals(target)) {
                    return;
                }
                for (ComplexTypeMetadata subType : containedType.getSubTypes()) {
                    for (FieldMetadata field : subType.getFields()) {
                        if (field.getDeclaringType() == subType) {
                            _path(subType, target, path, processedTypes, includeReferences);
                            if (path.peek().equals(target)) {
                                return;
                            }
                        }
                    }
                }
            } else if (current instanceof ReferenceFieldMetadata) {
                if (includeReferences) {
                    ComplexTypeMetadata referencedType = ((ReferenceFieldMetadata) current).getReferencedType();
                    _path(referencedType, target, path, processedTypes, true);
                    if (path.peek().equals(target)) {
                        return;
                    }
                    for (ComplexTypeMetadata subType : referencedType.getSubTypes()) {
                        for (FieldMetadata field : subType.getFields()) {
                            if (field.getDeclaringType() == subType) {
                                _path(subType, target, path, processedTypes, true);
                                if (path.peek().equals(target)) {
                                    return;
                                }
                            }
                        }
                    }
                }
            }
            path.pop();
        }
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
        //
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

    /**
     * Checks whether <code>value</code> is valid for <code>typeName</code>.
     *
     * @param value The value to check.
     * @param typeName The type name of the value (should be one of {@link org.talend.mdm.commmon.metadata.Types}).
     * @return <code>true</code> if correct, <code>false</code> otherwise.
     */
    public static boolean isValueAssignable(String value, String typeName) {
        try {
            convert(value, typeName);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Checks whether <code>value</code> is valid for <code>typeName</code>.
     * 
     * @param value The value to check.
     * @param field The field to receive the value.
     * @return <code>true</code> if correct, <code>false</code> otherwise. Since all fields can receive
     * <code>null</code>, <code>null</code> always returns <code>true</code>.
     */
    public static boolean isValueAssignable(final String value, FieldMetadata field) {
        if (value == null) {
            return true;
        }
        try {
            List<TypeMetadata> fieldType = field.accept(new DefaultMetadataVisitor<List<TypeMetadata>>() {
                List<TypeMetadata> fieldTypes = new LinkedList<TypeMetadata>();

                @Override
                public List<TypeMetadata> visit(ReferenceFieldMetadata referenceField) {
                    fieldTypes.add(MetadataUtils.getSuperConcreteType(referenceField.getReferencedField().getType()));
                    return fieldTypes;
                }

                @Override
                public List<TypeMetadata> visit(SimpleTypeFieldMetadata simpleField) {
                    fieldTypes.add(MetadataUtils.getSuperConcreteType(simpleField.getType()));
                    return fieldTypes;
                }

                @Override
                public List<TypeMetadata> visit(EnumerationFieldMetadata enumField) {
                    fieldTypes.add(MetadataUtils.getSuperConcreteType(enumField.getType()));
                    return fieldTypes;
                }
            });
            List<String> convertValue = field.accept(new DefaultMetadataVisitor<List<String>>() {
                List<String> values = new LinkedList<String>();

                @Override
                public List<String> visit(ReferenceFieldMetadata referenceField) {
                    if (value.startsWith("[")) { //$NON-NLS-1$
                        StringTokenizer tokenizer = new StringTokenizer(value, "["); //$NON-NLS-1$
                        while (tokenizer.hasMoreTokens()) {
                            String nextToken = tokenizer.nextToken();
                            values.add(nextToken.substring(1, nextToken.length() - 1));
                        }
                    } else {
                        values.add(value);
                    }
                    return values;
                }

                @Override
                public List<String> visit(SimpleTypeFieldMetadata simpleField) {
                    values.add(value);
                    return values;
                }

                @Override
                public List<String> visit(EnumerationFieldMetadata enumField) {
                    values.add(value);
                    return values;
                }
            });
            for (int i = 0; i < fieldType.size(); i++) {
                try {
                    convert(convertValue.get(i), fieldType.get(i));
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
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

    public static Object convert(String dataAsString, FieldMetadata field, TypeMetadata actualType) {
        if (actualType == null) {
            // Use field's declared type if no actual type (TMDM-6898)
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Type is null, replacing type with field's declared type");
            }
            actualType = field.getType();
            if (actualType == null) {
                throw new IllegalArgumentException("Actual type for field '" + field.getName() + "' cannot be null.");
            }
        }
        if (field instanceof ReferenceFieldMetadata) {
            if (dataAsString == null || dataAsString.trim().isEmpty()) {
                return null;
            }
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
            if (ids.isEmpty()) {
                throw new IllegalArgumentException("Id '" + dataAsString + "' does not match expected format (no id found).");
            }
            if (!(actualType instanceof ComplexTypeMetadata)) {
                throw new IllegalArgumentException("Type '" + actualType.getName() + "' was expected to be an entity type.");
            }
            ComplexTypeMetadata actualComplexType = (ComplexTypeMetadata) actualType;
            DataRecord referencedRecord = new DataRecord(actualComplexType, UnsupportedDataRecordMetadata.INSTANCE);
            Collection<FieldMetadata> keyFields = actualComplexType.getKeyFields();
            if (ids.size() != keyFields.size()) {
                throw new IllegalStateException("Type '" + actualType.getName() + "' expects " + keyFields.size()
                        + " keys values, but got " + ids.size() + ".");
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

    public static Object convert(String dataAsString, TypeMetadata type) {
        String typeName = type.getName();
        if (dataAsString == null
                || (dataAsString.isEmpty() && !Types.STRING.equals(typeName) && !typeName.contains("limitedString"))) { //$NON-NLS-1$
            return null;
        } else {
            TypeMetadata superType = org.talend.mdm.commmon.metadata.MetadataUtils.getSuperConcreteType(type);
            return convert(dataAsString, superType.getName());
        }
    }

    public static Object convert(String dataAsString, String type) {
        if (Types.STRING.equals(type) || Types.TOKEN.equals(type) || Types.DURATION.equals(type)) {
            return dataAsString;
        } else if (Types.INTEGER.equals(type) || Types.POSITIVE_INTEGER.equals(type) || Types.NEGATIVE_INTEGER.equals(type)
                || Types.NON_NEGATIVE_INTEGER.equals(type) || Types.NON_POSITIVE_INTEGER.equals(type) || Types.INT.equals(type)
                || Types.UNSIGNED_INT.equals(type)) {
            return Integer.parseInt(dataAsString);
        } else if (Types.DATE.equals(type)) {
            // Be careful here: DateFormat is not thread safe
            synchronized (DateConstant.DATE_FORMAT) {
                try {
                    DateFormat dateFormat = DateConstant.DATE_FORMAT;
                    Date date = dateFormat.parse(dataAsString);
                    return new Timestamp(date.getTime());
                } catch (Exception e) {
                    throw new RuntimeException("Could not parse date string", e);
                }
            }
        } else if (Types.DATETIME.equals(type)) {
            // Be careful here: DateFormat is not thread safe
            synchronized (DateTimeConstant.DATE_FORMAT) {
                try {
                    DateFormat dateFormat = DateTimeConstant.DATE_FORMAT;
                    Date date = dateFormat.parse(dataAsString);
                    return new Timestamp(date.getTime());
                } catch (Exception e) {
                    throw new RuntimeException("Could not parse date time string", e);
                }
            }
        } else if (Types.BOOLEAN.equals(type)) {
            // Boolean.parseBoolean returns "false" if content isn't a boolean string value. Callers of this method
            // expect call to fail if data is malformed.
            if ("0".equals(dataAsString)) { //$NON-NLS-1$
                return false;
            } else if ("1".equals(dataAsString)) { //$NON-NLS-1$
                return true;
            }
            if (!"false".equalsIgnoreCase(dataAsString) && !"true".equalsIgnoreCase(dataAsString)) { //$NON-NLS-1$ //$NON-NLS-2$
                throw new IllegalArgumentException("Value '" + dataAsString + "' is not valid for boolean");
            }
            return Boolean.parseBoolean(dataAsString);
        } else if (Types.DECIMAL.equals(type)) {
            try {
                return new BigDecimal(dataAsString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + dataAsString + "' is not a number.", e);
            }
        } else if (Types.FLOAT.equals(type)) {
            return Float.parseFloat(dataAsString);
        } else if (Types.LONG.equals(type) || Types.UNSIGNED_LONG.equals(type)) {
            return Long.parseLong(dataAsString);
        } else if (Types.ANY_URI.equals(type)) {
            return dataAsString;
        } else if (Types.SHORT.equals(type) || Types.UNSIGNED_SHORT.equals(type)) {
            return Short.parseShort(dataAsString);
        } else if (Types.QNAME.equals(type)) {
            return dataAsString;
        } else if (Types.BASE64_BINARY.equals(type)) {
            return dataAsString;
        } else if (Types.HEX_BINARY.equals(type)) {
            return dataAsString;
        } else if (Types.BYTE.equals(type) || Types.UNSIGNED_BYTE.equals(type)) {
            return Byte.parseByte(dataAsString);
        } else if (Types.DOUBLE.equals(type) || Types.UNSIGNED_DOUBLE.equals(type)) {
            return Double.parseDouble(dataAsString);
        } else if (Types.TIME.equals(type)) {
            // Be careful here: DateFormat is not thread safe
            synchronized (TimeConstant.TIME_FORMAT) {
                try {
                    DateFormat dateFormat = TimeConstant.TIME_FORMAT;
                    Date date = dateFormat.parse(dataAsString);
                    return new Timestamp(date.getTime());
                } catch (ParseException e) {
                    throw new RuntimeException("Could not parse time string", e);
                }
            }
        } else {
            throw new NotImplementedException("No support for type '" + type + "'");
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
        } else {
            return String.valueOf(o);
        }
    }
}
