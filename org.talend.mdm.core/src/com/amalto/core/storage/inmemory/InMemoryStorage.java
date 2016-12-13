/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.inmemory;

import java.util.*;

import javax.xml.XMLConstants;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.*;
import org.talend.mdm.commmon.metadata.compare.Change;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;
import org.talend.mdm.commmon.metadata.compare.Compare.DiffResults;

import com.amalto.core.query.user.*;
import com.amalto.core.query.user.metadata.StagingBlockKey;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.StagingStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.inmemory.matcher.*;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.UnsupportedDataRecordMetadata;
import com.amalto.core.storage.transaction.StorageTransaction;
import com.amalto.core.storage.transaction.TransactionManager;

/**
 * A (very) simple implementation of storage to provide a {@link com.amalto.core.storage.Storage storage} API over a
 * group of {@link com.amalto.core.storage.record.DataRecord records} in memory.
 */
public class InMemoryStorage implements Storage {

    private static final String STORAGE_NAME = "IN-MEMORY"; //$NON-NLS-1$

    private static final InMemoryImpactAnalyzer inMemoryImpactAnalyzer = new InMemoryImpactAnalyzer();

    private final Set<DataRecord> storage = new HashSet<DataRecord>();

    private MetadataRepository repository;

    private DataSource dataSource;

    private boolean isClosed;

    @Override
    public Storage asInternal() {
        return this;
    }

    @Override
    public int getCapabilities() {
        return CAP_TRANSACTION;
    }

    @Override
    public StorageTransaction newStorageTransaction() {
        return new InMemoryStorageTransaction(this);
    }

    @Override
    public void init(DataSourceDefinition dataSource) {
        this.dataSource = dataSource.get(StorageType.MASTER);
    }

    @Override
    public void prepare(MetadataRepository repository, Set<Expression> optimizedExpressions, boolean force,
            boolean dropExistingData) {
        this.repository = repository;
    }

    @Override
    public void prepare(MetadataRepository repository, boolean dropExistingData) {
        // Nothing to do
        prepare(repository, Collections.<Expression>emptySet(), false, true);
    }

    @Override
    public MetadataRepository getMetadataRepository() {
        return repository;
    }

    @Override
    public StorageResults fetch(Expression userQuery) {
        Expression expression = userQuery.normalize();
        Matcher matcher = expression.accept(new MatcherCreator());
        List<DataRecord> matchRecords = new LinkedList<DataRecord>();
        for (DataRecord dataRecord : storage) {
            if (matcher.match(dataRecord)) {
                matchRecords.add(dataRecord);
            }
        }
        List<DataRecord> filteredRecords = expression.accept(new Filter(matchRecords));
        return new InMemoryStorageResults(filteredRecords);
    }

    @Override
    public void update(DataRecord record) {
        update(Collections.singletonList(record));
    }

    @Override
    public void update(Iterable<DataRecord> records) {
        for (DataRecord record : records) {
            storage.add(record);
        }
    }

    @Override
    public void delete(Expression userQuery) {
        throw new NotImplementedException();
    }

    @Override
    public void delete(DataRecord record) {
        storage.remove(record);
    }

    @Override
    public void close() {
        close(false);
    }

    @Override
    public void close(boolean dropExistingData) {
        storage.clear();
        isClosed = true;
    }

    @Override
    public void begin() {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        transactionManager.currentTransaction().include(this).begin();
    }

    @Override
    public void commit() {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        transactionManager.currentTransaction().include(this).commit();
    }

    @Override
    public void rollback() {
        TransactionManager transactionManager = ServerContext.INSTANCE.get().getTransactionManager();
        transactionManager.currentTransaction().include(this).rollback();
    }

    @Override
    public void end() {
        // Nothing to do
    }

    @Override
    public void reindex() {
        // Nothing to do
    }

    @Override
    public Set<String> getFullTextSuggestion(String keyword, FullTextSuggestion mode, int suggestionSize) {
        return Collections.emptySet();
    }

    @Override
    public String getName() {
        return STORAGE_NAME;
    }

    @Override
    public DataSource getDataSource() {
        return dataSource;
    }

    @Override
    public StorageType getType() {
        return StorageType.MASTER;
    }

    @Override
    public ImpactAnalyzer getImpactAnalyzer() {
        return inMemoryImpactAnalyzer;
    }

    @Override
    public void adapt(MetadataRepository newRepository, boolean force) {
        this.repository = newRepository;
    }

    @Override
    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public List<ComplexTypeMetadata> findSortedTypesToDrop(DiffResults diffResults, boolean force) {
        return new ArrayList<ComplexTypeMetadata>();
    }

    private static interface ValueBuilder {

        Object getValue(DataRecord record);

    }

    private static interface AggregateValueBuilder {

        Collection<Object> getValues(List<DataRecord> records);

    }

    private static class InMemoryImpactAnalyzer implements ImpactAnalyzer {

        @Override
        public Map<Impact, List<Change>> analyzeImpacts(Compare.DiffResults diffResult) {
            Map<Impact, List<Change>> impacts = new HashMap<Impact, List<Change>>();
            impacts.put(Impact.LOW, diffResult.getActions());
            return impacts;
        }
    }

    private static class InMemoryStorageTransaction extends StorageTransaction {

        private final Storage storage;

        public InMemoryStorageTransaction(Storage storage) {
            super();
            this.storage = storage;
        }

        @Override
        public Storage getStorage() {
            return storage;
        }

        @Override
        public void begin() {
        }

        @Override
        public boolean hasFailed() {
            return false;
        }
    }

    private static class InMemoryStorageResults implements StorageResults {

        private final List<DataRecord> matchRecords;

        public InMemoryStorageResults(List<DataRecord> matchRecords) {
            this.matchRecords = matchRecords;
        }

        @Override
        public int getSize() {
            return matchRecords.size();
        }

        @Override
        public int getCount() {
            return matchRecords.size();
        }

        @Override
        public void close() {
        }

        @Override
        public Iterator<DataRecord> iterator() {
            return matchRecords.iterator();
        }
    }

    private static class MatcherCreator extends VisitorAdapter<Matcher> {

        @Override
        public Matcher visit(Select select) {
            Condition condition = select.getCondition();
            if (condition != null) {
                return condition.accept(this);
            } else {
                return new MatchAll();
            }
        }

        @Override
        public Matcher visit(IsNull isNull) {
            return isNull.getField().accept(new VisitorAdapter<Matcher>() {

                @Override
                public Matcher visit(final Field field) {
                    return new Matcher() {

                        @Override
                        public boolean match(DataRecord record) {
                            return record.get(field.getFieldMetadata()) == null;
                        }
                    };
                }

                @Override
                public Matcher visit(StagingBlockKey stagingBlockKey) {
                    return new Matcher() {

                        @Override
                        public boolean match(DataRecord record) {
                            return record.getRecordMetadata().getRecordProperties()
                                    .get(StagingStorage.METADATA_STAGING_BLOCK_KEY) == null;
                        }
                    };
                }
            });
        }

        @Override
        public Matcher visit(final com.amalto.core.query.user.Compare condition) {
            final String value = condition.getRight().accept(new VisitorAdapter<String>() {

                @Override
                public String visit(StringConstant constant) {
                    return constant.getValue();
                }

                @Override
                public String visit(IntegerConstant constant) {
                    return String.valueOf(constant.getValue());
                }

                @Override
                public String visit(DateConstant constant) {
                    synchronized (DateConstant.DATE_FORMAT) {
                        return DateConstant.DATE_FORMAT.format(constant.getValue());
                    }
                }

                @Override
                public String visit(DateTimeConstant constant) {
                    synchronized (DateTimeConstant.DATE_FORMAT) {
                        return DateTimeConstant.DATE_FORMAT.format(constant.getValue());
                    }
                }

                @Override
                public String visit(BooleanConstant constant) {
                    return String.valueOf(constant.getValue());
                }

                @Override
                public String visit(BigDecimalConstant constant) {
                    return String.valueOf(constant.getValue());
                }

                @Override
                public String visit(TimeConstant constant) {
                    synchronized (TimeConstant.TIME_FORMAT) {
                        return TimeConstant.TIME_FORMAT.format(constant.getValue());
                    }
                }

                @Override
                public String visit(ShortConstant constant) {
                    return String.valueOf(constant.getValue());
                }

                @Override
                public String visit(ByteConstant constant) {
                    return String.valueOf(constant.getValue());
                }

                @Override
                public String visit(LongConstant constant) {
                    return String.valueOf(constant.getValue());
                }

                @Override
                public String visit(DoubleConstant constant) {
                    return String.valueOf(constant.getValue());
                }

                @Override
                public String visit(FloatConstant constant) {
                    return String.valueOf(constant.getValue());
                }
            });
            return condition.getLeft().accept(new VisitorAdapter<Matcher>() {

                @Override
                public Matcher visit(StagingBlockKey stagingBlockKey) {
                    return new BuiltInBlockKeyMatcher(condition.getPredicate(), value);
                }

                @Override
                public Matcher visit(Field field) {
                    return new CompareMatcher(field.getFieldMetadata(), condition.getPredicate(), value);
                }
            });
        }

        @Override
        public Matcher visit(BinaryLogicOperator condition) {
            Matcher leftMatcher = condition.getLeft().accept(this);
            Matcher rightMatcher = condition.getRight().accept(this);
            return new BinaryMatcher(leftMatcher, condition.getPredicate(), rightMatcher);
        }

        @Override
        public Matcher visit(UnaryLogicOperator condition) {
            Matcher conditionMatcher = condition.getCondition().accept(this);
            return new UnaryMatcher(conditionMatcher, condition.getPredicate());
        }
    }

    private static class Filter extends VisitorAdapter<List<DataRecord>> {

        private final List<DataRecord> records;

        private final Map<FieldMetadata, ValueBuilder> recordProjection = new HashMap<FieldMetadata, ValueBuilder>();

        private final Map<FieldMetadata, AggregateValueBuilder> aggregateProjection = new HashMap<FieldMetadata, AggregateValueBuilder>();

        private final ComplexTypeMetadata explicitProjection = new ComplexTypeMetadataImpl("", "ExplicitProjectionType", true);

        private FieldMetadata lastField;

        public Filter(List<DataRecord> records) {
            this.records = records;
        }

        @Override
        public List<DataRecord> visit(Select select) {
            if (!select.isProjection()) {
                return records;
            } else {
                List<DataRecord> filteredRecords = new LinkedList<DataRecord>();
                for (TypedExpression expression : select.getSelectedFields()) {
                    expression.accept(this);
                }
                if (!aggregateProjection.isEmpty()) {
                    for (FieldMetadata fieldMetadata : aggregateProjection.keySet()) {
                        explicitProjection.addField(fieldMetadata);
                    }
                    for (Map.Entry<FieldMetadata, AggregateValueBuilder> entry : aggregateProjection.entrySet()) {
                        Collection<Object> aggregateValues = entry.getValue().getValues(records);
                        for (Object aggregateValue : aggregateValues) {
                            DataRecord newRecord = new DataRecord(explicitProjection, UnsupportedDataRecordMetadata.INSTANCE);
                            newRecord.set(entry.getKey(), aggregateValue);
                            filteredRecords.add(newRecord);
                        }
                    }
                } else if (!recordProjection.isEmpty()) {
                    ComplexTypeMetadata explicitProjection = new ComplexTypeMetadataImpl("", "ExplicitProjectionType", true);
                    for (FieldMetadata fieldMetadata : recordProjection.keySet()) {
                        explicitProjection.addField(fieldMetadata);
                    }
                    for (DataRecord inputReport : records) {
                        DataRecord newRecord = new DataRecord(explicitProjection, UnsupportedDataRecordMetadata.INSTANCE);
                        for (Map.Entry<FieldMetadata, ValueBuilder> entry : recordProjection.entrySet()) {
                            newRecord.set(entry.getKey(), entry.getValue().getValue(inputReport));
                        }
                    }
                }
                return filteredRecords;
            }
        }

        @Override
        public List<DataRecord> visit(Field field) {
            lastField = field.getFieldMetadata();
            recordProjection.put(lastField, new ValueBuilder() {

                @Override
                public Object getValue(DataRecord record) {
                    return record.get(lastField);
                }
            });
            return records;
        }

        @Override
        public List<DataRecord> visit(Alias alias) {
            alias.getTypedExpression().accept(this);
            FieldMetadata aliasField = new SimpleTypeFieldMetadata(explicitProjection, false, lastField.isMany(),
                    lastField.isMandatory(), alias.getAliasName(), lastField.getType(), Collections.<String> emptyList(),
                    Collections.<String> emptyList(), Collections.<String> emptyList(), StringUtils.EMPTY);
            ValueBuilder previousValueBuilder = recordProjection.remove(lastField);
            if (previousValueBuilder == null) {
                AggregateValueBuilder previous = aggregateProjection.remove(lastField);
                aggregateProjection.put(aliasField, previous);
            } else {
                recordProjection.put(aliasField, previousValueBuilder);
            }
            return records;
        }

        @Override
        public List<DataRecord> visit(StagingBlockKey stagingBlockKey) {
            FieldMetadata blockField = new SimpleTypeFieldMetadata(explicitProjection, false, false, false,
                    "blockKey", //$NON-NLS-1$
                    new SimpleTypeMetadata(XMLConstants.W3C_XML_SCHEMA_NS_URI, Types.STRING), Collections.<String> emptyList(),
                    Collections.<String> emptyList(), Collections.<String> emptyList(), StringUtils.EMPTY);
            lastField = blockField;
            recordProjection.put(blockField, new ValueBuilder() {

                @Override
                public Object getValue(DataRecord record) {
                    return record.getRecordMetadata().getRecordProperties().get(StagingStorage.METADATA_STAGING_BLOCK_KEY);
                }
            });
            return records;
        }

        @Override
        public List<DataRecord> visit(final Distinct distinct) {
            distinct.getExpression().accept(this);
            final ValueBuilder builder = recordProjection.remove(lastField);
            aggregateProjection.put(lastField, new AggregateValueBuilder() {

                @Override
                public Collection<Object> getValues(List<DataRecord> records) {
                    Set<Object> distinctObjects = new HashSet<Object>();
                    for (DataRecord record : records) {
                        distinctObjects.add(builder.getValue(record));
                    }
                    return distinctObjects;
                }
            });
            return records;
        }
    }

}
