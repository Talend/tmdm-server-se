/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */
package com.amalto.core.util;

import java.util.HashSet;
import com.amalto.core.objects.role.RolePOJO;
import com.amalto.core.webservice.WSRole;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.xmlserver.interfaces.WhereCondition;

import junit.framework.TestCase;

public class XConverterTest extends TestCase {
    
    public void testRolePOJOWS2POJO() throws Exception {
        String objectType = "View"; //$NON-NLS-1$
        String instanceName = "Browse_items_ProductFamily"; //$NON-NLS-1$
        
        com.amalto.core.webservice.WSRoleSpecificationInstance wsRoleInstance = new com.amalto.core.webservice.WSRoleSpecificationInstance();
        wsRoleInstance.setInstanceName(instanceName);
        wsRoleInstance.setWritable(true);
        wsRoleInstance.setParameter(new String[]{"<?xml version='1.0' encoding='UTF-8'?><role-where-condition><predicate>And</predicate><right-value-or-path>'Shirts'</right-value-or-path><operator>=</operator><left-path>ProductFamily/Name</left-path></role-where-condition>",  //$NON-NLS-1$
                "<?xml version='1.0' encoding='UTF-8'?><role-where-condition><predicate>Or</predicate><right-value-or-path>'Pending'</right-value-or-path><operator>=</operator><left-path>ProductFamily/ChangeStatus</left-path></role-where-condition>", //$NON-NLS-1$
                "<?xml version='1.0' encoding='UTF-8'?><role-where-condition><predicate>And</predicate><right-value-or-path>'Hats'</right-value-or-path><operator>=</operator><left-path>ProductFamily/Name</left-path></role-where-condition>",  //$NON-NLS-1$
                "<?xml version='1.0' encoding='UTF-8'?><role-where-condition><predicate></predicate><right-value-or-path>'Pending'</right-value-or-path><operator>=</operator><left-path>ProductFamily/ChangeStatus</left-path></role-where-condition>"}); //$NON-NLS-1$
        
        com.amalto.core.webservice.WSRoleSpecification wsRoleSpecification = new com.amalto.core.webservice.WSRoleSpecification();
        wsRoleSpecification.setAdmin(false);
        wsRoleSpecification.setInstance(new com.amalto.core.webservice.WSRoleSpecificationInstance[]{wsRoleInstance});
        wsRoleSpecification.setObjectType(objectType);
        
        WSRole wsRole = new WSRole();
        wsRole.setName("EU_PRDMDM_PM_LOGISTIC"); //$NON-NLS-1$
        wsRole.setDescription("[EN:Normal User]"); //$NON-NLS-1$
        wsRole.setSpecification(new com.amalto.core.webservice.WSRoleSpecification[]{wsRoleSpecification});
        RolePOJO rolePOJO = XConverter.WS2POJO(wsRole);
        
        assertNotNull(rolePOJO);
        HashSet<?> parameters = rolePOJO.getRoleSpecifications().get(objectType).getInstances().get(instanceName).getParameters();
        String[] roleParameters = parameters.toArray(new String[parameters.size()]);
        
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><role-where-condition><predicate>And</predicate><right-value-or-path>'Shirts'</right-value-or-path><operator>=</operator><left-path>ProductFamily/Name</left-path></role-where-condition>", roleParameters[0]); //$NON-NLS-1$
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><role-where-condition><predicate>Or</predicate><right-value-or-path>'Pending'</right-value-or-path><operator>=</operator><left-path>ProductFamily/ChangeStatus</left-path></role-where-condition>", roleParameters[1]); //$NON-NLS-1$
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><role-where-condition><predicate>And</predicate><right-value-or-path>'Hats'</right-value-or-path><operator>=</operator><left-path>ProductFamily/Name</left-path></role-where-condition>", roleParameters[2]); //$NON-NLS-1$
        assertEquals("<?xml version='1.0' encoding='UTF-8'?><role-where-condition><predicate></predicate><right-value-or-path>'Pending'</right-value-or-path><operator>=</operator><left-path>ProductFamily/ChangeStatus</left-path></role-where-condition>", roleParameters[3]); //$NON-NLS-1$
    }
    
    public void testVO2WSWithNonePredicate() throws Exception {
        WhereCondition whereCondition1 = new WhereCondition("Party/Contactpoints/ContactpointRel/FkContactpointID", "JOINS", "Contactpoint/MDMContactpointID", "&");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$//$NON-NLS-4$
        WSWhereCondition wsWhereCondition = XConverter.VO2WS(whereCondition1);
        assertEquals("Party/Contactpoints/ContactpointRel/FkContactpointID", wsWhereCondition.getLeftPath()); //$NON-NLS-1$
        assertEquals(com.amalto.core.webservice.WSWhereOperator.JOIN, wsWhereCondition.getOperator());
        assertEquals("Contactpoint/MDMContactpointID", wsWhereCondition.getRightValueOrPath()); //$NON-NLS-1$
        assertEquals(com.amalto.core.webservice.WSStringPredicate.AND, wsWhereCondition.getStringPredicate());
        
        WhereCondition whereCondition2 = new WhereCondition("Party/Contactpoints/ContactpointRel/FkContactpointID", "JOINS", "Contactpoint/MDMContactpointID", "");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        wsWhereCondition = XConverter.VO2WS(whereCondition2);
        assertEquals(com.amalto.core.webservice.WSStringPredicate.AND, wsWhereCondition.getStringPredicate());
        
        WhereCondition whereCondition3 = new WhereCondition("Party/Contactpoints/ContactpointRel/FkContactpointID", "JOINS", "Contactpoint/MDMContactpointID", null);  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        wsWhereCondition = XConverter.VO2WS(whereCondition3);
        assertEquals(com.amalto.core.webservice.WSStringPredicate.NONE, wsWhereCondition.getStringPredicate());
    }
}
