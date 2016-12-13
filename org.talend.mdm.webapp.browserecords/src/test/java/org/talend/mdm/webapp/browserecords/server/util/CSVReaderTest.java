/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import junit.framework.TestCase;

public class CSVReaderTest extends TestCase {

    private StringReader testReader;

    private char separator;

    private char delimiter;

    private CSVReader reader;

    public void testReadResultWithComma() throws Exception {
        // init test data
        separator = ',';
        delimiter = '\'';
        StringBuffer testString = new StringBuffer("1,'1,'',2'"); //$NON-NLS-1$
        testString.append("\n"); //$NON-NLS-1$
        testString.append("2,'benz,b'"); //$NON-NLS-1$
        testString.append("\n"); //$NON-NLS-1$
        testString.append("3,'audi,'','',c'"); //$NON-NLS-1$
        testString.append("\n"); //$NON-NLS-1$
        testString.append("4,honda"); //$NON-NLS-1$
        testString.append("\n"); //$NON-NLS-1$        
        reader = new CSVReader(new StringReader(testString.toString()), separator, delimiter);

        // testing
        List<String[]> results = new ArrayList<String[]>();
        results.add(new String[] { "1", "1,',2" }); //$NON-NLS-1$//$NON-NLS-2$
        results.add(new String[] { "2", "benz,b" }); //$NON-NLS-1$//$NON-NLS-2$
        results.add(new String[] { "3", "audi,',',c" }); //$NON-NLS-1$//$NON-NLS-2$
        results.add(new String[] { "4", "honda" }); //$NON-NLS-1$//$NON-NLS-2$

        String[] result;
        String[] record;
        List<String[]> records = reader.readAll();
        for (int i = 0; i < records.size(); i++) {
            record = records.get(i);
            result = results.get(i);
            for (int j = 0; j < record.length; j++) {
                Assert.assertEquals(record[j], result[j]);
            }
        }

        // destory
        reader = null;
        testString = null;
    }

    public void testReadResultWithSemicolon() throws Exception {
        // init test data
        separator = ';';
        delimiter = '"';
        StringBuffer testString = new StringBuffer("1;\"1;\"\";2\""); //$NON-NLS-1$
        testString.append("\n"); //$NON-NLS-1$
        testString.append("2;\"benz;b\""); //$NON-NLS-1$
        testString.append("\n"); //$NON-NLS-1$
        testString.append("3;\"audi;c\""); //$NON-NLS-1$
        testString.append("\n"); //$NON-NLS-1$
        testString.append("4;honda"); //$NON-NLS-1$
        testString.append("\n"); //$NON-NLS-1$        
        reader = new CSVReader(new StringReader(testString.toString()), separator, delimiter);

        // testing
        List<String[]> results = new ArrayList<String[]>();
        results.add(new String[] { "1", "1;\";2" }); //$NON-NLS-1$//$NON-NLS-2$
        results.add(new String[] { "2", "benz;b" }); //$NON-NLS-1$//$NON-NLS-2$
        results.add(new String[] { "3", "audi;c" }); //$NON-NLS-1$//$NON-NLS-2$
        results.add(new String[] { "4", "honda" }); //$NON-NLS-1$//$NON-NLS-2$

        String[] result;
        String[] record;
        List<String[]> records = reader.readAll();
        for (int i = 0; i < records.size(); i++) {
            record = records.get(i);
            result = results.get(i);
            for (int j = 0; j < record.length; j++) {
                Assert.assertEquals(record[j], result[j]);
            }
        }

        // destory
        reader = null;
        testString = null;
    }
}
