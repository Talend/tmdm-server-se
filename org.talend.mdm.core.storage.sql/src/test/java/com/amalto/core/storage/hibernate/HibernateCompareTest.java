/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
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
package com.amalto.core.storage.hibernate;

import static com.amalto.core.query.user.UserQueryBuilder.from;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.compare.Change;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;

import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;


public class HibernateCompareTest {

    protected static final String STORAGE_NAME = "Test";
    
    @BeforeClass
    public static void setUp() throws Exception {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }
    
    @Test
    public void testFieldOptionToMadatory_noValue(){
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        HibernateStorage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository original = new MetadataRepository();
        original.load(HibernateCompareTest.class.getResourceAsStream("../adapt/schema12_1.xsd"));
        storage.prepare(original, true);
        
        MetadataRepository updated1 = new MetadataRepository();
        updated1.load(HibernateCompareTest.class.getResourceAsStream("../adapt/schema12_2.xsd"));
        //storage.adapt(updated1, true);
        
        Compare.DiffResults diffResults = Compare.compare(original, updated1);
        assertEquals(6, diffResults.getActions().size());
        assertEquals(6, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());
        
        ImpactAnalyzer analyzer = new HibernateStorageDataAnaylzer(storage);
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(6, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }
    
    @Test
    public void testFiledOptionToMadatory_containsValue(){
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        HibernateStorage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository original = new MetadataRepository();
        original.load(HibernateCompareTest.class.getResourceAsStream("../adapt/schema12_1.xsd"));
        storage.prepare(original, true);
        
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        String[] typeNames = { "Person" };
        
        
        String input1 = "<Person><Id>1</Id><first_name>Jack</first_name><second_name>Chen</second_name><full_name>Jack Chen</full_name><age>11</age><married>true</married><birthday>2017-02-04</birthday></Person>";
        createRecord(storage, factory, original, typeNames, new String[] { input1 });
        
        MetadataRepository updated1 = new MetadataRepository();
        updated1.load(HibernateCompareTest.class.getResourceAsStream("../adapt/schema12_2.xsd"));
        //storage.adapt(updated1, true);
        
        Compare.DiffResults diffResults = Compare.compare(original, updated1);
        assertEquals(6, diffResults.getActions().size());
        assertEquals(6, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());
        
        ImpactAnalyzer analyzer = new HibernateStorageDataAnaylzer(storage);
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(6, sort.get(ImpactAnalyzer.Impact.LOW).size());
        
        UserQueryBuilder qb = from(original.getComplexType("Person"));
        storage.delete(qb.getSelect());
    }
    
    @Test
    public void testFiledOptionToMadatory_containsNullValueWithDefaultValue(){
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        HibernateStorage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository original = new MetadataRepository();
        original.load(HibernateCompareTest.class.getResourceAsStream("../adapt/schema12_1.xsd"));
        storage.prepare(original, true);
        
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        String[] typeNames = { "Person" };
        
        String input1 = "<Person><Id>1</Id><full_name>Jack Chen</full_name><age>11</age><birthday>2017-02-04</birthday></Person>";
        createRecord(storage, factory, original, typeNames, new String[] { input1 });
        
        ComplexTypeMetadata objectType = original.getComplexType("Person");//$NON-NLS-1$
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.end();
        
        
        MetadataRepository updated1 = new MetadataRepository();
        updated1.load(HibernateCompareTest.class.getResourceAsStream("../adapt/schema12_2.xsd"));
        //storage.adapt(updated1, true);
        
        Compare.DiffResults diffResults = Compare.compare(original, updated1);
        assertEquals(6, diffResults.getActions().size());
        assertEquals(6, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());
        
        ImpactAnalyzer analyzer = new HibernateStorageDataAnaylzer(storage);
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(2, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
        
        qb = from(original.getComplexType("Person"));
        storage.delete(qb.getSelect());
    }

    @Test
    public void testRenameFKName_NoFKData() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        HibernateStorage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository original = new MetadataRepository();
        original.load(HibernateCompareTest.class.getResourceAsStream("../adapt/schema21_1.xsd"));
        storage.prepare(original, true);

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        String typeName = "Product";
        String[] typeNames = { typeName };

        String input1 = "<Product><Id>1</Id><Name>John</Name></Product>";
        createRecord(storage, factory, original, typeNames, new String[] { input1 });

        ComplexTypeMetadata objectType = original.getComplexType(typeName);
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.end();

        MetadataRepository updated1 = new MetadataRepository();
        updated1.load(HibernateCompareTest.class.getResourceAsStream("../adapt/schema21_2.xsd"));

        Compare.DiffResults diffResults = Compare.compare(original, updated1);
        assertEquals(2, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(1, diffResults.getRemoveChanges().size());
        assertEquals(1, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageDataAnaylzer(storage);
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());

        qb = from(original.getComplexType(typeName));
        storage.delete(qb.getSelect());
    }

    @Test
    public void testRenameFKName_ContainsFKData() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS3", STORAGE_NAME);
        HibernateStorage storage = new HibernateStorage("Person", StorageType.MASTER);
        storage.init(dataSource);
        MetadataRepository original = new MetadataRepository();
        original.load(HibernateCompareTest.class.getResourceAsStream("../adapt/schema21_1.xsd"));
        storage.prepare(original, true);

        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        String typeName = "Product";
        String[] typeNames = { "ProductFamily", typeName };

        String input1 = "<ProductFamily><Id>1</Id><Name>Product family #1</Name></ProductFamily>";
        String input2 = "<Product><Id>1</Id><Name>John</Name><Family>1</Family></Product>";
        createRecord(storage, factory, original, typeNames, new String[] { input1, input2 });

        ComplexTypeMetadata objectType = original.getComplexType(typeName);
        UserQueryBuilder qb = from(objectType);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id"));
            }
        } finally {
            results.close();
        }
        storage.end();

        MetadataRepository updated1 = new MetadataRepository();
        updated1.load(HibernateCompareTest.class.getResourceAsStream("../adapt/schema21_2.xsd"));

        Compare.DiffResults diffResults = Compare.compare(original, updated1);
        assertEquals(2, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(1, diffResults.getRemoveChanges().size());
        assertEquals(1, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageDataAnaylzer(storage);
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());

        qb = from(original.getComplexType(typeName));
        storage.delete(qb.getSelect());
    }
    
    private void createRecord(Storage storage, DataRecordReader<String> factory, MetadataRepository repository,  String[] typeNames, String[] inputs){
        List<DataRecord> records = new ArrayList<DataRecord>();
        for (int i = 0; i < typeNames.length; i++) {
            records.add(factory.read(repository, repository.getComplexType(typeNames[i]), inputs[i]));
        }
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }
    }
}
