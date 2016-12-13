/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.query.optimization;

import com.amalto.core.query.user.*;
import com.amalto.core.query.user.metadata.*;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import java.util.HashSet;
import java.util.Set;

public class ImplicitOrderBy implements Optimizer {

    private static final Logger               LOGGER                  = Logger.getLogger(ImplicitOrderBy.class);

    private static final AllowImplicitOrderBy ALLOW_IMPLICIT_ORDER_BY = new AllowImplicitOrderBy();

    private final RDBMSDataSource             dataSource;

    public ImplicitOrderBy(RDBMSDataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void optimize(Select select) {
        switch (dataSource.getDialectName()) {
        case H2:        
        case MYSQL:
        case DB2:
        case ORACLE_10G:
        case SQL_SERVER:
        case POSTGRES:
            // If query has paging, check if an implicit order by is needed and possible
            if (select.getPaging().getLimit() < Integer.MAX_VALUE && !select.getTypes().isEmpty() && select.getOrderBy().size() == 0) {
                Set<TypedExpression> missingOrderByExpressions = getMissingOrderByExpressions(select);
                if (!missingOrderByExpressions.isEmpty()) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Adding implicit order by id to keep consistent order through pages."); //$NON-NLS-1$
                    }
                    // Check if projection allow use of implicit order by.
                    boolean enableAddOrder = true;
                    for (TypedExpression typedExpression : select.getSelectedFields()) {
                        Boolean allow = typedExpression.accept(ALLOW_IMPLICIT_ORDER_BY);
                        if (allow != null && !allow) {
                            enableAddOrder = false;
                            break; // No need to check other fields, only one field is enough to cancel
                        }
                    }
                    if (enableAddOrder) {
                        for (TypedExpression missingOrderByExpression : missingOrderByExpressions) {
                            select.addOrderBy(new OrderBy(missingOrderByExpression, OrderBy.Direction.ASC));
                        }
                    } else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Order by not supported by this query: "); //$NON-NLS-1$
                            select.accept(new UserQueryDumpConsole(LOGGER, Level.DEBUG));
                        }
                    }
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Not adding implicit order by (already present in query)"); //$NON-NLS-1$
                    }
                }
            }
            return;
        default:
            throw new IllegalArgumentException("No support for dialect '" + dataSource.getDialectName() + "'."); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    private Set<TypedExpression> getMissingOrderByExpressions(Select select) {
        // Compute already present orderBy expressions
        Set<TypedExpression> orderByExpressions = new HashSet<TypedExpression>();
        for (OrderBy orderBy : select.getOrderBy()) {
            orderByExpressions.add(orderBy.getExpression());
        }
        // Create key fields expressions
        Set<TypedExpression> neededExpressions = new HashSet<TypedExpression>();
        for (FieldMetadata field : select.getTypes().get(0).getKeyFields()) {
            neededExpressions.add(new Field(field));
        }
        // Compute missing expressions
        neededExpressions.removeAll(orderByExpressions);
        return neededExpressions;
    }

    private static class AllowImplicitOrderBy extends VisitorAdapter<Boolean> {

        @Override
        public Boolean visit(Type type) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visit(Count count) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visit(Distinct distinct) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visit(Max max) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visit(Min min) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visit(Timestamp timestamp) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(TaskId taskId) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(StagingStatus stagingStatus) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(StagingError stagingError) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(StagingSource stagingSource) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(GroupSize groupSize) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visit(Field field) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(StringConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(IntegerConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(DateConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(DateTimeConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(BooleanConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(BigDecimalConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(TimeConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(ShortConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(ByteConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(LongConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(DoubleConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(FloatConstant constant) {
            return Boolean.TRUE;
        }

        @Override
        public Boolean visit(Alias alias) {
            return alias.getTypedExpression().accept(this);
        }

        @Override
        public Boolean visit(FullText fullText) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visit(FieldFullText fieldFullText) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visit(IndexedField indexedField) {
            return Boolean.FALSE;
        }

        @Override
        public Boolean visit(StagingBlockKey stagingBlockKey) {
            return Boolean.TRUE;
        }
    }
}