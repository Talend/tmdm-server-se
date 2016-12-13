/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.save;

import static com.amalto.core.query.user.UserQueryBuilder.from;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.save.RecordValidationTest.MockAdmin;
import com.amalto.core.save.RecordValidationTest.MockSaverSource;
import com.amalto.core.save.RecordValidationTest.MockUserDelegator;
import com.amalto.core.save.context.DocumentSaver;
import com.amalto.core.save.generator.AutoIncrementGenerator;
import com.amalto.core.save.generator.StorageAutoIncrementGenerator;
import com.amalto.core.server.MockMetadataRepositoryAdmin;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.MockStorageAdmin;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.util.Util;

@SuppressWarnings("nls")
public class AutoIncrementTest extends TestCase {
    
    private static final Logger LOG = Logger.getLogger(AutoIncrementTest.class);
    
    protected static final Storage systemStorage;
    
    protected static final Storage masterStorage;
    
    protected static final MetadataRepository systemRepository;
    
    protected static final MetadataRepository masterRepository;
    
    protected static MockUserDelegator userSecurity = new MockUserDelegator();
    
    protected static final ComplexTypeMetadata typeAutoIncrement;
    
    protected static final ComplexTypeMetadata typeA;

    protected static final ComplexTypeMetadata typeB;

    protected static final ComplexTypeMetadata typeC;

    public static final String DATASOURCE = "H2-Fulltext";

    static {
        LOG.info("Setting up MDM server environment...");
        ServerContext.INSTANCE.get(new MockServerLifecycle());
        LOG.info("MDM server environment set.");
        
        LOG.info("Preparing system storage");
        systemStorage = new SecuredStorage(new HibernateStorage("__SYSTEM", StorageType.SYSTEM), userSecurity);
        systemRepository = buildSystemRepository();
        MockMetadataRepositoryAdmin.INSTANCE.register("__SYSTEM", systemRepository);
        typeAutoIncrement = systemRepository.getComplexType("AutoIncrement");
        systemStorage.init(ServerContext.INSTANCE.get().getDefinition(DATASOURCE, "TestAI"));
        systemStorage.prepare(systemRepository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(systemStorage);
        LOG.info("System storage prepared");
        
        LOG.info("Preparing master storage");
        masterStorage = new SecuredStorage(new HibernateStorage("TestAI", StorageType.MASTER), userSecurity);
        masterRepository = new MetadataRepository();
        masterRepository.load(RecordValidationTest.class.getResourceAsStream("../save/TestAI.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("TestAI", masterRepository);
        typeA = masterRepository.getComplexType("A");
        typeB = masterRepository.getComplexType("B");
        typeC = masterRepository.getComplexType("C");
        masterStorage.init(ServerContext.INSTANCE.get().getDefinition(DATASOURCE, "TestAI"));
        masterStorage.prepare(masterRepository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(masterStorage);
        LOG.info("Master storage prepared");
        
        BeanDelegatorContainer.createInstance();
    }
    
    @SuppressWarnings("rawtypes")
    private static ClassRepository buildSystemRepository() {
        ClassRepository repository = new ClassRepository();
        Class[] objectsToParse = new Class[ObjectPOJO.OBJECT_TYPES.length];
        int i = 0;
        for (Object[] objects : ObjectPOJO.OBJECT_TYPES) {
            objectsToParse[i++] = (Class) objects[1];
        }
        repository.load(objectsToParse);
        String[] models = new String[] { "/com/amalto/core/initdb/data/datamodel/CONF" };
        for (String model : models) {
            InputStream builtInStream = RecordValidationTest.class.getResourceAsStream(model);
            if (builtInStream == null) {
                throw new RuntimeException("Built in model '" + model + "' cannot be found.");
            }
            try {
                DataModelPOJO modelPOJO = ObjectPOJO.unmarshal(DataModelPOJO.class, IOUtils.toString(builtInStream, "UTF-8"));
                repository.load(new ByteArrayInputStream(modelPOJO.getSchema().getBytes("UTF-8")));
            } catch (Exception e) {
                throw new RuntimeException("Could not parse builtin data model '" + model + "'.", e);
            } finally {
                try {
                    builtInStream.close();
                } catch (IOException e) {
                    // Ignored
                }
            }
        }
        return repository;
    }
    
    protected void cleanAutoIncrement(){
        try {
            systemStorage.begin();
            systemStorage.delete(from(typeAutoIncrement).getSelect());
            systemStorage.commit();
        } finally {
            systemStorage.end();
        }
    }
    
    protected void cleanTestAIData(){
        try {
            masterStorage.begin();
            masterStorage.delete(from(typeA).getSelect());
            masterStorage.commit();
        } finally {
            masterStorage.end();
        }
    }
    
    // Create test data
    protected static void createData(String storageName, String documentXml) throws Exception {
        BeanDelegatorContainer.getInstance().setDelegatorInstancePool(Collections.<String, Object> singletonMap("LocalUser", new MockAdmin()));
        SaverSession session = SaverSession.newSession(new MockSaverSource(masterRepository, true));
        InputStream is = new ByteArrayInputStream(documentXml.getBytes("UTF-8"));
        Util.getDataClusterCtrlLocal().putDataCluster(new DataClusterPOJO("CONF"));
        Util.getDataClusterCtrlLocal().putDataCluster(new DataClusterPOJO("TestAI"));
        DocumentSaverContext context = session.getContextFactory().create(storageName, storageName, StringUtils.EMPTY, is, true, false, false, false, false);
        DocumentSaver saver = context.createSaver();
        try {
            session.begin(storageName);
            saver.save(session, context);
            session.end();
        } catch (Exception e) {
            session.abort();
            throw e;
        }
    }
    
    // B and C extend A, the concept for AutoIncrement should all be "A"
    public void testGetConceptForAutoIncrement() throws Exception {
        String[] conceptNames = { "A.Id", "B.Id", "C.Id", "A", "B", "C" };
        for (String conceptName : conceptNames) {
            assertEquals("A", AutoIncrementGenerator.getConceptForAutoIncrement("TestAI", conceptName));
        }
    }
    
    public void testStorageGenerateId() throws Exception {
        StorageAutoIncrementGenerator saig = new StorageAutoIncrementGenerator(systemStorage);
        String idA = saig.generateId("TestAI", "A", "Id");
        assertEquals("1", idA);
        String idB = saig.generateId("TestAI", "B", "Id");
        assertEquals("2", idB);
        String idC = saig.generateId("TestAI", "C", "Id");
        assertEquals("3", idC);
        
        cleanAutoIncrement();
    }
    
    // TMDM-9414
    @SuppressWarnings("unchecked")
    public void testPolymorphismAndAutoIncrement() throws Exception {
        // Create test data
        createData("TestAI", "<A></A>"); // Id=1
        createData("TestAI", "<B><NameB>NameB</NameB></B>"); // Id=2
        createData("TestAI", "<C><NameC>NameC</NameC></C>"); // Id=3
        
        // Validate AutoIncrement
        UserQueryBuilder qb1 = from(typeAutoIncrement);
        StorageResults results1 = systemStorage.fetch(qb1.getSelect());
        DataRecord autoIncrementRecord = results1.iterator().next();       
        FieldMetadata entryField = typeAutoIncrement.getField("entry"); 
        ComplexTypeMetadata entryType = (ComplexTypeMetadata) entryField.getType();
        DataRecord entry = ((List<DataRecord>) autoIncrementRecord.get(entryField)).get(0);
        assertEquals("TestAI.A.Id", entry.get(entryType.getField("key")));
        assertEquals(3, entry.get(entryType.getField("value")));
        
        cleanAutoIncrement();
        
        // Validate ID
        UserQueryBuilder qb2 = from(typeA);
        StorageResults results2 = masterStorage.fetch(qb2.getSelect());
        for (DataRecord result : results2) {
            if("A".equals(result.getType().getName())) {
                assertEquals("1", result.get("Id"));
            } else if("B".equals(result.getType().getName())) {
                assertEquals("2", result.get("Id"));
            } else {
                assertEquals("3", result.get("Id"));
            }
        }
        
        cleanTestAIData();
        
    }

}
