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


@SuppressWarnings("nls")
public class ReusableTypeTest extends SchemaManagerAbstractTest {

    public void testParseReusableType() throws Exception {
        SchemaManager schemaManager = new SchemaAgentTestMock();

        String schemaFileName = "Contrat.xsd";
        String schema = loadDataModelSchema(schemaFileName);
        if (schema == null)
            fail("Failed to load " + schemaFileName);

        DataModelBean dataModelBean = schemaManager.instantiateDataModelBean(schema);

        ReusableType typeEDA = dataModelBean.getReusableType("typeEDA");
        typeEDA.load();
        assertNull(typeEDA.getOrderValue());

        ReusableType balit = dataModelBean.getReusableType("Balit");
        balit.load();
        assertEquals("2", balit.getOrderValue());

        ReusableType secoursMutuelGrt = dataModelBean.getReusableType("SecoursMutuelGrt");
        secoursMutuelGrt.load();
        assertEquals("1", secoursMutuelGrt.getOrderValue());

        ReusableType pointInjectionRpt = dataModelBean.getReusableType("PointInjectionRpt");
        pointInjectionRpt.load();
        assertEquals("11", pointInjectionRpt.getOrderValue());

    }

}