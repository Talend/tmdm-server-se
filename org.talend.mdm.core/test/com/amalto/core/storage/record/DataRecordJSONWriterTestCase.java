// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
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

import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;


public class DataRecordJSONWriterTestCase {
    
    private DataRecordJSONWriter writer;
    
    private SecuredStorage.UserDelegator delegate;
    
    private MetadataRepository repository;
    
    @Before
    public void setup() throws Exception{
        writer = new DataRecordJSONWriter();
        delegate = Mockito.mock(SecuredStorage.UserDelegator.class);
        Mockito.when(delegate.hide(Mockito.any(ComplexTypeMetadata.class))).thenReturn(false);
        Mockito.when(delegate.hide(Mockito.any(FieldMetadata.class))).thenReturn(false);
        writer.setSecurityDelegator(delegate);
        repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("metadata.xsd"));
    }
    
    @Test
    public void testSimpleComplexType() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("SimpleProduct"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", "Name");
        setDataRecordField(record, "Description", "Desc");
        setDataRecordField(record, "Availability", Boolean.FALSE);
        String result = toJSON(record);
        Assert.assertEquals("{\"simpleproduct\":{\"id\":\"12345\",\"name\":\"Name\",\"description\":\"Desc\",\"availability\":\"false\"}}", result);
    }

    @Test
    public void testSimpleComplexTypeWithDifferentLanguage() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("SimpleProduct"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", "1jGTpu é #dwe # é  BCLS #237 28TH & MAIN &&&& > < ,>= привет 안녕 하세요  こんにちは 你好");
        setDataRecordField(record, "Description", "Desc");
        setDataRecordField(record, "Availability", Boolean.FALSE);
        String result = toJSON(record);
        Assert.assertEquals(
                "{\"simpleproduct\":{\"id\":\"12345\",\"name\":\"1jGTpu é #dwe # é  BCLS #237 28TH & MAIN &&&& > < ,>= привет 안녕 하세요  こんにちは 你好\",\"description\":\"Desc\",\"availability\":\"false\"}}",
                result);
    }

    @Test
    public void testSimpleTypeWithArray() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithArray"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Repeat", "ABC");
        setDataRecordField(record, "Repeat", "DEF");
        String result = toJSON(record);
        Assert.assertEquals("{\"witharray\":{\"id\":\"12345\",\"repeat\":[\"ABC\",\"DEF\"]}}", result);
    }
    
    @Test
    public void testSimpleTypeWithNullArray() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithArray"));
        setDataRecordField(record, "Id", "12345");
        String result = toJSON(record);
        Assert.assertEquals("{\"witharray\":{\"id\":\"12345\"}}", result);
    }
    
    @Test
    public void testSimpleTypeWithEmptyArray() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("WithArray"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Repeat", new ArrayList<String>());
        String result = toJSON(record);
        Assert.assertEquals("{\"witharray\":{\"id\":\"12345\",\"repeat\":[]}}", result);
    }
    
    @Test
    public void testTypeWithComplexType() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("Customer"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", "Name");
        String result = toJSON(record);
        Assert.assertEquals("{\"customer\":{\"id\":\"12345\",\"name\":\"Name\",\"address\":{}}}", result);
    }
    
    @Test
    public void testContainedType() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");
        
        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata)field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata)containedField.getType();

        DataRecord contained = createDataRecord(tm);
        setDataRecordField(contained, "ContainedId", "CID");
        setDataRecordField(contained, "ContainedName", "CName");
        
        setDataRecordField(record, "Contained", contained);
        
        String result = toJSON(record);
        
        Assert.assertEquals("{\"withcontained\":{\"id\":\"ABCD\",\"contained\":{\"containedid\":\"CID\",\"containedname\":\"CName\"}}}", result);
    }
    
    @Test
    public void testMultiContainedType() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithMultiContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");
        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata)field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata)containedField.getType();
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
        
        String result = toJSON(record);
        
        Assert.assertEquals("{\"withmulticontained\":{\"id\":\"ABCD\",\"contained\":"
                + "[{\"containedid\":\"CID1\",\"containedname\":\"CName1\"},"
                + "{\"containedid\":\"CID2\",\"containedname\":\"CName2\"}]}}", result);
    }
    
    @Test
    public void testMultiContainedTypeWithNullList() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithMultiContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");
        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata)field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata)containedField.getType();
        
        String result = toJSON(record);
        
        Assert.assertEquals("{\"withmulticontained\":{\"id\":\"ABCD\",\"contained\":"
                + "[]}}", result);
    }
    
    @Test
    public void testMultiContainedTypeWithEmptyList() throws Exception {
        ComplexTypeMetadata type = repository.getComplexType("WithMultiContained");
        DataRecord record = createDataRecord(type);
        setDataRecordField(record, "Id", "ABCD");
        FieldMetadata field = type.getField("Contained");
        Assert.assertTrue(field instanceof ContainedTypeFieldMetadata);
        ContainedTypeFieldMetadata containedField = (ContainedTypeFieldMetadata)field;
        ContainedComplexTypeMetadata tm = (ContainedComplexTypeMetadata)containedField.getType();

        setDataRecordField(record, "Contained", new ArrayList<DataRecord>());
        
        String result = toJSON(record);
        
        Assert.assertEquals("{\"withmulticontained\":{\"id\":\"ABCD\",\"contained\":"
                + "[]}}", result);
    }
    
    private void setDataRecordField(DataRecord record, String name, Object value) throws Exception {
        FieldMetadata fieldMd = record.getType().getField(name);
        Assert.assertNotNull("Unknown field " + name, fieldMd);
        record.set(fieldMd, value);
    }
    
    private DataRecord createDataRecord(ComplexTypeMetadata type) throws Exception {
        Assert.assertNotNull(type);
        DataRecordMetadataImpl recordMeta = new DataRecordMetadataImpl(System.currentTimeMillis(), "taskId");
        DataRecord record = new DataRecord(type, recordMeta);
        return record;
    }
    
    private String toJSON(DataRecord record) throws Exception {
        Writer w = new StringWriter();
        writer.write(record, w);
        return w.toString();
    }

}
