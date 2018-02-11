/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.server;

import java.util.Collections;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;

import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.objects.datamodel.DataModelPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJOPK;
import com.amalto.core.server.api.DataModel;
import com.amalto.core.storage.Storage;
import com.amalto.core.storage.StorageType;
import com.amalto.core.util.Util;
import com.amalto.core.util.XtentisException;

import junit.framework.TestCase;

public class DefaultDataModelTest extends TestCase {

    @Override
    public void tearDown() throws Exception {
        ServerContext.INSTANCE.close();
    }

    @Override
    public void setUp() throws Exception {
        ServerContext.INSTANCE.get(new MockServerLifecycle());

        BeanDelegatorContainer.createInstance().setDelegatorInstancePool(
                Collections.<String, Object> singletonMap("LocalUser", new ILocalUser() {

                    @Override
                    public ILocalUser getILocalUser() throws XtentisException {
                        return new MockILocalUser();
                    }
                }));
    }

    public void testRemoveDataModel() throws Exception {
        String modelName = "Product";
        Server server = ServerContext.INSTANCE.get();
        assertNotNull(server);

        StorageAdmin storageAdmin = ServerContext.INSTANCE.get().getStorageAdmin();
        Storage storage = storageAdmin.get(modelName, StorageType.MASTER);

        assertNotNull(storage);

        DataModelPOJO dataModelPOJO = new DataModelPOJO(modelName);
        dataModelPOJO.setSchema(IOUtils.toString(
                DefaultDataModelTest.class.getResourceAsStream("../storage/Product.xsd"), "UTF-8")); //$NON-NLS-1$
        dataModelPOJO.store();

        MockMetadataRepositoryAdmin metadataRepositoryAdmin = (MockMetadataRepositoryAdmin) ServerContext.INSTANCE.get()
                .getMetadataRepositoryAdmin();
        assertNotNull(metadataRepositoryAdmin.getMetadataRepository().get(modelName));

        DataModel DefaultDataModel = Util.getDataModelCtrlLocal();
        DefaultDataModel.removeDataModel(new DataModelPOJOPK(modelName));

        assertNull(metadataRepositoryAdmin.getMetadataRepository().get(modelName));
    }

    private static class MockILocalUser extends com.amalto.core.delegator.ILocalUser {

        @Override
        public ILocalUser getILocalUser() throws XtentisException {
            return this;
        }

        @Override
        public HashSet<String> getRoles() {
            HashSet<String> roleSet = new HashSet<String>();
            roleSet.add("System_View"); //$NON-NLS-1$
            return roleSet;
        }

        @Override
        public String getUsername() {
            return "Admin"; //$NON-NLS-1$
        }

        @Override
        public boolean isAdmin(Class<?> objectTypeClass) throws XtentisException {
            return false;
        }
    }

}
