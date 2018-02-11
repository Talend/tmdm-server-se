/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.history.accessor.record;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordDataWriterTestCase;

public class DataRecordAccessorTest extends DataRecordDataWriterTestCase{

    private static MetadataRepository repository;

    @BeforeClass
    public static void setUp() {
        repository = new MetadataRepository();
        repository.load(DataRecordAccessorTest.class.getResourceAsStream("Party.xsd"));
    }

    @Test
    public void testGetType_with_simpleType() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("PartyProduct"));
        setDataRecordField(record, "id", "1");
        setDataRecordField(record, "name", "prat Product");

        DataRecordAccessor dataRecordAccessor = new DataRecordAccessor(repository, record, "name");
        assertEquals("PartyProduct", dataRecordAccessor.getActualType());
    }

    @Test
    public void testGetType_with_referenceType() throws Exception {
        DataRecord record = createDataRecord(repository.getComplexType("PartyProduct"));
        setDataRecordField(record, "id", "1");
        setDataRecordField(record, "name", "prat Product");

        DataRecord referenced = createDataRecord(repository.getComplexType("PartyCompany"));
        setDataRecordField(referenced, "code", "pp1");
        setDataRecordField(referenced, "name", "product company");

        setDataRecordField(record, "supplier", referenced);
        DataRecordAccessor dataRecordAccessor = new DataRecordAccessor(repository, record, "supplier");
        assertEquals(true, dataRecordAccessor.exist());
        assertEquals("PartyCompany", dataRecordAccessor.getActualType());
        
        setDataRecordField(record, "supplier", referenced);
        dataRecordAccessor = new DataRecordAccessor(repository, record, "supplier/name");
        assertEquals(false, dataRecordAccessor.exist());
    }
}
