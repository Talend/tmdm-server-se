// ============================================================================
//
// Copyright (C) 2006-2012 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.base.client.util;

import junit.framework.TestCase;

@SuppressWarnings("nls")
public class FormatUtilTest extends TestCase {

    public void testLanguageValueEncode() {
        String value = "Talend[]";
        assertEquals("Talend&amp;#91;&amp;#93;", FormatUtil.languageValueEncode(value));
        value = "Talend\\China";
        assertEquals("Talend&amp;#92;China", FormatUtil.languageValueEncode(value));
        value = "Talend\\China[]";
        assertEquals("Talend&amp;#92;China&amp;#91;&amp;#93;", FormatUtil.languageValueEncode(value));
    }

    public void testLanguageValueDecode() {
        String value = "Talend&amp;#91;&amp;#93;";
        assertEquals("Talend[]", FormatUtil.languageValueDecode(value));
        value = "Talend&amp;#92;China";
        assertEquals("Talend\\China", FormatUtil.languageValueDecode(value));
        value = "Talend&amp;#92;China&amp;#91;&amp;#93;";
        assertEquals("Talend\\China[]", FormatUtil.languageValueDecode(value));
    }

}
