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

import java.io.IOException;

import junit.framework.TestCase;

import com.amalto.core.jobox.util.JoboxUtil;

/**
 * DOC Starkey  class global comment. Detailled comment
 */
@SuppressWarnings("nls")
public class JoboxUtilTest extends TestCase {

    /**
     * DOC Starkey Comment method "testParseMainClassFromJCL".
     */
    public void testParseMainClassFromJCL() {

        try {
            // sanity check
            String content = "java -Xms256M -Xmx1024M -cp classpath.jar; tests.testtalendmdmjob_0_1.TestTalendMDMJob --context=Default";
            String mainClass = JoboxUtil.parseMainClassFromJCL(content);
            assertEquals("tests.testtalendmdmjob_0_1.TestTalendMDMJob", mainClass);

            // multi lines
            content = "%~d0\ncd %~dp0\n java -Xms256M -Xmx1024M -cp classpath.jar; tests.testtalendmdmjob_0_1.TestTalendMDMJob --context=Default %*";
            mainClass = JoboxUtil.parseMainClassFromJCL(content);
            assertEquals("tests.testtalendmdmjob_0_1.TestTalendMDMJob", mainClass);

            // change order
            content = "%~d0\ncd %~dp0\njava -cp classpath.jar; tests.testtalendmdmjob_0_1.TestTalendMDMJob -Xms256M -Xmx1024M --context=Default %*";
            mainClass = JoboxUtil.parseMainClassFromJCL(content);
            assertEquals("tests.testtalendmdmjob_0_1.TestTalendMDMJob", mainClass);

            // different options
            content = "%~d0\ncd %~dp0\njava -Xms256M -classpath classpath.jar; tests.testtalendmdmjob_0_1.TestTalendMDMJob -Dfoo=\"some string\" --context=Default %*";
            mainClass = JoboxUtil.parseMainClassFromJCL(content);
            assertEquals("tests.testtalendmdmjob_0_1.TestTalendMDMJob", mainClass);

            // wrong command
            content = "java -cp tests.testtalendmdmjob_0_1.TestTalendMDMJob --context=Default ";
            mainClass = JoboxUtil.parseMainClassFromJCL(content);
            assertNull(mainClass);

        } catch (IOException e) {
            e.printStackTrace();
            assertFalse(true);
        }

    }

}
