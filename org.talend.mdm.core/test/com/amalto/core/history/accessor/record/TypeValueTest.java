/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 *  %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.history.accessor.record;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import static org.junit.Assert.assertNotNull;

@RunWith(JUnit4.class)
public class TypeValueTest {

    private TypeValue typeValue;

    @Before
    public void setUp() throws Exception {
        typeValue = new TypeValue();
    }

    @Test
    public void lookupFieldWithName() {
        MetadataRepository repository = new MetadataRepository();
        repository.load(TypeValueTest.class.getResourceAsStream("schema1.xsd"));

        ComplexTypeMetadata newType = typeValue
                .lookupNonInstantiableFieldWithName(repository.getComplexType("insuredAddressAttorneyAF"), "addressPersonType");
        assertNotNull(newType);
    }

    @Test
    public void lookupFieldWithName2() {
        MetadataRepository repository = new MetadataRepository();
        repository.load(TypeValueTest.class.getResourceAsStream("schema2.xsd"));

        ComplexTypeMetadata newType = typeValue
                .lookupNonInstantiableFieldWithName(repository.getComplexType("Test"), "JuniorSchool");
        assertNotNull(newType);
    }
}