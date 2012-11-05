/*
 * Copyright (C) 2006-2012 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.metadata;

import com.amalto.core.integrity.ForeignKeyIntegrity;
import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.TimeConstant;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import org.apache.commons.lang.NotImplementedException;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;

public class MetadataUtils {

    private MetadataUtils() {
    }

    private static final double ENTITY_RANK_ADJUST = 0.9;

    /**
     * <p>
     * Computes "entity rank": entity rank score is based on a modified version of Google's Page Rank algorithm (it's
     * the inverse operation of Page Rank).
     * </p>
     * <p>
     * Entity rank is computed with this algorithm:
     * ER(E) = N + d (ER(E1)/C(E1) + ... + ER(En)/C(En))
     * where:
     * <ul>
     * <li>ER(E) is the entity rank of E.</li>
     * <li>N the number of entities in <code>repository</code></li>
     * <li>d an adjustement factor (between 0 and 1)</li>
     * <li>ER(Ei) is the entity rank for entity Ei that E references via a reference field</li>
     * <li>C(Ei) the number of entities in <code>repository</code> that reference Ei in the repository.</li>
     * </ul>
     * </p>
     * <p>
     * Code is expected to run in linear time (O(n+p) where n is the number of entities and p the number of references).
     * Used memory is O(n^2) (due to a dependency ordering).
     * </p>
     * @param repository A {@link MetadataRepository} instance that contains entity types.
     * @return A {@link Map} that maps a entity to its entity rank value.
     */
    public static Map<ComplexTypeMetadata, Long> computeEntityRank(MetadataRepository repository) {
        List<ComplexTypeMetadata> sortedTypes = sortTypes(repository);
        int totalNumber = sortedTypes.size();

        Map<ComplexTypeMetadata, Long> entityRank = new HashMap<ComplexTypeMetadata, Long>();
        for (ComplexTypeMetadata currentType : sortedTypes) {
            if (currentType.isInstantiable()) {
                double rank = totalNumber;
                for (FieldMetadata currentField : currentType.getFields()) {
                    if (currentField instanceof ReferenceFieldMetadata) {
                        ComplexTypeMetadata referencedType = ((ReferenceFieldMetadata) currentField).getReferencedType();
                        if (referencedType != currentType) {
                            Long referencedEntityRank = entityRank.get(referencedType);
                            if (referencedEntityRank != null) {
                                double inboundReferencesCount = getInboundReferencesCount(repository, referencedType);
                                rank += ENTITY_RANK_ADJUST * (referencedEntityRank / inboundReferencesCount);
                            }
                        }
                    }
                }
                entityRank.put(currentType, Math.round(rank));
            }
        }
        return entityRank;
    }

    private static double getInboundReferencesCount(MetadataRepository repository, ComplexTypeMetadata referencedType) {
        return repository.accept(new ForeignKeyIntegrity(referencedType)).size();
    }

    /**
     * <p>
     * Find <b>a</b> path (<b>not necessarily the shortest</b>) from type <code>origin</code> to field <code>target</code>.
     * </p>
     * <p>
     * Method is expected to run in linear time, depending on:
     * <ul>
     * <li>Number of fields in <code>origin</code>.</li>
     * <li>Number of references fields accessible from <code>origin</code>.</li>
     * </ul>
     * </p>
     *
     * @param origin Point of entry in the metadata graph.
     * @param target Field to look for as end of path.
     * @return A path from type <code>origin</code> to field <code>target</code>. Returns empty stack if no path could be found.
     * @throws IllegalArgumentException If either <code>origin</code> or <code>path</code> is null.
     */
    public static List<FieldMetadata> path(ComplexTypeMetadata origin, FieldMetadata target) {
        if (origin == null) {
            throw new IllegalArgumentException("Origin can not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("Target field can not be null");
        }
        if (Storage.PROJECTION_TYPE.equals(origin.getName()) && origin.hasField(target.getName())) {
            return Collections.singletonList(origin.getField(target.getName()));
        }
        if (target.getContainingType().equals(origin)) { // Optimization: don't compute paths if field is defined in origin type.
            return Collections.singletonList(origin.getField(target.getName()));
        }
        Stack<FieldMetadata> processStack = new Stack<FieldMetadata>();
        LinkedList<FieldMetadata> path = new LinkedList<FieldMetadata>();
        processStack.addAll(origin.getFields());
        Set<FieldMetadata> processedFields = new HashSet<FieldMetadata>();
        while (!processStack.isEmpty()) {
            FieldMetadata current = processStack.pop();
            if (!path.isEmpty() && path.getLast().getContainingType().equals(current.getContainingType())) {
                path.removeLast();
            }
            if (current.equals(target)) {
                path.add(current);
                int lastIndex = path.size() - 1;
                Iterator<FieldMetadata> iterator = path.descendingIterator();
                while (iterator.hasNext()) {
                    if (iterator.next().getContainingType().equals(origin)) {
                        break;
                    }
                    lastIndex--;
                }
                for (int j = 0; j < lastIndex; j++) {
                    path.remove(0);
                }
                return path;
            } else if(!(current instanceof SimpleTypeFieldMetadata || current instanceof EnumerationFieldMetadata)){
                path.add(current);
            }
            if (!processedFields.contains(current)) {
                processedFields.add(current);
                if (current instanceof ContainedTypeFieldMetadata) {
                    processStack.addAll(((ContainedTypeFieldMetadata) current).getContainedType().getFields());
                } else if (current instanceof ReferenceFieldMetadata) {
                    processStack.addAll(((ReferenceFieldMetadata) current).getReferencedType().getFields());
                }
            }
        }
        return path;
    }

    /**
     * Creates a value from <code>dataAsString</code>. Type and/or format of the returned value depends on <code>field</code>.
     * For instance, calling this method with {@link String} with value "0" and a field typed as integer returns {@link Integer}
     * instance with value 0.
     *
     * @param dataAsString A {@link String} containing content to initialize a value.
     * @param field        A {@link FieldMetadata} that describes type information about the field.
     * @return A {@link Object} value that has correct type according to <code>field</code>. Returns <code>null</code> if
     *         field is instance of {@link ContainedTypeFieldMetadata} (this type of field isn't expected to have values).
     *         Also returns <code>null</code> is parameter <code>dataAsString</code> is null <b>OR</b> if <code>dataAsString</code>
     *         is empty string.
     * @throws RuntimeException Throws sub classes of {@link RuntimeException} if <code>dataAsString</code>  format does
     *                          not match field's type.
     */
    public static Object convert(String dataAsString, FieldMetadata field) {
        return convert(dataAsString, field.getType());
    }

    public static Object convert(String dataAsString, FieldMetadata field, TypeMetadata actualType) {
        if (actualType == null) {
            throw new IllegalArgumentException("Actual type for field '" + field.getName() + "' cannot be null.");
        }
        if (field instanceof ReferenceFieldMetadata) {
            List<String> ids = new LinkedList<String>();
            if (dataAsString.startsWith("[")) {
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
            Iterator<FieldMetadata> keyIterator = actualComplexType.getKeyFields().iterator();
            for (String id : ids) {
                FieldMetadata nextKey = keyIterator.next();
                referencedRecord.set(nextKey, convert(id, nextKey));
            }
            return referencedRecord;
        } else {
            String xmlData = dataAsString;
            if (xmlData == null) {
                return null;
            }
            xmlData = xmlData.trim();
            if (xmlData.trim().isEmpty()) { // Empty string is considered as null value
                return null;
            }

            TypeMetadata type = field.getType();
            if (!(field instanceof ContainedTypeFieldMetadata)) {  // Contained (anonymous types) values can't have values
                return convert(xmlData, type);
            } else {
                return null;
            }
        }
    }

    public static Object convert(String dataAsString, TypeMetadata type) {
        // Move up the inheritance tree to find the "most generic" type (used when simple types inherits from XSD types,
        // in this case, the XSD type is interesting, not the custom one).
        while (!type.getSuperTypes().isEmpty()) {
            type = type.getSuperTypes().iterator().next();
        }
        return convert(dataAsString, type.getName());
    }

    public static Object convert(String dataAsString, String type) {
        if (dataAsString.isEmpty()) {
            return null;
        }
        if ("string".equals(type)) { //$NON-NLS-1$
            return dataAsString;
        } else if ("integer".equals(type) //$NON-NLS-1$
                || "positiveInteger".equals(type) //$NON-NLS-1$
                || "negativeInteger".equals(type) //$NON-NLS-1$
                || "nonNegativeInteger".equals(type) //$NON-NLS-1$
                || "nonPositiveInteger".equals(type) //$NON-NLS-1$
                || "int".equals(type) //$NON-NLS-1$
                || "unsignedInt".equals(type)) { //$NON-NLS-1$
            return Integer.parseInt(dataAsString);
        } else if ("date".equals(type)) { //$NON-NLS-1$
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
        } else if ("dateTime".equals(type)) { //$NON-NLS-1$
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
        } else if ("boolean".equals(type)) { //$NON-NLS-1$
            // Boolean.parseBoolean returns "false" if content isn't a boolean string value. Callers of this method
            // expect call to fail if data is malformed.
            if (!"false".equalsIgnoreCase(dataAsString) && !"true".equalsIgnoreCase(dataAsString)) { //$NON-NLS-1$ //$NON-NLS-2$
                throw new IllegalArgumentException("Value '" + dataAsString + "' is not valid for boolean");
            }
            return Boolean.parseBoolean(dataAsString);
        } else if ("decimal".equals(type)) { //$NON-NLS-1$
            return new BigDecimal(dataAsString);
        } else if ("float".equals(type)) { //$NON-NLS-1$
            return Float.parseFloat(dataAsString);
        } else if ("long".equals(type) || "unsignedLong".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
            return Long.parseLong(dataAsString);
        } else if ("anyURI".equals(type)) { //$NON-NLS-1$
            return dataAsString;
        } else if ("short".equals(type) || "unsignedShort".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
            return Short.parseShort(dataAsString);
        } else if ("QName".equals(type)) { //$NON-NLS-1$
            return dataAsString;
        } else if ("base64Binary".equals(type)) { //$NON-NLS-1$
            return dataAsString;
        } else if ("hexBinary".equals(type)) { //$NON-NLS-1$
            return dataAsString;
        } else if ("byte".equals(type) || "unsignedByte".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
            return Byte.parseByte(dataAsString);
        } else if ("double".equals(type) || "unsignedDouble".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
            return Double.parseDouble(dataAsString);
        } else if ("duration".equals(type) || "time".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
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

    /**
     * Returns the corresponding Java type for the {@link TypeMetadata} type.
     *
     * @param metadata A {@link TypeMetadata} instance.
     * @return The name of Java class for the <code>metadata</code> argument. Returned string might directly be used for
     *         a {@link Class#forName(String)} call.
     */
    public static String getJavaType(TypeMetadata metadata) {
        // Move up the inheritance tree to find the "most generic" type (used when simple types inherits from XSD types,
        // in this case, the XSD type is interesting, not the custom one).
        while (!metadata.getSuperTypes().isEmpty()) {
            metadata = metadata.getSuperTypes().iterator().next();
        }
        String type = metadata.getName();
        if ("string".equals(type)) { //$NON-NLS-1$
            return "java.lang.String"; //$NON-NLS-1$
        } else if ("anyURI".equals(type)) {
            return "java.lang.String"; //$NON-NLS-1$
        } else if ("int".equals(type) //$NON-NLS-1$
                || "integer".equals(type) //$NON-NLS-1$
                || "positiveInteger".equals(type) //$NON-NLS-1$
                || "nonPositiveInteger".equals(type) //$NON-NLS-1$
                || "nonNegativeInteger".equals(type) //$NON-NLS-1$
                || "negativeInteger".equals(type) //$NON-NLS-1$
                || "unsignedInt".equals(type)) { //$NON-NLS-1$
            return "java.lang.Integer"; //$NON-NLS-1$
        } else if ("boolean".equals(type)) { //$NON-NLS-1$
            return "java.lang.Boolean"; //$NON-NLS-1$
        } else if ("decimal".equals(type)) { //$NON-NLS-1$
            return "java.math.BigDecimal"; //$NON-NLS-1$
        } else if ("date".equals(type) //$NON-NLS-1$
                || "dateTime".equals(type) //$NON-NLS-1$
                || "time".equals(type) //$NON-NLS-1$
                || "duration".equals(type)) { //$NON-NLS-1$
            return "java.sql.Timestamp"; //$NON-NLS-1$
        } else if ("unsignedShort".equals(type) || "short".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
            return "java.lang.Short"; //$NON-NLS-1$
        } else if ("unsignedLong".equals(type) || "long".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
            return "java.lang.Long"; //$NON-NLS-1$
        } else if ("float".equals(type)) { //$NON-NLS-1$
            return "java.lang.Float"; //$NON-NLS-1$
        } else if ("base64Binary".equals(type)) { //$NON-NLS-1$
            return "java.lang.String"; //$NON-NLS-1$
        } else if ("byte".equals(type) || "unsignedByte".equals(type)) { //$NON-NLS-1$ //$NON-NLS-2$
            return "java.lang.Byte"; //$NON-NLS-1$
        } else if ("QName".equals(type)) { //$NON-NLS-1$
            return "java.lang.String"; //$NON-NLS-1$
        } else if ("hexBinary".equals(type)) { //$NON-NLS-1$
            return "java.lang.String"; //$NON-NLS-1$
        } else if ("double".equals(type)) { //$NON-NLS-1$
            return "java.lang.Double"; //$NON-NLS-1$
        } else {
            throw new UnsupportedOperationException("No support for field typed as '" + type + "'");
        }
    }

    /**
     * @param javaClassName A java class name.
     * @return The XSD type that can be used to store a value typed as <code>javaClassName</code>.
     * @throws UnsupportedOperationException If there's no known mapping from this java class to a XSD primitive type.
     */
    public static String getType(String javaClassName) {
        if ("java.lang.String".equals(javaClassName)) {
            return "string";
        } else if ("java.lang.Integer".equals(javaClassName)
                || "java.math.BigInteger".equals(javaClassName)) {
            return "int";
        } else if ("java.lang.Boolean".equals(javaClassName)) {
            return "boolean";
        } else if ("java.math.BigDecimal".equals(javaClassName)) {
            return "decimal";
        } else if ("java.sql.Timestamp".equals(javaClassName)) {
            return "dateTime";
        } else if ("java.lang.Short".equals(javaClassName)) {
            return "short";
        } else if ("java.lang.Long".equals(javaClassName)) {
            return "long";
        } else if ("java.lang.Float".equals(javaClassName)) {
            return "float";
        } else if ("java.lang.Byte".equals(javaClassName)) {
            return "byte";
        } else if ("java.lang.Double".equals(javaClassName)) {
            return "double";
        } else {
            throw new UnsupportedOperationException("No support for java class '" + javaClassName + "'");
        }
    }

    /**
     * <p>
     * Sorts type in inverse order of dependency (topological sort). A dependency to <i>type</i> might be:
     * <ul>
     * <li>FK reference to <i>type</i> (sub types of <i>type</i> are all included as a dependency).</li>
     * <li>Use of <i>type</i> as a super type.</li>
     * </ul>
     * This method runs in linear time <i>O(n+p)</i> (<i>n</i> number of types and <i>p</i> number of dependencies
     * between types). This method uses <i>n²</i> bytes in memory for processing (<i>n</i> still being the number of types
     * in <code>repository</code>).
     * </p>
     * <p>
     * This method is thread safe.
     * </p>
     *
     * @param repository The repository that contains entity types to sort.
     * @return A sorted list of {@link ComplexTypeMetadata} types. First type of list is a type that has no dependency on
     *         any other type of the list.
     * @throws IllegalArgumentException If repository contains types that creates a cyclic dependency. Error message contains
     *                                  information on where the cycle is.
     * @see #sortNonInstantiableTypes(MetadataRepository)
     */
    public static List<ComplexTypeMetadata> sortTypes(MetadataRepository repository) {
        Collection<ComplexTypeMetadata> userDefinedTypes = repository.getUserComplexTypes();
        return _sortTypes(userDefinedTypes, true);
    }

    /**
     * <p>
     * Sorts type in inverse order of dependency (topological sort). A dependency to <i>type</i> might be:
     * <ul>
     * <li>FK reference to <i>type</i> (sub types of <i>type</i> are all included as a dependency).</li>
     * <li>Use of <i>type</i> as a super type.</li>
     * </ul>
     * This method runs in linear time <i>O(n+p)</i> (<i>n</i> number of types and <i>p</i> number of dependencies
     * between types). This method uses <i>n²</i> bytes in memory for processing (<i>n</i> still being the number of types
     * in <code>repository</code>).
     * </p>
     * <p>
     * This method is thread safe.
     * </p>
     *
     * @param repository The repository that contains <b>non instantiable</b> (aka "reusable") types to sort.
     * @return A sorted list of {@link ComplexTypeMetadata} types. First type of list is a type that has no dependency on
     *         any other type of the list.
     * @throws IllegalArgumentException If repository contains types that creates a cyclic dependency. Error message contains
     *                                  information on where the cycle is.
     * @see #sortTypes(MetadataRepository)
     */
    public static List<ComplexTypeMetadata> sortNonInstantiableTypes(MetadataRepository repository) {
        Collection<ComplexTypeMetadata> nonInstantiableTypes = repository.getNonInstantiableTypes();
        return _sortTypes(nonInstantiableTypes, false);
    }

    private static List<ComplexTypeMetadata> _sortTypes(Collection<ComplexTypeMetadata> typesToSort, final boolean isInstantiable) {
        final List<ComplexTypeMetadata> types = new ArrayList<ComplexTypeMetadata>(typesToSort);
        /*
        * Compute additional data for topological sorting
        */
        final byte[][] dependencyGraph = new byte[types.size()][types.size()];
        for (final ComplexTypeMetadata type : types) {
            byte[] lineValue = new byte[types.size()];
            dependencyGraph[getId(type, types)] = lineValue;
            type.accept(new DefaultMetadataVisitor<Void>() {
                @Override
                public Void visit(ComplexTypeMetadata complexType) {
                    Collection<TypeMetadata> superTypes = complexType.getSuperTypes();
                    for (TypeMetadata superType : superTypes) {
                        if (superType instanceof ComplexTypeMetadata) {
                            dependencyGraph[getId(type, types)][getId(((ComplexTypeMetadata) superType), types)]++;
                        }
                    }
                    super.visit(complexType);
                    return null;
                }

                @Override
                public Void visit(ReferenceFieldMetadata referenceField) {
                    ComplexTypeMetadata referencedType = referenceField.getReferencedType();
                    if (!type.equals(referencedType) && referenceField.isFKIntegrity()) { // Don't count a dependency to itself as a dependency.
                        if (isInstantiable == referencedType.isInstantiable()) {
                            dependencyGraph[getId(type, types)][getId(referencedType, types)]++;
                            // Implicitly include reference to sub types of referenced type.
                            for (ComplexTypeMetadata subType : referencedType.getSubTypes()) {
                                dependencyGraph[getId(type, types)][getId(subType, types)]++;
                            }
                        }
                    }
                    return null;
                }
            });
        }

        /*
        * TOPOLOGICAL SORTING
        * See "Kahn, A. B. (1962), "Topological sorting of large networks", Communications of the ACM"
        */
        List<ComplexTypeMetadata> sortedTypes = new LinkedList<ComplexTypeMetadata>();
        Set<ComplexTypeMetadata> noIncomingEdges = new HashSet<ComplexTypeMetadata>();
        int lineNumber = 0;
        for (byte[] line : dependencyGraph) {
            if (!hasIncomingEdges(line)) {
                noIncomingEdges.add(getType(types, lineNumber));
            }
            lineNumber++;
        }

        while (!noIncomingEdges.isEmpty()) {
            Iterator<ComplexTypeMetadata> iterator = noIncomingEdges.iterator();
            ComplexTypeMetadata type = iterator.next();
            iterator.remove();

            sortedTypes.add(type);
            int columnNumber = getId(type, types);
            for (int i = 0; i < types.size(); i++) {
                int edge = dependencyGraph[i][columnNumber];
                if (edge > 0) {
                    dependencyGraph[i][columnNumber] -= edge;

                    if (!hasIncomingEdges(dependencyGraph[i])) {
                        noIncomingEdges.add(getType(types, i));
                    }
                }
            }
        }

        lineNumber = 0;
        for (byte[] line : dependencyGraph) {
            for (int column : line) {
                if (column != 0) { // unresolved dependency (means there is a cycle somewhere).
                    int currentLineNumber = lineNumber;
                    List<ComplexTypeMetadata> dependencyPath = new LinkedList<ComplexTypeMetadata>();
                    // use dependency graph matrix to get cyclic dependency.
                    do {
                        ComplexTypeMetadata type = getType(types, currentLineNumber);
                        if (!dependencyPath.contains(type)) {
                            dependencyPath.add(type);
                        } else {
                            dependencyPath.add(type); // Include cycle start to get a better exception message.
                            break;
                        }
                        byte[] bytes = dependencyGraph[getId(type, types)];
                        for (int currentByte = 0; currentByte < bytes.length; currentByte++) {
                            if (bytes[currentByte] > 0) { // This gets the first unresolved dependency (but there might be more of them).
                                currentLineNumber = currentByte;
                                break;
                            }
                        }
                    } while (currentLineNumber != column);

                    StringBuilder pathAsString = new StringBuilder();
                    Iterator<ComplexTypeMetadata> dependencyPathIterator = dependencyPath.iterator();
                    while (dependencyPathIterator.hasNext()) {
                        pathAsString.append(dependencyPathIterator.next().getName());
                        if (dependencyPathIterator.hasNext()) {
                            pathAsString.append(" -> ");
                        }
                    }
                    throw new IllegalArgumentException("Data model has at least one circular dependency (Hint: " + pathAsString + ")");
                }
            }
            lineNumber++;
        }

        return sortedTypes;
    }

    private static ComplexTypeMetadata getType(List<ComplexTypeMetadata> types, int lineNumber) {
        return types.get(lineNumber);
    }

    // internal method for sortTypes
    private static boolean hasIncomingEdges(byte[] line) {
        boolean hasIncomingEdge = false;
        for (byte column : line) {
            if (column > 0) {
                hasIncomingEdge = true;
                break;
            }
        }
        return hasIncomingEdge;
    }

    // internal method for sortTypes
    private static int getId(ComplexTypeMetadata type, List<ComplexTypeMetadata> types) {
        if (!types.contains(type)) {
            types.add(type);
        }
        return types.indexOf(type);
    }

    public static boolean isValueAssignable(String value, String typeName) {
        try {
            convert(value, typeName);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}
