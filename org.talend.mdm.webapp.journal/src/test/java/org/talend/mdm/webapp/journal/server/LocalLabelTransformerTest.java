/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.server;

import java.io.InputStream;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilderFactory;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.dom4j.DocumentHelper;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit3.PowerMockSuite;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;
import org.talend.mdm.commmon.metadata.MetadataRepository;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.save.DOMDocument;

@PrepareForTest({ com.amalto.core.util.LocaleUtil.class})
public class LocalLabelTransformerTest extends TestCase{

    private DocumentBuilderFactory documentBuilderFactory;

    private MetadataRepository metadataRepository;

    private ComplexTypeMetadata typeMetadata;

    private com.amalto.core.history.Document document;

    private com.amalto.core.history.Document transformedDocument;

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + LocalLabelTransformerTest.class.getSimpleName(),
                LocalLabelTransformerTest.class);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        metadataRepository.close();
        metadataRepository = null;
    }

    public void testMultipleLanguageLabel() {
        String concept = "Product";
        String clusterName = "Product";
        String modelName = "Product";

        metadataRepository = new MetadataRepository();
        InputStream dataModelStream = this.getClass().getResourceAsStream("/Product.xsd");
        metadataRepository.load(dataModelStream);

        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);

        typeMetadata = (ComplexTypeMetadata) metadataRepository.getType(concept);

        String content = "<Product><Id>2</Id><Name>2-name</Name><Description>2</Description><Features><Sizes><Size>Medium</Size></Sizes><Colors><Color>Light Blue</Color></Colors></Features><Price>2.00</Price><Family>[1]</Family><Stores/></Product>";

        try {
            document = new DOMDocument(XMLUtils.parse(content), typeMetadata, clusterName, modelName);
        } catch (Exception e) {
            fail("failed to create document");
        }

        LocalLabelTransformer multipleLanguageLabel = new LocalLabelTransformer("fr");

        transformedDocument = document.transform(multipleLanguageLabel);
        org.dom4j.Document newDcoument = null;
        try {

            newDcoument = DocumentHelper.parseText(transformedDocument.exportToString());
        } catch (Exception e) {
            fail("failed to test.");
        }
        org.dom4j.Element rootElement = newDcoument.getRootElement();
        assertEquals("Produit", rootElement.attributeValue("label"));
        assertEquals(7, rootElement.elements().size());
        assertEquals("Id unique", rootElement.element("Id").attributeValue("label"));
        assertEquals("Nom", rootElement.element("Name").attributeValue("label"));
        assertEquals("Description", rootElement.element("Description").attributeValue("label"));
        assertEquals("Caractéristiques", rootElement.element("Features").attributeValue("label"));
        assertEquals("Prix", rootElement.element("Price").attributeValue("label"));
        assertEquals("Famille", rootElement.element("Family").attributeValue("label"));

        org.dom4j.Element featuresElement = rootElement.element("Features");
        assertEquals(2, featuresElement.elements().size());
        assertEquals("Tailles", featuresElement.element("Sizes").attributeValue("label"));
        assertEquals("Couleurs", featuresElement.element("Colors").attributeValue("label"));

        org.dom4j.Element sizesElement = featuresElement.element("Sizes");
        assertEquals(1, sizesElement.elements().size());
        assertEquals("Taille", sizesElement.element("Size").attributeValue("label"));

        org.dom4j.Element colorsElement = featuresElement.element("Colors");
        assertEquals(1, colorsElement.elements().size());
        assertEquals("Couleur", colorsElement.element("Color").attributeValue("label"));

    }

    public void testMultipleLanguageLabel_ForRepeatableModel() {
        String concept = "Citizen";
        String clusterName = "Nav";
        String modelName = "Nav";

        metadataRepository = new MetadataRepository();
        InputStream dataModelStream = this.getClass().getResourceAsStream("/Nationality.xsd");
        metadataRepository.load(dataModelStream);

        documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setValidating(false);

        typeMetadata = (ComplexTypeMetadata) metadataRepository.getType(concept);

        String content = "<Citizen><CitizenId>1</CitizenId><name>1</name><identity><type>1</type><nationality>[n1]</nationality></identity><identity><type>2</type><nationality>[n1]</nationality></identity><identity><type>3</type><nationality>[n1]</nationality></identity><aa><subelement>23</subelement><n3><subelement>23</subelement><name>23</name><n2>[n1]</n2></n3><n3><subelement>24</subelement><name>23</name><n2>[n1]</n2></n3></aa><aa><subelement>23</subelement><n3><subelement>23</subelement><name>25</name><n2>[n1]</n2></n3><n3><subelement>23</subelement><name>26</name><n2>[n1]</n2></n3></aa></Citizen>";

        try {
            document = new DOMDocument(XMLUtils.parse(content), typeMetadata, clusterName, modelName);
        } catch (Exception e) {
            fail("failed to create document");
        }

        LocalLabelTransformer multipleLanguageLabel = new LocalLabelTransformer("en");

        transformedDocument = document.transform(multipleLanguageLabel);
        org.dom4j.Document newDcoument = null;
        try {

            newDcoument = DocumentHelper.parseText(transformedDocument.exportToString());
        } catch (Exception e) {
            fail("failed to test.");
        }
        org.dom4j.Element rootElement = newDcoument.getRootElement();
        assertEquals("Citizen", rootElement.attributeValue("label"));
        assertEquals(7, rootElement.elements().size());
        assertEquals("CitizenId", rootElement.element("CitizenId").attributeValue("label"));
        assertEquals("name", rootElement.element("name").attributeValue("label"));

        Iterator it = rootElement.elementIterator("identity");
        int i = 0;
        while (it.hasNext()) {
            org.dom4j.Element identityElement = (org.dom4j.Element) it.next();
            assertEquals("identity", identityElement.attributeValue("label"));
            i++;
        }
        assertEquals(3, i);

        it = rootElement.elementIterator("aa");
        i = 0;
        int j = 0;
        while (it.hasNext()) {
            org.dom4j.Element aaElement = (org.dom4j.Element) it.next();
            assertEquals("aa", aaElement.attributeValue("label"));
            i++;
            assertEquals("subelement", aaElement.element("subelement").attributeValue("label"));

            Iterator n3It = aaElement.elementIterator("n3");
            while (n3It.hasNext()) {
                org.dom4j.Element n3Element = (org.dom4j.Element) n3It.next();
                assertEquals("n3", n3Element.attributeValue("label"));
                j++;
                assertEquals("subelement", n3Element.element("subelement").attributeValue("label"));
                assertEquals("name", n3Element.element("name").attributeValue("label"));
                assertEquals("n2", n3Element.element("n2").attributeValue("label"));
            }
            assertEquals(2, j);
            j = 0;
        }
        assertEquals(2, i);
    }
}
