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

package com.amalto.core.query.user;

import com.amalto.core.query.user.metadata.*;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.talend.mdm.commmon.metadata.*;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class UserQueryDumpConsole implements Visitor<Void> {

    private final DumpPrinter printer;

    public UserQueryDumpConsole() {
        printer = ConsolePrinter.INSTANCE;
    }

    public UserQueryDumpConsole(Logger logger) {
        this(logger, Level.DEBUG);
    }

    public UserQueryDumpConsole(Logger logger, Level priority) {
        printer = new LogPrinter(logger, priority);
    }

    public UserQueryDumpConsole(DumpPrinter printer) {
        this.printer = printer;
    }

    public Void visit(Select select) {
        print("[SELECT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Types"); //$NON-NLS-1$
            increaseIndent();
            {
                List<ComplexTypeMetadata> types = select.getTypes();
                for (ComplexTypeMetadata type : types) {
                    print(type.getName());
                }
            }
            decreaseIndent();

            print("Selected fields"); //$NON-NLS-1$
            increaseIndent();
            {
                List<TypedExpression> selectedFields = select.getSelectedFields();
                for (Expression selectedField : selectedFields) {
                    selectedField.accept(this);
                }
            }
            decreaseIndent();

            print("Join"); //$NON-NLS-1$
            increaseIndent();
            {
                List<Join> joins = select.getJoins();
                if (!joins.isEmpty()) {
                    for (Join join : joins) {
                        join.accept(this);
                    }
                } else {
                    print("<NONE>"); //$NON-NLS-1$
                }

            }
            decreaseIndent();

            print("Condition"); //$NON-NLS-1$
            increaseIndent();
            {
                Condition condition = select.getCondition();
                if (condition != null) {
                    condition.accept(this);
                } else {
                    print("<NONE>"); //$NON-NLS-1$
                }
            }
            decreaseIndent();

            print("Order by"); //$NON-NLS-1$
            increaseIndent();
            {
                if (!select.getOrderBy().isEmpty()) {
                    for (OrderBy current : select.getOrderBy()) {
                        current.accept(this);
                    }
                } else {
                    print("<NONE>"); //$NON-NLS-1$
                }
            }
            decreaseIndent();

            print("Paging"); //$NON-NLS-1$
            increaseIndent();
            {
                Paging paging = select.getPaging();
                if (paging != null) {
                    paging.accept(this);
                } else {
                    print("<NONE>"); //$NON-NLS-1$
                }

            }
            decreaseIndent();
        }
        decreaseIndent();

        return null;
    }

    @Override
    public Void visit(NativeQuery nativeQuery) {
        print("[NATIVE QUERY]"); //$NON-NLS-1$
        increaseIndent();
        {
            print(nativeQuery.getQueryText());
        }
        decreaseIndent();
        return null;
    }

    private void increaseIndent() {
        printer.increaseIndent();
    }

    private void decreaseIndent() {
        printer.decreaseIndent();
    }

    public Void visit(Condition condition) {
        if (condition == UserQueryHelper.TRUE) {
            print("[NO OP (TRUE)]"); //$NON-NLS-1$
        } else {
            print("Unvisited condition: " + condition.toString()); //$NON-NLS-1$
        }
        return null;
    }

    @Override
    public Void visit(ConstantCondition constantCondition) {
        if (constantCondition.value()) {
            print("[NO OP (TRUE)]"); //$NON-NLS-1$
        } else {
            print("[NO OP (FALSE)]"); //$NON-NLS-1$
        }
        return null;
    }

    @Override
    public Void visit(Max max) {
        print("[MAX]"); //$NON-NLS-1$
        increaseIndent();
        {
            max.getExpression().accept(this);
        }
        decreaseIndent();
        return null;
    }

    @Override
    public Void visit(Min min) {
        print("[MIN]"); //$NON-NLS-1$
        increaseIndent();
        {
            min.getExpression().accept(this);
        }
        decreaseIndent();
        return null;
    }

    public Void visit(Compare condition) {
        print("[COMPARE]"); //$NON-NLS-1$
        increaseIndent();
        {
            condition.getLeft().accept(this);
            condition.getPredicate().accept(this);
            condition.getRight().accept(this);
        }
        decreaseIndent();
        return null;
    }

    public Void visit(BinaryLogicOperator condition) {
        print("[BINARY LOGIC OPERATOR]"); //$NON-NLS-1$
        increaseIndent();
        {
            condition.getLeft().accept(this);
            condition.getPredicate().accept(this);
            condition.getRight().accept(this);
        }
        decreaseIndent();
        return null;
    }

    public Void visit(UnaryLogicOperator condition) {
        print("[UNARY LOGIC OPERATOR]"); //$NON-NLS-1$
        increaseIndent();
        {
            condition.getPredicate().accept(this);
            condition.getCondition().accept(this);
        }
        decreaseIndent();
        return null;
    }

    public Void visit(Range range) {
        print("[RANGE]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Expression"); //$NON-NLS-1$
            increaseIndent();
            {
                range.getExpression().accept(this);
            }
            decreaseIndent();
            print("Start"); //$NON-NLS-1$
            increaseIndent();
            {
                range.getStart().accept(this);
            }
            decreaseIndent();
            print("End"); //$NON-NLS-1$
            increaseIndent();
            {
                range.getEnd().accept(this);
            }
            decreaseIndent();
        }
        decreaseIndent();
        return null;
    }

    public Void visit(Timestamp timestamp) {
        print("[Technical field: TIMESTAMP]"); //$NON-NLS-1$
        return null;
    }

    public Void visit(TaskId taskId) {
        print("[Technical field: TASK_ID]"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visit(Type type) {
        print("[Field type name]"); //$NON-NLS-1$
        increaseIndent();
        type.getField().accept(this);
        decreaseIndent();
        return null;
    }

    @Override
    public Void visit(Distinct distinct) {
        print("[DISTINCT]"); //$NON-NLS-1$
        increaseIndent();
        distinct.getExpression().accept(this);
        decreaseIndent();
        return null;
    }

    @Override
    public Void visit(StagingStatus stagingStatus) {
        print("[Technical field: STAGING_STATUS]"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visit(StagingHasTask stagingHasTask) {
        print("[Technical field: STAGING_HAS_TASK]"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visit(StagingError stagingError) {
        print("[Technical field: STAGING_ERROR]"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visit(StagingSource stagingSource) {
        print("[Technical field: STAGING_SOURCE]"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visit(StagingBlockKey stagingBlockKey) {
        print("[Technical field: STAGING_BLOCK_KEY]"); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visit(GroupSize groupSize) {
        print("[Computed field: GROUP_SIZE]"); //$NON-NLS-1$
        return null;
    }

    public Void visit(Join join) {
        print("[JOIN]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("[LEFT]"); //$NON-NLS-1$
            increaseIndent();
            {
                join.getLeftField().accept(this);
            }
            decreaseIndent();
            print("[RIGHT]"); //$NON-NLS-1$
            increaseIndent();
            {
                join.getRightField().accept(this);
            }
            decreaseIndent();
        }
        decreaseIndent();
        return null;
    }

    public Void visit(Expression expression) {
        print("Unvisited expression: " + expression.toString()); //$NON-NLS-1$
        return null;
    }

    public Void visit(Predicate predicate) {
        if (predicate == Predicate.NOT) {
            print("[PREDICATE] NOT"); //$NON-NLS-1$
        } else if (predicate == Predicate.STARTS_WITH) {
            print("[PREDICATE] STARTS WITH"); //$NON-NLS-1$
        } else if (predicate == Predicate.EQUALS) {
            print("[PREDICATE] EQUALS"); //$NON-NLS-1$
        } else if (predicate == Predicate.GREATER_THAN) {
            print("[PREDICATE] GREATER THAN"); //$NON-NLS-1$
        } else if (predicate == Predicate.GREATER_THAN_OR_EQUALS) {
            print("[PREDICATE] GREATER THAN OR EQUALS"); //$NON-NLS-1$
        } else if (predicate == Predicate.LOWER_THAN) {
            print("[PREDICATE] LOWER THAN"); //$NON-NLS-1$
        } else if (predicate == Predicate.LOWER_THAN_OR_EQUALS) {
            print("[PREDICATE] LOWER THAN OR EQUALS"); //$NON-NLS-1$
        } else if (predicate == Predicate.AND) {
            print("[PREDICATE] AND"); //$NON-NLS-1$
        } else if (predicate == Predicate.OR) {
            print("[PREDICATE] OR"); //$NON-NLS-1$
        } else if (predicate == Predicate.CONTAINS) {
            print("[PREDICATE] CONTAINS"); //$NON-NLS-1$
        } else {
            print("Unvisited predicate: " + predicate.toString()); //$NON-NLS-1$
        }
        return null;
    }

    public Void visit(Field field) {
        DefaultMetadataVisitor<String> fieldPrinter = new DefaultMetadataVisitor<String>() {
            @Override
            public String visit(ReferenceFieldMetadata referenceField) {
                return referenceField.getEntityTypeName() + '/' + referenceField.getPath();
            }

            @Override
            public String visit(SimpleTypeFieldMetadata simpleField) {
                return simpleField.getEntityTypeName() + '/' + simpleField.getPath();
            }

            @Override
            public String visit(EnumerationFieldMetadata enumField) {
                return enumField.getEntityTypeName() + '/' + enumField.getPath();
            }

            @Override
            public String visit(ContainedTypeFieldMetadata containedField) {
                return containedField.getEntityTypeName() + '/' + containedField.getPath();
            }
        };

        print("[FIELD]"); //$NON-NLS-1$
        {
            increaseIndent();
            {
                print(field.getFieldMetadata().accept(fieldPrinter));
            }
            increaseIndent();
            {
                if (field.getPath().size() > 1) {
                    print("[PATH]"); //$NON-NLS-1$
                    Iterator<FieldMetadata> pathIterator = field.getPath().iterator();
                    for (int i = 1; pathIterator.hasNext(); i++) {
                        print(i + ") " + pathIterator.next().accept(fieldPrinter)); //$NON-NLS-1$
                    }
                }
            }
            decreaseIndent();
        }
        decreaseIndent();
        return null;
    }

    public Void visit(Alias alias) {
        print("[ALIAS]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Alias name: " + alias.getAliasName()); //$NON-NLS-1$
            alias.getTypedExpression().accept(this);
        }
        decreaseIndent();
        return null;
    }

    public Void visit(Id id) {
        print("[ID REFERENCE]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Type -> '" + id.getType().getName() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
            print("Value -> '" + id.getId() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        decreaseIndent();
        return null;
    }

    @Override
    public Void visit(ConstantCollection collection) {
        print("[CONSTANT COLLECTION]"); //$NON-NLS-1$
        increaseIndent();
        {
            for (Expression expression : collection.getValues()) {
                expression.accept(this);
            }
        }
        decreaseIndent();
        return null;
    }

    public Void visit(StringConstant constant) {
        print("[STRING CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> '" + constant.getValue() + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(IntegerConstant constant) {
        print("[INTEGER CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + constant.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(DateConstant constant) {
        print("[DATE CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + constant.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(DateTimeConstant constant) {
        print("[DATE TIME CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + constant.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(BooleanConstant constant) {
        print("[BOOLEAN CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + constant.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(BigDecimalConstant constant) {
        print("[BIG DECIMAL CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + constant.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(TimeConstant constant) {
        print("[TIME CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + constant.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(ShortConstant constant) {
        print("[SHORT CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + constant.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(ByteConstant constant) {
        print("[BYTE CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + constant.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(LongConstant constant) {
        print("[LONG CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + constant.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(DoubleConstant constant) {
        print("[DOUBLE CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + constant.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(FloatConstant constant) {
        print("[FLOAT CONSTANT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + constant.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(IsEmpty isEmpty) {
        print("[IS EMPTY]"); //$NON-NLS-1$
        increaseIndent();
        {
            isEmpty.getField().accept(this);
        }
        decreaseIndent();
        return null;
    }

    public Void visit(NotIsEmpty notIsEmpty) {
        print("[NOT IS EMPTY]"); //$NON-NLS-1$
        increaseIndent();
        {
            notIsEmpty.getField().accept(this);
        }
        decreaseIndent();
        return null;
    }

    public Void visit(IsNull isNull) {
        print("[IS NULL]"); //$NON-NLS-1$
        increaseIndent();
        {
            isNull.getField().accept(this);
        }
        decreaseIndent();
        return null;
    }

    public Void visit(NotIsNull notIsNull) {
        print("[NOT IS NULL]"); //$NON-NLS-1$
        increaseIndent();
        {
            notIsNull.getField().accept(this);
        }
        decreaseIndent();
        return null;
    }

    public Void visit(FullText fullText) {
        print("[FULLTEXT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + fullText.getValue()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(Isa isa) {
        print("[IS A]"); //$NON-NLS-1$
        increaseIndent();
        {
            isa.getExpression().accept(this);
            print("Type -> " + isa.getType().getName()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    @Override
    public Void visit(ComplexTypeExpression expression) {
        print("Type: " + expression.getTypeName()); //$NON-NLS-1$
        return null;
    }

    @Override
    public Void visit(IndexedField indexedField) {
        print("[INDEXED FIELD]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Field: " + indexedField.getFieldMetadata().getName()); //$NON-NLS-1$
            print("Index: " + indexedField.getPosition()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    @Override
    public Void visit(FieldFullText fieldFullText) {
        print("[FIELD FULL TEXT]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Value -> " + fieldFullText.getValue()); //$NON-NLS-1$
            fieldFullText.getField().accept(this);
        }
        decreaseIndent();
        return null;
    }

    @Override
    public Void visit(At at) {
        print("[HISTORY]"); //$NON-NLS-1$
        increaseIndent();
        {
            synchronized (DateTimeConstant.DATE_FORMAT) {
                print("Date: " + DateTimeConstant.DATE_FORMAT.format(new Date(at.getDateTime()))); //$NON-NLS-1$
                print("Swing: " + at.getSwing()); //$NON-NLS-1$
            }
        }
        decreaseIndent();
        return null;
    }

    public Void visit(OrderBy orderBy) {
        print("[ORDER BY]"); //$NON-NLS-1$
        increaseIndent();
        {
            orderBy.getExpression().accept(this);
            print("Direction: " + orderBy.getDirection()); //$NON-NLS-1$
        }
        decreaseIndent();
        return null;
    }

    public Void visit(Paging paging) {
        print("[PAGING]"); //$NON-NLS-1$
        increaseIndent();
        {
            print("Start: " + paging.getStart()); //$NON-NLS-1$
            if (paging.getLimit() == Integer.MAX_VALUE) {
                print("Limit: <NO LIMIT>"); //$NON-NLS-1$
            } else {
                print("Limit: " + paging.getLimit()); //$NON-NLS-1$
            }
        }
        decreaseIndent();
        return null;
    }

    public Void visit(Count count) {
        print("[COUNT]"); //$NON-NLS-1$
        if (count.getExpression() != null) {
            increaseIndent();
            {
                count.getExpression().accept(this);
            }
            decreaseIndent();
        }
        return null;
    }

    private void print(String message) {
        printer.print(message);
    }
    
    public static interface DumpPrinter {
        
        void increaseIndent();
        
        void print(String message);
        
        void decreaseIndent();
    }
    
    public static class ConsolePrinter implements DumpPrinter {

        public static DumpPrinter INSTANCE = new ConsolePrinter();

        private int indent;

        private ConsolePrinter() {
        }

        @Override
        public void increaseIndent() {
            indent++;
        }

        @Override
        public void print(String message) {
            for (int i = 0; i < indent; i++) {
                System.out.print('\t');
            }
            System.out.println(message);
        }

        @Override
        public void decreaseIndent() {
            indent--;
        }
    }

    public static class LogPrinter implements DumpPrinter {

        private final Logger logger;

        private final Priority priority;

        private int indent;

        LogPrinter(Logger logger, Priority priority) {
            this.logger = logger;
            this.priority = priority;
        }

        @Override
        public void increaseIndent() {
            indent++;
        }

        @Override
        public void print(String message) {
            StringBuilder indentString = new StringBuilder();
            for (int i = 0; i < indent; i++) {
                indentString.append('\t');
            }
            indentString.append(message);
            logger.log(priority, indentString.toString());
        }

        @Override
        public void decreaseIndent() {
            indent--;
        }
    }
}
