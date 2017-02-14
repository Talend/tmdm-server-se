/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.load;

import junit.framework.TestCase;

import org.talend.mdm.commmon.util.core.MDMConfiguration;

import com.amalto.core.load.action.DefaultLoadAction;
import com.amalto.core.load.action.LoadAction;
import com.amalto.core.load.action.OptimizedLoadAction;
import com.amalto.core.objects.datacluster.DataClusterPOJO;
import com.amalto.core.servlet.LoadServlet;

/**
 *
 */
public class LoadServletTest extends TestCase {

    public static final String TEST_DATA_CLUSTER = "TestDataCluster";

    static {
        MDMConfiguration.createConfiguration(LoadServletTest.class.getResource("/org/talend/mdm/commmon/util/core/mdm.conf").getFile(), false);
    }

    public void testOptimizationSelection() {
        LoadServletTestFixture servlet = getFixture();

        LoadAction loadAction = servlet.getLoadAction(false);
        assertEquals(OptimizedLoadAction.class, loadAction.getClass());
        assertFalse(loadAction.supportValidation());

        loadAction = servlet.getLoadAction(true);
        assertEquals(DefaultLoadAction.class, loadAction.getClass());
        assertTrue(loadAction.supportValidation());

    }

    public void testNullArguments() {
        LoadServletTestFixture servlet = getFixture();

        LoadAction loadAction = servlet.getLoadAction(TEST_DATA_CLUSTER, null, null, false);
        assertEquals(OptimizedLoadAction.class, loadAction.getClass());
        assertFalse(loadAction.supportValidation());

        loadAction = servlet.getLoadAction(TEST_DATA_CLUSTER, null, null, true);
        assertEquals(DefaultLoadAction.class, loadAction.getClass());
        assertTrue(loadAction.supportValidation());
    }

    public void testNonExistingDataCluster() {
        LoadServletTestFixture servlet = getFixture();

        // Qizx database
        try {
            servlet.getLoadAction("NonExistingDataCluster", null, null, false);
            fail("Expected an error (data cluster does not exist)");
        } catch (IllegalArgumentException e) {
            // Expected
        }

    }

    private LoadServletTestFixture getFixture() {
        return new LoadServletTestFixture(TEST_DATA_CLUSTER);
    }

    private class LoadServletTestFixture extends LoadServlet {

        private static final long serialVersionUID = 7664145661072675577L;

        private final String[] dataClusterNames;

        private LoadServletTestFixture(String... dataClusterNames) {
            this.dataClusterNames = dataClusterNames;
        }

        public LoadAction getLoadAction(boolean needValidate) {
            return getLoadAction(TEST_DATA_CLUSTER, "", "", needValidate, false);
        }

        public LoadAction getLoadAction(String dataClusterName, String typeName, String dataModelName, boolean needValidate) {
            return getLoadAction(dataClusterName, typeName, dataModelName, needValidate, false);
        }

        @Override
        protected DataClusterPOJO getDataCluster(String dataClusterName) {
            for (String clusterName : dataClusterNames) {
                if (clusterName.equals(dataClusterName)) {
                    return new DataClusterPOJO(clusterName);
                }
            }

            return null;
        }
    }

}
