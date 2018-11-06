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

import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.Select;
import com.amalto.core.query.user.UserQueryBuilder;
import junit.framework.TestCase;
import org.talend.mdm.QueryParserTest;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import static com.amalto.core.query.user.UserQueryBuilder.and;
import static com.amalto.core.query.user.UserQueryBuilder.eq;

/**
 * Goal of unit test is pretty simple:
 * <ul>
 *     <li>Create a {@link Expression}</li>
 *     <li>Serialize it to JSON</li>
 *     <li>Parse JSON using {@link QueryParser}</li>
 *     <li>Assert {@link Expression} parsed from JSON is equals to initial</li>
 * </ul>
 */
public class UserQueryJsonSerializerTest extends TestCase {

    private MetadataRepository repository;

    public void setUp() throws Exception {
        super.setUp();
        repository = new MetadataRepository();
        repository.load(QueryParserTest.class.getResourceAsStream("metadata.xsd"));
    }

    public void testUserQueryEquals() {
        // given
        final ComplexTypeMetadata type1 = repository.getComplexType("Type1");
        final Select select = UserQueryBuilder.from(type1) //
                .where(eq(type1.getField("id"), "1"))
                .cache() //
                .getSelect();

        // when, then
        assertRoundTrip(select);
    }

    public void testUserQueryAnd() {
        // given
        final ComplexTypeMetadata type1 = repository.getComplexType("Type1");
        final Select select = UserQueryBuilder.from(type1) //
                .where(and(eq(type1.getField("id"), "1"), eq(type1.getField("id"), "2")))
                .getSelect();

        // when, then
        assertRoundTrip(select);
    }

    public void testUserQueryJoin() {
        // given
        final ComplexTypeMetadata type1 = repository.getComplexType("Type1");
        final Select select = UserQueryBuilder.from(type1) //
                .where(eq(type1.getField("id"), "1"))
                .join(type1.getField("fk2"))
                .getSelect();

        // when, then
        assertRoundTrip(select);
    }

    private void assertRoundTrip(Select select) {
        // when
        final String jsonAsString = UserQueryJsonSerializer.toJson(select);

        // then
        final Expression roundTripQuery = QueryParser.newParser(repository).parse(jsonAsString);
        assertEquals(select, roundTripQuery);
    }
}