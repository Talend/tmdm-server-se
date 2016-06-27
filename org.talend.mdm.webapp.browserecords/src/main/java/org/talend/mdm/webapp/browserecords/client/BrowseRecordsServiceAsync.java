// ============================================================================
//
// Copyright (C) 2006-2016 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.browserecords.client;

import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.client.model.ItemResult;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ColumnTreeLayoutModel;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyDrawer;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyModel;
import org.talend.mdm.webapp.browserecords.client.model.FormatModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.model.QueryModel;
import org.talend.mdm.webapp.browserecords.client.model.Restriction;
import org.talend.mdm.webapp.browserecords.client.model.UpdateItemModel;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>BrowseRecordsService</code>.
 */
public interface BrowseRecordsServiceAsync {

    void getForeignKeyList(BasePagingLoadConfigImpl config, TypeModel model, String foreignKeyFilterValue, String dataClusterPK,
            String language, AsyncCallback<ItemBasePageLoadResult<ForeignKeyBean>> callback);

    void getForeignKeyPolymTypeList(String xpathForeignKey, String language, AsyncCallback<List<Restriction>> callback);

    void switchForeignKeyType(String targetEntityType, String xpathForeignKey, String xpathInfoForeignKey, String fkFilter,
            AsyncCallback<ForeignKeyDrawer> callback);

    void queryItemBeans(QueryModel config, String language, AsyncCallback<ItemBasePageLoadResult<ItemBean>> callback);

    void queryItemBeanById(String dataClusterPK, ViewBean viewBean, EntityModel entityModel, String id, String language,
            AsyncCallback<ItemBean> callback);

    void saveItemBean(ItemBean item, String language, AsyncCallback<String> callback);

    void getItem(ItemBean itemBean, String viewPK, EntityModel entityModel, boolean isStaging, String language,
            AsyncCallback<ItemBean> callback);

    void getExsitedViewName(String concept, AsyncCallback<String> callback);

    void getView(String viewPk, String language, AsyncCallback<ViewBean> callback);

    void deleteItemBeans(List<ItemBean> items, boolean override, String language, AsyncCallback<List<ItemResult>> callback);

    void checkFKIntegrity(List<ItemBean> selectedItems, AsyncCallback<Map<ItemBean, FKIntegrityResult>> asyncCallback);

    void logicalDeleteItem(ItemBean item, String path, boolean override, AsyncCallback<Void> callback);

    void logicalDeleteItems(List<ItemBean> items, String path, boolean override, AsyncCallback<Void> callback);

    void getViewsList(String language, AsyncCallback<List<ItemBaseModel>> callback);

    void getCriteriaByBookmark(String bookmark, AsyncCallback<String> callback);

    void getUserCriterias(String view, AsyncCallback<List<ItemBaseModel>> callback);

    void getAppHeader(AsyncCallback<AppHeader> callback);

    void getCurrentDataModel(AsyncCallback<String> callback);

    void getCurrentDataCluster(AsyncCallback<String> callback);

    void getCurrentDataCluster(boolean isStaging, AsyncCallback<String> callback);

    void querySearchTemplates(String view, boolean isShared, BasePagingLoadConfigImpl load,
            AsyncCallback<ItemBasePageLoadResult<ItemBaseModel>> callback);

    void deleteSearchTemplate(String id, AsyncCallback<Void> callback);

    void isExistCriteria(String dataObjectLabel, String id, AsyncCallback<Boolean> callback);

    void saveCriteria(String viewPK, String templateName, boolean isShared, String criteriaString, AsyncCallback<Void> callback);

    void getItemNodeModel(ItemBean item, EntityModel entity, boolean isStaging, String language,
            AsyncCallback<ItemNodeModel> callback);

    void getMandatoryFieldList(String tableName, AsyncCallback<List<String>> callback);

    void saveItem(ViewBean viewBean, String ids, String xml, boolean isCreate, String language, AsyncCallback<ItemResult> callback);

    void saveItem(String concept, String ids, String xml, boolean isCreate, String language, AsyncCallback<ItemResult> callback);

    void getColumnTreeLayout(String concept, AsyncCallback<ColumnTreeLayoutModel> callback);

    void getForeignKeyModel(String concept, String ids, boolean isStaging, String language,
            AsyncCallback<ForeignKeyModel> callback);

    void getForeignKeyBean(String concept, String ids, String xml, String currentXpath, String foreignKey,
            List<String> foreignKeyInfo, String foreignKeyFilter, boolean staging, String language,
            AsyncCallback<ForeignKeyBean> callback);

    void getRunnableProcessList(String concept, String language, AsyncCallback<List<ItemBaseModel>> callback);

    void processItem(String concept, String[] ids, String transformerPK, AsyncCallback<String> callback);

    void getLineageEntity(String concept, AsyncCallback<List<String>> callback);

    void getSmartViewList(String regex, AsyncCallback<List<ItemBaseModel>> callback);

    void getItemBeanById(String concept, String ids, String language, AsyncCallback<ItemBean> callback);
    
    void handleNavigatorNodeLabel(String jsonString, String language, AsyncCallback<String> callback);

    void getItemBeanById(String concept, String[] ids, String language, AsyncCallback<ItemBean> callback);

    void executeVisibleRule(ViewBean viewBean, String xml, AsyncCallback<List<VisibleRuleResult>> asyncCallback);

    void isItemModifiedByOthers(ItemBean itemBean, AsyncCallback<Boolean> callback);

    void updateItem(String concept, String ids, Map<String, String> changedNodes, String xml, EntityModel entityModel,
            String language, AsyncCallback<ItemResult> callback);

    void updateItems(List<UpdateItemModel> updateItems, String language, AsyncCallback<List<ItemResult>> callback);

    void getGoldenRecordIdByGroupId(String dataClusterPK, String viewPK, String concept, String[] keys, String groupId,
            AsyncCallback<String> callback);

    void checkTask(String dataClusterPK, String concept, String groupId, AsyncCallback<Integer> callback);

    void getRecords(String concept, List<String> idsList, AsyncCallback<List<ItemBean>> callback);

    void formatValue(FormatModel model, AsyncCallback<String> callback);

    void getEntityModel(String concept, String language, AsyncCallback<EntityModel> callback);

    void createDefaultItemNodeModel(ViewBean viewBean, Map<String, List<String>> initDataMap, String language,
            AsyncCallback<ItemNodeModel> callback);

    void createSubItemNodeModel(ViewBean viewBean, String xml, String typePath, String contextPath, String realType,
            boolean isStaging, String language, AsyncCallback<ItemNodeModel> callback);

    void getForeignKeyValues(String concept, String[] ids, String language, AsyncCallback<Map<String, List<String>>> callback);

    void isExistId(String concept, String[] ids, String language, AsyncCallback<Boolean> callback);

    void getForeignKeySuggestion(BasePagingLoadConfigImpl config, TypeModel model, String foreignKeyFilterValue,
            String dataClusterPK, String language, AsyncCallback<List<ForeignKeyBean>> callback);
}
