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

import java.util.ArrayList;
import java.util.Collection;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.ObjectPOJOPK;
import com.amalto.core.objects.role.RolePOJO;
import com.amalto.core.objects.role.RolePOJOPK;
import com.amalto.core.server.api.Role;
import com.amalto.core.util.Util;

import junit.framework.TestCase;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ com.amalto.core.objects.ObjectPOJO.class })
public class DefaultRoleTest extends TestCase {
    
    @Override
    public void setUp() throws Exception {
    }
    
    public void testLoadAllRolePKS() throws Exception {
        ArrayList<ObjectPOJOPK> list = new ArrayList<ObjectPOJOPK>();
        list.add(new ObjectPOJOPK("SystemAdmin")); //$NON-NLS-1$
        list.add(new ObjectPOJOPK("DemoManager")); //$NON-NLS-1$
        list.add(new ObjectPOJOPK("DemoUser")); //$NON-NLS-1$
        list.add(new ObjectPOJOPK("User")); //$NON-NLS-1$
        
        PowerMockito.mockStatic(ObjectPOJO.class);
        PowerMockito.when(ObjectPOJO.findAllPKs(RolePOJO.class, "*")).thenReturn(list); //$NON-NLS-1$
        
        Role ctrl = Util.getRoleCtrlLocal();
        Collection<RolePOJOPK> c = ctrl.getRolePKs("*"); //$NON-NLS-1$
        
        assertEquals(4, c.size());
        assertEquals("DemoManager", ((ArrayList<RolePOJOPK>)c).get(0).getUniqueId()); //$NON-NLS-1$
        assertEquals("DemoUser", ((ArrayList<RolePOJOPK>)c).get(1).getUniqueId()); //$NON-NLS-1$
        assertEquals("SystemAdmin", ((ArrayList<RolePOJOPK>)c).get(2).getUniqueId()); //$NON-NLS-1$
        assertEquals("User", ((ArrayList<RolePOJOPK>)c).get(3).getUniqueId()); //$NON-NLS-1$
    }
}
