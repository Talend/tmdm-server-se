/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.typefield;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.AppHeader;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class MiscTypeFieldFactoryGWTTest extends GWTTestCase {

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }
    
    public void testCreateField () {
        TypeModel typeModel = new SimpleTypeModel("boolean", DataTypeConstants.BOOLEAN);
        TypeFieldCreateContext context = new TypeFieldCreateContext(typeModel);
        context.setWithValue(true);
        context.setNode(new ItemNodeModel("boolean"));
        TypeFieldSource typeFieldSource = new TypeFieldSource(TypeFieldSource.FORM_INPUT);
        MiscTypeFieldFactory miscTypeFieldFactory = new MiscTypeFieldFactory(typeFieldSource, context);
        Field<?> field = miscTypeFieldFactory.createField();
        assertNotNull(field);
        assertFalse(Boolean.valueOf(field.getValue().toString()));
        assertNotNull(context.getNode());
        // Boolean field which generated by MiscTypeField has no default value
        assertNull(context.getNode().getObjectValue());
    }
   
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords";
    }
}
