/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server.util;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.powermock.api.mockito.PowerMockito;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.talend.mdm.webapp.base.client.exception.ParserException;
import org.talend.mdm.webapp.base.client.i18n.BaseMessages;
import org.talend.mdm.webapp.base.client.i18n.BaseMessagesFactory;
import org.talend.mdm.webapp.base.client.model.Criteria;
import org.talend.mdm.webapp.base.client.model.SimpleCriterion;

import com.amalto.core.webservice.WSStringPredicate;
import com.amalto.core.webservice.WSWhereAnd;
import com.amalto.core.webservice.WSWhereCondition;
import com.amalto.core.webservice.WSWhereItem;
import com.amalto.core.webservice.WSWhereOperator;
import com.amalto.core.webservice.WSWhereOr;

@SuppressWarnings("nls")
public class CommonUtilTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        BaseMessagesFactory.setMessages(new MockBaseMessages());
    }

    @Override
    protected void tearDown() throws Exception {
        BaseMessagesFactory.setMessages(null);
    }

    public void testExtractIdWithDots() throws Exception {
        String[] keys = new String[] { "Id" };
        String ids = ".3";

        String[] result = CommonUtil.extractIdWithDots(keys, ids);
        assertTrue(result.length == 1);
        assertEquals(result[0], ids);

        ids = "1.3";
        result = CommonUtil.extractIdWithDots(keys, ids);
        assertTrue(result.length == 1);
        assertEquals(result[0], ids);

        ids = "3.";
        result = CommonUtil.extractIdWithDots(keys, ids);
        assertTrue(result.length == 1);
        assertEquals(result[0], ids);

        ids = "1";
        result = CommonUtil.extractIdWithDots(keys, ids);
        assertTrue(result.length == 1);
        assertEquals(result[0], ids);

        // Composite key is only support the following format, otherwise it will throw exception
        // see com.amalto.core.storage.StorageWrapper.getSelectTypeById(ComplexTypeMetadata, String, String[])
        keys = new String[] { "Id1", "Id2" };
        ids = "1.3";
        result = CommonUtil.extractIdWithDots(keys, ids);
        assertTrue(result.length == 2);
        assertEquals(result[0], "1");
        assertEquals(result[1], "3");
    }

    public void testJoinStrings() {
        List<String> testee = new ArrayList<String>();
        testee.add("NJ01");
        testee.add("#@$.");
        testee.add("\\/.");
        assertEquals("NJ01.#@$..\\/.", CommonUtil.joinStrings(testee, "."));
    }

    public void testBuildWhereItems() throws Exception {
        String inputString = "Agency/Id EQUALS *";
        WSWhereItem wi = CommonUtil.buildWhereItem(inputString);
        assertNotNull(wi);
        WSWhereOr wa = wi.getWhereOr();
        assertNotNull(wa);
        WSWhereItem[] wis = wa.getWhereItems();
        assertNotNull(wis);
        assertEquals(1, wis.length);
        WSWhereCondition wcond = wis[0].getWhereCondition();
        assertNotNull(wcond);
        assertEquals("Agency/Id", wcond.getLeftPath());
        assertEquals("*", wcond.getRightValueOrPath());
        assertEquals(WSWhereOperator.EQUALS, wcond.getOperator());

    }

    public void testBuildWhereItemsByCriteria() throws Exception {
        Criteria input = new SimpleCriterion("Agent/Id", "EQUALS", "*");
        WSWhereItem wi = CommonUtil.buildWhereItemsByCriteria(input);
        assertNotNull(wi);
        WSWhereOr wa = wi.getWhereOr();
        assertNotNull(wa);
        WSWhereItem[] wis = wa.getWhereItems();
        assertNotNull(wis);
        assertEquals(1, wis.length);
        WSWhereCondition wcond = wis[0].getWhereCondition();
        assertNotNull(wcond);
        assertEquals("Agent/Id", wcond.getLeftPath());
        assertEquals("*", wcond.getRightValueOrPath());
        assertEquals(WSWhereOperator.EQUALS, wcond.getOperator());

        // try to search one not existing
        Criteria input2 = new SimpleCriterion("Agentsss../Id", "EQUALS", "*");
        WSWhereItem wi2 = CommonUtil.buildWhereItemsByCriteria(input2);
        assertNotNull(wi2);
        WSWhereOr wa2 = wi2.getWhereOr();
        assertNotNull(wa2);
        WSWhereItem[] wis2 = wa2.getWhereItems();
        assertNotNull(wis2);
        assertEquals(1, wis2.length);
        WSWhereCondition wcond2 = wis2[0].getWhereCondition();
        assertNotNull(wcond2);
        assertEquals("Agentsss../Id", wcond2.getLeftPath());
        assertEquals("*", wcond2.getRightValueOrPath());
        assertEquals(WSWhereOperator.EQUALS, wcond2.getOperator());
    }

    public void testContainsSearch() {
        String criteria = "(MyEntity/id CONTAINS \\(test\\(\\)\\))";
        try {
            WSWhereItem wsWhereItem = CommonUtil.buildWhereItems(criteria);

            assertNotNull(wsWhereItem);
            WSWhereOr whereAnd = wsWhereItem.getWhereOr();
            assertNotNull(whereAnd);
            assertEquals(1, whereAnd.getWhereItems().length);
            WSWhereItem whereItem = whereAnd.getWhereItems()[0];
            assertNotNull(whereItem);
            WSWhereCondition condition = whereItem.getWhereCondition();
            assertNotNull(condition);

            assertEquals("MyEntity/id", condition.getLeftPath());
            assertEquals(WSWhereOperator.CONTAINS, condition.getOperator());
            assertEquals("(test())", condition.getRightValueOrPath());
            assertEquals(WSStringPredicate.NONE, condition.getStringPredicate());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testContainsSearch2() {
        String criteria = "(MyEntity/id CONTAINS \\(test \\(\\))"; // NON-NLS
        try {
            WSWhereItem wsWhereItem = CommonUtil.buildWhereItems(criteria);

            assertNotNull(wsWhereItem);
            WSWhereOr whereAnd = wsWhereItem.getWhereOr();
            assertNotNull(whereAnd);
            assertEquals(1, whereAnd.getWhereItems().length);
            WSWhereItem whereItem = whereAnd.getWhereItems()[0];
            assertNotNull(whereItem);
            WSWhereCondition condition = whereItem.getWhereCondition();
            assertNotNull(condition);

            assertEquals("MyEntity/id", condition.getLeftPath());
            assertEquals(WSWhereOperator.CONTAINS, condition.getOperator());
            assertEquals("(test ()", condition.getRightValueOrPath());
            assertEquals(WSStringPredicate.NONE, condition.getStringPredicate());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void testContainsSearch3() throws Exception {
        String criteria = "(MyEntity/id CONTAINS test\\))";
        WSWhereItem wsWhereItem = CommonUtil.buildWhereItems(criteria);

        assertNotNull(wsWhereItem);
        WSWhereOr whereAnd = wsWhereItem.getWhereOr();
        assertNotNull(whereAnd);
        assertEquals(1, whereAnd.getWhereItems().length);
        WSWhereItem whereItem = whereAnd.getWhereItems()[0];
        assertNotNull(whereItem);
        WSWhereCondition condition = whereItem.getWhereCondition();
        assertNotNull(condition);

        assertEquals("MyEntity/id", condition.getLeftPath());
        assertEquals(WSWhereOperator.CONTAINS, condition.getOperator());
        assertEquals("test)", condition.getRightValueOrPath());
        assertEquals(WSStringPredicate.NONE, condition.getStringPredicate());
    }

    public void testBlocks() throws Exception {
        String criteria = "((MyEntity/id CONTAINS test) AND (MyEntity/id2 EQUALS 1))";
        WSWhereItem wsWhereItem = CommonUtil.buildWhereItems(criteria);

        assertNotNull(wsWhereItem);
        WSWhereAnd whereAnd = wsWhereItem.getWhereAnd();
        assertNotNull(whereAnd);
        assertEquals(2, whereAnd.getWhereItems().length);
        {
            WSWhereItem whereItem = whereAnd.getWhereItems()[0];
            assertNotNull(whereItem);
            WSWhereOr nestedAnd = whereItem.getWhereOr();
            assertNotNull(nestedAnd);
            WSWhereCondition condition = nestedAnd.getWhereItems()[0].getWhereCondition();
            assertNotNull(condition);

            assertEquals("MyEntity/id", condition.getLeftPath());
            assertEquals(WSWhereOperator.CONTAINS, condition.getOperator());
            assertEquals("test", condition.getRightValueOrPath());
            assertEquals(WSStringPredicate.NONE, condition.getStringPredicate());
        }
        {
            WSWhereItem whereItem = whereAnd.getWhereItems()[1];
            assertNotNull(whereItem);
            WSWhereOr nestedAnd = whereItem.getWhereOr();
            assertNotNull(nestedAnd);
            WSWhereCondition condition = nestedAnd.getWhereItems()[0].getWhereCondition();
            assertNotNull(condition);

            assertEquals("MyEntity/id2", condition.getLeftPath());
            assertEquals(WSWhereOperator.EQUALS, condition.getOperator());
            assertEquals("1", condition.getRightValueOrPath());
            assertEquals(WSStringPredicate.NONE, condition.getStringPredicate());
        }
    }

    public void testBlocks2() throws Exception {
        String criteria = "((MyEntity/id CONTAINS test\\(\\)\\)) AND (MyEntity/id2 EQUALS 1))";
        WSWhereItem wsWhereItem = CommonUtil.buildWhereItems(criteria);

        assertNotNull(wsWhereItem);
        WSWhereAnd whereAnd = wsWhereItem.getWhereAnd();
        assertNotNull(whereAnd);
        assertEquals(2, whereAnd.getWhereItems().length);
        {
            WSWhereItem whereItem = whereAnd.getWhereItems()[0];
            assertNotNull(whereItem);
            WSWhereOr nestedAnd = whereItem.getWhereOr();
            assertNotNull(nestedAnd);
            WSWhereCondition condition = nestedAnd.getWhereItems()[0].getWhereCondition();
            assertNotNull(condition);

            assertEquals("MyEntity/id", condition.getLeftPath());
            assertEquals(WSWhereOperator.CONTAINS, condition.getOperator());
            assertEquals("test())", condition.getRightValueOrPath());
            assertEquals(WSStringPredicate.NONE, condition.getStringPredicate());
        }
        {
            WSWhereItem whereItem = whereAnd.getWhereItems()[1];
            assertNotNull(whereItem);
            WSWhereOr nestedAnd = whereItem.getWhereOr();
            assertNotNull(nestedAnd);
            WSWhereCondition condition = nestedAnd.getWhereItems()[0].getWhereCondition();
            assertNotNull(condition);

            assertEquals("MyEntity/id2", condition.getLeftPath());
            assertEquals(WSWhereOperator.EQUALS, condition.getOperator());
            assertEquals("1", condition.getRightValueOrPath());
            assertEquals(WSStringPredicate.NONE, condition.getStringPredicate());
        }
    }

    public void testSlashes() throws Exception {
        String criteria = "(MyEntity/id CONTAINS H/F Sundbyvester)";
        WSWhereItem wsWhereItem = CommonUtil.buildWhereItems(criteria);

        assertNotNull(wsWhereItem);
        WSWhereOr whereAnd = wsWhereItem.getWhereOr();
        assertNotNull(whereAnd);
        assertEquals(1, whereAnd.getWhereItems().length);
        {
            WSWhereItem whereItem = whereAnd.getWhereItems()[0];
            assertNotNull(whereItem);
            WSWhereCondition condition = whereItem.getWhereCondition();
            assertNotNull(condition);

            assertEquals("MyEntity/id", condition.getLeftPath());
            assertEquals(WSWhereOperator.CONTAINS, condition.getOperator());
            // assertEquals("H/F Sundbyvester", condition.getRightValueOrPath());
            assertEquals(WSStringPredicate.NONE, condition.getStringPredicate());
        }
    }

    public void testError() {
        String criteria = "(MyEntity/id CONTAINS test";
        try {
            CommonUtil.buildWhereItems(criteria);
            fail("Exception was expected (unclosed parenthesis");
        } catch (Exception e) {
            // Expected
        }
    }

    public void testError2() {
        String criteria = "(MyEntity/id OEIOD test)";
        try {
            CommonUtil.buildWhereItems(criteria);
            fail("Exception was expected (predicate doesn't exist)");
        } catch (Exception e) {
            // Expected
        }
    }

    public void testError3() {
        String criteria = "()";
        try {
            CommonUtil.buildWhereItems(criteria);
            fail("Exception was expected (no expression)");
        } catch (Exception e) {
            // Expected
        }
    }

    public void testError4() {
        String criteria = "MyEntity/id CONTAINS *";
        try {
            CommonUtil.buildWhereItems(criteria);
        } catch (Exception e) {
            fail();
        }
    }

    public void testError5() {
        String criteria = "blahblah";
        RequestAttributes mockRequestAtrributes = PowerMockito.mock(RequestAttributes.class);
        RequestContextHolder.setRequestAttributes(mockRequestAtrributes);
        try {
            CommonUtil.buildWhereItems(criteria);
        } catch (ParserException e) {
            // OK
        } catch (Exception e) {
            fail();
        }

    }

    public void testBuildCriteriaByIds() {
        String[] keys = null;
        String[] ids = null;
        assertNull(CommonUtil.buildCriteriaByIds(keys, ids));

        keys = new String[1];
        ids = new String[1];
        keys[0] = "Product/Id";
        ids[0] = "10001";
        String criteria = CommonUtil.buildCriteriaByIds(keys, ids);
        assertEquals("Product/Id EQUALS 10001", criteria);

        keys = new String[2];
        ids = new String[2];
        keys[0] = "Product/Id";
        keys[1] = "Product/Name";
        ids[0] = "10001";
        ids[1] = "Test";
        criteria = CommonUtil.buildCriteriaByIds(keys, ids);
        assertEquals("((Product/Id EQUALS 10001) AND (Product/Name EQUALS Test))", criteria);
    }

    public void testExtractFKRefValue() {
        String ids = "[1][2]";
        String[] id = CommonUtil.extractFKRefValue(ids, "en");
        assertEquals("1", id[0]);
        assertEquals("2", id[1]);
    }

    public void testSplitString() {
        String s = "Small;Medium;Large";
        List<String> result = CommonUtil.splitString(s, ';');
        assertEquals(3, result.size());
        assertTrue(result.contains("Small"));
        assertTrue(result.contains("Medium"));
        assertTrue(result.contains("Large"));
        result = CommonUtil.splitString(s, '|');
        assertEquals(1, result.size());
        assertEquals(s, result.get(0));

        s = "Small|Medium|Large";
        result = CommonUtil.splitString(s, '|');
        assertEquals(3, result.size());
        assertTrue(result.contains("Small"));
        assertTrue(result.contains("Medium"));
        assertTrue(result.contains("Large"));
        result = CommonUtil.splitString(s, ';');
        assertEquals(1, result.size());
        assertEquals(s, result.get(0));
    }

    private class MockBaseMessages implements BaseMessages {

        @Override
        public String exception_parse_illegalChar(int beginIndex) {
            return "";
        }

        @Override
        public String exception_parse_unknownOperator(String value) {
            return "";
        }

        @Override
        public String exception_parse_missEndBlock(char endBlock, int i) {
            return "";
        }

        @Override
        public String exception_parse_tooManyEndBlock(char endBlock, int i) {
            return "";
        }

        @Override
        public String page_size_label() {
            return "";
        }

        @Override
        public String page_size_notice() {
            return "";
        }

        @Override
        public String info_title() {
            return "";
        }

        @Override
        public String error_title() {
            return "";
        }

        @Override
        public String warning_title() {
            return "";
        }

        @Override
        public String confirm_title() {
            return "";
        }

        @Override
        public String unknown_error() {
            return "";
        }

        @Override
        public String session_timeout_error() {
            return "";
        }

        @Override
        public String open_mls_title() {
            return "";
        }

        @Override
        public String language_title() {
            return "";
        }

        @Override
        public String value_title() {
            return "";
        }

        @Override
        public String multiLanguage_edit_failure() {
            return "";
        }

        @Override
        public String multiLangauge_language_duplicate() {
            return "";
        }

        @Override
        public String edit_success_info() {
            return null;
        }

        @Override
        public String message_success() {
            return "";
        }

        @Override
        public String message_error() {
            return "";
        }

        @Override
        public String message_fail() {
            return "";
        }

        @Override
        public String edititem() {
            return "";
        }

        @Override
        public String add_btn() {
            return "";
        }

        @Override
        public String remove_btn() {
            return "";
        }

        @Override
        public String exception_fk_malform(String fk) {
            return "";
        }

        @Override
        public String overwrite_confirm() {
            return "";
        }

        @Override
        public String label_exception_id_malform(String id) {
            return "";
        }

        @Override
        public String server_error() {
            return "";
        }

        @Override
        public String server_error_notification() {
            return "";
        }

        @Override
        public String service_rest_error() {
            return "";
        }

        @Override
        public String service_rest_exception() {
            return "";
        }

        @Override
        public String underlying_cause() {
            return "";
        }

        @Override
        public String matching_failed(String concept) {
            return "";
        }

        @Override
        public String delete_success_prefix() {
            return "";
        }

        @Override
        public String delete_fail_prefix() {
            return "";
        }

        @Override
        public String restore_success_prefix() {
            return "";
        }

        @Override
        public String restore_fail_prefix() {
            return "";
        }

        @Override
        public String save_more_btn() {
            return "";
        }
    }
}
