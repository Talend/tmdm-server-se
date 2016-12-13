/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox;

import java.util.Properties;

import com.amalto.core.jobox.properties.StandardPropertiesStrategyFactory;
import junit.framework.TestCase;

public class DefaultSystemPropertiesTest extends TestCase {

    public void testDefaultSystemProperties() throws Exception {
        Properties standardProperties = StandardPropertiesStrategyFactory.create().getStandardProperties();

        assertTrue(standardProperties.containsKey("java.runtime.name"));
        assertTrue(standardProperties.containsKey("sun.jnu.encoding"));
        assertFalse(standardProperties.containsKey("javax.xml.parsers.DocumentBuilderFactory"));
    }
}
