/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import org.talend.mdm.webapp.general.model.GroupItem;
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

    public void testGetGroupItems() throws Exception {
        List<GroupItem> menuList = Utils.getGroupItems("en");
        assertEquals(5, menuList.size());
        String[] menuHeader = { "Home", "Browse", "Govern", "Administration", "Tools" };
        assertMenu(menuHeader, menuList);

        menuList = Utils.getGroupItems("fr");
        String[] frMenuHeader = { "Accueil", "Consultation", "Gouvernance", "Administration", "Outils" };
        assertEquals(5, menuList.size());
        assertMenu(frMenuHeader, menuList);

        menuList = Utils.getGroupItems("ru");
        String[] ruMenuHeader = { "Домашняя страница", "Просмотр", "Управление", "Администрирование", "Tools" };
        assertEquals(5, menuList.size());
        assertMenu(ruMenuHeader, menuList);
    }

    protected void assertMenu(String[] expectedHeader, List<GroupItem> menuList) {
        // assert home
        assertEquals(expectedHeader[0], menuList.get(0).getGroupHeader());
        assertEquals(2, menuList.get(0).getMenuItems().size());
        assertEquals("welcomeportal.WelcomePortal", menuList.get(0).getMenuItems().get(0));
        assertEquals("browserecords.BrowseRecords", menuList.get(0).getMenuItems().get(1));

        // assert browse
        assertEquals(expectedHeader[1], menuList.get(1).getGroupHeader());
        assertEquals(6, menuList.get(1).getMenuItems().size());
        assertEquals("browserecords.BrowseRecords", menuList.get(1).getMenuItems().get(0));
        assertEquals("browserecords.BrowseRecordsInStaging", menuList.get(1).getMenuItems().get(1));
        assertEquals("hierarchy.Hierarchy", menuList.get(1).getMenuItems().get(2));
        assertEquals("journal.Journal", menuList.get(1).getMenuItems().get(3));
        assertEquals("search.Search", menuList.get(1).getMenuItems().get(4));
        assertEquals("recyclebin.RecycleBin", menuList.get(1).getMenuItems().get(5));

        // assert govern
        assertEquals(expectedHeader[2], menuList.get(2).getGroupHeader());
        assertEquals(4, menuList.get(2).getMenuItems().size());
        assertEquals("stagingarea.Stagingarea", menuList.get(2).getMenuItems().get(0));
        assertEquals("datastewardship.Datastewardship", menuList.get(2).getMenuItems().get(1));
        assertEquals("workflowtasks.BonitaWorkflowTasks", menuList.get(2).getMenuItems().get(2));
        assertEquals("crossreference.CrossReference", menuList.get(2).getMenuItems().get(3));

        // assert administration
        assertEquals(expectedHeader[3], menuList.get(3).getGroupHeader());
        assertEquals(1, menuList.get(3).getMenuItems().size());
        assertEquals("usermanager.UserManager", menuList.get(3).getMenuItems().get(0));

        // assert tools
        assertEquals(expectedHeader[4], menuList.get(4).getGroupHeader());
        assertEquals(3, menuList.get(4).getMenuItems().size());
        assertEquals("logviewer.LogViewer", menuList.get(4).getMenuItems().get(0));
        assertEquals("h2console.H2Console", menuList.get(4).getMenuItems().get(1));
        assertEquals("apidoc.RestApiDoc", menuList.get(4).getMenuItems().get(2));
    }
}
