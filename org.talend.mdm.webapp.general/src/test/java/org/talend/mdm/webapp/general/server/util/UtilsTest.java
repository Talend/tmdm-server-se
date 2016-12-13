/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.server.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit3.PowerMockSuite;
import org.talend.mdm.webapp.general.model.LanguageBean;
import org.talend.mdm.webapp.general.server.actions.GeneralAction;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.amalto.commons.core.utils.XMLUtils;

@PrepareForTest({ Utils.class, GeneralAction.class })
@SuppressWarnings("nls")
public class UtilsTest extends TestCase {

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + UtilsTest.class.getSimpleName(), UtilsTest.class);
    }

    public void testSetLanguages() throws Exception {
        String xml = "<User><username>administrator</username><password>200ceb26807d6bf99fd6f4f0d1ca54d4</password><givenname>Default</givenname><familyname>Administrator</familyname><company>Company</company><id>null</id><realemail>admin@company.com</realemail><viewrealemail>no</viewrealemail><registrationdate>1393637417135</registrationdate><lastvisitdate>0</lastvisitdate><enabled>yes</enabled><homepage>Home</homepage><language>en</language><roles><role>System_Admin</role><role>administration</role></roles></User>";
        assertEquals("en", xml.substring(xml.indexOf("<language>") + 10, xml.indexOf("</language>")));
        xml = Utils.setLanguage(xml, "fr");
        assertEquals("fr", xml.substring(xml.indexOf("<language>") + 10, xml.indexOf("</language>")));
    }

    public void testAddLanguages() throws DOMException, TransformerException, ParserConfigurationException, IOException,
            SAXException {
        String xml = "<User></User>";
        Document doc = XMLUtils.parse(xml);
        assertNull(doc.getElementsByTagName("language").item(0));
        Element node = doc.createElement("language");
        node.setTextContent("en");
        doc.getDocumentElement().appendChild(node);
        String newXml = XMLUtils.nodeToString(doc);
        assertEquals("en", XMLUtils.parse(newXml).getElementsByTagName("language").item(0).getTextContent());
    }

    public void testGetLanguages() throws Exception {
        PowerMockito.mockStatic(Utils.class);

        List<LanguageBean> enList = new ArrayList<LanguageBean>();
        List<LanguageBean> frList = new ArrayList<LanguageBean>();
        LanguageBean beanA = new LanguageBean();
        LanguageBean beanB = new LanguageBean();
        LanguageBean beanC = new LanguageBean();
        LanguageBean beanD = new LanguageBean();

        beanA.setText("en");
        beanA.setValue("en");
        beanB.setText("fr");
        beanB.setValue("fr");
        beanC.setText("en");
        beanC.setValue("en");
        beanD.setText("fr");
        beanD.setValue("fr");

        beanA.setSelected(true);
        beanB.setSelected(false);
        enList.add(beanA);
        enList.add(beanB);

        beanC.setSelected(false);
        beanD.setSelected(true);
        frList.add(beanC);
        frList.add(beanD);

        GeneralAction generalAction = new GeneralAction();

        PowerMockito.when(Utils.getDefaultLanguage()).thenReturn("fr");
        PowerMockito.when(Utils.getLanguages("en")).thenReturn(enList);
        PowerMockito.when(Utils.getLanguages("fr")).thenReturn(frList);

        List<LanguageBean> result = generalAction.getLanguages("en");
        String lang = "";
        for (LanguageBean langBean : result) {
            if (langBean.isSelected()) {
                lang = langBean.getValue();
            }
        }
        assertEquals("fr", lang);
        assertEquals(result.size(), 2);

        PowerMockito.when(Utils.getDefaultLanguage()).thenReturn("");
        PowerMockito.when(Utils.setDefaultLanguage(Mockito.<String> any())).thenReturn(true);

        result = generalAction.getLanguages("en");
        for (LanguageBean langBean : result) {
            if (langBean.isSelected()) {
                lang = langBean.getValue();
            }
        }
        assertEquals("en", lang);
        assertEquals(result.size(), 2);
    }
}
