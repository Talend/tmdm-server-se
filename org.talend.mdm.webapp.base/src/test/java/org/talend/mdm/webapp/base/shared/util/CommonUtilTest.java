/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.shared.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.talend.mdm.webapp.base.shared.util.CommonUtil;


/**
 * created by talend2 on 2013-8-1
 * Detailled comment
 *
 */
public class CommonUtilTest extends TestCase {

    public void testEscape() {
        String target = "12.23@34$45%56^67&78*89("; //$NON-NLS-1$
        assertEquals("12%2e23%4034%2445%2556%5e67%2678%2a89%28", CommonUtil.escape(target)); //$NON-NLS-1$
        target = "abc.dbc.dgc"; //$NON-NLS-1$
        assertEquals("abc%2edbc%2edgc", CommonUtil.escape(target)); //$NON-NLS-1$
        target = "db/.dfe/cdfser.dfsdf"; //$NON-NLS-1$
        assertEquals("db%2f%2edfe%2fcdfser%2edfsdf", CommonUtil.escape(target)); //$NON-NLS-1$
    }

    public void testUnescape() {
        String target = "12%2e23%4034%2445%2556%5e67%2678%2a89%28"; //$NON-NLS-1$
        assertEquals("12.23@34$45%56^67&78*89(", CommonUtil.unescape(target)); //$NON-NLS-1$
        target = "abc%2edbc%2edgc"; //$NON-NLS-1$
        assertEquals("abc.dbc.dgc", CommonUtil.unescape(target)); //$NON-NLS-1$
        target = "db%2f%2edfe%2fcdfser%2edfsdf"; //$NON-NLS-1$
        assertEquals("db/.dfe/cdfser.dfsdf", CommonUtil.unescape(target)); //$NON-NLS-1$
    }

    public void testConvertListToString() {
        List<String> sourceList = new LinkedList<String>();
        sourceList.add("1!2@3#"); //$NON-NLS-1$
        sourceList.add("4$5%6^"); //$NON-NLS-1$
        sourceList.add("7&8*9(0)"); //$NON-NLS-1$
        assertEquals("1%212%403%23@4%245%256%5e@7%268%2a9%280%29", CommonUtil.convertListToString(sourceList, "@"));         //$NON-NLS-1$ //$NON-NLS-2$

        sourceList = new LinkedList<String>();
        sourceList.add("1!2@3#"); //$NON-NLS-1$
        sourceList.add("4$5;%6^"); //$NON-NLS-1$
        sourceList.add("7&8*9(0)"); //$NON-NLS-1$
        assertEquals("1!2@3#;4$5%3b%256^;7&8*9(0)", CommonUtil.convertListToString(sourceList));         //$NON-NLS-1$
    }

    public void testConvertStrigToList() {
        String sourceString = "1%212%403%23@4%245%256%5e@7%268%2a9%280%29"; //$NON-NLS-1$
        List<String> sourceList = CommonUtil.convertStrigToList(sourceString, "@"); //$NON-NLS-1$
        List<String> targetList = new ArrayList<String>();
        targetList.add("1!2@3#"); //$NON-NLS-1$
        targetList.add("4$5%6^"); //$NON-NLS-1$
        targetList.add("7&8*9(0)"); //$NON-NLS-1$
        assertEquals(sourceList.size(), targetList.size());
        for (int i = 0; i < sourceList.size(); i++) {
            assertEquals(targetList.get(i), sourceList.get(i));
        }

        sourceString = "1!2@3#;4$5%3b%256^;7&8*9(0)"; //$NON-NLS-1$
        sourceList = CommonUtil.convertStrigToList(sourceString);
        targetList = new ArrayList<String>();
        targetList.add("1!2@3#"); //$NON-NLS-1$
        targetList.add("4$5;%6^"); //$NON-NLS-1$
        targetList.add("7&8*9(0)"); //$NON-NLS-1$
        assertEquals(sourceList.size(), targetList.size());
        for (int i = 0; i < sourceList.size(); i++) {
            assertEquals(targetList.get(i), sourceList.get(i));
        }
    }

    public void testGetArgumentsWithXpath() {
        String function = "fn:concat(\"xpath:Product/Name\", \"xpath:Product/Description\")";
        Map<String, String> args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(2, args.size());
        assertTrue(args.containsKey("xpath:Product/Name"));
        assertTrue(args.containsKey("xpath:Product/Description"));
        assertEquals("Product/Name", args.get("xpath:Product/Name"));
        assertEquals("Product/Description", args.get("xpath:Product/Description"));

        function = "fn:concat(\"xpath:Product/Name\")";
        args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(1, args.size());
        assertTrue(args.containsKey("xpath:Product/Name"));
        assertEquals("Product/Name", args.get("xpath:Product/Name"));

        function = "fn:concat(\"xpath:Product/Name\", \" s\")";
        args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(1, args.size());
        assertTrue(args.containsKey("xpath:Product/Name"));
        assertEquals("Product/Name", args.get("xpath:Product/Name"));

        function = "fn:starts-with(\"xpath:Product/Name\",\"s\")";
        args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(1, args.size());
        assertTrue(args.containsKey("xpath:Product/Name"));
        assertEquals("Product/Name", args.get("xpath:Product/Name"));

        function = "fn:string-length(\"xpath:Product/Name\") > 3\")";
        args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(1, args.size());
        assertTrue(args.containsKey("xpath:Product/Name"));
        assertEquals("Product/Name", args.get("xpath:Product/Name"));

        function = "fn:matches(\"xpath:../Name\" ,\"test\")";
        args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(1, args.size());
        assertTrue(args.containsKey("xpath:../Name"));
        assertEquals("../Name", args.get("xpath:../Name"));

        function = "fn:concat(/Product/Name, /Product/Description)";
        args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(2, args.size());
        assertTrue(args.containsKey("/Product/Name"));
        assertTrue(args.containsKey("/Product/Description"));
        assertEquals("Product/Name", args.get("/Product/Name"));
        assertEquals("Product/Description", args.get("/Product/Description"));

        function = "fn:string-length(/BasicVisibleRuleWithFunctionXPath/name) > 5";
        args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(1, args.size());
        assertTrue(args.containsKey("/BasicVisibleRuleWithFunctionXPath/name"));
        assertEquals("BasicVisibleRuleWithFunctionXPath/name", args.get("/BasicVisibleRuleWithFunctionXPath/name"));

        function = "fn:string-length(\"xpath:/Product/Name\") > 3\")";
        args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(1, args.size());
        assertTrue(args.containsKey("xpath:/Product/Name"));
        assertEquals("Product/Name", args.get("xpath:/Product/Name"));

        function = "fn:concat(\"xpath:/Product/Name\", \"xpath:/Product/Description\")";
        args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(2, args.size());
        assertTrue(args.containsKey("xpath:/Product/Name"));
        assertTrue(args.containsKey("xpath:/Product/Description"));
        assertEquals("Product/Name", args.get("xpath:/Product/Name"));
        assertEquals("Product/Description", args.get("xpath:/Product/Description"));

        function = "fn:concat(&quot;xpath:/Caracteristique/Environnement&quot;, &quot;s&quot;)";
        args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(1, args.size());
        assertTrue(args.containsKey("xpath:/Caracteristique/Environnement"));
        assertEquals("Caracteristique/Environnement", args.get("xpath:/Caracteristique/Environnement"));

        function = "fn:concat(&quot;xpath:/Product/Name&quot;, &quot; &quot;)";
        args = CommonUtil.getArgumentsWithXpath(function);
        assertEquals(1, args.size());
        assertTrue(args.containsKey("xpath:/Product/Name"));
        assertEquals("Product/Name", args.get("xpath:/Product/Name"));
    }

    public void testContainsXPath() {
        assertFalse(CommonUtil.containsXPath("fn:concat('hello', 'world!!!')"));
        assertTrue(CommonUtil.containsXPath("fn:string-length(\"xpath:Product/Name\") > 3\")"));
        assertTrue(CommonUtil.containsXPath("fn:matches(\"xpath:../Name\" ,\"test\")"));
        assertTrue(CommonUtil.containsXPath("fn:starts-with(\"xpath:Product/Name\",\"s\")"));
        assertTrue(CommonUtil.containsXPath("fn:concat(\"xpath:Product/Name\", \" s\")"));
        assertTrue(CommonUtil.containsXPath("fn:string-length(/Product/Name) > 3)"));
        assertTrue(CommonUtil.containsXPath("fn:string-length(/BasicVisibleRuleWithFunctionXPath/name) > 5"));
        assertTrue(CommonUtil.containsXPath("fn:concat(\"xpath:Product/Name\", \"xpath:Product/Description\")"));
        assertTrue(CommonUtil.containsXPath("fn:concat(\"xpath:Product/Name\", \"/Product/Description\")"));
        assertFalse(CommonUtil.containsXPath("fn:concat(\"/Product/Name\", \"/Product/Description\")"));
        assertTrue(CommonUtil.containsXPath("fn:concat(/Product/Name\", /Product/Description\")"));
    }

}
