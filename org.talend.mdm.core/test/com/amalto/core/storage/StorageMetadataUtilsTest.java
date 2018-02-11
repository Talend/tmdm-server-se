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
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;

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
}
