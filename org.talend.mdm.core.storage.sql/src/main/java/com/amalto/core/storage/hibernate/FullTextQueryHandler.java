/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import com.amalto.core.query.user.*;
import com.amalto.core.query.user.metadata.*;
import com.amalto.core.storage.CloseableIterator;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.exception.FullTextQueryCompositeKeyException;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.search.*;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.search.FullTextQuery;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.talend.mdm.commmon.metadata.*;

import javax.xml.XMLConstants;
import java.io.IOException;
import java.util.*;


class FullTextQueryHandler extends AbstractQueryHandler {

    private FullTextQuery query;

    private int pageSize;

    private final MappingRepository mappings;

    public FullTextQueryHandler(Storage storage,
                                MappingRepository mappings,
                                StorageClassLoader storageClassLoader,
                                Session session,
                                Select select,
                                List<TypedExpression> selectedFields,
                                Set<ResultsCallback> callbacks) {
        super(storage, storageClassLoader, session, select, selectedFields, callbacks);
        this.mappings = mappings;
    }

    @Override
    public StorageResults visit(Select select) {
        // TMDM-4654: Checks if entity has a composite PK.
        Set<ComplexTypeMetadata> compositeKeyTypes = new HashSet<ComplexTypeMetadata>();
        // TMDM-7496: Search should include references to reused types
        Collection<ComplexTypeMetadata> types = new HashSet<ComplexTypeMetadata>(select.accept(new SearchTransitiveClosure(storage)));
        for (ComplexTypeMetadata type : types) {
            if (type.getKeyFields().size() > 1) {
                compositeKeyTypes.add(type);
            }
        }
        if (!compositeKeyTypes.isEmpty()) {
            StringBuilder message = new StringBuilder();
            Iterator it = compositeKeyTypes.iterator();
            while (it.hasNext()) {
                ComplexTypeMetadata compositeKeyType = (ComplexTypeMetadata) it.next();
                message.append(compositeKeyType.getName());
                if (it.hasNext()) {
                    message.append(',');
                }
            }
            throw new FullTextQueryCompositeKeyException(message.toString());
        }
        // Removes Joins and joined fields.
        List<Join> joins = select.getJoins();
        if (!joins.isEmpty()) {
            Set<ComplexTypeMetadata> joinedTypes = new HashSet<ComplexTypeMetadata>();
            for (Join join : joins) {
                joinedTypes.add(join.getRightField().getFieldMetadata().getContainingType());
            }
            for (ComplexTypeMetadata joinedType : joinedTypes) {
                types.remove(joinedType);
            }
            List<TypedExpression> filteredFields = new LinkedList<TypedExpression>();
            for (TypedExpression expression : select.getSelectedFields()) {
                if (expression instanceof Field) {
                    FieldMetadata fieldMetadata = ((Field) expression).getFieldMetadata();
                    if (joinedTypes.contains(fieldMetadata.getContainingType())) {
                        TypeMapping mapping = mappings.getMappingFromDatabase(fieldMetadata.getContainingType());
                        filteredFields.add(new Alias(new StringConstant(StringUtils.EMPTY), mapping.getUser(fieldMetadata).getName()));
                    } else {
                        filteredFields.add(expression);
                    }
                } else {
                    filteredFields.add(expression);
                }
            }
            selectedFields.clear();
            selectedFields.addAll(filteredFields);
        }
        // Handle condition
        Condition condition = select.getCondition();
        if (condition == null) {
            throw new IllegalArgumentException("Expected a condition in select clause but got 0.");
        }
        // Create Lucene query (concatenates all sub queries together).
        FullTextSession fullTextSession = Search.getFullTextSession(session);
        Query parsedQuery = select.getCondition().accept(new LuceneQueryGenerator(types));
        // Create Hibernate Search query
        Set<Class> classes = new HashSet<Class>();
        for (ComplexTypeMetadata type : types) {
            String className = ClassCreator.getClassName(type.getName());
            try {
                ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
                classes.add(contextClassLoader.loadClass(className));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("Could not find class '" + className + "'.", e);
            }
        }
        FullTextQuery fullTextQuery = fullTextSession.createFullTextQuery(parsedQuery,
                classes.toArray(new Class<?>[classes.size()]));
        // Very important to leave this null (would disable ability to search across different types)
        fullTextQuery.setCriteriaQuery(null);
        fullTextQuery.setSort(Sort.RELEVANCE); // Default sort (if no order by specified).
        query = EntityFinder.wrap(fullTextQuery, (HibernateStorage) storage, session, select.getTypes()); // ensures only MDM entity objects are returned.
        // Order by
        for (OrderBy current : select.getOrderBy()) {
            current.accept(this);
        }
        // Paging
        Paging paging = select.getPaging();
        paging.accept(this);
        pageSize = paging.getLimit();
        boolean hasPaging = pageSize < Integer.MAX_VALUE;
        if (!hasPaging) {
            return createResults(query.scroll(ScrollMode.FORWARD_ONLY));
        } else {
            return createResults(query.list());
        }
    }

    @Override
    public StorageResults visit(Paging paging) {
        query.setFirstResult(paging.getStart());
        query.setFetchSize(AbstractQueryHandler.JDBC_FETCH_SIZE);
        query.setMaxResults(paging.getLimit());
        return null;
    }

    private StorageResults createResults(final List list) {
        CloseableIterator<DataRecord> iterator;
        if (selectedFields.isEmpty()) {
            iterator = new ListIterator(mappings, storageClassLoader, list.iterator(), callbacks);
        } else {
            iterator = new ListIterator(mappings, storageClassLoader, list.iterator(), callbacks) {
                @Override
                public DataRecord next() {
                    final DataRecord next = super.next();
                    final ComplexTypeMetadata explicitProjectionType = new ComplexTypeMetadataImpl(StringUtils.EMPTY, Storage.PROJECTION_TYPE, false);
                    final DataRecord nextRecord = new DataRecord(explicitProjectionType, UnsupportedDataRecordMetadata.INSTANCE);
                    VisitorAdapter<Void> visitor = new VisitorAdapter<Void>() {
                        private String aliasName;

                        @Override
                        public Void visit(Field field) {
                            FieldMetadata fieldMetadata = field.getFieldMetadata();
                            TypeMapping mapping = mappings.getMappingFromDatabase(fieldMetadata.getContainingType());
                            if (mapping != null && mapping.getUser(fieldMetadata) != null) {
                                fieldMetadata = mapping.getUser(fieldMetadata);
                            }
                            Object value;
                            if (fieldMetadata instanceof ReferenceFieldMetadata) {
                                value = getReferencedId(next, (ReferenceFieldMetadata) fieldMetadata);
                            } else {
                                value = next.get(fieldMetadata);
                            }
                            if (aliasName != null) {
                                SimpleTypeMetadata fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, fieldMetadata.getType().getName());
                                fieldMetadata = new SimpleTypeFieldMetadata(explicitProjectionType, false, false, false, aliasName, fieldType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY);
                                explicitProjectionType.addField(fieldMetadata);
                            } else {
                                explicitProjectionType.addField(fieldMetadata);
                            }
                            nextRecord.set(fieldMetadata, value);
                            return null;
                        }

                        @Override
                        public Void visit(StringConstant constant) {
                            if (aliasName != null) {
                                SimpleTypeMetadata fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING);
                                FieldMetadata fieldMetadata = new SimpleTypeFieldMetadata(explicitProjectionType, false, false, false, aliasName, fieldType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY);
                                explicitProjectionType.addField(fieldMetadata);
                                nextRecord.set(fieldMetadata, constant.getValue());
                            } else {
                                throw new IllegalStateException("Expected an alias for a constant expression.");
                            }
                            return null;
                        }

                        @Override
                        public Void visit(Count count) {
                            if (aliasName != null) {
                                SimpleTypeMetadata fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, count.getTypeName());
                                FieldMetadata fieldMetadata = new SimpleTypeFieldMetadata(explicitProjectionType, false, false, false, aliasName, fieldType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY);
                                explicitProjectionType.addField(fieldMetadata);
                                nextRecord.set(fieldMetadata, list.size());
                            }
                            return null;
                        }

                        @Override
                        public Void visit(Alias alias) {
                            aliasName = alias.getAliasName();
                            {
                                alias.getTypedExpression().accept(this);
                            }
                            aliasName = null;
                            return null;
                        }

                        private Void handleMetadataField(MetadataField field) {
                            SimpleTypeMetadata fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, field.getTypeName());
                            String fieldName = aliasName == null ? field.getFieldName() : aliasName;
                            SimpleTypeFieldMetadata aliasField = new SimpleTypeFieldMetadata(explicitProjectionType, false, false, false, fieldName, fieldType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY);
                            explicitProjectionType.addField(aliasField);
                            nextRecord.set(aliasField, field.getReader().readValue(next));
                            return null;
                        }

                        @Override
                        public Void visit(Timestamp timestamp) {
                            return handleMetadataField(timestamp);
                        }

                        @Override
                        public Void visit(TaskId taskId) {
                            return handleMetadataField(taskId);
                        }

                        @Override
                        public Void visit(StagingStatus stagingStatus) {
                            return handleMetadataField(stagingStatus);
                        }

                        @Override
                        public Void visit(StagingError stagingError) {
                            return handleMetadataField(stagingError);
                        }

                        @Override
                        public Void visit(StagingSource stagingSource) {
                            return handleMetadataField(stagingSource);
                        }

                        @Override
                        public Void visit(StagingBlockKey stagingBlockKey) {
                            return handleMetadataField(stagingBlockKey);
                        }

                        @Override
                        public Void visit(Type type) {
                            FieldMetadata fieldMetadata = type.getField().getFieldMetadata();
                            SimpleTypeMetadata fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING);
                            SimpleTypeFieldMetadata aliasField = new SimpleTypeFieldMetadata(explicitProjectionType, false, false, false, aliasName, fieldType, Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), StringUtils.EMPTY);
                            explicitProjectionType.addField(aliasField);
                            DataRecord dataRecord = (DataRecord) next.get(fieldMetadata.getName());
                            if (dataRecord != null) {
                                nextRecord.set(aliasField, dataRecord.getType().getName());
                            } else {
                                nextRecord.set(aliasField, StringUtils.EMPTY);
                            }
                            return null;
                        }
                    };
                    for (TypedExpression selectedField : selectedFields) {
                        selectedField.accept(visitor);
                    }
                    return nextRecord;
                }
            };
        }
        return new FullTextStorageResults(pageSize, list.size(), iterator);
    }

    private StorageResults createResults(ScrollableResults scrollableResults) {
        CloseableIterator<DataRecord> iterator;
        if (selectedFields.isEmpty()) {
            iterator = new ScrollableIterator(mappings,
                    storageClassLoader,
                    scrollableResults,
                    callbacks);
        } else {
            iterator = new ScrollableIterator(mappings,
                    storageClassLoader,
                    scrollableResults,
                    callbacks) {
                @Override
                public DataRecord next() {
                    DataRecord next = super.next();
                    ComplexTypeMetadata explicitProjectionType = new ComplexTypeMetadataImpl(StringUtils.EMPTY,
                            Storage.PROJECTION_TYPE,
                            false);
                    DataRecord nextRecord = new DataRecord(explicitProjectionType, UnsupportedDataRecordMetadata.INSTANCE);
                    for (TypedExpression selectedField : selectedFields) {
                        if (selectedField instanceof Field) {
                            FieldMetadata field = ((Field) selectedField).getFieldMetadata();
                            TypeMapping mapping = mappings.getMappingFromDatabase(field.getContainingType());
                            if (mapping != null && mapping.getUser(field) != null) {
                                field = mapping.getUser(field);
                            }
                            explicitProjectionType.addField(field);
                            if (field instanceof ReferenceFieldMetadata) {
                                nextRecord.set(field, getReferencedId(next, (ReferenceFieldMetadata) field));
                            } else {
                                nextRecord.set(field, next.get(field));
                            }
                        } else if (selectedField instanceof Alias) {
                            SimpleTypeMetadata fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, selectedField.getTypeName());
                            Alias alias = (Alias) selectedField;
                            SimpleTypeFieldMetadata newField = new SimpleTypeFieldMetadata(explicitProjectionType,
                                    false,
                                    false,
                                    false,
                                    alias.getAliasName(),
                                    fieldType,
                                    Collections.<String>emptyList(),
                                    Collections.<String>emptyList(),
                                    Collections.<String>emptyList(),
                                    StringUtils.EMPTY);
                            explicitProjectionType.addField(newField);
                            TypedExpression typedExpression = alias.getTypedExpression();
                            if (typedExpression instanceof StringConstant) {
                                nextRecord.set(newField, ((StringConstant) typedExpression).getValue());
                            } else if(typedExpression instanceof Field) {
                                nextRecord.set(newField, next.get(((Field) typedExpression).getFieldMetadata()));
                            } else if(typedExpression instanceof Type) {
                                FieldMetadata fieldMetadata = ((Type) typedExpression).getField().getFieldMetadata();
                                nextRecord.set(newField, ((DataRecord) next.get(fieldMetadata.getName())).getType().getName());
                            } else if (typedExpression instanceof MetadataField) {
                                nextRecord.set(newField, ((MetadataField) typedExpression).getReader().readValue(next));
                            } else {
                                throw new IllegalArgumentException("Aliased expression '" + typedExpression + "' is not supported.");
                            }
                        } else if (selectedField instanceof MetadataField) {
                            SimpleTypeMetadata fieldType = new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, selectedField.getTypeName());
                            SimpleTypeFieldMetadata newField = new SimpleTypeFieldMetadata(explicitProjectionType,
                                    false,
                                    false,
                                    false,
                                    ((MetadataField) selectedField).getFieldName(),
                                    fieldType,
                                    Collections.<String>emptyList(),
                                    Collections.<String>emptyList(),
                                    Collections.<String>emptyList(),
                                    StringUtils.EMPTY);
                            explicitProjectionType.addField(newField);
                            nextRecord.set(newField, ((MetadataField) selectedField).getReader().readValue(next));
                        }
                    }
                    explicitProjectionType.freeze();
                    return nextRecord;
                }
            };
        }

        return new FullTextStorageResults(pageSize, query.getResultSize(), iterator);
    }

    private static Object getReferencedId(DataRecord next, ReferenceFieldMetadata field) {
        DataRecord record = (DataRecord) next.get(field);
        if (record != null) {
            Collection<FieldMetadata> keyFields = record.getType().getKeyFields();
            if (keyFields.size() == 1) {
                return record.get(keyFields.iterator().next());
            } else {
                List<Object> compositeKeyValues = new ArrayList<Object>(keyFields.size());
                for (FieldMetadata keyField : keyFields) {
                    compositeKeyValues.add(record.get(keyField));
                }
                return compositeKeyValues.toArray(new Object[keyFields.size()]);
            }
        } else {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public StorageResults visit(OrderBy orderBy) {
        TypedExpression field = orderBy.getExpression();
        if (field instanceof Field) {
            FieldMetadata fieldMetadata = ((Field) field).getFieldMetadata();
            SortField sortField = new SortField(fieldMetadata.getName(),
                    getSortType(fieldMetadata),
                    orderBy.getDirection() == OrderBy.Direction.DESC);
            query.setSort(new Sort(sortField));
            return null;
        } else {
            throw new NotImplementedException("No support for order by for full text search on non-field.");
        }
    }

    private static SortField.Type getSortType(FieldMetadata fieldMetadata) {
        TypeMetadata fieldType = fieldMetadata.getType();
        String type = MetadataUtils.getSuperConcreteType(fieldType).getName();
        if (Types.STRING.equals(type)
                || Types.ANY_URI.equals(type)
                || Types.BOOLEAN.equals(type)
                || Types.BASE64_BINARY.equals(type)
                || Types.QNAME.equals(type)
                || Types.HEX_BINARY.equals(type)
                || Types.DURATION.equals(type)) {
            return SortField.Type.STRING_VAL; // STRING does not work well for 'long' strings.
        } else if (Types.INT.equals(type)
                || Types.INTEGER.equals(type)
                || Types.POSITIVE_INTEGER.equals(type)
                || Types.NON_POSITIVE_INTEGER.equals(type)
                || Types.NON_NEGATIVE_INTEGER.equals(type)
                || Types.NEGATIVE_INTEGER.equals(type)
                || Types.UNSIGNED_INT.equals(type)) {
            return SortField.Type.INT;
        } else if (Types.DECIMAL.equals(type) || Types.DOUBLE.equals(type)) {
            return SortField.Type.DOUBLE;
        } else if (Types.DATE.equals(type)
                || Types.DATETIME.equals(type)
                || Types.TIME.equals(type)) {
            return SortField.Type.STRING;
        } else if (Types.UNSIGNED_SHORT.equals(type) || Types.SHORT.equals(type)) {
            return SortField.Type.SHORT;
        } else if (Types.UNSIGNED_LONG.equals(type) || Types.LONG.equals(type)) {
            return SortField.Type.LONG;
        } else if (Types.FLOAT.equals(type)) {
            return SortField.Type.FLOAT;
        } else if (Types.BYTE.equals(type) || Types.UNSIGNED_BYTE.equals(type)) {
            return SortField.Type.BYTE;
        } else {
            throw new UnsupportedOperationException("No support for field typed as '" + type + "'");
        }
    }

    private static class FullTextStorageResults implements StorageResults {

        private final int size;

        private final int count;

        private final CloseableIterator<DataRecord> iterator;

        public FullTextStorageResults(int size, int count, CloseableIterator<DataRecord> iterator) {
            this.size = size;
            this.count = count;
            this.iterator = iterator;
        }

        public int getSize() {
            if (size == Integer.MAX_VALUE) {
                return getCount();
            }
            return size;
        }

        public int getCount() {
            return count;
        }

        public void close() {
            try {
                iterator.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public Iterator<DataRecord> iterator() {
            return iterator;
        }
    }

    private static class SearchTransitiveClosure extends VisitorAdapter<Collection<? extends ComplexTypeMetadata>> {

        final Storage storage;
        
        private final Set<ComplexTypeMetadata> closure = new HashSet<ComplexTypeMetadata>();

        public SearchTransitiveClosure(Storage storage) {
            this.storage = storage;
        }
        
        @Override
        public Collection<? extends ComplexTypeMetadata> visit(Range range) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(Select select) {
            closure.addAll(select.getTypes());
            if (select.getCondition() != null) {
                select.getCondition().accept(this);
            }
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(Alias alias) {
            alias.getTypedExpression().accept(this);
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(Field field) {
            closure.add(field.getFieldMetadata().getContainingType());
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(Compare condition) {
            condition.getLeft().accept(this);
            condition.getRight().accept(this);
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(BinaryLogicOperator condition) {
            condition.getLeft().accept(this);
            condition.getRight().accept(this);
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(UnaryLogicOperator condition) {
            condition.getCondition().accept(this);
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(FullText fullText) {
            if (closure != null && closure.size() > 0) {
                for (ComplexTypeMetadata ctm : closure) {
                    closure.addAll(ctm.accept(new ComplexTypeMetadataOptimization(this.storage)));
                }
            }
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(FieldFullText fieldFullText) {
            closure.add(fieldFullText.getField().getFieldMetadata().getContainingType());
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(ConstantCollection collection) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(StringConstant constant) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(IntegerConstant constant) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(DateConstant constant) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(DateTimeConstant constant) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(BooleanConstant constant) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(BigDecimalConstant constant) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(TimeConstant constant) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(ShortConstant constant) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(ByteConstant constant) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(LongConstant constant) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(DoubleConstant constant) {
            return closure;
        }

        @Override
        public Collection<? extends ComplexTypeMetadata> visit(FloatConstant constant) {
            return closure;
        }
    }

    private static class ComplexTypeMetadataOptimization extends DefaultMetadataVisitor<Set<ComplexTypeMetadata>> {

        private final Storage storage;
        private final Set<ComplexTypeMetadata> complexTypeMetadataSet = new HashSet<ComplexTypeMetadata>();

        public ComplexTypeMetadataOptimization(Storage storage) {
            this.storage = storage;
        }

        @Override
        public Set<ComplexTypeMetadata> visit(ComplexTypeMetadata complexTypeMetadata) {
            if (complexTypeMetadata != null && !complexTypeMetadataSet.contains(complexTypeMetadata)) {
                complexTypeMetadataSet.add(complexTypeMetadata);
                for (FieldMetadata fm : complexTypeMetadata.getFields()) {
                    fm.accept(this);
                }
            }
            return complexTypeMetadataSet;
        }

        @Override
        public Set<ComplexTypeMetadata> visit(ContainedComplexTypeMetadata containedType) {
            if (containedType != null && !complexTypeMetadataSet.contains(containedType)) {
                if (!this.storage.getMetadataRepository().getInstantiableTypes().contains(containedType)) {
                    complexTypeMetadataSet.add(containedType);
                    for (FieldMetadata fm : containedType.getFields()) {
                        fm.accept(this);
                    }
                }
            }
            return complexTypeMetadataSet;
        }

        @Override
        public Set<ComplexTypeMetadata> visit(ReferenceFieldMetadata referenceField) {
            if (referenceField != null && !complexTypeMetadataSet.contains(referenceField.getReferencedType())) {
                if (!this.storage.getMetadataRepository().getInstantiableTypes().contains(referenceField.getReferencedType())) {
                    complexTypeMetadataSet.add(referenceField.getReferencedType());
                    for (FieldMetadata fm : referenceField.getReferencedType().getFields()) {
                        fm.accept(this);
                    }
                }
            }
            return complexTypeMetadataSet;
        }

        @Override
        public Set<ComplexTypeMetadata> visit(ContainedTypeFieldMetadata containedField) {
            if (containedField != null && !complexTypeMetadataSet.contains(containedField.getContainedType())) {
                if (!this.storage.getMetadataRepository().getInstantiableTypes().contains(containedField.getContainedType())) {
                    complexTypeMetadataSet.add(containedField.getContainedType());
                    for (FieldMetadata fm : containedField.getContainedType().getFields()) {
                        fm.accept(this);
                    }
                }
            }
            return complexTypeMetadataSet;
        }
    }
}
