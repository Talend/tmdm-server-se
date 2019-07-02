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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.IItemCtrlDelegator;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.datacluster.DataClusterPOJOPK;
import com.amalto.core.server.MockMetadataRepositoryAdmin;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.util.XtentisException;

import junit.framework.TestCase;

public class DefaultRecycleBinTest extends TestCase {

    private static boolean beanDelegatorContainerFlag = false;

    private static void createBeanDelegatorContainer() {
        if (!beanDelegatorContainerFlag) {
            BeanDelegatorContainer.createInstance();
            beanDelegatorContainerFlag = true;
        }
    }

    @Override
    public void tearDown() throws Exception {
        ServerContext.INSTANCE.close();
    }

    @Override
    public void setUp() throws Exception {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }

    public void testAdminCanReadDataModel() throws Exception {
        String clusterName = "Model1"; //$NON-NLS-1$
        String conceptNameA = "Person"; //$NON-NLS-1$
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);

        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("../storage/RecycleBin_Model1.xsd")); //$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register(clusterName, repository);

        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        Storage storage = storageAdmin.create(clusterName, clusterName, StorageType.MASTER, "H2-DS1"); //$NON-NLS-1$
        assertNotNull(storage);

        Map<String, Object> delegatorInstancePool = new HashMap<String, Object>();
        delegatorInstancePool.put("LocalUser", new MockAdmin()); //$NON-NLS-1$
        delegatorInstancePool.put("ItemCtrl", new MockDefaultItemCtrlDelegator()); //$NON-NLS-1$
        createBeanDelegatorContainer();
        BeanDelegatorContainer.getInstance().setDelegatorInstancePool(delegatorInstancePool);

        try {
            DataClusterPOJOPK dataClusterPK1 = new DataClusterPOJOPK(new ObjectPOJOPK(new String[] { "1" }));
            BeanDelegatorContainer.getInstance().getItemCtrlDelegator().getItems(dataClusterPK1, conceptNameA, null, 0,
                    clusterName, conceptNameA, 0, 0, beanDelegatorContainerFlag);
            fail("Expected: only data don't exist."); //$NON-NLS-1$
        } catch (Exception e1) {
            // Expected
        }
    }

    public void testUserCanNotReadDataModel() throws Exception {
        String clusterName = "Model2"; //$NON-NLS-1$
        String conceptNameA = "Circle"; //$NON-NLS-1$
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);

        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("../storage/RecycleBin_Model2.xsd")); //$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register(clusterName, repository);

        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        Storage storage = storageAdmin.create(clusterName, clusterName, StorageType.MASTER, "H2-DS1"); //$NON-NLS-1$
        assertNotNull(storage);

        Map<String, Object> delegatorInstancePool = new HashMap<String, Object>();
        delegatorInstancePool.put("LocalUser", new MockILocalUser()); //$NON-NLS-1$
        delegatorInstancePool.put("ItemCtrl", new MockDefaultItemCtrlDelegator()); //$NON-NLS-1$
        createBeanDelegatorContainer();
        BeanDelegatorContainer.getInstance().setDelegatorInstancePool(delegatorInstancePool);

        try {
            DataClusterPOJOPK dataClusterPK1 = new DataClusterPOJOPK(new ObjectPOJOPK(new String[] { "1" }));
            BeanDelegatorContainer.getInstance().getItemCtrlDelegator().getItems(dataClusterPK1, conceptNameA, null, 0,
                    clusterName, conceptNameA, 0, 0, beanDelegatorContainerFlag);
            fail("Expected: not allowed."); //$NON-NLS-1$
        } catch (Exception e1) {
            // Expected
        }
    }

    private static class MockAdmin extends ILocalUser {

        @Override
        public ILocalUser getILocalUser() throws XtentisException {
            return this;
        }

        @Override
        public HashSet<String> getRoles() {
            HashSet<String> roleSet = new HashSet<String>();
            roleSet.add("Role1");
            roleSet.add("Role2");
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

    private static class MockILocalUser extends com.amalto.core.delegator.ILocalUser {

        @Override
        public ILocalUser getILocalUser() throws XtentisException {
            return this;
        }

        @Override
        public HashSet<String> getRoles() {
            HashSet<String> roleSet = new HashSet<String>();
            roleSet.add("Role1"); //$NON-NLS-1$
            return roleSet;
        }

        @Override
        public String getUsername() {
            return "LocalUser"; //$NON-NLS-1$
        }

        public boolean userCanRead(Class<?> objectTypeClass, String instanceId) throws XtentisException {
            return false;
        }

        @Override
        public boolean isAdmin(Class<?> objectTypeClass) throws XtentisException {
            return false;
        }
    }

    protected static class MockDefaultItemCtrlDelegator extends IItemCtrlDelegator {
    }
}
