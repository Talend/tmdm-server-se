/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.query.optimization;

import com.amalto.core.query.user.*;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

public class ContainsOptimizer implements Optimizer {

    private static final ContainsOptimization CONTAINS_OPTIMIZATION = new ContainsOptimization();

    private static final Logger LOGGER = Logger.getLogger(ContainsOptimizer.class);

    public void optimize(Select select) {
        synchronized (CONTAINS_OPTIMIZATION) {
            Condition condition = select.getCondition();
            if (condition != null) {
                select.setCondition(condition.accept(CONTAINS_OPTIMIZATION));
            }
        }
    }

    private static class ContainsOptimization extends VisitorAdapter<Condition> {

        private boolean isContains;

        private String containsValue;

        @Override
        public Condition visit(ConstantCondition constantCondition) {
            return constantCondition;
        }

        @Override
        public Condition visit(Select select) {
            return select.getCondition().accept(this);
        }

        @Override
        public Condition visit(StringConstant constant) {
            if (isContains) {
                containsValue = constant.getValue();
            }
            return null;
        }

        @Override
        public Condition visit(Isa isa) {
            return isa;
        }

        @Override
        public Condition visit(UnaryLogicOperator condition) {
            return condition;
        }

        @Override
        public Condition visit(IsEmpty isEmpty) {
            return isEmpty;
        }

        @Override
        public Condition visit(IsNull isNull) {
            return isNull;
        }

        @Override
        public Condition visit(NotIsEmpty notIsEmpty) {
            return notIsEmpty;
        }

        @Override
        public Condition visit(NotIsNull notIsNull) {
            return notIsNull;
        }

        @Override
        public Condition visit(BinaryLogicOperator condition) {
            condition.setLeft(condition.getLeft().accept(this));
            condition.setRight(condition.getRight().accept(this));
            return condition;
        }

        @Override
        public Condition visit(Compare condition) {
            Predicate predicate = condition.getPredicate();
            if (predicate == Predicate.CONTAINS) {
                isContains = true;
                containsValue = StringUtils.EMPTY;
                condition.getRight().accept(this);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Contains value before replace: " + containsValue);
                }
                if (!StringUtils.containsOnly(containsValue, new char[] {'*'})) {
                    String processedContains = containsValue.replace('*', '%');
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Contains value after replace: " + processedContains);
                    }
                    isContains = false;
                    containsValue = StringUtils.EMPTY;
                    return new Compare(condition.getLeft(), Predicate.CONTAINS, new StringConstant(processedContains));
                } else {
                    return UserQueryHelper.TRUE;
                }
            }
            return condition;
        }

        @Override
        public Condition visit(Range range) {
            return range;
        }

        @Override
        public Condition visit(Condition condition) {
            return condition;
        }

        @Override
        public Condition visit(FullText fullText) {
            return fullText;
        }

        @Override
        public Condition visit(FieldFullText fieldFullText) {
            return fieldFullText;
        }
    }
}
