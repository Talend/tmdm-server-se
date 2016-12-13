/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.ejb;

import junit.framework.TestCase;


/**
 * DOC achen  class global comment. Detailled comment
 */
@SuppressWarnings("nls")
public class RoutingConditionTestCase extends TestCase {

    private static String compileCondition(String condition) {
        String compiled = condition;
        compiled = compiled.replaceAll("(\\s+)([aA][nN][dD])(\\s+|\\(+)", "$1&&$3"); //$NON-NLS-1$ //$NON-NLS-2$
        compiled = compiled.replaceAll("(\\s+)([oO][rR])(\\s+|\\(+)", "$1||$3"); //$NON-NLS-1$//$NON-NLS-2$
        compiled = compiled.replaceAll("(\\s*)([nN][oO][tT])(\\s+|\\(+)", "$1!$3"); //$NON-NLS-1$ //$NON-NLS-2$

        return compiled;
    }

    public void testRoutingRuleCondition() {
        String condition = "C1 And Not(C2)";
        condition = compileCondition(condition);
        assertEquals("C1 && !(C2)", condition);

        condition = "Not(C1) And C2";
        condition = compileCondition(condition);
        assertEquals("!(C1) && C2", condition);

        condition = "Not(C1) And Not(C2)";
        condition = compileCondition(condition);
        assertEquals("!(C1) && !(C2)", condition);

        condition = "C1 and C2 And C3";
        condition = compileCondition(condition);
        assertEquals("C1 && C2 && C3", condition);

        condition = "(C1 And C2) or C3";
        condition = compileCondition(condition);
        assertEquals("(C1 && C2) || C3", condition);

        condition = "C1 and (C2 OR C3)";
        condition = compileCondition(condition);
        assertEquals("C1 && (C2 || C3)", condition);

        condition = "Store and (C2 or C3)";
        condition = compileCondition(condition);
        assertEquals("Store && (C2 || C3)", condition);

        condition = "Pand and (C2 or sort)";
        condition = compileCondition(condition);
        assertEquals("Pand && (C2 || sort)", condition);

        condition = "Noth and (C2 or sort)";
        condition = compileCondition(condition);
        assertEquals("Noth && (C2 || sort)", condition);

        condition = "workflow and andsave and notgood";
        condition = compileCondition(condition);
        assertEquals("workflow && andsave && notgood", condition);
    }

}
