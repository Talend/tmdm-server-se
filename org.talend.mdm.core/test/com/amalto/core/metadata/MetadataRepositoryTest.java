/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.metadata;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;

import org.junit.Assert;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.CompoundFieldMetadata;
import org.talend.mdm.commmon.metadata.ConsoleDumpMetadataVisitor;
import org.talend.mdm.commmon.metadata.ContainedComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.ContainedTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.MetadataUtils;
import org.talend.mdm.commmon.metadata.ReferenceFieldMetadata;
import org.talend.mdm.commmon.metadata.SimpleTypeFieldMetadata;
import org.talend.mdm.commmon.metadata.TypeMetadata;

/**
 * Schema parsing <br>
 * <li>add Chinese and Japanese language label testCase
 */
@SuppressWarnings("nls")
public class MetadataRepositoryTest extends TestCase {

    ConsoleDumpMetadataVisitor visitor = new ConsoleDumpMetadataVisitor();

    public void test1() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema.xsd");
        repository.load(stream);
        // repository.accept(visitor);

        ComplexTypeMetadata product = repository.getComplexType("Product");
        assertNotNull(product);
        assertEquals("Product", product.getName(new Locale("en")));
        assertEquals("Product", product.getName(new Locale("zn")));
        assertEquals("Produit", product.getName(new Locale("fr")));
        FieldMetadata field = product.getField("Features/Sizes/Size");
        assertNotNull(field);
        assertEquals("Features/Sizes/Size", field.getPath());
        assertEquals("Product", field.getEntityTypeName());
        FieldMetadata name = product.getField("Name");
        assertNotNull(name);
        assertEquals("Name", name.getName(new Locale("en")));
        assertEquals("Name", name.getName(new Locale("zn")));
        assertEquals("Nom", name.getName(new Locale("fr")));
    }

    public void test2() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema2.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test3() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema3.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test4() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema4.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test5() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema5.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test6() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema6.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test7() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema7.xsd");
        try {
            repository.load(stream); // Model has many errors to be fixed.
        } catch (Exception e) {
            // Expected
        }
        // repository.accept(visitor);
    }

    public void test8() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema8.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test9() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema9.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test10() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema10.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test11() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("schema11.xsd");
        repository.load(stream);
        // repository.accept(visitor);
    }

    public void test12() {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema12.xsd");
        repository.load(stream);
        assertTrue(repository.getTypes().size() > 0);
    }

    public void test13() {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema13.xsd");
        repository.load(stream);
        assertTrue(repository.getTypes().size() > 0);
        // repository.accept(visitor);
    }

    public void test14() {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema14.xsd");
        try {
            repository.load(stream);
            fail("Expected exception due to invalid key definition in inheritance tree.");
        } catch (Exception e) {
            // Expected.
        }
    }

    public void test15() {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema15.xsd");
        repository.load(stream);

        ComplexTypeMetadata company = repository.getComplexType("Company");
        assertNotNull(company);
        assertEquals(1, company.getKeyFields().size());
    }

    public void test16() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema16.xsd");
        repository.load(stream);

        ComplexTypeMetadata person = repository.getComplexType("Person");
        assertNotNull(person);
        assertEquals(2, person.getKeyFields().size());
        String[] expectedOrder = { "lastname", "firstname" };
        int i = 0;
        for (FieldMetadata keyField : person.getKeyFields()) {
            assertEquals(expectedOrder[i++], keyField.getName());
        }
    }

    public void test17() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema17.xsd");
        repository.load(stream);

        ComplexTypeMetadata entityType = repository.getComplexType("A");
        assertNotNull(entityType);
        TypeMetadata simpleType = repository.getNonInstantiableType("", "A");
        assertNotNull(simpleType);
        assertNotSame(entityType, simpleType);
    }

    public void test18() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema18.xsd");
        repository.load(stream);

        ComplexTypeMetadata entityType = repository.getComplexType("A");
        assertNotNull(entityType);
        assertTrue(entityType.hasField("string20"));
        assertTrue(entityType.hasField("string30"));
        assertTrue(entityType.hasField("string40"));

        TypeMetadata string20Field = entityType.getField("string20").getType();
        assertEquals("20", string20Field.getData(MetadataRepository.DATA_MAX_LENGTH));
        TypeMetadata string30Field = entityType.getField("string30").getType();
        assertEquals("30", string30Field.getData(MetadataRepository.DATA_MAX_LENGTH));
        TypeMetadata string40Field = entityType.getField("string40").getType();
        assertEquals("40", string40Field.getData(MetadataRepository.DATA_MAX_LENGTH));
    }

    public void test19() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema19.xsd");
        try {
            repository.load(stream);
            fail("Expected exception (cycles in model).");
        } catch (Exception e) {
            // Expected
        }
    }

    public void test20() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema20.xsd");
        repository.load(stream);
        ComplexTypeMetadata test = repository.getComplexType("Test");
        assertNotNull(test);
        assertTrue(test.hasField("text"));
        assertEquals("888", test.getField("text").getType().getData(MetadataRepository.DATA_MAX_LENGTH));
    }

    public void test21() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema21.xsd");
        repository.load(stream);
        ComplexTypeMetadata test = repository.getComplexType("shiporder");
        assertNotNull(test);
        assertTrue(test.hasField("shipto/name"));
    }

    public void testCopy() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema21.xsd");
        repository.load(stream);
        ComplexTypeMetadata test1 = repository.getComplexType("shiporder");

        MetadataRepository copy = repository.copy();
        ComplexTypeMetadata test2 = copy.getComplexType("shiporder");

        assertEquals(test1.getName(), test2.getName());
        assertTrue(test1 != test2);
    }

    public void test22() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema22.xsd");
        repository.load(stream);
        // repository.accept(visitor);
        ComplexTypeMetadata box = repository.getComplexType("Box");
        assertNotNull(box);
        FieldMetadata field = box.getField("FieldCTFK/Name");
        assertNotNull(field);
        assertEquals("Box/FieldCTFK/Name", field.getEntityTypeName() + "/" + field.getPath());
    }

    public void test23() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema23.xsd");
        try {
            repository.load(stream);
            fail("Expected a cycle in model.");
        } catch (Exception e) {
            // Expected
        }
        // repository.accept(visitor);
    }

    public void test24() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema24.xsd");
        try {
            repository.load(stream);
            fail("Expected a cycle in model.");
        } catch (Exception e) {
            // Expected
        }
        // repository.accept(visitor);
    }

    public void test25() throws Exception { // See TMDM-7235
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema25.xsd");
        repository.load(stream);
        ComplexTypeMetadata test = repository.getComplexType("Test");
        ComplexTypeMetadata containedType = (ComplexTypeMetadata) test.getField("documents").getType();
        List<ComplexTypeMetadata> sort = MetadataUtils.sortTypes(repository, Arrays.asList(test, containedType));
        assertEquals("Test", sort.get(0).getName());
    }

    public void test26() throws Exception { // See TMDM-7235
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema26.xsd");
        repository.load(stream);
        List<ComplexTypeMetadata> sortTypes = MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
        boolean hasMetHexavia = false;
        boolean hasMetAdresse = false;
        
        for (ComplexTypeMetadata sortType : sortTypes) {
            if ("Hexavia".equals(sortType.getName())) {
                hasMetHexavia = true;
            }
            if ("Adresse".equals(sortType.getName())) {
                assertTrue(hasMetHexavia);
                hasMetAdresse = true;
            }
        }
        assertTrue(hasMetAdresse);
    }
    
    // More cyclic sort test cases
    public void test26_1() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("SortType_01.xsd"); // BA_Adresse=(0..1)=>BB_Hexavia;BA_Adresse=(0..1)=>A_TypeVoie; BB_Hexavia=(0..1)=>A_TypeVoie/BB_Hexavia;
        repository.load(stream);
        List<ComplexTypeMetadata> sortTypes = MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
        boolean hasMetHexavia = false;
        boolean hasMetAdresse = false;
        boolean hasTypeVoice= false ;
        
        for (ComplexTypeMetadata sortType : sortTypes) {
            if("A_TypeVoie".equals(sortType.getName())){
                hasTypeVoice = true ;
            }
            if ("BB_Hexavia".equals(sortType.getName())) {
                assertTrue(hasTypeVoice);
                hasMetHexavia = true;
            }
            if ("BA_Adresse".equals(sortType.getName())) {
                assertTrue(hasMetHexavia);
                hasMetAdresse = true;
            }
        }
        assertTrue(hasMetAdresse);
        
        repository = new MetadataRepository();
        stream = getClass().getResourceAsStream("SortType_02.xsd");// BA_Adresse=(1..1)=>BB_Hexavia;BA_Adresse=(1..1)=>A_TypeVoie; BB_Hexavia=(1..1)=>A_TypeVoie; BB_Hexavia=(0..1)=>BB_Hexavia;
        repository.load(stream);
        sortTypes = MetadataUtils.sortTypes(repository, MetadataUtils.SortType.STRICT);
        hasMetHexavia = false;
        hasMetAdresse = false;
        hasTypeVoice= false ;
        
        for (ComplexTypeMetadata sortType : sortTypes) {
            if("A_TypeVoie".equals(sortType.getName())){
                hasTypeVoice = true ;
            }
            if ("BB_Hexavia".equals(sortType.getName())) {
                assertTrue(hasTypeVoice);
                hasMetHexavia = true;
            }
            if ("BA_Adresse".equals(sortType.getName())) {
                assertTrue(hasMetHexavia);
                hasMetAdresse = true;
            }
        }
        assertTrue(hasMetAdresse);
        

        repository = new MetadataRepository();
        stream = getClass().getResourceAsStream("SortType_03.xsd");// BA_Adresse=(0..1)=>BB_Hexavia;BA_Adresse=(0..1)=>A_TypeVoie; BB_Hexavia=(0..1)=>BA_Adresse; BB_Hexavia=(0..1)=>Hexaposte;
        repository.load(stream);
        sortTypes = MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
        hasMetHexavia = false;
        hasMetAdresse = false;
        hasTypeVoice= false ;
        boolean hasHexaposte = false ;
        
        for (ComplexTypeMetadata sortType : sortTypes) {
            if("A_TypeVoie".equals(sortType.getName())){
                hasTypeVoice = true ;
            }
            if("Hexaposte".equals(sortType.getName())){
                hasHexaposte = true ;
            }
            if ("BB_Hexavia".equals(sortType.getName())) {
                assertTrue(hasHexaposte);
                hasMetHexavia = true;
            }
            if ("BA_Adresse".equals(sortType.getName())) {
                assertTrue(hasTypeVoice);
                hasMetAdresse = true;
            }
        }
        assertTrue(hasMetAdresse);
    }

    public void test27() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema27.xsd");
        repository.load(stream);

        ComplexTypeMetadata entityType = repository.getComplexType("A");
        assertNotNull(entityType);
        assertTrue(entityType.hasField("string20"));
        assertTrue(entityType.hasField("id"));

        TypeMetadata string20Field = entityType.getField("string20").getType();
        assertEquals("20", string20Field.getData(MetadataRepository.DATA_MAX_LENGTH));
        TypeMetadata string30Field = entityType.getField("id").getType();
        assertEquals("20", string30Field.getData(MetadataRepository.DATA_MAX_LENGTH));
    }

    public void test28() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema28.xsd");
        repository.load(stream);
        // Assert on composite key
        ComplexTypeMetadata entityType = repository.getComplexType("qq");
        assertNotNull(entityType);
        assertEquals(3, entityType.getKeyFields().size());
        String[] names = new String[3];
        int i = 0;
        for (FieldMetadata keyField : entityType.getKeyFields()) {
            names[i++] = keyField.getName();
        }
        // Assert on composite key (using copy).
        MetadataRepository copy = repository.copy();
        ComplexTypeMetadata entityTypeCopy = copy.getComplexType("qq");
        assertNotNull(entityTypeCopy);
        assertEquals(3, entityTypeCopy.getKeyFields().size());
        String[] copyNames = new String[3];
        i = 0;
        for (FieldMetadata keyField : entityTypeCopy.getKeyFields()) {
            copyNames[i++] = keyField.getName();
        }
        // Assert order is same
        for (int j = 0; j < names.length; j++) {
            assertEquals(names[j], copyNames[j]);
        }
    }
    
    // TMDM-8022
    public void test30() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema30.xsd");
        repository.load(stream);
        ComplexTypeMetadata entityType = repository.getComplexType("Goods");
        assertNotNull(entityType);
        FieldMetadata fieldMetadata = entityType.getField("Price");
        assertNotNull(fieldMetadata);
        assertEquals(fieldMetadata.getType().getData(MetadataRepository.DATA_TOTAL_DIGITS), "15");
        assertEquals(fieldMetadata.getType().getData(MetadataRepository.DATA_FRACTION_DIGITS), "3");
        assertEquals(fieldMetadata.getData(MetadataRepository.DATA_TOTAL_DIGITS), "15");
        assertEquals(fieldMetadata.getData(MetadataRepository.DATA_FRACTION_DIGITS), "3");
    }
    
    public void testLenientSortSimpleTest() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("product.xsd");
        repository.load(stream);
        List<ComplexTypeMetadata> sortTypes = MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
        Assert.assertEquals(repository.getInstantiableTypes().size(), sortTypes.size());
        assertIsBefore(sortTypes, repository, "ProductFamily", "Product");
        assertIsBefore(sortTypes, repository, "Supplier", "Product");
    }
    
    public void testLenientSortComplexTest() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema9.xsd");
        repository.load(stream);
        List<ComplexTypeMetadata> sortTypes = MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
        Assert.assertEquals(repository.getInstantiableTypes().size(), sortTypes.size());
        assertIsBefore(sortTypes, repository, "SocieteCliente", "Contrat");
        assertIsBefore(sortTypes, repository, "TypeInterlocuteur", "Interlocuteur");
        assertIsBefore(sortTypes, repository, "Interlocuteur", "Eda");
        assertIsBefore(sortTypes, repository, "UseUrse", "Eda");
        assertIsBefore(sortTypes, repository, "EdaFuture", "Eda");
        assertIsBefore(sortTypes, repository, "Pays", "Eda");
        assertIsBefore(sortTypes, repository, "UniteAgregation", "Eda");
        assertIsBefore(sortTypes, repository, "Edp", "Contrat");
        assertIsBefore(sortTypes, repository, "Interlocuteur", "Edp");
        assertIsBefore(sortTypes, repository, "UseUrse", "Edp");
        assertIsBefore(sortTypes, repository, "Grt", "Eda");
        assertIsBefore(sortTypes, repository, "UseUrse", "SiteInjectionRpt");
        assertIsBefore(sortTypes, repository, "SiteInjectionRpt", "Gdp");
        assertIsBefore(sortTypes, repository, "UseUrse", "Gdp");
        assertIsBefore(sortTypes, repository, "EdaFuture", "Contrat");
        assertIsBefore(sortTypes, repository, "Interlocuteur", "EdaFuture");
        assertIsBefore(sortTypes, repository, "UseUrse", "EdaFuture");
        assertIsBefore(sortTypes, repository, "Pays", "EdaFuture");
    }
    
    // TMDM-8760
    public void testLenientSortComplexTest2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema29.xsd");
        repository.load(stream);
        List<ComplexTypeMetadata> sortTypes = MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
        Assert.assertEquals(repository.getInstantiableTypes().size(), sortTypes.size());
        assertIsBefore(sortTypes, repository, "Fournisseur", "Variante");
        assertIsBefore(sortTypes, repository, "Article", "Variante");
        assertIsBefore(sortTypes, repository, "NiveauQualite", "Variante");
        assertIsBefore(sortTypes, repository, "SousFamille", "Segment");
        assertIsBefore(sortTypes, repository, "TypeMatiere", "Matiere");
        assertIsBefore(sortTypes, repository, "Provenance", "Pays");
        assertIsBefore(sortTypes, repository, "CouleurBasique", "CodePantone");
        
    }
    
    private void assertIsBefore(List<ComplexTypeMetadata> types, MetadataRepository repository, String type1, String type2){
        ComplexTypeMetadata m1 = repository.getComplexType(type1);
        Assert.assertNotNull(type1 + " does not exist", m1);
        Assert.assertFalse(type1 + " not part of list", types.indexOf(m1) == -1);
        ComplexTypeMetadata m2 = repository.getComplexType(type2);
        Assert.assertNotNull(type2 + " does not exist", m2);
        Assert.assertFalse(type2 + " not part of list", types.indexOf(m2) == -1);
        Assert.assertTrue(type1 + " is not before " + type2, types.indexOf(m1) < types.indexOf(m2));
    }

    public void testCompoundForeignKey() {
        MetadataRepository repository = new MetadataRepository();

        InputStream stream = getClass().getResourceAsStream("Sort_CompoundForeignKey.xsd");
        repository.load(stream);
        ComplexTypeMetadata tt = repository.getComplexType("TT");
        ComplexTypeMetadata rr = repository.getComplexType("RR");
        assertEquals(2, rr.getKeyFields().size());

        FieldMetadata e3 = tt.getField("E3");
        FieldMetadata reference = ((ReferenceFieldMetadata) e3).getReferencedField();
        int foreignKeyFields = ((CompoundFieldMetadata) reference).getFields().length;

        assertEquals(2, foreignKeyFields);
    }

    // TMDM-8085
	public void testEntityUsesAbstractType() {
		MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("EntityUsesAbstractType.xsd");
		try {
			repository.load(stream);
			fail("Runtime Exception should occur if Entity uses an abstract type.");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains("Entity 'Pet' is using an abstract reusable type. (line: 19 / column: 2)"));
		}
	}

    // TMDM-3612R
    public void testCircleTypeModel() {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("TMDM_3612.xsd");
        try {
            repository.load(stream);
            fail();
        } catch (Exception e) {
        }

    }

    public void testMetadataAnnotation() {
        Locale en = new Locale("en");
        Locale fr = new Locale("fr");
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("POS.xsd");
        repository.load(stream);

        ComplexTypeMetadata sale = repository.getComplexType("PointOfSale");
        assertEquals("Point of sale", sale.getName(en));
        assertEquals("Point de vente", sale.getName(fr));
        assertEquals("Point of Sale Desc", sale.getDescription(en));
        assertEquals("Point de vente Desc", sale.getDescription(fr));

        SimpleTypeFieldMetadata saleName = (SimpleTypeFieldMetadata) sale.getField("Name");
        assertEquals("POS Name", saleName.getName(en));
        assertEquals("Nom PV", saleName.getName(fr));
        assertEquals("Name Desc", saleName.getDescription(en));
        assertEquals("Nom Desc", saleName.getDescription(fr));

        ContainedTypeFieldMetadata address = (ContainedTypeFieldMetadata) sale.getField("Address");
        assertEquals("Address", address.getName(en));
        assertEquals("Adresse", address.getName(fr));
        assertEquals("Address Desc", address.getDescription(en));
        assertEquals("Adresse Desc", address.getDescription(fr));

        ReferenceFieldMetadata brandFk = (ReferenceFieldMetadata) sale.getField("BrandFk");
        assertEquals("Brand", brandFk.getName(en));
        assertEquals("Marque", brandFk.getName(fr));
        assertEquals("Brand Desc", brandFk.getDescription(en));
        assertEquals("Marque Desc", brandFk.getDescription(fr));
    }

    // TMDM-9547 Journal is frozen
    public void testSortTypes_01() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("SortType_04.xsd");
        repository.load(stream);
        List<ComplexTypeMetadata> sortTypes = MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
        assertEquals("Catalog", sortTypes.get(0).getName());
    }

    // TMDM-9598 can't deploy datamodel and the 'Journal' page is always 'Loading'
    public void testSortTypes_02() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("SortType_05.xsd");
        repository.load(stream);
        MetadataUtils.sortTypes(repository, MetadataUtils.SortType.LENIENT);
    }

    public void test_31() throws Exception{
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema31_1.xsd");
        repository.load(stream);
        ComplexTypeMetadata entityType = repository.getComplexType("T_TRANSCO_FO");

        assertNotNull(entityType);
        FieldMetadata fieldMetadata = entityType.getField("LOCAL_FO_ID");
        assertEquals(ReferenceFieldMetadata.class, fieldMetadata.getClass());
        assertNotNull(fieldMetadata);
        ReferenceFieldMetadata referFieldMetadata = (ReferenceFieldMetadata)fieldMetadata ;
        if(!referFieldMetadata.getForeignKeyInfoFields().isEmpty()){
            assertEquals(SimpleTypeFieldMetadata.class, referFieldMetadata.getForeignKeyInfoFields().get(0) .getClass()) ;
        }

        fieldMetadata = entityType.getField("LOCAL_SOURCE_ID");
        assertEquals(ReferenceFieldMetadata.class, fieldMetadata.getClass());
        assertNotNull(fieldMetadata);
        referFieldMetadata = (ReferenceFieldMetadata)fieldMetadata ;
        if(!referFieldMetadata.getForeignKeyInfoFields().isEmpty()){
            assertEquals(SimpleTypeFieldMetadata.class, referFieldMetadata.getForeignKeyInfoFields().get(0) .getClass()) ;
        }

        stream = getClass().getResourceAsStream("schema31_2.xsd");
        repository.load(stream);
        ComplexTypeMetadata entityType2 = repository.getComplexType("EA");
        assertNotNull(entityType);
        fieldMetadata = entityType2.getField("FK1ToEB");
        assertEquals(ReferenceFieldMetadata.class, fieldMetadata.getClass());
        assertNotNull(fieldMetadata);
        referFieldMetadata = (ReferenceFieldMetadata)fieldMetadata ;
        if(!referFieldMetadata.getForeignKeyInfoFields().isEmpty()){
            assertEquals(SimpleTypeFieldMetadata.class, referFieldMetadata.getForeignKeyInfoFields().get(0) .getClass()) ;
        }

        fieldMetadata = entityType2.getField("FK4ToEBName");
        assertEquals(ReferenceFieldMetadata.class, fieldMetadata.getClass());
        assertNotNull(fieldMetadata);
        referFieldMetadata = (ReferenceFieldMetadata)fieldMetadata ;
        if(!referFieldMetadata.getForeignKeyInfoFields().isEmpty()){
            assertEquals(SimpleTypeFieldMetadata.class, referFieldMetadata.getForeignKeyInfoFields().get(0) .getClass()) ;
        }

        stream = getClass().getResourceAsStream("schema31_3.xsd");
        repository.load(stream);
        ComplexTypeMetadata entityType3 = ((ContainedComplexTypeMetadata)repository.getComplexType("TT").getField("MUl").getType()).getContainedType() ;
        assertNotNull(entityType);
        fieldMetadata = entityType3.getField("E3");
        assertEquals(ReferenceFieldMetadata.class, fieldMetadata.getClass());
        assertNotNull(fieldMetadata);
        referFieldMetadata = (ReferenceFieldMetadata)fieldMetadata ;
        if(!referFieldMetadata.getForeignKeyInfoFields().isEmpty()){
            assertEquals(SimpleTypeFieldMetadata.class, referFieldMetadata.getForeignKeyInfoFields().get(0) .getClass()) ;
        }
    }

    //TMDM-9086 if the default value is string, number, and the fn:true(), fn:false(), filed's data contains DEFAULT_VALUE value
    public void test_32() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema32.xsd");
        repository.load(stream);

        ComplexTypeMetadata entityType = repository.getComplexType("Object");
        assertNotNull(entityType);
        assertTrue(entityType.hasField("name"));
        assertTrue(entityType.hasField("lastname"));
        assertTrue(entityType.hasField("sex"));

        assertEquals("\"Jason\"", entityType.getField("lastname").getData(MetadataRepository.DEFAULT_VALUE));
        assertEquals("6", entityType.getField("age").getData(MetadataRepository.DEFAULT_VALUE));
        assertEquals("12.6", entityType.getField("weight").getData(MetadataRepository.DEFAULT_VALUE));
        assertEquals("fn:true()", entityType.getField("sex").getData(MetadataRepository.DEFAULT_VALUE));
        assertEquals("\"true\"", entityType.getField("isGradeOne").getData(MetadataRepository.DEFAULT_VALUE));
        assertNull(entityType.getField("name_1").getData(MetadataRepository.DEFAULT_VALUE));
        assertNull(entityType.getField("name_2").getData(MetadataRepository.DEFAULT_VALUE));

        assertEquals("\"Jason\"", entityType.getField("lastname").getData(MetadataRepository.DEFAULT_VALUE_RULE));
        assertEquals("6", entityType.getField("age").getData(MetadataRepository.DEFAULT_VALUE_RULE));
        assertEquals("12.6", entityType.getField("weight").getData(MetadataRepository.DEFAULT_VALUE_RULE));
        assertEquals("fn:true()", entityType.getField("sex").getData(MetadataRepository.DEFAULT_VALUE_RULE));
        assertEquals("\"true\"", entityType.getField("isGradeOne").getData(MetadataRepository.DEFAULT_VALUE));
        assertEquals("fn:name()", entityType.getField("name_1").getData(MetadataRepository.DEFAULT_VALUE_RULE));
        assertEquals("John", entityType.getField("name_2").getData(MetadataRepository.DEFAULT_VALUE_RULE));
    }

    // test the min occurs and max occurs for TMDM-10534
    public void test_33() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema33.xsd");
        repository.load(stream);

        ComplexTypeMetadata entityType = repository.getComplexType("Person");
        assertNotNull(entityType);
        assertTrue(entityType.hasField("aa"));
        assertTrue(entityType.hasField("bb"));
        assertTrue(entityType.hasField("cc"));
        assertTrue(entityType.hasField("dd"));
        assertTrue(entityType.hasField("ee"));

        assertEquals(0, Integer.parseInt(entityType.getField("aa").getData(MetadataRepository.MIN_OCCURS).toString()));
        assertEquals(0, Integer.parseInt(entityType.getField("bb").getData(MetadataRepository.MIN_OCCURS).toString()));
        assertEquals(1, Integer.parseInt(entityType.getField("cc").getData(MetadataRepository.MIN_OCCURS).toString()));
        assertEquals(1, Integer.parseInt(entityType.getField("dd").getData(MetadataRepository.MIN_OCCURS).toString()));
        assertEquals(1, Integer.parseInt(entityType.getField("ee").getData(MetadataRepository.MIN_OCCURS).toString()));

        assertEquals(8, Integer.parseInt(entityType.getField("aa").getData(MetadataRepository.MAX_OCCURS).toString()));
        assertEquals(-1, Integer.parseInt(entityType.getField("bb").getData(MetadataRepository.MAX_OCCURS).toString()));
        assertEquals(-1, Integer.parseInt(entityType.getField("cc").getData(MetadataRepository.MAX_OCCURS).toString()));
        assertEquals(2, Integer.parseInt(entityType.getField("dd").getData(MetadataRepository.MAX_OCCURS).toString()));
        assertEquals(-1, Integer.parseInt(entityType.getField("ee").getData(MetadataRepository.MAX_OCCURS).toString()));
    }

    public void test_34() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema34.xsd");
        repository.load(stream);

        ComplexTypeMetadata entityType = repository.getComplexType("Human");
        assertNotNull(entityType);
        assertTrue(entityType.hasField("status"));
        assertTrue(entityType.hasField("exclusive"));
        assertTrue(entityType.hasField("address"));
        assertTrue(entityType.hasField("inclusive"));
        assertTrue(entityType.hasField("name"));

        assertEquals("[Approve, Reject, Ignore]", entityType.getField("status").getData(MetadataRepository.ENUMERATION_LIST)
                .toString());
        assertEquals(9999,
                Integer.parseInt(entityType.getField("exclusive").getData(MetadataRepository.MAX_EXCLUSIVE).toString()));
        assertEquals(2, Integer.parseInt(entityType.getField("exclusive").getData(MetadataRepository.MIN_EXCLUSIVE).toString()));
        assertEquals("[0-9]{2};[0-9]{1}", entityType.getField("address").getData(MetadataRepository.PATTERN).toString());
        assertEquals(9999,
                Integer.parseInt(entityType.getField("inclusive").getData(MetadataRepository.MAX_INCLUSIVE).toString()));
        assertEquals(1, Integer.parseInt(entityType.getField("inclusive").getData(MetadataRepository.MIN_INCLUSIVE).toString()));
        assertEquals(50, Integer.parseInt(entityType.getField("name").getData(MetadataRepository.DATA_MAX_LENGTH).toString()));
        assertEquals(20, Integer.parseInt(entityType.getField("name").getData(MetadataRepository.DATA_MIN_LENGTH).toString()));

        assertEquals("[Approve, Reject, Ignore]",
                entityType.getField("status").getType().getData(MetadataRepository.ENUMERATION_LIST).toString());
        assertEquals(9999,
                Integer.parseInt(entityType.getField("exclusive").getType().getData(MetadataRepository.MAX_EXCLUSIVE).toString()));
        assertEquals(2,
                Integer.parseInt(entityType.getField("exclusive").getType().getData(MetadataRepository.MIN_EXCLUSIVE).toString()));
        assertEquals("[0-9]{2};[0-9]{1}", entityType.getField("address").getType().getData(MetadataRepository.PATTERN).toString());
        assertEquals(9999,
                Integer.parseInt(entityType.getField("inclusive").getType().getData(MetadataRepository.MAX_INCLUSIVE).toString()));
        assertEquals(1,
                Integer.parseInt(entityType.getField("inclusive").getType().getData(MetadataRepository.MIN_INCLUSIVE).toString()));
        assertEquals(50,
                Integer.parseInt(entityType.getField("name").getType().getData(MetadataRepository.DATA_MAX_LENGTH).toString()));
        assertEquals(20,
                Integer.parseInt(entityType.getField("name").getType().getData(MetadataRepository.DATA_MIN_LENGTH).toString()));
    }

    public void test_35() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema35.xsd");
        repository.load(stream);

        ComplexTypeMetadata entityType = repository.getComplexType("Entity");
        assertNotNull(entityType);
        assertTrue(entityType.hasField("Id"));
        assertTrue(entityType.hasField("aa-non-anonymous"));
        assertTrue(entityType.hasField("aa-non-anonymous/aa-sub"));
        assertTrue(entityType.hasField("aa-non-anonymous/bb-anonymous"));
        assertTrue(entityType.hasField("aa-non-anonymous/bb-anonymous/bb-sub"));
        assertTrue(entityType.hasField("do"));
        assertTrue(entityType.hasField("do/do-sub"));

        assertEquals("X_ANONYMOUS0", entityType.getField("aa-non-anonymous/bb-anonymous").getType().getName());
        assertEquals("X_ANONYMOUS1", entityType.getField("do").getType().getName());
    }

    public void test36_RenderInMainTab() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("SortType_05.xsd");
        repository.load(stream);

        //1 set the X_ForeignKey_NotSep value
        ComplexTypeMetadata component = repository.getComplexType("Component");
        ReferenceFieldMetadata defaultAirbag = (ReferenceFieldMetadata) component.getField("DefaultAirbag_Fk");
        ReferenceFieldMetadata associatedComponent = (ReferenceFieldMetadata) component.getField("AssociatedComponent_Fk");

        assertTrue(defaultAirbag.isFKMainRender());
        assertFalse(associatedComponent.isFKMainRender());

        //2. no set X_ForeignKey_NotSep, default is false
        ComplexTypeMetadata finishedProduct = repository.getComplexType("FinishedProduct");
        ReferenceFieldMetadata compositionFk = (ReferenceFieldMetadata) finishedProduct.getField("Composition_Fk");

        assertFalse(compositionFk.isFKMainRender());
    }

    public void test37_RenderInMainTabForContainerField() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream = getClass().getResourceAsStream("schema37.xsd");
        repository.load(stream);

        // 1 Reference Field (not set X_ForeignKey_NotSep = false)
        ComplexTypeMetadata product = repository.getComplexType("Product");
        ReferenceFieldMetadata family = (ReferenceFieldMetadata) product.getField("Family");
        assertFalse(family.isFKMainRender());

        // 2. Reference in Anonymous Container field (set X_ForeignKey_NotSep = true )
        ContainedTypeFieldMetadata storeContainer = ((ContainedTypeFieldMetadata) product.getField("Stores"));
        ReferenceFieldMetadata storeRefer = (ReferenceFieldMetadata) storeContainer.getContainedType().getField("Store");
        assertTrue(storeRefer.isFKMainRender());

        // 3. Reference in NON-Anonymous Container field (set X_ForeignKey_NotSep = true )
        ContainedComplexTypeMetadata bookContainer = (ContainedComplexTypeMetadata) ((ContainedTypeFieldMetadata) product
                .getField("Book")).getContainedType().getContainer().getType();
        ReferenceFieldMetadata bookRefer = (ReferenceFieldMetadata) bookContainer.getContainedType().getField("FamilyName");
        assertTrue(bookRefer.isFKMainRender());

        // 4. Reference in NON-Anonymous Container field (set X_ForeignKey_NotSep = false )
        ContainedComplexTypeMetadata publishContainer = (ContainedComplexTypeMetadata) ((ContainedTypeFieldMetadata) product
                .getField("Publish")).getContainedType().getContainer().getType();
        ReferenceFieldMetadata publishRefer = (ReferenceFieldMetadata) publishContainer.getContainedType()
                .getField("PublishName");
        assertFalse(publishRefer.isFKMainRender());
    }
}
