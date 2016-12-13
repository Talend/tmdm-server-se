/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client;

import junit.framework.Test;
import junit.framework.TestCase;

import com.google.gwt.junit.tools.GWTTestSuite;

@SuppressWarnings("nls")
public class ClientGWTTestSuite extends TestCase /* note this is TestCase and not TestSuite */
{

    public static Test suite() {
        GWTTestSuite suite = new GWTTestSuite("All Gwt Tests go in here");
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.BrowseRecordsGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.creator.ItemCreatorGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.inputfield.celleditor.FormatCellEditorGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatDateFieldGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.inputfield.UrlFieldGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.treedetail.IncrementalBuildTreeGWTTest.class);
        // suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.foreignkey.ForeignKeyListWindowGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.treedetail.FieldCreatorGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.treedetail.SortSubTypesGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldCreatorGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.typefield.DateTimeTypeFieldFactoryGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.typefield.MiscTypeFieldFactoryGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandlerGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.util.ItemNodeModelGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.util.MultiOccurrenceManagerGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.util.ViewUtilGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.util.MockGridRefreshGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.util.CommonUtilGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.util.CallJsniGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.rest.ExplainRestServiceHandlerGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.foreignkey.ForeignKeySelectorGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.ItemsToolBarGWTTest.class);
        suite.addTestSuite(org.talend.mdm.webapp.browserecords.client.widget.treedetail.MultiOccurrenceChangeItemGWTTest.class);
        return suite;
    }
}
