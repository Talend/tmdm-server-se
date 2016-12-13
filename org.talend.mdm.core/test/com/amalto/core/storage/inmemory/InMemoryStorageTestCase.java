/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.inmemory;

import static com.amalto.core.query.user.UserQueryBuilder.alias;
import static com.amalto.core.query.user.UserQueryBuilder.distinct;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;


public class InMemoryStorageTestCase {
    
    private InMemoryStorage storage;
    
    private MetadataRepository repository;
    
    private ComplexTypeMetadata product;
    
    @Before
    public void setup() throws Exception {
        storage = new InMemoryStorage();
        storage.init(new DataSourceDefinition(null, null, null));
        repository = new MetadataRepository();
        repository.load(this.getClass().getResource("inmemory.xsd").openStream());
        product = repository.getComplexType("Product");
        storage.adapt(repository, true);
        
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        List<DataRecord> records = new ArrayList<DataRecord>();
        records.add(factory.read(repository, product, "<Product><Id>1</Id><Name>Name</Name><Description>Description</Description><Availability>true</Availability></Product>"));
        records.add(factory.read(repository, product, "<Product><Id>2</Id><Name>Name</Name><Description>Description</Description><Availability>false</Availability></Product>"));
        
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }
        
    }
    
    @After
    public void teardown() throws Exception {
        if(storage != null){
            storage.close();
        }
    }
    
    @Test
    public void testQuery() throws Exception {
        UserQueryBuilder builder = from(product).select(alias(distinct(product.getField("Availability")), "availability"));
        StorageResults result = this.storage.fetch(builder.getSelect());
        Assert.assertEquals(2, result.getCount());
        Iterator<DataRecord> it = result.iterator();
        while(it.hasNext()){
            DataRecord record = it.next();
            Object o = record.get("availability");
            Assert.assertNotNull(o);
            Assert.assertTrue(o instanceof Boolean);
        }
    }
}
