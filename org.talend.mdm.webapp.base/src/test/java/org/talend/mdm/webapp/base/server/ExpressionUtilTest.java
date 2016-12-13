/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.server;

import java.util.List;

import junit.framework.TestCase;

import org.talend.mdm.webapp.base.shared.ExpressionUtil;

public class ExpressionUtilTest extends TestCase {

    public void test_getDepTypes() {
        // 1. xpath = "/Test/subelement"
        String expression = "fn:starts-with(/Test/subelement,liang)"; //$NON-NLS-1$
        ExpressionUtil expressionUtil = new ExpressionUtil(expression);
        List<String> typePathes = expressionUtil.getDepTypes();
        assertEquals(1, typePathes.size());
        assertEquals("Test/subelement", typePathes.get(0)); //$NON-NLS-1$
        // 2. xpath = "Test/subelement"
        expression = "fn:starts-with(Test/subelement,liang)"; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(1, typePathes.size());
        assertEquals("subelement", typePathes.get(0)); //$NON-NLS-1$

        expression = "fn:starts-with(/Test/subelement,../subelement)"; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(2, typePathes.size());
        assertEquals("Test/subelement", typePathes.get(0)); //$NON-NLS-1$
        assertEquals("../subelement", typePathes.get(1)); //$NON-NLS-1$

        expression = "/Test/subelement"; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(1, typePathes.size());
        assertEquals("Test/subelement", typePathes.get(0)); //$NON-NLS-1$

        expression = "../subelement"; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(1, typePathes.size());
        assertEquals("../subelement", typePathes.get(0)); //$NON-NLS-1$

        expression = "./subelement"; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(1, typePathes.size());
        assertEquals("./subelement", typePathes.get(0)); //$NON-NLS-1$

        expression = "1 + ./subelement"; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(1, typePathes.size());
        assertEquals("./subelement", typePathes.get(0)); //$NON-NLS-1$

        expression = "./subelement + 1"; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(1, typePathes.size());
        assertEquals("./subelement", typePathes.get(0)); //$NON-NLS-1$

        expression = "./Test/cp[1]/subelement = 'zhang'"; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(1, typePathes.size());
        assertEquals("./Test/cp/subelement", typePathes.get(0)); //$NON-NLS-1$

        expression = "./Test/cp[position()=count(/Test/nodes)]/subelement[5] = 'zhang' and /Test/cp[2]/name[4] = /Test/subelement[6]"; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(3, typePathes.size());
        assertEquals("./Test/cp/subelement", typePathes.get(0)); //$NON-NLS-1$
        assertEquals("Test/cp/name", typePathes.get(1)); //$NON-NLS-1$
        assertEquals("Test/subelement", typePathes.get(2)); //$NON-NLS-1$

        expression = "./Test/cp[1]/subelement = 'zhang' or ./Test/subelement = \"wang\""; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(2, typePathes.size());
        assertEquals("./Test/cp/subelement", typePathes.get(0)); //$NON-NLS-1$
        assertEquals("./Test/subelement", typePathes.get(1)); //$NON-NLS-1$

        expression = "'zhang'"; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(0, typePathes.size());

        expression = "    'zhang'"; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(0, typePathes.size());

        expression = "\"zhang\""; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(0, typePathes.size());

        expression = "    \"zhang\""; //$NON-NLS-1$
        expressionUtil = new ExpressionUtil(expression);
        typePathes = expressionUtil.getDepTypes();
        assertEquals(0, typePathes.size());

    }

    public void test_regex() {
        // 1. xpath = "/Test/subelement[1]"
        String xpath = "/Test/subelement[1]"; //$NON-NLS-1$
        String typePath = xpath.replaceAll("\\[\\d+\\]", ""); //$NON-NLS-1$//$NON-NLS-2$
        assertEquals("/Test/subelement", typePath); //$NON-NLS-1$
        // 2. xpath = "/Test/subelement"
        xpath = "/Test/subelement"; //$NON-NLS-1$
        typePath = xpath.replaceAll("\\[\\d+\\]", ""); //$NON-NLS-1$//$NON-NLS-2$
        assertEquals("/Test/subelement", typePath); //$NON-NLS-1$
    }

}
