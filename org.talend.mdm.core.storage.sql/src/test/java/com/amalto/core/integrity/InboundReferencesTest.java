/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.integrity;

import org.talend.mdm.commmon.metadata.*;

import com.amalto.core.server.MockServerLifecycle;
import com.amalto.core.server.ServerContext;

import junit.framework.TestCase;

import java.util.Collections;
import java.util.Set;

/**
 *
 */
@SuppressWarnings({"HardCodedStringLiteral", "nls"})
public class InboundReferencesTest extends TestCase {
    
    @Override
    public void setUp() {
        ServerContext.INSTANCE.get(new MockServerLifecycle());
    }

    private Set<ReferenceFieldMetadata> getReferencedFields(MetadataRepository repository, String typeName) {
        TypeMetadata type = repository.getType(typeName);

        if (type != null) {
            InboundReferences keyIntegrity = new InboundReferences(type);
            return repository.accept(keyIntegrity);
        } else {
            return Collections.emptySet();
        }
    }

    private MetadataRepository getMetadataRepository(String xsdFileName) {
        MetadataRepository repository;
        try {
            repository = new MetadataRepository();
            repository.load(getClass().getResourceAsStream(xsdFileName));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return repository;
    }

    public void testNonExistingType() throws Exception {
        MetadataRepository repository = getMetadataRepository("model1.xsd");
        IntegrityCheckDataSourceMock dataSource = new IntegrityCheckDataSourceMock(repository);

        Set<ReferenceFieldMetadata> list = dataSource.getForeignKeyList("TypeThatDoesNotExist", "DataModel");
        assertTrue(list.isEmpty());

        FKIntegrityChecker integrityChecker = FKIntegrityChecker.getInstance();
        String dataCluster = "DataCluster";
        String typeName = "TypeThatDoesNotExist";
        String[] ids = {"1"};
        assertTrue(integrityChecker.allowDelete(dataCluster, typeName, ids, false, dataSource));
        FKIntegrityCheckResult policy = integrityChecker.getFKIntegrityPolicy(dataCluster, typeName, ids, dataSource);
        assertEquals(FKIntegrityCheckResult.ALLOWED, policy);
    }

    /**
     * A -ref(1)-> B
     *
     * @throws Exception
     */
    public void testModel1() throws Exception {
        MetadataRepository repository = getMetadataRepository("model1.xsd");

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertTrue(references.isEmpty());

        references = getReferencedFields(repository, "B");
        assertEquals(1, references.size());
    }

    /**
     * A -ref(1)-> B
     * B -ref(1)-> C
     * C -ref(1)-> A
     *
     * @throws Exception
     */
    public void testModel2() throws Exception {
        MetadataRepository repository = getMetadataRepository("model2.xsd");

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(1, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(1, references.size());

        references = getReferencedFields(repository, "C");
        assertEquals(1, references.size());
    }

    /**
     * A -ref(1)-> B
     * C -inherits-> A
     *
     * @throws Exception
     */
    public void testModel3() throws Exception {
        MetadataRepository repository = getMetadataRepository("model3.xsd");

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(2, references.size());

        references = getReferencedFields(repository, "C");
        assertEquals(0, references.size());
    }

    /**
     * A -ref(1)-> B
     * A -ref(1)-> B
     * A -ref(1)-> B
     * A -ref(1)-> B
     *
     * @throws Exception
     */
    public void testModel4() throws Exception {
        MetadataRepository repository = getMetadataRepository("model4.xsd");

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(4, references.size());
    }

    /**
     * A -ref(1)-> B
     * B -ref(1)-> A
     *
     * @throws Exception
     */
    public void testModel5() throws Exception {
        MetadataRepository repository = getMetadataRepository("model5.xsd");

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(1, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(1, references.size());

    }

    /**
     * A -ref(unbounded)-> B
     *
     * @throws Exception
     */
    public void testModel6() throws Exception {
        MetadataRepository repository = getMetadataRepository("model6.xsd");

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(1, references.size());

    }

    /**
     * A
     * B
     * <p/>
     * (no relationship between types).
     *
     * @throws Exception
     */
    public void testModel7() throws Exception {
        MetadataRepository repository = getMetadataRepository("model7.xsd");

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(0, references.size());

    }

    /**
     * A -ref(1)-> B
     * C -inherits-> B
     *
     * @throws Exception
     */
    public void testModel8() throws Exception {
        MetadataRepository repository = getMetadataRepository("model8.xsd");

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(1, references.size());

        references = getReferencedFields(repository, "C");
        assertEquals(1, references.size());
    }

    /**
     * A -ref(1)-> C
     * B -ref(1)-> C
     *
     * @throws Exception
     */
    public void testModel9() throws Exception {
        MetadataRepository repository = getMetadataRepository("model9.xsd");

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "C");
        assertEquals(2, references.size());
    }

    /**
     * A -ref(1)-> B
     * C -inherits-> B
     * D -inherits-> C
     *
     * @throws Exception
     */
    public void testModel10() throws Exception {
        MetadataRepository repository = getMetadataRepository("model10.xsd");

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(1, references.size());

        references = getReferencedFields(repository, "C");
        assertEquals(1, references.size());

        references = getReferencedFields(repository, "D");
        assertEquals(1, references.size());
    }

    /**
     * A -ref(1)-> C
     * B -ref(1)-> C
     *
     * @throws Exception
     */
    public void testModel11() throws Exception {
        final MetadataRepository repository = getMetadataRepository("model11.xsd");

        ComplexTypeMetadata typeA = repository.getComplexType("A");
        FieldMetadata field = typeA.getField("refC");
        assertEquals(ReferenceFieldMetadata.class, field.getClass());
        ReferenceFieldMetadata referenceFieldMetadata = (ReferenceFieldMetadata) field;
        assertFalse(referenceFieldMetadata.isFKIntegrity());
        assertTrue(referenceFieldMetadata.allowFKIntegrityOverride());

        ComplexTypeMetadata typeB = repository.getComplexType("B");
        field = typeB.getField("refC");
        assertEquals(ReferenceFieldMetadata.class, field.getClass());
        referenceFieldMetadata = (ReferenceFieldMetadata) field;
        assertFalse(referenceFieldMetadata.isFKIntegrity());
        assertTrue(referenceFieldMetadata.allowFKIntegrityOverride());

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "C");
        assertEquals(2, references.size());

        IntegrityCheckDataSourceMock dataSource = new IntegrityCheckDataSourceMock(repository);
        FKIntegrityChecker integrityChecker = FKIntegrityChecker.getInstance();
        String dataCluster = "DataCluster";
        String typeName = "C";
        String[] ids = {"1"};
        assertTrue(integrityChecker.allowDelete(dataCluster, typeName, ids, false, dataSource));
        FKIntegrityCheckResult policy = integrityChecker.getFKIntegrityPolicy(dataCluster, typeName, ids, dataSource);
        assertEquals(FKIntegrityCheckResult.ALLOWED, policy);

        assertFalse(dataSource.hasMetConflict());
    }

    /**
     * A -ref(1)-> C
     * B -ref(1)-> C
     *
     * @throws Exception
     */
    public void testModel12() throws Exception {
        MetadataRepository repository = getMetadataRepository("model12.xsd");

        ComplexTypeMetadata typeA = repository.getComplexType("A");
        FieldMetadata field = typeA.getField("refC");
        assertEquals(ReferenceFieldMetadata.class, field.getClass());
        ReferenceFieldMetadata referenceFieldMetadata = (ReferenceFieldMetadata) field;
        assertTrue(referenceFieldMetadata.isFKIntegrity());
        assertTrue(referenceFieldMetadata.allowFKIntegrityOverride());

        ComplexTypeMetadata typeB = repository.getComplexType("B");
        field = typeB.getField("refC");
        assertEquals(ReferenceFieldMetadata.class, field.getClass());
        referenceFieldMetadata = (ReferenceFieldMetadata) field;
        assertFalse(referenceFieldMetadata.isFKIntegrity());
        assertTrue(referenceFieldMetadata.allowFKIntegrityOverride());

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "C");
        assertEquals(2, references.size());

        IntegrityCheckDataSourceMock dataSource = new IntegrityCheckDataSourceMock(repository);
        FKIntegrityChecker integrityChecker = FKIntegrityChecker.getInstance();
        String dataCluster = "DataCluster";
        String typeName = "C";
        String[] ids = {"1"};
        assertFalse(integrityChecker.allowDelete(dataCluster, typeName, ids, false, dataSource));
        FKIntegrityCheckResult policy = integrityChecker.getFKIntegrityPolicy(dataCluster, typeName, ids, dataSource);
        assertEquals(FKIntegrityCheckResult.FORBIDDEN_OVERRIDE_ALLOWED, policy);

        assertTrue(dataSource.hasMetConflict());
    }

    /**
     * A -ref(1)-> C
     * B -ref(1)-> C
     *
     * @throws Exception
     */
    public void testModel13() throws Exception {
        MetadataRepository repository = getMetadataRepository("model13.xsd");

        ComplexTypeMetadata typeA = repository.getComplexType("A");
        FieldMetadata field = typeA.getField("refC");
        assertEquals(ReferenceFieldMetadata.class, field.getClass());
        ReferenceFieldMetadata referenceFieldMetadata = (ReferenceFieldMetadata) field;
        assertTrue(referenceFieldMetadata.isFKIntegrity());
        assertTrue(referenceFieldMetadata.allowFKIntegrityOverride());

        ComplexTypeMetadata typeB = repository.getComplexType("B");
        field = typeB.getField("refC");
        assertEquals(ReferenceFieldMetadata.class, field.getClass());
        referenceFieldMetadata = (ReferenceFieldMetadata) field;
        assertFalse(referenceFieldMetadata.isFKIntegrity());
        assertFalse(referenceFieldMetadata.allowFKIntegrityOverride());

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "B");
        assertEquals(0, references.size());

        references = getReferencedFields(repository, "C");
        assertEquals(2, references.size());

        IntegrityCheckDataSourceMock dataSource = new IntegrityCheckDataSourceMock(repository);
        FKIntegrityChecker integrityChecker = FKIntegrityChecker.getInstance();
        String dataCluster = "DataCluster";
        String typeName = "C";
        String[] ids = {"1"};
        assertFalse(integrityChecker.allowDelete(dataCluster, typeName, ids, false, dataSource));
        FKIntegrityCheckResult policy = integrityChecker.getFKIntegrityPolicy(dataCluster, typeName, ids, dataSource);
        assertEquals(FKIntegrityCheckResult.FORBIDDEN_OVERRIDE_ALLOWED, policy);

        assertTrue(dataSource.hasMetConflict());
    }

    /**
     * A -ref(1)-> C
     * B -ref(1)-> C
     *
     * @throws Exception
     */
    public void testModel14() throws Exception {
        MetadataRepository repository = getMetadataRepository("model14.xsd");

        ComplexTypeMetadata typeA = repository.getComplexType("A");
        FieldMetadata field = typeA.getField("refA");
        assertEquals(ReferenceFieldMetadata.class, field.getClass());
        ReferenceFieldMetadata referenceFieldMetadata = (ReferenceFieldMetadata) field;
        assertTrue(referenceFieldMetadata.isFKIntegrity());
        assertFalse(referenceFieldMetadata.allowFKIntegrityOverride());

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(1, references.size());

        IntegrityCheckDataSourceMock dataSource = new IntegrityCheckDataSourceMock(repository);
        FKIntegrityChecker integrityChecker = FKIntegrityChecker.getInstance();
        String dataCluster = "DataCluster";
        String typeName = "A";
        String[] ids = {"1"};
        assertFalse(integrityChecker.allowDelete(dataCluster, typeName, ids, false, dataSource));
        FKIntegrityCheckResult policy = integrityChecker.getFKIntegrityPolicy(dataCluster, typeName, ids, dataSource);
        assertEquals(FKIntegrityCheckResult.FORBIDDEN, policy);
    }

    public void testModel15() throws Exception {
        MetadataRepository repository = getMetadataRepository("model15.xsd");

        // Check FK integrity checks following TMDM-3051
        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "Product");
        assertEquals(1, references.size());
    }

    public void testModel16() throws Exception {
        MetadataRepository repository = getMetadataRepository("model16.xsd");

        // Check FK integrity checks following TMDM-3515
        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "BusinessFunction");
        assertEquals(6, references.size());

        IntegrityCheckDataSourceMock dataSource = new IntegrityCheckDataSourceMock(repository);
        FKIntegrityChecker integrityChecker = FKIntegrityChecker.getInstance();
        String dataCluster = "DataCluster";
        String typeName = "BusinessFunction";
        String[] ids = {"1"};
        assertTrue(integrityChecker.allowDelete(dataCluster, typeName, ids, false, dataSource));
        FKIntegrityCheckResult policy = integrityChecker.getFKIntegrityPolicy(dataCluster, typeName, ids, dataSource);
        assertEquals(FKIntegrityCheckResult.ALLOWED, policy);
    }

    /**
     * SocieteCliente -ref(1)-> Contrat
     * <p/>
     * The reference field "contrat" is defined under a contained complex type "contrats"
     *
     * @throws Exception
     */
    public void testModel17() throws Exception {

        MetadataRepository repository = getMetadataRepository("model17.xsd");

        // Check FK integrity checks following TMDM-3739
        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "Contrat");
        assertEquals(24, references.size());

        String dataCluster = "DataCluster";
        String typeName = "Contrat";
        String[] ids = {"1"};

        FKIntegrityChecker integrityChecker = FKIntegrityChecker.getInstance();
        IntegrityCheckDataSourceMock dataSource = new IntegrityCheckDataSourceMock(repository);
        assertTrue(integrityChecker.allowDelete(dataCluster, typeName, ids, false, dataSource));
        FKIntegrityCheckResult policy = integrityChecker.getFKIntegrityPolicy(dataCluster, typeName, ids, dataSource);
        assertEquals(FKIntegrityCheckResult.ALLOWED, policy);
    }

    /**
     * A -ref(1)-> A
     *
     * @throws Exception
     */
    public void test18() throws Exception {
        MetadataRepository repository = getMetadataRepository("model18.xsd");

        ComplexTypeMetadata typeA = repository.getComplexType("A");
        FieldMetadata field = typeA.getField("refA");
        assertEquals(ReferenceFieldMetadata.class, field.getClass());
        ReferenceFieldMetadata referenceFieldMetadata = (ReferenceFieldMetadata) field;
        assertTrue(referenceFieldMetadata.isFKIntegrity());
        assertFalse(referenceFieldMetadata.allowFKIntegrityOverride());

        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "A");
        assertEquals(1, references.size());

        IntegrityCheckDataSourceMock dataSource = new IntegrityCheckDataSourceMock(repository);
        FKIntegrityChecker integrityChecker = FKIntegrityChecker.getInstance();
        String dataCluster = "DataCluster";
        String typeName = "A";
        String[] ids = {"1"};
        assertFalse(integrityChecker.allowDelete(dataCluster, typeName, ids, false, dataSource));
        FKIntegrityCheckResult policy = integrityChecker.getFKIntegrityPolicy(dataCluster, typeName, ids, dataSource);
        assertEquals(FKIntegrityCheckResult.FORBIDDEN, policy);
    }

    public void testEmptyTypeName() throws Exception {

        MetadataRepository repository = getMetadataRepository("model17.xsd");
        Set<ReferenceFieldMetadata> references = getReferencedFields(repository, "Contrat");
        assertEquals(24, references.size());
        ReferenceFieldMetadata referencedField = references.iterator().next();

        String dataCluster = "DataCluster";
        String[] ids = {"1"};
        FKIntegrityCheckDataSource dataSource = new DefaultCheckDataSource();

        // Check the anonymous type and leave the type name empty
        assertEquals(0, dataSource.countInboundReferences(dataCluster, ids, null, referencedField));
        assertEquals(0, dataSource.countInboundReferences(dataCluster, ids, "", referencedField));

    }
}
