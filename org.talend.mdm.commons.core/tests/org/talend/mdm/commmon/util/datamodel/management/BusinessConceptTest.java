/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.commmon.util.datamodel.management;

import java.util.List;
import java.util.Map;

@SuppressWarnings("nls")
public class BusinessConceptTest extends SchemaManagerAbstractTest {

    public void testParseBusinessConcept() throws Exception {
        SchemaManager schemaManager = new SchemaAgentTestMock();

        String schemaFileName="Product.xsd";
        String schema = loadDataModelSchema(schemaFileName);
        if (schema == null)
            fail("Failed to load " + schemaFileName);

        DataModelBean dataModelBean = schemaManager.instantiateDataModelBean(schema);
        BusinessConcept bizConcept = dataModelBean.getBusinessConcept("Product");

        bizConcept.load();
        
        assertEquals("Product", bizConcept.getName());
        
        Map<String, String> xpathTypeMap = bizConcept.getXpathTypeMap();
        Map<String, String> xpathDerivedSimpleTypeMap = bizConcept.getXpathDerivedSimpleTypeMap();
        assertNotNull(xpathTypeMap);
        assertNotNull(xpathDerivedSimpleTypeMap);
        
        // basic simple type
        assertEquals("xsd:boolean", xpathTypeMap.get("Product/Availability"));
        assertEquals("xsd:string", xpathTypeMap.get("Product/Id"));
        assertEquals("xsd:decimal", xpathTypeMap.get("Product/Price"));

        // complex type
        assertEquals(ReusableType.COMPLEX_TYPE, xpathTypeMap.get("Product"));
        assertEquals(ReusableType.COMPLEX_TYPE, xpathTypeMap.get("Product/Features"));

        // derived simple type
        assertEquals("xsd:string", xpathTypeMap.get("Product/Features/Sizes/Size"));
        assertEquals("xsd:string", xpathTypeMap.get("Product/Picture"));
        assertEquals("xsd:PICTURE", xpathDerivedSimpleTypeMap.get("Product/Picture"));

        // key path
        List<String> keyPaths = bizConcept.getKeyFieldPaths();
        assertEquals(1, keyPaths.size());
        assertEquals("Product/Id", keyPaths.get(0));

    }

}
