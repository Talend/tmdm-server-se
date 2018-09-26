/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import static org.hibernate.criterion.Restrictions.and;
import static org.hibernate.criterion.Restrictions.eq;
import static org.hibernate.criterion.Restrictions.ge;
import static org.hibernate.criterion.Restrictions.gt;
import static org.hibernate.criterion.Restrictions.ilike;
import static org.hibernate.criterion.Restrictions.isNull;
import static org.hibernate.criterion.Restrictions.le;
import static org.hibernate.criterion.Restrictions.like;
import static org.hibernate.criterion.Restrictions.lt;
import static org.hibernate.criterion.Restrictions.not;
import static org.hibernate.criterion.Restrictions.or;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.AggregateProjection;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projection;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.PropertyProjection;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SQLProjection;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.sql.JoinType;
import org.hibernate.transform.DistinctRootEntityResultTransformer;
import org.hibernate.type.IntegerType;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.CompoundFieldMetadata;
import org.talend.mdm.commmon.metadata.DefaultMetadataVisitor;
import org.talend.mdm.commmon.metadata.EnumerationFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;

import com.amalto.core.query.user.Alias;
import com.amalto.core.query.user.BigDecimalConstant;
import com.amalto.core.query.user.BinaryLogicOperator;
import com.amalto.core.query.user.BooleanConstant;
import com.amalto.core.query.user.ByteConstant;
import com.amalto.core.query.user.Compare;
import com.amalto.core.query.user.ComplexTypeExpression;
import com.amalto.core.query.user.Condition;
import com.amalto.core.query.user.ConstantCollection;
import com.amalto.core.query.user.ConstantCondition;
import com.amalto.core.query.user.Count;
import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.Distinct;
import com.amalto.core.query.user.DoubleConstant;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.Field;
import com.amalto.core.query.user.FieldFullText;
import com.amalto.core.query.user.FloatConstant;
import com.amalto.core.query.user.FullText;
import com.amalto.core.query.user.Id;
import com.amalto.core.query.user.IndexedField;
import com.amalto.core.query.user.IntegerConstant;
import com.amalto.core.query.user.IsEmpty;
import com.amalto.core.query.user.IsNull;
import com.amalto.core.query.user.Isa;
import com.amalto.core.query.user.Join;
import com.amalto.core.query.user.LongConstant;
import com.amalto.core.query.user.Max;
import com.amalto.core.query.user.Min;
import com.amalto.core.query.user.NotIsEmpty;
import com.amalto.core.query.user.NotIsNull;
import com.amalto.core.query.user.OrderBy;
import com.amalto.core.query.user.Paging;
import com.amalto.core.query.user.Predicate;
import com.amalto.core.query.user.Range;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.ShortConstant;
import com.amalto.core.query.user.StringConstant;
import com.amalto.core.query.user.TimeConstant;
import com.amalto.core.query.user.Type;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.UnaryLogicOperator;
import com.amalto.core.query.user.VisitorAdapter;
import com.amalto.core.query.user.metadata.GroupSize;
import com.amalto.core.query.user.metadata.StagingBlockKey;
import com.amalto.core.query.user.metadata.StagingError;
import com.amalto.core.query.user.metadata.StagingHasTask;
import com.amalto.core.query.user.metadata.StagingSource;
import com.amalto.core.query.user.metadata.StagingStatus;
import com.amalto.core.query.user.metadata.TaskId;
import com.amalto.core.query.user.metadata.Timestamp;
import com.amalto.core.storage.CloseableIterator;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageMetadataUtils;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.exception.UnsupportedQueryException;
import com.amalto.core.storage.record.DataRecord;

class StandardQueryHandler extends AbstractQueryHandler {

    private static final Logger LOGGER = Logger.getLogger(StandardQueryHandler.class);

    private static final StringConstant EMPTY_STRING_CONSTANT = new StringConstant(StringUtils.EMPTY);

    private final CriterionAdapter criterionVisitor;

    private final StandardQueryHandler.CriterionFieldCondition criterionFieldCondition;

    private final Map<String, String> pathToAlias = new HashMap<>();

    private final Map<String, String> aliasToPath = new HashMap<>();

    protected final MappingRepository mappings;

    protected final TableResolver resolver;

    private Criteria criteria;

    private ProjectionList projectionList;

    private ComplexTypeMetadata mainType;

    private int aliasCount = 0;

    private String currentAliasName;

    private int countAggregateIndex = 0;

    public StandardQueryHandler(Storage storage, MappingRepository mappings, TableResolver resolver,
            StorageClassLoader storageClassLoader, Session session, Select select, List<TypedExpression> selectedFields,
            Set<ResultsCallback> callbacks) {
        super(storage, storageClassLoader, session, select, selectedFields, callbacks);
        this.mappings = mappings;
        this.resolver = resolver;
        criterionFieldCondition = new CriterionFieldCondition();
        DataSource dataSource = storage.getDataSource();
        if (!(dataSource instanceof RDBMSDataSource)) {
            throw new IllegalArgumentException("Storage '" + storage.getName() + "' is not using a RDBMS datasource."); //$NON-NLS-1$ //$NON-NLS-2$
        }
        criterionVisitor = new CriterionAdapter((RDBMSDataSource) dataSource);
    }

    @SuppressWarnings("rawtypes")
    protected StorageResults createResults(List list, boolean isProjection) {
        CloseableIterator<DataRecord> iterator;
        final Iterator listIterator = list.iterator();
        if (isProjection) {
            iterator = new ProjectionIterator(mappings, new CloseableIterator<Object>() {

                @Override
                public void close() throws IOException {
                }

                @Override
                public boolean hasNext() {
                    return listIterator.hasNext();
                }

                @Override
                public Object next() {
                    return listIterator.next();
                }

                @Override
                public void remove() {
                }
            }, selectedFields, callbacks);
        } else {
            iterator = new ListIterator(mappings, storageClassLoader, listIterator, callbacks);
        }

        return new HibernateStorageResults(storage, select, iterator);
    }

    private StorageResults createResults(ScrollableResults scrollableResults, boolean isProjection) {
        CloseableIterator<DataRecord> iterator;
        if (isProjection) {
            iterator = new ProjectionIterator(mappings, scrollableResults, selectedFields, callbacks);
        } else {
            iterator = new ScrollableIterator(mappings, storageClassLoader, scrollableResults, callbacks);
        }
        return new HibernateStorageResults(storage, select, iterator);
    }

    @Override
    public StorageResults visit(Join join) {
        FieldMetadata rightField = join.getRightField().getFieldMetadata();
        FieldMetadata leftField = join.getLeftField().getFieldMetadata();
        // Choose the right join alias
        String rightAlias = rightField.getContainingType().getName();
        if (rightField.getEntityTypeName().equals(leftField.getEntityTypeName())) {
            // TMDM-7170: use a new alias for recursive relations
            rightAlias = createNewAlias();
        }
        // Choose the right join type
        JoinType joinType;
        switch (join.getJoinType()) {
        case INNER:
            joinType = JoinType.INNER_JOIN;
            break;
        case LEFT_OUTER:
            joinType = JoinType.LEFT_OUTER_JOIN;
            break;
        case FULL:
            joinType = JoinType.FULL_JOIN;
            break;
        default:
            throw new NotImplementedException("No support for join type " + join.getJoinType()); //$NON-NLS-1$
        }
        // Select a path from mainType to the selected field (properties are '.' separated).
        List<FieldMetadata> path = StorageMetadataUtils.path(mainType, leftField);
        // Empty path means no path then this is an error (all joined entities should be reachable from main type).
        if (path.isEmpty()) {
            String destinationFieldName;
            try {
                destinationFieldName = rightField.getName();
            } catch (Exception e) {
                // Ignored
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Exception occurred during exception creation", e); //$NON-NLS-1$
                }
                destinationFieldName = String.valueOf(rightField);
            }
            throw new IllegalArgumentException("Join to '" + destinationFieldName + "' (in type '" //$NON-NLS-1$ //$NON-NLS-2$
                    + rightField.getContainingType().getName() + "') is invalid since there is no path from '" //$NON-NLS-1$
                    + mainType.getName() + "' to this field."); //$NON-NLS-1$
        }
        // Generate all necessary joins to go from main type to join right table.
        generateJoinPath(Collections.singleton(rightAlias), joinType, path);
        return null;
    }
    
    private String getReferenceFieldJoinPath(ReferenceFieldMetadata previousRefFieldMetadata, FieldMetadata nextField, String aliasPathKey) {
        if(previousRefFieldMetadata != null){
            if(nextField instanceof ReferenceFieldMetadata){
                ReferenceFieldMetadata referenceFieldMetadata = (ReferenceFieldMetadata)nextField;
                if(previousRefFieldMetadata.getReferencedType().getName().equals(referenceFieldMetadata.getContainingType().getName())){
                    return aliasPathKey + "/" + nextField.getName(); //$NON-NLS-1$
                } else {
                    if(previousRefFieldMetadata.getReferencedType().getSubTypes().contains(referenceFieldMetadata.getContainingType())){
                        return aliasPathKey + "/" + nextField.getName(); //$NON-NLS-1$
                    }
                }
            }
        }
        return nextField.getName();
    }

    private void generateJoinPath(Set<String> rightTableAliases, JoinType joinType, List<FieldMetadata> path) {
        Iterator<FieldMetadata> pathIterator = path.iterator();
        String previousAlias = mainType.getName();
        ReferenceFieldMetadata previousRefFieldMetadata = null;
        String aliasPathKey = ""; //$NON-NLS-1$
        
        while (pathIterator.hasNext()) {
            FieldMetadata nextField = pathIterator.next();
            String newAlias = createNewAlias();
            aliasPathKey = getReferenceFieldJoinPath(previousRefFieldMetadata, nextField, aliasPathKey);
            String pathToAliasKey = previousAlias + "/" + aliasPathKey; //$NON-NLS-1$
            // TODO One interesting improvement here: can add conditions on rightTable when defining join.
            if (pathIterator.hasNext()) {
                if (!pathToAlias.containsKey(pathToAliasKey)) {
                    criteria.createAlias(previousAlias + '.' + nextField.getName(), newAlias, joinType);
                    pathToAlias.put(pathToAliasKey, newAlias);
                    previousAlias = newAlias;
                } else {
                    previousAlias = pathToAlias.get(pathToAliasKey);
                }
            } else {
                if (!pathToAlias.containsKey(pathToAliasKey)) {
                    for (String rightTableAlias : rightTableAliases) {
                        criteria.createAlias(previousAlias + '.' + nextField.getName(), rightTableAlias, joinType);
                        pathToAlias.put(pathToAliasKey, rightTableAlias);
                        aliasToPath.put(rightTableAlias, aliasPathKey);
                    }
                    previousAlias = rightTableAliases.iterator().next();
                } else {
                    previousAlias = pathToAlias.get(pathToAliasKey);
                }
            }
            if(nextField instanceof ReferenceFieldMetadata){
                previousRefFieldMetadata = (ReferenceFieldMetadata)nextField;
            }
        }
    }

    private String createNewAlias() {
        return "a" + aliasCount++; //$NON-NLS-1$
    }

    @Override
    public StorageResults visit(Alias alias) {
        currentAliasName = alias.getAliasName();
        alias.getTypedExpression().accept(this);
        currentAliasName = null;
        return null;
    }

    @Override
    public StorageResults visit(Distinct distinct) {
        // Standard visit for the expression where distinct should be added
        distinct.getExpression().accept(this);
        // Wraps the last projection into a 'distinct' statement
        // Note: Hibernate does not provide projection editing functions... have to work around that with a new
        // projection list.
        ProjectionList newProjectionList = Projections.projectionList();
        int i = 0;
        for (; i < projectionList.getLength() - 1; i++) {
            newProjectionList.add(projectionList.getProjection(i));
        }
        newProjectionList.add(Projections.distinct(projectionList.getProjection(i)));
        projectionList = newProjectionList;
        return null;
    }

    @Override
    public StorageResults visit(Type type) {
        if (currentAliasName != null) {
            projectionList.add(new ClassNameProjection(currentAliasName), currentAliasName);
            ProjectionList previousList = projectionList;
            try {
                projectionList = ReadOnlyProjectionList.makeReadOnly(projectionList);
                type.getField().accept(this);
            } finally {
                projectionList = previousList;
            }
        } else {
            throw new IllegalStateException("Expected an alias for a type expression."); //$NON-NLS-1$
        }
        return null;
    }

    @Override
    public StorageResults visit(StringConstant constant) {
        Projection p = new ConstantStringProjection(currentAliasName, constant.getValue());
        if (currentAliasName != null) {
            projectionList.add(p, currentAliasName);
        } else {
            throw new IllegalStateException("Expected an alias for a constant expression."); //$NON-NLS-1$
        }
        return null;
    }

    @Override
    public StorageResults visit(Timestamp timestamp) {
        String timeStamp = mappings.getMappingFromDatabase(mainType).getDatabaseTimestamp();
        if (timeStamp != null) {
            projectionList.add(Projections.property(timeStamp));
        } else {
            EMPTY_STRING_CONSTANT.accept(this);
        }
        return null;
    }

    @Override
    public StorageResults visit(TaskId taskId) {
        String taskIdField = mappings.getMappingFromDatabase(mainType).getDatabaseTaskId();
        if (taskIdField != null) {
            projectionList.add(Projections.property(taskIdField));
        } else {
            EMPTY_STRING_CONSTANT.accept(this);
        }
        return null;
    }

    @Override
    public StorageResults visit(StagingStatus stagingStatus) {
        projectionList.add(Projections.property(Storage.METADATA_STAGING_STATUS));
        return null;
    }

    @Override
    public StorageResults visit(StagingError stagingError) {
        projectionList.add(Projections.property(Storage.METADATA_STAGING_ERROR));
        return null;
    }

    @Override
    public StorageResults visit(StagingSource stagingSource) {
        projectionList.add(Projections.property(Storage.METADATA_STAGING_SOURCE));
        return null;
    }

    @Override
    public StorageResults visit(StagingBlockKey stagingBlockKey) {
        projectionList.add(Projections.property(Storage.METADATA_STAGING_BLOCK_KEY));
        return null;
    }

    @Override
    public StorageResults visit(StagingHasTask stagingHasTask) {
        projectionList.add(Projections.property(Storage.METADATA_STAGING_HAS_TASK));
        return null;
    }

    @Override
    public StorageResults visit(GroupSize groupSize) {
        Projection groupSizeProjection = Projections.sqlGroupProjection(
                "count(this_." + Storage.METADATA_TASK_ID + ") as talend_group_size", //$NON-NLS-1$ //$NON-NLS-2$ 
                "this_." + Storage.METADATA_TASK_ID, new String[] { "talend_group_size" }, //$NON-NLS-1$ //$NON-NLS-2$ 
                new org.hibernate.type.Type[] { new IntegerType() });
        projectionList.add(groupSizeProjection);
        return null;
    }

    @Override
    public StorageResults visit(final Field field) {
        final FieldMetadata userFieldMetadata = field.getFieldMetadata();
        ComplexTypeMetadata containingType = getContainingType(userFieldMetadata);
        final Set<String> aliases = getAliases(containingType, field);
        userFieldMetadata.accept(new DefaultMetadataVisitor<Void>() {

            @Override
            public Void visit(ReferenceFieldMetadata referenceField) {
                // Automatically selects referenced ID in case of FK.
                if (userFieldMetadata instanceof ReferenceFieldMetadata) {
                    referenceField.getReferencedField().accept(this);
                }
                return null;
            }

            @Override
            public Void visit(SimpleTypeFieldMetadata simpleField) {
                if (!simpleField.isMany()) {
                    for (String alias : aliases) {
                        projectionList.add(Projections.property(alias + '.' + simpleField.getName()));
                    }
                } else {
                    projectionList.add(new ManyFieldProjection(aliases, simpleField, resolver, (RDBMSDataSource) storage
                            .getDataSource()));
                }
                return null;
            }

            @Override
            public Void visit(EnumerationFieldMetadata enumField) {
                if (!enumField.isMany()) {
                    for (String alias : aliases) {
                        projectionList.add(Projections.property(alias + '.' + enumField.getName()));
                    }
                } else {
                    projectionList.add(new ManyFieldProjection(aliases, enumField, resolver, (RDBMSDataSource) storage
                            .getDataSource()));
                }
                return null;
            }
        });
        return null;
    }

    private ComplexTypeMetadata getContainingType(FieldMetadata userFieldMetadata) {
        ComplexTypeMetadata containingType = userFieldMetadata.getContainingType();
        if (!containingType.isInstantiable()) {
            if(containingType.getContainer() != null) {
                return getContainingType(containingType.getContainer());
            }
            else {
                return mainType;
            }
        }
        return containingType;
    }

    /**
     * Test if a path has elements part of a inheritance tree.
     * 
     * @param path A list of {@link org.talend.mdm.commmon.metadata.FieldMetadata fields} that represents a path from an
     * entity type down to a selected field (for projection or condition).
     * @return <code>true</code> if at least one element in the <code>path</code> is contained in a type part of an
     * inheritance tree, <code>false</code> otherwise. If path is empty, returns <code>true</code> as method cannot
     * decide.
     */
    private static boolean pathContainsInheritance(List<FieldMetadata> path) {
        if (path.isEmpty()) {
            // Path is empty: it may contain inheritance elements, can't decide.
            return true;
        }
        for (FieldMetadata fieldMetadata : path) {
            ComplexTypeMetadata containingType = fieldMetadata.getContainingType();
            if (!containingType.getSubTypes().isEmpty() || !containingType.getSuperTypes().isEmpty()) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Test if a path has elements part of parent type
     * 
     * @param path A list of {@link org.talend.mdm.commmon.metadata.FieldMetadata fields} that represents a path from an
     * entity type down to a selected field (for projection or condition).
     * @param type A type contains this field    
     * @return <code>true</code> if field's path has contained its parent
     * {@link org.talend.mdm.commmon.metadata.ComplexTypeMetadata type}
     */
    private static boolean pathContainsParentType(List<FieldMetadata> path, ComplexTypeMetadata type) {
        for (FieldMetadata fieldMetadata : path) {
            if (type.equals(fieldMetadata.getContainingType())) {
                return true;
            }
        }
        return false;
    }

    /**
     * <p>
     * Generate an alias to the <code>field</code> starting from <code>type</code>. This code ensures all paths to
     * <code>field</code> are covered (this field might be present several times inside the MDM entity scope).
     * </p>
     * 
     * @param type A type in the query.
     * @param field A field to include in current Hibernate criteria.
     * @return A set of aliases that represents the <code>field</code>.
     */
    private Set<String> getAliases(ComplexTypeMetadata type, Field field) {
        FieldMetadata fieldMetadata = field.getFieldMetadata();
        boolean fieldContainerInstantiable = fieldMetadata.getContainingType().getEntity().isInstantiable();
        String previousAlias;
        if (fieldContainerInstantiable) {
            previousAlias = fieldMetadata.getEntityTypeName();
        } else {
            previousAlias = type.getName();
        }
        Set<List<FieldMetadata>> paths;
        if (pathContainsInheritance(field.getPath()) || (!fieldContainerInstantiable && !pathContainsParentType(field.getPath(), type))) {
            paths = StorageMetadataUtils.paths(type, fieldMetadata);
        } else {
            paths = Collections.singleton(field.getPath());
        }
        Set<String> aliases = new HashSet<>(paths.size());
        for (List<FieldMetadata> path : paths) {
            JoinType joinType = JoinType.INNER_JOIN;
            Iterator<FieldMetadata> iterator = path.iterator();
            ReferenceFieldMetadata previousRefFieldMetadata = null;
            String aliasPathKey = ""; //$NON-NLS-1$
            while (iterator.hasNext()) {
                FieldMetadata next = iterator.next();
                if (next instanceof ReferenceFieldMetadata) {
                    aliasPathKey = getReferenceFieldJoinPath(previousRefFieldMetadata, next, aliasPathKey);
                    String alias = getAliasByPath(previousAlias, aliasPathKey);
                    if (alias == null) {
                        alias = createNewAlias();
                        // Remembers aliases created for the next field in path (to prevent same alias name creation for
                        // field - in case of join, for example -)
                        pathToAlias.put(previousAlias + "/" + aliasPathKey, alias); //$NON-NLS-1$
                        // TMDM-4866: Do a left join in case FK is not mandatory (only if there's one path).
                        // TMDM-7636: As soon as a left join is selected all remaining join should remain left outer.
                        if (next.isMandatory() && paths.size() == 1 && joinType != JoinType.LEFT_OUTER_JOIN) {
                            if(storage != null && storage instanceof HibernateStorage && StorageType.STAGING == storage.getType()){
                                joinType = JoinType.LEFT_OUTER_JOIN;
                            } else {
                                joinType = JoinType.INNER_JOIN;
                            }
                        } else {
                            joinType = JoinType.LEFT_OUTER_JOIN;
                        }
                        criteria.createAlias(previousAlias + '.' + next.getName(), alias, joinType);
                    }
                    previousAlias = alias;
                    previousRefFieldMetadata = (ReferenceFieldMetadata)next;
                }
            }
            aliases.add(previousAlias);
            previousAlias = type.getName();

        }
        return aliases;
    }

    private String getAliasByPath(String previousAlias, String aliasPathKey) {
        String alias = pathToAlias.get(previousAlias + "/" + aliasPathKey); //$NON-NLS-1$
        if (alias == null) {
            String path = aliasToPath.get(previousAlias);
            if (path != null) {
                path = path + "/" + aliasPathKey; //$NON-NLS-1$
            }
            return pathToAlias.get(previousAlias + "/" + path); //$NON-NLS-1$
        } else {
            return alias;
        }
    }

    @Override
    public StorageResults visit(Count count) {
        projectionList.add(Projections.rowCount());
        return null;
    }

    @Override
    public StorageResults visit(Select select) {
        createCriteria(select);
        // Paging
        Paging paging = select.getPaging();
        paging.accept(this);
        return createResults(select);
    }

    @SuppressWarnings("rawtypes")
    protected StorageResults createResults(Select select) {
        Paging paging = select.getPaging();
        int pageSize = paging.getLimit();
        boolean hasPaging = pageSize < Integer.MAX_VALUE;
        // Results
        if (!hasPaging) {
            if (storage instanceof HibernateStorage) {
                RDBMSDataSource dataSource = (RDBMSDataSource) storage.getDataSource();
                if (dataSource.getDialectName() == RDBMSDataSource.DataSourceDialect.DB2) {
                    // TMDM-7701: DB2 doesn't like use of SCROLL_INSENSITIVE for projections including a CLOB.
                    if (select.isProjection()) {
                        return createResults(criteria.scroll(ScrollMode.FORWARD_ONLY), true);
                    } else {
                        return createResults(criteria.scroll(ScrollMode.SCROLL_INSENSITIVE), false);
                    }
                } else {
                    return createResults(criteria.scroll(ScrollMode.SCROLL_INSENSITIVE), select.isProjection());
                }
            }
            return createResults(criteria.scroll(ScrollMode.SCROLL_INSENSITIVE), select.isProjection());
        } else {
            List list = criteria.list();
            return createResults(list, select.isProjection());
        }
    }

    protected Criteria createCriteria(Select select) {
        List<ComplexTypeMetadata> selectedTypes = select.getTypes();
        if (selectedTypes.isEmpty()) {
            throw new IllegalArgumentException("Select clause is expected to select at least one entity type."); //$NON-NLS-1$
        }
        mainType = selectedTypes.get(0);
        String mainClassName = ClassCreator.getClassName(mainType.getName());
        criteria = session.createCriteria(mainClassName, mainType.getName());
        if (!select.forUpdate()) {
            criteria.setReadOnly(true); // We are reading data, turns on ready only mode.
        }
        // Handle JOIN (if any)
        List<Join> joins = select.getJoins();
        for (Join join : joins) {
            join.accept(this);
        }
        // If select is not a projection, selecting root type is enough, otherwise add projection for selected fields.
        if (select.isProjection()) {
            boolean toDistinct = true;
            projectionList = Projections.projectionList();
            {
                List<TypedExpression> queryFields = select.getSelectedFields();
                boolean isCountQuery = false;
                boolean hasGroupSize = false;
                for (Expression selectedField : queryFields) {
                    if (selectedField instanceof GroupSize) {
                        hasGroupSize = true;
                    }
                    selectedField.accept(this);
                    if (selectedField instanceof Alias) {
                        Alias alias = (Alias) selectedField;
                        if (alias.getTypedExpression() instanceof Count) {
                            isCountQuery = true;
                        }
                        if (alias.getTypedExpression() instanceof Distinct) {
                            toDistinct = false;
                        }
                    }
                }
                // TMDM-9502/TMDM-10395, If selected fields including "GroupSize", besides GROUP BY "x_talend_task_id"
                // NOT ORACLE DB, should GROUP BY "All Key Fields"
                // ORACLE DB, need to GROUP BY "All Selected Fields"
                if (hasGroupSize) {
                    projectionList = optimizeProjectionList(mainType, projectionList);
                }
                if (isCountQuery && queryFields.size() > 1) {
                    Projection projection = projectionList.getProjection(projectionList.getLength() - 1);
                    projectionList = Projections.projectionList();
                    projectionList.add(projection);
                    TypedExpression countTypedExpression = selectedFields.get(queryFields.size() - 1);
                    selectedFields.clear();
                    selectedFields.add(countTypedExpression);
                }
            }
            // for SELECT DISTINCT, ORDER BY expressions must appear in select list. Or it will throw exception in H2, postgres...
            for (OrderBy current : select.getOrderBy()) {
                if ((current.getExpression() instanceof Field && !select.getSelectedFields().contains(current.getExpression()))
                        || current.getExpression() instanceof Type || current.getExpression() instanceof Alias) {
                    toDistinct = false;
                    break;
                }
            }
            if (select.getOrderBy().size() > 0 && toDistinct) {
                criteria.setProjection(Projections.distinct(projectionList));
            } else {
                criteria.setProjection(projectionList);
            }
        } else {
            // TMDM-5388: Hibernate sometimes returns duplicate results (like for User stored in System storage), this
            // line avoids this situation.
            criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);
        }
        // Make projection read only in case code tries to modify it later (see code that handles condition).
        projectionList = ReadOnlyProjectionList.makeReadOnly(projectionList);
        // Handle condition (if there's any condition to handle).
        Condition condition = select.getCondition();
        if (condition != null) {
            condition.accept(this);
        }
        // Order by
        for (OrderBy current : select.getOrderBy()) {
            if (current.getExpression() instanceof Count) {
                int limit = select.getPaging().getLimit();
                if (limit > 0) {
                    RDBMSDataSource dataSource = (RDBMSDataSource) storage.getDataSource();
                    if (dataSource.getDialectName() == RDBMSDataSource.DataSourceDialect.DB2
                            || dataSource.getDialectName() == RDBMSDataSource.DataSourceDialect.SQL_SERVER) {
                        LOGGER.error("The query is not supported by DB2 and SQLSERVER Database."); //$NON-NLS-1$
                        throw new UnsupportedQueryException("The query is not supported by DB2 and SQLSERVER Database."); //$NON-NLS-1$
                    }
                }
            }
            current.accept(this);
        }
        return criteria;
    }

    private ProjectionList optimizeProjectionList(ComplexTypeMetadata mainType, ProjectionList oldProjectionList) {
        ProjectionList newProjectionList = null;
        RDBMSDataSource dataSource = (RDBMSDataSource) storage.getDataSource();
        if (dataSource.getDialectName() == RDBMSDataSource.DataSourceDialect.H2
                || dataSource.getDialectName() == RDBMSDataSource.DataSourceDialect.MYSQL) {
            newProjectionList = oldProjectionList;
            for (FieldMetadata keyField : mainType.getKeyFields()) {
                newProjectionList.add(Projections.groupProperty(keyField.getName()));
            }
        } else { // ORACLE, POSTGRES, DB2 and SQLSERVER need to GROUP BY all selected fields
            newProjectionList = Projections.projectionList();
            Set<String> groupBys = new LinkedHashSet<String>();// GROUP BY fields
            ProjectionList extraProjectionList = Projections.projectionList();
            for (int i = 0; i < oldProjectionList.getLength(); i++) {
                String propertyName = null;
                Projection oldProjection = oldProjectionList.getProjection(i);
                if (oldProjection instanceof SQLProjection) { // Group Size
                    newProjectionList.add(oldProjection);
                } else if (oldProjection instanceof PropertyProjection) {// normal fields
                    propertyName = ((PropertyProjection) oldProjection).getPropertyName();
                    newProjectionList.add(Projections.groupProperty(propertyName));
                } else if (oldProjection instanceof AggregateProjection) {// Max, Min
                    propertyName = ((AggregateProjection) oldProjection).getPropertyName();
                    newProjectionList.add(oldProjection);
                    extraProjectionList.add(Projections.groupProperty(propertyName));
                }
                if (propertyName != null) {
                    groupBys.add(propertyName);
                }
            }
            // Add key fields to GROUP BY
            for (FieldMetadata keyField : mainType.getKeyFields()) {
                String keyFieldName = mainType.getName() + '.' + keyField.getName();
                if (!groupBys.contains(keyFieldName)) {
                    extraProjectionList.add(Projections.groupProperty(keyFieldName));
                }
            }
            for (int i = 0; i < extraProjectionList.getLength(); i++) {
                newProjectionList.add(extraProjectionList.getProjection(i));
            }
        }
        return newProjectionList;
    }

    @Override
    public StorageResults visit(FullText fullText) {
        // Ignore full text queries (if any).
        return null;
    }

    @Override
    public StorageResults visit(FieldFullText fullText) {
        // Ignore full text queries (if any).
        return null;
    }

    @Override
    public StorageResults visit(Paging paging) {
        if (paging.getLimit() < Integer.MAX_VALUE) {
            criteria.setFirstResult(paging.getStart());
            criteria.setFetchSize(JDBC_FETCH_SIZE);
            criteria.setMaxResults(paging.getLimit());
        }
        return null;
    }

    @Override
    public StorageResults visit(OrderBy orderBy) {
        TypedExpression orderByExpression = orderBy.getExpression();
        CriterionFieldCondition fieldCondition = new CriterionFieldCondition();
        FieldCondition condition = orderByExpression.accept(fieldCondition);
        if (orderByExpression instanceof Field) {
            Field field = (Field) orderByExpression;
            FieldMetadata userFieldMetadata = field.getFieldMetadata();
            ComplexTypeMetadata containingType = getContainingType(userFieldMetadata);
            Set<String> aliases = getAliases(containingType, field);
            condition.criterionFieldNames = new ArrayList<>(aliases.size());
            for (String alias : aliases) {
                addCondition(condition, alias, userFieldMetadata);
            }
        }
        if (orderByExpression instanceof Count) {
            Count count = (Count) orderByExpression;
            String propertyName = count.getExpression().accept(fieldCondition).criterionFieldNames.get(0);
            ProjectionList list = projectionList;
            if (projectionList instanceof ReadOnlyProjectionList) {
                list = ((ReadOnlyProjectionList) projectionList).inner();
            }
            list.add(Projections.groupProperty(propertyName));
            String alias = "x_talend_countField" + countAggregateIndex++; //$NON-NLS-1$
            list.add(Projections.count(propertyName).as(alias));
            switch (orderBy.getDirection()) {
            case ASC:
                criteria.addOrder(Order.asc(alias));
                break;
            case DESC:
                criteria.addOrder(Order.desc(alias));
                break;
            }
        }
        if (condition != null) {
            for (String fieldName : condition.criterionFieldNames) {
                OrderBy.Direction direction = orderBy.getDirection();
                switch (direction) {
                case ASC:
                    criteria.addOrder(Order.asc(fieldName));
                    break;
                case DESC:
                    criteria.addOrder(Order.desc(fieldName));
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public StorageResults visit(BinaryLogicOperator condition) {
        criteria.add(condition.accept(criterionVisitor));
        return null;
    }

    @Override
    public StorageResults visit(UnaryLogicOperator condition) {
        criteria.add(condition.accept(criterionVisitor));
        return null;
    }

    @Override
    public StorageResults visit(Isa isa) {
        criteria.add(isa.accept(criterionVisitor));
        return null;
    }

    @Override
    public StorageResults visit(Compare condition) {
        Criterion criterion = condition.accept(criterionVisitor);
        criteria.add(criterion);
        return null;
    }

    @Override
    public StorageResults visit(Max max) {
        FieldCondition fieldCondition = max.getExpression().accept(criterionFieldCondition);
        for (String criterionFieldName : fieldCondition.criterionFieldNames) {
            projectionList.add(Projections.max(criterionFieldName));
        }
        return null;
    }

    @Override
    public StorageResults visit(Min min) {
        FieldCondition fieldCondition = min.getExpression().accept(criterionFieldCondition);
        for (String criterionFieldName : fieldCondition.criterionFieldNames) {
            projectionList.add(Projections.min(criterionFieldName));
        }
        return null;
    }

    @Override
    public StorageResults visit(IsNull isNull) {
        Criterion criterion = isNull.accept(criterionVisitor);
        criteria.add(criterion);
        return null;
    }

    @Override
    public StorageResults visit(IsEmpty isEmpty) {
        Criterion criterion = isEmpty.accept(criterionVisitor);
        criteria.add(criterion);
        return null;
    }

    @Override
    public StorageResults visit(NotIsEmpty notIsEmpty) {
        Criterion criterion = notIsEmpty.accept(criterionVisitor);
        criteria.add(criterion);
        return null;
    }

    @Override
    public StorageResults visit(NotIsNull notIsNull) {
        Criterion criterion = notIsNull.accept(criterionVisitor);
        criteria.add(criterion);
        return null;
    }

    @Override
    public StorageResults visit(Range range) {
        Criterion criterion = range.accept(criterionVisitor);
        if (criterion != null) {
            criteria.add(criterion);
        }
        return null;
    }

    private class CriterionAdapter extends VisitorAdapter<Criterion> {

        private final CriterionFieldCondition visitor = new CriterionFieldCondition();

        private final RDBMSDataSource datasource;

        private CriterionAdapter(RDBMSDataSource datasource) {
            this.datasource = datasource;
        }

        @Override
        public Criterion visit(Condition condition) {
            if (condition instanceof ConstantCondition) {
                ConstantCondition constantCondition = ((ConstantCondition) condition);
                if (constantCondition.value()) {
                    return TRUE_CRITERION;
                } else {
                    return FALSE_CRITERION;
                }
            }
            return super.visit(condition);
        }

        @Override
        public Criterion visit(UnaryLogicOperator condition) {
            Predicate predicate = condition.getPredicate();
            Criterion conditionCriterion = condition.getCondition().accept(this);

            if (predicate == Predicate.NOT) {
                return not(conditionCriterion);
            } else {
                throw new NotImplementedException("No support for predicate '" + predicate + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        @Override
        public Criterion visit(Isa isa) {
            FieldCondition fieldCondition = isa.getExpression().accept(visitor);
            if (fieldCondition == null) {
                return TRUE_CRITERION;
            }
            if (fieldCondition.criterionFieldNames.isEmpty()) {
                // Case #1: doing a simple instance type check on main selected type.
                return Restrictions.eq("class", storageClassLoader.getClassFromType(isa.getType())); //$NON-NLS-1$
            } else {
                // Case #2: doing a instance type check on a field reachable from main selected type.
                // First, need to join with all tables to get to the table that stores the type
                List<FieldMetadata> path = StorageMetadataUtils.path(mainType, fieldCondition.fieldMetadata);
                if (path.isEmpty()) {
                    throw new IllegalStateException("Expected field '" + fieldCondition.fieldMetadata.getName() //$NON-NLS-1$
                            + "' to be reachable from '" + mainType.getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                }
                // Generate the joins
                Set<String> aliases = getAliases(mainType, fieldCondition.field);
                generateJoinPath(aliases, JoinType.INNER_JOIN, path);
                // Find the criteria that does the join to the table to check (only way to get the SQL alias for table).
                Criteria foundSubCriteria = findCriteria(criteria, aliases);
                String name = storageClassLoader.getClassFromType(isa.getType()).getName();
                return new FieldTypeCriterion(foundSubCriteria, name);
            }
        }

        @Override
        public Criterion visit(IsNull isNull) {
            TypedExpression field = isNull.getField();
            FieldCondition fieldCondition = field.accept(visitor);
            if (fieldCondition == null) {
                return TRUE_CRITERION;
            }
            if (fieldCondition.isMany) {
                throw new UnsupportedOperationException("Does not support 'is null' operation on collections."); //$NON-NLS-1$
            }
            if (fieldCondition.criterionFieldNames.isEmpty()) {
                throw new IllegalStateException("No field name for 'is null' condition on " + field); //$NON-NLS-1$
            } else {
                // Criterion affect multiple fields
                Criterion current = null;
                for (String criterionFieldName : fieldCondition.criterionFieldNames) {
                    Criterion criterion;
                    if (field instanceof Field) {
                        // TMDM-7700: Fix incorrect alias for isNull condition on FK (pick the FK's containing type
                        // iso. the referenced type).
                        FieldMetadata fieldMetadata = ((Field) field).getFieldMetadata();
                        if (fieldMetadata.getContainingType().isInstantiable()) {
                            String typeName = fieldMetadata.getEntityTypeName();
                            criterion = Restrictions.isNull(typeName + '.' + fieldMetadata.getName());
                        } else {
                            criterion = Restrictions.isNull(criterionFieldName);
                        }
                    } else {
                        criterion = Restrictions.isNull(criterionFieldName);
                    }
                    if (current == null) {
                        current = criterion;
                    } else {
                        current = Restrictions.and(current, criterion);
                    }
                }
                return current;
            }
        }

        @Override
        public Criterion visit(IsEmpty isEmpty) {
            FieldCondition fieldCondition = isEmpty.getField().accept(visitor);
            if (fieldCondition == null) {
                return TRUE_CRITERION;
            }
            if (fieldCondition.isMany) {
                if (fieldCondition.criterionFieldNames.isEmpty()) {
                    throw new IllegalStateException("No field name for 'is empty' condition on " + isEmpty.getField()); //$NON-NLS-1$
                } else {
                    // Criterion affect multiple fields
                    Criterion current = null;
                    for (String criterionFieldName : fieldCondition.criterionFieldNames) {
                        if (current == null) {
                            current = Restrictions.isEmpty(criterionFieldName);
                        } else {
                            current = Restrictions.and(current, Restrictions.isEmpty(criterionFieldName));
                        }
                    }
                    return current;
                }
            } else {
                if (fieldCondition.criterionFieldNames.isEmpty()) {
                    throw new IllegalStateException("No field name for 'is empty' condition on " + isEmpty.getField()); //$NON-NLS-1$
                } else {
                    // Criterion affect multiple fields
                    Criterion current = null;
                    for (String criterionFieldName : fieldCondition.criterionFieldNames) {
                        if (current == null) {
                            current = Restrictions.eq(criterionFieldName, StringUtils.EMPTY);
                        } else {
                            current = Restrictions.and(current, Restrictions.eq(criterionFieldName, StringUtils.EMPTY));
                        }
                    }
                    return current;
                }
            }
        }

        @Override
        public Criterion visit(NotIsEmpty notIsEmpty) {
            FieldCondition fieldCondition = notIsEmpty.getField().accept(visitor);
            if (fieldCondition == null) {
                return TRUE_CRITERION;
            }
            if (fieldCondition.isMany) {
                if (fieldCondition.criterionFieldNames.isEmpty()) {
                    throw new IllegalStateException("No field name for 'not is empty' condition on " + notIsEmpty.getField()); //$NON-NLS-1$
                } else {
                    // Criterion affect multiple fields
                    Criterion current = null;
                    for (String criterionFieldName : fieldCondition.criterionFieldNames) {
                        if (current == null) {
                            current = Restrictions.isNotEmpty(criterionFieldName);
                        } else {
                            current = Restrictions.and(current, Restrictions.isNotEmpty(criterionFieldName));
                        }
                    }
                    return current;
                }
            } else {
                if (fieldCondition.criterionFieldNames.isEmpty()) {
                    throw new IllegalStateException("No field name for 'not is empty' condition on " + notIsEmpty.getField()); //$NON-NLS-1$
                } else {
                    // Criterion affect multiple fields
                    Criterion current = null;
                    for (String criterionFieldName : fieldCondition.criterionFieldNames) {
                        if (current == null) {
                            current = Restrictions.not(Restrictions.eq(criterionFieldName, StringUtils.EMPTY));
                        } else {
                            current = Restrictions.and(current,
                                    Restrictions.not(Restrictions.eq(criterionFieldName, StringUtils.EMPTY)));
                        }
                    }
                    return current;
                }
            }
        }

        @Override
        public Criterion visit(GroupSize groupSize) {
            return Restrictions.sqlRestriction("count()"); //$NON-NLS-1$
        }

        @Override
        public Criterion visit(NotIsNull notIsNull) {
            FieldCondition fieldCondition = notIsNull.getField().accept(visitor);
            if (fieldCondition == null) {
                return TRUE_CRITERION;
            }
            if (fieldCondition.isMany) {
                throw new UnsupportedOperationException("Does not support 'not is null' operation on collections."); //$NON-NLS-1$
            }
            if (fieldCondition.criterionFieldNames.isEmpty()) {
                throw new IllegalStateException("No field name for 'not is null' condition on " + notIsNull.getField()); //$NON-NLS-1$
            } else {
                // Criterion affect multiple fields
                Criterion current = null;
                for (String criterionFieldName : fieldCondition.criterionFieldNames) {
                    if (current == null) {
                        current = Restrictions.isNotNull(criterionFieldName);
                    } else {
                        current = Restrictions.and(current, Restrictions.isNotNull(criterionFieldName));
                    }
                }
                return current;
            }
        }

        @Override
        public Criterion visit(BinaryLogicOperator condition) {
            Predicate predicate = condition.getPredicate();
            Criterion left = condition.getLeft().accept(this);
            Criterion right = condition.getRight().accept(this);
            if (predicate == Predicate.AND) {
                return and(left, right);
            } else if (predicate == Predicate.OR) {
                return or(left, right);
            } else {
                throw new NotImplementedException("No support for predicate '" + predicate + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        @Override
        public Criterion visit(Range range) {
            FieldCondition condition = range.getExpression().accept(new CriterionFieldCondition());
            TypedExpression expression = range.getExpression();
            Object start;
            Object end;
            String startValue = String.valueOf(range.getStart().accept(VALUE_ADAPTER));
            String endValue = String.valueOf(range.getEnd().accept(VALUE_ADAPTER));
            if (expression instanceof Field) {
                Field field = (Field) expression;
                FieldMetadata fieldMetadata = field.getFieldMetadata();
                start = StorageMetadataUtils.convert(startValue, fieldMetadata);
                end = StorageMetadataUtils.convert(endValue, fieldMetadata);
            } else {
                start = StorageMetadataUtils.convert(startValue, expression.getTypeName());
                end = StorageMetadataUtils.convert(endValue, expression.getTypeName());
            }
            if (condition != null) {
                if (condition.criterionFieldNames.isEmpty()) {
                    throw new IllegalStateException("No field name for 'range' condition on " + range.getExpression()); //$NON-NLS-1$
                } else {
                    // Criterion affect multiple fields
                    Criterion current = null;
                    for (String criterionFieldName : condition.criterionFieldNames) {
                        if (current == null) {
                            current = Restrictions.between(criterionFieldName, start, end);
                        } else {
                            current = Restrictions.and(current, Restrictions.between(criterionFieldName, start, end));
                        }
                    }
                    return current;
                }
            } else {
                return null;
            }
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Criterion visit(Compare condition) {
            FieldCondition leftFieldCondition = condition.getLeft().accept(criterionFieldCondition);
            FieldCondition rightFieldCondition = condition.getRight().accept(criterionFieldCondition);
            Predicate predicate = condition.getPredicate();
            if (!leftFieldCondition.isProperty) {
                if (leftFieldCondition.isComputedProperty) {
                    // TODO Assuming that main table alias is "this_" is quite a bold statement.
                    String mainTableAlias = "this_"; //$NON-NLS-1$
                    String mainTableName = resolver.get(mainType);
                    Object value = condition.getRight().accept(VALUE_ADAPTER);
                    String comparator;
                    if (predicate == Predicate.EQUALS) {
                        comparator = "="; //$NON-NLS-1$
                    } else if (predicate == Predicate.GREATER_THAN) {
                        comparator = ">"; //$NON-NLS-1$
                    } else if (predicate == Predicate.GREATER_THAN_OR_EQUALS) {
                        comparator = ">="; //$NON-NLS-1$
                    } else if (predicate == Predicate.LOWER_THAN) {
                        comparator = "<"; //$NON-NLS-1$
                    } else if (predicate == Predicate.LOWER_THAN_OR_EQUALS) {
                        comparator = "<="; //$NON-NLS-1$
                    } else {
                        throw new IllegalArgumentException("Predicate '" + predicate + "' is not supported on group_size value."); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    String sqlConditionBuilder = "("; //$NON-NLS-1$
                    sqlConditionBuilder += "select count(1) from"; //$NON-NLS-1$
                    sqlConditionBuilder += ' ' + mainTableName + ' ';
                    sqlConditionBuilder += "where " + Storage.METADATA_TASK_ID + " = " + mainTableAlias + "." + Storage.METADATA_TASK_ID; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$  
                    sqlConditionBuilder += ')';
                    sqlConditionBuilder += ' ' + comparator + ' ' + value;
                    return Restrictions.sqlRestriction(sqlConditionBuilder);
                }
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Query on '" + leftFieldCondition + "' is not a user set property. Ignore this condition."); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return TRUE_CRITERION;
            }
            if (condition.getLeft() instanceof Field) {
                Field leftField = (Field) condition.getLeft();
                FieldMetadata fieldMetadata = leftField.getFieldMetadata();
                Set<String> aliases = Collections.singleton(fieldMetadata.getContainingType().getName());
                // TODO Ugly code path to fix once test coverage is ok.
                if (leftFieldCondition.position < 0
                        && (!mainType.equals(fieldMetadata.getContainingType()) || fieldMetadata instanceof ReferenceFieldMetadata)) {
                    leftField.accept(StandardQueryHandler.this);
                    aliases = getAliases(mainType, leftField);
                    if (!fieldMetadata.isMany()) {
                        leftFieldCondition.criterionFieldNames.clear();
                        for (String alias : aliases) {
                            if (fieldMetadata instanceof ReferenceFieldMetadata) {
                                // ignored CompoundFieldMetadata
                                if (!(((ReferenceFieldMetadata) fieldMetadata).getReferencedField() instanceof CompoundFieldMetadata)) {
                                    leftFieldCondition.criterionFieldNames.add(alias + '.'
                                            + ((ReferenceFieldMetadata) fieldMetadata).getReferencedField().getName());
                                }
                            } else {
                                leftFieldCondition.criterionFieldNames.add(alias + '.' + fieldMetadata.getName());
                            }
                        }
                    }
                }
                if (leftFieldCondition.isMany || rightFieldCondition.isMany) {
                    // This is what could be done with Hibernate 4 for searches that includes conditions on collections:
                    // criteria = criteria.createCriteria(leftFieldCondition.criterionFieldNames);
                    // This is what is done on Hibernate 3.5.6
                    if (criteria instanceof CriteriaImpl) {
                        Iterator iterator = ((CriteriaImpl) criteria).iterateSubcriteria();
                        Criteria typeCheckCriteria = criteria;
                        while (iterator.hasNext()) {
                            Criteria subCriteria = (Criteria) iterator.next();
                            if (aliases.contains(subCriteria.getAlias())) {
                                typeCheckCriteria = subCriteria;
                                break;
                            }
                        }
                        if (leftFieldCondition.position >= 0) {
                            return new ManyFieldCriterion(datasource, typeCheckCriteria, resolver, fieldMetadata, condition
                                    .getRight().accept(VALUE_ADAPTER), leftFieldCondition.position);
                        } else {
                            return new ManyFieldCriterion(datasource, typeCheckCriteria, resolver, fieldMetadata, condition
                                    .getRight().accept(VALUE_ADAPTER));
                        }
                    } else {
                        throw new IllegalStateException("Expected a criteria instance of " + CriteriaImpl.class.getName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }
            if (!rightFieldCondition.isProperty) { // "Standard" comparison between a field and a constant value.
                Object compareValue = condition.getRight().accept(VALUE_ADAPTER);
                if (condition.getLeft() instanceof Field) {
                    Field leftField = (Field) condition.getLeft();
                    FieldMetadata fieldMetadata = leftField.getFieldMetadata();
                    if (!fieldMetadata.getType().equals(fieldMetadata.getType())) {
                        compareValue = StorageMetadataUtils.convert(String.valueOf(compareValue), fieldMetadata);
                    }
                }
                if (compareValue instanceof Boolean && predicate == Predicate.EQUALS) {
                    if (!(Boolean) compareValue) {
                        // Special case for boolean: when looking for 'false' value, consider null values as 'false'
                        // too.
                        Criterion current = null;
                        for (String criterionFieldName : leftFieldCondition.criterionFieldNames) {
                            if (current == null) {
                                current = or(eq(criterionFieldName, compareValue), isNull(criterionFieldName));
                            } else {
                                current = or(current, or(eq(criterionFieldName, compareValue), isNull(criterionFieldName)));
                            }
                        }
                        return current;
                    }
                }
                if (predicate == Predicate.EQUALS) {
                    if (compareValue instanceof Object[]) {
                        Field leftField = (Field) condition.getLeft();
                        FieldMetadata fieldMetadata = leftField.getFieldMetadata();
                        Criterion current = null;
                        if (fieldMetadata instanceof ReferenceFieldMetadata) {
                            FieldMetadata referencedField = ((ReferenceFieldMetadata) fieldMetadata).getReferencedField();
                            if (!(referencedField instanceof CompoundFieldMetadata)) {
                                throw new IllegalArgumentException("Expected field '" + referencedField //$NON-NLS-1$
                                        + "' to be a composite key."); //$NON-NLS-1$
                            }
                            Set<String> aliases = getAliases(mainType, leftField);
                            current = null;
                            for (String alias : aliases) {
                                FieldMetadata[] fields = ((CompoundFieldMetadata) referencedField).getFields();
                                Object[] keyValues = (Object[]) compareValue;
                                Criterion[] keyValueCriteria = new Criterion[keyValues.length];
                                int i = 0;
                                for (FieldMetadata keyField : fields) {
                                    Object keyValue = StorageMetadataUtils.convert(
                                            StorageMetadataUtils.toString(keyValues[i], keyField), keyField);
                                    keyValueCriteria[i] = eq(alias + '.' + keyField.getName(), keyValue);
                                    i++;
                                }
                                Criterion newCriterion = makeAnd(keyValueCriteria);
                                if (current == null) {
                                    current = newCriterion;
                                } else {
                                    current = or(newCriterion, current);
                                }
                            }
                        } else {
                            Set<String> aliases = getAliases(mainType, leftField);
                            if (aliases.isEmpty()) {
                                throw new IllegalStateException("No alias found for field '" + fieldMetadata.getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                            }
                            for (String alias : aliases) {
                                current = Restrictions.in(alias + '.' + fieldMetadata.getName(), (Object[]) compareValue);
                            }
                        }
                        if (current == null) {
                            throw new IllegalStateException("No condition was generated for '" + fieldMetadata.getName() + "'."); //$NON-NLS-1$ //$NON-NLS-2$
                        }
                        return current;
                    } else {
                        Criterion current = null;
                        for (String fieldName : leftFieldCondition.criterionFieldNames) {
                            Criterion newCriterion = eq(fieldName, applyDatabaseType(leftFieldCondition, compareValue));
                            if (current == null) {
                                current = newCriterion;
                            } else {
                                current = or(newCriterion, current);
                            }
                        }
                        return current;
                    }
                } else if (predicate == Predicate.CONTAINS) {
                    String value = String.valueOf(compareValue);
                    if (value.length() > 2 && value.startsWith("'") && value.endsWith("'")) { //$NON-NLS-1$//$NON-NLS-2$
                        value = value.substring(1, value.length() - 1);
                    }
                    if (!value.isEmpty()) {
                        if (value.charAt(0) != '%') {
                            value = '%' + value;
                        }
                        if (value.charAt(value.length() - 1) != '%') {
                            value += '%';
                        }
                    } else {
                        value = "%"; //$NON-NLS-1$
                    }
                    Object databaseValue = applyDatabaseType(leftFieldCondition, value); // Converts to CLOB if needed
                    if (datasource.isCaseSensitiveSearch() || !(databaseValue instanceof String)) { // Can't use ilike
                                                                                                    // on CLOBs
                        Criterion current = null;
                        for (String fieldName : leftFieldCondition.criterionFieldNames) {
                            Criterion newCriterion = like(fieldName, databaseValue);
                            if (current == null) {
                                current = newCriterion;
                            } else {
                                current = or(newCriterion, current);
                            }
                        }
                        return current;
                    } else {
                        Criterion current = null;
                        for (String fieldName : leftFieldCondition.criterionFieldNames) {
                            Criterion newCriterion = ilike(fieldName, databaseValue);
                            if (current == null) {
                                current = newCriterion;
                            } else {
                                current = or(newCriterion, current);
                            }
                        }
                        return current;
                    }
                } else if (predicate == Predicate.STARTS_WITH) {
                    Object value = applyDatabaseType(leftFieldCondition, compareValue + "%"); //$NON-NLS-1$
                    if (datasource.isCaseSensitiveSearch() || !(value instanceof String)) {
                        Criterion current = null;
                        for (String fieldName : leftFieldCondition.criterionFieldNames) {
                            Criterion newCriterion = like(fieldName, value);
                            if (current == null) {
                                current = newCriterion;
                            } else {
                                current = or(newCriterion, current);
                            }
                        }
                        return current;
                    } else {
                        Criterion current = null;
                        for (String fieldName : leftFieldCondition.criterionFieldNames) {
                            Criterion newCriterion = ilike(fieldName, value);
                            if (current == null) {
                                current = newCriterion;
                            } else {
                                current = or(newCriterion, current);
                            }
                        }
                        return current;
                    }
                } else if (predicate == Predicate.GREATER_THAN) {
                    Criterion current = null;
                    for (String fieldName : leftFieldCondition.criterionFieldNames) {
                        Criterion newCriterion = gt(fieldName, compareValue);
                        if (current == null) {
                            current = newCriterion;
                        } else {
                            current = or(newCriterion, current);
                        }
                    }
                    return current;
                } else if (predicate == Predicate.LOWER_THAN) {
                    Criterion current = null;
                    for (String fieldName : leftFieldCondition.criterionFieldNames) {
                        Criterion newCriterion = lt(fieldName, compareValue);
                        if (current == null) {
                            current = newCriterion;
                        } else {
                            current = or(newCriterion, current);
                        }
                    }
                    return current;
                } else if (predicate == Predicate.GREATER_THAN_OR_EQUALS) {
                    Criterion current = null;
                    for (String fieldName : leftFieldCondition.criterionFieldNames) {
                        Criterion newCriterion = ge(fieldName, compareValue);
                        if (current == null) {
                            current = newCriterion;
                        } else {
                            current = or(newCriterion, current);
                        }
                    }
                    return current;
                } else if (predicate == Predicate.LOWER_THAN_OR_EQUALS) {
                    Criterion current = null;
                    for (String fieldName : leftFieldCondition.criterionFieldNames) {
                        Criterion newCriterion = le(fieldName, compareValue);
                        if (current == null) {
                            current = newCriterion;
                        } else {
                            current = or(newCriterion, current);
                        }
                    }
                    return current;
                } else {
                    throw new NotImplementedException("No support for predicate '" + predicate.getClass() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            } else { // Since we expect left part to be a field, this 'else' means we're comparing 2 fields
                if (rightFieldCondition.criterionFieldNames.size() > 1) {
                    throw new UnsupportedOperationException("Can't compare to multiple right fields (was " //$NON-NLS-1$
                            + rightFieldCondition.criterionFieldNames.size() + ")."); //$NON-NLS-1$
                }
                String rightValue = rightFieldCondition.criterionFieldNames.get(0);
                if (predicate == Predicate.EQUALS) {
                    Criterion current = null;
                    for (String fieldName : leftFieldCondition.criterionFieldNames) {
                        Criterion newCriterion = Restrictions.eqProperty(fieldName, rightValue);
                        if (current == null) {
                            current = newCriterion;
                        } else {
                            current = or(newCriterion, current);
                        }
                    }
                    return current;
                } else if (predicate == Predicate.GREATER_THAN) {
                    Criterion current = null;
                    for (String fieldName : leftFieldCondition.criterionFieldNames) {
                        Criterion newCriterion = Restrictions.gtProperty(fieldName, rightValue);
                        if (current == null) {
                            current = newCriterion;
                        } else {
                            current = or(newCriterion, current);
                        }
                    }
                    return current;
                } else if (predicate == Predicate.LOWER_THAN) {
                    Criterion current = null;
                    for (String fieldName : leftFieldCondition.criterionFieldNames) {
                        Criterion newCriterion = Restrictions.ltProperty(fieldName, rightValue);
                        if (current == null) {
                            current = newCriterion;
                        } else {
                            current = or(newCriterion, current);
                        }
                    }
                    return current;
                } else if (predicate == Predicate.GREATER_THAN_OR_EQUALS) {
                    // No GTE for properties, do it "manually"
                    Criterion current = null;
                    for (String fieldName : leftFieldCondition.criterionFieldNames) {
                        Criterion newCriterion = or(Restrictions.gtProperty(fieldName, rightValue),
                                Restrictions.eqProperty(fieldName, rightValue));
                        if (current == null) {
                            current = newCriterion;
                        } else {
                            current = or(newCriterion, current);
                        }
                    }
                    return current;
                } else if (predicate == Predicate.LOWER_THAN_OR_EQUALS) {
                    // No LTE for properties, do it "manually"
                    Criterion current = null;
                    for (String fieldName : leftFieldCondition.criterionFieldNames) {
                        Criterion newCriterion = or(Restrictions.ltProperty(fieldName, rightValue),
                                Restrictions.eqProperty(fieldName, rightValue));
                        if (current == null) {
                            current = newCriterion;
                        } else {
                            current = or(newCriterion, current);
                        }
                    }
                    return current;
                } else {
                    throw new NotImplementedException("No support for predicate '" + predicate.getClass() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
    }

    private Object applyDatabaseType(FieldCondition field, Object value) {
        if (field.fieldMetadata != null && TypeMapping.SQL_TYPE_CLOB.equals(field.fieldMetadata.getType().getData(TypeMapping.SQL_TYPE))) {
            return Hibernate.getLobCreator(session).createClob(String.valueOf(value));
        }
        return value;
    }

    @SuppressWarnings("rawtypes")
    public static Criteria findCriteria(Criteria mainCriteria, Set<String> aliases) {
        if (aliases.contains(mainCriteria.getAlias())) {
            return mainCriteria;
        }
        if (mainCriteria instanceof CriteriaImpl) {
            Criteria foundSubCriteria = null;
            Iterator iterator = ((CriteriaImpl) mainCriteria).iterateSubcriteria();
            while (iterator.hasNext()) {
                Criteria subCriteria = (Criteria) iterator.next();
                if (aliases.contains(subCriteria.getAlias())) {
                    foundSubCriteria = subCriteria;
                    break;
                }
            }
            if (foundSubCriteria == null) {
                throw new IllegalStateException("Could not find criteria for type check."); //$NON-NLS-1$
            }
            return foundSubCriteria;
        } else {
            throw new IllegalStateException("Expected a criteria instance of " + CriteriaImpl.class.getName() + "."); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static Criterion makeAnd(Criterion... criterions) {
        if (criterions.length == 1) {
            return criterions[0];
        }
        Criterion current = TRUE_CRITERION;
        for (Criterion cri : criterions) {
            current = Restrictions.and(current, cri);
        }
        return current;
    }

    private void addCondition(FieldCondition condition, String alias, FieldMetadata fieldMetadata) {
        if (fieldMetadata instanceof CompoundFieldMetadata) {
            FieldMetadata[] fields = ((CompoundFieldMetadata) fieldMetadata).getFields();
            for (FieldMetadata subFieldMetadata : fields) {
                condition.criterionFieldNames.add(alias + '.' + subFieldMetadata.getName());
            }
        } else if (fieldMetadata instanceof ReferenceFieldMetadata && mainType.equals(fieldMetadata.getContainingType())) {
            condition.criterionFieldNames.add(getFieldName(fieldMetadata, true));
        } else {
            condition.criterionFieldNames.add(alias + '.' + fieldMetadata.getName());
        }
    }

    private void addCondition(FieldCondition condition, FieldMetadata fieldMetadata) {
        if (fieldMetadata instanceof CompoundFieldMetadata) {
            FieldMetadata[] fields = ((CompoundFieldMetadata) fieldMetadata).getFields();
            for (FieldMetadata subFieldMetadata : fields) {
                condition.criterionFieldNames.add(getFieldName(subFieldMetadata, true));
            }
        } else {
            condition.criterionFieldNames.add(getFieldName(fieldMetadata, true));
        }
    }

    private class CriterionFieldCondition extends VisitorAdapter<FieldCondition> {

        private FieldCondition createInternalCondition(String fieldName) {
            FieldCondition condition = new FieldCondition();
            condition.criterionFieldNames.add(fieldName);
            condition.isMany = false;
            condition.isProperty = true;
            return condition;
        }

        private FieldCondition createConstantCondition() {
            FieldCondition condition = new FieldCondition();
            condition.isProperty = false;
            condition.isMany = false;
            condition.criterionFieldNames.clear();
            return condition;
        }

        @Override
        public FieldCondition visit(Count count) {
            FieldCondition fieldCondition = new FieldCondition();
            fieldCondition.isProperty = false;
            fieldCondition.isComputedProperty = true;
            return fieldCondition;
        }

        @Override
        public FieldCondition visit(Timestamp timestamp) {
            String databaseTimestamp = mappings.getMappingFromDatabase(mainType).getDatabaseTimestamp();
            if (databaseTimestamp != null) {
                return createInternalCondition(databaseTimestamp);
            } else {
                return null;
            }
        }

        @Override
        public FieldCondition visit(TaskId taskId) {
            String taskIdField = mappings.getMappingFromDatabase(mainType).getDatabaseTaskId();
            if (taskIdField != null) {
                return createInternalCondition(Storage.METADATA_TASK_ID);
            } else {
                return null;
            }
        }

        @Override
        public FieldCondition visit(GroupSize groupSize) {
            FieldCondition fieldCondition = new FieldCondition();
            fieldCondition.isProperty = false;
            fieldCondition.isComputedProperty = true;
            return fieldCondition;
        }

        @Override
        public FieldCondition visit(StagingStatus stagingStatus) {
            return createInternalCondition(Storage.METADATA_STAGING_STATUS);
        }

        @Override
        public FieldCondition visit(StagingError stagingError) {
            return createInternalCondition(Storage.METADATA_STAGING_ERROR);
        }

        @Override
        public FieldCondition visit(StagingSource stagingSource) {
            return createInternalCondition(Storage.METADATA_STAGING_SOURCE);
        }

        @Override
        public FieldCondition visit(StagingBlockKey stagingBlockKey) {
            return createInternalCondition(Storage.METADATA_STAGING_BLOCK_KEY);
        }

        @Override
        public FieldCondition visit(StagingHasTask stagingHasTask) {
            return createInternalCondition(Storage.METADATA_STAGING_HAS_TASK);
        }

        @Override
        public FieldCondition visit(Expression expression) {
            if (expression instanceof ComplexTypeExpression) {
                return createConstantCondition();
            } else {
                return super.visit(expression);
            }
        }

        @Override
        public FieldCondition visit(Alias alias) {
            return alias.getTypedExpression().accept(this);
        }

        @Override
        public FieldCondition visit(Field field) {
            FieldCondition condition = new FieldCondition();
            condition.isMany = field.getFieldMetadata().isMany();
            // Use line below to allow searches on collection fields (but Hibernate 4 should be used).
            // condition.criterionFieldNames = field.getFieldMetadata().isMany() ? "elements" : getFieldName(field,
            // StandardQueryHandler.this.mappingMetadataRepository);
            Set<String> aliases = getAliases(mainType, field);
            if (aliases.size() > 0) {
                for (String alias : aliases) {
                    List<FieldMetadata> path = field.getPath();
                    if (path.size() > 1) {
                        // For path with more than 1 element, the alias for the criterion is the *containing* one(s).
                        String containerAlias = pathToAlias.get(mainType.getName() + "/" + path.get(path.size() - 2).getPath()); //$NON-NLS-1$
                        addCondition(condition, containerAlias, field.getFieldMetadata());
                    } else {
                        // For path with size 1, code did not generate an alias for field and returned containing alias.
                        addCondition(condition, alias, field.getFieldMetadata());
                    }
                }
            } else {
                addCondition(condition, field.getFieldMetadata());
            }
            condition.fieldMetadata = field.getFieldMetadata();
            condition.field = field;
            condition.isProperty = true;
            return condition;
        }

        @Override
        public FieldCondition visit(IndexedField indexedField) {
            FieldCondition condition = new FieldCondition();
            condition.isMany = indexedField.getFieldMetadata().isMany();
            // Use line below to allow searches on collection fields (but Hibernate 4 should be used).
            // condition.criterionFieldNames = field.getFieldMetadata().isMany() ? "elements" : getFieldName(field,
            // StandardQueryHandler.this.mappingMetadataRepository);
            condition.criterionFieldNames.add(getFieldName(indexedField));
            condition.isProperty = true;
            condition.position = indexedField.getPosition();
            return condition;
        }

        @Override
        public FieldCondition visit(Id id) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(ConstantCollection collection) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(StringConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(IntegerConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(DateConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(DateTimeConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(BooleanConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(BigDecimalConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(TimeConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(ShortConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(ByteConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(LongConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(DoubleConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(FloatConstant constant) {
            return createConstantCondition();
        }

        @Override
        public FieldCondition visit(ComplexTypeExpression expression) {
            FieldCondition fieldCondition = new FieldCondition();
            fieldCondition.criterionFieldNames.clear();
            fieldCondition.isMany = false;
            fieldCondition.isProperty = true;
            return fieldCondition;
        }

        @Override
        public FieldCondition visit(Type type) {
            FieldCondition fieldCondition = new FieldCondition();
            Field field = type.getField();
            FieldMetadata fieldMetadata = field.getFieldMetadata();
            Set<String> aliases = getAliases(fieldMetadata.getContainingType(), field);
            for (String alias : aliases) {
                fieldCondition.criterionFieldNames.add(alias + ".class"); //$NON-NLS-1$
            }
            fieldCondition.isMany = false;
            fieldCondition.isProperty = true;
            return fieldCondition;
        }
    }
}
