/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.model;

import org.talend.mdm.webapp.base.client.model.ItemResult;

import junit.framework.TestCase;

/**
 * DOC Starkey  class global comment. Detailled comment
 */
@SuppressWarnings("nls")
public class ItemBeanStatusTest extends TestCase {

    /**
     * DOC Starkey Comment method "testLastUpdateTime".
     */

    public void testLastUpdateTime() {

        long myInitTimeToken=1327653438644l;
       
        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.set("time", myInitTimeToken);

        ItemBean itembean = new ItemBean();
        itembean.setLastUpdateTime(itemNodeModel);

        assertEquals(itembean.getLastUpdateTime(), myInitTimeToken);
        
        ItemResult itemResult1=new ItemResult(ItemResult.SUCCESS, "", "Test.test.1");
        itembean.setLastUpdateTime(itemResult1);

        assertEquals(itembean.getLastUpdateTime(), myInitTimeToken);
        
        long myUpdatedTimeToken=System.currentTimeMillis();
        ItemResult itemResult2 = new ItemResult(ItemResult.SUCCESS, "", "Test.test.1", myUpdatedTimeToken);
        itembean.setLastUpdateTime(itemResult2);

        assertEquals(itembean.getLastUpdateTime(), myUpdatedTimeToken);

    }

}
