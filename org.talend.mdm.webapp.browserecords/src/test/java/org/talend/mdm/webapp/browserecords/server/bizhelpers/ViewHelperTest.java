/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.bizhelpers;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.talend.mdm.webapp.base.client.model.DataTypeCustomized;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;

import com.amalto.core.webservice.WSView;

@SuppressWarnings("nls")
public class ViewHelperTest extends TestCase {
    
    private WSView wsView;
    private String dataModel;
    private String language;
    private EntityModel entityModel;
    private String[] searchableBusinessElements = {"ProductFamily","ProductFamily/Id","ProductFamily/Name", "ProductFamily/ChangeStatus"};
    private String[] viewableBusinessElements = {"ProductFamily/Id","ProductFamily/Name", "ProductFamily/ChangeStatus"};
    
    public ViewHelperTest(){
        initData();
    }
    
    public void testGetConceptFromDefaultViewName(){
        String viewName = "Browse_items_ProductFamily";        
        assertEquals("ProductFamily", ViewHelper.getConceptFromDefaultViewName(viewName));
        viewName = "#Browse_items_ProductFamily";
        assertEquals("", ViewHelper.getConceptFromDefaultViewName(viewName));
        viewName = "Browse_items_ProductFamily#Family2";
        assertEquals("ProductFamily", ViewHelper.getConceptFromDefaultViewName(viewName));
    }
    
    public void testGetViewLabel(){
        String language = "en";
        assertEquals("Product Family", ViewHelper.getViewLabel(language, wsView));
        language = "fr";
        assertEquals("Famille Produit", ViewHelper.getViewLabel(language, wsView));
    }
    
    public void testGetViewables(){       
        assertEquals(viewableBusinessElements, ViewHelper.getViewables(wsView));      
    }
    
    public void testGetSearchables(){        
        Map<String, String> result = new HashMap<String, String>();
        result.put("ProductFamily", "Whole content");
        result.put("ProductFamily/Id", "id");
        result.put("ProductFamily/Name", "name");
        result.put("ProductFamily/ChangeStatus", "changeSatus");
        language = "en";
        assertEquals(result, ViewHelper.getSearchables(wsView, dataModel, language, entityModel));
        result.clear();
        
        result.put("ProductFamily", "Tout le contenu");
        result.put("ProductFamily/Id", "rf-id");
        result.put("ProductFamily/Name", "rf-name");
        result.put("ProductFamily/ChangeStatus", "rf-changeSatus");
        language = "fr";
        assertEquals(result, ViewHelper.getSearchables(wsView, dataModel, language, entityModel));
    }
    
    /**
     * DOC Administrator Comment method "initData".
     */
    private void initData(){
        wsView = new WSView();
        wsView.setName("Browse_items_ProductFamily");
        wsView.setDescription("[FR:Famille Produit][EN:Product Family]");
        wsView.setSearchableBusinessElements(searchableBusinessElements);
        wsView.setViewableBusinessElements(viewableBusinessElements);
        
        dataModel = "Product";
        language = "en";
        entityModel = new EntityModel();
        entityModel.setConceptName("ProductFamily");
        String[] keys = {"1"};
        entityModel.setKeys(keys);
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();
        ComplexTypeModel family = new ComplexTypeModel();
        
        SimpleTypeModel id = new SimpleTypeModel("id",new DataTypeCustomized("",""));
        id.addLabel("en", "id");
        id.addLabel("fr", "rf-id");
        SimpleTypeModel name = new SimpleTypeModel("name",new DataTypeCustomized("",""));
        name.addLabel("en", "name");
        name.addLabel("fr", "rf-name");
        SimpleTypeModel changeSatus = new SimpleTypeModel("changeSatus",new DataTypeCustomized("",""));
        changeSatus.addLabel("en", "changeSatus");
        changeSatus.addLabel("fr", "rf-changeSatus");
        
        metaDataTypes.put("ProductFamily", family);
        metaDataTypes.put("ProductFamily/Id", id);
        metaDataTypes.put("ProductFamily/Name", name);
        metaDataTypes.put("ProductFamily/ChangeStatus", changeSatus);        
        entityModel.setMetaDataTypes(metaDataTypes);    
    }
}
