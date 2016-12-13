/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.talend.mdm.webapp.base.client.model.Criteria;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.MultipleCriteria;
import org.talend.mdm.webapp.base.client.model.SimpleCriterion;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyDrawer;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailUtil;
import org.talend.mdm.webapp.browserecords.server.util.TestData;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.amalto.webapp.core.util.Util;
import com.extjs.gxt.ui.client.data.ModelData;

@SuppressWarnings("nls")
public class CommonUtilTest extends TestCase {

    public void testParseSimpleSearchExpression() throws Exception {
        String s = "(foo/bar EQUALS 3/4)";
        CommonUtil.CriteriaAndC r = CommonUtil.parseSimpleSearchExpression(s.toCharArray(), 0);
        assertTrue(r.cr instanceof SimpleCriterion);
        assertTrue(r.c == s.length() - 1);
        assertTrue(((SimpleCriterion) r.cr).getOperator().equals("EQUALS"));
        assertTrue(((SimpleCriterion) r.cr).getKey().equals("foo/bar"));
        assertTrue(((SimpleCriterion) r.cr).getValue().equals("3/4"));
    }

    public void testParseSimpleSearchExpression_value_with_multi_words() throws Exception {
        String s = "(Product/Name CONTAINS New York City)";
        CommonUtil.CriteriaAndC r = CommonUtil.parseSimpleSearchExpression(s.toCharArray(), 0);
        assertTrue(r.cr instanceof SimpleCriterion);
        assertTrue(r.c == s.length() - 1);
        assertTrue(((SimpleCriterion) r.cr).getOperator().equals("CONTAINS"));
        assertTrue(((SimpleCriterion) r.cr).getKey().equals("Product/Name"));
        assertTrue(((SimpleCriterion) r.cr).getValue().equals("New York City"));

        s = "(Product/Name EMPTY_NULL)";
        r = CommonUtil.parseSimpleSearchExpression(s.toCharArray(), 0);
        assertTrue(r.cr instanceof SimpleCriterion);
        assertTrue(r.c == s.length() - 1);
        assertTrue(((SimpleCriterion) r.cr).getOperator().equals("EMPTY_NULL"));
        assertTrue(((SimpleCriterion) r.cr).getKey().equals("Product/Name"));
        assertTrue(((SimpleCriterion) r.cr).getValue().equals(""));

        s = "(Product/Name EMPTY_NULL *)";
        r = CommonUtil.parseSimpleSearchExpression(s.toCharArray(), 0);
        assertTrue(r.cr instanceof SimpleCriterion);
        assertTrue(r.c == s.length() - 1);
        assertTrue(((SimpleCriterion) r.cr).getOperator().equals("EMPTY_NULL"));
        assertTrue(((SimpleCriterion) r.cr).getKey().equals("Product/Name"));
        assertTrue(((SimpleCriterion) r.cr).getValue().equals("*"));
    }
    
    public void testParseMultipleSearchExpression() throws Exception {
        String s = "((foo/bar EQUALS 3/4) AND ((a/a/a MORETHAN a/b) OR (c/b/f LESSTHAN 3.2)) AND (c/c/c MORETHAN c/c))";
        CommonUtil.CriteriaAndC r = CommonUtil.parseMultipleSearchExpression(s.toCharArray(), 0);
        assertTrue(r.cr instanceof MultipleCriteria);
        assertTrue(r.c == s.length() - 1);
        MultipleCriteria mc = (MultipleCriteria) r.cr;
        assertTrue(mc.getOperator().equals("AND"));
        List<Criteria> children = mc.getChildren();
        assertTrue(children.size() == 3);
        MultipleCriteria child = (MultipleCriteria) children.get(1);
        assertTrue(child.getOperator().equals("OR"));
        children = child.getChildren();
        assertTrue(((SimpleCriterion) children.get(0)).getOperator().equals("MORETHAN"));
        assertTrue(((SimpleCriterion) children.get(1)).getOperator().equals("LESSTHAN"));

        s = "Product/Id CONTAINS *";
        if (!s.startsWith("(") && !s.endsWith(")")) {
            s = "((" + s + "))";
        }
        r = CommonUtil.parseMultipleSearchExpression(s.toCharArray(), 0);
        assertTrue(r.cr instanceof MultipleCriteria);
        assertTrue(r.c == s.length() - 1);
        mc = (MultipleCriteria) r.cr;
        assertTrue(mc.getOperator().equals("AND"));
        children = mc.getChildren();
        assertTrue(children.size() == 1);
        SimpleCriterion simpleCriterion = (SimpleCriterion) children.get(0);
        assertTrue(simpleCriterion.getKey().equals("Product/Id"));
        assertTrue(simpleCriterion.getOperator().equals("CONTAINS"));
        assertTrue(simpleCriterion.getValue().equals("*"));

        s = "Product/Id EMPTY_NULL *";
        if (!s.startsWith("(") && !s.endsWith(")")) {
            s = "((" + s + "))";
        }
        r = CommonUtil.parseMultipleSearchExpression(s.toCharArray(), 0);
        assertTrue(r.cr instanceof MultipleCriteria);
        assertTrue(r.c == s.length() - 1);
        mc = (MultipleCriteria) r.cr;
        assertTrue(mc.getOperator().equals("AND"));
        children = mc.getChildren();
        assertTrue(children.size() == 1);
        simpleCriterion = (SimpleCriterion) children.get(0);
        assertTrue(simpleCriterion.getKey().equals("Product/Id"));
        assertTrue(simpleCriterion.getOperator().equals("EMPTY_NULL"));
        assertTrue(simpleCriterion.getValue().equals("*"));

        s = "Product/Id EMPTY_NULL";
        if (!s.startsWith("(") && !s.endsWith(")")) {
            s = "((" + s + "))";
        }
        r = CommonUtil.parseMultipleSearchExpression(s.toCharArray(), 0);
        assertTrue(r.cr instanceof MultipleCriteria);
        assertTrue(r.c == s.length() - 1);
        mc = (MultipleCriteria) r.cr;
        assertTrue(mc.getOperator().equals("AND"));
        children = mc.getChildren();
        assertTrue(children.size() == 1);
        simpleCriterion = (SimpleCriterion) children.get(0);
        assertTrue(simpleCriterion.getKey().equals("Product/Id"));
        assertTrue(simpleCriterion.getOperator().equals("EMPTY_NULL"));
        assertTrue(simpleCriterion.getValue().equals(""));
    }

    public void testValidateSearchValue() {
        Map<String, TypeModel> xpathMap = new HashMap<String, TypeModel>();
        xpathMap.put("Product/Name", new SimpleTypeModel()); //$NON-NLS-1$
        assertTrue(CommonUtil.validateSearchValue(xpathMap, "/Product/Name")); //$NON-NLS-1$
        assertTrue(CommonUtil.validateSearchValue(xpathMap, "Product/Name")); //$NON-NLS-1$
        assertFalse(CommonUtil.validateSearchValue(xpathMap, "/Product/Name/")); //$NON-NLS-1$
        assertFalse(CommonUtil.validateSearchValue(xpathMap, "/Product//Name")); //$NON-NLS-1$
        assertFalse(CommonUtil.validateSearchValue(xpathMap, "a/b")); //$NON-NLS-1$
        assertTrue(CommonUtil.validateSearchValue(xpathMap, "\"a/b\"")); //$NON-NLS-1$
        assertTrue(CommonUtil.validateSearchValue(xpathMap, "\'a/b\'")); //$NON-NLS-1$
        assertFalse(CommonUtil.validateSearchValue(xpathMap, "\'a/b\"")); //$NON-NLS-1$
        assertFalse(CommonUtil.validateSearchValue(xpathMap, "\"a/b")); //$NON-NLS-1$
        assertTrue(CommonUtil.validateSearchValue(xpathMap, "aaa")); //$NON-NLS-1$
    }

    public void testGetHost() {
        assertTrue(CommonUtil.getHost("http://www.foo.com/aaa").equals("www.foo.com"));
        assertTrue(CommonUtil.getHost("https://www.foo.com/aaa").equals("www.foo.com"));
        assertTrue(CommonUtil.getHost("www.foo.com/aaa").equals("www.foo.com"));
        assertTrue(CommonUtil.getHost("www.foo.com").equals("www.foo.com"));
        assertTrue(CommonUtil.getHost("www.foo.com:8080").equals("www.foo.com:8080"));
        assertTrue(CommonUtil.getHost("foo.com/aaa").equals("foo.com"));
        assertTrue(CommonUtil.getHost("http://foo.com/aaa/bbb?aa=a").equals("foo.com"));
    }

    /**
     * DOC Starkey Comment method "testParseFileName".
     */
    public void testParseFileName() {

        // test filename
        String path = "hshu.jpg"; //$NON-NLS-1$
        assertEquals(CommonUtil.parseFileName(path)[0], "hshu"); //$NON-NLS-1$
        assertEquals(CommonUtil.parseFileName(path)[1], "jpg"); //$NON-NLS-1$

        // test for case "\"
        String path2 = "C:\\fakepath\\hshu.jpg"; //$NON-NLS-1$
        assertEquals(CommonUtil.parseFileName(path2)[0], "hshu"); //$NON-NLS-1$
        assertEquals(CommonUtil.parseFileName(path2)[1], "jpg"); //$NON-NLS-1$

        // test for case "/"
        String path3 = "C:/fakepath/hshu.jpg"; //$NON-NLS-1$
        assertEquals(CommonUtil.parseFileName(path3)[0], "hshu"); //$NON-NLS-1$
        assertEquals(CommonUtil.parseFileName(path3)[1], "jpg"); //$NON-NLS-1$

    }

    public void testTypePathToXpath() {

        String result = CommonUtil.typePathToXpath("Eda/typeEda/typeEDA:PointSoutirageRpt/crmaEda"); //$NON-NLS-1$
        assertEquals("Eda/typeEda/typeEDA[@xsi:type='PointSoutirageRpt']/crmaEda", result); //$NON-NLS-1$

        result = CommonUtil.typePathToXpath("Eda/typeEda/typeEDA:PointSoutirageRpt/crmaEda/qqq:www"); //$NON-NLS-1$
        assertEquals("Eda/typeEda/typeEDA[@xsi:type='PointSoutirageRpt']/crmaEda/qqq[@xsi:type='www']", result); //$NON-NLS-1$

    }

    public void transferXpath() {
        String xp = "a/b/c/d";
        Map<String, String> map = new HashMap<String, String>();
        map.put("a/b", "hello");
        map.put("a/b/c", "peili");
        map.put("a/b/c/d", "liang");
        StringBuffer sb = new StringBuffer();
        // a/b/c/d hello peili liang
        Stack<String> stack = new Stack<String>();
        do {
            String v = map.get(xp);
            stack.push(v);
            xp = xp.substring(0, xp.lastIndexOf("/"));
        } while (xp.indexOf("/") != -1);
        boolean flag = true;

        while (!stack.isEmpty()) {
            if (flag) {
                flag = false;
            } else {
                sb.append("/");
            }
            sb.append(stack.pop());
        }
        Assert.assertEquals("hello/peili/liang", sb.toString());
    }

    public void testGetRealPath() throws Exception {
        ItemNodeModel nodeModel = TestData.getModel();
        // 1. getPathWithIndex
        List<String> xpathes = TestData.getXpathes("xpathes.properties");
        Iterator<String> iter = xpathes.iterator();
        assertPathWithIndex(nodeModel, iter);
        // 2. getRealTypePath
        xpathes = TestData.getXpathes("realTypePathes.properties");
        iter = xpathes.iterator();
        assertRealTypePath(nodeModel, iter);
    }

    private void assertPathWithIndex(ItemNodeModel nodeModel, Iterator<String> iter) {
        String xpath = iter.next();
        String nodePath = CommonUtil.getRealXPath(nodeModel);
        Assert.assertEquals(xpath, nodePath);

        List<ModelData> children = nodeModel.getChildren();
        if (children != null) {
            for (ModelData child : children) {
                assertPathWithIndex((ItemNodeModel) child, iter);
            }
        }
    }

    private void assertRealTypePath(ItemNodeModel nodeModel, Iterator<String> iter) {
        String xpath = iter.next();
        String nodeRealTypePath = CommonUtil.getRealTypePath(nodeModel);
        assertEquals(xpath, nodeRealTypePath);

        List<ModelData> children = nodeModel.getChildren();
        if (children != null) {
            for (ModelData child : children) {
                assertRealTypePath((ItemNodeModel) child, iter);
            }
        }
    }

    public void test_polymorphismTypeXpathRegex() {
        String xpath = "Person:Student/Name";
        xpath = xpath.replaceAll(":\\w+", "");
        assertEquals("Person/Name", xpath);
    }

    public void test_getFKTabLabel() {
        // 1
        String label = "Agency{position()}";
        assertEquals("Agency", LabelUtil.getFKTabLabel(label));
        // 2
        label = "Agency:{position()}";
        assertEquals("Agency", LabelUtil.getFKTabLabel(label));
        // 3
        label = "Agent:Agency{position()}";
        assertEquals("Agent:Agency", LabelUtil.getFKTabLabel(label));
        // 4
        label = "Agency: {position()}";
        assertEquals("Agency", LabelUtil.getFKTabLabel(label));
    }

    public void test_Util_getExceptionMessage() {
        String message = "<msg>[EN:validate error][FR:validate error]</msg>";
        String language = "en";
        // 1
        String actualMsg = Util.getExceptionMessage(message, language);
        assertEquals("validate error", actualMsg);
        // 2
        message = "<msg/>";
        actualMsg = Util.getExceptionMessage(message, language);
        assertEquals("", actualMsg);
        // 3
        message = "<msg>[EN:validate error]</msg>";
        language = "fr";
        actualMsg = Util.getExceptionMessage(message, language);
        assertEquals(message, actualMsg);
        // 4
        message = "<msg>[CHINESE:validate error]</msg>";
        language = "chinese";
        actualMsg = Util.getExceptionMessage(message, language);
        assertEquals("validate error", actualMsg);
        // 5
        message = "<msg>[EN:validate error][FR:fr validate error]</msg>";
        language = "fr";
        actualMsg = Util.getExceptionMessage(message, language);
        assertEquals("fr validate error", actualMsg);
        // 6
        message = "<msg>[EN:validate error][CHINESE:validate error][FR:fr validate error]</msg>";
        language = "fr";
        actualMsg = Util.getExceptionMessage(message, language);
        assertEquals("fr validate error", actualMsg);
        // 7
        message = "[EN:price must > 10][FR:price must > 10]";
        if (message.length() > 0) {
            if (message.indexOf("<msg>") == -1) {
                message = "<msg>" + message + "</msg>";
            }
        }
        language = "en";
        actualMsg = Util.getExceptionMessage(message, language);
        assertEquals("price must > 10", actualMsg);
    }

    public void testGetFKFormat() {
        int result = -1;
        result = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getFKFormatType(null, null);
        assertEquals(0, result);

        result = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getFKFormatType("   ", null);
        assertEquals(0, result);

        result = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getFKFormatType("[1]", null);
        assertEquals(1, result);

        result = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getFKFormatType("a[1]b", null);
        assertEquals(0, result);

        result = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getFKFormatType("[1]-aaa", "-");
        assertEquals(2, result);

        result = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getFKFormatType("aaa-[1]-aaa", null);
        assertEquals(0, result);

    }

    public void testGetForeignKeyId() {
        String fk = null;
        fk = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getForeignKeyId("test", 0, null);
        assertNull(fk);

        fk = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getForeignKeyId("[1]", 1, null);
        assertEquals("[1]", fk);

        fk = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getForeignKeyId("test", 2, null);
        assertNull(fk);

        fk = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getForeignKeyId("[2]-test", 2, "-");
        assertEquals("[2]", fk);
    }

    public void test_isChangeValue() {
        String language = "en";
        boolean isCreate = true;
        // Build a root
        ComplexTypeModel root = new ComplexTypeModel("root", DataTypeConstants.STRING);
        root.addDescription(language, "root");
        // add id to root
        TypeModel idModel = new SimpleTypeModel("id", DataTypeConstants.LONG);
        idModel.addDescription(language, "id");
        root.addSubType(idModel);
        // add name to root
        TypeModel nameModel = new SimpleTypeModel("name", DataTypeConstants.STRING);
        nameModel.addDescription(language, "name");
        nameModel.setDefaultValueExpression("Hello");
        nameModel.setDefaultValue("Hello");
        root.addSubType(nameModel);
        // when create a tree model
        List<ItemNodeModel> list = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getDefaultTreeModel(root, isCreate,
                language);
        ItemNodeModel rootNode = list.get(0);
        assertTrue(TreeDetailUtil.isChangeValue(rootNode));
        // when display a tree model
        isCreate = false;
        list = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getDefaultTreeModel(root, isCreate, language);
        rootNode = list.get(0);
        assertFalse(TreeDetailUtil.isChangeValue(rootNode));
    }

    public void testExtractIDs() {
        ItemNodeModel model = new ItemNodeModel();

        ItemNodeModel child = new ItemNodeModel();
        child.setTypePath("Product/Id");
        child.setKey(true);
        child.setObjectValue("1");
        model.add(child);

        child = new ItemNodeModel();
        child.setTypePath("Product/Name");
        child.setKey(false);
        child.setObjectValue("2");
        model.add(child);

        child = new ItemNodeModel();
        child.setTypePath("Product/Price");
        child.setKey(true);
        child.setObjectValue("3");
        model.add(child);

        child = new ItemNodeModel();
        child.setTypePath("Product/Desription");
        child.setKey(false);
        child.setObjectValue("4");
        model.add(child);

        ViewBean viewBean = new ViewBean();
        EntityModel bindingEntityModel = new EntityModel();
        String[] keyPath = { "Product/Price", "Product/Id" };
        bindingEntityModel.setKeys(keyPath);
        viewBean.setBindingEntityModel(bindingEntityModel);

        String[] keys = CommonUtil.extractIDs(model, viewBean);
        assertTrue(keys.length == 2);
        assertTrue(keys[0].equals("3"));
        assertTrue(keys[1].equals("1"));

        model = new ItemNodeModel();
        keys = CommonUtil.extractIDs(model, viewBean);
        assertTrue(keys.length == 0);
    }

    public void testDefaultItemNodeValue() {

        String language = "en";
        boolean isCreate = true;

        ComplexTypeModel typeModel = new ComplexTypeModel("root", DataTypeConstants.STRING);
        typeModel.addDescription(language, "root");

        TypeModel idModel = new SimpleTypeModel("id", DataTypeConstants.LONG);
        typeModel.addSubType(idModel);

        TypeModel nameModel = new SimpleTypeModel("name", DataTypeConstants.STRING);
        nameModel.addDescription(language, "name");
        typeModel.addSubType(nameModel);

        TypeModel ageModel = new SimpleTypeModel("age", DataTypeConstants.INTEGER);
        typeModel.addSubType(ageModel);

        TypeModel dobModel = new SimpleTypeModel("DOB", DataTypeConstants.DATETIME);
        typeModel.addSubType(dobModel);

        TypeModel isActiveModel = new SimpleTypeModel("isActive", DataTypeConstants.BOOLEAN);
        typeModel.addSubType(isActiveModel);

        List<ItemNodeModel> list = org.talend.mdm.webapp.browserecords.server.util.CommonUtil.getDefaultTreeModel(typeModel,
                isCreate, language);

        assertNotNull(list);
        assertTrue(list.size() > 0);

        List<ModelData> children = list.get(0).getChildren();
        for (ModelData modelData : children) {
            ItemNodeModel itemNodeModel = (ItemNodeModel) modelData;
            assertNull(itemNodeModel.getObjectValue());
        }

    }

    public void testGetDownloadFileHeadName() {
        TypeModel typeModel = new SimpleTypeModel("downloadFileHeadName", null);
        assertEquals("downloadFileHeadName", CommonUtil.getDownloadFileHeadName(typeModel));

    }

    public void testSwitchForeignKeyEntityType() {
        String targetEntity = "Company";
        String xpathForeignKey = "Party/Code";
        String xpathInfoForeignKey = "Party/Name";
        ForeignKeyDrawer fkDrawer = CommonUtil.switchForeignKeyEntityType(targetEntity, xpathForeignKey, xpathInfoForeignKey);
        assertNotNull(fkDrawer);
        assertEquals("Company/Code", fkDrawer.getXpathForeignKey());
        assertEquals("Company/Name", fkDrawer.getXpathInfoForeignKey());
    }

}
