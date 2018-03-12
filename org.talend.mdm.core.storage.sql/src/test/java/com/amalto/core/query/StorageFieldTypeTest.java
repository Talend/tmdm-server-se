/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.query;

import static com.amalto.core.query.user.UserQueryBuilder.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.query.user.UserQueryHelper;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.xmlserver.interfaces.IWhereItem;
import com.amalto.xmlserver.interfaces.WhereAnd;
import com.amalto.xmlserver.interfaces.WhereCondition;

@SuppressWarnings("nls")
public class StorageFieldTypeTest extends StorageTestCase {

    enum Type {
        NUMERIC,
        BOOLEAN,
        STRING,
        DURATION,
        BINARY,
        DATE,
        TIME,
        NEGATIVE_NUMERIC,
        DATETIME
    }

    private void populateData() {
        assertNotNull(type);

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        InputStream testData = this.getClass().getResourceAsStream("StorageFieldTypeTest_1.xml");
        assertNotNull(testData);

        BufferedReader br = new BufferedReader(new InputStreamReader(testData));
        StringBuilder builder = new StringBuilder();
        String currentLine;
        try {
            while ((currentLine = br.readLine()) != null) {
                builder.append(currentLine);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<DataRecord> allRecords = new LinkedList<DataRecord>();
        allRecords.add(factory.read(repository, type, builder.toString()));

        try {
            storage.begin();
            storage.update(allRecords);
            storage.commit();
        } finally {
            storage.end();
        }
    }

    @Override
    public void setUp() throws Exception {
        populateData();
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        storage.begin();
        {
            UserQueryBuilder qb = from(type);
            storage.delete(qb.getSelect());
        }
        storage.commit();
        storage.end();
    }

    public void testString() throws Exception {
        testSimpleQuery("string");
        testSimpleSearch("string", Type.STRING);
        testMDMSearch("string", Type.STRING);
    }

    public void testBoolean() throws Exception {
        testSimpleQuery("boolean");
        testSimpleSearch("boolean", Type.BOOLEAN);
        testMDMSearch("boolean", Type.BOOLEAN);
    }

    public void testFloat() throws Exception {
        testSimpleQuery("float");
        testSimpleSearch("float", Type.NUMERIC);
        testMDMSearch("float", Type.NUMERIC);
    }

    public void testDouble() throws Exception {
        testSimpleQuery("double");
        testSimpleSearch("double", Type.NUMERIC);
        testMDMSearch("double", Type.NUMERIC);
    }

    public void testDecimal() throws Exception {
        testSimpleQuery("decimal");
        testSimpleSearch("decimal", Type.NUMERIC);
        testMDMSearch("decimal", Type.NUMERIC);
    }

    public void testDuration() throws Exception {
        testSimpleQuery("duration");
        testSimpleSearch("duration", Type.DURATION);
        testMDMSearch("duration", Type.DURATION);
    }

    public void testDateTime() throws Exception {
        testSimpleQuery("dateTime");
        testSimpleSearch("dateTime", Type.DATETIME);
        testMDMSearch("dateTime", Type.DATETIME);
    }

    public void testTime() throws Exception {
        testSimpleQuery("time");
        testSimpleSearch("time", Type.TIME);
        testMDMSearch("time", Type.TIME);
    }

    public void testDate() throws Exception {
        testSimpleQuery("date");
        testSimpleSearch("date", Type.DATE);
        testMDMSearch("date", Type.DATE);
    }

    public void testHexBinary() throws Exception {
        testSimpleQuery("hexBinary");
        testSimpleSearch("hexBinary", Type.BINARY);
        testMDMSearch("hexBinary", Type.BINARY);
    }

    public void testBase64() throws Exception {
        testSimpleQuery("base64Binary");
        testSimpleSearch("base64Binary", Type.BINARY);
        testMDMSearch("base64Binary", Type.BINARY);
    }

    public void testAnyURI() throws Exception {
        testSimpleQuery("anyURI");
        testSimpleSearch("anyURI", Type.STRING);
        testMDMSearch("anyURI", Type.STRING);
    }

    public void testQName() throws Exception {
        testSimpleQuery("qname");
        testSimpleSearch("qname", Type.STRING);
        testMDMSearch("qname", Type.STRING);
    }

    public void testInteger() throws Exception {
        testSimpleQuery("integer");
        testSimpleSearch("integer", Type.NUMERIC);
        testMDMSearch("integer", Type.NUMERIC);
    }

    public void testNonPositiveInteger() throws Exception {
        testSimpleQuery("nonPositiveInteger");
        testSimpleSearch("nonPositiveInteger", Type.NEGATIVE_NUMERIC);
        testMDMSearch("nonPositiveInteger", Type.NEGATIVE_NUMERIC);
    }

    public void testNegativeInteger() throws Exception {
        testSimpleQuery("negativeInteger");
        testSimpleSearch("negativeInteger", Type.NEGATIVE_NUMERIC);
        testMDMSearch("negativeInteger", Type.NEGATIVE_NUMERIC);
    }

    public void testLong() throws Exception {
        testSimpleQuery("long");
        testSimpleSearch("long", Type.NUMERIC);
        testMDMSearch("long", Type.NUMERIC);
    }

    public void testInt() throws Exception {
        testSimpleQuery("int");
        testSimpleSearch("int", Type.NUMERIC);
        testMDMSearch("int", Type.NUMERIC);
    }

    public void testShort() throws Exception {
        testSimpleQuery("short");
        testSimpleSearch("short", Type.NUMERIC);
        testMDMSearch("short", Type.NUMERIC);
    }

    public void testByte() throws Exception {
        testSimpleQuery("byte");
        testSimpleSearch("byte", Type.NUMERIC);
        testMDMSearch("byte", Type.NUMERIC);
    }

    public void testNonNegativeInteger() throws Exception {
        testSimpleQuery("nonNegativeInteger");
        testSimpleSearch("nonNegativeInteger", Type.NUMERIC);
        testMDMSearch("nonNegativeInteger", Type.NUMERIC);
    }

    public void testUnsignedLong() throws Exception {
        testSimpleQuery("unsignedLong");
        testSimpleSearch("unsignedLong", Type.NUMERIC);
        testMDMSearch("unsignedLong", Type.NUMERIC);
    }

    public void testUnsignedInt() throws Exception {
        testSimpleQuery("unsignedInt");
        testSimpleSearch("unsignedInt", Type.NUMERIC);
        testMDMSearch("unsignedInt", Type.NUMERIC);
    }

    public void testUnsignedShort() throws Exception {
        testSimpleQuery("unsignedShort");
        testSimpleSearch("unsignedShort", Type.NUMERIC);
        testMDMSearch("unsignedShort", Type.NUMERIC);
    }

    public void testUnsignedByte() throws Exception {
        testSimpleQuery("unsignedByte");
        testSimpleSearch("unsignedByte", Type.NUMERIC);
        testMDMSearch("unsignedByte", Type.NUMERIC);
    }

    public void testPositiveInteger() throws Exception {
        testSimpleQuery("positiveInteger");
        testSimpleSearch("positiveInteger", Type.NUMERIC);
        testMDMSearch("positiveInteger", Type.NUMERIC);
    }

    private void testSimpleQuery(String typeName) {
        UserQueryBuilder qb = from(type).select(type.getField(typeName));
        StorageResults results = storage.fetch(qb.getSelect());
        assertNotNull(results);
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    private void testSimpleSearch(String typeName, Type valueType) {
        UserQueryBuilder qb = from(type).select(type.getField(typeName));

        switch (valueType) {
        case NEGATIVE_NUMERIC:
            qb = qb.where(gt(type.getField(typeName), "-50"));
            break;
        case NUMERIC:
            qb = qb.where(gt(type.getField(typeName), "0"));
            break;
        case STRING:
            qb = qb.where(contains(type.getField(typeName), ""));
            break;
        case BOOLEAN:
            qb = qb.where(eq(type.getField(typeName), "true"));
            break;
        case DATE:
            qb = qb.where(gt(type.getField(typeName), "1970-01-01"));
            break;
        case DURATION:
            qb = qb.where(eq(type.getField(typeName), "P5Y2M10D"));
            break;
        case TIME:
            qb = qb.where(gt(type.getField(typeName), "00:00:00"));
            break;
        case BINARY:
            qb = qb.where(contains(type.getField(typeName), ""));
            break;
        case DATETIME:
            qb = qb.where(gt(type.getField(typeName), "1970-01-01T00:00:00"));
            break;
        default:
            throw new NotImplementedException();
        }

        StorageResults results = storage.fetch(qb.getSelect());
        assertNotNull(results);
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

    private void testMDMSearch(String typeName, Type valueType) {
        IWhereItem item;
        UserQueryBuilder qb = UserQueryBuilder.from(type);

        String fieldName = type.getName() + "/" + typeName;
        switch (valueType) {
        case NEGATIVE_NUMERIC:
            item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.GREATER_THAN, "-50",
                    WhereCondition.NO_OPERATOR)));
            break;
        case NUMERIC:
            item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.GREATER_THAN, "0",
                    WhereCondition.NO_OPERATOR)));
            break;
        case STRING:
            item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.CONTAINS, "",
                    WhereCondition.NO_OPERATOR)));
            break;
        case BOOLEAN:
            item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.EQUALS, "TRUE",
                    WhereCondition.NO_OPERATOR)));
            break;
        case DATE:
            item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.GREATER_THAN,
                    "1970-01-01", WhereCondition.NO_OPERATOR)));
            break;
        case DURATION:
            item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.EQUALS, "P5Y2M10D",
                    WhereCondition.NO_OPERATOR)));
            break;
        case TIME:
            item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.GREATER_THAN, "00:00:00",
                    WhereCondition.NO_OPERATOR)));
            break;
        case BINARY:
            item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.CONTAINS, "",
                    WhereCondition.NO_OPERATOR)));
            break;
        case DATETIME:
            item = new WhereAnd(Arrays.<IWhereItem> asList(new WhereCondition(fieldName, WhereCondition.GREATER_THAN,
                    "1970-01-01T00:00:00", WhereCondition.NO_OPERATOR)));
            break;
        default:
            throw new NotImplementedException();
        }
        qb = qb.where(UserQueryHelper.buildCondition(qb, item, repository));

        StorageResults results = storage.fetch(qb.getSelect());
        assertNotNull(results);
        try {
            assertEquals(1, results.getCount());
        } finally {
            results.close();
        }
    }

}
