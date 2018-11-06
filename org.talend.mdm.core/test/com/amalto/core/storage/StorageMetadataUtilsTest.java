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
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;

import com.amalto.core.query.user.DateConstant;
import com.amalto.core.query.user.DateTimeConstant;
import com.amalto.core.query.user.TimeConstant;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;

public class StorageMetadataUtilsTest {

    @BeforeClass
    public static void setup() throws Exception {
    }
    
    @Test
    public void testToString() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageMetadataUtilsTest.class.getResourceAsStream("../storage/record/metadata.xsd"));  //$NON-NLS-1$

        // test for escapeXml
        assertEquals("Name", StorageMetadataUtils.toString("Name"));
        assertEquals("Name &gt; &amp; &gt;=", StorageMetadataUtils.toString("Name > & >="));
        assertEquals("Name &gt; &amp; &gt;= &#233; or &#231; &#20320;&#22909;",
                StorageMetadataUtils.toString("Name > & >= é or ç 你好"));

        DataRecord record = createDataRecord(repository.getComplexType("SimpleProduct"));
        setDataRecordField(record, "Id", "12345");
        setDataRecordField(record, "Name", "Name");

        assertEquals("[12345]", StorageMetadataUtils.toString(record));

        record = createDataRecord(repository.getComplexType("SimpleProduct"));
        setDataRecordField(record, "Id", "12345  é or ç > & >= 你好");
        assertEquals("[12345  &#233; or &#231; &gt; &amp; &gt;= &#20320;&#22909;]", StorageMetadataUtils.toString(record));

        // test for no escapeXml
        assertEquals("Name", StorageMetadataUtils.toString("Name"));
        assertEquals("Name > & >=", StorageMetadataUtils.toString("Name > & >=", false));
        assertEquals("Name > & >= é or ç 你好", StorageMetadataUtils.toString("Name > & >= é or ç 你好", false));

        record = createDataRecord(repository.getComplexType("SimpleProduct"));
        setDataRecordField(record, "Id", "12345");

        assertEquals("[12345]", StorageMetadataUtils.toString(record, false));

        record = createDataRecord(repository.getComplexType("SimpleProduct"));
        setDataRecordField(record, "Id", "12345  é or ç > & >= 你好");
        setDataRecordField(record, "Name", "Name");
        assertEquals("[12345  é or ç > & >= 你好]", StorageMetadataUtils.toString(record, false));

        repository.load(StorageMetadataUtilsTest.class.getResourceAsStream("../storage/record/TestDecimalMetadata.xsd")); //$NON-NLS-1$
        ComplexTypeMetadata mainType = repository.getComplexType("TestDecimal"); //$NON-NLS-1$
        // Test scientific notation convert to number on float type,like 7.0E-8 convert to 0.00000007 and 7.0E-7 convert
        // to 0.0000007, shouldn't be 0.00000070
        assertEquals("0.00000007", StorageMetadataUtils.toString(Float.parseFloat("0.00000007"), mainType.getField("testFloat"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.0000007", StorageMetadataUtils.toString(Float.parseFloat("0.0000007"), mainType.getField("testFloat"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.000007", StorageMetadataUtils.toString(Float.parseFloat("0.000007"), mainType.getField("testFloat"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.00007", StorageMetadataUtils.toString(Float.parseFloat("0.00007"), mainType.getField("testFloat"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.0007", StorageMetadataUtils.toString(Float.parseFloat("0.0007"), mainType.getField("testFloat"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.007", StorageMetadataUtils.toString(Float.parseFloat("0.007"), mainType.getField("testFloat"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.07", StorageMetadataUtils.toString(Float.parseFloat("0.07"), mainType.getField("testFloat"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.7", StorageMetadataUtils.toString(Float.parseFloat("0.7"), mainType.getField("testFloat"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // Test scientific notation convert to number on custom float
        assertEquals("0.00000007", //$NON-NLS-1$
                StorageMetadataUtils.toString(Float.parseFloat("0.00000007"), mainType.getField("testCustomFloat"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("0.0007", //$NON-NLS-1$
                StorageMetadataUtils.toString(Float.parseFloat("0.0007"), mainType.getField("testCustomFloat"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("0.7", //$NON-NLS-1$
                StorageMetadataUtils.toString(Float.parseFloat("0.7"), mainType.getField("testCustomFloat"))); //$NON-NLS-1$ //$NON-NLS-2$
        // Test scientific notation convert to number on double type
        assertEquals("0.00000007", //$NON-NLS-1$
                StorageMetadataUtils.toString(Double.parseDouble("0.00000007"), mainType.getField("testDouble"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("0.0000007", //$NON-NLS-1$
                StorageMetadataUtils.toString(Double.parseDouble("0.0000007"), mainType.getField("testDouble"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("0.000007", //$NON-NLS-1$
                StorageMetadataUtils.toString(Double.parseDouble("0.000007"), mainType.getField("testDouble"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("0.00007", //$NON-NLS-1$
                StorageMetadataUtils.toString(Double.parseDouble("0.00007"), mainType.getField("testDouble"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("0.0007", //$NON-NLS-1$
                StorageMetadataUtils.toString(Double.parseDouble("0.0007"), mainType.getField("testDouble"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("0.007", //$NON-NLS-1$
                StorageMetadataUtils.toString(Double.parseDouble("0.007"), mainType.getField("testDouble"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("0.07", //$NON-NLS-1$
                StorageMetadataUtils.toString(Double.parseDouble("0.07"), mainType.getField("testDouble"))); //$NON-NLS-1$ //$NON-NLS-2$
        assertEquals("0.7", //$NON-NLS-1$
                StorageMetadataUtils.toString(Double.parseDouble("0.7"), mainType.getField("testDouble"))); //$NON-NLS-1$ //$NON-NLS-2$
        // Test scientific notation convert to number on decimal type,like 7.0E-7 convert
        // to 0.00000070, shouldn't be 0.0000007,because decimal type can set fraction digits.
        assertEquals("0.00000007", StorageMetadataUtils.toString(new BigDecimal("0.00000007"), mainType.getField("testDecimal"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.0000007", StorageMetadataUtils.toString(new BigDecimal("0.0000007"), mainType.getField("testDecimal"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.00000070", StorageMetadataUtils.toString(new BigDecimal("0.00000070"), mainType.getField("testDecimal"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.000007", StorageMetadataUtils.toString(new BigDecimal("0.000007"), mainType.getField("testDecimal"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.00000700", StorageMetadataUtils.toString(new BigDecimal("0.00000700"), mainType.getField("testDecimal"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.00007", StorageMetadataUtils.toString(new BigDecimal("0.00007"), mainType.getField("testDecimal"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.0007", StorageMetadataUtils.toString(new BigDecimal("0.0007"), mainType.getField("testDecimal"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.007", StorageMetadataUtils.toString(new BigDecimal("0.007"), mainType.getField("testDecimal"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.07", StorageMetadataUtils.toString(new BigDecimal("0.07"), mainType.getField("testDecimal"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("0.7", StorageMetadataUtils.toString(new BigDecimal("0.7"), mainType.getField("testDecimal"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        // Test scientific notation convert to number on custom decimal type
        assertEquals("0.00000007", //$NON-NLS-1$
                StorageMetadataUtils.toString(new BigDecimal("0.00000007"), mainType.getField("testCustomDecimal"))); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Test
    public void testPath() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageMetadataUtilsTest.class.getResourceAsStream("parnter.xsd")); //$NON-NLS-1$

        ComplexTypeMetadata mainType = repository.getComplexType("Partner_Relation");
        FieldMetadata fieldMetadata = mainType.getField("FK_Partner");
        FieldMetadata groupField = ((ReferenceFieldMetadata) fieldMetadata).getReferencedType().getField("FK_Account_Group");
        FieldMetadata domainField = mainType.getField("FK_Partner_Commercial_Domain");

        List<FieldMetadata> path = StorageMetadataUtils.path(mainType, fieldMetadata);
        assertEquals(1, path.size());
        assertEquals(fieldMetadata, path.get(0));

        path = StorageMetadataUtils.path(mainType, groupField);
        assertEquals(2, path.size());
        assertEquals(fieldMetadata, path.get(0));
        assertEquals(groupField, path.get(1));

        path = StorageMetadataUtils.path(mainType, domainField);
        assertEquals(1, path.size());
        assertEquals(domainField, path.get(0));

        // TMDM-11467 The path of 'CodeNoeud' field should not contains 'NiveauPere' field.
        repository.load(StorageMetadataUtilsTest.class.getResourceAsStream("si_usage.xsd")); //$NON-NLS-1$
        mainType = repository.getComplexType("NoeudNomenclature");
        fieldMetadata = mainType.getField("CodeNoeud");
        path = StorageMetadataUtils.path(mainType, fieldMetadata);
        assertEquals(1, path.size());
        assertEquals(fieldMetadata, path.get(0));
    }

    @Test
    public void testPathForInheritance() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageMetadataUtilsTest.class.getResourceAsStream("inheritance.xsd")); //$NON-NLS-1$

        ComplexTypeMetadata mainType = repository.getComplexType("A");
        FieldMetadata nestedBField = mainType.getField("nestedB");

        List<FieldMetadata> path = StorageMetadataUtils.path(mainType, nestedBField);
        assertEquals(1, path.size());
        assertEquals(nestedBField, path.get(0));

        mainType = repository.getComplexType("Compte");
        FieldMetadata childOfField = mainType.getField("childOf");

        path = StorageMetadataUtils.path(mainType, childOfField);
        assertEquals(1, path.size());
        assertEquals(childOfField, path.get(0));
    }

    protected DataRecord createDataRecord(ComplexTypeMetadata type) throws Exception {
        Assert.assertNotNull(type);
        DataRecordMetadataImpl recordMeta = new DataRecordMetadataImpl(System.currentTimeMillis(), "taskId"); //$NON-NLS-1$
        DataRecord record = new DataRecord(type, recordMeta);
        return record;
    }

    protected void setDataRecordField(DataRecord record, String name, Object value) throws Exception {
        FieldMetadata fieldMd = record.getType().getField(name);
        Assert.assertNotNull("Unknown field " + name, fieldMd); //$NON-NLS-1$
        record.set(fieldMd, value);
    }

    @Test
    public void testGetIds() {
        String ids = "[123], [456], [ab7]"; //$NON-NLS-1$        
        List<String> idList = StorageMetadataUtils.getIds(ids);
        
        for (int i=0; i<idList.size(); i++) {
            if (i==0) {
                assertEquals("123", idList.get(i)); //$NON-NLS-1$
            } else if (i==1) {
                assertEquals("456", idList.get(i)); //$NON-NLS-1$
            } else if (i==2) {
                assertEquals("ab7", idList.get(i)); //$NON-NLS-1$
            }
        }
    }

    @Test
    public void testConvertConstantExpressionDate() throws ParseException {
        DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$

        DateConstant dateConstant = new DateConstant("2018-10-06"); //$NON-NLS-1$
        Object dateStamp = StorageMetadataUtils.convert(dateConstant);
        assertTrue(dateStamp instanceof Timestamp);

        List<Date> listDate = new LinkedList<Date>();
        listDate.add(DATE_FORMAT.parse("2018-10-11")); //$NON-NLS-1$

        dateConstant = new DateConstant(listDate);
        Object dateStamps = StorageMetadataUtils.convert(dateConstant);
        @SuppressWarnings("unchecked")
        List<Timestamp> returnDateStamps = (List<Timestamp>)dateStamps;
        assertTrue(returnDateStamps.get(0) instanceof Timestamp);
    }

    @Test
    public void testConvertConstantExpressionDateTime() throws ParseException {
        DateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss"); //$NON-NLS-1$

        DateTimeConstant dateTimeConstant = new DateTimeConstant("2018-10-06T10:11:12");//$NON-NLS-1$
        Object dateTimeStamp = StorageMetadataUtils.convert(dateTimeConstant);
        assertTrue(dateTimeStamp instanceof Timestamp);

        List<Date> listDateTime = new LinkedList<Date>();
        listDateTime.add(DATE_TIME_FORMAT.parse("2018-10-11T10:11:12")); //$NON-NLS-1$

        dateTimeConstant = new DateTimeConstant(listDateTime);
        Object dateTimeStamps = StorageMetadataUtils.convert(dateTimeConstant);
        @SuppressWarnings("unchecked")
        List<Timestamp> returnDateTimeStamps = (List<Timestamp>)dateTimeStamps;
        assertTrue(returnDateTimeStamps.get(0) instanceof Timestamp);
    }

    @Test
    public void testConvertConstantExpressionTime() throws ParseException {
        DateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss"); //$NON-NLS-1$

        TimeConstant timeConstant = new TimeConstant("12:10:06"); //$NON-NLS-1$
        Object timeStamp = StorageMetadataUtils.convert(timeConstant);
        assertTrue(timeStamp instanceof Timestamp);

        List<Date> listTime = new LinkedList<Date>();
        listTime.add(TIME_FORMAT.parse("12:11:16")); //$NON-NLS-1$

        timeConstant = new TimeConstant(listTime);
        Object timeStamps = StorageMetadataUtils.convert(timeConstant);
        @SuppressWarnings("unchecked")
        List<Timestamp> returnTimeStamps = (List<Timestamp>)timeStamps;
        assertTrue(returnTimeStamps.get(0) instanceof Timestamp);
    }
}
