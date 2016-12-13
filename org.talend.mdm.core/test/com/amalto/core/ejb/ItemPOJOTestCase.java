/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.ejb;

import com.amalto.core.objects.ItemPOJO;
import junit.framework.TestCase;

import com.amalto.core.util.XtentisException;

/**
 * DOC achen class global comment. Detailled comment
 * 
 */
@SuppressWarnings("nls")
public class ItemPOJOTestCase extends TestCase {

    /**
     * 
     * DOC achen Comment method "testParse".
     * 
     * @throws Exception
     */
    public void testParse() throws Exception {
        // no taskId
        String xml = "<ii><c>DStar</c><n>Agency</n><dmn>DStarLax</dmn><sp>test2</sp><i>IL03</i><t>1315375127198</t><p><Agency xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<Name>Rock Island</Name>"
                + "<City>Rock Island</City>"
                + "<State>IL</State>"
                + "<Zip>61201</Zip>"
                + "<Region>MWST</Region>"
                + "<MoreInfo>Map@@http://maps.google.com/maps?q=41.4903,-90.56956&amp;ll=41.4903,-90.56956&amp;z=9</MoreInfo>"
                + "<Id>IL03</Id>" + "</Agency></p></ii>";
        ItemPOJO itempojo = ItemPOJO.parse(xml);
        assertEquals("IL03", itempojo.getItemIds()[0]);

        // contains taskId
        String xml1 = "<ii><c>DStar</c><n>Agency</n><dmn>DStarLax</dmn><sp>test2</sp><i>IL03</i><t>1315375127198</t><taskId>12345</taskId><p><Agency xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<Name>Rock Island</Name>"
                + "<City>Rock Island</City>"
                + "<State>IL</State>"
                + "<Zip>61201</Zip>"
                + "<Region>MWST</Region>"
                + "<MoreInfo>Map@@http://maps.google.com/maps?q=41.4903,-90.56956&amp;ll=41.4903,-90.56956&amp;z=9</MoreInfo>"
                + "<Id>IL03</Id>" + "</Agency></p></ii>";
        itempojo = ItemPOJO.parse(xml1);
        assertEquals("IL03", itempojo.getItemIds()[0]);
        assertNull(itempojo.getTaskId());
        //wrong xml
       xml1 = "<ii><i>IL03</i><t>1315375127198</t><taskId>12345</taskId><p><Agency xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
            + "<Name>Rock Island</Name>"
            + "<City>Rock Island</City>"
            + "<State>IL</State>"
            + "<Zip>61201</Zip>"
            + "<Region>MWST</Region>"
            + "<MoreInfo>Map@@http://maps.google.com/maps?q=41.4903,-90.56956&amp;ll=41.4903,-90.56956&amp;z=9</MoreInfo>"
            + "<Id>IL03</Id>" + "</Agency></p></ii>";
        try {
            ItemPOJO.parse(xml1);
        } catch (XtentisException e) {
            // Expected
        }

    }
}
