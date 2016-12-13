/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.commmon.util.core;

import java.io.File;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.talend.mdm.commmon.util.core.EncryptUtil;
import org.talend.mdm.commmon.util.core.MDMConfiguration;

import junit.framework.TestCase;

@SuppressWarnings({ "nls" })
public class EncryptUtilTest extends TestCase {

    @Test
    public void testEncypt() throws Exception {
        String path = getClass().getResource("mdm.conf").getFile();
        path = StringUtils.substringBefore(path, "mdm.conf");
        EncryptUtil.encrypt(path);

        File confFile = new File(path + "mdm.conf");
        PropertiesConfiguration confConfig = new PropertiesConfiguration();
        confConfig.setDelimiterParsingDisabled(true);
        confConfig.load(confFile);
        assertEquals("aYfBEdcXYP3t9pofaispXA==,Encrypt", confConfig.getString(MDMConfiguration.ADMIN_PASSWORD));
        assertEquals("tKyTop7U6czAJKGTd9yWRA==,Encrypt", confConfig.getString(MDMConfiguration.TECHNICAL_PASSWORD));
        assertEquals("DlqU02M503JUOVBeup29+w==,Encrypt", confConfig.getString(EncryptUtil.ACTIVEMQ_PASSWORD));

        File tdscFile = new File(path + "tdsc-database.properties");
        PropertiesConfiguration tdscConfig = new PropertiesConfiguration();
        tdscConfig.setDelimiterParsingDisabled(true);
        tdscConfig.load(tdscFile);
        assertEquals("yzuBTeQahXQS7ts8Dh6zeQ==,Encrypt", tdscConfig.getString(EncryptUtil.TDSC_DATABASE_PASSWORD));

        File datasource = new File(path + "datasources.xml");
        XMLConfiguration config = new XMLConfiguration();
        config.setDelimiterParsingDisabled(true);
        config.load(datasource);

        HierarchicalConfiguration sub = config.configurationAt("datasource(0)");
        String password = sub.getString("master.rdbms-configuration.connection-password");
        assertEquals("sa", password);
        password = sub.getString("master.rdbms-configuration.init.connection-password");
        assertNull(password);

        sub = config.configurationAt("datasource(1)");
        password = sub.getString("master.rdbms-configuration.connection-password");
        assertEquals("+WNho+eyvY2IdYENFaoKIA==,Encrypt", password);
        password = sub.getString("master.rdbms-configuration.init.connection-password");
        assertEquals("+WNho+eyvY2IdYENFaoKIA==,Encrypt", password);

    }
}
