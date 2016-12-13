/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
// ============================================================================
//
// Copyright (C) 2006-2014 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.delegator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.server.MockMetadataRepositoryAdmin;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.Server;
import com.amalto.core.server.ServerContext;
import com.amalto.core.server.StorageAdmin;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.util.XtentisException;

import junit.framework.TestCase;

public class IItemCtrlDelegatorTest extends TestCase {
    
    private static boolean beanDelegatorContainerFlag = false;
    
    private static void createBeanDelegatorContainer(){
        if(!beanDelegatorContainerFlag){
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
    
    @SuppressWarnings("unused")
    public void testAllowDelete() throws Exception {
        String clusterName = "ModelTest1"; //$NON-NLS-1$
        String conceptNameA = "EntityA"; //$NON-NLS-1$
        String conceptNameB = "EntityB"; //$NON-NLS-1$
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);
        
        MetadataRepository repository = new MetadataRepository();
        repository.load(this.getClass().getResourceAsStream("schema.xsd")); //$NON-NLS-1$
        MockMetadataRepositoryAdmin.INSTANCE.register(clusterName, repository);
        
        StorageAdmin storageAdmin = server.getStorageAdmin();
        assertNotNull(storageAdmin);
        Storage storage = storageAdmin.create(clusterName, clusterName, StorageType.MASTER, "H2-DS1"); //$NON-NLS-1$
        assertNotNull(storage);
        
        Map<String, Object> delegatorInstancePool = new HashMap<String, Object>();
        delegatorInstancePool.put("LocalUser", new MockUser()); //$NON-NLS-1$
        delegatorInstancePool.put("ItemCtrl", new MockDefaultItemCtrlDelegator()); //$NON-NLS-1$
        createBeanDelegatorContainer();
        BeanDelegatorContainer.getInstance().setDelegatorInstancePool(delegatorInstancePool); 
        
        try {
            BeanDelegatorContainer.getInstance().getItemCtrlDelegator().allowDelete(clusterName, conceptNameA, ComplexTypeMetadata.DeleteType.LOGICAL);
            fail("Expected: not allowed."); //$NON-NLS-1$
        } catch (Exception e) {
            // Expected
        }
        
        try {
            BeanDelegatorContainer.getInstance().getItemCtrlDelegator().allowDelete(clusterName, conceptNameA, ComplexTypeMetadata.DeleteType.PHYSICAL);
        } catch (Exception e) {
            fail("Expected: not allowed."); //$NON-NLS-1$
        }
        
        try {
            BeanDelegatorContainer.getInstance().getItemCtrlDelegator().allowDelete(clusterName, conceptNameB, ComplexTypeMetadata.DeleteType.LOGICAL);
        } catch (Exception e) {
            fail("Expected: not allowed."); //$NON-NLS-1$
        }
        
        try {
            BeanDelegatorContainer.getInstance().getItemCtrlDelegator().allowDelete(clusterName, conceptNameB, ComplexTypeMetadata.DeleteType.PHYSICAL);
            fail("Expected: not allowed."); //$NON-NLS-1$
        } catch (Exception e) {
            // Expected
        }
        
    }
    
    protected static class MockUser extends ILocalUser {

        @Override
        public ILocalUser getILocalUser() throws XtentisException {
            return this;
        }

        @SuppressWarnings("unused")
        @Override
        public HashSet<String> getRoles() {
            HashSet<String> roleSet = new HashSet<String>();
            roleSet.add("Demo_User"); //$NON-NLS-1$
            return roleSet;
        }
    }
    
    protected static class MockDefaultItemCtrlDelegator extends IItemCtrlDelegator {
    }
}
