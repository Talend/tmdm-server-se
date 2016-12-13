/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeLayoutModel;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyDrawer;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyModel;
import org.talend.mdm.webapp.browserecords.client.model.FormatModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.model.QueryModel;
import org.talend.mdm.webapp.browserecords.client.model.Restriction;
import org.talend.mdm.webapp.browserecords.client.model.UpdateItemModel;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.ViewHelper;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;

import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.rpc.AsyncCallback;

@PrepareForTest(ItemsToolBar.class)
@SuppressStaticInitializationFor({ "org.talend.mdm.webapp.browserecords.client.widget.ItemsToolBar",
        "org.talend.mdm.webapp.browserecords.client.widget.SearchPanel.SimpleCriterionPanel",
        "org.talend.mdm.webapp.browserecords.client.widget.SearchPanel.AdvancedSearchPanel",
        "org.talend.mdm.webapp.browserecords.client.widget.inputfield.ComboBoxField",
        "org.talend.mdm.webapp.browserecords.client.widget.ItemsListPanel",
        "org.talend.mdm.webapp.browserecords.client.widget.ItemsMainTabPanel",
        "org.talend.mdm.webapp.browserecords.client.util.ViewUtil", "org.talend.mdm.webapp.base.client.widget.ComboBoxEx",
        "com.extjs.gxt.ui.client.widget.form.FormPanel", "com.extjs.gxt.ui.client.widget.ContentPanel",
        "com.extjs.gxt.ui.client.widget.form.TriggerField", "com.extjs.gxt.ui.client.widget.form.TextField",
        "com.extjs.gxt.ui.client.widget.form.Field", "com.extjs.gxt.ui.client.widget.toolbar.ToolBar",
        "com.extjs.gxt.ui.client.widget.Container", "com.extjs.gxt.ui.client.widget.BoxComponent",
        "com.extjs.gxt.ui.client.widget.Component", "com.extjs.gxt.ui.client.event.Observable",
        "com.google.gwt.user.client.ui.Widget", "com.google.gwt.user.client.ui.UIObject" })
@SuppressWarnings("nls")
public class ItemsToolBarGWTTest extends GWTTestCase {

    @Override
    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords";
    }

    public void testSetDefaultView() throws ServiceException {
        MockBrowseRecordsServiceAsync mockService = new MockBrowseRecordsServiceAsync();
        Registry.register(BrowseRecords.BROWSERECORDS_SERVICE, mockService);
        Registry.register(BrowseRecords.USER_SESSION, new UserSession());
        AppHeader appHeader = new AppHeader();
        appHeader.setUseRelations(false);
        BrowseRecords.getSession().put(UserSession.APP_HEADER, appHeader);

        // Select default view
        Cookies.setCookie(PortletConstants.PARAMETER_ENTITY, "Product");
        GenerateContainer.setDefaultView();
        assertEquals(GenerateContainer.getDefaultViewPk(), "Product");

        String pk = ViewHelper.DEFAULT_VIEW_PREFIX + "_" + GenerateContainer.getDefaultViewPk();
        ItemsToolBar bar = new ItemsToolBar();
        assertEquals(bar.getEntityCombo().getValue().get("value"), pk);

        // No default view, select next available view
        Cookies.setCookie(PortletConstants.PARAMETER_ENTITY, "Store");
        GenerateContainer.setDefaultView();
        assertEquals(GenerateContainer.getDefaultViewPk(), "Store");

        String pk1 = ViewHelper.DEFAULT_VIEW_PREFIX + "_" + GenerateContainer.getDefaultViewPk();
        ItemsToolBar bar1 = new ItemsToolBar();
        assertTrue(bar1.getEntityCombo().getValue().get("value").toString().startsWith(pk1));

        // No available view
        Cookies.setCookie(PortletConstants.PARAMETER_ENTITY, "ProductFamily");
        GenerateContainer.setDefaultView();
        assertEquals(GenerateContainer.getDefaultViewPk(), "ProductFamily");

        ItemsToolBar bar2 = new ItemsToolBar();
        assertNull(bar2.getEntityCombo().getValue());

        // No cookie
        GenerateContainer.setDefaultView();
        assertEquals(GenerateContainer.getDefaultViewPk(), "");

        ItemsToolBar bar3 = new ItemsToolBar();
        assertNull(bar3.getEntityCombo().getValue());
    }

    public void testUploadButtonVislable() {
        MockBrowseRecordsServiceAsync mockService = new MockBrowseRecordsServiceAsync();
        Registry.register(BrowseRecords.BROWSERECORDS_SERVICE, mockService);
        Registry.register(BrowseRecords.USER_SESSION, new UserSession());
        AppHeader appHeader = new AppHeader();
        appHeader.setUseRelations(false);
        BrowseRecords.getSession().put(UserSession.APP_HEADER, appHeader);

        ItemsToolBar bar1 = new ItemsToolBar();
        assertEquals(false, bar1.getUploadButton().isVisible());
        assertEquals(false, bar1.getImportMenu().isVisible());
        assertEquals(false, bar1.getExportMenu().isVisible());
    }

    /*
     * public static TestSuite suite() throws Exception { return new PowerMockSuite(ItemsToolBarTest.class); }
     * 
     * public void testCreateDeletePermissions() throws Exception {
     * 
     * // Mock ItemsToolBar to test ItemsToolBar mock_bar = createMockItemsToolbar();
     * 
     * // Mock ItemsListPanel PowerMockito.mockStatic(ItemsListPanel.class); ItemsListPanel mock_items_list_panel =
     * PowerMockito.mock(ItemsListPanel.class);
     * PowerMockito.when(ItemsListPanel.getInstance()).thenReturn(mock_items_list_panel);
     * 
     * // Mock ItemsMainTabPanel PowerMockito.mockStatic(ItemsMainTabPanel.class); ItemsMainTabPanel
     * mock_items_main_tab_panel = PowerMockito.mock(ItemsMainTabPanel.class);
     * PowerMockito.when(ItemsMainTabPanel.getInstance()).thenReturn(mock_items_main_tab_panel);
     * 
     * // Mock import menu to test create permissions Button mock_upload_btn = PowerMockito.mock(Button.class);
     * Whitebox.setInternalState(mock_bar, "uploadBtn", mock_upload_btn); Menu mock_menu =
     * PowerMockito.mock(Menu.class); Component mock_import = PowerMockito.mock(Component.class);
     * PowerMockito.when(mock_upload_btn.getMenu()).thenReturn(mock_menu);
     * PowerMockito.when(mock_menu.getItemByItemId("importRecords")).thenReturn(mock_import);
     * 
     * 
     * // Mock ViewUtil PowerMockito.mockStatic(ViewUtil.class); ComboBoxField mock_combo =
     * PowerMockito.mock(ComboBoxField.class); ItemBaseModel mock_data = PowerMockito.mock(ItemBaseModel.class);
     * PowerMockito.when(mock_combo.getValue()).thenReturn(mock_data);
     * PowerMockito.when(mock_data.get("value")).thenReturn("");
     * PowerMockito.when(ViewUtil.getConceptFromBrowseItemView("")).thenReturn(""); Whitebox.setInternalState(mock_bar,
     * "entityCombo", mock_combo);
     * 
     * 
     * // Mock view bean ViewBean mock_view_bean = PowerMockito.mock(ViewBean.class); EntityModel mock_entity_model =
     * PowerMockito.mock(EntityModel.class); Map<String, TypeModel> mock_map = PowerMockito.mock(Map.class); TypeModel
     * mock_type_model = PowerMockito.mock(TypeModel.class);
     * PowerMockito.when(mock_view_bean.getBindingEntityModel()).thenReturn(mock_entity_model);
     * PowerMockito.when(mock_entity_model.getMetaDataTypes()).thenReturn(mock_map);
     * PowerMockito.when(mock_map.get("")).thenReturn(mock_type_model);
     * PowerMockito.when(mock_type_model.isDenyCreatable()).thenReturn(true);
     * PowerMockito.when(mock_type_model.isDenyLogicalDeletable()).thenReturn(true);
     * PowerMockito.when(mock_type_model.isDenyPhysicalDeleteable()).thenReturn(true);
     * 
     * 
     * // Mock delete objects to test delete permissions Button mock_delete_button = PowerMockito.mock(Button.class);
     * Whitebox.setInternalState(mock_bar, "deleteMenu", mock_delete_button); Menu mock_delete_menu =
     * PowerMockito.mock(Menu.class); Component mock_physical_delete = PowerMockito.mock(Component.class); Component
     * mock_logical_delete = PowerMockito.mock(Component.class);
     * PowerMockito.when(mock_delete_button.getMenu()).thenReturn(mock_delete_menu);
     * PowerMockito.when(mock_delete_menu.getItemByItemId("physicalDelMenuInGrid")).thenReturn(mock_physical_delete);
     * PowerMockito.when(mock_delete_menu.getItemByItemId("logicalDelMenuInGrid")).thenReturn(mock_logical_delete);
     * 
     * 
     * // Mock some private calls ItemsToolBar bar_spy = PowerMockito.spy(mock_bar);
     * PowerMockito.doNothing().when(bar_spy, "updateUserCriteriasList"); PowerMockito.doReturn(true).when(bar_spy,
     * "layout", true);
     * 
     * 
     * // Execute the method bar_spy.updateToolBar(mock_view_bean);
     * 
     * 
     * // Verify we did not enable the import button due to no create permissions Mockito.verify(mock_import,
     * Mockito.times(0)).setEnabled(true);
     * 
     * 
     * // Verify we disabled the entire delete menu since neither logical nor physical delete permissions // Twice
     * because by default we disable it then we disable it again when checking permissions
     * Mockito.verify(mock_delete_button, Mockito.times(2)).setEnabled(false);
     * 
     * 
     * // Verify we did not enable the delete sub-menu items Mockito.verify(mock_physical_delete,
     * Mockito.times(0)).setEnabled(true); Mockito.verify(mock_logical_delete, Mockito.times(0)).setEnabled(true); }
     * 
     * 
     * 
     * public ItemsToolBar createMockItemsToolbar () { // Create instance with nothing initialized, but working methods
     * ItemsToolBar bar = Whitebox.newInstance(ItemsToolBar.class);
     * 
     * // Set all internal instance variables to mock objects that do nothing Whitebox.setInternalState(bar,
     * "simplePanel", PowerMockito.mock(SimpleCriterionPanel.class)); Whitebox.setInternalState(bar, "advancedPanel",
     * PowerMockito.mock(AdvancedSearchPanel.class)); Whitebox.setInternalState(bar, "entityCombo",
     * PowerMockito.mock(ComboBoxField.class)); Whitebox.setInternalState(bar, "searchBut",
     * PowerMockito.mock(Button.class)); Whitebox.setInternalState(bar, "advancedBut",
     * PowerMockito.mock(ToggleButton.class)); Whitebox.setInternalState(bar, "managebookBtn",
     * PowerMockito.mock(Button.class)); Whitebox.setInternalState(bar, "bookmarkBtn", PowerMockito.mock(Button.class));
     * Whitebox.setInternalState(bar, "createBtn", PowerMockito.mock(Button.class)); Whitebox.setInternalState(bar,
     * "deleteMenu", PowerMockito.mock(Button.class)); Whitebox.setInternalState(bar, "uploadBtn",
     * PowerMockito.mock(Button.class)); Whitebox.setInternalState(bar, "service",
     * PowerMockito.mock(BrowseRecordsServiceAsync.class)); Whitebox.setInternalState(bar, "bookmarkName", "");
     * Whitebox.setInternalState(bar, "currentModel", PowerMockito.mock(ItemBaseModel.class));
     * Whitebox.setInternalState(bar, "relWindow", PowerMockito.mock(FKRelRecordWindow.class));
     * Whitebox.setInternalState(bar, "userCriteriasList", PowerMockito.mock(List.class));
     * 
     * return bar; }
     */

    public void testname() throws Exception {
        assertTrue(true);
    }

    class MockBrowseRecordsServiceAsync implements BrowseRecordsServiceAsync {
        @Override
        public void saveItem(ViewBean viewBean, String ids, String xml, boolean isCreate, String language,
                AsyncCallback<ItemResult> callback) {
        }

        @Override
        public void getForeignKeyPolymTypeList(String xpathForeignKey, String language, AsyncCallback<List<Restriction>> callback) {
        }

        @Override
        public void switchForeignKeyType(String targetEntityType, String xpathForeignKey, String xpathInfoForeignKey,
                String fkFilter, AsyncCallback<ForeignKeyDrawer> callback) {
        }

        @Override
        public void queryItemBeans(QueryModel config, String language, AsyncCallback<ItemBasePageLoadResult<ItemBean>> callback) {
        }

        @Override
        public void saveItemBean(ItemBean item, String language, AsyncCallback<String> callback) {
        }

        @Override
        public void getItem(ItemBean itemBean, String viewPK, EntityModel entityModel, boolean isStaging, String language,
                AsyncCallback<ItemBean> callback) {
        }

        @Override
        public void getView(String viewPk, String language, AsyncCallback<ViewBean> callback) {
        }

        @Override
        public void deleteItemBeans(List<ItemBean> items, boolean override, String language,
                AsyncCallback<List<ItemResult>> callback) {
        }

        @Override
        public void checkFKIntegrity(List<ItemBean> selectedItems, AsyncCallback<Map<ItemBean, FKIntegrityResult>> asyncCallback) {
        }

        @Override
        public void logicalDeleteItem(ItemBean item, String path, boolean override, AsyncCallback<Void> callback) {

        }

        @Override
        public void logicalDeleteItems(List<ItemBean> items, String path, boolean override, AsyncCallback<Void> callback) {
        }

        @Override
        public void getViewsList(String language, AsyncCallback<List<ItemBaseModel>> callback) {
            List<ItemBaseModel> list = new ArrayList<ItemBaseModel>();
            ItemBaseModel bm1 = new ItemBaseModel();
            bm1.set("name", "Product");
            bm1.set("value", ViewHelper.DEFAULT_VIEW_PREFIX + "_" + "Product");
            list.add(bm1);

            ItemBaseModel bm2 = new ItemBaseModel();
            bm1.set("name", "Store and Product");
            bm2.set("value", ViewHelper.DEFAULT_VIEW_PREFIX + "_" + "Store#Product");
            list.add(bm2);
            callback.onSuccess(list);
        }

        @Override
        public void getCriteriaByBookmark(String bookmark, AsyncCallback<String> callback) {
        }

        @Override
        public void getUserCriterias(String view, AsyncCallback<List<ItemBaseModel>> callback) {
        }

        @Override
        public void getAppHeader(AsyncCallback<AppHeader> callback) {
        }

        @Override
        public void getCurrentDataModel(AsyncCallback<String> callback) {
        }

        @Override
        public void getCurrentDataCluster(AsyncCallback<String> callback) {
        }

        @Override
        public void querySearchTemplates(String view, boolean isShared, BasePagingLoadConfigImpl load,
                AsyncCallback<ItemBasePageLoadResult<ItemBaseModel>> callback) {
        }

        @Override
        public void deleteSearchTemplate(String id, AsyncCallback<Void> callback) {
        }

        @Override
        public void isExistCriteria(String dataObjectLabel, String id, AsyncCallback<Boolean> callback) {
        }

        @Override
        public void saveCriteria(String viewPK, String templateName, boolean isShared, String criteriaString,
                AsyncCallback<Void> callback) {
        }

        @Override
        public void getMandatoryFieldList(String tableName, AsyncCallback<List<String>> callback) {
        }

        @Override
        public void saveItem(String concept, String ids, String xml, boolean isCreate, String language,
                AsyncCallback<ItemResult> callback) {
        }

        @Override
        public void getColumnTreeLayout(String concept, AsyncCallback<ColumnTreeLayoutModel> callback) {
        }

        @Override
        public void getRunnableProcessList(String concept, String language, AsyncCallback<List<ItemBaseModel>> callback) {
        }

        @Override
        public void processItem(String concept, String[] ids, String transformerPK, AsyncCallback<String> callback) {
        }

        @Override
        public void getLineageEntity(String concept, AsyncCallback<List<String>> callback) {
        }

        @Override
        public void getSmartViewList(String regex, AsyncCallback<List<ItemBaseModel>> callback) {
        }

        @Override
        public void getItemBeanById(String concept, String[] ids, String language, AsyncCallback<ItemBean> callback) {

        }

        @Override
        public void executeVisibleRule(ViewBean viewBean, String xml, AsyncCallback<List<VisibleRuleResult>> asyncCallback) {

        }

        @Override
        public void isItemModifiedByOthers(ItemBean itemBean, AsyncCallback<Boolean> callback) {

        }

        @Override
        public void updateItem(String concept, String ids, Map<String, String> changedNodes, String xml, EntityModel entityModel,
                String language, AsyncCallback<ItemResult> callback) {

        }

        @Override
        public void updateItems(List<UpdateItemModel> updateItems, String language, AsyncCallback<List<ItemResult>> callback) {

        }

        @Override
        public void formatValue(FormatModel model, AsyncCallback<String> callback) {

        }

        @Override
        public void getEntityModel(String concept, String language, AsyncCallback<EntityModel> callback) {

        }

        @Override
        public void createDefaultItemNodeModel(ViewBean viewBean, Map<String, List<String>> initDataMap, String language,
                AsyncCallback<ItemNodeModel> callback) {

        }

        @Override
        public void getForeignKeyValues(String concept, String[] ids, String language,
                AsyncCallback<Map<String, List<String>>> callback) {

        }

        @Override
        public void isExistId(String concept, String[] ids, String language, AsyncCallback<Boolean> callback) {

        }

        @Override
        public void queryItemBeanById(String dataClusterPK, ViewBean viewBean, EntityModel entityModel, String id,
                String language, AsyncCallback<ItemBean> callback) {
        }

        @Override
        public void getGoldenRecordIdByGroupId(String dataClusterPK, String viewPK, String concept, String[] keys,
                String groupId, AsyncCallback<String> callback) {
        }

        @Override
        public void getRecords(String concept, List<String> idsList, AsyncCallback<List<ItemBean>> callback) {

        }

        @Override
        public void getCurrentDataCluster(boolean isStaging, AsyncCallback<String> callback) {

        }

        @Override
        public void getItemNodeModel(ItemBean item, EntityModel entity, boolean isStaging, String language,
                AsyncCallback<ItemNodeModel> callback) {

        }

        @Override
        public void getForeignKeyModel(String concept, String ids, boolean isStaging, String language,
                AsyncCallback<ForeignKeyModel> callback) {

        }

        @Override
        public void createSubItemNodeModel(ViewBean viewBean, String xml, String typePath, String contextPath, String realType,
                boolean isStaging, String language, AsyncCallback<ItemNodeModel> callback) {
        }

        @Override
        public void checkTask(String dataClusterPK, String concept, String groupId, AsyncCallback<Integer> callback) {

        }

        @Override
        public void getForeignKeyBean(String concept, String ids, String xml, String currentXpath, String foreignKey,
                List<String> foreignKeyInfo, String foreignKeyFilter, boolean staging, String language,
                AsyncCallback<ForeignKeyBean> callback) {
        }

        @Override
        public void getItemBeanById(String concept, String ids, String language, AsyncCallback<ItemBean> callback) {
            // TODO Auto-generated method stub

        }

        @Override
        public void getForeignKeyList(BasePagingLoadConfigImpl config, TypeModel model, String foreignKeyFilterValue,
                String dataClusterPK, String language, AsyncCallback<ItemBasePageLoadResult<ForeignKeyBean>> callback) {
        }

        @Override
        public void getForeignKeySuggestion(BasePagingLoadConfigImpl config, TypeModel model, String foreignKeyFilterValue,
                String dataClusterPK, String language, AsyncCallback<List<ForeignKeyBean>> callback) {
        }

        @Override
        public void getExsitedViewName(String concept, AsyncCallback<String> callback) {
        }

        @Override
        public void handleNavigatorNodeLabel(String jsonString, String language, AsyncCallback<String> callback) {
            // TODO Auto-generated method stub

        }

        @Override
        public void bulkUpdateItem(String baseUrl, String concept, String xml, String language, AsyncCallback<String> callback) {
        }
    }
}
