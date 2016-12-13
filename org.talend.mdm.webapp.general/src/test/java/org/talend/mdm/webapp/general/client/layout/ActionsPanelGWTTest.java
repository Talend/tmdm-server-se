/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client.layout;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.client.util.UserContextUtil;
import org.talend.mdm.webapp.general.client.General;
import org.talend.mdm.webapp.general.client.GeneralServiceAsync;
import org.talend.mdm.webapp.general.model.ActionBean;
import org.talend.mdm.webapp.general.model.ComboBoxModel;
import org.talend.mdm.webapp.general.model.LanguageBean;
import org.talend.mdm.webapp.general.model.MenuGroup;
import org.talend.mdm.webapp.general.model.ProductInfo;
import org.talend.mdm.webapp.general.model.UserBean;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.AsyncCallback;

@SuppressWarnings("nls")
public class ActionsPanelGWTTest extends GWTTestCase {

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();

        Registry.register(General.OVERALL_SERVICE, new MockGeneralServiceAsync());
    }

    public void testComboBoxTitle() {
        String language = "en";
        UrlUtil.setCurrentLocale(language);
        UserContextUtil.setLanguage(language);

        ActionsPanel actionsPanel = ActionsPanel.getInstance();
        assertEquals("", actionsPanel.getDataModelBox().getTitle());
        assertEquals("", actionsPanel.getDataContainerBox().getTitle());

        actionsPanel.loadAction(mockActionBean());

        assertEquals("Product-english", actionsPanel.getDataModelBox().getTitle());
        assertEquals("Product-english", actionsPanel.getDataContainerBox().getTitle());

        language = "fr";
        UrlUtil.setCurrentLocale(language);
        UserContextUtil.setLanguage(language);

        actionsPanel.loadAction(mockActionBean());

        assertEquals("Produit-french", actionsPanel.getDataModelBox().getTitle());
        assertEquals("Produit-french", actionsPanel.getDataContainerBox().getTitle());
        
        language = "zh";
        UrlUtil.setCurrentLocale(language);
        UserContextUtil.setLanguage(language);

        actionsPanel.loadAction(mockActionBean());

        assertEquals("", actionsPanel.getDataModelBox().getTitle());
        assertEquals("", actionsPanel.getDataContainerBox().getTitle());

        //if the description is old, have no the multiple language, will be origin value
        ActionBean actionBean = mockActionBean() ;
        actionBean.setCurrentCluster("FKBug");
        actionBean.setCurrentModel("FKBug");
        
        language = "en";
        UrlUtil.setCurrentLocale(language);
        UserContextUtil.setLanguage(language);
        
        actionsPanel.loadAction(actionBean);

        assertEquals("FKBug", actionsPanel.getDataModelBox().getTitle());
        assertEquals("FKBug", actionsPanel.getDataContainerBox().getTitle());
    }

    public void testGetDataModelListForTransferToCurrentLanguageValue() {
        String language = "en";
        UrlUtil.setCurrentLocale(language);
        UserContextUtil.setLanguage(language);

        ActionsPanel actionsPanel = ActionsPanel.getInstance();

        List<ComboBoxModel> oldModelList = mockActionBean().getModels();
        assertEquals(6, oldModelList.size());
        List<ComboBoxModel> transferModelList = actionsPanel.getDataModelListForTransferToCurrentLanguageValue(oldModelList);
        assertNotNull(transferModelList);
        assertEquals(oldModelList.size(), transferModelList.size());
        assertModelListForEnglish(transferModelList);

        language = "fr";
        UrlUtil.setCurrentLocale(language);
        UserContextUtil.setLanguage(language);

        oldModelList = mockActionBean().getModels();
        assertEquals(6, oldModelList.size());
        transferModelList = actionsPanel.getDataModelListForTransferToCurrentLanguageValue(oldModelList);
        assertNotNull(transferModelList);
        assertEquals(oldModelList.size(), transferModelList.size());
        assertModelListForFrench(transferModelList);
        
        language = "zh";
        UrlUtil.setCurrentLocale(language);
        UserContextUtil.setLanguage(language);

        oldModelList = mockActionBean().getModels();
        assertEquals(6, oldModelList.size());
        transferModelList = actionsPanel.getDataModelListForTransferToCurrentLanguageValue(oldModelList);
        assertNotNull(transferModelList);
        assertEquals(oldModelList.size(), transferModelList.size());
        assertModelListForChinese(transferModelList);
    }

    public void testGetClusterListCopyModelDescription() {
        String language = "en";
        UrlUtil.setCurrentLocale(language);
        UserContextUtil.setLanguage(language);

        ActionsPanel actionsPanel = ActionsPanel.getInstance();

        List<ComboBoxModel> modelList = actionsPanel.getDataModelListForTransferToCurrentLanguageValue(mockActionBean()
                .getModels());
        assertModelListForEnglish(modelList);

        List<ComboBoxModel> oldClusterList = mockActionBean().getClusters();
        assertEquals(6, modelList.size());
        assertEquals(modelList.size(), oldClusterList.size());

        List<ComboBoxModel> transferClusterList = actionsPanel.getClusterListCopyModelDescription(oldClusterList, modelList);
        assertNotNull(transferClusterList);
        assertEquals(oldClusterList.size(), transferClusterList.size());
        assertModelListForEnglish(transferClusterList);

        language = "fr";
        UrlUtil.setCurrentLocale(language);
        UserContextUtil.setLanguage(language);

        modelList = actionsPanel.getDataModelListForTransferToCurrentLanguageValue(mockActionBean().getModels());
        assertModelListForFrench(modelList);

        transferClusterList = actionsPanel.getClusterListCopyModelDescription(oldClusterList, modelList);
        assertNotNull(transferClusterList);
        assertEquals(oldClusterList.size(), transferClusterList.size());
        assertModelListForFrench(transferClusterList);
        
        language = "zh";
        UrlUtil.setCurrentLocale(language);
        UserContextUtil.setLanguage(language);

        modelList = actionsPanel.getDataModelListForTransferToCurrentLanguageValue(mockActionBean().getModels());
        assertModelListForChinese(modelList);

        transferClusterList = actionsPanel.getClusterListCopyModelDescription(oldClusterList, modelList);
        assertNotNull(transferClusterList);
        assertEquals(oldClusterList.size(), transferClusterList.size());
        assertModelListForChinese(transferClusterList);

    }

    private void assertModelListForEnglish(List<ComboBoxModel> modelList) {
        for (ComboBoxModel model : modelList) {
            if (model.getValue().equals("aac")) {
                assertEquals("aac-english", model.getText());
            }
            if (model.getValue().equals("Product")) {
                assertEquals("Product-english", model.getText());
            }
            if (model.getValue().equals("ReadOnly")) {
                assertEquals("ReadOnly-english", model.getText());
            }
            if (model.getValue().equals("sort")) {
                assertEquals("sort-english", model.getText());
            }
            if (model.getValue().equals("testType")) {
                assertEquals("testType-english", model.getText());
            }
            if (model.getValue().equals("FKBug")) {
                assertEquals("FKBug", model.getText());
            }
        }
    }

    private void assertModelListForFrench(List<ComboBoxModel> modelList) {
        for (ComboBoxModel model : modelList) {
            if (model.getValue().equals("aac")) {
                assertEquals("aac-french", model.getText());
            }
            if (model.getValue().equals("Product")) {
                assertEquals("Produit-french", model.getText());
            }
            if (model.getValue().equals("ReadOnly")) {
                assertEquals("ReadOnly-french", model.getText());
            }
            if (model.getValue().equals("sort")) {
                assertEquals("sort-french", model.getText());
            }
            if (model.getValue().equals("testType")) {
                assertEquals("testType-french", model.getText());
            }
            if (model.getValue().equals("FKBug")) {
                assertEquals("FKBug", model.getText());
            }
        }
    }
    
    private void assertModelListForChinese(List<ComboBoxModel> modelList) {
        for (ComboBoxModel model : modelList) {
            if (model.getValue().equals("aac")) {
                assertEquals("aac-中文", model.getText());
            }
            if (model.getValue().equals("Product")) {
                assertEquals("", model.getText());
            }
            if (model.getValue().equals("ReadOnly")) {
                assertEquals("ReadOnly-中文", model.getText());
            }
            if (model.getValue().equals("sort")) {
                assertEquals("sort-中文", model.getText());
            }
            if (model.getValue().equals("testType")) {
                assertEquals("", model.getText());
            }
            if (model.getValue().equals("FKBug")) {
                assertEquals("FKBug", model.getText());
            }
        }
    }

    private ActionBean mockActionBean() {
        ActionBean action = new ActionBean();
        action.setClusters(getClusters());
        action.setModels(getModels());
        action.setCurrentCluster("Product");
        action.setCurrentModel("Product");
        return action;
    }

    private List<ComboBoxModel> getClusters() {
        List<ComboBoxModel> clusterList = new ArrayList<ComboBoxModel>();
        clusterList.add(new ComboBoxModel("", "aac"));
        clusterList.add(new ComboBoxModel("", "Product"));
        clusterList.add(new ComboBoxModel("", "ReadOnly"));
        clusterList.add(new ComboBoxModel("", "sort"));
        clusterList.add(new ComboBoxModel("", "testType"));
        clusterList.add(new ComboBoxModel("", "FKBug"));
        return clusterList;
    }

    private List<ComboBoxModel> getModels() {
        List<ComboBoxModel> modelList = new ArrayList<ComboBoxModel>();
        modelList.add(new ComboBoxModel("[FR:aac-french][EN:aac-english][ZH:aac-中文]", "aac"));
        modelList.add(new ComboBoxModel("[FR:Produit-french][EN:Product-english]", "Product"));
        modelList.add(new ComboBoxModel("[FR:ReadOnly-french][EN:ReadOnly-english][ZH:ReadOnly-中文]", "ReadOnly"));
        modelList.add(new ComboBoxModel("[FR:sort-french][EN:sort-english][ZH:sort-中文]", "sort"));
        modelList.add(new ComboBoxModel("[FR:testType-french][EN:testType-english]", "testType"));
        modelList.add(new ComboBoxModel("FKBug", "FKBug"));
        return modelList;
    }

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.general.TestGeneral";
    }

    class MockGeneralServiceAsync implements GeneralServiceAsync {

        @Override
        public void getProductInfo(AsyncCallback<ProductInfo> callback) {
        }

        @Override
        public void getMenus(String language, AsyncCallback<MenuGroup> callback) {
        }

        @Override
        public void setClusterAndModel(String cluster, String model, AsyncCallback<Void> callback) {
        }

        @Override
        public void getUser(AsyncCallback<UserBean> callback) {
        }

        @Override
        public void getLanguages(String language, AsyncCallback<List<LanguageBean>> callback) {
        }

        @Override
        public void getAction(AsyncCallback<ActionBean> callback) {
        }

        @Override
        public void logout(AsyncCallback<Void> callback) {
        }

        @Override
        public void isExpired(String language, AsyncCallback<Boolean> callback) {
        }

        @Override
        public void supportStaging(String dataCluster, AsyncCallback<Boolean> callback) {
        }

        @Override
        public void setDefaultLanguage(String language, AsyncCallback<Void> callback) {
        }

        @Override
        public void isEnterpriseVersion(AsyncCallback<Boolean> callback) {
            callback.onSuccess(true);
        }
        
    }
}