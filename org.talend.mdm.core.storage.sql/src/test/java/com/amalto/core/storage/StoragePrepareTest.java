/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage;

import static com.amalto.core.query.user.UserQueryBuilder.eq;
import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.junit.Assert;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.query.StorageTestCase;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.OrderBy;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.save.context.StorageDocument;
import com.amalto.core.server.MockMetadataRepositoryAdmin;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.MockStorageAdmin;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.adapt.StorageAdaptTest;
import com.amalto.core.storage.datasource.DataSource;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.datasource.RDBMSDataSource;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.storage.record.DataRecordReader;
import com.amalto.core.storage.record.XmlStringDataRecordReader;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;

import junit.framework.TestCase;

public class StoragePrepareTest extends TestCase {
    
    protected static MockUserDelegator userSecurity = new MockUserDelegator();
    protected static final String STORAGE_NAME = "Test";
    
    public StoragePrepareTest() {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }
    
    private MetadataRepository prepareMetadata(String xsd) {
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream(xsd));
        return repository;
    }

    private Storage prepareStorage(String name, MetadataRepository repository) {
        Storage storage = new HibernateStorage(name);
        storage.init(ServerContext.INSTANCE.get().getDefinition(StorageTestCase.DATABASE + "-Default", "MDM")); //$NON-NLS-1$//$NON-NLS-2$
        storage.prepare(repository, true);
        return storage;
    }
    
    public void test1_CompositeIdAndContainedType() {
        String[] userKeys = { "NumeroBdd", "BddSource", "NomApplication", "IdMDM" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        String[] dbKeys = { "x_numerobdd", "x_bddsource", "x_nomapplication", "x_idmdm" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        MetadataRepository repository = prepareMetadata("StoragePrepare_1.xsd"); //$NON-NLS-1$
        ComplexTypeMetadata userType = repository.getComplexType("XrefAgence"); //$NON-NLS-1$
        // assert user type
        assertNotNull(userType);
        assertEquals(4, userType.getKeyFields().size());
        int i = 0;
        for (FieldMetadata keyField : userType.getKeyFields()) {
            assertEquals(userKeys[i++], keyField.getName());
        }
        // assert database type
        HibernateStorage storage = (HibernateStorage) prepareStorage("Test1", repository); //$NON-NLS-1$
        ComplexTypeMetadata dbType = storage.getTypeEnhancer().getMappings().getMappingFromUser(userType).getDatabase();
        assertNotNull(dbType);
        assertEquals(4, dbType.getKeyFields().size());
        int j = 0;
        for (FieldMetadata keyField : dbType.getKeyFields()) {
            assertEquals(dbKeys[j++], keyField.getName());
        }
    }

    //TMDM-13960 Error deploying data model
    public void testEECDataModel() {
        Storage storage = new SecuredStorage(new HibernateStorage("Express_EEC_1", StorageType.MASTER), userSecurity);//$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("Express_EEC_1.xsd"));//$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("Express_EEC_1", repository);//$NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));// //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        storage.begin();
        ComplexTypeMetadata country = repository.getComplexType("Country");//$NON-NLS-1$
        ComplexTypeMetadata cacdo_RegisterCustomsWarehouseDetails = repository.getComplexType("cacdo_RegisterCustomsWarehouseDetails");//$NON-NLS-1$
        UserQueryBuilder qb = from(country);
        UserQueryBuilder qb_CustomsWarehouseDetails = from(cacdo_RegisterCustomsWarehouseDetails);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.end();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, country, "<Country><Code>3</Code><Name>33</Name></Country>")); //$NON-NLS-1$
        records.add(factory.read(repository, country, "<Country><Code>4</Code><Name>44</Name></Country>")); //$NON-NLS-1$
        records.add(factory.read(repository, cacdo_RegisterCustomsWarehouseDetails,
                "<cacdo_RegisterCustomsWarehouseDetails><NSIKey>5</NSIKey><cacdo_RegisterOrganizationDetails><ccdo_OrganizationDetails><csdo_CountryCode>[3]</csdo_CountryCode></ccdo_OrganizationDetails></cacdo_RegisterOrganizationDetails></cacdo_RegisterCustomsWarehouseDetails>")); //$NON-NLS-1$
        records.add(factory.read(repository, cacdo_RegisterCustomsWarehouseDetails,
                "<cacdo_RegisterCustomsWarehouseDetails><NSIKey>6</NSIKey><cacdo_RegisterOrganizationDetails><ccdo_OrganizationDetails><csdo_CountryCode>[4]</csdo_CountryCode></ccdo_OrganizationDetails></cacdo_RegisterOrganizationDetails></cacdo_RegisterCustomsWarehouseDetails>")); //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
        results = storage.fetch(qb_CustomsWarehouseDetails.getSelect());
        try {
            assertEquals(2, results.getCount());
            for (DataRecord result : results) {
                String id = result.get("NSIKey").toString();
                switch (id) {
                case "5"://$NON-NLS-1$
                    assertEquals("3", ((DataRecord) result.get( //$NON-NLS-1$
                            "cacdo_RegisterCustomsWarehouseDetails/cacdo_RegisterOrganizationDetails/ccdo_OrganizationDetails/csdo_CountryCode")).get("Code")); //$NON-NLS-1$
                    break;
                case "6"://$NON-NLS-1$
                    assertEquals("4", ((DataRecord) result.get( //$NON-NLS-1$
                            "cacdo_RegisterCustomsWarehouseDetails/cacdo_RegisterOrganizationDetails/ccdo_OrganizationDetails/csdo_CountryCode")).get("Code")); //$NON-NLS-1$
                    break;
                default:
                    assertNull(id);
                }
            }
        } finally {
            results.close();
        }
        storage.end();
        storage.close();
    }

    // TMDM-14560 Inheritance support composite key not well
    public void testDataModelWithInheritanceCompositeKey() {
        Storage storage = new SecuredStorage(new HibernateStorage("AddressCompositeKey", StorageType.MASTER), userSecurity); // $NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("AddressCompositeKey.xsd")); // $NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("AddressCompositeKey", repository); // $NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));// //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        storage.begin();
        ComplexTypeMetadata euAddress = repository.getComplexType("EUAddress"); // $NON-NLS-1$
        ComplexTypeMetadata address = repository.getComplexType("Address"); // $NON-NLS-1$
        UserQueryBuilder qb = from(euAddress);
        UserQueryBuilder qb_Address = from(address);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.end();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, euAddress, "<EUAddress><Line1>11</Line1><Line2>22</Line2><City>Paris</City><PostalCode>IY626</PostalCode><Country>Franch</Country></EUAddress>")); // $NON-NLS-1$
        records.add(factory.read(repository, euAddress, "<EUAddress><Line1>33</Line1><Line2>44</Line2><City>Munich</City><PostalCode>IY636</PostalCode><Country>Germany</Country></EUAddress>")); // $NON-NLS-1$
        records.add(factory.read(repository, address, "<Address><Line1>1</Line1><Line2>2</Line2><City>Beijing</City></Address>")); // $NON-NLS-1$
        records.add(factory.read(repository, address, "<Address><Line1>3</Line1><Line2>4</Line2><City>Shanghai</City></Address>")); // $NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
        } finally {
            results.close();
        }
        results = storage.fetch(qb_Address.getSelect());
        try {
            assertEquals(4, results.getCount());
            for (DataRecord result : results) {
                String id1 = result.get("Line1").toString();
                switch (id1) {
                case "1": // $NON-NLS-1$
                    assertEquals("Beijing", result.get("City").toString()); //$NON-NLS-1$ $NON-NLS-2$
                    break;
                case "3": // $NON-NLS-1$
                    assertEquals("Shanghai", result.get("City").toString()); //$NON-NLS-1$ $NON-NLS-2$
                    break;
                case "11": // $NON-NLS-1$
                    assertEquals("Paris", result.get("City").toString()); //$NON-NLS-1$ $NON-NLS-2$
                    break;
                case "33": // $NON-NLS-1$
                    assertEquals("Munich", result.get("City").toString()); //$NON-NLS-1$ $NON-NLS-2$
                    break;
                default:
                    assertNull(id1);
                }
            }
        } finally {
            results.close();
        }
        storage.end();
        storage.close();
    }

    public void testCreateWithInheritanceComplexType() {
        Storage storage = new SecuredStorage(new HibernateStorage("InheritanceComplexType", StorageType.MASTER), userSecurity);//$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("InheritanceComplexType.xsd"));//$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("InheritanceComplexType", repository);//$NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));// //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        //testing Add and Query record for Entity TestA, TestB and TestC
        doAddAndQueryWithEntityTestA(storage, repository);
        doAddAndQueryWithEntityTestB(storage, repository);
        doAddAndQueryWithEntityTestC(storage, repository);

        storage.close();
    }

    private void doAddAndQueryWithEntityTestB(Storage storage, MetadataRepository repository) {
        storage.begin();
        ComplexTypeMetadata testB = repository.getComplexType("TestB");//$NON-NLS-1$
        UserQueryBuilder qb = from(testB);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.end();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, testB, "<TestB><Id>3</Id><Person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"TeacherType\"><personName>personName33</personName><teacherName>teacherName33</teacherName></Person></TestB>")); //$NON-NLS-1$
        records.add(factory.read(repository, testB, "<TestB><Id>4</Id><Person xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"TeacherType\"><personName>personName44</personName><teacherName>teacherName44</teacherName></Person></TestB>")); //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            for (DataRecord result : results) {
                String id = result.get("Id").toString();
                switch (id) {
                case "3"://$NON-NLS-1$
                    assertEquals("personName33", result.get("Person/personName"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("teacherName33", result.get("Person/teacherName"));//$NON-NLS-1$ //$NON-NLS-2$
                    break;
                case "4"://$NON-NLS-1$
                    assertEquals("personName44", result.get("Person/personName"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("teacherName44", result.get("Person/teacherName"));//$NON-NLS-1$ //$NON-NLS-2$
                    break;
                default:
                    assertNull(id);
                }
            }
        } finally {
            results.close();
        }
        storage.end();
    }

    private void doAddAndQueryWithEntityTestC(Storage storage, MetadataRepository repository) {
        storage.begin();
        ComplexTypeMetadata testC = repository.getComplexType("TestC");//$NON-NLS-1$
        UserQueryBuilder qb = from(testC);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.end();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, testC, "<TestC><Id>5</Id><Student><personName>personName55</personName><studentName>studentName55</studentName></Student></TestC>")); //$NON-NLS-1$
        records.add(factory.read(repository, testC, "<TestC><Id>6</Id><Student><personName>personName66</personName><studentName>studentName66</studentName></Student></TestC>")); //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            for (DataRecord result : results) {
                String id = result.get("Id").toString();
                switch (id) {
                case "5"://$NON-NLS-1$
                    assertEquals("personName55", result.get("Student/personName"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("studentName55", result.get("Student/studentName"));//$NON-NLS-1$ //$NON-NLS-2$
                    break;
                case "6"://$NON-NLS-1$
                    assertEquals("personName66", result.get("Student/personName"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("studentName66", result.get("Student/studentName"));//$NON-NLS-1$ //$NON-NLS-2$
                    break;
                default:
                    assertNull(id);
                }
            }
        } finally {
            results.close();
        }
        storage.end();
    }

    private void doAddAndQueryWithEntityTestA(Storage storage, MetadataRepository repository) {
        storage.begin();
        ComplexTypeMetadata testA = repository.getComplexType("TestA");//$NON-NLS-1$
        UserQueryBuilder qb = from(testA);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.end();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, testA, "<TestA><Id>1</Id><Student><personName>myPersonName11</personName><studentName>myStudentName11</studentName></Student></TestA>")); //$NON-NLS-1$
        records.add(factory.read(repository, testA, "<TestA><Id>2</Id><Student><personName>myPersonName22</personName><studentName>myStudentName22</studentName></Student></TestA>")); //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            for (DataRecord result : results) {
                String id = result.get("Id").toString();
                switch (id) {
                case "1"://$NON-NLS-1$
                    assertEquals("myPersonName11", result.get("Student/personName"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("myStudentName11", result.get("Student/studentName"));//$NON-NLS-1$ //$NON-NLS-2$
                    break;
                case "2"://$NON-NLS-1$
                    assertEquals("myPersonName22", result.get("Student/personName"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("myStudentName22", result.get("Student/studentName"));//$NON-NLS-1$ //$NON-NLS-2$
                    break;
                default:
                    assertNull(id);
                }
            }

        } finally {
            results.close();
        }
        storage.end();
    }

    public void testCreateWithThreeLevelInheritanceComplexType() {
        Storage storage = new SecuredStorage(new HibernateStorage("InheritanceComplexType_1", StorageType.MASTER), userSecurity);//$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("InheritanceComplexType_1.xsd"));//$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("InheritanceComplexType_1", repository);//$NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));// //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        //testing Add and Query record for Entity City, Organization
        doAddAndQueryWithThreeLevelEntityCity(storage, repository);
        doAddAndQueryWithThreeLevelEntityOrganization(storage, repository);
        doDeleteAndQueryWithThreeLevelEntityOrganization(storage, repository);
        storage.close();
    }

    private void doDeleteAndQueryWithThreeLevelEntityOrganization(Storage storage, MetadataRepository repository) {
        storage.begin();
        ComplexTypeMetadata organization = repository.getComplexType("Organization");//$NON-NLS-1$
        ComplexTypeMetadata city = repository.getComplexType("City");//$NON-NLS-1$
        UserQueryBuilder qb = from(organization);
        UserQueryBuilder qb_city = from(city);
        StorageResults results = storage.fetch(qb.getSelect());
        StorageResults results_city = storage.fetch(qb_city.getSelect());
        try {
            assertEquals(2, results.getCount());
            assertEquals(2, results_city.getCount());
        } finally {
            results.close();
            results.close();
            storage.end();
        }

        try {
            storage.begin();
            storage.delete(qb.getSelect());
            storage.delete(qb_city.getSelect());
            storage.commit();
        } finally {
            storage.end();
        }

        results = storage.fetch(qb.getSelect());
        results_city = storage.fetch(qb_city.getSelect());
        try {
            assertEquals(0, results.getCount());
            assertEquals(0, results_city.getCount());
        } finally {
            results.close();
            storage.end();
        }
    }

    private void doAddAndQueryWithThreeLevelEntityOrganization(Storage storage, MetadataRepository repository) {
        storage.begin();
        ComplexTypeMetadata organization = repository.getComplexType("Organization");//$NON-NLS-1$
        UserQueryBuilder qb = from(organization);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.end();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, organization, "<Organization><org_id>11</org_id><address><street>part 1</street><city>[bj]</city><country><name>China</name><code>086</code></country></address></Organization>")); //$NON-NLS-1$
        records.add(factory.read(repository, organization, "<Organization><org_id>22</org_id><address><street>part 2</street><city>[hb]</city><country><name>China</name><code>088</code></country></address></Organization>")); //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            for (DataRecord result : results) {
                String id = result.get("org_id").toString();
                switch (id) {
                case "11"://$NON-NLS-1$
                    assertEquals("part 1", result.get("address/street"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("bj", ((DataRecord)result.get("address/city")).get("Code"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("China", result.get("address/country/name"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("086", result.get("address/country/code"));//$NON-NLS-1$ //$NON-NLS-2$
                    break;
                case "22"://$NON-NLS-1$
                    assertEquals("part 2", result.get("address/street"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("hb", ((DataRecord)result.get("address/city")).get("Code"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("China", result.get("address/country/name"));//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals("088", result.get("address/country/code"));//$NON-NLS-1$ //$NON-NLS-2$
                    break;
                default:
                    assertNull(id);
                }
            }
        } finally {
            results.close();
        }
        storage.end();
    }

    private void doAddAndQueryWithThreeLevelEntityCity(Storage storage, MetadataRepository repository) {
        storage.begin();
        ComplexTypeMetadata city = repository.getComplexType("City");//$NON-NLS-1$
        UserQueryBuilder qb = from(city);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.end();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, city, "<City><Code>bj</Code><Name>beijing</Name></City>")); //$NON-NLS-1$
        records.add(factory.read(repository, city, "<City><Code>hb</Code><Name>hebei</Name></City>"));   //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getCount());
            for (DataRecord result : results) {
                String id = result.get("Code").toString();
                switch (id) {
                case "bj"://$NON-NLS-1$
                    assertEquals("beijing", result.get("Name"));//$NON-NLS-1$ //$NON-NLS-2$
                    break;
                case "hb"://$NON-NLS-1$
                    assertEquals("hebei", result.get("Name"));//$NON-NLS-1$ //$NON-NLS-2$
                    break;
                default:
                    assertNull(id);
                }
            }
        } finally {
            results.close();
        }
        storage.end();
    }

    // TMDM-8022 custom decimal type totalDigits/fractionDigits is not considered while mapping to RDBMS db
    public void testDecimalComplexType() {
        Storage storage = new SecuredStorage(new HibernateStorage("Goods", StorageType.MASTER), userSecurity);//$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("GoodsDecimal.xml"));//$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("Goods", repository);//$NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));//    //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        storage.begin();
        ComplexTypeMetadata goods = repository.getComplexType("Goods");//$NON-NLS-1$
        UserQueryBuilder qb = from(goods);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.end();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        //scale is less than define, expect result will be enlarge to define scale length
        records.add(factory.read(repository, goods, "<Goods><Id>1</Id><Price>12.00</Price></Goods>")); //$NON-NLS-1$
        //scale is greater than define, expect result will be round to define scale length
        records.add(factory.read(repository, goods, "<Goods><Id>2</Id><Price>12.3588</Price></Goods>")); //$NON-NLS-1$
        records.add(factory.read(repository, goods, "<Goods><Id>3</Id><Price>12.3584</Price></Goods>")); //$NON-NLS-1$
        //for the number,scale is greater than define, expect result will be round to define scale length
        records.add(factory.read(repository, goods, "<Goods><Id>4</Id><Price>-2.02365</Price></Goods>")); //$NON-NLS-1$
        records.add(factory.read(repository, goods, "<Goods><Id>5</Id><Price>-2.0232</Price></Goods>")); //$NON-NLS-1$
        //for the max number and min number, will be insert into correctly.
        records.add(factory.read(repository, goods, "<Goods><Id>6</Id><Price>999999999999.999</Price></Goods>")); //$NON-NLS-1$
        records.add(factory.read(repository, goods, "<Goods><Id>7</Id><Price>-999999999999.999</Price></Goods>")); //$NON-NLS-1$

        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(7, results.getCount());
            for (DataRecord result : results) {
                if (result.get("Id").equals("1")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("12.000").doubleValue(), ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ //$NON-NLS-2$
                }
                if (result.get("Id").equals("2")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("12.359").doubleValue(), ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ //$NON-NLS-2$
                }
                if (result.get("Id").equals("3")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("12.358").doubleValue(), ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ //$NON-NLS-2$
                }
                if (result.get("Id").equals("4")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("-2.024").doubleValue(), ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ //$NON-NLS-2$
                }
                if (result.get("Id").equals("5")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("-2.023").doubleValue(), ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ //$NON-NLS-2$
                }
                if (result.get("Id").equals("6")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("999999999999.999").doubleValue(),//$NON-NLS-1$ 
                            ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ 
                }
                if (result.get("Id").equals("7")) {//$NON-NLS-1$ //$NON-NLS-2$
                    assertEquals(Double.valueOf("-999999999999.999").doubleValue(),//$NON-NLS-1$ 
                            ((BigDecimal) result.get("Price")).doubleValue());//$NON-NLS-1$ 
                }
            }

        } finally {
            results.close();
        }
        storage.end();

        try {
            storage.begin();
            {
                qb = from(goods);
                storage.delete(qb.getSelect());
            }
            storage.commit();
        } finally {
            storage.end();
        }

        //greater than the max number, failed to insert.
        records.add(factory.read(repository, goods, "<Goods><Id>8</Id><Price>1000000000000.000</Price></Goods>"));//$NON-NLS-1$ 
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
            fail("could not execute batch");//$NON-NLS-1$ 
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        } finally {
            storage.end();
        }

        //less than the min numnber, failed to insert.
        records.add(factory.read(repository, goods, "<Goods><Id>9</Id><Price>-1000000000000.000</Price></Goods>"));//$NON-NLS-1$ 
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
            fail("could not execute batch");//$NON-NLS-1$ 
        } catch (Exception e) {
            assertTrue(e instanceof RuntimeException);
        } finally {
            storage.end();
        }

        storage.close();
    }

    public void testStorageAfterFetchClassLoader() {
        Storage storage = new SecuredStorage(new HibernateStorage("Goods", StorageType.MASTER), userSecurity);//$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("GoodsDecimal.xml"));//$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("Goods", repository);//$NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));//    //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        storage.begin();
        ComplexTypeMetadata goods = repository.getComplexType("Goods");//$NON-NLS-1$
        UserQueryBuilder qb = from(goods);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
            for (DataRecord result : results) {
                break;
            }
        } finally {
            results.close();
        }
        storage.end();
        ClassLoader StorageClassLoader = (ClassLoader) Thread.currentThread().getContextClassLoader();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, goods, "<Goods><Id>1</Id><Price>12.00</Price></Goods>")); //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                break;
            }

        } finally {
            results.close();
        }
        storage.end();
        ClassLoader StorageClassLoader2 = (ClassLoader) Thread.currentThread().getContextClassLoader();
        assertEquals(StorageClassLoader, StorageClassLoader2);
    }

    // TMDM-10222
    public void testInClauseHandle() {
        Storage storage = new SecuredStorage(new HibernateStorage("Features", StorageType.MASTER), userSecurity);//$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("Features.xsd"));//$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("Features", repository);//$NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));//    //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        ComplexTypeMetadata factFacturesStg = repository.getComplexType("FactFacturesStg");//$NON-NLS-1$
        ComplexTypeMetadata Factures = repository.getComplexType("Factures");//$NON-NLS-1$

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        // scale is less than define, expect result will be enlarge to define scale length
        records.add(factory
                .read(repository,
                        factFacturesStg,
                        "<FactFacturesStg><CodeEntiteFact>1</CodeEntiteFact><CodeServiceFact>1</CodeServiceFact><NoDocument>1</NoDocument><DateDocument>2016-07-20</DateDocument><CycleDocument>1</CycleDocument><OrigineDocument>1</OrigineDocument><FluxDocument>1</FluxDocument><RefTiersPrestation>1</RefTiersPrestation><LignesFacturesStg><IdLigne>1</IdLigne></LignesFacturesStg><TvaFacturesStg><IdLigneTva>1</IdLigneTva></TvaFacturesStg></FactFacturesStg>")); //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }

        UserQueryBuilder qb = from(factFacturesStg).where(eq(factFacturesStg.getField("CodeEntiteFact"), "1"));
        qb.getSelect().getPaging().setLimit(10);
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("CodeServiceFact"));//$NON-NLS-1$ //$NON-NLS-2$
            }

        } finally {
            results.close();
        }
        storage.end();

        qb = from(factFacturesStg);
        try {
            storage.delete(qb.getSelect());
            storage.commit();
        } finally {
            storage.close();
        }
    }

    // TMDM-14399 [REST Api] PUT /data/{containerName}/query : issue when sort & paging (order_by & (start limit) )
    public void testQueryWithSortHandle() {
        Storage storage = new SecuredStorage(new HibernateStorage("Goods", StorageType.MASTER), userSecurity); //$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("GoodsDecimal.xml")); //$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("Goods", repository); //$NON-NLS-1$

        storage.init(getDatasource("H2-DS3")); //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        storage.begin();
        ComplexTypeMetadata goods = repository.getComplexType("Goods"); //$NON-NLS-1$
        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, goods, "<Goods><Id>1</Id><Price>12.00</Price></Goods>")); //$NON-NLS-1$
        records.add(factory.read(repository, goods, "<Goods><Id>2</Id><Price>3.00</Price></Goods>")); //$NON-NLS-1$
        records.add(factory.read(repository, goods, "<Goods><Id>3</Id><Price>15.00</Price></Goods>")); //$NON-NLS-1$
        records.add(factory.read(repository, goods, "<Goods><Id>4</Id><Price>5.00</Price></Goods>")); //$NON-NLS-1$
        records.add(factory.read(repository, goods, "<Goods><Id>5</Id><Price>2.00</Price></Goods>")); //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }
        FieldMetadata price = goods.getField("Price");
        UserQueryBuilder qb = from(goods).orderBy(price, OrderBy.Direction.DESC);
        qb.getSelect().getPaging().setLimit(10);
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(5, results.getCount());
            String[] ids = new String[5];
            int index = 0;
            for (DataRecord result : results) {
                ids[index++] = result.get("Id").toString();
            }
            Assert.assertArrayEquals(new String[] { "3", "1", "4", "2", "5" }, ids);
        } finally {
            results.close();
        }
        storage.end();

        // only return field Id, order by Price ASC, start 2 and limit 3
        qb = from(goods).select(goods.getField("Id")).orderBy(price, OrderBy.Direction.ASC);
        qb.getSelect().getPaging().setStart(2);
        qb.getSelect().getPaging().setLimit(3);
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(5, results.getCount());
            String[] ids = new String[3];
            int index = 0;
            for (DataRecord result : results) {
                ids[index++] = result.get("Id").toString();
                try {
                    result.get("Price").toString();
                    fail("could not execute get");//$NON-NLS-1$
                } catch (Exception e) {
                    assertTrue(e instanceof NullPointerException);
                }
            }
            Assert.assertArrayEquals(new String[] { "4", "1", "3" }, ids);
        } finally {
            results.close();
        }
        storage.end();
    }

    // TMDM-14115 Deploy the customer's datamodel failed(MS SQLServerDB)
    public void testDefaultValuePrepare() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS2", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.MASTER); //$NON-NLS-1$
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("EU_PRDMDM_eBomChild.xsd")); //$NON-NLS-1$
        storage.prepare(repository, true);
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        ComplexTypeMetadata EU_PRDMDM_eBomChild = repository.getComplexType("EU_PRDMDM_eBomChild"); //$NON-NLS-1$
        // test data had been added
        List<DataRecord> records = new ArrayList<DataRecord>();
        records.add(factory.read(repository, EU_PRDMDM_eBomChild, "<EU_PRDMDM_eBomChild><Id>1</Id><Quantity>23.000000</Quantity><StartDate>2100-01-01</StartDate><EndDate>2100-11-28</EndDate></EU_PRDMDM_eBomChild>")); // $NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } catch (Exception e) {
            fail("Faield to insert data");
        } finally {
            storage.end();
        }
        // test query data
        UserQueryBuilder qb = from(EU_PRDMDM_eBomChild);
        qb.getSelect().getPaging().setLimit(10);
        storage.begin();
        StorageResults results = storage.fetch(qb.getSelect());
        SimpleDateFormat format = new SimpleDateFormat("YYYY-MM-dd"); //$NON-NLS-1$
        try {
            assertEquals(1, results.getCount());
            for (DataRecord result : results) {
                assertEquals("1", result.get("Id")); //$NON-NLS-1$ $NON-NLS-2$
                assertEquals("2100-01-01", format.format(result.get("StartDate"))); //$NON-NLS-1$ $NON-NLS-2$
                assertEquals("2100-11-28", format.format(result.get("EndDate"))); //$NON-NLS-1$ $NON-NLS-2$
            }
        } finally {
            results.close();
        }
        storage.end();
        try {
            storage.begin();
            {
                qb = from(EU_PRDMDM_eBomChild);
                storage.delete(qb.getSelect());
            }
            storage.commit();
        } finally {
            storage.end();
        }
    }

    // TMDM-10861
    public void testStagingHasTask() throws Exception {
        DataSourceDefinition dataSource = ServerContext.INSTANCE.get().getDefinition("H2-DS2", STORAGE_NAME);
        Storage storage = new HibernateStorage("Test", StorageType.STAGING);
        storage.init(dataSource);
        MetadataRepository repository = new MetadataRepository();
        repository.load(StorageAdaptTest.class.getResourceAsStream("schema1_1.xsd"));
        storage.prepare(repository, true);

        DataSource staging = dataSource.getStaging();
        assertTrue(staging instanceof RDBMSDataSource);
        RDBMSDataSource rdbmsDataSource = (RDBMSDataSource) staging;
        assertEquals(RDBMSDataSource.DataSourceDialect.H2, rdbmsDataSource.getDialectName());
        Connection connection = DriverManager.getConnection(rdbmsDataSource.getConnectionURL());
        Statement statement = connection.createStatement();
        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM PRODUCT");
            ResultSetMetaData metaData = resultSet.getMetaData();
            boolean hasTask = false;
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                hasTask |= "x_talend_staging_hastask".equalsIgnoreCase(metaData.getColumnName(i));
            }
            assertTrue(hasTask);
        } finally {
            statement.close();
            connection.close();
        }
    }

    //TMDM-13811
    public void testDeleteMultiLevelRef() {
        Storage storage = new SecuredStorage(new HibernateStorage("MultiLevelRefRTE", StorageType.MASTER), userSecurity);//$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("MultiLevelRefRTE.xsd"));//$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("MultiLevelRefRTE", repository);//$NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));// //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        DataRecordMetadataImpl drm = new DataRecordMetadataImpl(new Date().getTime(), "123456");
        ComplexTypeMetadata entityD = repository.getComplexType("EntityD");//$NON-NLS-1$
        DataRecord dr = new DataRecord(entityD, drm);
        StorageDocument storageDoc = new StorageDocument("MultiLevelRefRTE", repository, dr);
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        DataRecord dataRecord = factory.read(repository, entityD,
                "<EntityD><Id>22</Id><NameEda>22</NameEda><RefEntityC>[33]</RefEntityC><EntityB>[11]</EntityB></EntityD>"); //$NON-NLS-1$
        dataRecord = storageDoc.cleanMultiLevelRef(dataRecord);
        assertEquals("33", ((LinkedList<DataRecord>) dataRecord.get("RefEntityC")).get(0).get("Id"));
        assertEquals("11", ((DataRecord) dataRecord.get("EntityB")).get("Id"));

        drm = new DataRecordMetadataImpl(new Date().getTime(), "123456");
        ComplexTypeMetadata entityC = repository.getComplexType("EntityC");//$NON-NLS-1$
        dr = new DataRecord(entityC, drm);
        storageDoc = new StorageDocument("MultiLevelRefRTE", repository, dr);
        dataRecord = factory.read(repository, entityC,
                "<EntityC><Id>33</Id><BaseType xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"TeacherType\"><TeacherCircleType><entitesPassees><EDAs><EDA><eda>[22]</eda><dateDebutApplication>2019-08-21</dateDebutApplication></EDA></EDAs></entitesPassees></TeacherCircleType></BaseType></EntityC>"); //$NON-NLS-1$
        dataRecord = storageDoc.cleanMultiLevelRef(dataRecord);
        assertNotNull(dataRecord);
        assertEquals("22",
                ((DataRecord) ((LinkedList<DataRecord>) ((DataRecord) ((DataRecord) ((DataRecord) ((DataRecord) dataRecord
                        .get("BaseType")).get("TeacherCircleType")).get("entitesPassees")).get("EDAs")).get("EDA")).get(0)
                                .get("eda")).get("Id"));
    }

    //TMDM-14012 XPath functions cannot be used on default values
    public void testParseXPathWithDefaultValueInDataModel() {
        Storage storage = new SecuredStorage(new HibernateStorage("EEC_Default_Value", StorageType.MASTER), userSecurity);//$NON-NLS-1$
        MetadataRepository repository = new MetadataRepository();
        repository.load(StoragePrepareTest.class.getResourceAsStream("EEC_Default_Value.xsd"));//$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register("EEC_Default_Value", repository);//$NON-NLS-1$

        storage.init(getDatasource("H2-DS3"));// //$NON-NLS-1$
        storage.prepare(repository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(storage);

        storage.begin();
        ComplexTypeMetadata country = repository.getComplexType("Country");//$NON-NLS-1$
        UserQueryBuilder qb = from(country);
        StorageResults results = storage.fetch(qb.getSelect());
        try {
            assertEquals(0, results.getCount());
        } finally {
            results.close();
        }
        storage.end();

        List<DataRecord> records = new ArrayList<DataRecord>();
        DataRecordReader<String> factory = new XmlStringDataRecordReader();
        records.add(factory.read(repository, country, "<Country><Code>1</Code><ImportToBelarus></ImportToBelarus><ImportToString></ImportToString></Country>")); //$NON-NLS-1$
        records.add(factory.read(repository, country, "<Country><Code>2</Code><ImportToBelarus>false</ImportToBelarus><ImportToString>hello world</ImportToString></Country>")); //$NON-NLS-1$
        records.add(factory.read(repository, country, "<Country><Code>3</Code><ImportToBelarus>true</ImportToBelarus><ImportToString>very good</ImportToString></Country>")); //$NON-NLS-1$
        records.add(factory.read(repository, country, "<Country><Code>4</Code><ImportToBelarus>false</ImportToBelarus><ImportToString>This is a test</ImportToString></Country>")); //$NON-NLS-1$
        try {
            storage.begin();
            storage.update(records);
            storage.commit();
        } finally {
            storage.end();
        }
        storage.begin();
        results = storage.fetch(qb.getSelect());
        try {
            assertEquals(4, results.getCount());
            for (DataRecord result : results) {
                String id = result.get("Code").toString();
                switch (id) {
                case "1"://$NON-NLS-1$
                    assertEquals(false, result.get("Country/ImportToBelarus"));//$NON-NLS-1$
                    assertEquals(null, result.get("Country/ImportToString"));//$NON-NLS-1$
                    break;
                case "2"://$NON-NLS-1$
                    assertEquals(false, result.get("Country/ImportToBelarus"));//$NON-NLS-1$
                    assertEquals("hello world", result.get("Country/ImportToString"));//$NON-NLS-1$ $NON-NLS-2$
                    break;
                case "3"://$NON-NLS-1$
                    assertEquals(true, result.get("Country/ImportToBelarus"));//$NON-NLS-1$
                    assertEquals("very good", result.get("Country/ImportToString"));//$NON-NLS-1$ $NON-NLS-2$
                    break;
                case "4"://$NON-NLS-1$
                    assertEquals(false, result.get("Country/ImportToBelarus"));//$NON-NLS-1$
                    assertEquals("This is a test", result.get("Country/ImportToString"));//$NON-NLS-1$ $NON-NLS-2$
                    break;
                default:
                    assertNull(id);
                }
            }
        } finally {
            results.close();
        }
        storage.end();
        storage.close();
    }

    protected static DataSourceDefinition getDatasource(String dataSourceName) {
        return ServerContext.INSTANCE.get().getDefinition(dataSourceName, "MDM");
    }
    protected static class MockUserDelegator implements SecuredStorage.UserDelegator {

        boolean isActive = true;

        public void setActive(boolean active) {
            isActive = active;
        }

        @Override
        public boolean hide(FieldMetadata field) {
            return isActive && field.getHideUsers().contains("System_Users");
        }

        @Override
        public boolean hide(ComplexTypeMetadata type) {
            return isActive && type.getHideUsers().contains("System_Users");
        }
    }
}