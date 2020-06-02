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
import java.util.stream.Collectors;

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
        StringBuilder sqlFragment = new StringBuilder();
        if (count && !isMSSQLDataDource()) {
            sqlFragment.append("count("); //$NON-NLS-1$
        }
        if (distinct) {
            sqlFragment.append("distinct "); //$NON-NLS-1$
        }
        Set<String> keyNameSet = containingType.getKeyFields().stream().map((item) -> resolver.get(item)).collect(Collectors.toSet());
        SpecialDBSQLProvider.selectDataSource(dataSource.getDialectName()).toSqlString(sqlFragment, criteriaQuery, subCriteria,
                containerTable, collectionTable, keyNameSet);

        if (count && !isMSSQLDataDource()) {
            sqlFragment.append(")"); //$NON-NLS-1$
        }
        sqlFragment.append(" as y").append(position).append('_'); //$NON-NLS-1$
        if (count && isMSSQLDataDource()) {
            sqlFragment = preProcessSQL(sqlFragment, criteria, containerTable, criteriaQuery, position);
        }
        return sqlFragment.toString();
    }

    private static enum SpecialDBSQLProvider {
          INNER_POSTGRES(DataSourceDialect.POSTGRES) {

              public void toFunctionNames(StringBuilder sqlFragment, String containerTable, String collectionTable) {
                  sqlFragment.append("(select string_agg(") //$NON-NLS-1$
                      .append(collectionTable)
                      .append(".value, ',') FROM ").append(containerTable);
              }
          },

          INNER_H2(DataSourceDialect.H2) {

              public void toFunctionNames(StringBuilder sqlFragment, String containerTable, String collectionTable) {
                  INNER_MYSQL.toFunctionNames(sqlFragment, containerTable, collectionTable);
              }
          },

          INNER_MYSQL(DataSourceDialect.MYSQL) {

              public void toFunctionNames(StringBuilder sqlFragment, String containerTable, String collectionTable) {
                  sqlFragment.append("(select group_concat(") //$NON-NLS-1$
                      .append(collectionTable)
                      .append(".value separator ',') FROM ").append(containerTable); //$NON-NLS-1$
              }
          },

          INNER_ORACLE_10G(DataSourceDialect.ORACLE_10G) {

              public void toFunctionNames(StringBuilder sqlFragment, String containerTable, String collectionTable) {
                  sqlFragment.append("(select listagg(") //$NON-NLS-1$
                      .append(collectionTable)
                      .append(".value, ',') WITHIN GROUP (ORDER BY pos) FROM ").append(containerTable); //$NON-NLS-1$
              }
          },

          INNER_SQL_SERVER(DataSourceDialect.SQL_SERVER) {

              public void toFunctionNames(StringBuilder sqlFragment, String containerTable, String collectionTable) {
                  sqlFragment.append("STUFF((select ',' + ") //$NON-NLS-1$
                      .append(collectionTable)
                      .append(".value FROM ").append(containerTable); //$NON-NLS-1$
              }

              @Override
              public void toJoinEnd(StringBuilder sqlFragment) {
                  sqlFragment.append(" FOR XML PATH ('')), 1, 1, '')"); //$NON-NLS-1$
              }
          },

          INNER_DB2(DataSourceDialect.DB2) {

              public void toFunctionNames(StringBuilder sqlFragment, String containerTable, String collectionTable) {
                  sqlFragment.append("(select listagg(") //$NON-NLS-1$
                      .append(collectionTable)
                      .append(".value, ',') WITHIN GROUP (ORDER BY pos) FROM ").append(containerTable); //$NON-NLS-1$
              }
          };

        private DataSourceDialect dataSourceDialect;

        private SpecialDBSQLProvider(DataSourceDialect dataSourceDialect) {
            this.dataSourceDialect = dataSourceDialect;
        }

        /**
         * Due to different DB to use separate aggregation function, The special enum instance will overwrite the
         * <b>toFunctionNames</b> method.
         */
        public abstract void toFunctionNames(StringBuilder sqlFragment, String containerTable, String collectionTable);

        public void toJoinEnd(StringBuilder sqlFragment) {
            sqlFragment.append(")"); //$NON-NLS-1$
        }

        /**
         * That is a entry point to generate the query statement from multiple tables against different DB. like the
         * following SQL cause from Mysql will be return:
         * 
         * <pre>
         * (select group_concat(AA.value separator ',') FROM CompositeKeyTable INNER JOIN AA 
         *  ON CompositeKeyTable.x_id2 = AA.x_id2 AND CompositeKeyTable.x_id1 = AA.x_id1 
         *  WHERE CompositeKeyTable.x_id2 = this_.x_id2 AND CompositeKeyTable.x_id1 = this_.x_id1)
         * </pre>
         */
        public void toSqlString(StringBuilder sqlFragment, CriteriaQuery criteriaQuery, Criteria subCriteria,
                String containerTable, String collectionTable, Set<String> keyNameSet) {
            toFunctionNames(sqlFragment, containerTable, collectionTable);
            treatedManyFieldJoin(sqlFragment, criteriaQuery, subCriteria, containerTable, collectionTable, keyNameSet);
        }

        /**
         * The implementation of method to query data from multiple tables using SQL INNER JOIN statement. we often want
         * to query data from multiple tables to have a complete result set for analysis. To query data from multiple
         * tables using join statements. The join condition is specified in the INNER JOIN clause after the ON keyword
         * as the expression:<b> ON SingleEntity.x_id1 = SingleEntity_x_name.x_id1 AND SingleEntity.x_id2 =
         * SingleEntity_x_name.x_id2 </b>, if that's a composite key, these key will be related with AND keyword.
         */
        private void treatedManyFieldJoin(StringBuilder sqlFragment, CriteriaQuery criteriaQuery, Criteria subCriteria,
                String containerTable, String collectionTable, Set<String> keyNameSet) {
            boolean flag = true;
            StringBuilder whereClause = new StringBuilder();
            for (String keyName : keyNameSet) {
                if (flag) {
                    sqlFragment.append(" INNER JOIN ").append(collectionTable).append(" ON "); //$NON-NLS-1$ $NON-NLS-1$
                    whereClause.append(" WHERE "); //$NON-NLS-1$
                    flag = false;
                } else {
                    sqlFragment.append(" AND "); //$NON-NLS-1$
                    whereClause.append(" AND "); //$NON-NLS-1$
                }
                sqlFragment.append(containerTable).append('.').append(keyName).append(" = ") //$NON-NLS-1$
                        .append(collectionTable).append('.').append(keyName);

                whereClause.append(containerTable).append('.').append(keyName).append(" = ") //$NON-NLS-1$
                    .append(criteriaQuery.getSQLAlias(subCriteria)).append('.').append(keyName);
            }
            sqlFragment.append(whereClause);
            toJoinEnd(sqlFragment);
        }

        public static SpecialDBSQLProvider selectDataSource(DataSourceDialect dataSourceDialect) {
            for (SpecialDBSQLProvider item : SpecialDBSQLProvider.values()) {
                if (item.dataSourceDialect.equals(dataSourceDialect)) {
                    return item;
                }
            }
            throw new NotImplementedException("Support for repeatable element not implemented for dialect '" + dataSourceDialect + "'.");
        }
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