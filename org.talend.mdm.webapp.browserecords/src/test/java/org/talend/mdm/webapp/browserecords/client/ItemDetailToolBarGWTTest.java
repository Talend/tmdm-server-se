/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client;

import java.util.LinkedHashMap;

import org.talend.mdm.webapp.base.client.ServiceEnhancer;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.util.StagingConstant;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.shared.AppHeader;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.amalto.core.storage.task.StagingConstants;
import com.extjs.gxt.ui.client.Registry;
import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

@SuppressWarnings("nls")
public class ItemDetailToolBarGWTTest extends GWTTestCase {

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        Registry.register(BrowseRecords.USER_SESSION, new UserSession());
        AppHeader appHeader = new AppHeader();
        appHeader.setUseRelations(false);
        BrowseRecords.getSession().put(UserSession.APP_HEADER, appHeader);

        ServiceDefTarget browseRecordService = GWT.create(BrowseRecordsService.class);
        ServiceEnhancer.customizeService(browseRecordService);
        Registry.register(BrowseRecords.BROWSERECORDS_SERVICE, browseRecordService);

        ServiceDefTarget browseStagingRecordService = GWT.create(BrowseStagingRecordsService.class);
        ServiceEnhancer.customizeService(browseStagingRecordService);
        Registry.register(BrowseRecords.BROWSESTAGINGRECORDS_SERVICE, browseStagingRecordService);
        ServiceFactory.initialize(new DefaultServiceFactoryImpl());
    }

    public void testItemDetailToolBar() {
        Registry.register(BrowseRecords.USER_SESSION, new UserSession());
        AppHeader appHeader = new AppHeader();
        appHeader.setUseRelations(false);
        BrowseRecords.getSession().put(UserSession.APP_HEADER, appHeader);

        ViewBean viewBean = new ViewBean();
        EntityModel entityModel = new EntityModel();
        LinkedHashMap<String, TypeModel> metaDataTypes = new LinkedHashMap<String, TypeModel>();
        metaDataTypes.put("Product", new SimpleTypeModel());
        entityModel.setMetaDataTypes(metaDataTypes);
        viewBean.setBindingEntityModel(entityModel);

        String ids = "1";
        String concept = "Product";
        String xml = "";
        ItemBean itemBean = new ItemBean(concept, ids, xml);
        itemBean.set(concept + StagingConstant.STAGING_STATUS, StagingConstants.SUCCESS_VALIDATE);

        boolean isStaging = true;
        String operation = ItemDetailToolBar.VIEW_OPERATION;
        boolean isFkToolBar = false;
        ItemsDetailPanel itemsDetailPanel = null;
        boolean openTab = true;
        ItemDetailToolBar toolBar = new ItemDetailToolBar(isStaging, itemBean, operation, isFkToolBar, viewBean,
                itemsDetailPanel, openTab);
        assertNotNull(toolBar.getItemByItemId("masterRecordButton"));

        isStaging = false;
        itemBean.setTaskId("123");
        toolBar = new ItemDetailToolBar(isStaging, itemBean, operation, isFkToolBar, viewBean, itemsDetailPanel, openTab);
        assertNotNull(toolBar.getItemByItemId("stagingRecordsButton"));
    }

    @Override
    public String getModuleName() {
        // GWTTestCase Required
        return "org.talend.mdm.webapp.browserecords.TestBrowseRecords"; //$NON-NLS-1$
    }
}
