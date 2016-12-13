/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.widget;

import org.talend.mdm.webapp.base.client.model.MultiLanguageModel;
import org.talend.mdm.webapp.base.shared.OperatorValueConstants;

import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class MultiLanguageFieldGWTTest extends GWTTestCase {

    private MultiLanguageField field;

    private String value;

    private String operator;

    @Override
    protected void gwtSetUp() throws Exception {
        field = new MultiLanguageField();
        value = "Talend";
        field.setValue(value);
        super.gwtSetUp();
    }

    public void testGetMultiLanguageKeywordsValue() {
        // 1. operator = CONTAINS
        operator = OperatorValueConstants.CONTAINS;
        String formatValue = field.getMultiLanguageKeywordsValue(operator);
        assertEquals("Talend", formatValue);
        // 2. operator = STARTSWITH
        operator = OperatorValueConstants.STARTSWITH;
        formatValue = field.getMultiLanguageKeywordsValue(operator);
        assertEquals("%:Talend", formatValue);
        // 3. value = Talend[] operator = CONTAINS
        value = "Talend[]";
        field.setValue(value);
        operator = OperatorValueConstants.CONTAINS;
        formatValue = field.getMultiLanguageKeywordsValue(operator);
        assertEquals("Talend&amp;#91;&amp;#93;", formatValue);
        // 4. value = Talend\\
        value = "Talend\\";
        field.setValue(value);
        formatValue = field.getMultiLanguageKeywordsValue(operator);
        assertEquals("Talend&amp;#92;", formatValue);
        // 5. value = Talend\\China[]
        value = "Talend\\China[]";
        field.setValue(value);
        formatValue = field.getMultiLanguageKeywordsValue(operator);
        assertEquals("Talend&amp;#92;China&amp;#91;&amp;#93;", formatValue);
    }

    public void testGetInputValue() {
        // 1. operator = CONTAINS
        operator = OperatorValueConstants.CONTAINS;
        String formatValue = "Talend";
        String inputValue = field.getInputValue(operator, formatValue);
        assertEquals("Talend", inputValue);
        // 2. operator = STARTSWITH
        operator = OperatorValueConstants.STARTSWITH;
        formatValue = "%:Talend";
        inputValue = field.getInputValue(operator, formatValue);
        assertEquals("Talend", inputValue);
        // 3. operator = CONTAINS
        formatValue = "*Talend&amp;#91;&amp;#93;*";
        operator = OperatorValueConstants.CONTAINS;
        inputValue = field.getInputValue(operator, formatValue);
        assertEquals("*Talend&amp;#91;&amp;#93;*", inputValue);
        // 4. formatValue = *[EN:*Talend&#92;*]*
        formatValue = "Talend&amp;#92;";
        inputValue = field.getInputValue(operator, formatValue);
        assertEquals("Talend&amp;#92;", inputValue);
        // 5. formatValue = *[EN:*Talend&#92;China&#91;&#93;*]*
        formatValue = "*Talend&amp;#92;China&amp;#91;&amp;#93;*";
        inputValue = field.getInputValue(operator, formatValue);
        assertEquals("*Talend&amp;#92;China&amp;#91;&amp;#93;*", inputValue);
    }

    public void testClear() {
        String multiLanguageString = "[EN:Talend England][FR:Talend Franch]";
        MultiLanguageModel multiLanguageModel = new MultiLanguageModel(multiLanguageString);
        field = new MultiLanguageField(multiLanguageModel);
        field.setValue("Talend England");
        assertEquals(multiLanguageString, field.getMultiLanguageStringValue());
        // clear
        field.clear();
        assertTrue(field.getMultiLanguageStringValue().trim().length() == 0);
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.base.TestBase";
    }

}
