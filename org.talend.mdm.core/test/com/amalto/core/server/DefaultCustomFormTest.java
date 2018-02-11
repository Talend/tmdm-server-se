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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amalto.core.delegator.ILocalUser;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.customform.CustomFormPOJO;
import com.amalto.core.objects.customform.CustomFormPOJOPK;
import com.amalto.core.objects.role.RolePOJO;
import com.amalto.core.objects.role.RolePOJOPK;
import com.amalto.core.util.LocalUser;
import com.amalto.core.util.RoleInstance;
import com.amalto.core.util.RoleSpecification;

import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ObjectPOJO.class, LocalUser.class })
public class DefaultCustomFormTest extends TestCase {

    public void testGetUserCustomForm() throws Exception {
        String datamodel = "Product"; //$NON-NLS-1$
        String entity = "Product"; //$NON-NLS-1$
        String customFormName = "Product2Columns"; //$NON-NLS-1$
        String pk = "Product..Product..Product2Columns"; //$NON-NLS-1$
        String objectType = "Custom Layout"; //$NON-NLS-1$
        String[] roles = { "authenticated", "UIAuthenticated", "System_Interactive", "Demo_Manager", "Demo_user" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
        String method = "load"; //$NON-NLS-1$

        HashSet<String> roleNames = new HashSet<>(Arrays.asList(roles));

        CustomFormPOJOPK cfpk = new CustomFormPOJOPK(datamodel, entity, customFormName);

        // role System_Interactive
        PowerMockito.mockStatic(LocalUser.class);
        ILocalUser mockUser = PowerMockito.mock(ILocalUser.class);
        PowerMockito.when(mockUser.getRoles()).thenReturn(roleNames);
        PowerMockito.when(LocalUser.getLocalUser()).thenReturn(mockUser);

        PowerMockito.mockStatic(ObjectPOJO.class);
        RolePOJO mockRolePojo_system_interactive = PowerMockito.mock(RolePOJO.class);
        PowerMockito.when(mockRolePojo_system_interactive.getRoleSpecifications())
                .thenReturn(new HashMap<String, RoleSpecification>());
        PowerMockito.when(ObjectPOJO.class, method, Mockito.same(RolePOJO.class), Mockito.eq(new RolePOJOPK(roles[2])))
                .thenReturn(mockRolePojo_system_interactive);

        // role Demo_Manager
        RolePOJO mockRolePojo_DemoManager = PowerMockito.mock(RolePOJO.class);
        RoleSpecification specification = new RoleSpecification();
        specification.setAdmin(false);
        HashMap<String, RoleInstance> instances = new HashMap<String, RoleInstance>();
        instances.put(pk, new RoleInstance());
        specification.setInstances(instances);
        HashMap<String, RoleSpecification> roleSpecifications = new HashMap<String, RoleSpecification>();
        roleSpecifications.put(objectType, specification);
        PowerMockito.when(mockRolePojo_DemoManager.getRoleSpecifications()).thenReturn(roleSpecifications);
        PowerMockito.when(ObjectPOJO.class, method, Mockito.same(RolePOJO.class), Mockito.eq(new RolePOJOPK(roles[3])))
                .thenReturn(mockRolePojo_DemoManager);

        CustomFormPOJO customFormPojo = new CustomFormPOJO();
        customFormPojo.setDatamodel(datamodel);
        customFormPojo.setEntity(entity);
        customFormPojo.setName(customFormName);
        PowerMockito.when(ObjectPOJO.class, method, Mockito.same(CustomFormPOJO.class),
                Mockito.eq(new ObjectPOJOPK(pk.split("\\.\\.")))).thenReturn(customFormPojo); //$NON-NLS-1$

        // role Demo_user
        RolePOJO mockRolePojo_DemoUser = PowerMockito.mock(RolePOJO.class);
        PowerMockito.when(mockRolePojo_DemoUser.getRoleSpecifications()).thenReturn(new HashMap<String, RoleSpecification>());
        PowerMockito.when(ObjectPOJO.class, method, Mockito.same(RolePOJO.class), Mockito.eq(new RolePOJOPK(roles[4])))
                .thenReturn(mockRolePojo_DemoUser);

        // not permit on all instance
        DefaultCustomForm defaultCustomForm = new DefaultCustomForm();
        CustomFormPOJO userCustomForm = defaultCustomForm.getUserCustomForm(cfpk);
        assertNotNull(userCustomForm);
        assertEquals(customFormPojo, userCustomForm);

        // permit on all instance
        specification.setAdmin(true);
        specification.getInstances().clear();
        userCustomForm = defaultCustomForm.getUserCustomForm(cfpk);
        assertNotNull(userCustomForm);
        assertEquals(customFormPojo, userCustomForm);

        // one role permit on all instance,another permit on special instance
        HashMap<String, RoleSpecification> demoUserRoleSpecifications = new HashMap<String, RoleSpecification>();
        RoleSpecification specification1 = new RoleSpecification();
        specification1.setAdmin(false);
        HashMap<String, RoleInstance> instances1 = new HashMap<String, RoleInstance>();
        instances1.put(pk, new RoleInstance());
        specification1.setInstances(instances1);
        demoUserRoleSpecifications.put(objectType, specification1);
        PowerMockito.when(mockRolePojo_DemoUser.getRoleSpecifications()).thenReturn(demoUserRoleSpecifications);
        cfpk.setName(""); //$NON-NLS-1$
        userCustomForm = defaultCustomForm.getUserCustomForm(cfpk);
        assertNotNull(userCustomForm);
        assertEquals(customFormPojo, userCustomForm);
    }
}
