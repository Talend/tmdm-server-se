/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.metadata.validation;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.metadata.TypeMetadata;
import org.talend.mdm.commmon.metadata.Types;
import org.talend.mdm.commmon.metadata.ValidationError;
import org.talend.mdm.commmon.metadata.ValidationHandler;
import org.w3c.dom.Element;

import junit.framework.TestCase;

public class MetadataValidationTest extends TestCase {

    // See TMDMQA-6270 / 6330
    public void testFK1() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FK_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(3, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_EXIST));
        assertTrue(handler.getLineNumbers().contains(31));
        assertTrue(handler.getLineNumbers().contains(47));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    // See TMDMQA-6363
    public void testFK2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FK2_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_EXIST));
        assertTrue(handler.getLineNumbers().contains(30));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testFK3() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FK3_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(0, handler.getErrorCount());
        assertEquals(0, handler.getWarningCount());
        assertTrue(handler.getLineNumbers().isEmpty());
    }

    public void testFK4() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FK4_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(0, handler.getErrorCount());
        assertEquals(1, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.FOREIGN_KEY_USES_MAX_LENGTH));
        assertTrue(handler.getLineNumbers().contains(18));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    // See TMDMQA-6364
    public void testFK5() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FK5_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_OWN_FIELD));
        assertTrue(handler.getLineNumbers().contains(39));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testFK6() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FK6_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_EXIST));
        assertTrue(handler.getLineNumbers().contains(39));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testFK7() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FK7_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_EXIST));
        assertTrue(handler.getLineNumbers().contains(181));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testFK8() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FK8_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(0, handler.getErrorCount());
        assertEquals(2, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.FOREIGN_KEY_USES_MAX_LENGTH));

        ComplexTypeMetadata type = repository.getComplexType("REF_CD_SET_VAL");
        assertNotNull(type);
        assertTrue(type.hasField("REF_VAL_CD"));
        FieldMetadata field = type.getField("REF_VAL_CD");
        assertTrue(field.getType().getName().startsWith("X_ANONYMOUS"));
        assertEquals(1, field.getType().getSuperTypes().size());
        assertEquals(Types.STRING, field.getType().getSuperTypes().iterator().next().getName());
    }

    // see TMDM-11593
    public void testFK9() throws Exception {
        TestValidationHandler handler = new TestValidationHandler();
        try {
            MetadataRepository repository = new MetadataRepository();
            InputStream resourceAsStream = this.getClass().getResourceAsStream("FK9_0.1.xsd");
            repository.load(resourceAsStream, handler);
        } catch (Exception e) {
            //
        }
        Set<ValidationError> errors = handler.errors;
        assertTrue(errors.size() == 1);
        assertTrue(errors.iterator().next() == ValidationError.FOREIGN_KEY_HOST_INVALID);
    }

    public void testVisibility1() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("Visibility_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(0, handler.getErrorCount());
        assertEquals(2, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.MANDATORY_FIELD_MAY_NOT_BE_VISIBLE));
    }

    public void testVisibility2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("Visibility2_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(0, handler.getErrorCount());
        assertEquals(0, handler.getWarningCount());
    }

    public void testFKPointToNonPK() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FKCheck.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(1, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.FOREIGN_KEY_SHOULD_POINT_TO_PRIMARY_KEY));
        assertTrue(handler.getLineNumbers().contains(30));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    // See TMDMQA-6366
    public void testFKINFO() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FKINFO.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.FOREIGN_KEY_INFO_NOT_PRIMITIVE_XSD_TYPED));
        assertEquals(2, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_OWN_FIELD));
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_EXIST));
        assertTrue(handler.getLineNumbers().contains(23));
        assertTrue(handler.getLineNumbers().contains(24));
        assertTrue(handler.getLineNumbers().contains(26));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testFKInfo2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FKINFO2.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(1, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.FOREIGN_KEY_INFO_NOT_PRIMITIVE_XSD_TYPED));
        assertTrue(handler.getLineNumbers().contains(10));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    // See TMDMQA-6365
    public void testFKInfo3() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FKINFO3.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.FOREIGN_KEY_INFO_NOT_REFERENCING_FK_TYPE));
        assertTrue(handler.getLineNumbers().contains(150));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    // See TMDMQA-6332
    public void testFKInfo4() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FKINFO4.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(1, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.FOREIGN_KEY_INFO_REPEATABLE));
        assertTrue(handler.getLineNumbers().contains(21));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testFKInfo5() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FKINFO5.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertEquals(0, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_EXIST));
        assertTrue(handler.getLineNumbers().contains(161));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testFKInfo6() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FKINFO6.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertEquals(0, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_OWN_FIELD));
        assertTrue(handler.getLineNumbers().contains(213));
        assertFalse(handler.getLineNumbers().contains(null));
    }
    
    public void testFKInfo7() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("FKINFO7.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertEquals(0, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.FOREIGN_KEY_INFO_NOT_REFERENCING_FK_TYPE));
        assertTrue(handler.getLineNumbers().contains(183));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testPKINFO_manyType() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PKINFO_manyType_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(2, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_OWN_FIELD));
        assertTrue(handler.getLineNumbers().contains(21));
        assertTrue(handler.getLineNumbers().contains(36));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testPK1() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PK1_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_OWN_FIELD));
        assertTrue(handler.getLineNumbers().contains(15));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    // See TMDMQA-6327 / 6328
    public void testPK2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PK2_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.PRIMARY_KEY_INFO_CANNOT_BE_REPEATABLE));
        assertTrue(handler.getLineNumbers().contains(16));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    // See TMDMQA-6329
    public void testPK3() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PK3_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertTrue(handler.getErrorPaths().contains("nameEEE"));
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_OWN_FIELD));
        assertTrue(handler.getLineNumbers().contains(15));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testPK3_ManyPKInfo() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PK3_ManyPKINFO_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(4, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_OWN_FIELD));
        assertTrue(handler.getMessages().contains(ValidationError.PRIMARY_KEY_INFO_NOT_IN_ENTITY));
        assertTrue(handler.getLineNumbers().contains(15));
        assertTrue(handler.getLineNumbers().contains(16));
        assertTrue(handler.getLineNumbers().contains(17));
        assertTrue(handler.getLineNumbers().contains(48));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    // See TMDMQA-6326
    public void testPK4() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PK4_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.PRIMARY_KEY_INFO_NOT_IN_ENTITY));
        assertTrue(handler.getLineNumbers().contains(15));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testPK5() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PK5_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(2, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.FIELD_KEY_MUST_BE_MANDATORY));
        assertTrue(handler.getLineNumbers().contains(28));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testPK6() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PK6_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(3, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.FIELD_KEY_MUST_BE_MANDATORY));
        assertTrue(handler.getMessages().contains(ValidationError.FIELD_KEY_CANNOT_BE_REPEATABLE));
        assertTrue(handler.getLineNumbers().contains(26));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testPK7() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PK7_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(2, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_OWN_FIELD));
        assertTrue(handler.getLineNumbers().contains(181));
        assertTrue(handler.getLineNumbers().contains(232));
        assertTrue(handler.getLineNumbers().contains(375));
        assertTrue(handler.getLineNumbers().contains(388));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testPKInfo() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PKINFO_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(3, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_OWN_FIELD));
        assertTrue(handler.getLineNumbers().contains(21));
        assertTrue(handler.getLineNumbers().contains(190));
        assertTrue(handler.getLineNumbers().contains(318));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    // See TMDMQA-6333
    public void testPKInfo2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PKINFO2_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(1, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.PRIMARY_KEY_INFO_TYPE_NOT_PRIMITIVE));
        assertTrue(handler.getLineNumbers().contains(15));
        assertFalse(handler.getLineNumbers().contains(null));
    }
    
    // See TMDM-6554
    public void testSecondLevelPkInfo() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("PKINFO_2LevelPKInfo.xsd"); //$NON-NLS-1$
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(0, handler.getWarningCount());
        assertEquals(0, handler.getErrorCount());
    }

    // See TMDMQA-6317
    public void testLookUpField1() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("LookUpField1_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(1, handler.getErrorCount());
        assertTrue(handler.getMessages().contains(ValidationError.LOOKUP_FIELD_CANNOT_BE_KEY));
        assertTrue(handler.getLineNumbers().contains(5));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    // See TMDMQA-6313
    public void testLookUpField2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("LookUpField2_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(2, handler.getErrorCount()); // TODO Should be 1?
        assertTrue(handler.getMessages().contains(ValidationError.LOOKUP_FIELD_NOT_IN_ENTITY));
        assertTrue(handler.getLineNumbers().contains(18));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testLookUpField3() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("LookUpField3_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(0, handler.getErrorCount());
        assertEquals(0, handler.getWarningCount());
        assertTrue(handler.getLineNumbers().isEmpty());
    }

    // See TMDMQA-6357 / 6367
    public void testInheritance() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("Inheritance.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
        assertEquals(3, handler.getErrorCount()); // TODO Check other errors in data model in assert
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_CANNOT_OVERRIDE_SUPER_TYPE_KEY));
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_DOES_NOT_EXIST));
        assertTrue(handler.getLineNumbers().contains(18));
        assertTrue(handler.getLineNumbers().contains(160));
        assertTrue(handler.getLineNumbers().contains(393));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testXSDAttributeWarning() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("xsd_attributes.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(6, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_USE_XSD_ATTRIBUTES));
        assertTrue(handler.getLineNumbers().contains(63));
        assertTrue(handler.getLineNumbers().contains(113));
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testReferenceTypeWarning1() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("ReferenceTypeElement1.xsd"); //$NON-NLS-1$
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(0, handler.getErrorCount());
    }
    
    public void testReferenceTypeWarning2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("ReferenceTypeElement2.xsd"); //$NON-NLS-1$
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
            fail("Should fail validation.");
        } catch (Exception e) {
            // Expected
        }
    }

    public void testUnusedType1() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("UnusedType1_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(1, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.UNUSED_REUSABLE_TYPE));
        assertTrue(handler.getLineNumbers().contains(50));
        assertFalse(handler.getLineNumbers().contains(null));
    }
    
    public void testUnusedType2() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("UnusedType2_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(0, handler.getWarningCount());
        assertFalse(handler.getLineNumbers().contains(null));
    }

    public void testUnusedType3() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("UnusedType3_0.1.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(0, handler.getWarningCount());
        assertFalse(handler.getLineNumbers().contains(null));
    }
    
    public void testTypeUsageCount() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream stream1 = getClass().getResourceAsStream("ReusableType.xsd"); //$NON-NLS-1$
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(stream1, handler);

        InputStream stream2 = getClass().getResourceAsStream("ReusableType.xsd"); //$NON-NLS-1$
        MetadataRepository repositoryBackup = repository.copy();
        try {
            repositoryBackup.load(stream2, handler);
        } catch (Exception e) {
            fail("Type usage count fail."); //$NON-NLS-1$
        }
        assertEquals(0, handler.getErrorCount());
    }

    public void testForeignKeyFilter() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("ForeignKeyFilter.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        repository.load(resourceAsStream, handler);
        assertEquals(0, handler.getErrorCount());
        assertEquals(1, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.FOREIGN_KEY_INFO_INVALID));
        assertTrue(handler.getLineNumbers().contains(22));
        assertFalse(handler.getLineNumbers().contains(null));
    }
    
    public void testInheritanceOverride() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("InheritanceOverride.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
        } catch (Exception e) {
            // Expected
        }
        assertEquals(2, handler.getErrorCount());
        assertEquals(0, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.FIELD_CANNOT_OVERRIDE_INHERITED_ELEMENT));
        assertTrue(handler.getLineNumbers().contains(359));
        assertFalse(handler.getLineNumbers().contains(null));
    }
    
    public void testTypeNameConstraint() throws Exception {
        MetadataRepository repository = new MetadataRepository();
        InputStream resourceAsStream = this.getClass().getResourceAsStream("TypeNaming.xsd");
        TestValidationHandler handler = new TestValidationHandler();
        try {
            repository.load(resourceAsStream, handler);
        } catch (Exception e) {
            // Expected
        }
        assertEquals(2, handler.getWarningCount());
        assertTrue(handler.getMessages().contains(ValidationError.TYPE_INVALID_NAME));
        assertTrue(handler.getLineNumbers().contains(3));
        assertTrue(handler.getLineNumbers().contains(27));
        
    }

    private static class TestValidationHandler implements ValidationHandler {

        private int errorField;

        private int warningField;

        private int errorType;

        private int warningType;

        private Set<ValidationError> errors = new HashSet<ValidationError>();

        private Set<Integer> lineNumbers = new HashSet<Integer>();

        private Set<String> errorPaths = new HashSet<String>();

        @Override
        public void fatal(FieldMetadata field, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
            errors.add(error);
        }

        @Override
        public void error(FieldMetadata field, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
            errorPaths.add(field.getPath());
            errors.add(error);
            lineNumbers.add(lineNumber);
            errorField++;
        }

        @Override
        public void warning(FieldMetadata field, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
            errors.add(error);
            lineNumbers.add(lineNumber);
            warningField++;
        }

        @Override
        public void fatal(TypeMetadata type, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
            errors.add(error);
        }

        @Override
        public void error(TypeMetadata type, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
            errors.add(error);
            lineNumbers.add(lineNumber);
            errorType++;
        }

        @Override
        public void warning(TypeMetadata type, String message, Element element, Integer lineNumber, Integer columnNumber, ValidationError error) {
            errors.add(error);
            lineNumbers.add(lineNumber);
            warningType++;
        }

        @Override
        public void end() {
            if (getErrorCount() > 0) {
                throw new RuntimeException("Validation failed.");
            }
        }

        @Override
        public int getErrorCount() {
            return errorField + errorType;
        }

        public int getWarningCount() {
            return warningField + warningType;
        }

        private Set<ValidationError> getMessages() {
            return errors;
        }

        private Set<Integer> getLineNumbers() {
            return lineNumbers;
        }

        public Set<String> getErrorPaths() {
            return errorPaths;
        }
    }
}
