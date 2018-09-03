/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.amalto.core.storage.datasource.RDBMSDataSource.DataSourceDialect;


public class HibernateStorageUtilsTest {

    @Test
    public void testConvertedDefaultValue() {
        assertEquals("true", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.H2, "\"true\"", "'"));
        assertEquals("true", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.POSTGRES, "\"true\"", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.SQL_SERVER, "\"true\"", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.MYSQL, "\"true\"", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.ORACLE_10G, "\"true\"", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.DB2, "\"true\"", "'"));

        assertEquals("true", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.H2, "fn:true()", "'"));
        assertEquals("true", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.POSTGRES, "fn:true()", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.SQL_SERVER, "fn:true()", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.MYSQL, "fn:true()", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.ORACLE_10G, "fn:true()", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.DB2, "fn:true()", "'"));

        assertEquals("true", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.H2, "'true'", "'"));
        assertEquals("true", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.POSTGRES, "'true'", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.SQL_SERVER, "'true'", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.MYSQL, "'true'", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.ORACLE_10G, "'true'", "'"));
        assertEquals("1", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.DB2, "'true'", "'"));

        assertEquals("false", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.H2, "\"false\"", "'"));
        assertEquals("false", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.POSTGRES, "\"false\"", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.SQL_SERVER, "\"false\"", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.MYSQL, "\"false\"", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.ORACLE_10G, "\"false\"", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.DB2, "\"false\"", "'"));

        assertEquals("false", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.H2, "'false'", "'"));
        assertEquals("false", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.POSTGRES, "'false'", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.SQL_SERVER, "'false'", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.MYSQL, "'false'", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.ORACLE_10G, "'false'", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.DB2, "'false'", "'"));

        assertEquals("false", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.H2, "fn:false()", "'"));
        assertEquals("false", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.POSTGRES, "fn:false()", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.SQL_SERVER, "fn:false()", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.MYSQL, "fn:false()", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.ORACLE_10G, "fn:false()", "'"));
        assertEquals("0", HibernateStorageUtils.convertedDefaultValue("boolean", DataSourceDialect.DB2, "fn:false()", "'"));

        assertEquals("'[En:*]'", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.H2, "\"[En:*]\"", "'"));
        assertEquals("'[En:*]'", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.POSTGRES, "\"[En:*]\"", "'"));
        assertEquals("'[En:*]'", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.SQL_SERVER, "\"[En:*]\"", "'"));
        assertEquals("'[En:*]'", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.MYSQL, "\"[En:*]\"", "'"));
        assertEquals("'[En:*]'", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.ORACLE_10G, "\"[En:*]\"", "'"));
        assertEquals("'[En:*]'", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.DB2, "\"[En:*]\"", "'"));

        assertEquals("100", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.H2, "100", "'"));
        assertEquals("100", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.POSTGRES, "100", "'"));
        assertEquals("100", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.SQL_SERVER, "100", "'"));
        assertEquals("100", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.MYSQL, "100", "'"));
        assertEquals("100", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.ORACLE_10G, "100", "'"));
        assertEquals("100", HibernateStorageUtils.convertedDefaultValue("string", DataSourceDialect.DB2, "100", "'"));
    }

    @Test
    public void testIsBooleanDefaultValue() throws Exception {
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("boolean", "\"true\""));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("boolean", "\"false\""));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("boolean", "\"true\""));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("boolean", "\"false\""));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("boolean", "fn:false()"));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("boolean", "fn:true()"));
        assertFalse(HibernateStorageUtils.isBooleanDefaultValue("boolean", "1"));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("boolean", "'true'"));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("boolean", "'false'"));

        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("\"true\""));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("\"false\""));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("\"true\""));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("\"false\""));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("fn:false()"));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("fn:true()"));
        assertFalse(HibernateStorageUtils.isBooleanDefaultValue("1"));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("'true'"));
        assertTrue(HibernateStorageUtils.isBooleanDefaultValue("'false'"));
    }
}
