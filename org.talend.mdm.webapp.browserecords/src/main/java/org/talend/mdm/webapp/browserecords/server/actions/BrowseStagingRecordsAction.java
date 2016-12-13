/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.server.actions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.talend.mdm.webapp.base.client.exception.ServiceException;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ItemBasePageLoadResult;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseStagingRecordsService;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.QueryModel;
import org.talend.mdm.webapp.browserecords.client.util.StagingConstant;
import org.talend.mdm.webapp.browserecords.server.bizhelpers.ViewHelper;
import org.talend.mdm.webapp.browserecords.shared.FKIntegrityResult;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.amalto.core.server.StorageAdmin;

public class BrowseStagingRecordsAction extends BrowseRecordsAction implements BrowseStagingRecordsService {

    private final Logger LOG = Logger.getLogger(BrowseStagingRecordsAction.class);

    @Override
    public ViewBean getView(String viewPk, String language) throws ServiceException {
        ViewBean view = super.getView(viewPk, language);
        String concept = ViewHelper.getConceptFromDefaultViewName(viewPk);
        EntityModel em = view.getBindingEntityModel();
        Map<String, TypeModel> metadatas = em.getMetaDataTypes();

        SimpleTypeModel stagingTaskidType = new SimpleTypeModel(StagingConstant.STAGING_TASKID, DataTypeConstants.STRING);
        stagingTaskidType.setXpath(concept + StagingConstant.STAGING_TASKID);
        SimpleTypeModel stagingStatusType = new SimpleTypeModel(StagingConstant.STAGING_STATUS, DataTypeConstants.INT);
        stagingStatusType.setXpath(concept + StagingConstant.STAGING_STATUS);
        SimpleTypeModel stagingErrorType = new SimpleTypeModel(StagingConstant.STAGING_ERROR, DataTypeConstants.STRING);
        stagingErrorType.setXpath(concept + StagingConstant.STAGING_ERROR);
        SimpleTypeModel stagingSourceType = new SimpleTypeModel(StagingConstant.STAGING_SOURCE, DataTypeConstants.STRING);
        stagingSourceType.setXpath(concept + StagingConstant.STAGING_SOURCE);

        metadatas.put(concept + StagingConstant.STAGING_TASKID, stagingTaskidType);
        metadatas.put(concept + StagingConstant.STAGING_STATUS, stagingStatusType);
        metadatas.put(concept + StagingConstant.STAGING_ERROR, stagingErrorType);
        metadatas.put(concept + StagingConstant.STAGING_SOURCE, stagingSourceType);

        Map<String, String> searchables = view.getSearchables();
        Locale locale = new Locale(language);

        searchables.put(concept + StagingConstant.STAGING_TASKID, MESSAGES.getMessage(locale, "match_group")); //$NON-NLS-1$
        searchables.put(concept + StagingConstant.STAGING_STATUS, MESSAGES.getMessage(locale, "status")); //$NON-NLS-1$
        searchables.put(concept + StagingConstant.STAGING_ERROR, MESSAGES.getMessage(locale, "error")); //$NON-NLS-1$
        searchables.put(concept + StagingConstant.STAGING_SOURCE, MESSAGES.getMessage(locale, "source")); //$NON-NLS-1$ 
        return view;
    }

    @Override
    public ItemBasePageLoadResult<ItemBean> queryItemBeans(QueryModel config, String language) throws ServiceException {
        if (!config.getDataClusterPK().endsWith(StorageAdmin.STAGING_SUFFIX)) {
            config.setDataClusterPK(config.getDataClusterPK() + StorageAdmin.STAGING_SUFFIX);
        }
        return super.queryItemBeans(config, language);
    }

    @Override
    public String getCurrentDataCluster() throws ServiceException {
        String cluster = super.getCurrentDataCluster();
        if (cluster != null && !cluster.endsWith(StorageAdmin.STAGING_SUFFIX)) {
            cluster += StorageAdmin.STAGING_SUFFIX;
        }
        return cluster;
    }

    @Override
    public Map<ItemBean, FKIntegrityResult> checkFKIntegrity(List<ItemBean> selectedItems) throws ServiceException {
        try {
            Map<ItemBean, FKIntegrityResult> itemBeanToResult = new HashMap<ItemBean, FKIntegrityResult>(selectedItems.size());
            for (ItemBean selectedItem : selectedItems) {
                itemBeanToResult.put(selectedItem, FKIntegrityResult.ALLOWED);
            }
            return itemBeanToResult;
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e.getLocalizedMessage());
        }
    }

    @Override
    protected void dynamicAssemble(ItemBean itemBean, EntityModel entityModel, String language) throws Exception {
        Map<String, TypeModel> metadatas = entityModel.getMetaDataTypes();
        String concept = entityModel.getConceptName();
        metadatas.remove(concept + StagingConstant.STAGING_TASKID);
        metadatas.remove(concept + StagingConstant.STAGING_STATUS);
        metadatas.remove(concept + StagingConstant.STAGING_ERROR);
        metadatas.remove(concept + StagingConstant.STAGING_SOURCE);
        super.dynamicAssemble(itemBean, entityModel, language);
    }

    @Override
    public void dynamicAssembleByResultOrder(ItemBean itemBean, ViewBean viewBean, EntityModel entityModel,
            Map<String, EntityModel> map, String language) throws Exception {
        List<String> viewableXpaths = new ArrayList<String>(viewBean.getViewableXpaths());
        viewableXpaths.add(entityModel.getConceptName() + StagingConstant.STAGING_TASKID);
        viewableXpaths.add(entityModel.getConceptName() + StagingConstant.STAGING_STATUS);
        viewableXpaths.add(entityModel.getConceptName() + StagingConstant.STAGING_ERROR);
        viewableXpaths.add(entityModel.getConceptName() + StagingConstant.STAGING_SOURCE);
        org.talend.mdm.webapp.browserecords.server.util.CommonUtil.dynamicAssembleByResultOrder(itemBean, viewableXpaths,
                entityModel, map, language, true);
    }

    @Override
    protected boolean isStaging() {
        return true;
    }

}
