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

package com.amalto.core.query;


import com.amalto.core.query.optimization.RecommendedIndexes;
import com.amalto.core.query.user.*;
import org.talend.mdm.commmon.metadata.*;

import java.util.*;

import static com.amalto.core.query.user.UserQueryBuilder.and;
import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

public class IndexDetectionTestCase extends StorageTestCase {

    public void testSimpleQuery() throws Exception {
        UserQueryBuilder qb = from(product).where(eq(product.getField("Id"), "1"));
        Collection<FieldMetadata> indexRecommendation = RecommendedIndexes.get(qb.getExpression());
        assertEquals(1, indexRecommendation.size());
        assertEquals(product.getField("Id"), indexRecommendation.iterator().next());
    }

    public void testJoinQuery() throws Exception {
        UserQueryBuilder qb = from(product)
                .where(eq(product.getField("Id"), "1"))
                .join(product.getField("Family"));
        Collection<FieldMetadata> indexRecommendation = RecommendedIndexes.get(qb.getExpression());
        assertEquals(3, indexRecommendation.size());
        assertTrue(indexRecommendation.contains(product.getField("Id")));
        assertTrue(indexRecommendation.contains(product.getField("Family")));
        assertTrue(indexRecommendation.contains(productFamily.getField("Id")));
    }

    public void testJoinQueryWithCondition() throws Exception {
        UserQueryBuilder qb = from(product)
                .where(and(eq(product.getField("Id"), "1"), eq(productFamily.getField("Name"), "a name")))
                .join(product.getField("Family"));
        Collection<FieldMetadata> indexRecommendation = RecommendedIndexes.get(qb.getExpression());
        assertEquals(4, indexRecommendation.size());
        assertTrue(indexRecommendation.contains(product.getField("Id")));
        assertTrue(indexRecommendation.contains(product.getField("Family")));
        assertTrue(indexRecommendation.contains(productFamily.getField("Id")));
        assertTrue(indexRecommendation.contains(productFamily.getField("Name")));
    }

    public void testOrderByQuery() throws Exception {
        UserQueryBuilder qb = from(product)
                .where(eq(product.getField("Id"), "1"))
                .orderBy(product.getField("Name"), OrderBy.Direction.ASC);
        Collection<FieldMetadata> indexRecommendation = RecommendedIndexes.get(qb.getExpression());
        assertEquals(2, indexRecommendation.size());
        assertTrue(indexRecommendation.contains(product.getField("Id")));
        assertTrue(indexRecommendation.contains(product.getField("Name")));
    }

    public void testOrderByQueryIncludingDuplicates() throws Exception {
        UserQueryBuilder qb = from(product)
                .where(eq(product.getField("Id"), "1"))
                .orderBy(product.getField("Id"), OrderBy.Direction.ASC);
        Collection<FieldMetadata> indexRecommendation = RecommendedIndexes.get(qb.getExpression());
        assertEquals(1, indexRecommendation.size());
        assertTrue(indexRecommendation.contains(product.getField("Id")));
    }

}
