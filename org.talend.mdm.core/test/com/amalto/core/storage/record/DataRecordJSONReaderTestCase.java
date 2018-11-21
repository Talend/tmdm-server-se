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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class DataRecordJSONReaderTestCase extends DataRecordDataWriterTestCase {

    private DataRecordJSONReader reader;

    private DataRecordXmlWriter writer;

    @Before
    public void setup() throws Exception {
        super.setup();
        writer = new DataRecordXmlWriter();
        writer.setSecurityDelegator(delegate);
        reader = new DataRecordJSONReader();
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));
    }

    @Test
    public void testComplexType() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("SimpleProduct"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", "Name");
        setDataRecordField(record, "Description", "Desc");
        setDataRecordField(record, "Availability", Boolean.FALSE);

        String inputJson = "{\"SimpleProduct\": {\"Id\": \"12345\",\"Name\": \"Name\",\"Description\": \"Desc\",\"Availability\": \"false\"}}";
        DataRecord resultRecord = toDataRecord(inputJson, "SimpleProduct");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithArray() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithArray"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Repeat", "ABC");
        setDataRecordField(record, "Repeat", "DEF");

        String inputJson = "{\"WithArray\": {\"Id\": \"12345\",\"Repeat\": \"ABC\",\"Repeat\": \"DEF\"}}";
        DataRecord resultRecord = toDataRecord(inputJson, "WithArray");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithArrayContainsNull() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithArray"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Repeat", "ABC");
        setDataRecordField(record, "Repeat", null);

        String inputJson = "{\"WithArray\": {\"Id\": \"12345\",\"Repeat\": \"ABC\"}}";
        DataRecord resultRecord = toDataRecord(inputJson, "WithArray");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithEmptyArray() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithArray"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Repeat", new ArrayList<String>());

        String inputJson = "{\"WithArray\": {\"Id\": \"12345\"}}";
        DataRecord resultRecord = toDataRecord(inputJson, "WithArray");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeContainsNull() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("SimpleProduct"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", null);
        setDataRecordField(record, "Description", "Desc");
        setDataRecordField(record, "Availability", null);

        String inputJson = "{\"SimpleProduct\": {\"Id\": \"12345\",\"Description\": \"Desc\"}}";
        DataRecord resultRecord = toDataRecord(inputJson, "SimpleProduct");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithReferencedField() throws Exception {
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

        String inputJson = "{\"Customer\": {\"Id\": \"12345\",\"Name\": \"Name\",\"Address\": {\"Id\": \"AddressId\",\"City\": \"AddressCity\",\"Street\": \"AddressStreet\"}}}";
        DataRecord resultRecord = toDataRecord(inputJson, "Customer");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithReferencedFieldContainsNull() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("Customer");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", null);

        FieldMetadata field = type.getField("Address");

        DataRecord referenced = createDataRecord(((ContainedComplexTypeMetadata) field.getType()).getContainedType());
        setDataRecordField(referenced, "Id", "AddressId");
        setDataRecordField(referenced, "Street", "AddressStreet");
        setDataRecordField(referenced, "City", null);

        setDataRecordField(record, "Address", referenced);
        String inputJson = "{\"Customer\": {\"Id\": \"12345\",\"Address\": {\"Id\": \"AddressId\",\"Street\": \"AddressStreet\"}}}";
        DataRecord resultRecord = toDataRecord(inputJson, "Customer");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithEmptyReferencedFieldContainsNull() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("Customer");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", null);

        FieldMetadata field = type.getField("Address");

        DataRecord referenced = createDataRecord(((ContainedComplexTypeMetadata) field.getType()).getContainedType());

        setDataRecordField(record, "Address", referenced);

        String inputJson = "{\"Customer\": {\"Id\": \"12345\",\"Address\": \"\"}}";
        DataRecord resultRecord = toDataRecord(inputJson, "Customer");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithContainedField() throws Exception {
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

        String inputJson = "{\"WithContained\": {\"Id\": \"ABCD\",\"Contained\": {\"ContainedId\": \"CID\", \"ContainedName\": \"CName\"}}}";
        DataRecord resultRecord = toDataRecord(inputJson, "WithContained");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testContainedTypeContainsNull() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");

        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata) field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata) containedField.getType();

        DataRecord contained = createDataRecord(tm);
        setDataRecordField(contained, "ContainedId", "CID");
        setDataRecordField(contained, "ContainedName", null);

        setDataRecordField(record, "Contained", contained);

        String inputJson = "{\"WithContained\": {\"Id\": \"ABCD\",\"Contained\": {\"ContainedId\": \"CID\"}}}";
        DataRecord resultRecord = toDataRecord(inputJson, "WithContained");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithNullContainedField() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");

        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata) field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata) containedField.getType();

        DataRecord contained = createDataRecord(tm);

        setDataRecordField(record, "Contained", contained);

        String inputJson = "{\"WithContained\":{\"Id\": \"ABCD\",\"Contained\": \"\"}}";
        DataRecord resultRecord = toDataRecord(inputJson, "WithContained");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithMultiContainedField() throws Exception {
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

        String inputJson = "{\"WithMultiContained\":{\"Id\": \"ABCD\",\"Contained\": [{\"ContainedId\": \"CID1\",\"ContainedName\": \"CName1\"},{\"ContainedId\": \"CID2\",\"ContainedName\": \"CName2\"}]}}";
        DataRecord resultRecord = toDataRecord(inputJson, "WithMultiContained");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithEmptyMultiContainedField() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithMultiContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");
        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata) field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata) containedField.getType();
        setDataRecordField(record, "Contained", new ArrayList<DataRecord>());

        String inputJson = "{\"WithMultiContained\":{\"Id\":\"ABCD\"}}";
        DataRecord resultRecord = toDataRecord(inputJson, "WithMultiContained");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithEnumContainsNull() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithEnum"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Color", "White");
        setDataRecordField(record, "Color", null);

        String inputJson = "{\"WithEnum\":{\"Id\":\"12345\",\"Color\":\"White\"}}";
        DataRecord resultRecord = toDataRecord(inputJson, "WithEnum");
        Assert.assertEquals(record, resultRecord);
    }

    @Test
    public void testComplexTypeWithEnum() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithEnum"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Color", "White");

        String inputJson = "{\"WithEnum\":{\"Id\":\"12345\",\"Color\":\"White\"}}";
        DataRecord resultRecord = toDataRecord(inputJson, "WithEnum");
        Assert.assertEquals(record, resultRecord);
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

        String inputJson = "{\"Customer\":{\"Id\":\"12345\",\"Name\": \"Name\",\"Address\": {\"Id\": \"AddressId\",\"Street\": \"AddressStreet\",\"City\": \"AddressCity\"}}}";
        DataRecord resultRecord = toDataRecord(inputJson, "Customer");
        Assert.assertEquals(record, resultRecord);
    }

    /*
    "Customer": {
        "Id": "33",
        "$ref": "#/NameField",
        "NameField": {  "Name": "New_Name" }
        }
    }
    */
    @Test
    public void testTypeWithJSONReference() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("Customer");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", "New_Name");

        String inputJson = "{\"Customer\":{\"Id\":\"12345\",\"$ref\": \"#/NameField\",\"NameField\": {  \"Name\": \"New_Name\" }}}";
        DataRecord resultRecord = toDataRecord(inputJson, "Customer");

        Assert.assertEquals(record.get("Name"), resultRecord.get("Name"));
    }

    private DataRecord toDataRecord(String strJson, String storageName) throws Exception {
        JsonParser parser = new JsonParser();
        JsonElement root = parser.parse(strJson);
        ComplexTypeMetadata complexTypeMetadata = repository.getComplexType(storageName);
        DataRecord record = reader.read(repository, complexTypeMetadata, root);
        return record;
    }
}
