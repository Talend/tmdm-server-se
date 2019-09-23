/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 *  %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import com.google.gwt.junit.client.GWTTestCase;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;

public class ForeignKeyUtilTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords";
    }

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
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

        ItemNodeModel targetNodeModel = ForeignKeyUtil.findTarget(targetPath, itemNodeModel);
        assertNotNull(targetNodeModel);
        assertEquals("IdTypeValeur", targetNodeModel.getLabel()); //$NON-NLS-1$
        assertEquals("Caracteristique/Format_Caracteristique:Caracteristique_Liste/IdTypeValeur", targetNodeModel.getTypePath()); //$NON-NLS-1$

        targetPath = "Caracteristique/Format_Caracteristique[@xsi:type=\"Caracteristique_Liste\"]/IdTypeValeur"; //$NON-NLS-1$
        targetNodeModel = ForeignKeyUtil.findTarget(targetPath, itemNodeModel);
        assertNotNull(targetNodeModel);
        assertEquals("IdTypeValeur", targetNodeModel.getLabel()); //$NON-NLS-1$
        assertEquals("Caracteristique/Format_Caracteristique:Caracteristique_Liste/IdTypeValeur", targetNodeModel.getTypePath()); //$NON-NLS-1$

        targetPath = "Product/Name"; //$NON-NLS-1$
        targetNodeModel = ForeignKeyUtil.findTarget(targetPath, generateItemNodeModel()[0]);
        assertNotNull(targetNodeModel);
        assertEquals("Product Name", targetNodeModel.getLabel()); //$NON-NLS-1$
        assertEquals("Product/Name", targetNodeModel.getTypePath()); //$NON-NLS-1$

    }

    public void testTransformPath() {
        String[] pathArray = { "Caracteristique", "Format_Caracteristique",  //$NON-NLS-1$//$NON-NLS-2$
                "IdTypeValeur" }; //$NON-NLS-1$
        assertEquals("Caracteristique/Format_Caracteristique/IdTypeValeur", //$NON-NLS-1$
                ForeignKeyUtil.transformPath(pathArray));
    }

    public void testFindRelativePath() {
        String filterValue = "../Name";
        String filterOfXPath = "Product/Description";
        String currentPath = "Product/Family";
        assertEquals("Hat", ForeignKeyUtil
                .findRelativePathValueForSelectFK(filterValue, filterOfXPath, currentPath, generateItemNodeModel()[7]));
    }

    public void testGetXpathValue() {
        String filterValue = "Product/Name";
        String currentPath = "Product/Family";
        assertEquals("Hat", ForeignKeyUtil.getXpathValue(filterValue, currentPath, generateItemNodeModel()[7]));
    }

    public void testFindTargetRelativePathForCellFK() {
        assertEquals("Product/Name", ForeignKeyUtil.findTargetRelativePathForCellFK("Product/Family", "../Name"));
    }

    /**
     * return the Product ItemNodeModel
     *         resultNode[0] = productNode;
     *         resultNode[1] = pictureNode;
     *         resultNode[2] = idNode;
     *         resultNode[3] = nameNode;
     *         resultNode[4] = descriptionNode;
     *         resultNode[5] = featuresNode;
     *         resultNode[6] = priceNode;
     *         resultNode[7] = testNode;
     * @return Product ItemNodeModel Array
     */
    private ItemNodeModel[] generateItemNodeModel() {
        ItemNodeModel productNode = new ItemNodeModel("Product"); //$NON-NLS-1$
        ItemNodeModel pictureNode = new ItemNodeModel("Picture"); //$NON-NLS-1$
        pictureNode.setTypePath("Product/Picture");
        ItemNodeModel idNode = new ItemNodeModel("Id"); //$NON-NLS-1$
        idNode.setTypePath("Product/Id");
        idNode.setKey(true);
        ItemNodeModel nameNode = new ItemNodeModel("Name"); //$NON-NLS-1$
        nameNode.setLabel("Product Name");
        nameNode.setTypePath("Product/Name");
        nameNode.setObjectValue("Hat");
        ItemNodeModel descriptionNode = new ItemNodeModel("Description"); //$NON-NLS-1$
        descriptionNode.setTypePath("Product/Description");
        ItemNodeModel featuresNode = new ItemNodeModel("Features"); //$NON-NLS-1$
        featuresNode.setTypePath("Product/Features");
        ItemNodeModel priceNode = new ItemNodeModel("Price"); //$NON-NLS-1$
        priceNode.setTypePath("Product/Price");
        ItemNodeModel testNode = new ItemNodeModel("Test"); //$NON-NLS-1$
        testNode.setTypePath("Product/Test");

        productNode.add(pictureNode);
        productNode.add(idNode);
        productNode.add(nameNode);
        productNode.add(descriptionNode);
        productNode.add(featuresNode);
        productNode.add(priceNode);
        productNode.add(testNode);

        ItemNodeModel[] resultNode = new ItemNodeModel[8];
        resultNode[0] = productNode;
        resultNode[1] = pictureNode;
        resultNode[2] = idNode;
        resultNode[3] = nameNode;
        resultNode[4] = descriptionNode;
        resultNode[5] = featuresNode;
        resultNode[6] = priceNode;
        resultNode[7] = testNode;
        return resultNode;
    }
}