/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.util;

import junit.framework.TestCase;

/**
 * DOC talend2 class global comment. Detailled comment
 */
public class XmlUtilTest extends TestCase {

    public void testTransformXmlAndString() {
        String xml = "<MultipleCriteria><appearance>true</appearance><SimpleCriterion><key>Product/Id</key><operator>EQUALS</operator><value>*</value><info>*</info></SimpleCriterion><operator>AND</operator><SimpleCriterion><key>Product/Name</key><operator>EQUALS</operator><value>*</value><info>*</info></SimpleCriterion><operator>AND</operator><MultipleCriteria><appearance>true</appearance><SimpleCriterion><key>Product/Description</key><operator>EQUALS</operator><value>*</value><info>*</info></SimpleCriterion><operator>AND</operator><SimpleCriterion><key>Product/Features</key><operator>FULLTEXTSEARCH</operator><value>*</value><info>*</info></SimpleCriterion><operator>AND</operator><MultipleCriteria><appearance>true</appearance><SimpleCriterion><key>Product/Id</key><operator>EQUALS</operator><value>1</value><info>1</info></SimpleCriterion><operator>AND</operator><SimpleCriterion><key>Product/Name</key><operator>EQUALS</operator><value>2</value><info>2</info></SimpleCriterion></MultipleCriteria></MultipleCriteria></MultipleCriteria>"; //$NON-NLS-1$
        String value = "&lt;MultipleCriteria&gt;&lt;appearance&gt;true&lt;/appearance&gt;&lt;SimpleCriterion&gt;&lt;key&gt;Product/Id&lt;/key&gt;&lt;operator&gt;EQUALS&lt;/operator&gt;&lt;value&gt;*&lt;/value&gt;&lt;info&gt;*&lt;/info&gt;&lt;/SimpleCriterion&gt;&lt;operator&gt;AND&lt;/operator&gt;&lt;SimpleCriterion&gt;&lt;key&gt;Product/Name&lt;/key&gt;&lt;operator&gt;EQUALS&lt;/operator&gt;&lt;value&gt;*&lt;/value&gt;&lt;info&gt;*&lt;/info&gt;&lt;/SimpleCriterion&gt;&lt;operator&gt;AND&lt;/operator&gt;&lt;MultipleCriteria&gt;&lt;appearance&gt;true&lt;/appearance&gt;&lt;SimpleCriterion&gt;&lt;key&gt;Product/Description&lt;/key&gt;&lt;operator&gt;EQUALS&lt;/operator&gt;&lt;value&gt;*&lt;/value&gt;&lt;info&gt;*&lt;/info&gt;&lt;/SimpleCriterion&gt;&lt;operator&gt;AND&lt;/operator&gt;&lt;SimpleCriterion&gt;&lt;key&gt;Product/Features&lt;/key&gt;&lt;operator&gt;FULLTEXTSEARCH&lt;/operator&gt;&lt;value&gt;*&lt;/value&gt;&lt;info&gt;*&lt;/info&gt;&lt;/SimpleCriterion&gt;&lt;operator&gt;AND&lt;/operator&gt;&lt;MultipleCriteria&gt;&lt;appearance&gt;true&lt;/appearance&gt;&lt;SimpleCriterion&gt;&lt;key&gt;Product/Id&lt;/key&gt;&lt;operator&gt;EQUALS&lt;/operator&gt;&lt;value&gt;1&lt;/value&gt;&lt;info&gt;1&lt;/info&gt;&lt;/SimpleCriterion&gt;&lt;operator&gt;AND&lt;/operator&gt;&lt;SimpleCriterion&gt;&lt;key&gt;Product/Name&lt;/key&gt;&lt;operator&gt;EQUALS&lt;/operator&gt;&lt;value&gt;2&lt;/value&gt;&lt;info&gt;2&lt;/info&gt;&lt;/SimpleCriterion&gt;&lt;/MultipleCriteria&gt;&lt;/MultipleCriteria&gt;&lt;/MultipleCriteria&gt;"; //$NON-NLS-1$
        assertEquals(xml, XmlUtil.transformStringToXml(value));
        assertEquals(value, XmlUtil.transformXmlToString(xml));
    }
}
