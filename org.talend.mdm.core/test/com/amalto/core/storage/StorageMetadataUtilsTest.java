/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;

public class StorageMetadataUtilsTest {

    protected static MetadataRepository repository;
    
    @BeforeClass
    public static void setup() throws Exception {
        repository = new MetadataRepository();
        repository.load(StorageMetadataUtilsTest.class.getResourceAsStream("../storage/record/metadata.xsd"));  //$NON-NLS-1$
    }
    
    @Test
    public void testToString() throws Exception {
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
    }

    protected DataRecord createDataRecord(ComplexTypeMetadata type) throws Exception {
        Assert.assertNotNull(type);
        DataRecordMetadataImpl recordMeta = new DataRecordMetadataImpl(System.currentTimeMillis(), "taskId");
        DataRecord record = new DataRecord(type, recordMeta);
        return record;
    }

    protected void setDataRecordField(DataRecord record, String name, Object value) throws Exception {
        FieldMetadata fieldMd = record.getType().getField(name);
        Assert.assertNotNull("Unknown field " + name, fieldMd);
        record.set(fieldMd, value);
    }
}
