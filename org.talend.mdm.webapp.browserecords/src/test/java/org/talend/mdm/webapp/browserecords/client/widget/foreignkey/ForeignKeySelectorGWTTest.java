/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.foreignkey;

import java.util.ArrayList;

import org.talend.mdm.webapp.base.client.ServiceEnhancer;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsService;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.ClientResourceData;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtilTestData;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeySelector;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

public class ForeignKeySelectorGWTTest extends GWTTestCase {

    private ForeignKeySelectorForTest foreignKeySelectorForTest;

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.CURRENT_ENTITY_MODEL, getCurrentEntityModel());
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
        ServiceDefTarget browseRecordService = GWT.create(BrowseRecordsService.class);
        ServiceEnhancer.customizeService(browseRecordService);
        Registry.register(BrowseRecords.BROWSERECORDS_SERVICE, browseRecordService);

        TypeModel typeModel = new SimpleTypeModel();
        typeModel.setForeignkey("TypeValeur_Lkp/IdTypeValeur"); //$NON-NLS-1$
        typeModel.setTypePath("Caracteristique/Format_Caracteristique:Caracteristique_Liste/IdTypeValeur"); //$NON-NLS-1$
        ItemsDetailPanel itemsDetailPanel = ItemsDetailPanel.newInstance();
        itemsDetailPanel.setStaging(false);
        foreignKeySelectorForTest = new ForeignKeySelectorForTest(typeModel, itemsDetailPanel, null);
    }

    public void testParseForeignKeyFilter() {
        ItemsDetailPanel itemsDetailPanel = ItemsDetailPanel.newInstance();
        itemsDetailPanel.setStaging(false);
        ItemNodeModel product = new ItemNodeModel();
        product.setTypeName("Product"); //$NON-NLS-1$
        product.setTypePath("Product"); //$NON-NLS-1$
        product.setObjectValue("Product"); //$NON-NLS-1$
        ItemNodeModel id = new ItemNodeModel();
        id.setTypeName("Id"); //$NON-NLS-1$
        id.setTypePath("Product/Id"); //$NON-NLS-1$
        id.setObjectValue("1"); //$NON-NLS-1$
        id.setParent(product);
        product.add(id);
        ItemNodeModel name = new ItemNodeModel();
        name.setTypeName("Name"); //$NON-NLS-1$
        name.setTypePath("Product/Name"); //$NON-NLS-1$
        name.setObjectValue("talend"); //$NON-NLS-1$
        name.setParent(product);
        product.add(name);
        ItemNodeModel price = new ItemNodeModel();
        price.setTypeName("Price"); //$NON-NLS-1$
        price.setTypePath("Product/Price"); //$NON-NLS-1$
        price.setObjectValue("9999"); //$NON-NLS-1$
        price.setParent(product);
        product.add(price);
        ForeignKeyBean foreignKeyBean = new ForeignKeyBean();
        foreignKeyBean.setId("[123]"); //$NON-NLS-1$
        foreignKeyBean.setDisplayInfo("ForeignKey DispalyInfo"); //$NON-NLS-1$
        ItemNodeModel family = new ItemNodeModel();
        family.setTypeName("Family"); //$NON-NLS-1$
        family.setTypePath("Product/Family"); //$NON-NLS-1$
        family.setObjectValue(foreignKeyBean);
        family.setParent(product);
        product.add(family);
        SimpleTypeModel subelementType = new SimpleTypeModel();
        subelementType.setForeignkey("ProductFamily/Id"); //$NON-NLS-1$
        subelementType.setForeignKeyInfo(new ArrayList<String>());
        subelementType.setXpath("Product/Family"); //$NON-NLS-1$
        subelementType.setForeignKeyFilter("ProductFamily/Name$$=$$Product/Name$$#"); //$NON-NLS-1$
        ForeignKeySelector foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, family);
        assertEquals("ProductFamily/Name$$=$$talend$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType.setForeignKeyFilter("ProductFamily/Name$$=$$../Name$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, family);
        assertEquals("ProductFamily/Name$$=$$../Name$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType.setForeignKeyFilter("ProductFamily/Name$$=$$ProductFamily/Id$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, family);
        assertEquals("ProductFamily/Name$$=$$ProductFamily/Id$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType.setForeignKeyFilter("ProductFamily/Name$$=$$\"talend\"$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, family);
        assertEquals("ProductFamily/Name$$=$$talend$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType.setForeignKeyFilter("ProductFamily/Name$$=$$\'talend\'$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, family);
        assertEquals("ProductFamily/Name$$=$$talend$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType.setForeignKeyFilter("ProductFamily/Name$$=$$\"1\"$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, family);
        assertEquals("ProductFamily/Name$$=$$1$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType.setForeignKeyFilter("ProductFamily/Name$$=$$\'1\'$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, family);
        assertEquals("ProductFamily/Name$$=$$1$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType.setForeignKeyFilter("ProductFamily/Name$$=$$1$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, family);
        assertEquals("ProductFamily/Name$$=$$1$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType.setForeignKeyFilter("ProductFamily/ChangeStatus$$Is Empty Or Null$$$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, family);
        assertEquals("ProductFamily/ChangeStatus$$Is Empty Or Null$$$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType.setForeignKeyFilter("ProductFamily/ChangeStatus$$Is Empty Or Null$$123456$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, family);
        assertEquals("ProductFamily/ChangeStatus$$Is Empty Or Null$$123456$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType
                .setForeignKeyFilter("ProductFamily/ChangeStatus$$Is Empty Or Null$$ProductFamily/Name$$=$$Product/Name$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, family);
        assertEquals(
                "ProductFamily/ChangeStatus$$Is Empty Or Null$$ProductFamily/Name$$=$$Product/Name$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        // test Person datamodel
        ItemNodeModel person = new ItemNodeModel();
        person.setTypeName("Person"); //$NON-NLS-1$
        person.setTypePath("Person"); //$NON-NLS-1$
        person.setObjectValue("Person"); //$NON-NLS-1$
        id = new ItemNodeModel();
        id.setTypeName("Id"); //$NON-NLS-1$
        id.setTypePath("Person/Id"); //$NON-NLS-1$
        id.setObjectValue("1"); //$NON-NLS-1$
        id.setParent(person);
        person.add(id);
        ItemNodeModel pname = new ItemNodeModel();
        pname.setTypeName("pname"); //$NON-NLS-1$
        pname.setTypePath("Person/pname"); //$NON-NLS-1$
        pname.setObjectValue("talend"); //$NON-NLS-1$
        pname.setParent(person);
        person.add(pname);
        ItemNodeModel addrs = new ItemNodeModel();
        addrs.setTypeName("addrs"); //$NON-NLS-1$
        addrs.setTypePath("Person/addrs"); //$NON-NLS-1$
        addrs.setParent(person);
        person.add(addrs);

        foreignKeyBean = new ForeignKeyBean();
        foreignKeyBean.setId("[123]"); //$NON-NLS-1$
        foreignKeyBean.setDisplayInfo("ForeignKey DispalyInfo"); //$NON-NLS-1$
        ItemNodeModel add_cod = new ItemNodeModel();
        add_cod.setTypeName("add_code"); //$NON-NLS-1$
        add_cod.setTypePath("Person/addrs/add_code"); //$NON-NLS-1$
        add_cod.setObjectValue(foreignKeyBean);
        add_cod.setParent(addrs);
        addrs.add(add_cod);
        name = new ItemNodeModel();
        name.setTypeName("name"); //$NON-NLS-1$
        name.setTypePath("Person/addrs/name"); //$NON-NLS-1$
        name.setObjectValue("name"); //$NON-NLS-1$
        name.setParent(addrs);
        addrs.add(name);
        ItemNodeModel abc = new ItemNodeModel();
        abc.setTypeName("abc"); //$NON-NLS-1$
        abc.setTypePath("Person/addrs/abc"); //$NON-NLS-1$
        abc.setObjectValue("abc"); //$NON-NLS-1$
        abc.setParent(addrs);
        addrs.add(abc);
        subelementType = new SimpleTypeModel();
        subelementType.setForeignkey("Person/addrs/add_code"); //$NON-NLS-1$
        subelementType.setForeignKeyInfo(new ArrayList<String>());
        subelementType.setXpath("Person/addrs/abc"); //$NON-NLS-1$
        subelementType.setForeignKeyFilter("Addr/AddrId$$=$$Person/addrs/add_code$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, abc);
        assertEquals("Addr/AddrId$$=$$123$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType.setForeignKeyFilter("Addr/AddrId$$=$$\"123\"$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, abc);
        assertEquals("Addr/AddrId$$=$$123$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$

        subelementType.setForeignKeyFilter("Addr/AddrId$$=$$\'123\'$$#"); //$NON-NLS-1$
        foreignKeySelector = new ForeignKeySelector(subelementType, itemsDetailPanel, abc);
        assertEquals("Addr/AddrId$$=$$123$$#", foreignKeySelector.parseForeignKeyFilter()); //$NON-NLS-1$
    }

    public void testFindTarget() {
        String targetPath = "Caracteristique/Format_Caracteristique/IdTypeValeur"; //$NON-NLS-1$
        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setLabel("Format_Caracteristique"); //$NON-NLS-1$
        itemNodeModel.setRealType("Caracteristique"); //$NON-NLS-1$
        itemNodeModel.setTypePath("Caracteristique/Format_Caracteristique"); //$NON-NLS-1$

        ItemNodeModel itemNodeModel1 = new ItemNodeModel();
        itemNodeModel1.setLabel("IdTypeValeur"); //$NON-NLS-1$
        itemNodeModel1.setTypePath("Caracteristique/Format_Caracteristique:Caracteristique_Liste/IdTypeValeur"); //$NON-NLS-1$
        ItemNodeModel itemNodeModel2 = new ItemNodeModel();
        itemNodeModel2.setLabel("Multiple"); //$NON-NLS-1$
        itemNodeModel2.setTypePath("Caracteristique/Format_Caracteristique:Caracteristique_Liste/Multiple"); //$NON-NLS-1$
        ItemNodeModel itemNodeModel3 = new ItemNodeModel();
        itemNodeModel3.setLabel("ValeursCaracteristique"); //$NON-NLS-1$
        itemNodeModel3.setTypePath("Caracteristique/Format_Caracteristique:Caracteristique_Liste/ValeursCaracteristique"); //$NON-NLS-1$
        itemNodeModel.getChildren().add(itemNodeModel1);
        itemNodeModel.getChildren().add(itemNodeModel2);
        itemNodeModel.getChildren().add(itemNodeModel3);

        ItemNodeModel targetNodeModel = foreignKeySelectorForTest.findTarget(targetPath, itemNodeModel);
        assertNotNull(targetNodeModel);
        assertEquals("IdTypeValeur", targetNodeModel.getLabel()); //$NON-NLS-1$
        assertEquals("Caracteristique/Format_Caracteristique:Caracteristique_Liste/IdTypeValeur", targetNodeModel.getTypePath()); //$NON-NLS-1$

        targetPath = "Caracteristique/Format_Caracteristique[@xsi:type=\"Caracteristique_Liste\"]/IdTypeValeur"; //$NON-NLS-1$
        targetNodeModel = foreignKeySelectorForTest.findTarget(targetPath, itemNodeModel);
        assertNotNull(targetNodeModel);
        assertEquals("IdTypeValeur", targetNodeModel.getLabel()); //$NON-NLS-1$
        assertEquals("Caracteristique/Format_Caracteristique:Caracteristique_Liste/IdTypeValeur", targetNodeModel.getTypePath()); //$NON-NLS-1$
    }

    public void testTransformPath() {
        String[] pathArray = { "Caracteristique", "Format_Caracteristique", "IdTypeValeur" }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("Caracteristique/Format_Caracteristique/IdTypeValeur", foreignKeySelectorForTest.transformPath(pathArray)); //$NON-NLS-1$
    }

    private EntityModel getCurrentEntityModel() {
        EntityModel productEntityModel = CommonUtilTestData.getEntityModel(ClientResourceData.getModelProduct());
        return productEntityModel;
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }

    public class ForeignKeySelectorForTest extends ForeignKeySelector {

        public ForeignKeySelectorForTest(TypeModel dataType, ItemsDetailPanel itemsDetailPanel, ItemNodeModel itemNode) {
            super(dataType, itemsDetailPanel, itemNode);
        }

        @Override
        public ItemNodeModel findTarget(String targetPath, ItemNodeModel node) {
            return super.findTarget(targetPath, node);
        }

        @Override
        public String transformPath(String[] pathArray) {
            return super.transformPath(pathArray);
        }
    }
}
