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

import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.client.creator.ItemCreator;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.BreadCrumbModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

public class BulkUpdatePanel extends ContentPanel {

    private List<Map<String, Object>> keyMapList;

    private EntityModel entityModel;

    private ViewBean viewBean;

    private boolean isStaging;

    public BulkUpdatePanel(EntityModel entityModel, ViewBean viewBean, List<Map<String, Object>> keyMapList, boolean isStaging) {
        this.entityModel = entityModel;
        this.viewBean = viewBean;
        this.keyMapList = keyMapList;
        this.isStaging = isStaging;
        initPanel();
    }
    
    private void initPanel() {
    	setId("BulkUpdatePanel");
        setHeading(MessagesFactory.getMessages().bulkUpdate_title());
        setHeaderVisible(false);
        setLayout(new FitLayout());

        ItemBean itemBean = ItemCreator.createDefaultItemBean(viewBean.getBindingEntityModel().getConceptName(), entityModel);
        ItemsDetailPanel bulkUpdateDetailPanel = ItemsDetailPanel.newInstance();
        bulkUpdateDetailPanel.clearAll();
        bulkUpdateDetailPanel.setOutMost(true);
        ItemPanel itemPanel = new ItemPanel(isStaging, viewBean, itemBean, ItemDetailToolBar.BULK_UPDATE_OPERATION,
                bulkUpdateDetailPanel, true);
        itemPanel.getToolBar().setFkToolBar(false);
        itemPanel.getToolBar().setHierarchyCall(false);
        List<BreadCrumbModel> breads = new ArrayList<BreadCrumbModel>();
        if (itemBean != null) {
            breads.add(new BreadCrumbModel("", BreadCrumb.DEFAULTNAME, null, null, false)); //$NON-NLS-1$
            breads.add(new BreadCrumbModel(itemBean.getConcept(), itemBean.getLabel(), null, null, true));
        }
        List<String> pkInfoList = new ArrayList<String>();
        pkInfoList.add(itemBean.getLabel());
        bulkUpdateDetailPanel.initBanner(pkInfoList, itemBean.getDescription());
        bulkUpdateDetailPanel.addTabItem(itemBean.getLabel(), itemPanel, ItemsDetailPanel.SINGLETON, itemBean.getConcept());
        bulkUpdateDetailPanel.initBreadCrumb(new BreadCrumb(breads, bulkUpdateDetailPanel));
        add(bulkUpdateDetailPanel);
        layout();
    }

    public List<Map<String, Object>> getKeyMapList() {
        return keyMapList;
    }

    public void renderPanel() {
        if (GWT.isScript()) {
            renderGwtPanel(this.getItemId(), this);
        } else {
            renderDebugPanel(this);
        }
    }

    private native void renderGwtPanel(String id, BulkUpdatePanel bulkUpdatePanel)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		var panel = @org.talend.mdm.webapp.browserecords.client.widget.ItemsToolBar::convertBulkUpdatePanel(Lorg/talend/mdm/webapp/browserecords/client/widget/BulkUpdatePanel;)(bulkUpdatePanel);
		tabPanel.add(panel);
		tabPanel.setSelection(id);
    }-*/;

    private void renderDebugPanel(BulkUpdatePanel panel) {
        Window window = new Window();
        window.setLayout(new FitLayout());
        window.add(panel);
        window.setSize(1100, 700);
        window.setMaximizable(true);
        window.setModal(false);
        window.show();
    }

    private native static JavaScriptObject convertBulkUpdatePanel(BulkUpdatePanel bulkUpdatePanel)/*-{
		var panel = {
			// imitate extjs's render method, really call gxt code.
			render : function(el) {
				var rootPanel = @com.google.gwt.user.client.ui.RootPanel::get(Ljava/lang/String;)(el.id);
				rootPanel.@com.google.gwt.user.client.ui.RootPanel::add(Lcom/google/gwt/user/client/ui/Widget;)(bulkUpdatePanel);
			},
			// imitate extjs's setSize method, really call gxt code.
			setSize : function(width, height) {
				bulkUpdatePanel.@org.talend.mdm.webapp.browserecords.client.widget.BulkUpdatePanel::setSize(II)(width, height);
			},
			// imitate extjs's getItemId, really return itemId of ContentPanel of GXT.
			getItemId : function() {
				return bulkUpdatePanel.@org.talend.mdm.webapp.browserecords.client.widget.BulkUpdatePanel::getItemId()();
			},
			// imitate El object of extjs
			getEl : function() {
				var el = bulkUpdatePanel.@org.talend.mdm.webapp.browserecords.client.widget.BulkUpdatePanel::getElement()();
				return {
					dom : el
				};
			},
			// imitate extjs's doLayout method, really call gxt code.
			doLayout : function() {
				return bulkUpdatePanel.@org.talend.mdm.webapp.browserecords.client.widget.BulkUpdatePanel::doLayout()();
			},
			title : function() {
				return bulkUpdatePanel.@org.talend.mdm.webapp.browserecords.client.widget.BulkUpdatePanel::getHeading()();
			}
		};
		return panel;
    }-*/;

    public void closePanel() {
        closeBulkUpdatePanel(this.getItemId());
    }

    private native void closeBulkUpdatePanel(String id)/*-{
		var tabPanel = $wnd.amalto.core.getTabPanel();
		tabPanel.remove(id);
    }-*/;
}
