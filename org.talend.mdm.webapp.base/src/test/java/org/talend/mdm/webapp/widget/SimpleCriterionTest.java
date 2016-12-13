/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.widget;

import junit.framework.TestCase;

import org.talend.mdm.webapp.base.client.model.SimpleCriterion;

/**
 * DOC talend2 class global comment. Detailled comment
 */
public class SimpleCriterionTest extends TestCase {

    public void testToXmlString() {
        String xmlResult = "<SimpleCriterion><key>Product</key><operator>EQUALS</operator><value>*</value><info>*</info></SimpleCriterion>"; //$NON-NLS-1$
        SimpleCriterion simpleCriterion = new SimpleCriterion("Product", "EQUALS", "*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        simpleCriterion.setInfo("*"); //$NON-NLS-1$
        assertEquals(xmlResult, simpleCriterion.toXmlString());
    }
}
