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
package org.talend.mdm.webapp.browserecords.client.widget;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.client.creator.ItemCreator;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.BreadCrumbModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;

public class BulkUpdatePanel extends ContentPanel {

    private static BulkUpdatePanel instance;

    private BulkUpdateListPanel bulkUpdateListPanel;

    private ContentPanel detailPanel;

    private List<String> idsList;

    public static BulkUpdatePanel getInstance() {
        if (instance == null) {
            instance = new BulkUpdatePanel();
        }
        return instance;
    }

    private BulkUpdatePanel() {
        setHeading(MessagesFactory.getMessages().bulkUpdate_title());
        setHeaderVisible(false);
        setLayout(new BorderLayout());
        setBorders(false);

        // BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 470);
        // westData.setSplit(true);
        // westData.setMargins(new Margins(0, 5, 0, 0));
        // westData.setFloatable(true);
        // westData.setMinSize(0);
        // westData.setMaxSize(7000);
        // bulkUpdateListPanel = BulkUpdateListPanel.getInstance();
        // add(BulkUpdateListPanel.getInstance(), westData);

        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        detailPanel = new ContentPanel();
        detailPanel.setFrame(false);
        detailPanel.setHeaderVisible(false);
        detailPanel.setLayout(new FitLayout());
        detailPanel.setBodyBorder(false);
        add(detailPanel, centerData);
    }

    public void initDetailPanel(EntityModel entityModel, ViewBean viewBean, List<String> idsList, boolean isStaging) {
        this.idsList = idsList;
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
        detailPanel.add(bulkUpdateDetailPanel);
        detailPanel.layout();
    }

    public BulkUpdateListPanel getBulkUpdateListPanel() {
        return bulkUpdateListPanel;
    }

    public List<String> getIdsList() {
        return idsList;
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
