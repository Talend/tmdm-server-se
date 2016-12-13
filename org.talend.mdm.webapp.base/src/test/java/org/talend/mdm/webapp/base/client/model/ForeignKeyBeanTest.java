/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.model;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class ForeignKeyBeanTest extends TestCase {

    
    private ForeignKeyBean mockForeignKeyBean(){        
        ForeignKeyBean bean = new ForeignKeyBean();
        bean.set("Name", "Apple Store");
        bean.set("Id", "1");
        bean.set("Code", "10001");
        bean.set("i", "1");
        return bean;
    }
    
    public void testToString(){
        ForeignKeyBean bean = mockForeignKeyBean();
        assertNull(bean.toString());

        String id = "[1]";
        bean.setId(id);
        assertNotNull(bean.toString());
        assertEquals(id, bean.toString());

        bean.getForeignKeyInfo().put("Store/Id", "1");
        bean.getForeignKeyInfo().put("Store/Code", "10001");
        bean.getForeignKeyInfo().put("Store/Name", "Apple Store");
        assertNotNull(bean.toString());
        assertNotSame(id, bean.toString());
        assertEquals("1-10001-Apple Store", bean.toString());

        String displayInfo = "1-10001-Apple Store";
        bean.setDisplayInfo(displayInfo);
        assertNotNull(bean.toString());
        assertEquals(displayInfo, bean.toString());

        // exist foreignKeyInfo, but value is empty and displayInfo is also empty
        bean.getForeignKeyInfo().clear();
        bean.setDisplayInfo("");
        bean.getForeignKeyInfo().put("Store/Name", "");
        assertNotNull(bean.toString());
        assertEquals(bean.getId(), bean.toString());

        // exist foreignKeyInfo, but value is empty and displayInfo is not empty
        bean.setDisplayInfo("You are welcome");
        assertNotNull(bean.toString());
        assertEquals(bean.getDisplayInfo(), bean.toString());
    }

}
