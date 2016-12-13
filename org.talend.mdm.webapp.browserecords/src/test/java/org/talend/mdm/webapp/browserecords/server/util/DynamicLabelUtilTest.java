/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.dom4j.Document;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit3.PowerMockSuite;
import org.talend.mdm.commmon.util.datamodel.management.DataModelID;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.server.util.XmlUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.DataModelHelper;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.SchemaMockAgent;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.amalto.commons.core.utils.XMLUtils;
import com.amalto.core.util.Util;
import com.extjs.gxt.ui.client.data.ModelData;

@PrepareForTest({ Util.class })
@SuppressWarnings("nls")
public class DynamicLabelUtilTest extends TestCase {

    @SuppressWarnings("unchecked")
    public static TestSuite suite() throws Exception {
        return new PowerMockSuite("Unit tests for " + DynamicLabelUtilTest.class.getSimpleName(), DynamicLabelUtilTest.class);
    }

    private Document parsedDocument = null;

    private String baseXpath = "";

    private ItemNodeModel itemModel = null;

    private EntityModel entity;

    private String language = "fr";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parsedDocument = getDocument();
        entity = getEntityModel();
        itemModel = getItemNodeModel(XmlUtil.parseDocument(parsedDocument).getDocumentElement(), "", true);
    }

    public void testGetDynamicLabel() throws Exception {
        Map<String, TypeModel> metaDataTypes = entity.getMetaDataTypes();
        // Mock Test DynamicLabelUtil.getDynamicLabel
        mock_getDynamicLabel(parsedDocument, baseXpath, itemModel, metaDataTypes, language);
        List<ModelData> list = itemModel.getChildren();
        List<ItemNodeModel> dynamicLableNodes = new ArrayList<ItemNodeModel>();
        findDynamicLableNodes(list, dynamicLableNodes);
        assertTrue(dynamicLableNodes.size() == 3);
        assertEquals("Contrat/detailContrat[1]/Perimetre[1]/entitesPresentes[1]/EDPs[1]/EDP[1]",
                CommonUtil.getRealXPath(dynamicLableNodes.get(0)));
        assertEquals("EDP: DFELDT-DIESELS ELD FRANCE", dynamicLableNodes.get(0).getDynamicLabel());
        assertEquals("Contrat/detailContrat[1]/Perimetre[1]/entitesPresentes[1]/EDPs[1]/EDP[2]",
                CommonUtil.getRealXPath(dynamicLableNodes.get(1)));
        assertEquals("EDP: Test-DIESELS & FRANCE", dynamicLableNodes.get(1).getDynamicLabel());
        assertEquals("Contrat/detailContrat[1]/Perimetre[1]/entitesPresentes[1]/EDPs[1]/EDP[3]",
                CommonUtil.getRealXPath(dynamicLableNodes.get(2)));
        assertEquals("EDP: Tester < Developer", dynamicLableNodes.get(2).getDynamicLabel());
    }

    private void findDynamicLableNodes(List<ModelData> list, List<ItemNodeModel> dynamicLableNodes) {
        for (ModelData modelData : list) {
            ItemNodeModel model = (ItemNodeModel) modelData;
            if (model.getDynamicLabel() != null && model.getDynamicLabel().trim().length() > 0) {
                dynamicLableNodes.add(model);
            }
            findDynamicLableNodes(model.getChildren(), dynamicLableNodes);
        }
    }

    /**
     * Mock Test DynamicLabelUtil.getDynamicLabel, the codes come from DynamicLabelUtil.getDynamicLabel<br>
     * only replace replaceForeignPath with mock_replaceForeignPath(PowerMock can not mock Configuration)
     * 
     * @param parsedDocument
     * @param baseXpath
     * @param itemModel
     * @param metaDataTypes
     * @param language
     * @throws Exception
     */
    private void mock_getDynamicLabel(Document parsedDocument, String baseXpath, ItemNodeModel itemModel,
            Map<String, TypeModel> metaDataTypes, String language) throws Exception {
        String typePath = itemModel.getTypePath();
        TypeModel typeModel = metaDataTypes.get(typePath);
        String fullxpath;
        if (baseXpath == null || baseXpath.trim().length() == 0) {
            fullxpath = CommonUtil.getRealXPath(itemModel);
        } else {
            fullxpath = baseXpath + "/" + CommonUtil.getRealXPath(itemModel); //$NON-NLS-1$
        }
        String label = typeModel.getLabel(language);
        if (org.talend.mdm.webapp.base.server.util.DynamicLabelUtil.isDynamicLabel(label)) {
            // Mock replaceForeignPath
            label = mock_replaceForeignPath(fullxpath, label, parsedDocument);
            String stylesheet = org.talend.mdm.webapp.base.server.util.DynamicLabelUtil.genStyle(fullxpath,
                    com.amalto.webapp.core.util.XmlUtil.escapeXml(label));
            String dynamicLB = org.talend.mdm.webapp.base.server.util.DynamicLabelUtil.getParsedLabel(XMLUtils.styleDocument(
                    parsedDocument, stylesheet));
            itemModel.setDynamicLabel(dynamicLB);
        }

        if (itemModel.getChildCount() == 0) {
            return;
        } else {
            for (int i = 0; i < itemModel.getChildCount(); i++) {
                mock_getDynamicLabel(parsedDocument, baseXpath, (ItemNodeModel) itemModel.getChild(i), metaDataTypes, language);
            }
        }
    }

    private String mock_replaceForeignPath(String fullxpath, String label, Document parsedDocument) {
        if (fullxpath.equals("Contrat/detailContrat[1]/Perimetre[1]/entitesPresentes[1]/EDPs[1]/EDP[1]")) {
            return "EDP: DFELDT-DIESELS ELD FRANCE";
        } else if (fullxpath.equals("Contrat/detailContrat[1]/Perimetre[1]/entitesPresentes[1]/EDPs[1]/EDP[2]")) {
            return "EDP: Test-DIESELS & FRANCE";
        } else if (fullxpath.equals("Contrat/detailContrat[1]/Perimetre[1]/entitesPresentes[1]/EDPs[1]/EDP[3]")) {
            return "EDP: Tester < Developer";
        }
        return label;
    }

    private Document getDocument() throws Exception {
        InputStream stream = getClass().getResourceAsStream("Contrat.xml");
        String xsd = inputStream2String(stream);
        return XmlUtil.parseText(xsd);
    }

    /**
     * Mock BrowseRecordsAction.builderNode()
     */
    private ItemNodeModel getItemNodeModel(Node el, String xpath, boolean isPolyType) throws Exception {
        Map<String, TypeModel> metaDataTypes = entity.getMetaDataTypes();
        String realType = ((Element) el).getAttribute("xsi:type");
        if (isPolyType) {
            xpath += ("".equals(xpath) ? el.getNodeName() : "/" + el.getNodeName());
            if (realType != null && realType.trim().length() > 0) {
                xpath += ":" + realType;
            }
        } else {
            xpath += ("".equals(xpath) ? el.getNodeName() : "/" + el.getNodeName());
        }
        String typePath;
        if ("".equals(baseXpath)) {
            typePath = xpath.replaceAll("\\[\\d+\\]", "");
        } else {
            typePath = (baseXpath + "/" + xpath).replaceAll("\\[\\d+\\]", "");
        }
        typePath = typePath.replaceAll(":" + realType + "$", "");
        ItemNodeModel nodeModel = new ItemNodeModel(el.getNodeName());

        TypeModel model = DataModelHelper.findTypeModelByTypePath(metaDataTypes, typePath);
        nodeModel.setTypePath(model.getTypePath());
        String realXPath = xpath;
        if (isPolyType) {
            realXPath = realXPath.replaceAll(":\\w+", "");
        }

        if (realType != null && realType.trim().length() > 0) {
            nodeModel.setRealType(((Element) el).getAttribute("xsi:type"));
        }
        nodeModel.setLabel(model.getLabel(language));

        String foreignKey = DataModelHelper.findTypeModelByTypePath(metaDataTypes, typePath).getForeignkey();
        if (foreignKey != null && foreignKey.trim().length() > 0) {
            // set foreignKeyBean
            model.setRetrieveFKinfos(true);
            String modelType = ((Element) el).getAttribute("tmdm:type");
            if (modelType != null && modelType.trim().length() > 0) {
                nodeModel.setTypeName(modelType);
            }
            ForeignKeyBean fkBean = mock_getForeignKeyDesc(model, el.getTextContent());
            if (fkBean != null) {
                nodeModel.setObjectValue(fkBean);
            }
        }
        NodeList children = el.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    nodeModel.add(getItemNodeModel(child, xpath, isPolyType));
                }
            }
        }
        return nodeModel;
    }

    /**
     * DOC Administrator Comment method "mock_getForeignKeyDesc".
     * 
     * @param model
     * @param textContent
     * @param b
     * @param modelType
     * @return
     */
    private ForeignKeyBean mock_getForeignKeyDesc(TypeModel model, String textContent) {
        ForeignKeyBean bean = new ForeignKeyBean();
        bean.setId(textContent);
        return bean;
    }

    private EntityModel getEntityModel() throws Exception {

        EntityModel entityModel = new EntityModel();
        String datamodelName = "RTE";
        String concept = "Contrat";
        String[] ids = { "" };
        String[] roles = { "Demo_Manager", "System_Admin", "authenticated", "administration" };
        InputStream stream = getClass().getResourceAsStream("../bizhelpers/RTE.xsd");
        String xsd = inputStream2String(stream);

        PowerMockito.mockStatic(Util.class);
        Mockito.when(Util.isEnterprise()).thenReturn(true);

        DataModelHelper.overrideSchemaManager(new SchemaMockAgent(xsd, new DataModelID(datamodelName)));
        DataModelHelper.parseSchema(datamodelName, concept, DataModelHelper.convertXsd2ElDecl(concept, xsd), ids, entityModel,
                Arrays.asList(roles));

        return entityModel;

    }

    private String inputStream2String(InputStream is) {

        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            fail();
        }
        return buffer.toString();

    }

}
