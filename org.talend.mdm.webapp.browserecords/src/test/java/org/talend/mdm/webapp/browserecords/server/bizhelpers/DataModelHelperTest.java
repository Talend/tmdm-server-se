/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.bizhelpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.lang.StringUtils;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit3.PowerMockSuite;
import org.talend.mdm.commmon.util.datamodel.management.BusinessConcept;
import org.talend.mdm.commmon.util.datamodel.management.DataModelID;
import org.talend.mdm.webapp.base.client.model.DataTypeCustomized;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;

import com.amalto.core.util.Util;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.impl.ComplexTypeImpl;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;

@PrepareForTest({ Util.class })
@SuppressWarnings("nls")
public class DataModelHelperTest extends TestCase {

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + DataModelHelperTest.class.getSimpleName(), DataModelHelperTest.class);
    }

    public void testParsingPermissionsMetadata() throws Exception {
        EntityModel entityModel = new EntityModel();
        String datamodelName = "M01";
        String concept = "M01_E01";
        String[] ids = { "M01_E01/subelement" };
        String[] roles = { "System_Admin", "authenticated", "administration" };
        InputStream stream = getClass().getResourceAsStream("M01.xsd");
        String xsd = inputStream2String(stream);

        PowerMockito.mockStatic(Util.class);
        Mockito.when(Util.isEnterprise()).thenReturn(true);

        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema("Contract", "Contract", DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, entityModel,
                Arrays.asList(roles));

        Map<String, TypeModel> metaDataTypes = entityModel.getMetaDataTypes();

        // Check that if a field is No Access, then parsed to be not visible in WebUI
        TypeModel tm = metaDataTypes.get("M01_E01/f2");
        assertNotNull(tm);
        assertTrue(!tm.isVisible());

        // Check that if a field has no explicit permissions, then parsed to be visible in WebUI
        tm = metaDataTypes.get("M01_E01/f1");
        assertNotNull(tm);
        assertTrue(tm.isVisible());

        // Check that if a field has explicit No Access and Write Access, then parsed to be not visible in WebUI
        tm = metaDataTypes.get("M01_E01/f3");
        assertNotNull(tm);
        assertTrue(!tm.isVisible());

        // Check that if a subfield has explicit No Access, then parsed to be not visible in WebUI
        tm = metaDataTypes.get("M01_E01/f4/sf1");
        assertNotNull(tm);
        assertTrue(!tm.isVisible());

        // Check that if a subfield has explicit No Access, then parsed to be not visible in WebUI
        tm = metaDataTypes.get("M01_E01/f4/sf2");
        assertNotNull(tm);
        assertTrue(!tm.isVisible());

        // Check that if a custom subfield has explicit No Access, then parsed to be not visible in WebUI
        tm = metaDataTypes.get("M01_E01/f5/sf1");
        assertNotNull(tm);
        assertTrue(!tm.isVisible());

        // Check that if a custom subfield has explicit No Access, then parsed to be not visible in WebUI
        tm = metaDataTypes.get("M01_E01/f5/sf2");
        assertNotNull(tm);
        assertTrue(!tm.isVisible());

        // What happens is parent node should be invisible but child node is set to visible?
        tm = metaDataTypes.get("M01_E01/f6");
        assertNotNull(tm);
        assertTrue(!tm.isVisible());

        // What happens is parent node should be invisible but child node is set to visible?
        tm = metaDataTypes.get("M01_E01/f6/sf1");
        assertNotNull(tm);
        assertTrue(tm.isVisible());

    }

    public void testTypePath() throws Exception {

        EntityModel entityModel = new EntityModel();
        String datamodelName = "RTE";
        String concept = "Contrat";
        String[] ids = { "" };
        String[] roles = { "Demo_Manager", "System_Admin", "authenticated", "administration" };
        InputStream stream = getClass().getResourceAsStream("RTE.xsd");
        String xsd = inputStream2String(stream);

        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema(datamodelName, concept, DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, entityModel,
                Arrays.asList(roles));
        Map<String, TypeModel> metaDataTypes = entityModel.getMetaDataTypes();
        TypeModel testModel = metaDataTypes
                .get("Contrat/detailContrat:AP-RP/Perimetre/entitesPresentes/EDPs/EDP/dateDebutApplication");
        assertEquals("Contrat/detailContrat/Perimetre/entitesPresentes/EDPs/EDP/dateDebutApplication", testModel.getXpath());

    }

    public void testParsingDeleteCreatePermissionsMetadata() throws Exception {

        EntityModel entityModel = new EntityModel();
        String datamodelName = "M01";
        String concept = "M01_E02";
        String[] ids = { "M01_E02/subelement" };
        String[] roles = { "System_Admin", "authenticated", "administration" };
        InputStream stream = getClass().getResourceAsStream("M01.xsd");
        String xsd = inputStream2String(stream);

        PowerMockito.mockStatic(Util.class);
        Mockito.when(Util.isEnterprise()).thenReturn(true);

        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema("Contract", "Contract", DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, entityModel,
                Arrays.asList(roles));

        Map<String, TypeModel> metaDataTypes = entityModel.getMetaDataTypes();

        // Check that if a field is No Access, then parsed to be not visible in WebUI
        TypeModel tm = metaDataTypes.get("M01_E02");
        assertNotNull(tm);
        assertTrue(tm.isDenyLogicalDeletable());
        assertTrue(tm.isDenyPhysicalDeleteable());
        assertTrue(tm.isDenyCreatable());
    }

    public void testParsingMetadata() throws Exception {

        EntityModel entityModel = new EntityModel();
        String datamodelName = "Contract";
        String concept = "Contract";
        String[] ids = { "" };
        String[] roles = { "Demo_Manager", "System_Admin", "authenticated", "administration" };
        InputStream stream = getClass().getResourceAsStream("Contract.xsd");
        String xsd = inputStream2String(stream);

        PowerMockito.mockStatic(Util.class);
        Mockito.when(Util.isEnterprise()).thenReturn(false);

        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema("Contract", "Contract", DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, entityModel,
                Arrays.asList(roles));
        Map<String, TypeModel> metaDataTypes = entityModel.getMetaDataTypes();
        assertEquals(13, metaDataTypes.size());
        assertTrue(!metaDataTypes.get("Contract/detail").isSimpleType());

        stream = getClass().getResourceAsStream("ContractMultiLevel.xsd");
        xsd = inputStream2String(stream);
        EntityModel newModel = new EntityModel();

        PowerMockito.mockStatic(Util.class);
        Mockito.when(Util.isEnterprise()).thenReturn(false);

        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema("Contract", "Contract", DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, newModel,
                Arrays.asList(roles));
        metaDataTypes = newModel.getMetaDataTypes();
        assertEquals(11, metaDataTypes.size());
        assertFalse(metaDataTypes.get("Contract/detail").isSimpleType());
        assertTrue(metaDataTypes.get("Contract/detail/code").isSimpleType());
        assertTrue(metaDataTypes.get("Contract/detail:ContractDetailSubType/code").isSimpleType());
        assertTrue(metaDataTypes.get("Contract/detail:ContractDetailSubType/subType").isSimpleType());
        assertTrue(metaDataTypes.get("Contract/detail:ContractDetailSubTypeOne/code").isSimpleType());
        assertTrue(metaDataTypes.get("Contract/detail:ContractDetailSubTypeOne/subType").isSimpleType());
        assertTrue(metaDataTypes.get("Contract/detail:ContractDetailSubTypeOne/subTypeOne").isSimpleType());
    }

    public void testParseTypeOrder() throws Exception {

        EntityModel entityModel = new EntityModel();
        String datamodelName = "Contract";
        String concept = "Contract";
        String[] ids = { "" };
        String[] roles = { "Demo_Manager", "System_Admin", "authenticated", "administration" };
        InputStream stream = getClass().getResourceAsStream("RTE.xsd");
        String xsd = inputStream2String(stream);

        PowerMockito.mockStatic(Util.class);
        Mockito.when(Util.isEnterprise()).thenReturn(false);

        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema("RTE", "Eda", DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, entityModel,
                Arrays.asList(roles));
        Map<String, TypeModel> metaDataTypes = entityModel.getMetaDataTypes();
        for (String typePath : metaDataTypes.keySet()) {
            if (typePath.equals("Eda/typesEda/typeEDA")) {
                TypeModel typeModel = metaDataTypes.get(typePath);
                List<ComplexTypeModel> reusableTypes = ((ComplexTypeModel) typeModel).getReusableComplexTypes();
                assertTrue(reusableTypes.size() > 0);
                for (ComplexTypeModel myTypeModel : reusableTypes) {
                    if (myTypeModel.getName().equals("typeEDA")) {
                        assertEquals(0, myTypeModel.getOrderValue());
                    } else if (myTypeModel.getName().equals("PointEchange")) {
                        assertEquals(3, myTypeModel.getOrderValue());
                    } else if (myTypeModel.getName().equals("PointInjectionRptRpd")) {
                        assertEquals(13, myTypeModel.getOrderValue());
                    }
                }
            }
        }

    }

    private String inputStream2String(InputStream is) {

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            fail();
        }
        return buffer.toString();

    }

    public void testConvertXsd2ElDecl() throws Exception {
        String concept = "Product";
        String xsd = inputStream2String(this.getClass().getResourceAsStream("Product.xsd"));
        XSElementDecl decl = DataModelHelper.convertXsd2ElDecl(concept, xsd);
        assertNotNull(decl);
        assertEquals(concept, decl.getName());

        concept = "ABC";
        decl = DataModelHelper.convertXsd2ElDecl(concept, xsd);
        assertNull(decl);
    }

    public void testFindTypeModelByTypePath() {
        try {
            DataModelHelper.findTypeModelByTypePath(null, null);
            fail();
        } catch (Exception e) {
            assertNotNull(e);
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        try {
            DataModelHelper.findTypeModelByTypePath(new HashMap<String, TypeModel>(), null);
            fail();
        } catch (Exception e) {
            assertNotNull(e);
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        try {
            DataModelHelper.findTypeModelByTypePath(null, "Product/Name");
            fail();
        } catch (Exception e) {
            assertNotNull(e);
            assertEquals(IllegalArgumentException.class, e.getClass());
        }

        try {
            DataModelHelper.findTypeModelByTypePath(new HashMap<String, TypeModel>(), "Product/Name");
            fail();
        } catch (Exception e) {
            assertNotNull(e);
            assertEquals(TypeModelNotFoundException.class, e.getClass());
            assertEquals(((TypeModelNotFoundException) e).getXpathNotFound(), "Product/Name");
        }

    }

    public void testGetBusinessConcept() {
        String datamodelName = "Product";
        String concept = "Product";
        String xsd = inputStream2String(this.getClass().getResourceAsStream("Product.xsd"));

        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        XSElementDecl decl = DataModelHelper.getBusinessConcept(datamodelName, concept);
        assertNotNull(decl);
        assertEquals(concept, decl.getName());
        assertEquals(ComplexTypeImpl.class, decl.getType().getClass());
    }

    public void testGetElementDeclByName() throws Exception {
        String concept = "Product";
        String xsd = inputStream2String(this.getClass().getResourceAsStream("Product.xsd"));
        XSOMParser reader = new XSOMParser();
        reader.setAnnotationParser(new DomAnnotationParserFactory());
        reader.parse(new StringReader(xsd));
        XSSchemaSet xss = reader.getResult();
        XSElementDecl decl = DataModelHelper.getElementDeclByName(concept, xss);
        assertNotNull(decl);
        assertEquals(concept, decl.getName());
        assertEquals(ComplexTypeImpl.class, decl.getType().getClass());
    }

    public void testProductDemo() throws Exception {
        EntityModel entityModel = new EntityModel();
        String datamodelName = "Product";
        String concept = "Product";
        String[] ids = { "" };
        String[] roles = { "Demo_User", "Demo_Manager", "System_Admin", "authenticated", "administration" };
        String xsd = inputStream2String(this.getClass().getResourceAsStream("Product.xsd"));
        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema(datamodelName, concept, DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, entityModel,
                Arrays.asList(roles));
        Map<String, TypeModel> metaDataTypes = entityModel.getMetaDataTypes();
        assertNotSame(0, metaDataTypes.size());
        assertEquals(concept, entityModel.getConceptName());
        assertEquals("Product", entityModel.getConceptLabel("en"));
        assertEquals("Produit", entityModel.getConceptLabel("fr"));
        assertEquals(16, metaDataTypes.size());

        Set<String> keySet = metaDataTypes.keySet();
        assertTrue(keySet.contains("Product"));
        assertFalse(keySet.contains("ProductFamily"));

        assertTrue(keySet.contains("Product/Id"));
        assertEquals(1, metaDataTypes.get("Product/Id").getMinOccurs());
        assertEquals(1, metaDataTypes.get("Product/Id").getMaxOccurs());
        assertEquals("string", metaDataTypes.get("Product/Id").getType().getTypeName());
        assertEquals("Unique Id", metaDataTypes.get("Product/Id").getLabelMap().get("en"));
        assertEquals("Id unique", metaDataTypes.get("Product/Id").getLabelMap().get("fr"));

        assertTrue(keySet.contains("Product/Picture"));
        assertEquals(0, metaDataTypes.get("Product/Picture").getMinOccurs());
        assertEquals(1, metaDataTypes.get("Product/Picture").getMaxOccurs());
        assertEquals("PICTURE", metaDataTypes.get("Product/Picture").getType().getTypeName());
        assertEquals("Picture", metaDataTypes.get("Product/Picture").getLabelMap().get("en"));
        assertEquals("Image", metaDataTypes.get("Product/Picture").getLabelMap().get("fr"));

        assertTrue(keySet.contains("Product/Name"));
        assertEquals(SimpleTypeModel.class, metaDataTypes.get("Product/Name").getClass());
        assertEquals(1, metaDataTypes.get("Product/Name").getMinOccurs());
        assertEquals(1, metaDataTypes.get("Product/Name").getMaxOccurs());
        assertEquals("string", metaDataTypes.get("Product/Name").getType().getTypeName());
        assertEquals("Name", metaDataTypes.get("Product/Name").getLabelMap().get("en"));
        assertEquals("Nom", metaDataTypes.get("Product/Name").getLabelMap().get("fr"));

        assertTrue(keySet.contains("Product/Description"));
        assertFalse(metaDataTypes.get("Product/Description").isDenyCreatable());
        assertFalse(metaDataTypes.get("Product/Description").isDenyLogicalDeletable());
        assertFalse(metaDataTypes.get("Product/Description").isDenyPhysicalDeleteable());
        assertNull(metaDataTypes.get("Product/Description").getReusableTypes());

        assertTrue(keySet.contains("Product/Features"));
        assertEquals(ComplexTypeModel.class, metaDataTypes.get("Product/Features").getClass());
        assertEquals(0, metaDataTypes.get("Product/Features").getMinOccurs());
        assertEquals(1, metaDataTypes.get("Product/Features").getMaxOccurs());
        assertEquals("unknow", metaDataTypes.get("Product/Features").getType().getTypeName());
        assertFalse(metaDataTypes.get("Product/Features").isAutoExpand());
        assertNull(metaDataTypes.get("Product/Features").getReusableTypes());
        assertNotNull(((ComplexTypeModel) metaDataTypes.get("Product/Features")).getSubTypes());
        assertEquals(2, ((ComplexTypeModel) metaDataTypes.get("Product/Features")).getSubTypes().size());

        assertTrue(keySet.contains("Product/Features/Sizes"));
        assertEquals(0, metaDataTypes.get("Product/Features/Sizes").getMinOccurs());
        assertEquals(1, metaDataTypes.get("Product/Features/Sizes").getMaxOccurs());
        assertEquals(((ComplexTypeModel) metaDataTypes.get("Product/Features")).getSubTypes().get(0),
                metaDataTypes.get("Product/Features/Sizes"));

        assertTrue(keySet.contains("Product/Features/Sizes/Size"));
        assertEquals(SimpleTypeModel.class, metaDataTypes.get("Product/Features/Sizes/Size").getClass());
        assertEquals(1, metaDataTypes.get("Product/Features/Sizes/Size").getMinOccurs());
        assertEquals(-1, metaDataTypes.get("Product/Features/Sizes/Size").getMaxOccurs());
        assertEquals(DataTypeCustomized.class, metaDataTypes.get("Product/Features/Sizes/Size").getType().getClass());
        assertEquals("Size", metaDataTypes.get("Product/Features/Sizes/Size").getType().getTypeName());
        assertNotNull(((SimpleTypeModel) metaDataTypes.get("Product/Features/Sizes/Size")).getEnumeration());
        assertEquals(4, ((SimpleTypeModel) metaDataTypes.get("Product/Features/Sizes/Size")).getEnumeration().size());
        assertTrue(((SimpleTypeModel) metaDataTypes.get("Product/Features/Sizes/Size")).getEnumeration().contains("Small"));
        assertTrue(((SimpleTypeModel) metaDataTypes.get("Product/Features/Sizes/Size")).getEnumeration().contains("Medium"));
        assertTrue(((SimpleTypeModel) metaDataTypes.get("Product/Features/Sizes/Size")).getEnumeration().contains("Large"));
        assertTrue(((SimpleTypeModel) metaDataTypes.get("Product/Features/Sizes/Size")).getEnumeration().contains("X-Large"));

        assertEquals(true, keySet.contains("Product/Features/Colors"));
        assertEquals(((ComplexTypeModel) metaDataTypes.get("Product/Features")).getSubTypes().get(1),
                metaDataTypes.get("Product/Features/Colors"));

        assertEquals(true, keySet.contains("Product/Features/Colors/Color"));
        assertEquals(SimpleTypeModel.class, metaDataTypes.get("Product/Features/Colors/Color").getClass());
        assertEquals(1, metaDataTypes.get("Product/Features/Colors/Color").getMinOccurs());
        assertEquals(-1, metaDataTypes.get("Product/Features/Colors/Color").getMaxOccurs());
        assertEquals(DataTypeCustomized.class, metaDataTypes.get("Product/Features/Colors/Color").getType().getClass());
        assertEquals("Color", metaDataTypes.get("Product/Features/Colors/Color").getType().getTypeName());
        assertEquals(5, ((SimpleTypeModel) metaDataTypes.get("Product/Features/Colors/Color")).getEnumeration().size());
        assertTrue(((SimpleTypeModel) metaDataTypes.get("Product/Features/Colors/Color")).getEnumeration().contains("White"));
        assertTrue(((SimpleTypeModel) metaDataTypes.get("Product/Features/Colors/Color")).getEnumeration().contains("Light Blue"));
        assertTrue(((SimpleTypeModel) metaDataTypes.get("Product/Features/Colors/Color")).getEnumeration().contains("Light Pink"));
        assertTrue(((SimpleTypeModel) metaDataTypes.get("Product/Features/Colors/Color")).getEnumeration().contains("Lemon"));
        assertTrue(((SimpleTypeModel) metaDataTypes.get("Product/Features/Colors/Color")).getEnumeration().contains("Khaki"));

        assertEquals(true, keySet.contains("Product/Availability"));
        assertEquals("boolean", metaDataTypes.get("Product/Availability").getType().getTypeName());
        assertNull(metaDataTypes.get("Product/Availability").getForeignkey());

        assertEquals(true, keySet.contains("Product/Price"));
        assertEquals(1, metaDataTypes.get("Product/Price").getMinOccurs());
        assertEquals(1, metaDataTypes.get("Product/Price").getMaxOccurs());
        assertEquals("decimal", metaDataTypes.get("Product/Price").getType().getTypeName());
        assertNotNull(metaDataTypes.get("Product/Price").getDescriptionMap());
        assertEquals(2, metaDataTypes.get("Product/Price").getDescriptionMap().size());
        assertEquals("Run a price request to change this price", metaDataTypes.get("Product/Price").getDescriptionMap().get("en"));
        assertEquals("Faites une demande de changement de prix pour modifier", metaDataTypes.get("Product/Price")
                .getDescriptionMap().get("fr"));

        assertEquals(true, keySet.contains("Product/Family"));
        assertNotNull(metaDataTypes.get("Product/Family").getForeignkey());
        assertEquals("ProductFamily/Id", metaDataTypes.get("Product/Family").getForeignkey());
        assertNotNull(metaDataTypes.get("Product/Family").getForeignKeyInfo());
        assertEquals(1, metaDataTypes.get("Product/Family").getForeignKeyInfo().size());
        assertEquals("ProductFamily/Name", metaDataTypes.get("Product/Family").getForeignKeyInfo().get(0));
        assertEquals("\"Id: '\" + ProductFamily/Id +\"' Name: '\"+ ProductFamily/Name + \"'\"",
                metaDataTypes.get("Product/Family").getForeignKeyInfoFormat());
        assertNull(metaDataTypes.get("Product/Family").getForeignKeyFilter());

        assertEquals(true, keySet.contains("Product/OnlineStore"));
        assertEquals("URL", metaDataTypes.get("Product/OnlineStore").getType().getTypeName());

        assertEquals(true, keySet.contains("Product/Stores"));
        assertEquals(0, metaDataTypes.get("Product/Stores").getMinOccurs());
        assertEquals(1, metaDataTypes.get("Product/Stores").getMaxOccurs());
        assertEquals(ComplexTypeModel.class, metaDataTypes.get("Product/Stores").getClass());
        assertNotNull(((ComplexTypeModel) metaDataTypes.get("Product/Stores")).getSubTypes());
        assertEquals(1, ((ComplexTypeModel) metaDataTypes.get("Product/Stores")).getSubTypes().size());

        assertEquals(true, keySet.contains("Product/Stores/Store"));
        assertEquals(((ComplexTypeModel) metaDataTypes.get("Product/Stores")).getSubTypes().get(0),
                metaDataTypes.get("Product/Stores/Store"));
        assertNotNull(metaDataTypes.get("Product/Stores/Store").getForeignkey());
        assertEquals("Store/Id", metaDataTypes.get("Product/Stores/Store").getForeignkey());
        assertNotNull(metaDataTypes.get("Product/Stores/Store").getForeignKeyInfo());
        assertEquals(1, metaDataTypes.get("Product/Stores/Store").getForeignKeyInfo().size());
        assertEquals("Store/Address", metaDataTypes.get("Product/Stores/Store").getForeignKeyInfo().get(0));
        assertNull(metaDataTypes.get("Product/Stores/Store").getForeignKeyFilter());

        concept = "ProductFamily";
        entityModel = new EntityModel();
        DataModelHelper.parseSchema(datamodelName, concept, DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, entityModel,
                Arrays.asList(roles));
        metaDataTypes = entityModel.getMetaDataTypes();

        assertNotNull(metaDataTypes.get("ProductFamily"));
        assertNotNull(metaDataTypes.get("ProductFamily").getPrimaryKeyInfo());
        assertEquals(1, metaDataTypes.get("ProductFamily").getPrimaryKeyInfo().size());
        assertEquals("ProductFamily/Name", metaDataTypes.get("ProductFamily").getPrimaryKeyInfo().get(0));

        assertNotNull(metaDataTypes.get("ProductFamily/Id"));
        assertEquals("AUTO_INCREMENT", metaDataTypes.get("ProductFamily/Id").getType().getTypeName());
    }

    public void testParsePolymorphismLabels() throws Exception {
        String datamodelName = "Employee";
        String concept = "Employee";
        String[] ids = { "" };
        String[] roles = { "Emp_Manager", "Emp_User" };
        InputStream stream = getClass().getResourceAsStream("Employee.xsd");
        String language = "fr";
        String xsd = inputStream2String(stream);

        EntityModel employeeModel = new EntityModel();

        PowerMockito.mockStatic(Util.class);
        Mockito.when(Util.isEnterprise()).thenReturn(false);
        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema(datamodelName, concept, DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, employeeModel,
                Arrays.asList(roles));

        Map<String, TypeModel> types = employeeModel.getMetaDataTypes();
        TypeModel addressType = types.get("Employee/Address");
        assertEquals(true, addressType.isAbstract());
        assertEquals(2, addressType.getLabelMap().size());
        assertEquals("adresse", addressType.getLabel(language));

        List<ComplexTypeModel> reusableTypes = ((ComplexTypeModel) addressType).getReusableComplexTypes();
        assertEquals(5, reusableTypes.size());
        for (ComplexTypeModel complexTypeModel : reusableTypes) {
            String typeName = complexTypeModel.getName();
            if (typeName.equals("AddressType")) {
                assertEquals(true, complexTypeModel.isAbstract());
                assertEquals(2, complexTypeModel.getLabelMap().size());
                assertEquals("adresseType", complexTypeModel.getLabel(language));
            } else if (typeName.equals("CNAddressType")) {
                assertEquals(false, complexTypeModel.isAbstract());
                assertEquals(0, complexTypeModel.getLabelMap().size());
                assertEquals(typeName, complexTypeModel.getLabel(language));
            } else if (typeName.equals("EUAddressType")) {
                assertEquals(false, complexTypeModel.isAbstract());
                assertEquals(0, complexTypeModel.getLabelMap().size());
                assertEquals(typeName, complexTypeModel.getLabel(language));
            } else if (typeName.equals("USAddressType")) {
                assertEquals(false, complexTypeModel.isAbstract());
                assertEquals(2, complexTypeModel.getLabelMap().size());
                assertEquals("USAdresseType", complexTypeModel.getLabel(language));
            } else if (typeName.equals(StringUtils.EMPTY)) {
                assertEquals(false, complexTypeModel.isAbstract());
                assertEquals(0, complexTypeModel.getLabelMap().size());
                assertEquals(typeName, complexTypeModel.getLabel(language));
                assertEquals(0, complexTypeModel.getSubTypes().size());
            }

        }

    }

    public void testGetAllBusinessConcept() throws Exception {
        String datamodelName = "Product";
        List<String> entityList = new ArrayList<String>();
        entityList.add("Product");
        entityList.add("ProductFamily");
        entityList.add("Store");
        entityList.add("MyTest");
        String xsd = inputStream2String(this.getClass().getResourceAsStream("Product.xsd"));
        SchemaMockAgent schemaAgent = new SchemaMockAgent(xsd, new DataModelID(datamodelName));
        List<BusinessConcept> list = schemaAgent.getAllBusinessConcepts();
        assertEquals(4, list.size());
        for (BusinessConcept businessConcept : list) {
            assertTrue(entityList.contains(businessConcept.getName()));
        }
    }
}
