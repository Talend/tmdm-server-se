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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.junit.BeforeClass;
import org.junit.Test;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("nls")
public class AutoIncrementUtilTest {
    private static final Logger LOG = LogManager.getLogger(AutoIncrementUtilTest.class);

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

    @Test
    public void generatedNormalField() throws Exception {
        Set<String> normalFields = new HashSet<>();
        normalFields.add("Account");
        normalFields.add("Site");
        normalFields.add("Course/Score");
        normalFields.add("Course/Like");

        String recordXml = "<Student><Id>3</Id><Name>John</Name><Age>23</Age><Site>20</Site><Course><Id>English</Id><Teacher>Mike</Teacher><Like/><Score>10</Score></Course></Student>";

        String[] result = AutoIncrementUtil.getAutoNormalFieldsToGenerate(normalFields, recordXml);
        assertEquals("Account", result[0]);
        assertEquals("Course/Like", result[1]);

        recordXml ="<Student><Id>3</Id><Account/><Name>John</Name><Age>23</Age><Site>20</Site><Course><Id>English</Id><Teacher>Mike</Teacher><Score/></Course></Student>";
        result = AutoIncrementUtil.getAutoNormalFieldsToGenerate(normalFields, recordXml);
        assertEquals("Account", result[0]);
        assertEquals("Course/Like", result[1]);
        assertEquals("Course/Score", result[2]);

        recordXml ="<Student><Id>3</Id><Account/><Name>John</Name><Age>23</Age><Site>20</Site></Student>";
        result = AutoIncrementUtil.getAutoNormalFieldsToGenerate(normalFields, recordXml);
        assertEquals("Account", result[0]);

        recordXml ="<Student><Id>5</Id><Account/><Name>John</Name><Age>23</Age><Course><Id>English</Id><Teacher>Mike</Teacher></Course></Student><Student><Id>6</Id><Name>John</Name><Age>23</Age><Course><Id>English</Id><Teacher>Mike</Teacher></Course></Student><Student><Id>7</Id><Name>John</Name><Age>23</Age><Course><Id>English</Id><Teacher>Mike</Teacher></Course></Student>";
        result = AutoIncrementUtil.getAutoNormalFieldsToGenerate(normalFields, recordXml);
        assertEquals("Site", result[0]);
        assertEquals("Account", result[1]);

        normalFields.clear();

        normalFields.add("Account");

        recordXml ="<Student><Id>5</Id><Account/><Name>John</Name><Age>23</Age><Course><Id>English</Id><Teacher>Mike</Teacher></Course></Student>";
        result = AutoIncrementUtil.getAutoNormalFieldsToGenerate(normalFields, recordXml);
        assertEquals("Account", result[0]);
    }

    @Test
    public void testGetNormalAutoIncrementFields(){
        Set<String> normalFields = new HashSet<>();
        normalFields.add("Course/Like");
        normalFields.add("Course/Score");
        normalFields.add("Account");
        normalFields.add("Site");

        Set<String> results = AutoIncrementUtil.getNormalAutoIncrementFields("",normalFields);
        assertEquals(2, results.size());
        assertTrue(results.contains("Account"));
        assertTrue(results.contains("Site"));

        results = AutoIncrementUtil.getNormalAutoIncrementFields(null,normalFields);
        assertEquals(0, results.size());

        results = AutoIncrementUtil.getNormalAutoIncrementFields("Support",null);
        assertEquals(0, results.size());

        results = AutoIncrementUtil.getNormalAutoIncrementFields("Course",normalFields);
        assertEquals(2, results.size());
        assertTrue(results.contains("Course/Like"));
        assertTrue(results.contains("Course/Score"));

        results = AutoIncrementUtil.getNormalAutoIncrementFields("Support",normalFields);
        assertEquals(0, results.size());

        results = AutoIncrementUtil.getNormalAutoIncrementFields("Course/Score",normalFields);
        assertEquals(0, results.size());

        normalFields.add("Course/Score/Name");
        results = AutoIncrementUtil.getNormalAutoIncrementFields("Course/Score",normalFields);
        assertEquals(1, results.size());
        assertTrue(results.contains("Course/Score/Name"));
    }
}