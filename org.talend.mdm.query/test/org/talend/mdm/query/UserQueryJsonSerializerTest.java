/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.query;

import com.amalto.core.query.user.BinaryLogicOperator;
import com.amalto.core.query.user.Compare;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.Field;
import com.amalto.core.query.user.IsNull;
import com.amalto.core.query.user.Predicate;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.StringConstant;
import com.amalto.core.query.user.TypedExpression;
import com.amalto.core.query.user.UnaryLogicOperator;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.query.user.UserQueryHelper;
import com.amalto.xmlserver.interfaces.WhereCondition;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import junit.framework.TestCase;
import org.talend.mdm.QueryParserTest;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import java.util.Arrays;
import java.util.List;

import static com.amalto.core.query.user.UserQueryBuilder.and;
import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.in;

/**
 * Goal of unit test is pretty simple:
 * <ul>
 * <li>Create a {@link Expression}</li>
 * <li>Serialize it to JSON</li>
 * <li>Parse JSON using {@link QueryParser}</li>
 * <li>Assert {@link Expression} parsed from JSON is equals to initial</li>
 * </ul>
 */
public class UserQueryJsonSerializerTest extends TestCase {

    private MetadataRepository repository;

    public void setUp() throws Exception {
        super.setUp();
        this.repository = new MetadataRepository();
        this.repository.load(QueryParserTest.class.getResourceAsStream("metadata.xsd"));
    }

    public void testUserQueryEquals() {
        // given
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final Select select = UserQueryBuilder.from(type1) //
                .where(eq(type1.getField("id"), "1"))
                .cache() //
                .getSelect();

        // when, then
        assertRoundTrip(select);
    }

    public void testUserQueryAnd() {
        // given
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final Select select = UserQueryBuilder.from(type1) //
                .where(and(eq(type1.getField("id"), "1"), eq(type1.getField("id"), "2")))
                .getSelect();

        // when, then
        assertRoundTrip(select);
    }

    public void testUserQueryJoin() {
        // given
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final Select select = UserQueryBuilder.from(type1) //
                .where(eq(type1.getField("id"), "1"))
                .join(type1.getField("fk2"))
                .getSelect();

        // when, then
        assertRoundTrip(select);
    }

    public void testUserQueryIn() {
        List<String> ids = Arrays.asList("1", "2");
        // given
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final Select select = UserQueryBuilder.from(type1) //
                .where(in(type1.getField("id"), ids))
                .getSelect();

        // when, then
        assertRoundTrip(select);
    }

    public void testBuildConditionJoins() {
        // given
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        UserQueryBuilder uq = UserQueryBuilder.from(type1);
        uq.where(UserQueryHelper.buildCondition(uq, new WhereCondition("Type1/fk2", "JOINS", "Type2/id", "NONE"), repository));

        // when, then
        assertRoundTrip(uq.getSelect());
    }

    public void testStringConstantOnAlias() {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse("{\"alias\":[{\"name\":\"Sizes\"},{\"value\":\"\"}]}").getAsJsonObject();
        assertNotNull(jsonObject);
        TypedExpressionProcessor typedExpressionProcessor = Deserializer.getTypedExpression(jsonObject);
        TypedExpression typedExpression = typedExpressionProcessor.process(jsonObject, this.repository);
        assertNotNull(typedExpression);
    }

    public void testConditionEqualsTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("Type1.value1 = 10");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertEquals(select.getTypes().get(0), type1);
        assertEquals(select.getCondition(), new Compare(new Field(type1.getField("value1")), Predicate.EQUALS, new StringConstant("10")));
        assertRoundTrip(select);
    }

    public void testConditionEqualsFieldTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("Type1.value1 = field(Type1.fk2)");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertEquals(select.getTypes().get(0), type1);
        assertRoundTrip(select);
    }

    public void testConditionContainsTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("Type1.value1 contains 'Toto'");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertEquals(select.getTypes().get(0), type1);
        assertEquals(select.getCondition(), new Compare(new Field(type1.getField("value1")), Predicate.CONTAINS, new StringConstant("Toto")));
        assertRoundTrip(select);
    }

    public void testConditionOrContainsTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("(Type1.value1 contains 'Toto') or (Type1.value1 contains 'Titi') or (Type1.value1 contains 'Tutu')");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertEquals(select.getTypes().get(0), type1);
        assertRoundTrip(select);
    }

    public void testConditionContainsForeignKeyInfoTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("Type1.fk2 contains 'k'");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertTrue(select.getCondition() instanceof BinaryLogicOperator);
        assertEquals(((BinaryLogicOperator)select.getCondition()).getPredicate(), Predicate.OR);
        assertRoundTrip(select);
    }

    public void testConditionEqualForeignKeyInfoTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("Type1.fk2 = 'k'");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertTrue(select.getCondition() instanceof BinaryLogicOperator);
        assertEquals(((BinaryLogicOperator)select.getCondition()).getPredicate(), Predicate.OR);
        assertRoundTrip(select);
    }

    public void testConditionNotEqualForeignKeyInfoTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("not(Type1.fk2 = 'k')");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertTrue(select.getCondition() instanceof UnaryLogicOperator);
        assertEquals(((UnaryLogicOperator)select.getCondition()).getPredicate(), Predicate.NOT);
        assertRoundTrip(select);
    }

    public void testConditionIsEmptyTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("Type1.value1 is empty");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertEquals(select.getTypes().get(0), type1);
        assertTrue(select.getCondition() instanceof IsNull);
        assertRoundTrip(select);
    }

    public void testConditionInTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("Type1.value1 in ['value1', 'value2']");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertEquals(select.getTypes().get(0), type1);
        assertEquals(select.getCondition(), new Compare(new Field(type1.getField("value1")), Predicate.IN, UserQueryBuilder.createConstant(new Field(type1.getField("value1")), Arrays.asList("value1", "value2"))));
        assertRoundTrip(select);
    }

    public void testAndInTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("Type1.value1 = 'value1' and Type1.value1 = 'value2'");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertTrue(select.getCondition() instanceof BinaryLogicOperator);
        assertEquals(((BinaryLogicOperator)select.getCondition()).getPredicate(), Predicate.AND);
        assertRoundTrip(select);
    }

    public void testOrInTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("Type1.value1 = 'value1' or Type1.value1 = 'value2'");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertTrue(select.getCondition() instanceof BinaryLogicOperator);
        assertEquals(((BinaryLogicOperator)select.getCondition()).getPredicate(), Predicate.OR);
        assertRoundTrip(select);
    }

    public void testNotInTQL() {
        final ComplexTypeMetadata type1 = this.repository.getComplexType("Type1");
        final UserQueryBuilder userQueryBuilder = UserQueryBuilder.from(type1).where("Type1.value1 = 'value1' and not(Type1.id = 'value2')");
        final Select select = userQueryBuilder.getSelect();
        assertNotNull(select.getCondition());
        assertTrue(select.getCondition() instanceof BinaryLogicOperator);
        assertEquals(((BinaryLogicOperator)select.getCondition()).getPredicate(), Predicate.AND);
        assertTrue(((BinaryLogicOperator)select.getCondition()).getRight() instanceof UnaryLogicOperator);
        assertRoundTrip(select);
    }

    private void assertRoundTrip(Select select) {
        // when
        final String jsonAsString = UserQueryJsonSerializer.toJson(select);

        // then
        final Expression roundTripQuery = QueryParser.newParser(this.repository).parse(jsonAsString);
        assertEquals(select, roundTripQuery);
    }
}