/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import com.amalto.core.server.api.DataCluster;
import com.amalto.core.storage.StorageType;

import junit.framework.TestCase;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.ICoreConstants;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.query.user.Expression;
import com.amalto.core.query.user.UserQueryBuilder;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageResults;
import com.amalto.core.util.XtentisException;

@SuppressWarnings("nls")
public class ServerTest extends TestCase {

    @Override
    public void tearDown() throws Exception {
        ServerContext.INSTANCE.close();
    }

    @Override
    public void setUp() throws Exception {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }

    public void testStorageInitialization() throws Exception {
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);

        String metadataRepositoryId = "../query/metadata.xsd";
        MetadataRepository metadataRepository = server.getMetadataRepositoryAdmin().get(metadataRepositoryId);
        assertNotNull(metadataRepository);

        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        Storage storage = storageAdmin.create(metadataRepositoryId, "Storage", StorageType.MASTER, "H2-DS1");
        assertNotNull(storage);

        ComplexTypeMetadata person = metadataRepository.getComplexType("Person");
        assertNotNull(person);

        UserQueryBuilder qb = UserQueryBuilder.from(person);
        storage.begin();
        StorageResults fetch = storage.fetch(qb.getSelect());
        assertNotNull(fetch);

        try {
            assertTrue(fetch.getCount() >= 0); // Execute this just to check initialization was ok.
        } finally {
            fetch.close();
        }
        storage.commit();
    }

    public void testStorageRestart() throws Exception {
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);

        String metadataRepositoryId = "../query/metadata.xsd";
        MetadataRepository metadataRepository = server.getMetadataRepositoryAdmin().get(metadataRepositoryId);
        assertNotNull(metadataRepository);

        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        Storage storage = storageAdmin.create(metadataRepositoryId, "Storage", StorageType.MASTER, "H2-DS1");
        assertNotNull(storage);

        storage.prepare(metadataRepository, Collections.<Expression> emptySet(), true, true);
    }

    // Temporary exclusion, see TMDM-5825
    /*
     * public void testStorageReindex() throws Exception { Server server = ServerContext.INSTANCE.get();
     * assertNotNull(server);
     * 
     * String metadataRepositoryId = "../query/metadata.xsd"; MetadataRepository metadataRepository =
     * server.getMetadataRepositoryAdmin().get(metadataRepositoryId); assertNotNull(metadataRepository);
     * 
     * StorageAdmin storageAdmin = server.getStorageAdmin(); assertNotNull(storageAdmin); Storage storage =
     * storageAdmin.create(metadataRepositoryId, "Storage", "H2-Default", null); assertNotNull(storage);
     * 
     * storage.reindex(); }
     */

    public void testConnectionCleanUp() throws Exception {
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);
        String storageName = "Storage";
        MetadataRepository metadataRepository = server.getMetadataRepositoryAdmin().get(storageName);
        assertNotNull(metadataRepository);
        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        Storage storage = storageAdmin.create(storageName, storageName, StorageType.MASTER, "H2-DS1");
        assertTrue(new File("data/h2_ds1.mv.db").exists());
        storage.close(true);
        assertFalse(new File("data/h2_ds1.mv.db").exists());
    }

    public void testCreateWithSlash() throws Exception {
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);

        String storageName = "Storage";
        MetadataRepository metadataRepository = server.getMetadataRepositoryAdmin().get(storageName);
        assertNotNull(metadataRepository);

        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        Storage storage = null;
        Storage storage2 = null;
        try {
            storage = storageAdmin.create(storageName + "/MyTypeNameThatShouldBeSkipped", "Storage", StorageType.MASTER, "H2-DS1");
            assertNotNull(storage);
            assertEquals(storageName, storage.getName());

            storage2 = storageAdmin.create(storageName, "Storage", StorageType.MASTER, "H2-DS1");
            assertSame(storage2, storage);
        } finally {
            if (storage != null) {
                storage.close();
            }
            if (storage2 != null) {
                storage2.close(); // Also test if consecutive close() calls don't raise exception.
            }
        }
    }

    public void testFailedInit() throws Exception {
        ServerContext.INSTANCE.close(); // Setup set the wrong lifecycle
        ServerLifecycle serverLifecycle = new MockServerLifecycle() {

            @Override
            public MetadataRepositoryAdmin createMetadataRepositoryAdmin() {
                return new MockMetadataRepositoryAdmin() {

                    @Override
                    public boolean exist(String metadataRepositoryId) {
                        return false;
                    }

                    @Override
                    public MetadataRepository get(String metadataRepositoryId) {
                        return null;
                    }
                };
            }
        };
        Server server = ServerContext.INSTANCE.get(serverLifecycle);
        assertNotNull(server);

        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        try {
            storageAdmin.create("dataModelNameThatFailsFirstTime", "Storage", StorageType.MASTER, "H2-DS1");
            fail("Expected exception because data model does not exist.");
        } catch (Exception e) {
            // Expected
        }
    }

    public void testStorageDrop() throws Exception {
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);
        // Create a storage
        String metadataRepositoryId = "../query/metadata.xsd";
        MetadataRepository metadataRepository = server.getMetadataRepositoryAdmin().get(metadataRepositoryId);
        assertNotNull(metadataRepository);
        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        Storage storage = storageAdmin.create(metadataRepositoryId, "Storage", StorageType.MASTER, "H2-DS1");
        assertNotNull(storage);
        ComplexTypeMetadata person = metadataRepository.getComplexType("Person");
        assertNotNull(person);
        UserQueryBuilder qb = UserQueryBuilder.from(person);
        storage.begin();
        StorageResults fetch = storage.fetch(qb.getSelect());
        assertNotNull(fetch);
        try {
            assertTrue(fetch.getCount() >= 0); // Execute this just to check initialization was ok.
        } finally {
            fetch.close();
            storage.commit();
        }
        // Destroy storage (and data).
        storageAdmin.delete("Storage", StorageType.MASTER, true);
        // Re create a storage.
        storage = storageAdmin.create(metadataRepositoryId, "Storage", StorageType.MASTER, "H2-DS1");
        assertNotNull(storage);
        qb = UserQueryBuilder.from(person);
        storage.begin();
        fetch = storage.fetch(qb.getSelect());
        assertNotNull(fetch);
        try {
            assertTrue(fetch.getCount() >= 0); // Execute this just to check initialization was ok.
        } finally {
            fetch.close();
            storage.commit();
        }
    }
    
    public void testStorageDropAll() throws Exception {
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);
        // Create a storage
        String metadataRepositoryId = "../query/metadata.xsd";
        MetadataRepository metadataRepository = server.getMetadataRepositoryAdmin().get(metadataRepositoryId);
        assertNotNull(metadataRepository);
        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        Storage storage = storageAdmin.create(metadataRepositoryId, "Storage", StorageType.MASTER, "H2-DS1");
        assertNotNull(storage);
        ComplexTypeMetadata person = metadataRepository.getComplexType("Person");
        assertNotNull(person);
        UserQueryBuilder qb = UserQueryBuilder.from(person);
        storage.begin();
        StorageResults fetch = storage.fetch(qb.getSelect());
        assertNotNull(fetch);
        try {
            assertTrue(fetch.getCount() >= 0); // Execute this just to check initialization was ok.
        } finally {
            fetch.close();
            storage.commit();
        }
        
        //instantiate BeanDelegator to manage user
        BeanDelegatorContainer.createInstance().setDelegatorInstancePool(
            Collections.<String, Object> singletonMap("LocalUser", new ILocalUser() {
            @Override
            public ILocalUser getILocalUser() throws XtentisException {
                return this;
            }
            
            //no need to mock the getRole methode as its going 
            //to retrieve the role in securityContext (see below)
//            @Override
//            public HashSet<String> getRoles() {
//                HashSet<String> roleSet = new HashSet<String>();
//                roleSet.add("Demo_Manager");
//                roleSet.add("System_Admin");
//                roleSet.add("authenticated");
//                roleSet.add("administration");
//                return roleSet;
//            }
        }));
        
        //create the storage __SYSTEM 
        // Initialize system storage
        String systemDataSourceName = storageAdmin.getDatasource(StorageAdmin.SYSTEM_STORAGE);
        storageAdmin.create(StorageAdmin.SYSTEM_STORAGE, StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM, systemDataSourceName);
        Storage systemStorage = storageAdmin.get(StorageAdmin.SYSTEM_STORAGE, StorageType.SYSTEM);
        assertNotNull(systemStorage);
        
        List<GrantedAuthority> roles = AuthorityUtils.createAuthorityList(ICoreConstants.ADMIN_PERMISSION,
                ICoreConstants.AUTHENTICATED_PERMISSION);
        Authentication authentication = new UsernamePasswordAuthenticationToken("MDMInternalUser", "", roles); //$NON-NLS-1$
        
        //set the authentication to the security context holder
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        //add cluster into __SYSTEM
        DataCluster cluster = com.amalto.core.util.Util.getDataClusterCtrlLocal();
        if (cluster.existsDataCluster(new DataClusterPOJOPK("Storage")) == null) {
            cluster.putDataCluster(new DataClusterPOJO("Storage", "cluster for a person", ""));
        }
        
        //test if the cluster is present
        assertNotNull(cluster.existsDataCluster(new DataClusterPOJOPK("Storage")));
        
        //delete storage/cluster
        //check if cluster still exist 
        
        // Destroy storage (and data).
        storageAdmin.delete("Storage", true);
        
        //After removing the cluster, this result should be null
        assertNull(cluster.existsDataCluster(new DataClusterPOJOPK("Storage")));
        
        
        //test if storage is still accessible after deleteting it
        assertNotNull(storage);
        qb = UserQueryBuilder.from(person);
        try {
            storage.begin();
            fail( "should have thown an exception : java.lang.IllegalStateException: Storage has not been prepared." );
        } catch (java.lang.IllegalStateException expectedException) {
            //do nothing
        }        
        
        // Re create a storage on the same metadataRepository so we can do action again
        storage = storageAdmin.create(metadataRepositoryId, "Storage", StorageType.MASTER, "H2-DS1");
        assertNotNull(storage);
        qb = UserQueryBuilder.from(person);
        storage.begin();
        fetch = storage.fetch(qb.getSelect());
        assertNotNull(fetch);
        try {
            assertTrue(fetch.getCount() >= 0); // Execute this just to check initialization was ok.
        } finally {
            fetch.close();
            storage.commit();
        }
    }

    public void testSuperTypeFK() {
        Server server = ServerContext.INSTANCE.get();

        String metadataRepositoryId = "../query/metadata.xsd";
        MetadataRepository metadataRepository = server.getMetadataRepositoryAdmin().get(metadataRepositoryId);

        StorageAdmin storageAdmin = server.getStorageAdmin();
        Storage storage = storageAdmin.create(metadataRepositoryId, "Storage", StorageType.MASTER, "H2-DS1");

        storage.prepare(metadataRepository, Collections.<Expression> emptySet(), true, true);
        ComplexTypeMetadata person = metadataRepository.getComplexType("MainEntity");
        assertEquals(3, person.getFields().size());
        assertNotNull(person.getField("fk_ForeignTypeInherited"));
    }

}
