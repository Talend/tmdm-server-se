/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield.propertyeditor;

import java.math.BigDecimal;

import org.talend.mdm.webapp.base.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;

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

    public void testGetStringValue() {
        // Test scientific notation convert to number on float type,like 7.0E-8 convert to 0.00000007 and 7.0E-7 convert
        // to 0.0000007, shouldn't be 0.00000070
        FormatNumberPropertyEditor formatNumberPropertyEditor = new FormatNumberPropertyEditor(Float.class);
        assertEquals("0.00000007", formatNumberPropertyEditor.getStringValue(Float.parseFloat("0.00000007")));
        assertEquals("0.0000007", formatNumberPropertyEditor.getStringValue(Float.parseFloat("0.0000007")));
        assertEquals("0.000007", formatNumberPropertyEditor.getStringValue(Float.parseFloat("0.000007")));
        assertEquals("0.00007", formatNumberPropertyEditor.getStringValue(Float.parseFloat("0.00007")));
        assertEquals("0.0007", formatNumberPropertyEditor.getStringValue(Float.parseFloat("0.0007")));
        assertEquals("0.007", formatNumberPropertyEditor.getStringValue(Float.parseFloat("0.007")));
        assertEquals("0.07", formatNumberPropertyEditor.getStringValue(Float.parseFloat("0.07")));
        assertEquals("0.7", formatNumberPropertyEditor.getStringValue(Float.parseFloat("0.7")));
        assertEquals("0.0", formatNumberPropertyEditor.getStringValue(Float.parseFloat("0")));
        // Test scientific notation convert to number on double type
        formatNumberPropertyEditor = new FormatNumberPropertyEditor(Double.class);
        assertEquals("0.00000007", formatNumberPropertyEditor.getStringValue(Double.parseDouble("0.00000007")));
        assertEquals("0.0000007", formatNumberPropertyEditor.getStringValue(Double.parseDouble("0.0000007")));
        assertEquals("0.000007", formatNumberPropertyEditor.getStringValue(Double.parseDouble("0.000007")));
        assertEquals("0.00007", formatNumberPropertyEditor.getStringValue(Double.parseDouble("0.00007")));
        assertEquals("0.0007", formatNumberPropertyEditor.getStringValue(Double.parseDouble("0.0007")));
        assertEquals("0.007", formatNumberPropertyEditor.getStringValue(Double.parseDouble("0.007")));
        assertEquals("0.07", formatNumberPropertyEditor.getStringValue(Double.parseDouble("0.07")));
        assertEquals("0.7", formatNumberPropertyEditor.getStringValue(Double.parseDouble("0.7")));
        assertEquals("0.0", formatNumberPropertyEditor.getStringValue(Double.parseDouble("0")));
        // Test scientific notation convert to number on decimal type,like 7.0E-7 convert
        // to 0.00000070, shouldn't be 0.0000007,because decimal type can set fraction digits.
        formatNumberPropertyEditor = new FormatNumberPropertyEditor(BigDecimal.class);
        assertEquals("0.00000007", formatNumberPropertyEditor.getStringValue(new BigDecimal("0.00000007")));
        assertEquals("0.00000070", formatNumberPropertyEditor.getStringValue(new BigDecimal("0.00000070")));
        assertEquals("0.0000007", formatNumberPropertyEditor.getStringValue(new BigDecimal("0.0000007")));
        assertEquals("0.000007", formatNumberPropertyEditor.getStringValue(new BigDecimal("0.000007")));
        assertEquals("0.00000700", formatNumberPropertyEditor.getStringValue(new BigDecimal("0.00000700")));
        assertEquals("0.00007", formatNumberPropertyEditor.getStringValue(new BigDecimal("0.00007")));
        assertEquals("0.0007", formatNumberPropertyEditor.getStringValue(new BigDecimal("0.0007")));
        assertEquals("0.007", formatNumberPropertyEditor.getStringValue(new BigDecimal("0.007")));
        assertEquals("0.07", formatNumberPropertyEditor.getStringValue(new BigDecimal("0.07")));
        assertEquals("0.7", formatNumberPropertyEditor.getStringValue(new BigDecimal("0.7")));
        assertEquals("0", formatNumberPropertyEditor.getStringValue(new BigDecimal("0")));
    }
}
