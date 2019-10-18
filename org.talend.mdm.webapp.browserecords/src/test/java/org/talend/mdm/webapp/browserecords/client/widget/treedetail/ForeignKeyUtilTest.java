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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
        itemNodeModel3
                .setTypePath("Caracteristique/Format_Caracteristique:Caracteristique_Liste/ValeursCaracteristique"); //$NON-NLS-1$
        itemNodeModel.getChildren().add(itemNodeModel1);
        itemNodeModel.getChildren().add(itemNodeModel2);
        itemNodeModel.getChildren().add(itemNodeModel3);

        ItemNodeModel targetNodeModel = ForeignKeyUtil.findTarget(targetPath, itemNodeModel);
        assertNotNull(targetNodeModel);
        assertEquals("IdTypeValeur", targetNodeModel.getLabel()); //$NON-NLS-1$
        assertEquals("Caracteristique/Format_Caracteristique:Caracteristique_Liste/IdTypeValeur",
                targetNodeModel.getTypePath()); //$NON-NLS-1$

        targetPath = "Caracteristique/Format_Caracteristique[@xsi:type=\"Caracteristique_Liste\"]/IdTypeValeur"; //$NON-NLS-1$
        targetNodeModel = ForeignKeyUtil.findTarget(targetPath, itemNodeModel);
        assertNotNull(targetNodeModel);
        assertEquals("IdTypeValeur", targetNodeModel.getLabel()); //$NON-NLS-1$
        assertEquals("Caracteristique/Format_Caracteristique:Caracteristique_Liste/IdTypeValeur",
                targetNodeModel.getTypePath()); //$NON-NLS-1$

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

    public void testParseFilterValue() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("Product/Name", "Talend Dog T-Shirt");
        Set<String> notInViewFieldSet = new HashSet<String>();
        notInViewFieldSet.add("Product/Name");
        Map<String, String> relativePathMapping = new HashMap<String, String>();
        relativePathMapping.put("Product/Name", "../Name");

        // Not exist in viewable
        // 1. two XSI function xpath not exist in viewable
        String filter = "fn:concat(&quot;xpath:/Product/Name&quot;, &quot;xpath:/Product/Name&quot;)";
        String expectedFilter = "fn:concat(&quot;Talend Dog T-Shirt&quot;, &quot;Talend Dog T-Shirt&quot;)";
        assertEquals(expectedFilter, ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter));

        // 2. one XSI function xpath not exist in viewable
        filter = "fn:concat(&quot;xpath:/Product/Name&quot;, &quot;s&quot;)";
        expectedFilter = "fn:concat(&quot;Talend Dog T-Shirt&quot;, &quot;s&quot;)";
        assertEquals(expectedFilter, ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter));

        // 3. one xpath not exist in viewable
        filter = "Product/Name";
        expectedFilter = "\"Talend Dog T-Shirt\"";
        assertEquals(expectedFilter, ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter));

        // 4. one relative path not exist in viewable
        filter = "../Name";
        expectedFilter = "\"Talend Dog T-Shirt\"";
        assertEquals(expectedFilter, ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter));

        // 5. one XSI function relative xpath not exist in viewable
        filter = "fn:concat(&quot;xpath:../Name&quot;, &quot;s&quot;)";
        expectedFilter = "fn:concat(&quot;Talend Dog T-Shirt&quot;, &quot;s&quot;)";
        assertEquals(expectedFilter, ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter));

        // Exist in viewable
        // 6. one xpath exist in viewable
        filter = "Product/Description";
        expectedFilter = "Product/Description";
        assertEquals(expectedFilter, ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter));

        // 7. one XSI function xpath exist in viewable
        filter = "fn:concat(&quot;xpath:/Product/Description&quot;, &quot;s&quot;)";
        expectedFilter = "fn:concat(&quot;xpath:/Product/Description&quot;, &quot;s&quot;)";
        assertEquals(expectedFilter, ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter));

        // 8. ../Description
        filter = "../Description";
        expectedFilter = "../Description";
        assertEquals(expectedFilter, ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter));

        // 9. fn:concat("xpath:../Description", "s")
        filter = "fn:concat(&quot;xpath:../Description&quot;, &quot;s&quot;)";
        expectedFilter = "fn:concat(&quot;xpath:../Description&quot;, &quot;s&quot;)";
        assertEquals(expectedFilter, ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter));

        // 10. fn:concat("xpath:/Product/Description", "xpath:/Product/Description")
        filter = "fn:concat(&quot;xpath:/Product/Description&quot;, &quot;xpath:/Product/Description&quot;)";
        expectedFilter = "fn:concat(&quot;xpath:/Product/Description&quot;, &quot;xpath:/Product/Description&quot;)";
        assertEquals(expectedFilter, ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter));

        // 11. One Exist in viewable, one don't exist in viewable
        filter = "fn:concat(&quot;xpath:/Product/Description&quot;, &quot;xpath:/Product/Name&quot;)";
        expectedFilter = "fn:concat(&quot;xpath:/Product/Description&quot;, &quot;Talend Dog T-Shirt&quot;)";
        String aa = ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter);
        assertEquals(expectedFilter, ForeignKeyUtil.parseFilterValue(result, notInViewFieldSet, relativePathMapping, filter));
    }

    /**
     * Return the Product ItemNodeModel
     *     resultNode[0] = productNode;
     *     resultNode[1] = pictureNode;
     *     resultNode[2] = idNode;
     *     resultNode[3] = nameNode;
     *     resultNode[4] = descriptionNode;
     *     resultNode[5] = featuresNode;
     *     resultNode[6] = priceNode;
     *     resultNode[7] = testNode;
     *
     * @return Product ItemNodeModel Array
     */
    private ItemNodeModel[] generateItemNodeModel() {
        ItemNodeModel productNode = new ItemNodeModel("Product"); //$NON-NLS-1$
        ItemNodeModel pictureNode = new ItemNodeModel("Picture"); //$NON-NLS-1$
        pictureNode.setTypePath("Product/Picture"); //$NON-NLS-1$
        ItemNodeModel idNode = new ItemNodeModel("Id"); //$NON-NLS-1$
        idNode.setTypePath("Product/Id"); //$NON-NLS-1$
        idNode.setKey(true);
        ItemNodeModel nameNode = new ItemNodeModel("Name"); //$NON-NLS-1$
        nameNode.setLabel("Product Name"); //$NON-NLS-1$
        nameNode.setTypePath("Product/Name"); //$NON-NLS-1$
        nameNode.setObjectValue("Hat"); //$NON-NLS-1$
        ItemNodeModel descriptionNode = new ItemNodeModel("Description"); //$NON-NLS-1$
        descriptionNode.setTypePath("Product/Description"); //$NON-NLS-1$
        ItemNodeModel featuresNode = new ItemNodeModel("Features"); //$NON-NLS-1$
        featuresNode.setTypePath("Product/Features"); //$NON-NLS-1$
        ItemNodeModel priceNode = new ItemNodeModel("Price"); //$NON-NLS-1$
        priceNode.setTypePath("Product/Price"); //$NON-NLS-1$
        ItemNodeModel testNode = new ItemNodeModel("Test"); //$NON-NLS-1$
        testNode.setTypePath("Product/Test"); //$NON-NLS-1$

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