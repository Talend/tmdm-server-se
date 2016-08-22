/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.metadata.compare;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.compare.Change;
import org.talend.mdm.commmon.metadata.compare.Compare;
import org.talend.mdm.commmon.metadata.compare.HibernateStorageImpactAnalyzer;
import org.talend.mdm.commmon.metadata.compare.ImpactAnalyzer;

import com.amalto.core.metadata.ClassRepository;
import com.amalto.core.objects.ObjectPOJO;
import com.amalto.core.objects.datamodel.DataModelPOJO;

@SuppressWarnings("nls")
public class CompareTest extends TestCase {

    private static Logger LOG = Logger.getLogger(CompareTest.class);

    public void test1() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema1_1.xsd"));
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema1_2.xsd"));
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(4, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(3, diffResults.getRemoveChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(2, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(2, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
    }

    public void test2() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema2_1.xsd"));
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema2_2.xsd"));
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(1, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test3() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema3_1.xsd"));
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema3_2.xsd"));
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(1, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test4() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema4_1.xsd"));
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema4_2.xsd"));
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test5() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema5_1.xsd"));
        original = original.copy();
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema5_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(0, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test6_SystemDMs_CONF() throws Exception {
        ClassRepository repository = buildRepository();
        String[] models = new String[] { "/com/amalto/core/initdb/data/datamodel/CONF" };
        for (String model : models) {
            InputStream builtInStream = this.getClass().getResourceAsStream(model);
            if (builtInStream == null) {
                throw new RuntimeException("Built in model '" + model + "' cannot be found.");
            }
            try {
                DataModelPOJO modelPOJO = ObjectPOJO.unmarshal(DataModelPOJO.class, IOUtils.toString(builtInStream, "UTF-8")); //$NON-NLS-1$
                repository.load(new ByteArrayInputStream(modelPOJO.getSchema().getBytes("UTF-8")));
            } catch (Exception e) {
                throw new RuntimeException("Could not parse builtin data model '" + model + "'.", e);
            } finally {
                try {
                    builtInStream.close();
                } catch (IOException e) {
                    // Ignored
                }
            }
        }
        LOG.info("Storage prepared.");

        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema6_2.xsd"));
        Compare.DiffResults diffResults = Compare.compare(repository, updated);

        assertEquals(30, diffResults.getActions().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(30, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test6_SystemDMs_PROVISIONING() throws Exception {
        ClassRepository repository = buildRepository();
        String[] models = new String[] { "/com/amalto/core/initdb/data/datamodel/PROVISIONING" };
        for (String model : models) {
            InputStream builtInStream = this.getClass().getResourceAsStream(model);
            if (builtInStream == null) {
                throw new RuntimeException("Built in model '" + model + "' cannot be found.");
            }
            try {
                DataModelPOJO modelPOJO = ObjectPOJO.unmarshal(DataModelPOJO.class, IOUtils.toString(builtInStream, "UTF-8")); //$NON-NLS-1$
                repository.load(new ByteArrayInputStream(modelPOJO.getSchema().getBytes("UTF-8")));
            } catch (Exception e) {
                throw new RuntimeException("Could not parse builtin data model '" + model + "'.", e);
            } finally {
                try {
                    builtInStream.close();
                } catch (IOException e) {
                    // Ignored
                }
            }
        }
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema8_2.xsd"));
        Compare.DiffResults diffResults = Compare.compare(repository, updated);

        assertEquals(30, diffResults.getActions().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(30, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test6_SystemDMs_SearchTemplate() throws Exception {
        ClassRepository repository = buildRepository();
        String[] models = new String[] { "/com/amalto/core/initdb/data/datamodel/SearchTemplate" };
        for (String model : models) {
            InputStream builtInStream = this.getClass().getResourceAsStream(model);
            if (builtInStream == null) {
                throw new RuntimeException("Built in model '" + model + "' cannot be found.");
            }
            try {
                DataModelPOJO modelPOJO = ObjectPOJO.unmarshal(DataModelPOJO.class, IOUtils.toString(builtInStream, "UTF-8")); //$NON-NLS-1$
                repository.load(new ByteArrayInputStream(modelPOJO.getSchema().getBytes("UTF-8")));
            } catch (Exception e) {
                throw new RuntimeException("Could not parse builtin data model '" + model + "'.", e);
            } finally {
                try {
                    builtInStream.close();
                } catch (IOException e) {
                    // Ignored
                }
            }
        }
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema9_2.xsd"));
        Compare.DiffResults diffResults = Compare.compare(repository, updated);

        assertEquals(30, diffResults.getActions().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(30, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test7() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema10_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema10_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test8() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema11_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema11_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test9() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema12_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema12_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(0, diffResults.getActions().size());
    }

    public void test10() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema13_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema13_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(1, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test11() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema14_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema14_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(5, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(5, diffResults.getAddChanges().size());
        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(2, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
        // Test messages (should get null messages).
        for (Map.Entry<ImpactAnalyzer.Impact, List<Change>> category : sort.entrySet()) {
            List<Change> changes = category.getValue();
            for (Change change : changes) {
                assertNotNull(change.getMessage(Locale.ENGLISH));
            }
        }
    }

    public void test15() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema15_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema15_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());

        updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema15_3.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(original, updated);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test16() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema16_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema16_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(4, diffResults.getActions().size());
        assertEquals(3, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(1, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }
    
    public void test17() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema17_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema17_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    // TMDM-9463 Impact analysis issues with custom decimal type
    public void test18() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema18_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema18_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());

        updated = new MetadataRepository();
        updated.load(CompareTest.class.getResourceAsStream("schema18_3.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(original, updated);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }
    
    // TMDM-9515 Impact Analyzer issue with Change a existing FK field should be considered as high change
    public void test19() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema19_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated1 = new MetadataRepository();
        updated1.load(CompareTest.class.getResourceAsStream("schema19_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated1);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());
        
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema19_3.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(updated1, updated2);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());
        
        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    // TMDM-9909 Increase the length of a string element should be low impact
    public void test20() {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema20_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated1 = new MetadataRepository();
        updated1.load(CompareTest.class.getResourceAsStream("schema20_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated1);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());

        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema20_3.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(original, updated2);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());

        // for the inherit, child has no maxlengh
        MetadataRepository updated3 = new MetadataRepository();
        updated3.load(CompareTest.class.getResourceAsStream("schema20_4.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(original, updated3);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());

        // for the inherit, child has maxlengh
        MetadataRepository updated4 = new MetadataRepository();
        updated4.load(CompareTest.class.getResourceAsStream("schema20_5.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(original, updated4);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());

        // for the inherit, child has maxlength
        MetadataRepository updated5 = new MetadataRepository();
        updated5.load(CompareTest.class.getResourceAsStream("schema20_6.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(original, updated5);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());

        // for multiple the inherit
        MetadataRepository updated6 = new MetadataRepository();
        updated6.load(CompareTest.class.getResourceAsStream("schema20_7.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(original, updated6);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());

        // for multiple the inherit
        MetadataRepository updated7 = new MetadataRepository();
        updated7.load(CompareTest.class.getResourceAsStream("schema20_8.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(original, updated7);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    @SuppressWarnings("rawtypes")
    private ClassRepository buildRepository() {
        ClassRepository repository = new ClassRepository();
        Class[] objectsToParse = new Class[ObjectPOJO.OBJECT_TYPES.length];
        int i = 0;
        for (Object[] objects : ObjectPOJO.OBJECT_TYPES) {
            objectsToParse[i++] = (Class) objects[1];
        }
        repository.load(objectsToParse);
        return repository;
    }
}
