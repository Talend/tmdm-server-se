/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.NotExpression;
import org.hibernate.criterion.NotNullExpression;
import org.hibernate.criterion.NullExpression;
import org.hibernate.criterion.SimpleExpression;
import org.hibernate.criterion.SimpleProjection;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.CriteriaImpl.CriterionEntry;
import org.hibernate.type.BigDecimalType;
import org.hibernate.type.StringType;
import org.hibernate.type.Type;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.datasource.RDBMSDataSource.DataSourceDialect;

class ManyFieldProjection extends SimpleProjection {

    private static final long serialVersionUID = 6859830208040310510L;

    private final Set<String> aliases;

    private final FieldMetadata field;

    private final TableResolver resolver;

    private final RDBMSDataSource dataSource;

    private boolean distinct = false;

    private boolean count = false;

    ManyFieldProjection(Set<String> aliases, FieldMetadata field, TableResolver resolver, RDBMSDataSource dataSource) {
        this.aliases = aliases;
        this.field = field;
        this.resolver = resolver;
        this.dataSource = dataSource;
    }

    @Override
    public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) throws HibernateException {
        Criteria subCriteria = StandardQueryHandler.findCriteria(criteria, aliases);
        ComplexTypeMetadata containingType = field.getContainingType();
        String containerTable = resolver.get(containingType);
        String collectionTable = resolver.getCollectionTable(field);
        String containerIdColumn = resolver.get(containingType.getKeyFields().iterator().next());
        StringBuilder sqlFragment = new StringBuilder();
        if (count && !isMSSQLDataDource()) {
            sqlFragment.append("count("); //$NON-NLS-1$
        }
        if (distinct) {
            sqlFragment.append("distinct "); //$NON-NLS-1$
        }
        switch (dataSource.getDialectName()) {
            // For Postgres, uses "string_agg" function (introduced in 9.0).
            case POSTGRES:
                sqlFragment.append("(select string_agg(") //$NON-NLS-1$
                        .append(collectionTable)
                        .append(".value, ',') FROM ").append(containerTable); //$NON-NLS-1$
                for (FieldMetadata currentKey : containingType.getKeyFields()) {
                    String keyName = resolver.get(currentKey);
                    sqlFragment.append(" INNER JOIN ") //$NON-NLS-1$
                            .append(collectionTable)
                            .append(" on ") //$NON-NLS-1$
                            .append(containerTable).append('.').append(keyName)
                            .append(" = ") //$NON-NLS-1$
                            .append(collectionTable).append('.').append(keyName);
                }
                sqlFragment.append(" WHERE ") //$NON-NLS-1$
                        .append(containerTable)
                        .append('.')
                        .append(containerIdColumn)
                        .append(" = ") //$NON-NLS-1$
                        .append(criteriaQuery.getSQLAlias(subCriteria))
                        .append('.')
                        .append(containerIdColumn).append(")"); //$NON-NLS-1$
                break;
            // Following databases support group_concat function
            case H2:
            case MYSQL:
                sqlFragment.append("(select group_concat(") //$NON-NLS-1$
                        .append(collectionTable)
                        .append(".value separator ',') FROM ").append(containerTable); //$NON-NLS-1$
                for (FieldMetadata currentKey : containingType.getKeyFields()) {
                    String keyName = resolver.get(currentKey);
                    sqlFragment.append(" INNER JOIN ") //$NON-NLS-1$
                            .append(collectionTable)
                            .append(" on ") //$NON-NLS-1$
                            .append(containerTable).append('.').append(keyName)
                            .append(" = ") //$NON-NLS-1$
                            .append(collectionTable).append('.').append(keyName);
                }
                sqlFragment.append(" WHERE ") //$NON-NLS-1$
                        .append(containerTable)
                        .append('.')
                        .append(containerIdColumn)
                        .append(" = ") //$NON-NLS-1$
                        .append(criteriaQuery.getSQLAlias(subCriteria))
                        .append('.')
                        .append(containerIdColumn).append(")"); //$NON-NLS-1$
                break;
            // Use Oracle 10g "listagg" function (no group_concat on Oracle).
            case ORACLE_10G:
                sqlFragment.append("(select listagg(") //$NON-NLS-1$
                        .append(collectionTable)
                        .append(".value, ',') WITHIN GROUP (ORDER BY pos) FROM ").append(containerTable); //$NON-NLS-1$
                for (FieldMetadata currentKey : containingType.getKeyFields()) {
                    String keyName = resolver.get(currentKey);
                    sqlFragment.append(" INNER JOIN ") //$NON-NLS-1$
                            .append(collectionTable)
                            .append(" on ") //$NON-NLS-1$
                            .append(containerTable).append('.').append(keyName)
                            .append(" = ") //$NON-NLS-1$
                            .append(collectionTable).append('.').append(keyName);
                }
                sqlFragment.append(" WHERE ") //$NON-NLS-1$
                        .append(containerTable)
                        .append('.')
                        .append(containerIdColumn)
                        .append(" = ") //$NON-NLS-1$
                        .append(criteriaQuery.getSQLAlias(subCriteria))
                        .append('.')
                        .append(containerIdColumn).append(")"); //$NON-NLS-1$
                break;
            // SQL Server doesn't support the group_concat function -> use "stuff" function
            case SQL_SERVER:
                sqlFragment.append("STUFF((select ',' + ") //$NON-NLS-1$
                        .append(collectionTable)
                        .append(".value FROM ").append(containerTable); //$NON-NLS-1$
                for (FieldMetadata currentKey : containingType.getKeyFields()) {
                    String keyName = resolver.get(currentKey);
                    sqlFragment.append(" INNER JOIN ") //$NON-NLS-1$
                            .append(collectionTable)
                            .append(" on ") //$NON-NLS-1$
                            .append(containerTable).append('.').append(keyName)
                            .append(" = ") //$NON-NLS-1$
                            .append(collectionTable).append('.').append(keyName);
                }
                sqlFragment.append(" WHERE ") //$NON-NLS-1$
                        .append(containerTable)
                        .append('.')
                        .append(containerIdColumn)
                        .append(" = ") //$NON-NLS-1$
                        .append(criteriaQuery.getSQLAlias(subCriteria))
                        .append('.')
                        .append(containerIdColumn)
                        .append(" FOR XML PATH ('')), 1, 1, '')"); //$NON-NLS-1$
                break;
            // DB2 supports listagg() function after DB2 9.7
            case DB2:
                sqlFragment.append("(select listagg(") //$NON-NLS-1$
                        .append(collectionTable)
                        .append(".value, ',') WITHIN GROUP (ORDER BY pos) FROM ").append(containerTable); //$NON-NLS-1$
                for (FieldMetadata currentKey : containingType.getKeyFields()) {
                    String keyName = resolver.get(currentKey);
                    sqlFragment.append(" INNER JOIN ") //$NON-NLS-1$
                            .append(collectionTable)
                            .append(" on ") //$NON-NLS-1$
                            .append(containerTable).append('.').append(keyName)
                            .append(" = ") //$NON-NLS-1$
                            .append(collectionTable).append('.').append(keyName);
                }
                sqlFragment.append(" WHERE ") //$NON-NLS-1$
                        .append(containerTable)
                        .append('.')
                        .append(containerIdColumn)
                        .append(" = ") //$NON-NLS-1$
                        .append(criteriaQuery.getSQLAlias(subCriteria))
                        .append('.')
                        .append(containerIdColumn).append(")"); //$NON-NLS-1$
                break;
            default:
                throw new NotImplementedException("Support for repeatable element not implemented for dialect '" + dataSource.getDialectName() + "'.");
        }
        if (count && !isMSSQLDataDource()) {
            sqlFragment.append(")"); //$NON-NLS-1$
        }
        sqlFragment.append(" as y").append(position).append('_'); //$NON-NLS-1$
        if (count && isMSSQLDataDource()) {
            sqlFragment = preProcessSQL(sqlFragment, criteria, containerTable, criteriaQuery, position);
        }
        return sqlFragment.toString();
    }

    /**
     * Below method will generate special SQL fragment template for MS Server when existing count and distinct function.
     * <pre>
     *   distinct (select count(*) from (select distinct this_1.x_id as y0_ from Product this_1 where this_1.x_price>2) as yy0_ where y0_ is not null) as y0_
     * </pre>
     */
    private StringBuilder preProcessSQL(StringBuilder sqlFragment, Criteria subCriteria, String containerTable,
            CriteriaQuery criteriaQuery, int position) {
        sqlFragment.insert(0, " distinct (select count(*) from (select ");//$NON-NLS-1$
        Iterator<CriterionEntry> iterator = ((CriteriaImpl) subCriteria).iterateExpressionEntries();
        String whereCondition = "";//$NON-NLS-1$
        if (iterator.hasNext()) {
            CriterionEntry criterionEntry = (CriterionEntry) iterator.next();
            Criterion criterion = criterionEntry.getCriterion();
            whereCondition = getHandler(criterion).handle(criterion, subCriteria, containerTable, criteriaQuery);
        }
        sqlFragment.append(" from ").append(containerTable).append(" ").append(criteriaQuery.getSQLAlias(subCriteria)); //$NON-NLS-1$ $NON-NLS-2$
        if (StringUtils.isNotEmpty(whereCondition)) {
            sqlFragment.append(" where ").append(whereCondition);//$NON-NLS-1$
        }
        sqlFragment.append(" ) as my_y0_ where y0_ is not null)").append(" as y").append(position).append('_').append(" ");//$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ $NON-NLS-4$

        return new StringBuilder(sqlFragment.toString().replaceAll(criteriaQuery.getSQLAlias(subCriteria),
                criteriaQuery.getSQLAlias(subCriteria) + position));
    }

    private static WhereHandler getHandler(Criterion criterion) {
        if (criterion instanceof LogicalExpression) {
            return new LogicalWhereHandler();
        } else if (criterion instanceof SimpleExpression) {
            return new SimpleWhereHandler();
        } else if (criterion instanceof NotExpression) {
            return new NotWhereHandler();
        } else if (criterion instanceof NullExpression) {
            return new NullWhereHandler();
        } else if (criterion instanceof NotNullExpression) {
            return new NotNullWhereHandler();
        } else {
            return new DefaultWhereHandler();
        }
    }

    private static interface WhereHandler {

        String handle(Criterion criterion, Criteria subCriteria, String containerTable, CriteriaQuery criteriaQuery);
    }

    private static class DefaultWhereHandler implements WhereHandler {

        @Override
        public String handle(Criterion criterion, Criteria subCriteria, String containerTable, CriteriaQuery criteriaQuery) {
            return StringUtils.EMPTY;
        }
    }

    private static class NotNullWhereHandler implements WhereHandler {

        @Override
        public String handle(Criterion criterion, Criteria subCriteria, String containerTable, CriteriaQuery criteriaQuery) {
            return criterion.toSqlString(subCriteria, criteriaQuery);
        }
    }

    private static class NullWhereHandler implements WhereHandler {

        @Override
        public String handle(Criterion criterion, Criteria subCriteria, String containerTable, CriteriaQuery criteriaQuery) {
            return criterion.toSqlString(subCriteria, criteriaQuery);
        }
    }

    private static class NotWhereHandler implements WhereHandler {

        @Override
        public String handle(Criterion criterion, Criteria subCriteria, String containerTable, CriteriaQuery criteriaQuery) {
            return getWhereClause(criterion, subCriteria, criteriaQuery);
        }
    }

    private static class SimpleWhereHandler implements WhereHandler {

        @Override
        public String handle(Criterion criterion, Criteria subCriteria, String containerTable, CriteriaQuery criteriaQuery) {
            return getWhereClause(criterion, subCriteria, criteriaQuery);
        }
    }

    private static String getWhereClause(Criterion basedExp, Criteria subCriteria, CriteriaQuery criteriaQuery) {
        String whereCondition = basedExp.toSqlString(subCriteria, criteriaQuery);
        TypedValue[] typedValues = basedExp.getTypedValues(subCriteria, criteriaQuery);
        for (TypedValue typedValue : typedValues) {
            if (typedValue.getType() instanceof BigDecimalType) {
                whereCondition = whereCondition.replaceFirst("\\?", typedValue.getValue().toString());//$NON-NLS-1$
            } else {
                whereCondition = whereCondition.replaceFirst("\\?", "'" + typedValue.getValue() + "'");//$NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
            }
        }
        return whereCondition;
    }

    private static class LogicalWhereHandler implements WhereHandler {
        @Override
        public String handle(Criterion criterion, Criteria subCriteria, String containerTable, CriteriaQuery criteriaQuery) {
            return getWhereClause(criterion, subCriteria, criteriaQuery);
        }
    }

    private boolean isMSSQLDataDource() {
        if (DataSourceDialect.SQL_SERVER == dataSource.getDialectName()) {
            return true;
        } else {
            return false;
        }
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public void setCount(boolean count) {
        this.count = count;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public boolean isCount() {
        return count;
    }

    @Override
    public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
        return new Type[]{new StringType()};
    }
}
