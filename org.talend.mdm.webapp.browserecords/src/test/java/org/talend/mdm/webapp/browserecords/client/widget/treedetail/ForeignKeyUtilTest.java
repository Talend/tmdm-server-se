/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 *  %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.junit.client.GWTTestCase;
import org.junit.Test;
import org.talend.mdm.webapp.base.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;

import static org.junit.Assert.*;

public class ForeignKeyUtilTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords";
    }

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
    }

    @Test
    public void testFindTarget() {
        String targetPath = "Caracteristique/Format_Caracteristique/IdTypeValeur"; //$NON-NLS-1$
        ItemNodeModel itemNodeModel = new ItemNodeModel();
        itemNodeModel.setLabel("Format_Caracteristique"); //$NON-NLS-1$
        itemNodeModel.setRealType("Caracteristique"); //$NON-NLS-1$
        itemNodeModel.setTypePath("Caracteristique/Format_Caracteristique"); //$NON-NLS-1$

        ItemNodeModel itemNodeModel1 = new ItemNodeModel();
        itemNodeModel1.setLabel("IdTypeValeur"); //$NON-NLS-1$
        itemNodeModel1.setTypePath("Caracteristique/Format_Caracteristique:Caracteristique_Liste/IdTypeValeur"); //$NON-NLS-1$
        ItemNodeModel itemNodeModel2 = new ItemNodeModel();
        itemNodeModel2.setLabel("Multiple"); //$NON-NLS-1$
        itemNodeModel2.setTypePath("Caracteristique/Format_Caracteristique:Caracteristique_Liste/Multiple"); //$NON-NLS-1$
        ItemNodeModel itemNodeModel3 = new ItemNodeModel();
        itemNodeModel3.setLabel("ValeursCaracteristique"); //$NON-NLS-1$
        itemNodeModel3.setTypePath("Caracteristique/Format_Caracteristique:Caracteristique_Liste/ValeursCaracteristique"); //$NON-NLS-1$
        itemNodeModel.getChildren().add(itemNodeModel1);
        itemNodeModel.getChildren().add(itemNodeModel2);
        itemNodeModel.getChildren().add(itemNodeModel3);

        ItemNodeModel targetNodeModel = ForeignKeyUtil.findTarget(targetPath, itemNodeModel);
        assertNotNull(targetNodeModel);
        assertEquals("IdTypeValeur", targetNodeModel.getLabel()); //$NON-NLS-1$
        assertEquals("Caracteristique/Format_Caracteristique:Caracteristique_Liste/IdTypeValeur", targetNodeModel.getTypePath()); //$NON-NLS-1$

        targetPath = "Caracteristique/Format_Caracteristique[@xsi:type=\"Caracteristique_Liste\"]/IdTypeValeur"; //$NON-NLS-1$
        targetNodeModel = ForeignKeyUtil.findTarget(targetPath, itemNodeModel);
        assertNotNull(targetNodeModel);
        assertEquals("IdTypeValeur", targetNodeModel.getLabel()); //$NON-NLS-1$
        assertEquals("Caracteristique/Format_Caracteristique:Caracteristique_Liste/IdTypeValeur", targetNodeModel.getTypePath()); //$NON-NLS-1$
    }

    @Test
    public void testTransformPath() {
        String[] pathArray = { "Caracteristique", "Format_Caracteristique", "IdTypeValeur" }; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
        assertEquals("Caracteristique/Format_Caracteristique/IdTypeValeur", ForeignKeyUtil.transformPath(pathArray)); //$NON-NLS-1$
    }
}