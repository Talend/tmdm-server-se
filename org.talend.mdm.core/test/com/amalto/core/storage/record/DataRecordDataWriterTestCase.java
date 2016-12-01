package com.amalto.core.storage.record;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;

public class DataRecordDataWriterTestCase {

    protected MetadataRepository repository;
    
    protected SecuredStorage.UserDelegator delegate;

    public DataRecordDataWriterTestCase() {
        super();
    }

    protected void setDataRecordField(DataRecord record, String name, Object value) throws Exception {
        FieldMetadata fieldMd = record.getType().getField(name);
        Assert.assertNotNull("Unknown field " + name, fieldMd);
        record.set(fieldMd, value);
    }

    protected DataRecord createDataRecord(ComplexTypeMetadata type) throws Exception {
        Assert.assertNotNull(type);
        DataRecordMetadataImpl recordMeta = new DataRecordMetadataImpl(System.currentTimeMillis(), "taskId");
        DataRecord record = new DataRecord(type, recordMeta);
        return record;
    }

    @Before
    public void setup() throws Exception {
        delegate = Mockito.mock(SecuredStorage.UserDelegator.class);
        Mockito.when(delegate.hide(Mockito.any(ComplexTypeMetadata.class))).thenReturn(false);
        Mockito.when(delegate.hide(Mockito.any(FieldMetadata.class))).thenReturn(false);
        repository = new MetadataRepository();
    }

}