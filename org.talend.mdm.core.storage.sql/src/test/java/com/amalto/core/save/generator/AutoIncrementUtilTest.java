/*
 * Copyright (C) 2006-2020 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 *  %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save.generator;

import com.amalto.core.delegator.BaseSecurityCheck;
import com.amalto.core.delegator.BeanDelegatorContainer;
import com.amalto.core.delegator.MockILocalUser;
import com.amalto.core.server.MockMetadataRepositoryAdmin;
import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@SuppressWarnings("nls")
public class AutoIncrementUtilTest {
    private static final Logger LOG = Logger.getLogger(AutoIncrementUtilTest.class);

    private static boolean beanDelegatorContainerFlag = false;
    private static class MockISecurityCheck extends BaseSecurityCheck {

    }

    private static void createBeanDelegatorContainer() {
        if (!beanDelegatorContainerFlag) {
            BeanDelegatorContainer.createInstance();
            beanDelegatorContainerFlag = true;
        }
    }

    @BeforeClass
    public static void setUp() {
        LOG.info("Setting up MDM server environment...");
        ServerContext.INSTANCE.get(new MockServerLifecycle());
        MDMConfiguration.getConfiguration().setProperty("xmlserver.class", "com.amalto.core.storage.DispatchWrapper");
        LOG.info("MDM server environment set.");

        Map<String, Object> delegatorInstancePool = new HashMap<>();
        delegatorInstancePool.put("LocalUser", new MockILocalUser());
        delegatorInstancePool.put("SecurityCheck", new AutoIncrementUtilTest.MockISecurityCheck());
        createBeanDelegatorContainer();
        BeanDelegatorContainer.getInstance().setDelegatorInstancePool(delegatorInstancePool);

    }

    @Test
    public void getAutoIncrementFieldName_ForNormalAutoField() {
        MetadataRepository repository = new MetadataRepository();
        repository.load(AutoIncrementUtilTest.class.getResourceAsStream("metadata01.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("AutoInc", repository);

        assertNull(AutoIncrementUtil.getAutoIncrementFieldPath(null, "Person", "Person.Id"));
        assertNull(AutoIncrementUtil.getAutoIncrementFieldPath("Person", null, "Person.Id"));
        assertNull(AutoIncrementUtil.getAutoIncrementFieldPath("Person", "Person", null));
        assertEquals("Id", AutoIncrementUtil.getAutoIncrementFieldPath("Person", "Person", "Person.Id"));
        assertEquals("AA", AutoIncrementUtil.getAutoIncrementFieldPath("Person", "Person", "Person.AA"));
        assertEquals("BB", AutoIncrementUtil.getAutoIncrementFieldPath("Person", "Person", "Person.BB"));
        assertEquals("CC", AutoIncrementUtil.getAutoIncrementFieldPath("Person", "Person", "Person.CC"));

    }

    @Test
    public void getAutoIncrementFieldName_ForNormalAutoField_2() {
        MetadataRepository repository = new MetadataRepository();
        repository.load(AutoIncrementUtilTest.class.getResourceAsStream("metadata02.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("AutoInc", repository);

        assertEquals("Id", AutoIncrementUtil.getAutoIncrementFieldPath("Product", "Product", "Product.Id"));
        assertEquals("Support", AutoIncrementUtil.getAutoIncrementFieldPath("Product", "Product", "Product.Support"));
        assertEquals("Id", AutoIncrementUtil.getAutoIncrementFieldPath("Product", "ProductFamily", "ProductFamily.Id"));
        assertEquals("Name", AutoIncrementUtil.getAutoIncrementFieldPath("Product", "ProductFamily", "ProductFamily.Name"));
    }

    @Test
    public void getAutoIncrementFieldName_ForComplexType() {
        MetadataRepository repository = new MetadataRepository();
        repository.load(AutoIncrementUtilTest.class.getResourceAsStream("metadata03.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("AutoInc", repository);

        assertEquals("Site", AutoIncrementUtil.getAutoIncrementFieldPath("Student", "Student", "Student.Site"));
        assertEquals("Course.Score", AutoIncrementUtil.getAutoIncrementFieldPath("Student", "Student", "Course.Score"));
    }

    @Test
    public void getAutoIncrementFieldName_ForInherited() {
        MetadataRepository repository = new MetadataRepository();
        repository.load(AutoIncrementUtilTest.class.getResourceAsStream("metadata04.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Party", repository);

        assertEquals("Id", AutoIncrementUtil.getAutoIncrementFieldPath("Party", "A", "B.Id"));
        assertEquals("Id", AutoIncrementUtil.getAutoIncrementFieldPath("Party", "A", "C.Id"));
    }

    @Test
    public void getAutoIncrementFieldName_ForMultipleLayerInComplex() {
        MetadataRepository repository = new MetadataRepository();
        repository.load(AutoIncrementUtilTest.class.getResourceAsStream("metadata05.xsd"));
        MockMetadataRepositoryAdmin.INSTANCE.register("Party", repository);
        ComplexTypeMetadata type = repository.getComplexType("Person");

        assertEquals("Habit.Detail.Count",
                AutoIncrementUtil.getAutoIncrementFieldPath("Person", "Person", "Habit.Detail.Count"));
    }
}