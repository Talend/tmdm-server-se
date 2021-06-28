/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
// ============================================================================
//
// Copyright (C) 2006-2019 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage.hibernate;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.type.Type;
import org.mockito.Mockito;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeMetadata;
import org.talend.mdm.commmon.metadata.Types;

import com.amalto.core.query.user.Predicate;
import com.amalto.core.storage.datasource.RDBMSDataSource;

public class ManyFieldCriterionTest extends TestCase {

    public void testToSqlString() {
        RDBMSDataSource dataSource = new RDBMSDataSource(
                "", "MySQL", "", "", "", 0, 0, "", "", null, "create", false, null, "", "", null, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$
                "", "", "", false); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Criteria criteria = new CriteriaImpl(getName(), null);
        TableResolver tableResolver = new TableResolverForTest();
        CriteriaQuery criteriaQuery = new CriteriaQueryForTest();
        FieldMetadata field = new ReferenceFieldMetadata(
                null,
                false,
                false,
                false,
                "Song", null, new SimpleTypeFieldMetadata(null, false, false, false, "Song", new SimpleTypeMetadata("", "x_songname"), null, null, null, ""), null, "", false, false, null, null, //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
                null, null, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
        ManyFieldCriterion cirCriterion = new ManyFieldCriterion(dataSource, criteria, tableResolver, field,
                "Hell Ain't A Bad Place To Be",Predicate.EQUALS, Types.STRING, -1); //$NON-NLS-1$
        assertEquals("(a1.x_id = Hell Ain\\'t A Bad Place To Be)", cirCriterion.toSqlString(criteria, criteriaQuery)); //$NON-NLS-1$

        List<String> valueList = new ArrayList<>();
        valueList.add("shirt");
        valueList.add("talend");
        cirCriterion = new ManyFieldCriterion(dataSource, criteria, tableResolver, field, valueList, Predicate.IN, Types.STRING);
        try {
            cirCriterion.toSqlString(criteria, criteriaQuery); // $NON-NLS-1$
            fail();
        } catch (Exception e) {
            assertNotNull(e);
        }

        ComplexTypeMetadata complexType = Mockito.mock(ComplexTypeMetadata.class);
        List<FieldMetadata> keyList = new ArrayList<>();
        keyList.add(new SimpleTypeFieldMetadata(complexType, false, false, false, "Song",
                new SimpleTypeMetadata("", "x_songname"), null, null, ""));
        Mockito.when(complexType.getKeyFields()).thenReturn(keyList);

        field = new SimpleTypeFieldMetadata(complexType, false, false, false, "Song", new SimpleTypeMetadata("", "x_songname"), //$NON-NLS-2$
                null, null, "");
        cirCriterion = new ManyFieldCriterion(dataSource, criteria, tableResolver, field, valueList, Predicate.IN, Types.STRING);
        assertEquals(
                "(SELECT COUNT(1) FROM Product INNER JOIN product_collection ON Product.x_id = product_collection.x_id WHERE product_collection.value in('shirt','talend')  AND Product.x_id = a1.x_id) > 0", //$NON-NLS-1$
                cirCriterion.toSqlString(criteria, criteriaQuery));
    }

    public void testToSqlStringWithDate() {
        RDBMSDataSource dataSource = new RDBMSDataSource("", "Oracle10g", "", "", "", 0, 0, "", "", null, "create", false, null, "", "", null, "", "", "", false);
        Criteria criteria = new CriteriaImpl(getName(), null);
        TableResolver tableResolver = new TableResolverForTest();
        CriteriaQuery criteriaQuery = new CriteriaQueryForTest();
        FieldMetadata field = new ReferenceFieldMetadata(
                null, true, false, true, "x_birthday", null, new SimpleTypeFieldMetadata(null, false, false, false, "Song",
                        new SimpleTypeMetadata("", "date"), null, null, ""),
                null, "", false, false, null, null, null, "", "");

        ComplexTypeMetadata complexType = Mockito.mock(ComplexTypeMetadata.class);
        List<FieldMetadata> keyList = new ArrayList<>();
        keyList.add(new SimpleTypeFieldMetadata(complexType, true, false, true, "x_birthday", new SimpleTypeMetadata("", "date"), null, null, ""));
        Mockito.when(complexType.getKeyFields()).thenReturn(keyList);

        ManyFieldCriterion cirCriterion = new ManyFieldCriterion(dataSource, criteria, tableResolver, field, "2019-11-25 00:00:00.000", Predicate.EQUALS, Types.DATE);
        assertEquals("(a1.x_id = timestamp '2019-11-25 00:00:00.000')", cirCriterion.toSqlString(criteria, criteriaQuery));
    }

    private class TableResolverForTest implements TableResolver {

        @Override
        public String get(ComplexTypeMetadata type) {
            return "Product";
        }

        @Override
        public String get(FieldMetadata field) {
            return "x_id";
        }

        @Override
        public String get(FieldMetadata field, String prefix) {
            return null;
        }

        @Override
        public boolean isIndexed(FieldMetadata field) {
            return false;
        }

        @Override
        public String getIndex(String fieldName, String prefix) {
            return null;
        }

        @Override
        public String getCollectionTable(FieldMetadata field) {
            return "product_collection";
        }

        @Override
        public String getFkConstraintName(ReferenceFieldMetadata referenceField) {
            return null;
        }

        @Override
        public String get(String name) {
            return null;
        }

		@Override
		public String getCollectionTableToDrop(FieldMetadata field) {
			return "product_collection";
		}
    }

    private class CriteriaQueryForTest implements CriteriaQuery {

        @Override
        public SessionFactoryImplementor getFactory() {
            return null;
        }

        @Override
        public String getColumn(Criteria criteria, String propertyPath) throws HibernateException {
            return null;
        }

        @Override
        public String[] getColumns(String propertyPath, Criteria criteria) throws HibernateException {
            return null;
        }

        @Override
        public String[] findColumns(String propertyPath, Criteria criteria) throws HibernateException {
            return null;
        }

        @Override
        public Type getType(Criteria criteria, String propertyPath) throws HibernateException {
            return null;
        }

        @Override
        public String[] getColumnsUsingProjection(Criteria criteria, String propertyPath) throws HibernateException {
            return null;
        }

        @Override
        public Type getTypeUsingProjection(Criteria criteria, String propertyPath) throws HibernateException {
            return null;
        }

        @Override
        public TypedValue getTypedValue(Criteria criteria, String propertyPath, Object value) throws HibernateException {
            return null;
        }

        @Override
        public String getEntityName(Criteria criteria) {
            return null;
        }

        @Override
        public String getEntityName(Criteria criteria, String propertyPath) {
            return null;
        }

        @Override
        public String getSQLAlias(Criteria criteria) {
            return "a1";
        }

        @Override
        public String getSQLAlias(Criteria criteria, String propertyPath) {
            return null;
        }

        @Override
        public String getPropertyName(String propertyName) {
            return null;
        }

        @Override
        public String[] getIdentifierColumns(Criteria criteria) {
            return null;
        }

        @Override
        public Type getIdentifierType(Criteria criteria) {
            return null;
        }

        @Override
        public TypedValue getTypedIdentifierValue(Criteria criteria, Object value) {
            return null;
        }

        @Override
        public String generateSQLAlias() {
            return null;
        }
    }
}
