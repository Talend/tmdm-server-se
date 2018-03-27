// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================

package com.amalto.core.storage.record;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import com.amalto.core.load.io.ResettableStringWriter;

public class DataRecordIncludeNullValueXmlWriterTestCase extends DataRecordDataWriterTestCase{

    private DataRecordIncludeNullValueXmlWriter writer;

    @Before
    public void setup() throws Exception {
        super.setup();
        writer = new DataRecordIncludeNullValueXmlWriter();
        writer.setSecurityDelegator(delegate);
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));
    }

    @Test
    public void testSimpleComplexType() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("SimpleProduct"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", "Name");
        setDataRecordField(record, "Description", "Desc");
        setDataRecordField(record, "Availability", Boolean.FALSE);
        String result = toXmlString(record);
        Assert.assertEquals(
                "<SimpleProduct><Id>12345</Id><Name>Name</Name><Description>Desc</Description><Availability>false</Availability></SimpleProduct>",
                result);
    }

    @Test
    public void testSimpleComplexTypeWithNullValue() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("SimpleProduct"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", null);
        setDataRecordField(record, "Description", null);
        setDataRecordField(record, "Availability", Boolean.FALSE);
        String result = toXmlString(record);
        Assert.assertEquals(
                "<SimpleProduct><Id>12345</Id><Name></Name><Description></Description><Availability>false</Availability></SimpleProduct>",
                result);
    }

    @Test
    public void testSimpleTypeWithArray() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithArray"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Repeat", "ABC");
        setDataRecordField(record, "Repeat", "DEF");
        String result = toXmlString(record);
        Assert.assertEquals("<WithArray><Id>12345</Id><Repeat>ABC</Repeat><Repeat>DEF</Repeat></WithArray>", result);
    }

    @Test
    public void testSimpleTypeWithNullArray() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithArray"));
        setDataRecordField(record, "Id", "12345");
        String result = toXmlString(record);
        Assert.assertEquals("<WithArray><Id>12345</Id><Repeat></Repeat></WithArray>", result);
    }

    @Test
    public void testSimpleTypeWithEmptyArray() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithArray"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Repeat", new ArrayList<String>());
        String result = toXmlString(record);
        Assert.assertEquals("<WithArray><Id>12345</Id><Repeat></Repeat></WithArray>", result);
    }

    @Test
    public void testTypeWithComplexType() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("Customer"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", "Name");
        String result = toXmlString(record);
        Assert.assertEquals("<Customer><Id>12345</Id><Name>Name</Name><Address></Address></Customer>", result);
    }

    @Test
    public void testTypeWithComplexTypeOfReference() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("Customer");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", "Name");

        FieldMetadata field = type.getField("Address");

        DataRecord referenced = createDataRecord(((ContainedComplexTypeMetadata) field.getType()).getContainedType());
        setDataRecordField(referenced, "Id", "AddressId");
        setDataRecordField(referenced, "Street", "AddressStreet");
        setDataRecordField(referenced, "City", "AddressCity");

        setDataRecordField(record, "Address", referenced);

        String result = toXmlString(record);
        Assert.assertEquals(
                "<Customer><Id>12345</Id><Name>Name</Name><Address><Id>AddressId</Id><Street>AddressStreet</Street><City>AddressCity</City></Address></Customer>",
                result);
    }

    @Test
    public void testTypeWithComplexTypeOfReferenceNullValue() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("Customer");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", "Name");

        FieldMetadata field = type.getField("Address");

        DataRecord referenced = createDataRecord(((ContainedComplexTypeMetadata) field.getType()).getContainedType());
        setDataRecordField(referenced, "Id", "AddressId");
        setDataRecordField(referenced, "Street", "AddressStreet");
        setDataRecordField(referenced, "City", "AddressCity");

        setDataRecordField(record, "Address", null);

        String result = toXmlString(record);
        Assert.assertEquals("<Customer><Id>12345</Id><Name>Name</Name><Address></Address></Customer>", result);
    }

    @Test
    public void testContainedType() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");

        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata) field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata) containedField.getType();

        DataRecord contained = createDataRecord(tm);
        setDataRecordField(contained, "ContainedId", "CID");
        setDataRecordField(contained, "ContainedName", "CName");

        setDataRecordField(record, "Contained", contained);

        String result = toXmlString(record);

        Assert.assertEquals(
                "<WithContained><Id>ABCD</Id><Contained><ContainedId>CID</ContainedId><ContainedName>CName</ContainedName></Contained></WithContained>",
                result);
    }

    @Test
    public void testMultiContainedType() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithMultiContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");
        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata) field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata) containedField.getType();
        List<DataRecord> list = new ArrayList<DataRecord>();
        DataRecord contained1 = createDataRecord(tm);
        setDataRecordField(contained1, "ContainedId", "CID1");
        setDataRecordField(contained1, "ContainedName", "CName1");
        list.add(contained1);

        DataRecord contained2 = createDataRecord(tm);
        setDataRecordField(contained2, "ContainedId", "CID2");
        setDataRecordField(contained2, "ContainedName", "CName2");
        list.add(contained2);

        setDataRecordField(record, "Contained", list);

        String result = toXmlString(record);

        Assert.assertEquals(
                "<WithMultiContained><Id>ABCD</Id><Contained><ContainedId>CID1</ContainedId><ContainedName>CName1</ContainedName></Contained><Contained><ContainedId>CID2</ContainedId><ContainedName>CName2</ContainedName></Contained></WithMultiContained>",
                result);
    }

    @Test
    public void testMultiContainedTypeWithNullValue() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithMultiContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");
        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata) field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata) containedField.getType();
        List<DataRecord> list = new ArrayList<DataRecord>();
        DataRecord contained1 = createDataRecord(tm);
        setDataRecordField(contained1, "ContainedId", "CID1");
        setDataRecordField(contained1, "ContainedName", null);
        list.add(contained1);

        DataRecord contained2 = createDataRecord(tm);
        setDataRecordField(contained2, "ContainedId", "CID2");
        setDataRecordField(contained2, "ContainedName", "CName2");
        list.add(contained2);

        setDataRecordField(record, "Contained", list);

        String result = toXmlString(record);

        Assert.assertEquals(
                "<WithMultiContained><Id>ABCD</Id><Contained><ContainedId>CID1</ContainedId><ContainedName></ContainedName></Contained><Contained><ContainedId>CID2</ContainedId><ContainedName>CName2</ContainedName></Contained></WithMultiContained>",
                result);
    }

    @Test
    public void testMultiContainedTypeWithOneDataRecord() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithMultiContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");
        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata) field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata) containedField.getType();
        List<DataRecord> list = new ArrayList<DataRecord>();
        DataRecord contained1 = createDataRecord(tm);
        list.add(contained1);

        DataRecord contained2 = createDataRecord(tm);
        list.add(contained2);

        DataRecord contained3 = createDataRecord(tm);
        setDataRecordField(contained3, "ContainedId", "CID2");
        setDataRecordField(contained3, "ContainedName", null);
        list.add(contained3);

        setDataRecordField(record, "Contained", list);

        String result = toXmlString(record);

        Assert.assertEquals(
                "<WithMultiContained><Id>ABCD</Id><Contained><ContainedId>CID2</ContainedId><ContainedName></ContainedName></Contained></WithMultiContained>",
                result);
    }

    @Test
    public void testMultiContainedTypeWithEmptyDataRecord() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithMultiContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");
        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata) field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata) containedField.getType();
        List<DataRecord> list = new ArrayList<DataRecord>();
        DataRecord contained1 = createDataRecord(tm);
        list.add(contained1);

        DataRecord contained2 = createDataRecord(tm);
        list.add(contained2);

        DataRecord contained3 = createDataRecord(tm);
        list.add(contained3);

        setDataRecordField(record, "Contained", list);

        String result = toXmlString(record);

        Assert.assertEquals(
                "<WithMultiContained><Id>ABCD</Id><Contained><ContainedId></ContainedId><ContainedName></ContainedName></Contained></WithMultiContained>",
                result);
    }

    @Test
    public void testMultiContainedTypeWithNullList() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithMultiContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");
        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata) field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata) containedField.getType();

        String result = toXmlString(record);

        Assert.assertEquals("<WithMultiContained><Id>ABCD</Id><Contained></Contained></WithMultiContained>", result);
    }

    @Test
    public void testMultiContainedTypeWithEmptyList() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithMultiContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");
        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata) field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata) containedField.getType();

        setDataRecordField(record, "Contained", new ArrayList<DataRecord>());

        String result = toXmlString(record);

        Assert.assertEquals("<WithMultiContained><Id>ABCD</Id></WithMultiContained>", result);
    }

    @Test
    public void testEnumType() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithEnum"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Color", "White");
        String result = toXmlString(record);
        Assert.assertEquals("<WithEnum><Id>12345</Id><Color>White</Color></WithEnum>", result);
    }

    @Test
    public void testEnumTypeWithNullValue() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithEnum"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Color", null);
        String result = toXmlString(record);
        Assert.assertEquals("<WithEnum><Id>12345</Id><Color></Color></WithEnum>", result);
    }

    private String toXmlString(DataRecord record) throws Exception {
        ResettableStringWriter w = new ResettableStringWriter();
        writer.write(record, w);
        return w.toString();
    }
}
