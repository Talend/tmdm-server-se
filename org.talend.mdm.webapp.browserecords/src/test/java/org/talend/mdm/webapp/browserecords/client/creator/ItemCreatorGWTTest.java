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

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;

import com.google.gwt.junit.client.GWTTestCase;

@SuppressWarnings("nls")
public class ItemCreatorGWTTest extends GWTTestCase {

    public void testCreateDefaultItemBean() {
        LinkedHashMap<String, TypeModel> types = new LinkedHashMap<String, TypeModel>();
        
        ComplexTypeModel productType = new ComplexTypeModel("Product", null);
        productType.addLabel("en", "product info");
        productType.addDescription("en", "this is product info");
        types.put("Product", productType);

        SimpleTypeModel idType = new SimpleTypeModel("Id", DataTypeConstants.STRING);
        idType.addLabel("en", "Identifiant");
        idType.addDescription("", "Identifiant");
        types.put("Product/Id", idType);

        SimpleTypeModel nameType = new SimpleTypeModel("Name", DataTypeConstants.STRING);
        nameType.addLabel("en", "name");
        nameType.addDescription("en", "name");
        types.put("Product/Name", nameType);

        SimpleTypeModel namedType = new SimpleTypeModel("NamedProduct", DataTypeConstants.STRING);
        namedType.addLabel("en", "name");
        namedType.addDescription("en", "name");
        types.put("Product/NamedProduct", namedType);

        ComplexTypeModel featuresType = new ComplexTypeModel("Features", null);
        featuresType.addLabel("en", "");
        featuresType.addDescription("en", "");
        types.put("Product/Features", featuresType);

        ComplexTypeModel sizesType = new ComplexTypeModel("Sizes", null);
        sizesType.addLabel("en", "");
        sizesType.addDescription("en", "");
        types.put("Product/Features/Sizes", sizesType);

        SimpleTypeModel sizeType = new SimpleTypeModel("Size", DataTypeConstants.STRING);
        sizeType.addLabel("en", "");
        sizeType.addDescription("en", "");
        sizeType.setMinOccurs(2);
        sizeType.setMaxOccurs(3);
        types.put("Product/Features/Sizes/Size", sizeType);

        ComplexTypeModel colorsType = new ComplexTypeModel("Colors", null);
        colorsType.addLabel("en", "");
        colorsType.addDescription("en", "");
        types.put("Product/Features/Colors", colorsType);

        SimpleTypeModel colorType = new SimpleTypeModel("Color", DataTypeConstants.STRING);
        colorType.addLabel("en", "");
        colorType.addDescription("en", "");
        colorType.setMinOccurs(3);
        colorType.setMaxOccurs(5);
        types.put("Product/Features/Colors/Color", colorType);

        SimpleTypeModel priceType = new SimpleTypeModel("Price", DataTypeConstants.DECIMAL);
        priceType.addLabel("en", "price");
        priceType.addDescription("en", "price");
        types.put("Product/Price", priceType);

        DataTypeConstants.UNKNOW.setBaseTypeName("date");
        SimpleTypeModel myDateType = new SimpleTypeModel("date", DataTypeConstants.UNKNOW);
        myDateType.addLabel("en", "CreateDate");
        myDateType.addDescription("en", "CreateDate");
        types.put("Product/date", myDateType);

        EntityModel em = new EntityModel();
        em.setMetaDataTypes(types);

        ItemBean item = ItemCreator.createDefaultItemBean("Product", em);

        assertNotNull(item);

        assertEquals("product info", item.getLabel());
        assertEquals("this is product info", item.getDescription());

        assertEquals("", item.get("Product/Id"));
        assertEquals("", item.get("Product/Name"));
        assertEquals("", item.get("Product/NamedProduct"));
        assertEquals("", item.get("Product/Price"));
        assertTrue(item.get("Product/date").toString().length() >= 10);

        Object sizes = item.get("Product/Features/Sizes/Size");
        assertTrue(sizes instanceof List);
        assertEquals(2, ((List) sizes).size());

        Object colors = item.get("Product/Features/Colors/Color");
        assertTrue(colors instanceof List);
        assertEquals(3, ((List) colors).size());

    }

    @Override
    public String getModuleName() {
        // GWTTestCase Required
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }
}
