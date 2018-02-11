/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm;

import com.amalto.core.query.user.*;
import com.amalto.core.query.user.Compare;
import com.amalto.core.query.user.metadata.*;
import junit.framework.TestCase;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.query.QueryParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class QueryParserTest extends TestCase {

    private MetadataRepository repository;

    public void setUp() throws Exception {
        super.setUp();
        repository = new MetadataRepository();
        repository.load(QueryParserTest.class.getResourceAsStream("metadata.xsd"));
    }

    public void testArguments() {
        try {
            QueryParser.newParser(null);
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
        QueryParser parser = QueryParser.newParser(new MetadataRepository());
        try {
            parser.parse(((String) null));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
        try {
            parser.parse(((InputStream) null));
            fail();
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    public void testQuery1() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query1.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertEquals(1, select.getTypes().size());
        assertEquals("Type1", select.getTypes().get(0).getName()); //$NON-NLS-1$
    }

    public void testQuery2() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query2.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertEquals(Compare.class, condition.getClass());
        assertEquals(Predicate.EQUALS, ((Compare) condition).getPredicate());
        assertEquals(StringConstant.class, ((Compare) condition).getRight().getClass());
        assertEquals(Field.class, ((Compare) condition).getLeft().getClass());
    }

    public void testQuery3() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query3.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertEquals(BinaryLogicOperator.class, condition.getClass());
        assertEquals(Predicate.AND, ((BinaryLogicOperator) condition).getPredicate());
        assertEquals(Compare.class, ((BinaryLogicOperator) condition).getLeft().getClass());
        assertEquals(Compare.class, ((BinaryLogicOperator) condition).getRight().getClass());
    }

    public void testQuery4() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query4.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertEquals(BinaryLogicOperator.class, condition.getClass());
        assertEquals(Predicate.OR, ((BinaryLogicOperator) condition).getPredicate());
        assertEquals(Compare.class, ((BinaryLogicOperator) condition).getLeft().getClass());
        assertEquals(Compare.class, ((BinaryLogicOperator) condition).getRight().getClass());
    }

    public void testQuery5() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query5.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertEquals(BinaryLogicOperator.class, condition.getClass());
        assertEquals(Predicate.OR, ((BinaryLogicOperator) condition).getPredicate());
        assertEquals(Compare.class, ((BinaryLogicOperator) condition).getLeft().getClass());
        assertEquals(BinaryLogicOperator.class, ((BinaryLogicOperator) condition).getRight().getClass());
        assertEquals(Predicate.AND, ((BinaryLogicOperator) ((BinaryLogicOperator) condition).getRight()).getPredicate());
    }

    public void testQuery6() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query6.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertEquals(1, select.getSelectedFields().size());
        assertEquals(Field.class, select.getSelectedFields().get(0).getClass());
    }

    public void testQuery7() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query7.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertEquals(2, select.getSelectedFields().size());
        assertEquals(Field.class, select.getSelectedFields().get(0).getClass());
        assertEquals(Alias.class, select.getSelectedFields().get(1).getClass());
    }

    public void testQuery8() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query8.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        final List<Predicate> metPredicates = new ArrayList<>();
        condition.accept(new VisitorAdapter<Object>() {

            @Override
            public Object visit(BinaryLogicOperator condition) {
                condition.getLeft().accept(this);
                condition.getRight().accept(this);
                return null;
            }

            @Override
            public Object visit(UnaryLogicOperator condition) {
                condition.getCondition().accept(this);
                return null;
            }

            @Override
            public Object visit(Compare condition) {
                metPredicates.add(condition.getPredicate());
                return null;
            }
        });
        assertEquals(7, metPredicates.size());
        assertTrue(metPredicates.contains(Predicate.EQUALS));
        assertTrue(metPredicates.contains(Predicate.GREATER_THAN));
        assertTrue(metPredicates.contains(Predicate.GREATER_THAN_OR_EQUALS));
        assertTrue(metPredicates.contains(Predicate.LOWER_THAN));
        assertTrue(metPredicates.contains(Predicate.LOWER_THAN_OR_EQUALS));
        assertTrue(metPredicates.contains(Predicate.CONTAINS));
        assertTrue(metPredicates.contains(Predicate.STARTS_WITH));
    }

    public void testQuery9() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query9.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertEquals(UnaryLogicOperator.class, condition.getClass());
        assertEquals(Predicate.NOT, ((UnaryLogicOperator) condition).getPredicate());
        assertEquals(Compare.class, ((UnaryLogicOperator) condition).getCondition().getClass());
        assertEquals(Field.class, ((Compare) ((UnaryLogicOperator) condition).getCondition()).getLeft().getClass());
    }

    public void testQuery10() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query10.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertEquals(2, select.getJoins().size());
        assertEquals("fk2", select.getJoins().get(0).getLeftField().getFieldMetadata().getName());
        assertEquals("fk3", select.getJoins().get(1).getLeftField().getFieldMetadata().getName());
    }

    public void testQuery11() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query11.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertEquals(0, select.getPaging().getStart());
        assertEquals(10, select.getPaging().getLimit());
    }

    public void testQuery12() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query12.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        assertTrue(condition instanceof Compare);
        Expression left = ((Compare) condition).getLeft();
        assertTrue(left instanceof IndexedField);
        IndexedField indexedField = (IndexedField) left;
        assertEquals(0, indexedField.getPosition());
    }

    public void testQuery13() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query13.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertEquals(2, select.getSelectedFields().size());
        assertTrue(select.getSelectedFields().get(0) instanceof Alias);
        Alias alias = (Alias) select.getSelectedFields().get(0);
        assertTrue(alias.getTypedExpression() instanceof Max);
        assertTrue(select.getSelectedFields().get(0) instanceof Alias);
        alias = (Alias) select.getSelectedFields().get(1);
        assertTrue(alias.getTypedExpression() instanceof Min);
    }

    public void testQuery14() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query14.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertEquals(Isa.class, condition.getClass());
        assertEquals("Contained2", ((Isa) condition).getType().getName());
        assertEquals(Field.class, ((Isa) condition).getExpression().getClass());
    }

    public void testQuery15() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query15.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        At condition = select.getHistory();
        assertNotNull(condition);
        assertEquals(At.Swing.BEFORE, condition.getSwing());
        assertEquals(1000, condition.getDateTime());
    }

    public void testQuery16() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query16.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertEquals(2, select.getOrderBy().size());
        OrderBy orderBy = select.getOrderBy().get(0);
        assertTrue(orderBy.getExpression() instanceof Field);
        assertEquals("id", ((Field) orderBy.getExpression()).getFieldMetadata().getPath());
        assertEquals("Type1", ((Field) orderBy.getExpression()).getFieldMetadata().getEntityTypeName());
        assertEquals(OrderBy.Direction.DESC, orderBy.getDirection());
        orderBy = select.getOrderBy().get(1);
        assertTrue(orderBy.getExpression() instanceof Field);
        assertEquals("value1", ((Field) orderBy.getExpression()).getFieldMetadata().getPath());
        assertEquals("Type1", ((Field) orderBy.getExpression()).getFieldMetadata().getEntityTypeName());
        assertEquals(OrderBy.Direction.ASC, orderBy.getDirection());
    }

    public void testQuery17() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query17.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertEquals(FieldFullText.class, condition.getClass());
        assertEquals("1", ((FieldFullText) condition).getValue());
    }

    public void testQuery18() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query18.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertEquals(FullText.class, condition.getClass());
        assertEquals("1", ((FullText) condition).getValue());
    }

    public void testQuery19() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query19.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Class<TypedExpression>[] expectedProjections = new Class[] { Timestamp.class, TaskId.class, GroupSize.class,
                StagingError.class, StagingSource.class, StagingStatus.class, StagingBlockKey.class };
        int i = 0;
        for (TypedExpression typedExpression : select.getSelectedFields()) {
            assertEquals(expectedProjections[i++], typedExpression.getClass());
        }
    }

    public void testQuery20() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query20.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        final List<Class<? extends MetadataField>> expectedMetadataFields = new ArrayList<>(
                Arrays.asList(Timestamp.class, TaskId.class, GroupSize.class, StagingError.class, StagingSource.class,
                        StagingStatus.class, StagingBlockKey.class));
        select.getCondition().accept(new VisitorAdapter<Void>() {

            @Override
            public Void visit(Compare condition) {
                condition.getLeft().accept(this);
                return null;
            }

            @Override
            public Void visit(BinaryLogicOperator condition) {
                condition.getLeft().accept(this);
                condition.getRight().accept(this);
                return null;
            }

            @Override
            public Void visit(TaskId taskId) {
                expectedMetadataFields.remove(taskId.getClass());
                return null;
            }

            @Override
            public Void visit(StagingStatus stagingStatus) {
                expectedMetadataFields.remove(stagingStatus.getClass());
                return null;
            }

            @Override
            public Void visit(StagingError stagingError) {
                expectedMetadataFields.remove(stagingError.getClass());
                return null;
            }

            @Override
            public Void visit(StagingSource stagingSource) {
                expectedMetadataFields.remove(stagingSource.getClass());
                return null;
            }

            @Override
            public Void visit(StagingBlockKey stagingBlockKey) {
                expectedMetadataFields.remove(stagingBlockKey.getClass());
                return null;
            }

            @Override
            public Void visit(GroupSize groupSize) {
                expectedMetadataFields.remove(groupSize.getClass());
                return null;
            }

            @Override
            public Void visit(Timestamp timestamp) {
                expectedMetadataFields.remove(timestamp.getClass());
                return null;
            }
        });
        assertEquals(0, expectedMetadataFields.size());
    }

    public void testQuery21() throws Exception {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query21.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertTrue(select.cache());
    }

    public void testQuery22() throws Exception {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query22.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertEquals(Compare.class, condition.getClass());
        assertEquals(Field.class, ((Compare) condition).getLeft().getClass());
        assertEquals(Field.class, ((Compare) condition).getRight().getClass());
    }

    public void testQuery23() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query23.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertEquals(1, select.getSelectedFields().size());
        TypedExpression typedExpression = select.getSelectedFields().get(0);
        assertEquals(Alias.class, typedExpression.getClass());
        assertEquals("id", ((Alias) typedExpression).getAliasName());
        TypedExpression aliasedExpression = ((Alias) typedExpression).getTypedExpression();
        assertEquals(Distinct.class, aliasedExpression.getClass());
        assertEquals(Field.class, ((Distinct) aliasedExpression).getExpression().getClass());
    }

    public void testQuery24() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query24.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertEquals(1, select.getSelectedFields().size());
        TypedExpression typedExpression = select.getSelectedFields().get(0);
        assertEquals(Alias.class, typedExpression.getClass());
        assertEquals("taskId", ((Alias) typedExpression).getAliasName());
        TypedExpression aliasedExpression = ((Alias) typedExpression).getTypedExpression();
        assertEquals(Distinct.class, aliasedExpression.getClass());
        assertEquals(TaskId.class, ((Distinct) aliasedExpression).getExpression().getClass());
    }

    public void testQuery25() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query25.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertEquals(1, select.getOrderBy().size());
        OrderBy orderBy = select.getOrderBy().get(0);
        assertEquals(Count.class, orderBy.getExpression().getClass());
        assertEquals(Field.class, ((Count) orderBy.getExpression()).getExpression().getClass());
    }

    public void testQuery26() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query26.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        assertEquals(1, select.getSelectedFields().size());
        TypedExpression selectedField = select.getSelectedFields().get(0);
        assertEquals(Alias.class, selectedField.getClass());
        assertEquals(Count.class, ((Alias) selectedField).getTypedExpression().getClass());
    }

    public void testQuery27() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query27.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        Condition condition = select.getCondition();
        assertNotNull(condition);
        assertEquals(Compare.class, condition.getClass());
        assertEquals(Timestamp.class, ((Compare) condition).getLeft().getClass());
        assertEquals(LongConstant.class, ((Compare) condition).getRight().getClass());
    }

    public void testQuery28() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query28.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        At condition = select.getHistory();
        assertNotNull(condition);
        assertEquals(At.Swing.BEFORE, condition.getSwing());
        long expected = System.currentTimeMillis() - (24 * 60 * 60 * 1000);
        assertTrue(expected - condition.getDateTime() < 1000); // Assumes it took less than a second to parse query.
    }

    public void testQuery29() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query29.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        At condition = select.getHistory();
        assertNotNull(condition);
        assertEquals(At.Swing.BEFORE, condition.getSwing());
        long expected = System.currentTimeMillis();
        assertTrue(expected - condition.getDateTime() < 1000); // Assumes it took less than a second to parse query.
    }

    public void testQuery30() {
        QueryParser parser = QueryParser.newParser(repository);
        Expression expression = parser.parse(QueryParserTest.class.getResourceAsStream("query30.json")); //$NON-NLS-1$
        assertTrue(expression instanceof Select);
        Select select = (Select) expression;
        At condition = select.getHistory();
        assertNotNull(condition);
        assertEquals(At.Swing.AFTER, condition.getSwing());
        assertEquals(0, condition.getDateTime());
    }

}
