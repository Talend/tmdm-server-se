/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.storage.hibernate;

import static com.amalto.core.query.user.UserQueryBuilder.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.type.IntegerType;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import com.amalto.core.query.user.Paging;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.storage.EmptyIterator;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;

public class InClauseOptimization extends StandardQueryHandler {

    private static final Logger LOGGER = Logger.getLogger(InClauseOptimization.class);

    private static final int IN_CLAUSE_MAX = 1000;

    private final Mode mode;

    public static enum Mode {
        SUB_QUERY,
        CONSTANT
    }

    public InClauseOptimization(Storage storage, MappingRepository mappings, TableResolver resolver,
            StorageClassLoader storageClassLoader, Session session, Select select, List<TypedExpression> selectedFields,
            Set<ResultsCallback> callbacks, Mode mode) {
        super(storage, mappings, resolver, storageClassLoader, session, select, selectedFields, callbacks);
        this.mode = mode;
    }

    @Override
    public StorageResults visit(Select select) {
        StorageResults results = null;
        switch (mode) {
        case SUB_QUERY:
            throw new NotImplementedException("Not supported in this MDM version"); //$NON-NLS-1$
            //
            // Uncomment lines below for supporting this (NOT SUPPORTED ON ALL DATABASES!!!).
            //
            // ComplexTypeMetadata typeMetadata = (ComplexTypeMetadata) MetadataUtils.getSuperConcreteType(mainType);
            // String idColumnName = typeMetadata.getKeyFields().iterator().next().getName();
            // String tableName = typeMetadata.getName();
            // criteria.add(new IdInSubQueryClause(idColumnName, tableName, start, limit));
        case CONSTANT:
            // Create in clause for the id
            ComplexTypeMetadata mainType = select.getTypes().get(0);
            Paging paging = select.getPaging();
            int start = paging.getStart();
            int limit = paging.getLimit();
            UserQueryBuilder qb = from(mainType).selectId(mainType).start(start).limit(limit);
            if (select.getCondition() != null) {
                qb.where(select.getCondition());
            }
            List<String[]> constants;
            if (limit != Integer.MAX_VALUE) {
                constants = new ArrayList<String[]>(limit);
            } else {
                constants = new LinkedList<String[]>();
            }
            // Get ids for constant list
            StorageResults records = storage.fetch(qb.getSelect()); // Expects an active transaction here
            // TMDM-7124. Oracle doesn't like > 1000 number of values in 'IN (...)' clause,
            // and too many values in 'IN (...)' clause hurt database performance
            if (records.getSize() >= IN_CLAUSE_MAX) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Too many ids in 'IN()' clause, abort this optimization. Total ids = " + records.getSize()); //$NON-NLS-1$
                }
                results = super.visit(select);
            } else {
                for (DataRecord record : records) {
                    Set<FieldMetadata> setFields = record.getSetFields();
                    String[] constant = new String[setFields.size()];
                    int i = 0;
                    for (FieldMetadata setField : setFields) {
                        Object o = record.get(setField);
                        constant[i++] = String.valueOf(o);
                    }
                    constants.add(constant);
                }
                if (!constants.isEmpty()) {
                    // Standard criteria
                    Criteria criteria = createCriteria(select);
                    criteria.add(new IdInConstantClause(mainType.getKeyFields(), resolver, constants));
                    results = createResults(criteria.list(), select.isProjection());
                } else {
                    results = new HibernateStorageResults(storage, select, EmptyIterator.INSTANCE);
                }
            }
        }
      return results;
    }

    private static class IdInConstantClause implements Criterion {

        private final Collection<FieldMetadata> keyFields;

        private final TableResolver resolver;

        private final List<String[]> values;

        public IdInConstantClause(Collection<FieldMetadata> keyFields, TableResolver resolver, List<String[]> values) {
            this.keyFields = keyFields;
            this.resolver = resolver;
            this.values = values;
        }

        @Override
        public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            StringBuilder inClause = new StringBuilder();
            String alias = criteriaQuery.getSQLAlias(criteria);
            Iterator<FieldMetadata> keyFieldIterator = keyFields.iterator();
            int i = 0;
            while (keyFieldIterator.hasNext()) {
                inClause.append(alias).append('.').append(resolver.get(keyFieldIterator.next())).append(" IN ").append('('); //$NON-NLS-1$
                Iterator<String[]> valuesIterator = values.iterator();
                while (valuesIterator.hasNext()) {
                    inClause.append('\'').append(valuesIterator.next()[i]).append('\'');
                    if (valuesIterator.hasNext()) {
                        inClause.append(',');
                    }
                }
                inClause.append(')');
                if (keyFieldIterator.hasNext()) {
                    inClause.append(" AND "); //$NON-NLS-1$
                }
                i++;
            }
            return inClause.toString();
        }

        @Override
        public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            return new TypedValue[0];
        }
    }

    // Not used: but would be interesting to use this on databases that support it.
    private static class IdInSubQueryClause implements Criterion {

        private final String idColumnName;

        private final String tableName;

        private final int start;

        private final int limit;

        public IdInSubQueryClause(String idColumnName, String tableName, int start, int limit) {
            this.idColumnName = idColumnName;
            this.tableName = tableName;
            this.start = start;
            this.limit = limit;
        }

        @Override
        public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            Dialect dialect = criteriaQuery.getFactory().getDialect();
            boolean useOffset = dialect.supportsLimitOffset();
            if (!dialect.supportsLimit()) {
                throw new HibernateException("Can not use this optimization: database does not support limits."); //$NON-NLS-1$
            }
            String sql = "SELECT " //$NON-NLS-1$
                    + idColumnName + " FROM " //$NON-NLS-1$
                    + tableName;
            String sqlWithLimitString = dialect.getLimitString(sql, useOffset ? start : 0,
                    dialect.useMaxForLimit() ? Integer.MAX_VALUE : limit);
            String alias = criteriaQuery.getSQLAlias(criteria);
            return alias + "." + idColumnName + " IN (" + sqlWithLimitString + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }

        @Override
        public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
            Dialect dialect = criteriaQuery.getFactory().getDialect();
            boolean useOffset = dialect.supportsLimitOffset() && start > 0;
            if (useOffset) {
                return new TypedValue[] { new TypedValue(new IntegerType(), limit, EntityMode.POJO),
                        new TypedValue(new IntegerType(), start, EntityMode.POJO) };
            } else {
                return new TypedValue[] { new TypedValue(new IntegerType(), limit, EntityMode.POJO) };
            }
        }
    }
}
