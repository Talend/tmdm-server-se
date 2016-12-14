// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.base.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit3.PowerMockSuite;
import org.talend.mdm.commmon.metadata.MetadataRepository;
import org.talend.mdm.commmon.util.core.EDBType;
import org.talend.mdm.commmon.util.core.MDMConfiguration;
import org.talend.mdm.commmon.util.datamodel.management.DataModelID;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.base.shared.util.CommonUtil;
import org.w3c.dom.Element;

import com.amalto.core.webservice.WSStringPredicate;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.core.webservice.WSWhereItem;
import com.amalto.core.webservice.WSWhereOperator;
import com.amalto.webapp.core.util.Util;

@PrepareForTest({ ForeignKeyHelper.class, org.talend.mdm.webapp.base.server.util.CommonUtil.class })
@SuppressWarnings("nls")
public class ForeignKeyHelperTest extends TestCase {

    private MetadataRepository repository;

    static {
        MDMConfiguration.createConfiguration("", true);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        repository = new MetadataRepository();
        PowerMockito.mockStatic(org.talend.mdm.webapp.base.server.util.CommonUtil.class);
        Mockito.when(org.talend.mdm.webapp.base.server.util.CommonUtil.getCurrentRepository()).thenReturn(repository);
    }

    // TODO Refactor FK handler, also this JUnit case
    public void testGetForeignKeyHolder() throws Exception {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("product.xsd");
        repository.load(resourceAsStream);

        TypeModel model = new SimpleTypeModel("family", DataTypeConstants.STRING); //$NON-NLS-1$
        model.setForeignkey("ProductFamily/Id"); //$NON-NLS-1$
        List<String> foreignKeyInfos = new ArrayList<String>();
        foreignKeyInfos.add("ProductFamily/Name"); //$NON-NLS-1$
        model.setForeignKeyInfo(foreignKeyInfos);
        model.setRetrieveFKinfos(true);
        model.setXpath("Product/Family"); //$NON-NLS-1$
        boolean ifFKFilter = false;
        String value = "Hats"; //$NON-NLS-1$
        String xml = null;
        String dataCluster = "Product";
        String currentXpath = "Product/Family";

        InputStream stream = getClass().getResourceAsStream("product.xsd");
        String xsd = inputStream2String(stream);

        ForeignKeyHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID("Product")));

        // 1. ForeignKeyInfo = ProductFamily/Name
        MDMConfiguration.getConfiguration().setProperty("xmldb.type", EDBType.QIZX.getName()); //$NON-NLS-1$
        String foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter,
                currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        model.setFilterValue(value);
        ForeignKeyHelper.ForeignKeyHolder result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        WSWhereItem whereItem = result.whereItem;
        assertNotNull(whereItem);
        assertNotNull(whereItem.getWhereOr());
        assertNotNull(whereItem.getWhereOr().getWhereItems());

        WSWhereCondition condition1 = whereItem.getWhereOr().getWhereItems()[0].getWhereAnd().getWhereItems()[0].getWhereAnd()
                .getWhereItems()[0].getWhereCondition();
        assertEquals("ProductFamily/Id", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition1.getOperator());
        assertEquals("Hats", condition1.getRightValueOrPath()); //$NON-NLS-1$

        WSWhereItem whereItem2 = whereItem.getWhereOr().getWhereItems()[1];
        WSWhereCondition condition2 = whereItem2.getWhereAnd().getWhereItems()[0].getWhereAnd().getWhereItems()[0]
                .getWhereCondition();
        assertEquals("ProductFamily/Name", condition2.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition2.getOperator());
        assertEquals("Hats", condition2.getRightValueOrPath()); //$NON-NLS-1$

        // 2. foreignKeyInfo = ProductFamily/Name,ProductFaimly/Id
        foreignKeyInfos.add("ProductFamily/Id"); //$NON-NLS-1$

        model.setForeignKeyInfo(foreignKeyInfos);
        result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);

        List<String> viewableXpaths = result.xpaths;
        assertTrue(viewableXpaths.size() > 0);
        assertEquals("ProductFamily/Name", viewableXpaths.get(0));
        assertEquals("ProductFamily/Id", viewableXpaths.get(1));
        assertEquals("ProductFamily/../../i", viewableXpaths.get(2));

        whereItem = result.whereItem;
        condition1 = whereItem.getWhereOr().getWhereItems()[0].getWhereAnd().getWhereItems()[0].getWhereAnd().getWhereItems()[0]
                .getWhereCondition();
        assertEquals("ProductFamily/Id", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition1.getOperator());
        assertEquals("Hats", condition1.getRightValueOrPath()); //$NON-NLS-1$

        whereItem2 = whereItem.getWhereOr().getWhereItems()[1];
        condition2 = whereItem2.getWhereAnd().getWhereItems()[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("ProductFamily/Name", condition2.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition2.getOperator());
        assertEquals("Hats", condition2.getRightValueOrPath()); //$NON-NLS-1$

        // 3. foreignKeyInfo is null
        model.getForeignKeyInfo().clear();
        result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        whereItem = result.whereItem;
        condition1 = whereItem.getWhereOr().getWhereItems()[0].getWhereAnd().getWhereItems()[0].getWhereAnd().getWhereItems()[0]
                .getWhereCondition();
        assertEquals("ProductFamily/../*", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition1.getOperator());
        assertEquals("Hats", condition1.getRightValueOrPath()); //$NON-NLS-1$

        // 4. foreignKey = ProductFamily, foreignKeyInfo = ProductFamily/Name
        String xpathForeignKey = "ProductFamily"; //$NON-NLS-1$
        String xpathInfoForeignKey = "ProductFamily/Name"; //$NON-NLS-1$
        String realXpathForeignKey = "ProductFamily/Id"; //$NON-NLS-1$
        whereItem = mock_SpecialForeignKeyWhereCondition(xpathForeignKey, xpathInfoForeignKey, realXpathForeignKey, value);

        assertNotNull(whereItem);
        assertNotNull(whereItem.getWhereOr());
        assertNotNull(whereItem.getWhereOr().getWhereItems());
        assertEquals(2, whereItem.getWhereOr().getWhereItems().length);

        condition1 = whereItem.getWhereOr().getWhereItems()[0].getWhereAnd().getWhereItems()[0].getWhereAnd().getWhereItems()[0]
                .getWhereCondition();
        assertEquals("ProductFamily/Name", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition1.getOperator());
        assertEquals("Hats", condition1.getRightValueOrPath()); //$NON-NLS-1$

        whereItem2 = whereItem.getWhereOr().getWhereItems()[1];
        condition2 = whereItem2.getWhereAnd().getWhereItems()[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("ProductFamily/Id", condition2.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition2.getOperator());
        assertEquals("Hats", condition2.getRightValueOrPath()); //$NON-NLS-1$

        // 5. ifFKFilter = true,fkFilter = Product/Family$$<$$.$$#,foreignKeyInfo is null,value is null,
        ifFKFilter = true;
        model.setForeignKeyFilter("ProductFamily/Id$$<$$.$$#");
        xml = "<Product><id>1</id><Name>Shirts</Name><Family>[3]</Family></Product>";
        foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter, currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        model.getForeignKeyInfo().clear();
        model.setFilterValue("");
        result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        whereItem = result.whereItem;
        condition1 = whereItem.getWhereCondition();
        assertEquals("ProductFamily/Id", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.LOWER_THAN, condition1.getOperator());
        assertEquals("3", condition1.getRightValueOrPath()); //$NON-NLS-1$

        model.setForeignKeyFilter("ProductFamily/Id$$<$$.$$And#ProductFamily/Name$$Contains$$../Name$$#");
        foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter, currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        model.setFilterValue("");
        result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        whereItem = result.whereItem;
        assertEquals(2, whereItem.getWhereAnd().getWhereItems().length);
        condition1 = whereItem.getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("ProductFamily/Id", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.LOWER_THAN, condition1.getOperator());
        assertEquals("3", condition1.getRightValueOrPath()); //$NON-NLS-1$
        assertEquals(WSStringPredicate.AND, condition1.getStringPredicate());
        condition2 = whereItem.getWhereAnd().getWhereItems()[1].getWhereCondition();
        assertEquals("ProductFamily/Name", condition2.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition2.getOperator());
        assertEquals("Shirts", condition2.getRightValueOrPath()); //$NON-NLS-1$

        // 6. ifFKFilter = true,fkFilter = 'ProductFamily/Name$$Contains$$Product/Name$$#',foreignKeyInfo is null
        ifFKFilter = true;
        model.setForeignKeyFilter("ProductFamily/Name$$Contains$$../Name$$#");
        xml = "<Product><id>1</id><Name>Shirts</Name><Family>[3]</Family></Product>";
        foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter, currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        model.getForeignKeyInfo().clear();
        model.setFilterValue("");
        result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        whereItem = result.whereItem;
        condition1 = whereItem.getWhereCondition();
        assertEquals("ProductFamily/Name", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition1.getOperator());
        assertEquals("Shirts", condition1.getRightValueOrPath()); //$NON-NLS-1$

        // 7. FK inheritance: ifFKFilter = true,fkFilter = 'ProductType/Type/@xsi:type$$=$$ProductTypeOne$$#'
        /*******************************************************************************************************
         * FK filter note<br>
         * Product/Type is a foreign key<br>
         * -----Foreign Key : ProductType/Id<br>
         * -----Foreign Key Filter : ProductType/Type/@xsi:type$$=$$ProductTypeOne$$#<br>
         * ProductType is an entity(exist three attributes: Id,Name and Type, Type is a comlexType(ComplexProductType))<br>
         * ComplexProductType is an abstract complex type<br>
         * ProductTypeOne inherited ComplexProductType<br>
         * ProductTypeTwo inherited ComplexProductType<br>
         ********************************************************************************************************/
        ifFKFilter = true;
        xml = "<Product><Id>123</Id><Name>zhang</Name><Type>type1</Type></Product>";
        dataCluster = "Product";
        currentXpath = "Product/Type";
        model.setForeignKeyFilter("ProductType/Type/@xsi:type$$=$$ProductTypeOne$$#");
        foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter, currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        model.setForeignkey("ProductFamily/Id");
        model.getForeignKeyInfo().clear();
        result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        whereItem = result.whereItem;
        condition1 = whereItem.getWhereCondition();
        assertEquals("ProductType/Type/@xsi:type", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.EQUALS, condition1.getOperator());
        assertEquals("ProductTypeOne", condition1.getRightValueOrPath()); //$NON-NLS-1$

        xml = "<Product><id>1</id><Name>Shirts</Name><Family>[3]</Family><Type>type1</Type></Product>";
        model.setForeignKeyFilter("ProductType/Type/@xsi:type$$=$$ProductTypeTwo$$And#ProductType/Name$$Contains$$/Product/Name$$#");
        foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter, currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        whereItem = result.whereItem;
        assertEquals(2, whereItem.getWhereAnd().getWhereItems().length);
        condition1 = whereItem.getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("ProductType/Type/@xsi:type", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.EQUALS, condition1.getOperator());
        assertEquals("ProductTypeTwo", condition1.getRightValueOrPath()); //$NON-NLS-1$
        assertEquals(WSStringPredicate.AND, condition1.getStringPredicate());
        condition2 = whereItem.getWhereAnd().getWhereItems()[1].getWhereCondition();
        assertEquals("ProductType/Name", condition2.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition2.getOperator());
        assertEquals("Shirts", condition2.getRightValueOrPath()); //$NON-NLS-1$

        // 8. ifFKFilter = true,fkFilter = 'CategoryOrga/TypeOrgaFK$$=$$OrganisationOperationnelle/OpOrgType$$#'
        ifFKFilter = true;
        dataCluster = "OrganisationOperationnelle";
        currentXpath = "OrganisationOperationnelle/OpOrgCategory";
        xml = "<OrganisationOperationnelle><OpOrgIdentifiantNoeud>1</OpOrgIdentifiantNoeud><OpOrgType>[1]</OpOrgType><OpOrgCategory>aa</OpOrgCategory></OrganisationOperationnelle>";
        model = new SimpleTypeModel("OpOrgCategory", DataTypeConstants.STRING); //$NON-NLS-1$
        foreignKeyInfos.clear();
        foreignKeyInfos.add("CategoryOrga/CategoryOrgaName");
        model.setForeignKeyInfo(foreignKeyInfos);
        model.setForeignkey("CategoryOrga/Id");
        model.setForeignKeyFilter("CategoryOrga/TypeOrgaFK$$=$$/OrganisationOperationnelle/OpOrgType$$");
        model.setFilterValue("");
        foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter, currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        whereItem = result.whereItem;
        condition1 = whereItem.getWhereCondition();
        assertEquals("CategoryOrga/TypeOrgaFK", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.EQUALS, condition1.getOperator());
        assertEquals("1", condition1.getRightValueOrPath()); //$NON-NLS-1$

        // 9. ifFKFilter = true,fkFilter = Product/Family$$=$$"[3]"$$#,foreignKeyInfo is null,value is null,
        ifFKFilter = true;
        model.setForeignKeyFilter("ProductFamily/Id$$=$$\"[3]\"$$#");
        xml = "<Product><id>1</id><Name>Shirts</Name><Family>[3]</Family></Product>";
        model.getForeignKeyInfo().clear();
        foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter, currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        model.setFilterValue("");
        result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        whereItem = result.whereItem;
        condition1 = whereItem.getWhereCondition();
        assertEquals("ProductFamily/Id", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.EQUALS, condition1.getOperator());
        assertEquals("3", condition1.getRightValueOrPath()); //$NON-NLS-1$

        // 10. ifFKFilter = true,fkFilter = $CFFP:Id="[3]" and Name=Product/Name,foreignKeyInfo is null,value is null,
        ifFKFilter = true;
        model.setForeignKeyFilter("$CFFP:Id=\"[3]\" and Name=Product/Name");
        xml = "<Product><id>1</id><Name>Shirts</Name><Family>[3]</Family></Product>";
        dataCluster = "Product";
        currentXpath = "Product/Family";
        foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter, currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        model.setForeignkey("ProductFamily/Id");
        model.getForeignKeyInfo().clear();
        result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        assertNull(result.whereItem);
        assertEquals("$CFFP:Id=\"[3]\" and Name=\"Shirts\"", result.fkFilter);

        // 11. foreignKeyinfo is null, DB is SQL db
        MDMConfiguration.getConfiguration().setProperty("xmlserver.class", "com.amalto.core.storage.DispatchWrapper");
        xml = null;
        model.setForeignKeyFilter(null);
        ifFKFilter = false;
        foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter, currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        model.setForeignkey("ProductFamily/Id");
        model.getForeignKeyInfo().clear();
        model.setFilterValue("1");
        result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        whereItem = result.whereItem;
        condition1 = whereItem.getWhereOr().getWhereItems()[0].getWhereAnd().getWhereItems()[0].getWhereAnd().getWhereItems()[0]
                .getWhereCondition();
        assertEquals("ProductFamily/../*", condition1.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, condition1.getOperator());
        assertEquals("1", condition1.getRightValueOrPath());

    }

    // TMDM-9417 Polymorphism Entity / Foreign Key / label issue
    public void testGetForeignKeyHolder_PolymorphismEntity() throws Exception {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("TMDM-9417_Polymorphism_FK.xsd");
        repository.load(resourceAsStream);

        TypeModel model = new SimpleTypeModel("supplier", DataTypeConstants.STRING); //$NON-NLS-1$
        model.setForeignkey("Issue1_Individual/Id"); //$NON-NLS-1$
        List<String> foreignKeyInfos = new ArrayList<String>();
        foreignKeyInfos.add("Issue1_Party/label"); //$NON-NLS-1$
        model.setForeignKeyInfo(foreignKeyInfos);
        model.setRetrieveFKinfos(true);
        model.setXpath("Issue1_Product/supplier"); //$NON-NLS-1$

        InputStream stream = getClass().getResourceAsStream("product.xsd");
        String xsd = inputStream2String(stream);

        ForeignKeyHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID("PolymorphismModel")));

        MDMConfiguration.getConfiguration().setProperty("xmldb.type", EDBType.QIZX.getName()); //$NON-NLS-1$
        model.setFilterValue("");
        ForeignKeyHelper.ForeignKeyHolder result = ForeignKeyHelper.getForeignKeyHolder(model, "");

        assertEquals("Issue1_Individual", result.conceptName);
        assertEquals("Issue1_Individual/label", result.xpaths.get(0));
        assertEquals("Issue1_Individual/../../i", result.xpaths.get(1));
    }

    public void testGetForeignKeyHolder_given_userInputAs_starWildcard_only_shouldReturn_null_for_WhereItem() throws Exception {

        TypeModel model = new SimpleTypeModel("family", DataTypeConstants.STRING); //$NON-NLS-1$
        model.setForeignkey("ProductFamily/Id"); //$NON-NLS-1$
        List<String> foreignKeyInfos = new ArrayList<String>();
        foreignKeyInfos.add("ProductFamily/Name"); //$NON-NLS-1$
        model.setForeignKeyInfo(foreignKeyInfos);
        model.setRetrieveFKinfos(true);
        model.setXpath("Product/Family"); //$NON-NLS-1$
        boolean ifFKFilter = false;
        String xml = null;
        String dataCluster = "Product";
        String currentXpath = "Product/Family";

        InputStream stream = getClass().getResourceAsStream("product.xsd");
        String xsd = inputStream2String(stream);

        ForeignKeyHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID("Product")));

        // ForeignKeyInfo = ProductFamily/Name, user user "*" as only filter value
        String foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter,
                currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        model.setFilterValue("'*'");
        ForeignKeyHelper.ForeignKeyHolder result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        WSWhereItem whereItem = result.whereItem;
        assertNull(whereItem);
    }

    public void testForeignKeyBean_From_ForeignKeyHolder_WhereItem() throws Exception {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("product.xsd");
        repository.load(resourceAsStream);

        TypeModel model = new SimpleTypeModel("family", DataTypeConstants.STRING); //$NON-NLS-1$
        model.setForeignkey("ProductFamily/Id"); //$NON-NLS-1$
        List<String> foreignKeyInfos = new ArrayList<String>();
        foreignKeyInfos.add("ProductFamily/Name"); //$NON-NLS-1$
        model.setForeignKeyInfo(foreignKeyInfos);
        model.setRetrieveFKinfos(true);
        model.setXpath("Product/Family"); //$NON-NLS-1$
        boolean ifFKFilter = false;
        String value = "Id"; //$NON-NLS-1$
        String xml = null;
        String dataCluster = "Product";
        String currentXpath = "Product/Family";

        InputStream stream = getClass().getResourceAsStream("product.xsd");
        String xsd = inputStream2String(stream);

        ForeignKeyHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID("Product")));

        // ForeignKeyInfo = ProductFamily/Name, user user "*" as only filter value
        String foreignKeyFilter = ForeignKeyHelper.getForeignKeyFilter(ifFKFilter,
                currentXpath.split("/")[0], xml, currentXpath, model); //$NON-NLS-1$
        model.setFilterValue("Id");
        ForeignKeyHelper.ForeignKeyHolder result = ForeignKeyHelper.getForeignKeyHolder(model, foreignKeyFilter);
        WSWhereItem whereItem = result.whereItem;
        assertNotNull(whereItem);
        assertNotNull(whereItem.getWhereOr());
        assertNotNull(whereItem.getWhereOr().getWhereItems());

        WSWhereCondition condition1 = whereItem.getWhereOr().getWhereItems()[0].getWhereAnd().getWhereItems()[0].getWhereAnd()
                .getWhereItems()[0].getWhereCondition();
        assertEquals("ProductFamily/Id", condition1.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition1.getOperator());
        assertEquals("Id", condition1.getRightValueOrPath()); //$NON-NLS-1$

        WSWhereCondition condition2 = whereItem.getWhereOr().getWhereItems()[1].getWhereAnd().getWhereItems()[0].getWhereAnd()
                .getWhereItems()[0].getWhereCondition();
        assertEquals("ProductFamily/Name", condition2.getLeftPath()); //$NON-NLS-1$
        assertEquals(WSWhereOperator.CONTAINS, condition2.getOperator());
        assertEquals("Id", condition2.getRightValueOrPath()); //$NON-NLS-1$
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

    public void testFormatForeignKeyValue() {

        String value = CommonUtil.wrapFkValue("123");
        assertEquals("[123]", value);

        value = CommonUtil.wrapFkValue("[123]");
        assertEquals("[123]", value);

        value = CommonUtil.unwrapFkValue("[123]");
        assertEquals("123", value);

        value = CommonUtil.unwrapFkValue("123");
        assertEquals("123", value);

        value = CommonUtil.unwrapFkValue("[123][456]");
        assertEquals("[123][456]", value);
    }

    /**
     * Mock org.talend.mdm.webapp.base.server.ForeignKeyHelper.getForeignKeyHolder(TypeModel, boolean, String) <li>when
     * the foreignKey = conceptName, using the method to test where condition.
     * 
     * @return
     * @throws Exception
     */
    private WSWhereItem mock_SpecialForeignKeyWhereCondition(String xpathForeignKey, String xpathInfoForeignKey,
            String realXpathForeignKey, String value) throws Exception {
        String initXpathForeignKey = Util.getForeignPathFromPath(xpathForeignKey);
        initXpathForeignKey = initXpathForeignKey.split("/")[0]; //$NON-NLS-1$
        String[] fkInfos = xpathInfoForeignKey.split(","); //$NON-NLS-1$
        String fkWhere = initXpathForeignKey + "/../* CONTAINS " + value; //$NON-NLS-1$
        if (xpathInfoForeignKey.trim().length() > 0) {
            StringBuffer ids = new StringBuffer();
            String realForeignKey = null;
            if (xpathForeignKey.indexOf("/") == -1) { //$NON-NLS-1$
                String[] fks = new String[] { realXpathForeignKey };
                if (fks != null && fks.length > 0) {
                    realForeignKey = fks[0];
                    for (int i = 0; i < fks.length; i++) {
                        ids.append(fks[i] + " CONTAINS " + value); //$NON-NLS-1$
                        if (i != fks.length - 1) {
                            ids.append(" OR "); //$NON-NLS-1$
                        }
                    }
                }
            }
            StringBuffer sb = new StringBuffer();
            for (String fkInfo : fkInfos) {
                sb.append((fkInfo.startsWith(".") ? Util.convertAbsolutePath((realForeignKey != null && realForeignKey.trim() //$NON-NLS-1$
                        .length() > 0) ? realForeignKey : xpathForeignKey, fkInfo) : fkInfo) + " CONTAINS " + value); //$NON-NLS-1$
                sb.append(" OR "); //$NON-NLS-1$
            }
            if (realForeignKey != null) {
                sb.append(ids.toString());
            } else {
                sb.append(xpathForeignKey + " CONTAINS " + value); //$NON-NLS-1$
            }
            fkWhere = sb.toString();
        }

        return Util.buildWhereItems(fkWhere);
    }

    public void testConvertFKInfo2DisplayInfo() {
        ForeignKeyBean bean1 = new ForeignKeyBean();
        bean1.set("Name", "Apple Store");
        bean1.set("Id", "1");
        bean1.set("Code", "10001");
        bean1.set("i", "1");
        bean1.set("Description", "[EN:Talend MDM][FR:Talend DQ]");
        bean1.getForeignKeyInfo().put("Store/Id", "1");
        bean1.getForeignKeyInfo().put("Store/Code", "10001");
        bean1.getForeignKeyInfo().put("Store/Name", "Apple Store");
        bean1.getForeignKeyInfo().put("Stroe/Description",
                MultilanguageMessageParser.getValueByLanguage((String) bean1.get("Description"), "en"));

        SimpleTypeModel model = new SimpleTypeModel();
        List<String> fkInfoList = new ArrayList<String>();
        fkInfoList.add("Store/Name");
        fkInfoList.add("Store/Code");
        fkInfoList.add("Store/Id");
        fkInfoList.add("Stroe/Description");
        model.setForeignKeyInfo(fkInfoList);

        ForeignKeyHelper.convertFKInfo2DisplayInfo(bean1, model);

        assertNotNull(bean1.getDisplayInfo());
        assertEquals("Apple Store-10001-1-Talend MDM", bean1.getDisplayInfo());

        ForeignKeyBean bean2 = new ForeignKeyBean();
        bean2.set("Name", "Google Market");
        bean2.set("Id", "2");
        bean2.set("Code", "10002");
        bean2.set("i", "2");
        bean2.getForeignKeyInfo().put("Store/Id", "2");
        bean2.getForeignKeyInfo().put("Store/Code", "10002");
        bean2.getForeignKeyInfo().put("Store/Name", "Google Market");

        fkInfoList.clear();
        model.setForeignKeyInfo(fkInfoList);

        ForeignKeyHelper.convertFKInfo2DisplayInfo(bean2, model);
        assertNull(bean2.getDisplayInfo());

        ForeignKeyBean bean3 = new ForeignKeyBean();
        bean3.set("Name", "Google Market3");
        bean3.set("Id", "3");
        bean3.set("Code", "10003");
        bean3.set("i", "3");
        bean3.getForeignKeyInfo().put("Store/Id", "3");
        bean3.getForeignKeyInfo().put("Store/Code", "10003");
        bean3.getForeignKeyInfo().put("Store/Name", "Google Market3");

        fkInfoList.clear();
        model.setForeignKeyInfo(fkInfoList);
        model.setForeignKeyInfoFormat(getName());
        model.setForeignKeyInfoFormat("\"Id: '\" + Store/Id +\"' Name: '\"+ Store/Name + \"'\"");

        ForeignKeyHelper.convertFKInfo2DisplayInfo(bean3, model);
        assertEquals(bean3.getDisplayInfo(), "Id: '3' Name: 'Google Market3'");
    }

    public void testIsPolymorphismTypeFK() throws Exception {
        InputStream stream = getClass().getResourceAsStream("metadata.xsd");
        String xsd = inputStream2String(stream);

        SchemaMockAgent schemaMockAgent = new SchemaMockAgent(xsd, new DataModelID("Territory"));
        assertFalse(schemaMockAgent.isPolymorphismTypeFK("Country"));

        stream = getClass().getResourceAsStream("person.xsd");
        xsd = inputStream2String(stream);

        schemaMockAgent = new SchemaMockAgent(xsd, new DataModelID("ProductEntity"));
        assertTrue(schemaMockAgent.isPolymorphismTypeFK("Party"));
        assertTrue(schemaMockAgent.isPolymorphismTypeFK("Individual"));
        assertTrue(schemaMockAgent.isPolymorphismTypeFK("Company"));
        assertFalse(schemaMockAgent.isPolymorphismTypeFK("Family"));

    }

    public void testInitFKBean() throws Exception {
        String[] results = new String[] {
                "<result>\n\t<C_Name>cname1</C_Name>\n\t<C_Title>ctitle1</C_Title>\n\t<C_Sub_Name>csubname1</C_Sub_Name>\n\t<C_Sub_Title>csubtitle1</C_Sub_Title>\n\t<C_Sub_Sub_Name>csubsubname1</C_Sub_Sub_Name>\n\t<C_Sub_Sub_Title>csubsubtitle1</C_Sub_Sub_Title>\n\t<i>c1</i>\n</result>",
                "<result>\n\t<C_Name>cname2</C_Name>\n\t<C_Title>ctitle2</C_Title>\n\t<C_Sub_Name>csubname2</C_Sub_Name>\n\t<C_Sub_Title>csubtitle2</C_Sub_Title>\n\t<C_Sub_Sub_Name>csubsubname2</C_Sub_Sub_Name>\n\t<C_Sub_Sub_Title>csubsubtitle2</C_Sub_Sub_Title>\n\t<i>c2</i>\n</result>" };
        String[] fKInfoExpected = new String[] { "cname1-ctitle1-csubname1-csubtitle1-csubsubname1-csubsubtitle1",
                "cname2-ctitle2-csubname2-csubtitle2-csubsubname2-csubsubtitle2" };

        String dataClusterPK = "TestFK";
        EntityModel entityModel = new EntityModel();
        entityModel.setConceptName("C");
        entityModel.setKeys(new String[] { "C/C_Id" });
        String fk = "C";
        SimpleTypeModel model = new SimpleTypeModel();
        List<String> foreignKeyInfos = Arrays.asList("C/C_Name", "C/C_Title", "C/C_SubInfo/C_Sub_Name",
                "C/C_SubInfo/C_Sub_Title", "C/C_SubInfo/C_SubSubInfo/C_Sub_Sub_Name", "C/C_SubInfo/C_SubSubInfo/C_Sub_Sub_Title");
        model.setForeignKeyInfo(foreignKeyInfos);
        Map<String, String> xpathTypeMap = new HashMap<String, String>();
        String language = "zh";
        ForeignKeyBean[] fkBeans = new ForeignKeyBean[] { new ForeignKeyBean(), new ForeignKeyBean() };
        fkBeans[0].setId("c1");
        fkBeans[1].setId("c2");

        Element[] resultAsDOM = new Element[2];
        for (int i = 0; i < fkBeans.length; i++) {
            resultAsDOM[i] = Util.parse(results[i]).getDocumentElement();

            ForeignKeyHelper.initFKBean(dataClusterPK, entityModel, resultAsDOM[i], fkBeans[i], fk, foreignKeyInfos,
                    xpathTypeMap, language);
            ForeignKeyHelper.convertFKInfo2DisplayInfo(fkBeans[i], model);
        }

        for (int i = 0; i < fkBeans.length; i++) {
            assertEquals(fkBeans[i].getId() + "->Expected displayInfo is not equal to acutal displayInfo", fKInfoExpected[i],
                    fkBeans[i].getDisplayInfo());
        }
    }

    public void testInitFKBean_for_missing_values_In_FKInfos() throws Exception {
        String[] results = new String[] { "<result>\n\t<Name>bname0</Name>\n\t<CF>cname1</CF>\n\t<i>b0</i>\n</result>",
                "<result>\n\t<CF>cname1</CF>\n\t<i>b1</i>\n</result>", "<result>\n\t<Name>bname2</Name>\n\t<i>b2</i>\n</result>",
                "<result>\n\t<i>b2</i>\n</result>" };
        String[] fKInfoExpected = new String[] { "bname0-cname1", "cname1", "bname2", "" };

        String dataClusterPK = "TestFK";
        EntityModel entityModel = new EntityModel();
        entityModel.setConceptName("B");
        entityModel.setKeys(new String[] { "B/subelement" });
        String fk = "B";
        SimpleTypeModel model = new SimpleTypeModel();
        List<String> foreignKeyInfos = Arrays.asList("B/Name", "B/CF");
        model.setForeignKeyInfo(foreignKeyInfos);
        Map<String, String> xpathTypeMap = new HashMap<String, String>();
        String language = "zh";
        ForeignKeyBean[] fkBeans = new ForeignKeyBean[] { new ForeignKeyBean(), new ForeignKeyBean(), new ForeignKeyBean(),
                new ForeignKeyBean() };
        fkBeans[0].setId("b0");
        fkBeans[1].setId("b1");
        fkBeans[2].setId("b2");
        fkBeans[3].setId("b3");

        Element[] resultAsDOM = new Element[4];
        for (int i = 0; i < fkBeans.length; i++) {
            resultAsDOM[i] = Util.parse(results[i]).getDocumentElement();

            ForeignKeyHelper.initFKBean(dataClusterPK, entityModel, resultAsDOM[i], fkBeans[i], fk, foreignKeyInfos,
                    xpathTypeMap, language);
            ForeignKeyHelper.convertFKInfo2DisplayInfo(fkBeans[i], model);
        }

        for (int i = 0; i < fkBeans.length; i++) {
            assertEquals(fkBeans[i].getId() + "-> Expected displayInfo is not equal to actual displayInfo", fKInfoExpected[i],
                    fkBeans[i].getDisplayInfo());
        }
    }

    public void testGetFKQueryCondition() throws Exception {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("TMDM-8966.xsd");
        repository.load(resourceAsStream);
        String concept = "TMDM-8966FK";
        String xpathForeignKey = "TMDM-8966FK/Id";
        String xpathInfoForeignKey = "TMDM-8966FK/city,TMDM-8966FK/state,TMDM-8966FK/zipCode";
        String keyValue = "testValue";
        WSWhereItem fkQueryCondition = ForeignKeyHelper.getFKQueryCondition(concept, xpathForeignKey, xpathInfoForeignKey,
                keyValue);
        WSWhereItem[] whereItems = fkQueryCondition.getWhereOr().getWhereItems();
        assertEquals(3, whereItems.length);
        WSWhereItem[] whereItems1 = whereItems[0].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition1 = whereItems1[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/Id", whereCondition1.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition1.getOperator());
        assertEquals("testValue", whereCondition1.getRightValueOrPath());

        WSWhereItem[] whereItems2 = whereItems[1].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition2 = whereItems2[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/city", whereCondition2.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition2.getOperator());
        assertEquals("testValue", whereCondition2.getRightValueOrPath());

        WSWhereItem[] whereItems3 = whereItems[2].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition3 = whereItems3[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/state", whereCondition3.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition3.getOperator());
        assertEquals("testValue", whereCondition3.getRightValueOrPath());

        keyValue = "123";
        fkQueryCondition = ForeignKeyHelper.getFKQueryCondition(concept, xpathForeignKey, xpathInfoForeignKey, keyValue);
        whereItems = fkQueryCondition.getWhereOr().getWhereItems();
        assertEquals(4, whereItems.length);
        whereItems1 = whereItems[0].getWhereAnd().getWhereItems();
        whereCondition1 = whereItems1[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/Id", whereCondition1.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition1.getOperator());
        assertEquals("123", whereCondition1.getRightValueOrPath());

        whereItems2 = whereItems[1].getWhereAnd().getWhereItems();
        whereCondition2 = whereItems2[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/city", whereCondition2.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition2.getOperator());
        assertEquals("123", whereCondition2.getRightValueOrPath());

        whereItems3 = whereItems[2].getWhereAnd().getWhereItems();
        whereCondition3 = whereItems3[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/state", whereCondition3.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition3.getOperator());
        assertEquals("123", whereCondition3.getRightValueOrPath());

        WSWhereItem[] whereItems4 = whereItems[3].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition4 = whereItems4[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/zipCode", whereCondition4.getLeftPath());
        assertEquals(WSWhereOperator.EQUALS, whereCondition4.getOperator());
        assertEquals("123", whereCondition4.getRightValueOrPath());
    }

    public void testGetFKQueryConditionWithIntegerFK() throws Exception {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("TMDM-8966_Integer_FK.xsd");
        repository.load(resourceAsStream);
        String concept = "TMDM-8966FK";
        String xpathForeignKey = "TMDM-8966FK/Id";
        String xpathInfoForeignKey = "TMDM-8966FK/city,TMDM-8966FK/state,TMDM-8966FK/zipCode";
        String keyValue = "testValue";

        WSWhereItem fkQueryCondition = ForeignKeyHelper.getFKQueryCondition(concept, xpathForeignKey, xpathInfoForeignKey,
                keyValue);
        WSWhereItem[] whereItems = fkQueryCondition.getWhereOr().getWhereItems();
        assertEquals(2, whereItems.length);
        WSWhereItem[] whereItems1 = whereItems[0].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition1 = whereItems1[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/city", whereCondition1.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition1.getOperator());
        assertEquals("testValue", whereCondition1.getRightValueOrPath());

        WSWhereItem[] whereItems2 = whereItems[1].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition2 = whereItems2[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/state", whereCondition2.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition2.getOperator());
        assertEquals("testValue", whereCondition2.getRightValueOrPath());

        keyValue = "22";
        fkQueryCondition = ForeignKeyHelper.getFKQueryCondition(concept, xpathForeignKey, xpathInfoForeignKey, keyValue);
        whereItems = fkQueryCondition.getWhereOr().getWhereItems();
        assertEquals(4, whereItems.length);
        whereItems1 = whereItems[0].getWhereAnd().getWhereItems();
        whereCondition1 = whereItems1[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/Id", whereCondition1.getLeftPath());
        assertEquals(WSWhereOperator.EQUALS, whereCondition1.getOperator());
        assertEquals("22", whereCondition1.getRightValueOrPath());

        whereItems2 = whereItems[1].getWhereAnd().getWhereItems();
        whereCondition2 = whereItems2[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/city", whereCondition2.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition2.getOperator());
        assertEquals("22", whereCondition2.getRightValueOrPath());

        WSWhereItem[] whereItems3 = whereItems[2].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition3 = whereItems3[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/state", whereCondition3.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition3.getOperator());
        assertEquals("22", whereCondition3.getRightValueOrPath());

        WSWhereItem[] whereItems4 = whereItems[3].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition4 = whereItems4[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("TMDM-8966FK/zipCode", whereCondition4.getLeftPath());
        assertEquals(WSWhereOperator.EQUALS, whereCondition4.getOperator());
        assertEquals("22", whereCondition4.getRightValueOrPath());
    }

    public void testGetFKQueryConditionWithInheritanceSimpleTypeFieldMetadata() throws Exception {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("InheritanceSimpleTypeField.xsd");
        repository.load(resourceAsStream);
        String concept = "InheritanceSimpleTypeField";
        String xpathForeignKey = "InheritanceSimpleTypeField/Id";
        String xpathInfoForeignKey = "InheritanceSimpleTypeField/Name,InheritanceSimpleTypeField/Age,InheritanceSimpleTypeField/Description";
        String keyValue = "testValue";
        WSWhereItem fkQueryCondition = ForeignKeyHelper.getFKQueryCondition(concept, xpathForeignKey, xpathInfoForeignKey,
                keyValue);
        WSWhereItem[] whereItems = fkQueryCondition.getWhereOr().getWhereItems();
        assertEquals(3, whereItems.length);
        WSWhereItem[] whereItems1 = whereItems[0].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition1 = whereItems1[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("InheritanceSimpleTypeField/Id", whereCondition1.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition1.getOperator());
        assertEquals("testValue", whereCondition1.getRightValueOrPath());
        WSWhereItem[] whereItems2 = whereItems[1].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition2 = whereItems2[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("InheritanceSimpleTypeField/Name", whereCondition2.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition2.getOperator());
        assertEquals("testValue", whereCondition2.getRightValueOrPath());
        WSWhereItem[] whereItems3 = whereItems[2].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition3 = whereItems3[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("InheritanceSimpleTypeField/Description", whereCondition3.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition3.getOperator());
        assertEquals("testValue", whereCondition3.getRightValueOrPath());

        keyValue = "123";
        fkQueryCondition = ForeignKeyHelper.getFKQueryCondition(concept, xpathForeignKey, xpathInfoForeignKey, keyValue);
        whereItems = fkQueryCondition.getWhereOr().getWhereItems();
        assertEquals(4, whereItems.length);
        whereItems1 = whereItems[0].getWhereAnd().getWhereItems();
        whereCondition1 = whereItems1[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("InheritanceSimpleTypeField/Id", whereCondition1.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition1.getOperator());
        assertEquals("123", whereCondition1.getRightValueOrPath());
        whereItems2 = whereItems[1].getWhereAnd().getWhereItems();
        whereCondition2 = whereItems2[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("InheritanceSimpleTypeField/Name", whereCondition2.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition2.getOperator());
        assertEquals("123", whereCondition2.getRightValueOrPath());
        whereItems3 = whereItems[2].getWhereAnd().getWhereItems();
        whereCondition3 = whereItems3[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("InheritanceSimpleTypeField/Age", whereCondition3.getLeftPath());
        assertEquals(WSWhereOperator.EQUALS, whereCondition3.getOperator());
        assertEquals("123", whereCondition3.getRightValueOrPath());
        WSWhereItem[] whereItems4 = whereItems[3].getWhereAnd().getWhereItems();
        WSWhereCondition whereCondition4 = whereItems4[0].getWhereAnd().getWhereItems()[0].getWhereCondition();
        assertEquals("InheritanceSimpleTypeField/Description", whereCondition4.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, whereCondition4.getOperator());
        assertEquals("123", whereCondition4.getRightValueOrPath());
    }

    // TMDM-9353 Search on FK info field doesn't work in FK picker dialog when a FK info is based on a field of a
    // complex entity
    public void testGetForeignKeyHolder_FKInfoFromComplexType() throws Exception {
        InputStream resourceAsStream = this.getClass().getResourceAsStream("TMDM-9353.xsd");
        repository.load(resourceAsStream);

        TypeModel model = new SimpleTypeModel("FK_FONCTION_REF", DataTypeConstants.STRING); //$NON-NLS-1$
        model.setForeignkey("REF_FONCTION/ID"); //$NON-NLS-1$
        List<String> foreignKeyInfos = new ArrayList<String>();
        foreignKeyInfos.add("REF_FONCTION/REFERENCE/LIBELLE"); //$NON-NLS-1$
        model.setForeignKeyInfo(foreignKeyInfos);
        model.setRetrieveFKinfos(true);
        model.setXpath("CURSUS/FK_FONCTION_REF"); //$NON-NLS-1$
        String value = "Hats"; //$NON-NLS-1$
        InputStream stream = getClass().getResourceAsStream("TMDM-9353.xsd");
        String xsd = inputStream2String(stream);
        ForeignKeyHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID("CURSUS")));

        // 1. ForeignKeyInfo = ProductFamily/Name
        MDMConfiguration.getConfiguration().setProperty("xmldb.type", EDBType.QIZX.getName()); //$NON-NLS-1$
        model.setFilterValue(value);
        ForeignKeyHelper.ForeignKeyHolder result = ForeignKeyHelper.getForeignKeyHolder(model, "");
        WSWhereItem whereItem = result.whereItem;
        assertNotNull(whereItem);
        WSWhereCondition condition = whereItem.getWhereOr().getWhereItems()[0].getWhereAnd().getWhereItems()[0].getWhereAnd()
                .getWhereItems()[0].getWhereCondition();
        assertEquals("REF_FONCTION/REFERENCE/LIBELLE", condition.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, condition.getOperator());
        assertEquals("Hats", condition.getRightValueOrPath());
    }

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + ForeignKeyHelperTest.class.getSimpleName(), ForeignKeyHelperTest.class);
    }
}
