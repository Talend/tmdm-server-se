/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.talend.mdm.commmon.util.core.MDMXMLUtils;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.DataTypeCustomized;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@SuppressWarnings("nls")
public class TestData {

    private static ItemNodeModel builder(Node node) {
        ItemNodeModel nodeModel = new ItemNodeModel(node.getNodeName());
        String realType = ((Element) node).getAttribute("xsi:type");
        if (realType != null && realType.trim().length() > 0)
            nodeModel.setRealType(realType);
        NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    nodeModel.add(builder(child));
                }
            }
        }
        return nodeModel;
    }

    public static ItemNodeModel getModel() throws Exception {
        Document doc = getDocument();
        ItemNodeModel nodeModel = builder(doc.getDocumentElement());
        return nodeModel;
    }

    public static List<String> getXpathes(String fileName) throws Exception {
        List<String> xpathes = new ArrayList<String>();
        InputStream is = TestData.class.getResourceAsStream("../../" + fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String ln = br.readLine();
        while (ln != null) {
            xpathes.add(ln);
            ln = br.readLine();
        }
        return xpathes;
    }

    public static Document getDocument() throws Exception {
        InputStream is = CommonUtilTest.class.getResourceAsStream("../../data.xml");
        DocumentBuilder builder = MDMXMLUtils.getDocumentBuilder().get();
        Document doc = builder.parse(is);
        return doc;

    }

    public static EntityModel getEntityModel() {
        LinkedHashMap<String, TypeModel> metadata = new LinkedHashMap<String, TypeModel>();
        DataTypeCustomized agency = new DataTypeCustomized("AgencyType", "anyType");
        ComplexTypeModel complexModel = new ComplexTypeModel("Agency", agency);
        complexModel.getLabelMap().put("en", "D* Agence");
        complexModel.getLabelMap().put("fr", "Agency");
        complexModel.setXpath("Agency");
        metadata.put("Agency", complexModel);

        SimpleTypeModel typeModel = new SimpleTypeModel("Id", DataTypeConstants.STRING);
        complexModel.addSubType(typeModel);
        typeModel.getLabelMap().put("fr", "Identifiant");
        typeModel.getLabelMap().put("en", "Identifier");
        typeModel.setXpath("Agency/Id");
        typeModel.setTypePath("Agency/Id");
        metadata.put("Agency/Id", typeModel);

        typeModel = new SimpleTypeModel("Name", DataTypeConstants.STRING);
        typeModel.getLabelMap().put("fr", "Nom");
        typeModel.getLabelMap().put("en", "Name");
        typeModel.setMaxOccurs(-1);
        typeModel.setMinOccurs(1);
        metadata.put("Agency/Name", typeModel);
        typeModel.setXpath("Agency/Name");
        typeModel.setTypePath("Agency/Name");
        complexModel.addSubType(typeModel);

        typeModel = new SimpleTypeModel("City", DataTypeConstants.STRING);
        typeModel.getLabelMap().put("fr", "City");
        typeModel.getLabelMap().put("en", "City");
        typeModel.setXpath("Agency/City");
        typeModel.setTypePath("Agency/City");
        metadata.put("Agency/City", typeModel);
        complexModel.addSubType(typeModel);

        typeModel = new SimpleTypeModel("State", DataTypeConstants.STRING);
        typeModel.getLabelMap().put("fr", "State");
        typeModel.getLabelMap().put("en", "State");
        typeModel.setXpath("Agency/State");
        typeModel.setTypePath("Agency/State");
        metadata.put("Agency/State", typeModel);
        complexModel.addSubType(typeModel);

        typeModel = new SimpleTypeModel("Zip", DataTypeConstants.STRING);
        typeModel.getLabelMap().put("fr", "Zip");
        typeModel.getLabelMap().put("en", "Zip");
        typeModel.setXpath("Agency/Zip");
        typeModel.setTypePath("Agency/Zip");
        metadata.put("Agency/Zip", typeModel);
        complexModel.addSubType(typeModel);

        typeModel = new SimpleTypeModel("Region", DataTypeConstants.STRING);
        typeModel.getLabelMap().put("fr", "Region");
        typeModel.getLabelMap().put("en", "Region");
        typeModel.setXpath("Agency/Region");
        typeModel.setTypePath("Agency/Region");
        metadata.put("Agency/Region", typeModel);
        complexModel.addSubType(typeModel);

        typeModel = new SimpleTypeModel("MoreInfo", DataTypeConstants.STRING);
        typeModel.getLabelMap().put("fr", "MoreInfo");
        typeModel.getLabelMap().put("en", "MoreInfo");
        typeModel.setXpath("Agency/MoreInfo");
        typeModel.setTypePath("Agency/MoreInfo");
        metadata.put("Agency/MoreInfo", typeModel);
        complexModel.addSubType(typeModel);

        EntityModel entity = new EntityModel();
        entity.setMetaDataTypes(metadata);
        entity.setConceptName("Agency");
        String[] keys = { "Agency/Id" };
        entity.setKeys(keys);

        return entity;
    }
}