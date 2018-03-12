/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */
package com.amalto.core.history.accessor;

import org.junit.Assert;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.xml.sax.SAXException;
import com.amalto.core.save.DOMDocument;
import com.amalto.core.util.Util;

public class ManyFieldAccessorTest {
    
    @Test
    public void testCreateEmptyChildNode() throws Exception {
        DOMDocument document = getTypeWithNoElementContent();

        Accessor accessor = document.createAccessor("/Communications/TypeCommunication[1]"); //$NON-NLS-1$
        accessor.create();
        
        Assert.assertNotNull(document);
    }
    
    private DOMDocument getTypeWithNoElementContent() throws IOException, SAXException, ParserConfigurationException {
        String xml = 
                "<TI_Tiers xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\r\n" +  //$NON-NLS-1$
        		"   <Id>7</Id>\r\n" +  //$NON-NLS-1$
        		"   <Communications>\r\n" +  //$NON-NLS-1$
        		"      <TypeCommunication xsi:type=\"TI_TypeComType\"/>\r\n" +  //$NON-NLS-1$
        		"   </Communications>\r\n" +   //$NON-NLS-1$
        		"</TI_Tiers>"; //$NON-NLS-1$
        return new DOMDocument(Util.parse(xml), null, StringUtils.EMPTY, StringUtils.EMPTY);
    }
}
