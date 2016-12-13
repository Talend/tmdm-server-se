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
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.utils.json.JSONException;
import org.talend.utils.json.JSONObject;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.history.MutableDocument;
import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.OrderBy;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.save.SaverSession.Committer;
import com.amalto.core.save.context.DocumentSaver;
import com.amalto.core.save.context.StorageSaverSource;
import com.amalto.core.server.MockMetadataRepositoryAdmin;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.MockStorageAdmin;
import com.amalto.core.server.ServerContext;
import com.amalto.core.storage.SecuredStorage;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.storage.StorageType;
import com.amalto.core.storage.datasource.DataSourceDefinition;
import com.amalto.core.storage.hibernate.HibernateStorage;
import com.amalto.core.storage.record.DataRecord;
import com.amalto.core.util.OutputReport;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

@SuppressWarnings("nls")
public class RecordValidationTest extends TestCase {

    private static final Logger LOG = Logger.getLogger(RecordValidationTest.class);

    protected static final Storage systemStorage;

    protected static final Storage masterStorage;

    protected static final Storage stagingStorage;

    protected static final MetadataRepository systemRepository;

    protected static final MetadataRepository productRepository;

    protected static MockUserDelegator userSecurity = new MockUserDelegator();

    protected static final ComplexTypeMetadata product;

    protected static final ComplexTypeMetadata store;

    protected static final ComplexTypeMetadata productFamily;

    public static final String DATASOURCE = "H2-Fulltext";

    static {
        LOG.info("Setting up MDM server environment...");
        ServerContext.INSTANCE.get(new MockServerLifecycle());
        LOG.info("MDM server environment set.");

        LOG.info("Preparing system storage");
        systemStorage = new SecuredStorage(new HibernateStorage("__SYSTEM", StorageType.SYSTEM), userSecurity);
        systemRepository = buildSystemRepository();
        MockMetadataRepositoryAdmin.INSTANCE.register("__SYSTEM", systemRepository);
        systemStorage.init(getDatasource(DATASOURCE));
        systemStorage.prepare(systemRepository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(systemStorage);
        LOG.info("System storage prepared");

        LOG.info("Preparing master storage");
        masterStorage = new SecuredStorage(new HibernateStorage("Product", StorageType.MASTER), userSecurity);
        productRepository = new MetadataRepository();
        productRepository.load(RecordValidationTest.class.getResourceAsStream("../storage/Product.xsd"));
        productRepository.load(RecordValidationTest.class.getResourceAsStream("../save/updateReport.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Product", productRepository);
        product = productRepository.getComplexType("Product");
        store = productRepository.getComplexType("Store");
        productFamily = productRepository.getComplexType("ProductFamily");
        masterStorage.init(getDatasource(DATASOURCE));
        masterStorage.prepare(productRepository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(masterStorage);
        LOG.info("Master storage prepared");

        LOG.info("Preparing staging storage");
        stagingStorage = new SecuredStorage(new HibernateStorage("Product", StorageType.STAGING), userSecurity);
        stagingStorage.init(getDatasource(DATASOURCE));
        stagingStorage.prepare(productRepository, Collections.<Expression> emptySet(), true, true);
        ((MockStorageAdmin) ServerContext.INSTANCE.get().getStorageAdmin()).register(stagingStorage);
        LOG.info("Staging storage prepared");

        BeanDelegatorContainer.createInstance();
    }

    @Override
    protected void setUp() throws Exception {
        String xmlStore = "<Store><Id>1</Id><Address>Address</Address><Lat>1.0</Lat><Long>1.0</Long></Store>";
        String xmlProduct = "<Product><Id>1</Id><Name>Product</Name><Description>Product Description</Description><Features><Sizes/><Colors/></Features><Price>2.00</Price><Stores><Store>[1]</Store></Stores></Product>";
        createData("Product", false, xmlStore);
        createData("Product", false, xmlProduct);
        createData("Product", true, xmlStore);
        createData("Product", true, xmlProduct);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            masterStorage.begin();
            {
                UserQueryBuilder qb = from(product);
                masterStorage.delete(qb.getSelect());

                qb = from(productFamily);
                masterStorage.delete(qb.getSelect());

                qb = from(store);
                masterStorage.delete(qb.getSelect());
            }
            masterStorage.commit();
        } finally {
            masterStorage.end();
        }
        try {
            stagingStorage.begin();
            {
                UserQueryBuilder qb = from(product);
                stagingStorage.delete(qb.getSelect());

                qb = from(productFamily);
                stagingStorage.delete(qb.getSelect());

                qb = from(store);
                stagingStorage.delete(qb.getSelect());
            }
            stagingStorage.commit();
        } finally {
            stagingStorage.end();
        }
    }

    protected static DataSourceDefinition getDatasource(String dataSourceName) {
        return ServerContext.INSTANCE.get().getDefinition(dataSourceName, "MDM");
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

    protected static class MockAdmin extends ILocalUser {

        @Override
        public ILocalUser getILocalUser() throws XtentisException {
            return this;
        }

        @Override
        public HashSet<String> getRoles() {
            HashSet<String> roleSet = new HashSet<String>();
            roleSet.add("System_Admin");
            roleSet.add("administration");
            return roleSet;
        }

        @Override
        public String getUsername() {
            return "administrator";
        }

        @Override
        public boolean isAdmin(Class<?> objectTypeClass) throws XtentisException {
            return true;
        }
    }

    protected static class MockUser extends ILocalUser {

        @Override
        public ILocalUser getILocalUser() throws XtentisException {
            return this;
        }

        @Override
        public HashSet<String> getRoles() {
            HashSet<String> roleSet = new HashSet<String>();
            roleSet.add("System_Interactive");
            return roleSet;
        }

        @Override
        public String getUsername() {
            return "user";
        }

        @Override
        public boolean isAdmin(Class<?> objectTypeClass) throws XtentisException {
            return false;
        }
    }

    protected static class MockSaverSource extends StorageSaverSource {

        private final MetadataRepository repository;

        private final boolean isAdmin;
        
        private final boolean invokeBeforeSaving;

        public MockSaverSource(MetadataRepository repository, boolean isAdmin) {
            this.repository = repository;
            this.isAdmin = isAdmin;
            this.invokeBeforeSaving = true;
        }
        
        public MockSaverSource(MetadataRepository repository, boolean isAdmin, boolean invokeBeforeSaving) {
            this.repository = repository;
            this.isAdmin = isAdmin;
            this.invokeBeforeSaving = invokeBeforeSaving;
        }

        @Override
        public String getUserName() {
            if(isAdmin) {
                return "administrator";
            } else {
                return "user";
            }
        }

        @Override
        public Set<String> getCurrentUserRoles() {
            if(isAdmin) {
                HashSet<String> roleSet = new HashSet<String>();
                roleSet.add("System_Admin");
                roleSet.add("administration");
                return roleSet;
            } else {
                HashSet<String> roleSet = new HashSet<String>();
                roleSet.add("System_Interactive");
                return roleSet;
            }
        }

        @Override
        public InputStream getSchema(String dataModelName) {
            if ("UpdateReport".equals(dataModelName)) {
               return RecordValidationTest.class.getResourceAsStream("../save/updateReport.xsd");
            } else {
                return RecordValidationTest.class.getResourceAsStream("../storage/Product.xsd");
            }
        }

        @Override
        public synchronized MetadataRepository getMetadataRepository(String dataModelName) {
            return repository;
        }

        public void routeItem(String dataCluster, String typeName, String[] id) {
            // nothing to do
        }

        public OutputReport invokeBeforeSaving(DocumentSaverContext context, MutableDocument updateReportDocument) {
            if (invokeBeforeSaving) {
                throw new RuntimeException("Before saving validation failed.");
            }
            return null;
        }
    }

    protected static JSONObject validateRecord(String storageName, boolean isStaging, boolean isAdmin, String documentXml) throws Exception {
        return validateRecord(storageName, isStaging, isAdmin, false, documentXml);
    }
    
    // Simulate the Record Validation API of DataService#validateRecord() to test RecordValidationContext, RecordValidationCommitter, etc.
    protected static JSONObject validateRecord(String storageName, boolean isStaging, boolean isAdmin, boolean invokeBeforeSaving, String documentXml) throws Exception {
        BeanDelegatorContainer.getInstance().setDelegatorInstancePool(Collections.<String, Object> singletonMap("LocalUser", isAdmin ? new MockAdmin() : new MockUser()));
        String dataCluster = isStaging ? storageName + "#STAGING" : storageName;
        DataRecord.ValidateRecord.set(true);
        boolean isValid = true;
        String message = StringUtils.EMPTY;

        SaverSession session = SaverSession.newSession(new MockSaverSource(productRepository, isAdmin, invokeBeforeSaving));
        Committer committer = new RecordValidationCommitter();
        DocumentSaverContext context = session.getContextFactory().createValidation(dataCluster, storageName, invokeBeforeSaving, new ByteArrayInputStream(documentXml.getBytes("UTF-8")));
        DocumentSaver saver = context.createSaver();
        try {
            session.begin(dataCluster, committer);
            saver.save(session, context);
            session.end(committer);
        } catch (Exception e) {
            isValid = false;
            message = getRootException(e).getMessage();
        } finally {
            session.abort(committer);
            DataRecord.ValidateRecord.remove();
        }

        JSONObject result = new JSONObject();
        try {
            result.put("isValid", isValid); 
            result.put("message", message); 
        } catch (JSONException e) {
            throw new RuntimeException("Unable to build the record validation result.", e); 
        }
        return result;
    }

    // Create test data
    protected static void createData(String storageName, boolean isStaging, String documentXml) throws Exception {
        BeanDelegatorContainer.getInstance().setDelegatorInstancePool(Collections.<String, Object> singletonMap("LocalUser", new MockAdmin()));
        Util.getDataClusterCtrlLocal().putDataCluster(new DataClusterPOJO("UpdateReport"));
        Util.getDataClusterCtrlLocal().putDataCluster(new DataClusterPOJO("CONF"));
        Util.getDataClusterCtrlLocal().putDataCluster(new DataClusterPOJO("Product"));
        
        String dataCluster = isStaging ? storageName + "#STAGING" : storageName;
        SaverSession session = SaverSession.newSession(new MockSaverSource(productRepository, true));
        DocumentSaverContext context = session.getContextFactory().createValidation(dataCluster, storageName, false, new ByteArrayInputStream(documentXml.getBytes("UTF-8")));
        DocumentSaver saver = context.createSaver();
        try {
            session.begin(dataCluster);
            saver.save(session, context);
            session.end();
        } catch (Exception e) {
            session.abort();
            throw e;
        }
    }

    protected static Throwable getRootException(Throwable e) {
        Throwable root = e;
        while(root != null && root.getCause() != null && root.getCause().getMessage() != null) {
            root = root.getCause();
        }
        return root;
    }

    // MASTER, get Schema error, STAGING won't validate Schema, no error
    public void testSchemaValidation() throws Exception {
        String xmlForSchema1 = "<ProductFamily><Name>Test Product Family</Name><ChangeStatus>NotValid</ChangeStatus></ProductFamily>";
        String xmlForSchema2 = "<ProductFamily><Name>Test Product Family</Name><ChangeStatus>Approved</ChangeStatus></ProductFamily>";
        // MASTER
        JSONObject resp = validateRecord("Product", false, true, xmlForSchema1);
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").contains("[Error] :-1:-1: cvc-type.3.1.3: The value 'NotValid' of element 'ChangeStatus' is not valid."));
        resp = validateRecord("Product", false, true, xmlForSchema2);
        assertTrue(resp.getBoolean("isValid"));// PASS
        // STAGING
        resp = validateRecord("Product", true, true, xmlForSchema1);
        assertTrue(resp.getBoolean("isValid"));// PASS, doesn't do schema validation
        resp = validateRecord("Product", true, true, xmlForSchema2);
        assertTrue(resp.getBoolean("isValid"));// PASS
    }

    // MASTER, CREATE will get Schema error, UPDATE will get convert error, STAGING will both get convert error
    public void testValueTypeValidation() throws Exception {
        String xmlForValue1 = "<Product><Id>1</Id><Name>Test Product 1</Name><Description>Test Product Description</Description><Features><Sizes/><Colors/></Features><Price>a2.00</Price></Product>";
        String xmlForValue2 = "<Product><Id>2</Id><Name>Test Product 2</Name><Description>Test Product Description</Description><Features><Sizes/><Colors/></Features><Price>a2.00</Price></Product>";
        String xmlForValue3 = "<Product><Id>3</Id><Name>Test Product 1</Name><Description>Test Product Description</Description><Features><Sizes/><Colors/></Features><Price>3.00</Price></Product>";
        //MASTER
        JSONObject resp = validateRecord("Product", false, true, xmlForValue1);
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").equals("'a2.00' is not a number."));// UPDATE will return value error
        resp = validateRecord("Product", false, true, xmlForValue2);
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").contains("[Error] :-1:-1: cvc-datatype-valid.1.2.1: 'a2.00' is not a valid value for 'decimal'."));// CREATE will return schema error
        resp = validateRecord("Product", false, true, xmlForValue3);
        assertTrue(resp.getBoolean("isValid"));// PASS
        //STAGING
        resp = validateRecord("Product", true, true, xmlForValue1);
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").equals("'a2.00' is not a number."));// UPDATE will return value error
        resp = validateRecord("Product", true, true, xmlForValue2);
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").equals("'a2.00' is not a number."));// CREATE will return value error
        resp = validateRecord("Product", true, true, xmlForValue3);
        assertTrue(resp.getBoolean("isValid"));// PASS
    }

    // MASTER & STAGING will both validate the existence of FK
    public void testForeignKeyValidation() throws Exception {
        String xmlForFK1 = "<Product><Id>1</Id><Name>Product</Name><Description>Product Description 1</Description><Features><Sizes/><Colors/></Features><Price>2.00</Price><Stores><Store>[1]</Store></Stores></Product>";
        String xmlForFK2 = "<Product><Id>1</Id><Name>Product</Name><Description>Product Description 2</Description><Features><Sizes/><Colors/></Features><Price>2.00</Price><Stores><Store>[2]</Store></Stores></Product>";
        // MASTER
        JSONObject resp = validateRecord("Product", false, true, xmlForFK1); // exists FK
        assertTrue(resp.getBoolean("isValid"));// PASS
        resp = validateRecord("Product", false, true, xmlForFK2); // not exist
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").equals("Invalid foreign key: [org.talend.mdm.storage.hibernate.Store#2] doesn't exist."));
        // STAGING
        resp = validateRecord("Product", true, true, xmlForFK1); // exists FK
        assertTrue(resp.getBoolean("isValid"));// PASS
        resp = validateRecord("Product", true, true, xmlForFK2); // not exist
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").equals("Invalid foreign key: [org.talend.mdm.storage.hibernate.Store#2] doesn't exist."));
    }

    // MASTER can control if call beforeSaving or not, STAGING won't call beforeSaving
    public void testBeforeSavingValidation() throws Exception {
        String xmlBeforeSaving = "<ProductFamily><Name>Test Product Family</Name><ChangeStatus>Approved</ChangeStatus></ProductFamily>";
        // MASTER
        JSONObject resp = validateRecord("Product", false, true, true, xmlBeforeSaving);
        assertFalse(resp.getBoolean("isValid"));//  FAIL, MASTER call beforeSaving
        assertTrue(resp.getString("message").equals("Before saving validation failed."));
        resp = validateRecord("Product", false, true, false, xmlBeforeSaving);
        assertTrue(resp.getBoolean("isValid"));//  PASS, MASTER doesn't call beforeSaving
        // STAGING
        resp = validateRecord("Product", true, true, true, xmlBeforeSaving);
        assertTrue(resp.getBoolean("isValid"));//  PASS, STAGING won't call beforeSaving process
    }

    // MASTER & STAGING will both validate the security
    public void testSecurityValidation() throws Exception {
        String xmlForSecurity= "<ProductFamily><Name>Test Product Family</Name><ChangeStatus>Approved</ChangeStatus></ProductFamily>";
        // MASTER
        JSONObject resp = validateRecord("Product", false, false, xmlForSecurity);// 'user' can't write ProductFamily
        assertFalse(resp.getBoolean("isValid"));//  FAIL
        assertTrue(resp.getString("message").equals("User 'user' is not allowed to write to type 'ProductFamily'."));
        // STAGING
        resp = validateRecord("Product", true, false, xmlForSecurity); // 'user' can't write ProductFamily
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").equals("User 'user' is not allowed to write to type 'ProductFamily'."));
    }

    // Validate record contains AutoIncrement won't affect the value stored in system
    public void testNoAutoIncrementImpactValidation() throws Exception {
        String xmlFamily1 = "<ProductFamily><Name>test_product_family_1</Name><ChangeStatus>Approved</ChangeStatus></ProductFamily>";
        String xmlFamily2 = "<ProductFamily><Name>test_product_family_2</Name><ChangeStatus>Approved</ChangeStatus></ProductFamily>";
        String xmlForAutoIncrement= "<ProductFamily><Name>Test Product Family</Name><ChangeStatus>Approved</ChangeStatus></ProductFamily>";
        FieldMetadata id = productFamily.getField("Id");
        FieldMetadata name = productFamily.getField("Name");
        UserQueryBuilder qb = from(productFamily).orderBy(id, OrderBy.Direction.DESC);
        // MASTER
        createData("Product", false, xmlFamily1);
        validateRecord("Product", false, false, xmlForAutoIncrement);
        createData("Product", false, xmlFamily2);
        StorageResults results = masterStorage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            int id1 = 0;
            int id2 = 0;
            for (DataRecord result : results) {
                if("test_product_family_2".equals(result.get(name))) {
                    id2 = Integer.parseInt((String)result.get(id));
                } else {
                    id1 = Integer.parseInt((String)result.get(id));
                }
            }
            assertTrue((id2 - id1) == 1);
        } finally {
            results.close();
        }
        // STAGING
        createData("Product", true, xmlFamily1);
        validateRecord("Product", true, false, xmlForAutoIncrement);
        createData("Product", true, xmlFamily2);
        results = stagingStorage.fetch(qb.getSelect());
        try {
            assertEquals(2, results.getSize());
            int id1 = 0;
            int id2 = 0;
            for (DataRecord result : results) {
                if("test_product_family_2".equals(result.get(name))) {
                    id2 = Integer.parseInt((String)result.get(id));
                } else {
                    id1 = Integer.parseInt((String)result.get(id));
                }
            }
            assertTrue((id2 - id1) == 1);
        } finally {
            results.close();
        }
    }
    
    // Validate record contains wrong xml node(for existing record)
    public void testXmlNodeValidation() throws Exception {
        String xmlForNode1 = "<Product><Id>1</Id><Names>Product</Names><Description>Product Description</Description></Product>";
        String xmlForNode2 = "<Product><Id>1</Id><Name>Product</Name><Description>Product Description</Description><Features><Sizes><Size2>Small</Size2></Sizes></Features></Product>";
        // MASTER
        JSONObject resp = validateRecord("Product", false, true, xmlForNode1);
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").contains("Entity 'Product' does not own field 'Names'."));
        resp = validateRecord("Product", false, true, xmlForNode2);
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").contains("Entity 'Product' does not own field 'Size2'."));
        // STAGING
        resp = validateRecord("Product", true, true, xmlForNode1);
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").contains("Entity 'Product' does not own field 'Names'."));
        resp = validateRecord("Product", true, true, xmlForNode2);
        assertFalse(resp.getBoolean("isValid"));// FAIL
        assertTrue(resp.getString("message").contains("Entity 'Product' does not own field 'Size2'."));
    }
}