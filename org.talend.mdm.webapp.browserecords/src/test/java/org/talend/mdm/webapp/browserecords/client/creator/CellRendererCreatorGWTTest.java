/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.creator;

import java.math.BigDecimal;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.DataTypeCustomized;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.util.Format;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class CellRendererCreatorGWTTest extends GWTTestCase {

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
    }

    public void testDataTypeCustomized() {
        DataTypeCustomized dataType = new DataTypeCustomized();

        assertEquals(getAfterFormatValue(dataType, ""), "");
        assertEquals(getAfterFormatValue(dataType, null), null);
        assertEquals(getAfterFormatValue(dataType, "50"), "");

        // Float
        dataType.setBaseTypeName(DataTypeConstants.FLOAT.getBaseTypeName());
        assertEquals(getAfterFormatValue(dataType, ""), "");
        assertEquals(getAfterFormatValue(dataType, null), null);
        assertEquals(getAfterFormatValue(dataType, "50"), "50");
        assertEquals(getAfterFormatValue(dataType, "50.2"), "50.2");
        assertEquals(getAfterFormatValue(dataType, "-50.2"), "-50.2");
        assertEquals(getAfterFormatValue(dataType, "50.200"), "50.2");
        assertEquals(getAfterFormatValue(dataType, "-50.200"), "-50.2");
        assertEquals(getAfterFormatValue(dataType, "050.200"), "50.2");
        assertEquals(getAfterFormatValue(dataType, "-050.200"), "-50.2");

        // Double
        dataType.setBaseTypeName(DataTypeConstants.DOUBLE.getBaseTypeName());
        assertEquals(getAfterFormatValue(dataType, ""), "");
        assertEquals(getAfterFormatValue(dataType, null), null);
        assertEquals(getAfterFormatValue(dataType, "50"), "50");
        assertEquals(getAfterFormatValue(dataType, "50.2"), "50.2");
        assertEquals(getAfterFormatValue(dataType, "-50.2"), "-50.2");
        assertEquals(getAfterFormatValue(dataType, "50.200"), "50.2");
        assertEquals(getAfterFormatValue(dataType, "-50.200"), "-50.2");
        assertEquals(getAfterFormatValue(dataType, "050.200"), "50.2");
        assertEquals(getAfterFormatValue(dataType, "-050.200"), "-50.2");

        // Decimial
        dataType.setBaseTypeName(DataTypeConstants.DECIMAL.getBaseTypeName());
        assertEquals(getAfterFormatValue(dataType, ""), "");
        assertEquals(getAfterFormatValue(dataType, null), null);
        assertEquals(getAfterFormatValue(dataType, "50"), "50");
        assertEquals(getAfterFormatValue(dataType, "50.2"), "50.2");
        assertEquals(getAfterFormatValue(dataType, "-50.2"), "-50.2");
        assertEquals(getAfterFormatValue(dataType, "50.200"), "50.2");
        assertEquals(getAfterFormatValue(dataType, "-50.200"), "-50.2");
        assertEquals(getAfterFormatValue(dataType, "050.200"), "50.2");
        assertEquals(getAfterFormatValue(dataType, "-050.200"), "-50.2");

    }

    private String getAfterFormatValue(DataTypeCustomized dataType, String value) {
        if (value == null || value.equals("")) {
            return Format.htmlEncode(value);
        }
        if (DataTypeConstants.FLOAT.getBaseTypeName().equals(dataType.getBaseTypeName())) {
            NumberFormat nb = NumberFormat.getDecimalFormat();
            nb.format(Float.valueOf(value));
            return Format.htmlEncode(nb.format(Float.valueOf(value)));
        } else if (DataTypeConstants.DOUBLE.getBaseTypeName().equals(dataType.getBaseTypeName())) {
            NumberFormat nb = NumberFormat.getDecimalFormat();
            nb.format(Float.valueOf(value));
            return Format.htmlEncode(nb.format(Double.valueOf(value)));
        } else if (DataTypeConstants.DECIMAL.getBaseTypeName().equals(dataType.getBaseTypeName())) {
            NumberFormat nb = NumberFormat.getDecimalFormat();
            nb.getPattern();
            nb.format(Float.valueOf(value));
            BigDecimal bigdecimal = new BigDecimal(value);
            return Format.htmlEncode(nb.format(bigdecimal));
        }
        return "";
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }

}
