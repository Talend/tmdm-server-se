/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield.validator;

import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatNumberField;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.FacetEnum;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class NumberFieldValidatorGWTTest extends GWTTestCase{

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }

    public void testValidateDecimalValue() {
        FormatNumberField dateField = new FormatNumberField();
        
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "12.999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "99.999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "12.6987"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-12.6987"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-12.999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "0.123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-.999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "00.123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-00.123"));
        // for the localize
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "12,999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "99,999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "12,6987"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-12,6987"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-12,999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "0,123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-,999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "00,123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-00,123"));
        
        dateField.setData(FacetEnum.TOTAL_DIGITS.getFacetName(), "5");
        dateField.setData(FacetEnum.FRACTION_DIGITS.getFacetName(), "3");

        assertNull(NumberFieldValidator.getInstance().validate(dateField, "12.999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "99.999"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "12.6987"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "-12.6987"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-12.999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "0.123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-.999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "00.123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-00.123"));
        // for the localize
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "12,999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "99,999"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "12,6987"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "-12,6987"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-12,999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "0,123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-,999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "00,123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-00,123"));

        dateField.setData(FacetEnum.TOTAL_DIGITS.getFacetName(), "3");
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "12.999"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "99.999"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "12.6987"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "-12.999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "0.123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-0.123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-.999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, ".999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "00.123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-00.123"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "-00.1230"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "00.1230"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "-.1230"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, ".1230"));
        // for the localize
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "12,999"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "99,999"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "12,6987"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "-12,999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "0,123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-0,123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-,999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, ",999"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "00,123"));
        assertNull(NumberFieldValidator.getInstance().validate(dateField, "-00,123"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "-00,1230"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "00,1230"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "-,1230"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, ",1230"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "999999999999.999"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "-999999999999.999"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "99999999999999999999.999"));
        assertNotNull(NumberFieldValidator.getInstance().validate(dateField, "-99999999999999999999.999"));
    }
    
    
    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }

}
