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

import org.talend.mdm.webapp.base.client.model.MultipleCriteria;
import org.talend.mdm.webapp.base.client.model.SimpleCriterion;

/**
 * DOC talend2 class global comment. Detailled comment
 */
public class MultipleCriteriaTest extends TestCase {

    public void testToXmlString() {
        String xmlResult = "<MultipleCriteria><appearance>false</appearance><operator>AND</operator><SimpleCriterion><key>S1</key><operator>EQUALS</operator><value>*</value><info>S6_INFO</info></SimpleCriterion><SimpleCriterion><key>S2</key><operator>EQUALS</operator><value>*</value><info>S2_INFO</info></SimpleCriterion><MultipleCriteria><appearance>false</appearance><operator>AND</operator><SimpleCriterion><key>S3</key><operator>EQUALS</operator><value>*</value><info>S3_INFO</info></SimpleCriterion><SimpleCriterion><key>S4</key><operator>EQUALS</operator><value>*</value><info>S4_INFO</info></SimpleCriterion><MultipleCriteria><appearance>false</appearance><operator>AND</operator><SimpleCriterion><key>S5</key><operator>EQUALS</operator><value>*</value><info>null</info></SimpleCriterion><SimpleCriterion><key>S6</key><operator>EQUALS</operator><value>*</value><info>null</info></SimpleCriterion></MultipleCriteria></MultipleCriteria></MultipleCriteria>"; //$NON-NLS-1$
        MultipleCriteria multipleCriteria1 = new MultipleCriteria("AND"); //$NON-NLS-1$ 
        SimpleCriterion SimpleCriterion1 = new SimpleCriterion("S1", "EQUALS", "*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SimpleCriterion1.setInfo("S1_INFO"); //$NON-NLS-1$
        SimpleCriterion SimpleCriterion2 = new SimpleCriterion("S2", "EQUALS", "*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SimpleCriterion2.setInfo("S2_INFO"); //$NON-NLS-1$
        multipleCriteria1.getChildren().add(SimpleCriterion1);
        multipleCriteria1.getChildren().add(SimpleCriterion2);
        MultipleCriteria multipleCriteria2 = new MultipleCriteria("AND"); //$NON-NLS-1$
        multipleCriteria2.setOperator("AND"); //$NON-NLS-1$
        SimpleCriterion SimpleCriterion3 = new SimpleCriterion("S3", "EQUALS", "*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SimpleCriterion3.setInfo("S3_INFO"); //$NON-NLS-1$
        SimpleCriterion SimpleCriterion4 = new SimpleCriterion("S4", "EQUALS", "*"); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
        SimpleCriterion4.setInfo("S4_INFO"); //$NON-NLS-1$
        multipleCriteria2.getChildren().add(SimpleCriterion3);
        multipleCriteria2.getChildren().add(SimpleCriterion4);
        MultipleCriteria multipleCriteria3 = new MultipleCriteria("AND"); //$NON-NLS-1$
        SimpleCriterion SimpleCriterion5 = new SimpleCriterion("S5", "EQUALS", "*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SimpleCriterion1.setInfo("S5_INFO"); //$NON-NLS-1$
        SimpleCriterion SimpleCriterion6 = new SimpleCriterion("S6", "EQUALS", "*"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        SimpleCriterion1.setInfo("S6_INFO"); //$NON-NLS-1$
        multipleCriteria3.getChildren().add(SimpleCriterion5);
        multipleCriteria3.getChildren().add(SimpleCriterion6);
        multipleCriteria2.getChildren().add(multipleCriteria3);
        multipleCriteria1.getChildren().add(multipleCriteria2);
        assertEquals(xmlResult, multipleCriteria1.toXmlString());
    }
}
