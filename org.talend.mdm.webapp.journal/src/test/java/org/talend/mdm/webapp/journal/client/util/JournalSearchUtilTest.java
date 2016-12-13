/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.client.util;

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.talend.mdm.webapp.journal.shared.JournalGridModel;
import org.talend.mdm.webapp.journal.shared.JournalParameters;

/**
 * created by yjli on 2013-4-25
 * Detailled comment
 *
 */
public class JournalSearchUtilTest extends TestCase {
    
    public void testBuildParameterWith_id_contains_dot() {
        String action = "current"; //$NON-NLS-1$
        boolean auth = true;
        JournalGridModel gridModel = new JournalGridModel();
        gridModel.setDataContainer("Product-Container"); //$NON-NLS-1$
        gridModel.setDataModel("Product-Model"); //$NON-NLS-1$
        gridModel.setEntity("Product-Entity"); //$NON-NLS-1$
        gridModel.setOperationTime("1381903894501"); //$NON-NLS-1$
        gridModel.setIds("genericUI.1381903894501"); //$NON-NLS-1$
        gridModel.setKey("3.5.6"); //$NON-NLS-1$
        
        JournalParameters parameter = JournalSearchUtil.buildParameter(gridModel, action, auth);
        assertEquals(parameter.getDataClusterName(), gridModel.getDataContainer());
        assertEquals(parameter.getDataModelName(), gridModel.getDataModel());
        assertEquals(parameter.getConceptName(), gridModel.getEntity());
        assertEquals(parameter.getDate(), Long.parseLong(gridModel.getOperationTime()));
        assertEquals(parameter.getIds(), gridModel.getIds());
        assertArrayEquals(parameter.getId(), new String[] {"3","5","6"});  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        assertEquals(parameter.getAction(), action);
        assertEquals(parameter.isAuth(), auth);
    }
    
    public void testBuildParameterWith_id_startsWith_dot() {
        String action = "current"; //$NON-NLS-1$
        boolean auth = true;
        JournalGridModel gridModel = new JournalGridModel();
        gridModel.setDataContainer("Product-Container"); //$NON-NLS-1$
        gridModel.setDataModel("Product-Model"); //$NON-NLS-1$
        gridModel.setEntity("Product-Entity"); //$NON-NLS-1$
        gridModel.setOperationTime("1381903894501"); //$NON-NLS-1$
        gridModel.setIds("genericUI.1381903894501"); //$NON-NLS-1$
        gridModel.setKey(".3.5.6"); //$NON-NLS-1$
        
        JournalParameters parameter = JournalSearchUtil.buildParameter(gridModel, action, auth);
        assertEquals(parameter.getDataClusterName(), gridModel.getDataContainer());
        assertEquals(parameter.getDataModelName(), gridModel.getDataModel());
        assertEquals(parameter.getConceptName(), gridModel.getEntity());
        assertEquals(parameter.getDate(), Long.parseLong(gridModel.getOperationTime()));
        assertEquals(parameter.getIds(), gridModel.getIds());
        assertArrayEquals(parameter.getId(), new String[] {"","3","5","6"});   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$  //$NON-NLS-4$
        assertEquals(parameter.getAction(), action);
        assertEquals(parameter.isAuth(), auth);
    }
    
    public void testBuildParameterWith_id_EndsWith_dot() {
        String action = "current"; //$NON-NLS-1$
        boolean auth = true;
        JournalGridModel gridModel = new JournalGridModel();
        gridModel.setDataContainer("Product-Container"); //$NON-NLS-1$
        gridModel.setDataModel("Product-Model"); //$NON-NLS-1$
        gridModel.setEntity("Product-Entity"); //$NON-NLS-1$
        gridModel.setOperationTime("1381903894501"); //$NON-NLS-1$
        gridModel.setIds("genericUI.1381903894501"); //$NON-NLS-1$
        gridModel.setKey(".3.5.6."); //$NON-NLS-1$
        
        JournalParameters parameter = JournalSearchUtil.buildParameter(gridModel, action, auth);
        assertEquals(parameter.getDataClusterName(), gridModel.getDataContainer());
        assertEquals(parameter.getDataModelName(), gridModel.getDataModel());
        assertEquals(parameter.getConceptName(), gridModel.getEntity());
        assertEquals(parameter.getDate(), Long.parseLong(gridModel.getOperationTime()));
        assertEquals(parameter.getIds(), gridModel.getIds());
        assertArrayEquals(parameter.getId(), new String[] {"","3","5","6",""});   //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$  //$NON-NLS-4$   //$NON-NLS-5$
        assertEquals(parameter.getAction(), action);
        assertEquals(parameter.isAuth(), auth);
    }
}
