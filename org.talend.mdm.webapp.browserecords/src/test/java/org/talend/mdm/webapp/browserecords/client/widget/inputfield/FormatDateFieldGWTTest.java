/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.model.ItemResult;
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
import org.talend.mdm.webapp.browserecords.client.util.DateUtil;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.ViewHelper;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.form.DateTimePropertyEditor;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.rpc.AsyncCallback;

@SuppressWarnings("nls")
public class FormatDateFieldGWTTest extends GWTTestCase {

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        MockBrowseRecordsServiceAsync mockService = new MockBrowseRecordsServiceAsync();
        UserSession session = new UserSession();
        session.put(UserSession.APP_HEADER, new AppHeader());
        Registry.register(BrowseRecords.USER_SESSION, session);
        Registry.register(BrowseRecords.BROWSERECORDS_SERVICE, mockService);
    }

    public void testCompareDateAndString() {

        // 1. Date (value = "2012-05-08", date = 2012-05-08)
        String value = "2012-05-08";
        FormatDateField dateField = new FormatDateField();
        dateField.setPropertyEditor(new DateTimePropertyEditor(DateUtil.datePattern));
        Date date = DateUtil.convertStringToDate(value.toString());
        assertEquals(false, compareDateAndString(dateField.getPropertyEditor(), date, value));

        // 2. Date (value = "2012-05-09", date = 2012-05-08)
        value = "2012-05-09";
        assertEquals(true, compareDateAndString(dateField.getPropertyEditor(), date, value));

        // 3. DateTime (value = "2012-05-08T00:00:00", date = 2012-05-08T00:00:00)
        value = "2012-05-08T00:00:00";
        dateField.setPropertyEditor(new DateTimePropertyEditor(DateUtil.formatDateTimePattern));
        date = DateUtil.convertStringToDate(DateUtil.dateTimePattern, value.toString());
        dateField.setValue(date);
        assertEquals(false, compareDateAndString(dateField.getPropertyEditor(), date, value));

        // 4. DateTime (value = "2012-05-09T00:00:00", date = 2012-05-08T00:00:00)
        value = "2012-05-09T00:00:00";
        assertEquals(true, compareDateAndString(dateField.getPropertyEditor(), date, value));

        // 5. value = date = null;
        assertEquals(false, compareDateAndString(dateField.getPropertyEditor(), null, null));

        // 6. value = null, date != null
        assertEquals(true, compareDateAndString(dateField.getPropertyEditor(), date, null));

        // 7. value != null, date = null
        assertEquals(true, compareDateAndString(dateField.getPropertyEditor(), null, value));

    }

    public void testValidateValue() {
        String value = "2012-05-09T00:00:00";
        FormatDateField dateField = new FormatDateField();
        dateField.setPropertyEditor(new DateTimePropertyEditor(DateUtil.formatDateTimePattern));
        Date date = DateUtil.convertStringToDate(DateUtil.formatDateTimePattern, value);

        assertEquals(true, dateField.validateValue(""));
        assertEquals(true, dateField.validateValue(value));
        dateField.setValue(date);
        assertEquals(true, dateField.validateValue(value));

        value = "2012-05-09";
        dateField.setValue(null);
        assertEquals(false, dateField.validateValue(value));

        dateField.setValue(date);
        dateField.setFormatPattern(DateUtil.formatDateTimePattern);
        assertEquals(true, dateField.validateValue(value));
        
        dateField.setFormatPattern("%1$te/%1$tm/%1$tY");
        value = "02/05/2016";
        assertEquals(true, dateField.validateValue(value));
        
    }

    public void testGetRawValue() {
        String value = "2012-05-09T00:00:00";
        FormatDateField dateField = new FormatDateField();
        dateField.setPropertyEditor(new DateTimePropertyEditor(DateUtil.formatDateTimePattern));
        dateField.setFormatPattern(DateUtil.formatDateTimePattern);
        Date date = DateUtil.convertStringToDate(DateUtil.formatDateTimePattern, value);
        dateField.render(DOM.createElement("createDate"));
        assertEquals("", dateField.getRawValue());
        dateField.setValue(date);
        assertEquals("2012-05-09T00:00:00", dateField.getRawValue());

        value = "2012-05-09";
        dateField = new FormatDateField();
        dateField.setPropertyEditor(new DateTimePropertyEditor(DateUtil.datePattern));
        dateField.setFormatPattern(DateUtil.datePattern);
        date = DateUtil.convertStringToDate(DateUtil.datePattern, value);
        dateField.render(DOM.createElement("createDate"));
        assertEquals("", dateField.getRawValue());
        dateField.setValue(date);
        assertEquals("2012-05-09", dateField.getRawValue());

        dateField.setFormatPattern("%1$te/%1$tm/%1$tY");
        dateField.setRawValue("01/01/2015");
        assertEquals("2012-05-09", dateField.getRawValue());

    }

    private boolean compareDateAndString(DateTimePropertyEditor propertyEditor, Date date, String objectValue) {
        if (date == null && objectValue == null)
            return false;
        if (date != null && objectValue == null)
            return true;
        if (date == null && objectValue != null)
            return true;
        // convert date to string according to the DateTimePropertyEditor
        String str = propertyEditor.getStringValue(date);
        if (str.equalsIgnoreCase(objectValue))
            return false;
        else
            return true;
    }

    public String getModuleName() {
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords";
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
            if (model.getFormat().equals(DateUtil.formatDateTimePattern)) {
                callback.onSuccess("2012-05-09T00:00:00");
            } else if (model.getFormat().equals(DateUtil.datePattern)) {
                callback.onSuccess("2012-05-09");
            }
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
        }

        @Override
        public void bulkUpdateItem(String baseUrl, String concept, String xml, String language, AsyncCallback<String> callback) {
        }
    }
}