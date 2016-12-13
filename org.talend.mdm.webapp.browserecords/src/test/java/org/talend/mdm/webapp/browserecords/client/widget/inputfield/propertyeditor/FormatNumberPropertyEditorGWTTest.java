/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield.propertyeditor;

import java.math.BigDecimal;

import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class FormatNumberPropertyEditorGWTTest extends GWTTestCase {

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }
    
    public void testConvertStringValue() {
        FormatNumberPropertyEditor formatNumberPropertyEditor = new FormatNumberPropertyEditor(Float.class);
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.23").toString(), "1.23");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.23").toString(), "-1.23");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.230").toString(), "1.23");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.230").toString(), "-1.23");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.000").toString(), "1.0");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.000").toString(), "-1.0");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1").toString(), "1.0");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1").toString(), "-1.0");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.23265984552457").toString(), "1.2326598");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.23265988552457").toString(), "1.2326599");
        assertEquals(formatNumberPropertyEditor.convertStringValue("569874584547.365"), Float.valueOf("569874580000"));
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.23265984552457").toString(), "-1.2326598");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-569874584547.365"), Float.valueOf("-569874580000"));

        formatNumberPropertyEditor = new FormatNumberPropertyEditor(Double.class);
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.23").toString(), "1.23");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.23").toString(), "-1.23");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.230").toString(), "1.23");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.230").toString(), "-1.23");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.000").toString(), "1.0");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.000").toString(), "-1.0");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1").toString(), "1.0");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1").toString(), "-1.0");
        // 14 digits
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.23265984552457").toString(), "1.23265984552457");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.23265984552457").toString(), "-1.23265984552457");

        // 15 digits
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.232659845524576").toString(), "1.232659845524576");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.232659845524576").toString(), "-1.232659845524576");

        formatNumberPropertyEditor = new FormatNumberPropertyEditor(BigDecimal.class);
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.23").toString(), "1.23");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.23").toString(), "-1.23");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.230").toString(), "1.230");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.230").toString(), "-1.230");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.000").toString(), "1.000");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.000").toString(), "-1.000");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1").toString(), "1");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1").toString(), "-1");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.23265984552457").toString(), "1.23265984552457");
        assertEquals(formatNumberPropertyEditor.convertStringValue("1.23265988552457").toString(), "1.23265988552457");
        assertEquals(formatNumberPropertyEditor.convertStringValue("569874584547.365").toString(), "569874584547.365");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-1.23265984552457").toString(), "-1.23265984552457");
        assertEquals(formatNumberPropertyEditor.convertStringValue("-569874584547.365").toString(), "-569874584547.365");
        assertEquals(formatNumberPropertyEditor.convertStringValue("999999999999.999").toString(), "999999999999.999");
        assertEquals(formatNumberPropertyEditor.convertStringValue("9999999999999.99999").toString(), "9999999999999.99999");
        assertEquals(formatNumberPropertyEditor.convertStringValue("99999999999999.99999").toString(), "99999999999999.99999");
        assertEquals(formatNumberPropertyEditor.convertStringValue("99999999999999999999999999999.99999999999999999999999")
                .toString(), "99999999999999999999999999999.99999999999999999999999");
    }

}
