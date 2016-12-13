/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.typefield;

import java.util.HashMap;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.widget.MultiLanguageField;
import org.talend.mdm.webapp.base.client.widget.MultiLanguageTextAreaField;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.junit.client.GWTTestCase;

public class MultiLanguageTextAreaFieldGWTTest  extends GWTTestCase {

    private String languageEN = "en"; //$NON-NLS-1$
    
    private TypeModel descriptionNodeModel = new SimpleTypeModel("description", DataTypeConstants.MLS); //$NON-NLS-1$
    
    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }
    
    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }

    @SuppressWarnings("unused")
    public void testCreateField4MultiLanguageTextAreaField() {
        Field<?> createdField = null;
        String descriptionValue = "[EN:1234567890123456789012345678901][FR:1234567890]"; //$NON-NLS-1$
        TypeFieldCreateContext context = new TypeFieldCreateContext();
        context.setLanguage(languageEN);
        context.setAutoTextAreaLength(30);
        TypeFieldCreator typeFieldCreator = new TypeFieldCreator(new TypeFieldSource(TypeFieldSource.FORM_INPUT), context);
        
        ItemNodeModel descriptionNode = new ItemNodeModel("Description"); //$NON-NLS-1$
        descriptionNode.setObjectValue(descriptionValue);
        
        Map<String, TypeFieldStyle> sytles = new HashMap<String, TypeFieldStyle>();
        sytles.put(TypeFieldStyle.ATTRI_WIDTH, new TypeFieldStyle(TypeFieldStyle.ATTRI_WIDTH, "400", //$NON-NLS-1$
                TypeFieldStyle.SCOPE_BUILTIN_TYPEFIELD));
        
        context.setDataType(descriptionNodeModel);
        createdField = typeFieldCreator.createFieldWithValueAndUpdateStyle(descriptionNode, sytles);
        assertNotNull(createdField);
        assertTrue(createdField instanceof MultiLanguageTextAreaField);
        assertEquals(descriptionValue, ((MultiLanguageField) createdField).getMultiLanguageStringValue());
    }
}
