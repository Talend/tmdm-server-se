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

import org.apache.commons.lang.StringUtils;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.talend.mdm.commmon.util.core.ICoreConstants;

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
import com.amalto.core.util.XtentisException;

import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ ObjectPOJO.class, LocalUser.class })
public class DefaultCustomFormTest extends TestCase {

    private static final String DEMO_USER = "Demo_user";

    private static final String DEMO_MANAGER = "Demo_Manager";

    private String datamodel = "Product"; //$NON-NLS-1$

    private String entity = "Product"; //$NON-NLS-1$

    private String customFormName = "Product2Columns"; //$NON-NLS-1$

    private String pk = "Product..Product..Product2Columns"; //$NON-NLS-1$

    private String objectType = "Custom Layout"; //$NON-NLS-1$

    private String method = "load"; //$NON-NLS-1$

    private CustomFormPOJOPK cfpk;

    private DefaultCustomForm defaultCustomForm;

    public void setUp() {
        cfpk = new CustomFormPOJOPK(datamodel, entity, customFormName);
        defaultCustomForm = new DefaultCustomForm();
    }

    public void testGetUserCustomForm() throws Exception {
        String[] roles = { ICoreConstants.AUTHENTICATED_PERMISSION, ICoreConstants.UI_AUTHENTICATED_PERMISSION,
                ICoreConstants.SYSTEM_INTERACTIVE_ROLE, DEMO_MANAGER, DEMO_USER };

        mockLocalUser(roles);
        mockRolePOJO(roles[2]);
        RoleSpecification specification = mockSpecificationForRole();
        CustomFormPOJO customFormPojo = mockCustomerFormPOJO();

        // role Demo_user
        RolePOJO mockRolePojo_DemoUser = PowerMockito.mock(RolePOJO.class);
        PowerMockito.when(mockRolePojo_DemoUser.getRoleSpecifications()).thenReturn(new HashMap<String, RoleSpecification>());
        PowerMockito.when(ObjectPOJO.class, method, Mockito.same(RolePOJO.class), Mockito.eq(new RolePOJOPK(roles[4])))
                .thenReturn(mockRolePojo_DemoUser);

        // not permit on all instance
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
        cfpk.setName(StringUtils.EMPTY);
        userCustomForm = defaultCustomForm.getUserCustomForm(cfpk);
        assertNotNull(userCustomForm);
        assertEquals(customFormPojo, userCustomForm);
    }

    public void testGetUserCustomFormForSuperAdmin() throws Exception {
        String[] roles = { ICoreConstants.AUTHENTICATED_PERMISSION, ICoreConstants.UI_AUTHENTICATED_PERMISSION,
                ICoreConstants.SYSTEM_ADMIN_ROLE, ICoreConstants.ADMIN_PERMISSION };
        mockLocalUser(roles);

        mockRolePOJO(roles[2]);
        mockSpecificationForRole();
        CustomFormPOJO customFormPojo = mockCustomerFormPOJO();

        // for super admin role, can get the customer layout
        CustomFormPOJO userCustomForm = defaultCustomForm.getUserCustomForm(cfpk);
        assertNotNull(userCustomForm);
        assertEquals(customFormPojo, userCustomForm);
    }

    public void testGetUserCustomFormForAdministrator() throws Exception {
        String[] roles = { ICoreConstants.AUTHENTICATED_PERMISSION, ICoreConstants.UI_AUTHENTICATED_PERMISSION,
                ICoreConstants.ADMIN_PERMISSION };
        HashSet<String> roleNames = new HashSet<>(Arrays.asList(roles));
        ILocalUser mockUser = mockLocalUser(roles);

        mockRolePOJO(roles[2]);
        mockSpecificationForRole();
        CustomFormPOJO customFormPojo = mockCustomerFormPOJO();

        // only Administration role, can't get the any customer layout
        CustomFormPOJO userCustomForm = defaultCustomForm.getUserCustomForm(cfpk);
        assertNotNull(userCustomForm);
        assertEquals(customFormPojo, userCustomForm);
    }

    public void testGetUserCustomFormForSystemWeb() throws Exception {
        String[] roles = { ICoreConstants.AUTHENTICATED_PERMISSION, ICoreConstants.UI_AUTHENTICATED_PERMISSION,
                ICoreConstants.SYSTEM_WEB_ROLE };
        HashSet<String> roleNames = new HashSet<>(Arrays.asList(roles));
        ILocalUser mockUser = mockLocalUser(roles);

        mockRolePOJO(roles[2]);
        mockSpecificationForRole();
        CustomFormPOJO customFormPojo = mockCustomerFormPOJO();

        // only SYSTEM_WEB_ROLE role, can't get the any customer layout
        CustomFormPOJO userCustomForm = defaultCustomForm.getUserCustomForm(cfpk);
        assertNull(userCustomForm);

        // add the DEMO_MANAGER role for local user
        roleNames.add(DEMO_MANAGER);
        PowerMockito.when(mockUser.getRoles()).thenReturn(roleNames);

        userCustomForm = defaultCustomForm.getUserCustomForm(cfpk);
        assertNotNull(userCustomForm);
        assertEquals(customFormPojo, userCustomForm);
    }

    public void testGetUserCustomFormForInteractive() throws Exception {
        String[] roles = { ICoreConstants.AUTHENTICATED_PERMISSION, ICoreConstants.UI_AUTHENTICATED_PERMISSION,
                ICoreConstants.SYSTEM_INTERACTIVE_ROLE };
        HashSet<String> roleNames = new HashSet<>(Arrays.asList(roles));
        ILocalUser mockUser = mockLocalUser(roles);

        mockRolePOJO(roles[2]);
        mockSpecificationForRole();
        CustomFormPOJO customFormPojo = mockCustomerFormPOJO();

        // only SYSTEM_INTERACTIVE_ROLE role, can't get the any customer layout
        CustomFormPOJO userCustomForm = defaultCustomForm.getUserCustomForm(cfpk);
        assertNull(userCustomForm);

        // add the DEMO_MANAGER role for local user
        roleNames.add(DEMO_MANAGER);
        PowerMockito.when(mockUser.getRoles()).thenReturn(roleNames);

        userCustomForm = defaultCustomForm.getUserCustomForm(cfpk);
        assertNotNull(userCustomForm);
        assertEquals(customFormPojo, userCustomForm);
    }

    protected CustomFormPOJO mockCustomerFormPOJO() throws Exception {
        CustomFormPOJO customFormPojo = new CustomFormPOJO();
        customFormPojo.setDatamodel(datamodel);
        customFormPojo.setEntity(entity);
        customFormPojo.setName(customFormName);
        PowerMockito.when(ObjectPOJO.class, method, Mockito.same(CustomFormPOJO.class),
                Mockito.eq(new ObjectPOJOPK(pk.split("\\.\\.")))).thenReturn(customFormPojo); //$NON-NLS-1$
        return customFormPojo;
    }

    protected RoleSpecification mockSpecificationForRole() throws Exception {
        HashMap<String, RoleSpecification> roleSpecifications = mockRoleSpecification();

        RolePOJO mockRolePojo = PowerMockito.mock(RolePOJO.class);
        PowerMockito.when(mockRolePojo.getRoleSpecifications()).thenReturn(roleSpecifications);
        PowerMockito.when(ObjectPOJO.class, method, Mockito.same(RolePOJO.class), Mockito.eq(new RolePOJOPK(DEMO_MANAGER)))
                .thenReturn(mockRolePojo);
        return roleSpecifications.get(objectType);
    }

    protected HashMap<String, RoleSpecification> mockRoleSpecification() {
        RoleSpecification specification = new RoleSpecification();
        specification.setAdmin(false);
        HashMap<String, RoleInstance> instances = new HashMap<String, RoleInstance>();
        instances.put(pk, new RoleInstance());
        specification.setInstances(instances);
        HashMap<String, RoleSpecification> roleSpecifications = new HashMap<String, RoleSpecification>();
        roleSpecifications.put(objectType, specification);
        return roleSpecifications;
    }

    protected ILocalUser mockLocalUser(String[] roles) throws XtentisException {
        HashSet<String> roleNames = new HashSet<>(Arrays.asList(roles));
        PowerMockito.mockStatic(LocalUser.class);
        ILocalUser mockUser = PowerMockito.mock(ILocalUser.class);
        PowerMockito.when(mockUser.getRoles()).thenReturn(roleNames);
        PowerMockito.when(LocalUser.getLocalUser()).thenReturn(mockUser);
        return mockUser;
    }

    protected ILocalUser mockLocalUser(HashSet<String> roleNames) throws XtentisException {
        PowerMockito.mockStatic(LocalUser.class);
        ILocalUser mockUser = PowerMockito.mock(ILocalUser.class);
        PowerMockito.when(mockUser.getRoles()).thenReturn(roleNames);
        PowerMockito.when(LocalUser.getLocalUser()).thenReturn(mockUser);
        return mockUser;
    }

    protected void mockRolePOJO(String role) throws Exception {
        PowerMockito.mockStatic(ObjectPOJO.class);
        RolePOJO mockRolePojo = PowerMockito.mock(RolePOJO.class);
        PowerMockito.when(mockRolePojo.getRoleSpecifications()).thenReturn(new HashMap<String, RoleSpecification>());
        PowerMockito.when(ObjectPOJO.class, method, Mockito.same(RolePOJO.class), Mockito.eq(new RolePOJOPK(role)))
                .thenReturn(mockRolePojo);
    }
}
