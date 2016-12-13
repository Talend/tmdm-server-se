/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import java.util.HashMap;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ComboBoxModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.ComboBoxField;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class SortSubTypesGWTTest extends GWTTestCase {

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }
    
    public void testSortSubTypes() {
        ComplexTypeModel dataType = new ComplexTypeModel("typeEDA", DataTypeConstants.STRING);

        ComplexTypeModel typeEDA = new ComplexTypeModel("typeEDA", null);
        typeEDA.setOrderValue(0);
        dataType.addComplexReusableTypes(typeEDA);

        ComplexTypeModel balit = new ComplexTypeModel("Balit", null); 
        balit.setOrderValue(1);
        dataType.addComplexReusableTypes(balit);

        ComplexTypeModel pointEchange = new ComplexTypeModel("PointEchange", null); 
        pointEchange.setOrderValue(3);
        dataType.addComplexReusableTypes(pointEchange);

        ComplexTypeModel secoursMutuelGrt = new ComplexTypeModel("SecoursMutuelGrt", null); 
        secoursMutuelGrt.setOrderValue(2);
        dataType.addComplexReusableTypes(secoursMutuelGrt);

        ComplexTypeModel pointSoutirageJumeleRpd = new ComplexTypeModel("PointSoutirageJumeleRpd", null); 
        pointSoutirageJumeleRpd.setOrderValue(9);
        dataType.addComplexReusableTypes(pointSoutirageJumeleRpd);

        ComplexTypeModel pointSoutirageRpt = new ComplexTypeModel("PointSoutirageRpt", null); 
        pointSoutirageRpt.setOrderValue(4);
        dataType.addComplexReusableTypes(pointSoutirageRpt);

        ComplexTypeModel pointInjectionRptRpd = new ComplexTypeModel("PointInjectionRptRpd", null); 
        pointInjectionRptRpd.setOrderValue(13);
        dataType.addComplexReusableTypes(pointInjectionRptRpd);

        ComplexTypeModel pointSoutirageRpd = new ComplexTypeModel("PointSoutirageRpd", null); 
        pointSoutirageRpd.setOrderValue(5);
        dataType.addComplexReusableTypes(pointSoutirageRpd);

        ComplexTypeModel pointSoutirageProfile = new ComplexTypeModel("PointSoutirageProfile", null); 
        pointSoutirageProfile.setOrderValue(7);
        dataType.addComplexReusableTypes(pointSoutirageProfile);

        ComplexTypeModel pointInjectionRpt = new ComplexTypeModel("PointInjectionRpt", null); 
        pointInjectionRpt.setOrderValue(11);
        dataType.addComplexReusableTypes(pointInjectionRpt);

        ComplexTypeModel pointInjectionRpd = new ComplexTypeModel("PointInjectionRpd", null); 
        pointInjectionRpd.setOrderValue(12);
        dataType.addComplexReusableTypes(pointInjectionRpd);

        ComplexTypeModel pointSoutirageJumeleRpt = new ComplexTypeModel("PointSoutirageJumeleRpt", null); 
        pointSoutirageJumeleRpt.setOrderValue(8);
        dataType.addComplexReusableTypes(pointSoutirageJumeleRpt);

        ComplexTypeModel pointSoutirageRptRpd = new ComplexTypeModel("PointSoutirageRptRpd", null); 
        pointSoutirageRptRpd.setOrderValue(6);
        dataType.addComplexReusableTypes(pointSoutirageRptRpd);

        ComplexTypeModel pointSoutirageJumeleRptRpd = new ComplexTypeModel("PointSoutirageJumeleRptRpd", null); 
        pointSoutirageJumeleRptRpd.setOrderValue(10);
        dataType.addComplexReusableTypes(pointSoutirageJumeleRptRpd);

        ItemNodeModel node = new ItemNodeModel();
        node.setRealType("Eda/typesEda/typeEDA");
        Map<String, String> labelMap=new HashMap<String, String>();
        labelMap.put("en", "typeEDA");
        dataType.setLabelMap(labelMap);
        Field field = TreeDetailGridFieldCreator.createField(node, dataType, "en", new HashMap<String, Field<?>>(), null,
                null);

        assertNotNull(field);
        assertTrue(field instanceof ComboBoxField);

        ComboBoxField<ComboBoxModel> comboxField = (ComboBoxField<ComboBoxModel>) field;
        ListStore<ComboBoxModel> reusableTypes = comboxField.getStore();
        assertNotNull(reusableTypes);

        assertEquals("typeEDA", ((ComplexTypeModel) reusableTypes.getAt(0).get("reusableType")).getName());
        assertEquals("Balit", ((ComplexTypeModel) reusableTypes.getAt(1).get("reusableType")).getName());
        assertEquals("SecoursMutuelGrt", ((ComplexTypeModel) reusableTypes.getAt(2).get("reusableType")).getName());
        assertEquals("PointEchange", ((ComplexTypeModel) reusableTypes.getAt(3).get("reusableType")).getName());
        assertEquals("PointSoutirageRpt", ((ComplexTypeModel) reusableTypes.getAt(4).get("reusableType")).getName());
        assertEquals("PointSoutirageRpd", ((ComplexTypeModel) reusableTypes.getAt(5).get("reusableType")).getName());
        assertEquals("PointSoutirageRptRpd", ((ComplexTypeModel) reusableTypes.getAt(6).get("reusableType")).getName());
        assertEquals("PointSoutirageProfile", ((ComplexTypeModel) reusableTypes.getAt(7).get("reusableType")).getName());
        assertEquals("PointSoutirageJumeleRpt", ((ComplexTypeModel) reusableTypes.getAt(8).get("reusableType")).getName());
        assertEquals("PointSoutirageJumeleRpd", ((ComplexTypeModel) reusableTypes.getAt(9).get("reusableType")).getName());
        assertEquals("PointSoutirageJumeleRptRpd", ((ComplexTypeModel) reusableTypes.getAt(10).get("reusableType")).getName());
        assertEquals("PointInjectionRpt", ((ComplexTypeModel) reusableTypes.getAt(11).get("reusableType")).getName());
        assertEquals("PointInjectionRpd", ((ComplexTypeModel) reusableTypes.getAt(12).get("reusableType")).getName());
        assertEquals("PointInjectionRptRpd", ((ComplexTypeModel) reusableTypes.getAt(13).get("reusableType")).getName());

    }

    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; 
    }
}
