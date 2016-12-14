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

import com.amalto.core.query.analysis.ConditionChecks;
import com.amalto.core.query.analysis.Result;
import com.amalto.core.query.user.*;
import com.amalto.core.query.user.metadata.*;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.EmptyIterator;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.inmemory.InMemoryJoinStrategy;
import com.amalto.core.storage.transaction.Transaction;
import com.amalto.core.storage.transaction.TransactionManager;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.talend.mdm.commmon.metadata.*;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

class SelectAnalyzer extends VisitorAdapter<Visitor<StorageResults>> {

    private static final Logger LOGGER = Logger.getLogger(SelectAnalyzer.class);

    private static final boolean ALLOW_IN_MEMORY_JOINS = Boolean.valueOf(MDMConfiguration.getConfiguration().getProperty("db.allow.memory.join", "false")); //$NON-NLS-1$ //$NON-NLS-2$

    private final List<TypedExpression> selectedFields = new LinkedList<TypedExpression>();

    private final MappingRepository mappings;

    private final StorageClassLoader storageClassLoader;

    private final Session session;

    private final Set<ResultsCallback> callbacks;

    private final Storage storage;

    private final TableResolver resolver;

    private boolean isFullText = false;

    private boolean isCheckingProjection;

    private FullText fullTextExpression;

    SelectAnalyzer(MappingRepository mappings,
                   StorageClassLoader storageClassLoader,
                   Session session,
                   Set<ResultsCallback> callbacks,
                   Storage storage,
                   TableResolver resolver) {
        this.mappings = mappings;
        this.storageClassLoader = storageClassLoader;
        this.session = session;
        this.callbacks = callbacks;
        this.storage = storage;
        this.resolver = resolver;
    }

    @Override
    public Visitor<StorageResults> visit(NativeQuery nativeQuery) {
        return new NativeQueryHandler(storage, storageClassLoader, session, callbacks);
    }

    @Override
    public Visitor<StorageResults> visit(Select select) {
        List<TypedExpression> selectedFields = select.getSelectedFields();
        isCheckingProjection = true;
        {
            for (Expression selectedField : selectedFields) {
                selectedField.accept(this);
            }
        }
        isCheckingProjection = false;
        Condition condition = select.getCondition();
        if (condition != null) {
            // Shortcut: condition is set to "FALSE", means no record should ever match, return empty record.
            if (condition == UserQueryHelper.FALSE) {
                return new EmptyResultAdapter();
            } else {
                condition.accept(this);
            }
        }
        // Full text
        if (isFullText) {
            DataSource dataSource = storage.getDataSource();
            RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) dataSource;
            String fullTextValue = fullTextExpression.getValue().trim();
            if (fullTextValue.isEmpty() || StringUtils.containsOnly(fullTextValue, new char[]{'*'})) {
                if (select.getTypes().size() == 1) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Using \"standard query\" strategy (full text query on '*' or ' ')");
                    }
                    return new StandardQueryHandler(storage, mappings, resolver, storageClassLoader, session, select, this.selectedFields, callbacks);
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Using \"multi type\" strategy (full text query on '*' or ' ')");
                    }
                    return new MultiTypeStrategy(storage);
                }
            }
            if (rdbmsDataSource.supportFullText()) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Using \"full text query\" strategy");
                }
                return new FullTextQueryHandler(storage, mappings, storageClassLoader, session, select, this.selectedFields, callbacks);
            } else {
                throw new IllegalArgumentException("Storage '" + storage.getName() + "(" + storage.getType().name() + ")' is not configured to support full text queries.");
            }
        }
        // Condition optimizations
        if (condition != null) {
            ConditionChecks conditionChecks = new ConditionChecks(select);
            Result result = condition.accept(conditionChecks);
            if (result.id && !select.isProjection()) { // TMDM-5965: IdQueryHandler has trouble with projections using
                                                       // reusable type's elements.
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Using \"get by id\" strategy");
                }
                return new IdQueryHandler(storage, mappings, storageClassLoader, session, select, this.selectedFields, callbacks);
            }
            if (ALLOW_IN_MEMORY_JOINS && result.limitJoins) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Using \"in memory\" strategy");
                }
                return new InMemoryJoinStrategy(storage, mappings);
            }
        }
        // Instance paging (TMDM-5388).
        int limit = select.getPaging().getLimit();
        if (!select.isProjection() && select.getTypes().size() == 1) {
            ComplexTypeMetadata uniqueType = select.getTypes().get(0);
            if (uniqueType.getSubTypes().isEmpty() && uniqueType.getSuperTypes().isEmpty()) {
                if (limit < Integer.MAX_VALUE) {
                    TypeMapping mappingFromDatabase = mappings.getMappingFromDatabase(uniqueType);
                    if (allowInClauseOptimization(mappingFromDatabase)) {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Using \"id in clause\" strategy");
                        }
                        return new InClauseOptimization(storage,
                                mappings,
                                resolver,
                                storageClassLoader,
                                session,
                                select,
                                this.selectedFields,
                                callbacks,
                                InClauseOptimization.Mode.CONSTANT);
                    }
                } else if(select.getPaging().getStart() == 0) {
                    // Scroll over instances
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Using \"scroll over instances\" strategy");
                    }
                    return new InstanceScrollOptimization(storage,
                            mappings,
                            resolver,
                            storageClassLoader,
                            session,
                            select,
                            this.selectedFields,
                            callbacks);
                }
            }
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Using \"standard query\" strategy");
        }
        return new StandardQueryHandler(storage, mappings, resolver, storageClassLoader, session, select, this.selectedFields, callbacks);
    }

    private static boolean allowInClauseOptimization(TypeMapping mappingFromDatabase) {
        boolean allowInClauseOptimization = mappingFromDatabase instanceof ScatteredTypeMapping;
        boolean containsManyField = false;
        int referenceFieldCount = 0;
        if (mappingFromDatabase instanceof FlatTypeMapping) {
            Collection<FieldMetadata> fields = mappingFromDatabase.getDatabase().getFields();
            for (FieldMetadata field : fields) {
                if (field instanceof ReferenceFieldMetadata) {
                    referenceFieldCount++;
                }
                if (field.isMany()) {
                    containsManyField = true;
                }
            }
        }
        // Last but not least, in clause optimization includes projections that don't get along with locking, check this
        // before choosing this optimization (see https://hibernate.atlassian.net/browse/HHH-3313).
        TransactionManager manager = ServerContext.INSTANCE.get().getTransactionManager();
        Transaction.LockStrategy lockStrategy = manager.currentTransaction().getLockStrategy();
        if (lockStrategy == Transaction.LockStrategy.LOCK_FOR_UPDATE) {
            return false;
        }
        return allowInClauseOptimization || containsManyField || referenceFieldCount > 1;
    }

    @Override
    public Visitor<StorageResults> visit(Distinct distinct) {
        return null;
    }

    @Override
    public Visitor<StorageResults> visit(GroupSize groupSize) {
        if (isCheckingProjection && StorageType.STAGING == storage.getType()) {
            selectedFields.add(groupSize);
        }
        return null;
    }

    @Override
    public Visitor<StorageResults> visit(Max max) {
        return null;
    }

    @Override
    public Visitor<StorageResults> visit(Min min) {
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(Isa isa) {
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(StringConstant constant) {
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(IsEmpty isEmpty) {
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(IsNull isNull) {
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(NotIsEmpty notIsEmpty) {
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(NotIsNull notIsNull) {
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(Alias alias) {
        if (isCheckingProjection) {
            selectedFields.add(alias);
        }
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(Timestamp timestamp) {
        if (isCheckingProjection) {
            selectedFields.add(timestamp);
        }
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(TaskId taskId) {
        if (isCheckingProjection) {
            selectedFields.add(taskId);
        }
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(Type type) {
        if (isCheckingProjection) {
            selectedFields.add(type);
        }
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(BinaryLogicOperator condition) {
        condition.getLeft().accept(this);
        condition.getRight().accept(this);
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(UnaryLogicOperator condition) {
        condition.getCondition().accept(this);
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(Compare condition) {
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(StagingStatus stagingStatus) {
        if (isCheckingProjection && StorageType.STAGING == storage.getType()) {
            selectedFields.add(stagingStatus);
        }
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(StagingError stagingError) {
        if (isCheckingProjection && StorageType.STAGING == storage.getType()) {
            selectedFields.add(stagingError);
        }
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(StagingSource stagingSource) {
        if (isCheckingProjection && StorageType.STAGING == storage.getType()) {
            selectedFields.add(stagingSource);
        }
        return null;
    }

    @Override
    public Visitor<StorageResults> visit(StagingBlockKey stagingBlockKey) {
        if (isCheckingProjection && StorageType.STAGING == storage.getType()) {
            selectedFields.add(stagingBlockKey);
        }
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(Condition condition) {
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(Count count) {
        if (isCheckingProjection) {
            selectedFields.add(count);
        }
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(FullText fullText) {
        fullTextExpression = fullText;
        isFullText = true;
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(FieldFullText fieldFullText) {
        fullTextExpression = fieldFullText;
        isFullText = true;
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(Range range) {
        return null;
    }

    @Override
    public VisitorAdapter<StorageResults> visit(Field field) {
        selectedFields.add(field);
        return null;
    }

    private class EmptyResultAdapter extends VisitorAdapter<StorageResults> {

        @Override
        public StorageResults visit(Select select) {
            return new HibernateStorageResults(storage, select, EmptyIterator.INSTANCE);
        }
    }
}
