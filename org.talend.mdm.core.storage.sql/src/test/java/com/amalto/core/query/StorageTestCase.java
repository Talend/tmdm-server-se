/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.query;

import static com.amalto.core.query.user.UserQueryBuilder.*;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.hibernate.HibernateStorage;

@SuppressWarnings("nls")
public class StorageTestCase extends TestCase {

    private static Logger LOG = Logger.getLogger(StorageTestCase.class);

    protected static final Storage systemStorage;

    protected static final Storage storage;

    protected static final MetadataRepository systemRepository;

    protected static final MetadataRepository repository;

    protected static final ComplexTypeMetadata product;

    protected static final ComplexTypeMetadata ff;

    protected static final ComplexTypeMetadata productFamily;

    protected static final ComplexTypeMetadata store;

    protected static final ComplexTypeMetadata supplier;

    protected static final ComplexTypeMetadata type;

    protected static final ComplexTypeMetadata person;
    
    protected static final ComplexTypeMetadata dateKey;

    protected static final ComplexTypeMetadata customer;

    protected static final ComplexTypeMetadata address;

    protected static final ComplexTypeMetadata country;

    protected static final ComplexTypeMetadata countryLong;

    protected static final ComplexTypeMetadata countryShort;

    protected static final ComplexTypeMetadata a;

    protected static final ComplexTypeMetadata b;

    protected static final ComplexTypeMetadata c;

    protected static final ComplexTypeMetadata d;

    protected static final ComplexTypeMetadata e;

    protected static final ComplexTypeMetadata e2;

    protected static final ComplexTypeMetadata e1;

    protected static final ComplexTypeMetadata updateReport;

    protected static final ComplexTypeMetadata persons;

    protected static final ComplexTypeMetadata employee;

    protected static final ComplexTypeMetadata manager;

    protected static final ComplexTypeMetadata ss;

    protected static final ComplexTypeMetadata employee1;

    protected static final ComplexTypeMetadata manager1;

    protected static final ComplexTypeMetadata company;

    protected static final ComplexTypeMetadata entityA;

    protected static final ComplexTypeMetadata entityB;

    protected static final ComplexTypeMetadata ContainedEntityA;

    protected static final ComplexTypeMetadata ContainedEntityB;

    protected static final ComplexTypeMetadata ContainedEntityC;

    protected static final ComplexTypeMetadata PointToSelfEntity;

    protected static final ComplexTypeMetadata organization;

    protected static final ComplexTypeMetadata city;

    protected static final ComplexTypeMetadata repeatableElementsEntity;

    protected static final ComplexTypeMetadata tt;

    protected static final ComplexTypeMetadata rr;

    protected static final ComplexTypeMetadata compte;

    protected static TestUserDelegator userSecurity = new TestUserDelegator();

    protected static final ComplexTypeMetadata contexte;

    protected static final ComplexTypeMetadata personne;

    protected static final ComplexTypeMetadata hierarchy;

    protected static final ComplexTypeMetadata cpo_service;

    protected static final ComplexTypeMetadata location;

    protected static final ComplexTypeMetadata organisation;
    
    protected static final ComplexTypeMetadata e_entity;

    protected static final ComplexTypeMetadata t_entity;
    
    protected static final ComplexTypeMetadata checkPointDetails_1;
    
    protected static final ComplexTypeMetadata checkPointDetails_2;
    
    protected static final ComplexTypeMetadata fullTextSearchEntityA;

    protected static final ComplexTypeMetadata channel;

    protected static final ComplexTypeMetadata party;

    protected static final ComplexTypeMetadata mm;

    protected static final ComplexTypeMetadata nn;

    public static final String DATABASE = "H2";
    
    public static final String DATASOURCE_DEFAULT = DATABASE + "-Default";
    
    public static final String DATASOURCE_FULLTEXT = DATABASE + "-Fulltext";

    static {
        LOG.info("Setting up MDM server environment...");
        ServerContext.INSTANCE.get(new MockServerLifecycle());
        LOG.info("MDM server environment set.");

        storage = new SecuredStorage(new HibernateStorage("MDM"), userSecurity);
        repository = new MetadataRepository();
        repository.load(StorageQueryTest.class.getResourceAsStream("metadata.xsd"));

        type = repository.getComplexType("TypeA");
        person = repository.getComplexType("Person");
        dateKey = repository.getComplexType("DateKey");
        customer = repository.getComplexType("Customer");
        address = repository.getComplexType("Address");
        country = repository.getComplexType("Country");
        countryLong = repository.getComplexType("CountryLong");
        countryShort = repository.getComplexType("CountryShort");
        product = repository.getComplexType("Product");
        productFamily = repository.getComplexType("ProductFamily");
        store = repository.getComplexType("Store");
        supplier = repository.getComplexType("Supplier");
        a = repository.getComplexType("A");
        b = repository.getComplexType("B");
        c = repository.getComplexType("C");
        d = repository.getComplexType("D");
        e = repository.getComplexType("E");
        updateReport = repository.getComplexType("Update");
        ff = repository.getComplexType("ff");
        e2 = repository.getComplexType("E2");
        e1 = repository.getComplexType("E1");
        ss = repository.getComplexType("SS");
        persons = repository.getComplexType("Persons");
        employee = repository.getComplexType("Employee");
        manager = repository.getComplexType("Manager");
        employee1 = repository.getComplexType("Employee1");
        manager1 = repository.getComplexType("Manager1");
        company = repository.getComplexType("Company");
        entityA = repository.getComplexType("EntityA");
        entityB = repository.getComplexType("EntityB");
        ContainedEntityA = repository.getComplexType("ContainedEntityA");
        ContainedEntityB = repository.getComplexType("ContainedEntityB");
        ContainedEntityC = repository.getComplexType("ContainedEntityC");
        PointToSelfEntity = repository.getComplexType("PointToSelfEntity");
        organization = repository.getComplexType("Organization");
        city = repository.getComplexType("City");
        repeatableElementsEntity = repository.getComplexType("RepeatableElementsEntity");
        tt = repository.getComplexType("TT");
        rr = repository.getComplexType("RR");
        compte = repository.getComplexType("Compte");
        contexte = repository.getComplexType("Contexte");
        personne = repository.getComplexType("Personne");
        hierarchy = repository.getComplexType("HierarchySearchItem");
        cpo_service = repository.getComplexType("cpo_service");
        location = repository.getComplexType("Location");
        organisation = repository.getComplexType("Organisation");
        e_entity = repository.getComplexType("E_Entity");
        t_entity = repository.getComplexType("T_Entity");        
        checkPointDetails_1 = repository.getComplexType("CheckPointDetails_1");
        checkPointDetails_2 = repository.getComplexType("CheckPointDetails_2");        
        fullTextSearchEntityA = repository.getComplexType("FullTextSearchEntityA");
        
        channel = repository.getComplexType("MYLOOKUP_BUSINESS_CHANNEL");
        party = repository.getComplexType("MYPARTY");

        mm = repository.getComplexType("MM");
        nn = repository.getComplexType("NN");

        systemStorage = new SecuredStorage(new HibernateStorage("MDM", StorageType.SYSTEM), userSecurity);
        systemRepository = buildSystemRepository();
        
        initStorage(DATASOURCE_DEFAULT);
    }
    
    protected static void initStorage(String datasource) {
        LOG.info("Preparing storage for tests...");
        storage.init(getDatasource(datasource));
        // Indexed expressions
        List<Expression> indexedExpressions = new LinkedList<Expression>();
        indexedExpressions.add(UserQueryBuilder.from(person).where(isNull(person.getField("firstname"))).getExpression());
        indexedExpressions.add(UserQueryBuilder.from(person).where(isNull(person.getField("score"))).getExpression());
        indexedExpressions.add(UserQueryBuilder.from(person).where(isNull(address.getField("score"))).getExpression());
        indexedExpressions.add(UserQueryBuilder.from(person).where(isNull(product.getField("Stores/Store"))).getExpression());
        indexedExpressions.add(UserQueryBuilder.from(country).where(isNull(country.getField("notes/comment"))).getExpression());
        indexedExpressions.add(UserQueryBuilder.from(d).where(isNull(d.getField("commonText"))).getExpression());
        indexedExpressions.add(UserQueryBuilder.from(e).where(isNull(e.getField("commonText"))).getExpression());
        storage.prepare(repository, new HashSet<Expression>(indexedExpressions), true, true);
        LOG.info("Storage prepared.");

        LOG.info("Preparing system storage");
        systemStorage.init(getDatasource(datasource));
        systemStorage.prepare(systemRepository, new HashSet<Expression>(), true, true);
        LOG.info("System storage prepared");
    }

    private static ClassRepository buildSystemRepository() {
        ClassRepository repository = new ClassRepository();
        Class[] objectsToParse = new Class[ObjectPOJO.OBJECT_TYPES.length];
        int i = 0;
        for (Object[] objects : ObjectPOJO.OBJECT_TYPES) {
            objectsToParse[i++] = (Class) objects[1];
        }
        repository.load(objectsToParse);
        return repository;
    }

    private ClassLoader previous;

    protected static DataSourceDefinition getDatasource(String dataSourceName) {
        return ServerContext.INSTANCE.get().getDefinition(dataSourceName, "MDM");
    }

    public void test() throws Exception {
        // Just there so JUnit does not complain about a test case that has no test.
    }

    @Override
    public void setUp() throws Exception {
        previous = Thread.currentThread().getContextClassLoader();
        storage.begin();
    }

    @Override
    public void tearDown() throws Exception {
        storage.commit();
        assertTrue(previous == Thread.currentThread().getContextClassLoader());
    }

    protected static class TestUserDelegator implements SecuredStorage.UserDelegator {

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
