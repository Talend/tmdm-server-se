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

import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.TimeConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.NotImplementedException;
import org.talend.mdm.commmon.metadata.*;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

public class CommonStorageMetaDataUtils {

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
        if (CommonStorage.PROJECTION_TYPE.equals(type.getName()) && type.hasField(target.getName())) {
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
        if (processedTypes.contains(type)) {
            return;
        }
        processedTypes.add(type);
        Collection<FieldMetadata> fields = type.getFields();
        for (FieldMetadata current : fields) {
            if (current instanceof ReferenceFieldMetadata
                    && ((ReferenceFieldMetadata) current).getReferencedType().equals(target.getContainingType())
                    && !((ReferenceFieldMetadata) current).getReferencedType().equals(type)) {
                path.push(current);
                if (current.equals(target)) {
                    return;
                }

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
                path.pop();
            }

        }

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


    public static Object convert(String dataAsString, FieldMetadata field) {
        return convert(dataAsString, field.getType());
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
                    throw new RuntimeException("Could not parse date string", e); //$NON-NLS-1$
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
                    throw new RuntimeException("Could not parse date time string", e); //$NON-NLS-1$
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
                throw new IllegalArgumentException("Value '" + dataAsString + "' is not valid for boolean"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            return Boolean.parseBoolean(dataAsString);
        } else if (Types.DECIMAL.equals(type)) {
            try {
                return new BigDecimal(dataAsString);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + dataAsString + "' is not a number.", e); //$NON-NLS-1$ //$NON-NLS-2$
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
                    throw new RuntimeException("Could not parse time string", e); //$NON-NLS-1$
                }
            }
        } else {
            throw new NotImplementedException("No support for type '" + type + "'"); //$NON-NLS-1$ //$NON-NLS-2$
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

    @SuppressWarnings("rawtypes")
    public static boolean isValueAssignable(final List value, FieldMetadata field) {
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

                @SuppressWarnings("unchecked")
                @Override
                public List<String> visit(ReferenceFieldMetadata referenceField) {
                    values.addAll(value);
                    return values;
                }

                @SuppressWarnings("unchecked")
                @Override
                public List<String> visit(SimpleTypeFieldMetadata simpleField) {
                    values.addAll(value);
                    return values;
                }

                @SuppressWarnings("unchecked")
                @Override
                public List<String> visit(EnumerationFieldMetadata enumField) {
                    values.addAll(value);
                    return values;
                }
            });
            for (int i = 0; i < fieldType.size(); i++) {
                try {
                    convert(convertValue, fieldType.get(i));
                } catch (Exception e) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Object convert(List<String> dataAsString, TypeMetadata type) {
        String typeName = type.getName();
        if (dataAsString == null
                || (dataAsString.isEmpty() && !Types.STRING.equals(typeName) && !typeName.contains("limitedString"))) { //$NON-NLS-1$
            return null;
        } else {
            TypeMetadata superType = org.talend.mdm.commmon.metadata.MetadataUtils.getSuperConcreteType(type);
            return convert(dataAsString, superType.getName());
        }
    }

    @SuppressWarnings("rawtypes")
    public static Object convert(List valueList, String type) {
        if (Types.STRING.equals(type) || Types.TOKEN.equals(type) || Types.DURATION.equals(type)) {
            return valueList;
        } else if (Types.INTEGER.equals(type) || Types.POSITIVE_INTEGER.equals(type) || Types.NEGATIVE_INTEGER.equals(type)
                || Types.NON_NEGATIVE_INTEGER.equals(type) || Types.NON_POSITIVE_INTEGER.equals(type) || Types.INT.equals(type)
                || Types.UNSIGNED_INT.equals(type)) {
            CollectionUtils.transform(valueList, new Transformer() {
                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return new Integer((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return valueList;
        } else if (Types.DATE.equals(type)) {
            // Be careful here: DateFormat is not thread safe
            synchronized (DateConstant.DATE_FORMAT) {
                CollectionUtils.transform(valueList, new Transformer() {
                    public java.lang.Object transform(java.lang.Object input) {
                        if (input instanceof String) {
                            try {
                                return DateConstant.DATE_FORMAT.parse((String) input);
                            } catch (ParseException e) {
                                throw new IllegalArgumentException("Value '" + input.toString() + "' is not valid for Date.", e); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        } else {
                            return input;
                        }
                    }
                });
                return valueList;
            }
        } else if (Types.DATETIME.equals(type)) {
            // Be careful here: DateFormat is not thread safe
            synchronized (DateTimeConstant.DATE_FORMAT) {
                CollectionUtils.transform(valueList, new Transformer() {
                    public java.lang.Object transform(java.lang.Object input) {
                        if (input instanceof String) {
                            try {
                                return DateTimeConstant.DATE_FORMAT.parse((String) input);
                            } catch (ParseException e) {
                                throw new IllegalArgumentException("Value '" + input.toString() + "' is not valid for Date Time.", e); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        } else {
                            return input;
                        }
                    }
                });
                return valueList;
            }
        } else if (Types.BOOLEAN.equals(type)) {
            // Boolean.parseBoolean returns "false" if content isn't a boolean string value. Callers of this method
            // expect call to fail if data is malformed.
            CollectionUtils.transform(valueList, new Transformer() {
                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        if ("0".equals(input.toString())) { //$NON-NLS-1$
                            return false;
                        } else if ("1".equals(input.toString())) { //$NON-NLS-1$
                            return true;
                        }
                        if (!"false".equalsIgnoreCase(input.toString()) && !"true".equalsIgnoreCase(input.toString())) { //$NON-NLS-1$ //$NON-NLS-2$
                            throw new IllegalArgumentException("Value '" + input.toString() + "' is not valid for Boolean"); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        return Boolean.parseBoolean(input.toString());
                    } else {
                        return input;
                    }
                }
            });
            return valueList;
        } else if (Types.DECIMAL.equals(type)) {
            CollectionUtils.transform(valueList, new Transformer() {
                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return new BigDecimal((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return valueList;
        } else if (Types.FLOAT.equals(type)) {
            CollectionUtils.transform(valueList, new Transformer() {
                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return Float.parseFloat((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return valueList;
        } else if (Types.LONG.equals(type) || Types.UNSIGNED_LONG.equals(type)) {
            CollectionUtils.transform(valueList, new Transformer() {
                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return Long.parseLong((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return valueList;
        } else if (Types.ANY_URI.equals(type)) {
            return valueList;
        } else if (Types.SHORT.equals(type) || Types.UNSIGNED_SHORT.equals(type)) {
            CollectionUtils.transform(valueList, new Transformer() {
                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return Short.parseShort((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return valueList;
        } else if (Types.QNAME.equals(type)) {
            return valueList;
        } else if (Types.BASE64_BINARY.equals(type)) {
            return valueList;
        } else if (Types.HEX_BINARY.equals(type)) {
            return valueList;
        } else if (Types.BYTE.equals(type) || Types.UNSIGNED_BYTE.equals(type)) {
            CollectionUtils.transform(valueList, new Transformer() {
                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return Byte.parseByte((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return valueList;
        } else if (Types.DOUBLE.equals(type) || Types.UNSIGNED_DOUBLE.equals(type)) {
            CollectionUtils.transform(valueList, new Transformer() {
                public java.lang.Object transform(java.lang.Object input) {
                    if (input instanceof String) {
                        return Double.parseDouble((String) input);
                    } else {
                        return input;
                    }
                }
            });
            return valueList;
        } else if (Types.TIME.equals(type)) {
            // Be careful here: DateFormat is not thread safe
            synchronized (TimeConstant.TIME_FORMAT) {
                CollectionUtils.transform(valueList, new Transformer() {
                    public java.lang.Object transform(java.lang.Object input) {
                        if (input instanceof String) {
                            try {
                                return TimeConstant.TIME_FORMAT.parse((String) input);
                            } catch (ParseException e) {
                                throw new IllegalArgumentException("Value '" + input.toString() + "' is not valid for Time.", e); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                        } else {
                            return input;
                        }
                    }
                });
            }
            return valueList;
        } else {
            throw new NotImplementedException("No support for type '" + type + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
}
