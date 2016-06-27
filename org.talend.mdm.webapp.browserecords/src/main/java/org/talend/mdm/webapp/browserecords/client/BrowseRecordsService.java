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

import org.talend.mdm.webapp.base.client.exception.ServiceException;
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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 */
@RemoteServiceRelativePath("BrowseRecordsService")
public interface BrowseRecordsService extends RemoteService {

    ItemBasePageLoadResult<ItemBean> queryItemBeans(final QueryModel config, String language) throws ServiceException;

    ItemBean queryItemBeanById(String dataClusterPK, ViewBean viewBean, EntityModel entityModel, String id, String language)
            throws ServiceException;

    ItemBean getItem(ItemBean itemBean, String viewPK, EntityModel entityModel, boolean isStaging, String language)
            throws ServiceException;

    String saveItemBean(ItemBean item, String language) throws ServiceException;

    ItemBasePageLoadResult<ForeignKeyBean> getForeignKeyList(BasePagingLoadConfigImpl config, TypeModel model,
            String foreignKeyFilterValue, String dataClusterPK, String language) throws ServiceException;

    List<Restriction> getForeignKeyPolymTypeList(String xpathForeignKey, String language) throws ServiceException;

    ForeignKeyDrawer switchForeignKeyType(String targetEntityType, String xpathForeignKey, String xpathInfoForeignKey,
            String fkFilter) throws ServiceException;

    EntityModel getEntityModel(String concept, String language) throws ServiceException;

    String getExsitedViewName(String concept) throws ServiceException;

    ViewBean getView(String viewPk, String language) throws ServiceException;

    List<ItemResult> deleteItemBeans(List<ItemBean> items, boolean override, String language) throws ServiceException;

    Map<ItemBean, FKIntegrityResult> checkFKIntegrity(List<ItemBean> selectedItems) throws ServiceException;

    void logicalDeleteItem(ItemBean item, String path, boolean override) throws ServiceException;

    void logicalDeleteItems(List<ItemBean> items, String path, boolean override) throws ServiceException;

    List<ItemBaseModel> getViewsList(String language) throws ServiceException;

    String getCriteriaByBookmark(String bookmark) throws ServiceException;

    List<ItemBaseModel> getUserCriterias(String view) throws ServiceException;

    AppHeader getAppHeader() throws ServiceException;

    String getCurrentDataModel() throws ServiceException;

    String getCurrentDataCluster() throws ServiceException;

    String getCurrentDataCluster(boolean isStaging) throws ServiceException;

    ItemBasePageLoadResult<ItemBaseModel> querySearchTemplates(String view, boolean isShared, BasePagingLoadConfigImpl load)
            throws ServiceException;

    void deleteSearchTemplate(String id) throws ServiceException;

    boolean isExistCriteria(String dataObjectLabel, String id) throws ServiceException;

    void saveCriteria(String viewPK, String templateName, boolean isShared, String criteriaString) throws ServiceException;

    ItemNodeModel getItemNodeModel(ItemBean item, EntityModel entity, boolean isStaging, String language) throws ServiceException;

    ItemNodeModel createDefaultItemNodeModel(ViewBean viewBean, Map<String, List<String>> initDataMap, String language)
            throws ServiceException;

    ItemNodeModel createSubItemNodeModel(ViewBean viewBean, String xml, String typePath, String contextPath, String realType,
            boolean isStaging, String language) throws ServiceException;

    List<String> getMandatoryFieldList(String tableName) throws ServiceException;

    ItemResult saveItem(ViewBean viewBean, String ids, String xml, boolean isCreate, String language) throws ServiceException;

    ItemResult saveItem(String concept, String ids, String xml, boolean isCreate, String language) throws ServiceException;

    ItemResult updateItem(String concept, String ids, Map<String, String> changedNodes, String xml, EntityModel entityModel,
            String language) throws ServiceException;

    List<ItemResult> updateItems(List<UpdateItemModel> updateItems, String language) throws ServiceException;

    ColumnTreeLayoutModel getColumnTreeLayout(String concept) throws ServiceException;

    ForeignKeyModel getForeignKeyModel(String concept, String ids, boolean isStaging, String language) throws ServiceException;

    ForeignKeyBean getForeignKeyBean(String concept, String ids, String xml, String currentXpath, String foreignKey,
            List<String> foreignKeyInfo, String foreignKeyFilter, boolean staging, String language) throws ServiceException;

    List<ItemBaseModel> getRunnableProcessList(String concept, String language) throws ServiceException;

    String processItem(String concept, String[] ids, String transformerPK) throws ServiceException;

    List<String> getLineageEntity(String concept) throws ServiceException;

    List<ItemBaseModel> getSmartViewList(String regex) throws ServiceException;

    ItemBean getItemBeanById(String concept, String ids, String language) throws ServiceException;

    ItemBean getItemBeanById(String concept, String[] ids, String language) throws ServiceException;

    List<VisibleRuleResult> executeVisibleRule(ViewBean viewBean, String xml) throws ServiceException;

    boolean isItemModifiedByOthers(ItemBean itemBean) throws ServiceException;

    String formatValue(FormatModel model) throws ServiceException;

    String getGoldenRecordIdByGroupId(String dataClusterPK, String viewPK, String concept, String[] keys, String groupId)
            throws ServiceException;

    int checkTask(String dataClusterPK, String concept, String groupId) throws ServiceException;

    List<ItemBean> getRecords(String concept, List<String> idsList) throws ServiceException;

    Map<String, List<String>> getForeignKeyValues(String concept, String[] ids, String language) throws ServiceException;

    boolean isExistId(String concept, String[] ids, String language) throws ServiceException;

    List<ForeignKeyBean> getForeignKeySuggestion(BasePagingLoadConfigImpl config, TypeModel model, String foreignKeyFilterValue,
            String dataClusterPK, String language) throws ServiceException;
    
    String handleNavigatorNodeLabel(String jsonString,String language) throws ServiceException;
}
