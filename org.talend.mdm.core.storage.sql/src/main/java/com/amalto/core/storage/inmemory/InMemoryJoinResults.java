/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.inmemory;

import com.amalto.core.query.user.*;
import com.amalto.core.storage.EmptyIterator;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.CloseableIterator;
import com.amalto.core.storage.hibernate.MappingRepository;
import com.amalto.core.storage.hibernate.TypeMapping;
import com.amalto.core.storage.record.DataRecord;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import java.util.*;

import static com.amalto.core.query.user.UserQueryBuilder.from;

class InMemoryJoinResults implements StorageResults {

    private static final Logger LOGGER = Logger.getLogger(InMemoryJoinResults.class);

    private static final int MEMORY_COST_LIMIT = 10 * 1024 * 1024;

    private static final int UUID_SIZE = 64;

    private final MappingRepository mappings;

    private final InMemoryJoinNode node;

    private final Storage storage;

    private List list;

    interface Executor {
        Set<Object> execute(Storage storage, InMemoryJoinNode node);
    }

    public InMemoryJoinResults(Storage storage, MappingRepository mappings, InMemoryJoinNode node) {
        this.storage = storage;
        this.mappings = mappings;
        this.node = node;
    }

    @Override
    public int getSize() {
        materialize();
        return list.size();
    }

    @Override
    public int getCount() {
        materialize();
        return list.size();
    }

    @Override
    public void close() {
    }

    @Override
    public Iterator<DataRecord> iterator() {
        materialize();
        return list.iterator();
    }

    private synchronized void materialize() {
        if (list == null) {
            try {
                storage.begin();
                CloseableIterator<DataRecord> iterator = evaluateResults(storage, node);
                try {
                    list = IteratorUtils.toList(iterator);
                } finally {
                    iterator.close();
                }
                storage.commit();
            } catch (Exception e) {
                storage.rollback();
                throw new RuntimeException("Could not materialize result list.", e);
            }
        }
    }

    private static int computeTreeSize(InMemoryJoinNode node) {
        int size = node.expression == null ? 1 : 0;
        for (InMemoryJoinNode inMemoryJoinNode : node.children.keySet()) {
            size += computeTreeSize(inMemoryJoinNode);
        }
        return size;
    }

    private static int computeConditionCost(Storage storage, InMemoryJoinNode node) {
        int size = 0;
        if(node.expression != null) {
            StorageResults results = storage.fetch(node.expression); // Expects an active transaction here
            try {
                size += results.getCount();
            } finally {
                results.close();
            }
        }
        for (InMemoryJoinNode inMemoryJoinNode : node.children.keySet()) {
            size += computeConditionCost(storage, inMemoryJoinNode);
        }
        return size;
    }

    private CloseableIterator<DataRecord> evaluateResults(Storage storage, InMemoryJoinNode node) {
        // Computes memory cost for check.
        // {
        //   a = sum of all path length from node
        //   b = sum of all count based on condition in tree.
        //   UUID_SIZE = size in memory (bytes) required to store a UUID.
        // }
        // memoryCost = (((a * b) * (additionalProjectionNodes + 1) * UUID_SIZE)
        int conditionCost = computeTreeSize(node) * computeConditionCost(storage, node);
        int additionalNodes = 1;
        int memoryCost = (conditionCost * (additionalNodes + 1)) * UUID_SIZE;
        if(memoryCost > MEMORY_COST_LIMIT) {
            throw new IllegalStateException("Query execution requires too much memory (" + memoryCost + " > " + MEMORY_COST_LIMIT + ")");
        }
        // Actual evaluation
        return process(storage, node);
    }

    public CloseableIterator<DataRecord> process(Storage storage, InMemoryJoinNode node) {
        // Additional logging
        if (LOGGER.isTraceEnabled()) {
            InMemoryJoinResults.trace(node);
        }
        if (LOGGER.isDebugEnabled()) {
            InMemoryJoinResults.debug(node);
        }
        // Evaluate direct children
        Set<Object> childIds = new HashSet<Object>();
        for (InMemoryJoinNode child : node.children.keySet()) {
            Set<Object> childrenIds = InMemoryJoinResults._evaluateConditions(storage, child);
            switch (node.merge) {
                case UNION:
                case NONE:
                    childIds.addAll(childrenIds);
                    break;
                case INTERSECTION:
                    childIds.retainAll(childrenIds);
                    break;
                default:
                    throw new IllegalArgumentException("Not supported: " + child.merge);
            }
        }
        // Compute DataRecord results
        if (childIds.isEmpty()) {
            return EmptyIterator.INSTANCE;
        } else {
            FieldMetadata field = node.type.getKeyFields().iterator().next(); // TODO Support compound keys.
            Condition condition = InMemoryJoinResults.buildConditionFromValues(node.expression.getCondition(), field, childIds);
            UserQueryBuilder qb = from(node.type).where(condition);
            for (TypedExpression typedExpression : node.expression.getSelectedFields()) {
                TypedExpression mappedExpression = typedExpression.accept(new VisitorAdapter<TypedExpression>() {
                    @Override
                    public TypedExpression visit(Alias alias) {
                        return new Alias(alias.getTypedExpression().accept(this), alias.getAliasName());
                    }

                    @Override
                    public TypedExpression visit(Field field) {
                        FieldMetadata fieldMetadata = field.getFieldMetadata();
                        TypeMapping typeMapping = mappings.getMappingFromDatabase(fieldMetadata.getContainingType());
                        FieldMetadata user = typeMapping.getUser(fieldMetadata);
                        return new Field(user);
                    }
                });
                qb.select(mappedExpression);
            }
            return (CloseableIterator<DataRecord>) storage.fetch(qb.getSelect()).iterator();
        }
    }

    private static Set<Object> _evaluateConditions(Storage storage, InMemoryJoinNode node) {
        if (node.expression != null) {
            Set<Object> expressionIds = new HashSet<Object>();
            StorageResults results = storage.fetch(node.expression); // Expects an active transaction here
            try {
                for (DataRecord result : results) {
                    for (FieldMetadata field : result.getSetFields()) {
                        expressionIds.add(result.get(field));
                    }
                }
            } finally {
                results.close();
            }
            return expressionIds;
        } else {
            Executor executor = getExecutor(node);
            Set<Object> ids = new HashSet<Object>();
            switch (node.merge) {
                case UNION:
                case NONE:
                    for (InMemoryJoinNode child : node.children.keySet()) {
                        ids.addAll(executor.execute(storage, child));
                    }
                    break;
                case INTERSECTION:
                    for (InMemoryJoinNode child : node.children.keySet()) {
                        if (ids.isEmpty()) {
                            ids.addAll(executor.execute(storage, child));
                        } else {
                            ids.retainAll(executor.execute(storage, child));
                        }
                    }
                    break;
                default:
                    throw new NotImplementedException("No support for '" + node.merge + "'.");
            }
            //
            Set<Object> returnIds = new HashSet<Object>();
            if (node.childProperty != null) {
                if (ids.isEmpty()) {
                    return Collections.emptySet();
                }
                long execTime = System.currentTimeMillis();
                {
                    UserQueryBuilder qb = from(node.type)
                            .selectId(node.type)
                            .where(buildConditionFromValues(null, node.childProperty, ids));
                    node.expression = qb.getSelect();
                    StorageResults results = storage.fetch(qb.getSelect()); // Expects an active transaction here
                    try {
                        for (DataRecord result : results) {
                            for (FieldMetadata field : result.getSetFields()) {
                                returnIds.add(result.get(field));
                            }
                        }
                    } finally {
                        results.close();
                    }
                }
                node.execTime = System.currentTimeMillis() - execTime;
            }
            return returnIds;
        }
    }

    private static Condition buildConditionFromValues(Condition existingCondition, FieldMetadata field, Set<Object> values) {
        Condition condition = null;
        for (Object id : values) {
            Condition newCondition = UserQueryBuilder.eq(field, String.valueOf(id));
            if (condition == null) {
                condition = newCondition;
            } else {
                condition = UserQueryBuilder.or(condition, newCondition);
            }
        }
        if(existingCondition != null) {
            return UserQueryBuilder.and(existingCondition, condition);
        }
        return condition;
    }

    private static Executor getExecutor(InMemoryJoinNode node) {
        if (node.children.keySet().size() == 1) {
            return new Executor() {
                @Override
                public Set<Object> execute(Storage storage, InMemoryJoinNode node) {
                    return _evaluateConditions(storage, node);
                }
            };
        } else {
            return new Executor() {
                @Override
                public Set<Object> execute(Storage storage, InMemoryJoinNode node) {
                    return _evaluateConditions(storage, node);
                }
            };
        }
    }

    private static void listNodes(InMemoryJoinNode node, List<InMemoryJoinNode> nodes) {
        nodes.add(node);
        for (InMemoryJoinNode child : node.children.keySet()) {
            listNodes(child, nodes);
        }
    }

    private static void debug(InMemoryJoinNode node) {
        List<InMemoryJoinNode> orderedNodesByTime = new LinkedList<InMemoryJoinNode>();
        listNodes(node, orderedNodesByTime);
        Collections.sort(orderedNodesByTime, new Comparator<InMemoryJoinNode>() {
            @Override
            public int compare(InMemoryJoinNode o1, InMemoryJoinNode o2) {
                return (int) (o2.execTime - o1.execTime);
            }
        });
        LOGGER.debug("Execution times:");
        for (InMemoryJoinNode inMemoryJoinNode : orderedNodesByTime) {
            LOGGER.debug(inMemoryJoinNode + ": " + inMemoryJoinNode.execTime + " ms.");
        }
    }

    private static void trace(InMemoryJoinNode node) {
        LOGGER.trace("====");
        LOGGER.trace("Node " + node);
        LOGGER.trace("Merge: " + node.merge);
        LOGGER.trace("Execution time: " + node.execTime + " ms");
        if (node.expression != null) {
            LOGGER.trace("Query:");
            node.expression.accept(new UserQueryDumpConsole());
        }
        LOGGER.trace("====");
        for (InMemoryJoinNode child : node.children.keySet()) {
            trace(child);
        }
    }

}
