/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
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
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());
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
        assertEquals(6, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(2, diffResults.getRemoveChanges().size());
        assertEquals(3, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
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

        diffResults = Compare.compare(updated6, updated7);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());

        // test for the product demo, modify predefination's type(PRICTURE)
        MetadataRepository productOriginal = new MetadataRepository();
        productOriginal.load(CompareTest.class.getResourceAsStream("Product.xsd")); //$NON-NLS-1$
        MetadataRepository productUpdated = new MetadataRepository();
        productUpdated.load(CompareTest.class.getResourceAsStream("Product_updated.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(productOriginal, productUpdated);
        assertEquals(0, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());

        // test for the product demo, modify Color type
        MetadataRepository productUpdated1 = new MetadataRepository();
        productUpdated1.load(CompareTest.class.getResourceAsStream("Product_updated_1.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(productOriginal, productUpdated1);
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

    // Test for change length attribute
    public void test21() {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema21_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema21_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.LOW).size());

        // maxLength and length mix(increase the maxLength)
        MetadataRepository updated3 = new MetadataRepository();
        updated3.load(CompareTest.class.getResourceAsStream("schema21_3.xsd")); //$NON-NLS-1$
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

        // test for reduce length
        MetadataRepository updated4 = new MetadataRepository();
        updated4.load(CompareTest.class.getResourceAsStream("schema21_4.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(updated3, updated4);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());

        // mix maxLength and length for inherit
        MetadataRepository updated5 = new MetadataRepository();
        updated5.load(CompareTest.class.getResourceAsStream("schema20_8.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(updated3, updated5);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(1, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());

        diffResults = Compare.compare(updated5, updated3);
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

    // TMDM-9086 test for add mandatory field with default value
    public void test22() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema22_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema22_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(4, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(4, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(4, sort.get(ImpactAnalyzer.Impact.LOW).size());

        // if the defalutValueRule=fn:name(), will be high
        MetadataRepository updated3 = new MetadataRepository();
        updated3.load(CompareTest.class.getResourceAsStream("schema22_3.xsd")); //$NON-NLS-1$
        diffResults = Compare.compare(original, updated3);
        assertEquals(1, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(1, diffResults.getAddChanges().size());

        analyzer = new HibernateStorageImpactAnalyzer();
        sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test23_option_to_mandatory_for_simpleType() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema23_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema23_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(6, diffResults.getActions().size());
        assertEquals(6, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());
    }

    public void test24_mandatory_to_option_for_simpleType() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("Product_mandatory.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("Product_optioanl.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(16, diffResults.getActions().size());
        assertEquals(16, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(14, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test25_mantory_to_option_for_complexType() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema25_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema25_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(8, diffResults.getActions().size());
        assertEquals(8, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(8, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test26_mantory_to_option_for_complexType() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema26_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema26_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
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

    public void test27_mantory_to_option_for_complexType() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema27_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema27_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
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

    public void test28_forChangeMany() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema28_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema28_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(11, diffResults.getActions().size());
        assertEquals(11, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(8, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test29_forComplexType() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema29_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema29_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(12, diffResults.getActions().size());
        assertEquals(12, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(9, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test30_forMovedField() throws Exception {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema30_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema30_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(25, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(25, diffResults.getRemoveChanges().size());
        assertEquals(0, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(20, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(5, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_non_anonymous_1() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-non-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (0-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_na_1.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(4, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(4, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(4, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_non_anonymous_2() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-non-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_na_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(4, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(4, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(4, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_non_anonymous_3() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-non-anonymous (ComplexType) (0-many)
         *                                                          |__do-subelement (SimpleField) (0-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_na_3.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(4, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(4, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(4, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_non_anonymous_4() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-non-anonymous (ComplexType) (0-many)
         *                                                          |__do-subelement (SimpleField) (1-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_na_4.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(4, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(4, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(4, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_non_anonymous_5() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-non-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-anonymous (ComplexType) (0-1)
         *                                                             |__bb-subelement (SimpleField) (0-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_na_5.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(7, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(7, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(7, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_non_anonymous_6() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-non-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-anonymous (ComplexType) (0-1)
         *                                                             |__bb-subelement (SimpleField) (1-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_na_6.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(7, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(7, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(7, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_non_anonymous_7() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-non-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-non-anonymous (ComplexType) (0-1)
         *                                                             |__bb-subelement (SimpleField) (0-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_na_7.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(8, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(8, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(8, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_non_anonymous_8() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-non-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-non-anonymous (ComplexType) (0-1)
         *                                                             |__bb-subelement (SimpleField) (1-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_na_8.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(8, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(8, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(8, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_non_anonymous_9() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-non-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-non-anonymous (ComplexType) (1-1)
         *                                                             |__bb-subelement (SimpleField) (0-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_na_9.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(8, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(8, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(8, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_non_anonymous_10() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-non-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-anonymous (ComplexType) (1-1)
         *                                                             |__bb-subelement (SimpleField) (0-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_na_10.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(7, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(7, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(7, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_anonymous_1() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (0-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_a_1.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(3, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(3, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_anonymous_2() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         */
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_a_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(3, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(3, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_anonymous_3() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-anonymous (ComplexType) (0-many)
         *                                                          |__do-subelement (SimpleField) (0-1)
         */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_a_3.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(3, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(3, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_anonymous_4() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-anonymous (ComplexType) (0-many)
         *                                                          |__do-subelement (SimpleField) (1-1)
         */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_a_4.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(3, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(3, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_anonymous_5() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-anonymous (ComplexType) (0-1)
         *                                                             |__bb-subelement (SimpleField) (0-1)
         */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_a_5.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(6, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(6, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(6, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_anonymous_6() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-anonymous (ComplexType) (0-1)
         *                                                             |__bb-subelement (SimpleField) (1-1)
         */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_a_6.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(6, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(6, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(6, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_anonymous_7() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-non-anonymous (ComplexType) (0-1)
         *                                                             |__bb-subelement (SimpleField) (0-1)
         */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_a_7.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(7, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(7, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(7, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_anonymous_8() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-non-anonymous (ComplexType) (0-1)
         *                                                             |__bb-subelement (SimpleField) (1-1)
         */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_a_8.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(7, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(7, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(7, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_anonymous_9() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-non-anonymous (ComplexType) (1-1)
         *                                                             |__bb-subelement (SimpleField) (0-1)
         */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_a_9.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(7, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(7, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(7, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_anonymous_10() throws Exception {
        /*
         * Entity                                             Entity
         *   |__id (SimpleField) (1-1)    ===========>           |__id (SimpleField) (1-1)
         *                                                       |__do-anonymous (ComplexType) (0-1)
         *                                                          |__do-subelement (SimpleField) (1-1)
         *                                                          |__bb-anonymous (ComplexType) (1-1)
         *                                                             |__bb-subelement (SimpleField) (0-1)
         */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_a_10.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(6, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(6, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(6, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test31_addOptionalComplexField_existed_1() throws Exception {
        /*
         * Entity                                                                 Entity
         *   |__id (SimpleField) (1-1)                                              |__id (SimpleField) (1-1)
         *   |__do1-anonymous (ComplexType) (0-1)                                   |__do1-anonymous (ComplexType) (0-1)
         *      |__do1-subelement (SimpleField) (1-1)                                  |__do1-subelement (SimpleField) (1-1)
         *      |__aa1-anonymous (ComplexType) (0-1)                                   |__aa1-anonymous (ComplexType) (0-1)
         *           |__aa1-subelement (SimpleField) (1-1)                                  |__aa1-subelement (SimpleField) (1-1)
         *      |__aa2-anonymous (ComplexType) (1-many)                                |__aa2-anonymous (ComplexType) (1-many)
         *           |__aa2-subelement (SimpleField) (1-1)                                  |__aa2-subelement (SimpleField) (1-1)
         *      |__aa3-non-anonymous (ComplexType) (0-1)                               |__aa3-non-anonymous (ComplexType) (0-1)
         *           |__aa3-subelement (SimpleField) (1-1)                                  |__aa3-subelement (SimpleField) (1-1)
         *      |__aa4-non-anonymous (ComplexType) (1-1)                               |__aa4-non-anonymous (ComplexType) (1-1)
         *           |__aa4-subelement (SimpleField) (1-1)     =====>                       |__aa4-subelement (SimpleField) (1-1)
         *                                                                          |__do3-anonymous (ComplexType) (0-1)
         *                                                                             |__do1-subelement (SimpleField) (1-1)
         *                                                                             |__aa1-anonymous (ComplexType) (0-1)
         *                                                                                  |__aa1-subelement (SimpleField) (1-1)
         *                                                                             |__aa2-anonymous (ComplexType) (1-many)
         *                                                                                  |__aa2-subelement (SimpleField) (1-1)
         *                                                                             |__aa3-non-anonymous (ComplexType) (0-1)
         *                                                                                  |__aa3-subelement (SimpleField) (1-1)
         *                                                                             |__aa4-non-anonymous (ComplexType) (1-1)
         *                                                                                  |__aa4-subelement (SimpleField) (1-1)
         *                                                                          |__do4-non-anonymous (ComplexType) (0-1)
         *                                                                             |__do2-subelement (SimpleField) (1-1)
         *                                                                             |__bb1-anonymous (ComplexType) (0-1)
         *                                                                                  |__bb1-subelement (SimpleField) (1-1)
         *                                                                             |__bb2-anonymous (ComplexType) (1-many)
         *                                                                                  |__aa2-subelement (SimpleField) (1-1)
         *                                                                             |__bb3-non-anonymous (ComplexType) (0-1)
         *                                                                                  |__aa3-subelement (SimpleField) (1-1)
         *                                                                             |__bb4-non-anonymous (ComplexType) (1-1)
         *                                                                                  |__aa4-subelement (SimpleField) (1-1)
        */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema31_e_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema31_e_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(33, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(33, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(33, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test32_1() throws Exception {
        /*
         * Entity                                                                 Entity
         *   |__id (SimpleField) (1-1)                                              |__id (SimpleField) (1-1)
         *   |__aa-non-anonymous (ComplexType) (0-1)                                   |__aa-non-anonymous (ComplexType) (0-1)
         *           |__aa-sub (SimpleField) (1-1)                                            |__aa-sub (SimpleField) (1-1)
         *           |__bb-anonymous (ComplexType) (1-1)         ======>                         |__bb-anonymous (ComplexType) (1-1)
         *                  |__bb-sub (SimpleField) (1-1)                                        |__bb-sub (SimpleField) (1-1)
         *                                                                             |__do-anonymous (ComplexType) (0-1)
         *                                                                                    |__do-sub (SimpleField) (1-1)
        */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema32_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema32_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(3, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(3, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test32_2() throws Exception {
        /*
         * Entity                                                                 Entity
         *   |__id (SimpleField) (1-1)                                              |__id (SimpleField) (1-1)
         *   |__aa-anonymous (ComplexType) (0-1)                                    |__aa-anonymous (ComplexType) (0-1)
         *           |__aa-sub (SimpleField) (1-1)                                            |__aa-sub (SimpleField) (1-1)
         *           |__bb-anonymous (ComplexType) (1-1)     ======>                          |__bb-anonymous (ComplexType) (1-1)
         *                  |__bb-sub (SimpleField) (1-1)                                            |__bb-sub (SimpleField) (1-1)
         *                                                                          |__do-anonymous (ComplexType) (0-1)
         *                                                                                    |__do-sub (SimpleField) (1-1)
        */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema32_3.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema32_4.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(3, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(3, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test32_3() throws Exception {
        /*
         * Entity                                                                 Entity
         *   |__id (SimpleField) (1-1)                                              |__id (SimpleField) (1-1)
         *   |__aa-anonymous (ComplexType) (1-1)                                    |__aa-anonymous (ComplexType) (1-1)
         *           |__aa-sub (SimpleField) (1-1)                                            |__aa-sub (SimpleField) (1-1)
         *           |__bb-anonymous (ComplexType) (1-1)              =======>                |__bb-anonymous (ComplexType) (1-1)
         *                  |__bb-sub (SimpleField) (1-1)                                         |__bb-sub (SimpleField) (1-1)
         *                                                                          |__do-anonymous (ComplexType) (0-1)
         *                                                                                    |__do-sub (SimpleField) (1-1)
        */

        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema32_5.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema32_6.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(3, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(0, diffResults.getRemoveChanges().size());
        assertEquals(3, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
        assertEquals(3, sort.get(ImpactAnalyzer.Impact.LOW).size());
    }

    public void test33_1_RenamedFKName() {
        MetadataRepository original = new MetadataRepository();
        original.load(CompareTest.class.getResourceAsStream("schema33_1.xsd")); //$NON-NLS-1$
        original = original.copy();
        MetadataRepository updated2 = new MetadataRepository();
        updated2.load(CompareTest.class.getResourceAsStream("schema33_2.xsd")); //$NON-NLS-1$
        Compare.DiffResults diffResults = Compare.compare(original, updated2);
        assertEquals(2, diffResults.getActions().size());
        assertEquals(0, diffResults.getModifyChanges().size());
        assertEquals(1, diffResults.getRemoveChanges().size());
        assertEquals(1, diffResults.getAddChanges().size());

        ImpactAnalyzer analyzer = new HibernateStorageImpactAnalyzer();
        Map<ImpactAnalyzer.Impact, List<Change>> sort = analyzer.analyzeImpacts(diffResults);
        assertEquals(0, sort.get(ImpactAnalyzer.Impact.HIGH).size());
        assertEquals(1, sort.get(ImpactAnalyzer.Impact.MEDIUM).size());
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
