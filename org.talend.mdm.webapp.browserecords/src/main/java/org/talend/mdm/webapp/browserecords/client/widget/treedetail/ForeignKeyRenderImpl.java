/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.treedetail;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.BrowseRecordsMessages;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ForeignKeyTabModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.mvc.BrowseRecordsView;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.LabelUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.util.UserSession;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel.ForeignKeyHandler;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel.ItemDetailTabPanelContentHandle;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.DynamicTreeItem;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;
import org.talend.mdm.webapp.browserecords.shared.VisibleRuleResult;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;

public class ForeignKeyRenderImpl implements ForeignKeyRender {

    HashMap<ItemNodeModel, ItemDetailTabPanelContentHandle> relationFk = new HashMap<ItemNodeModel, ItemDetailTabPanelContentHandle>();

    HashMap<ItemNodeModel, ItemPanel> relationFKItems = new HashMap<ItemNodeModel, ItemPanel>();

    HashMap<ItemPanel, Object[]> tabPanelParameters = new HashMap<ItemPanel, Object[]>();

    BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    public ForeignKeyRenderImpl() {

    }

    @Override
    public void RenderForeignKey(final ItemNodeModel parentModel, final List<ItemNodeModel> fkNodeModelList,
            final TypeModel fkTypeModel, final ItemDetailToolBar toolBar, final ViewBean pkViewBean, final ContentPanel cp,
            final ItemsDetailPanel detailPanel) {
        if (fkNodeModelList != null) {
            final String concept = fkTypeModel.getForeignkey().split("/")[0]; //$NON-NLS-1$

            DynamicTreeItem root;
            if (cp instanceof TreeDetail) {
                TreeDetail treeDetail = (TreeDetail) cp;
                root = (DynamicTreeItem) treeDetail.getRoot();
            } else {
                ForeignKeyTreeDetail fkTreeDetail = (ForeignKeyTreeDetail) cp;
                root = (DynamicTreeItem) fkTreeDetail.getRoot();
            }
            final ForeignKeyTablePanel fkPanel = new ForeignKeyTablePanel(
                    concept + "_ForeignKeyTablePanel", toolBar.isStaging(), parentModel.isMassUpdate()); //$NON-NLS-1$

            final ItemPanel itemPanel = new ItemPanel(toolBar.isStaging(), pkViewBean, toolBar.getItemBean(),
                    toolBar.getOperation(), fkPanel, root, detailPanel, toolBar.isOpenTab());
            itemPanel.getToolBar().setOutMost(toolBar.isOutMost());
            itemPanel.getToolBar().setHierarchyCall(toolBar.isHierarchyCall());
            itemPanel.getToolBar().setFkToolBar(true);
            itemPanel.getToolBar().setViewCode(toolBar.getViewCode());
            itemPanel.getToolBar().setType(toolBar.getType());
            itemPanel.getToolBar().setReturnCriteriaFK(toolBar.getReturnCriteriaFK()); // TMDM-3380. FK Tab Title should
                                                                                       // not support the dynamic label.
                                                                                       // If FK exist dynamic label, it
                                                                                       // should remove

            if (cp != detailPanel.getTreeDetail()) {
                return;
            }
            if (fkNodeModelList.get(0) != null) {
                relationFKItems.put(fkNodeModelList.get(0), itemPanel);
                tabPanelParameters.put(itemPanel, new Object[] { parentModel, fkNodeModelList, fkTypeModel, toolBar, pkViewBean,
                        detailPanel, fkPanel, itemPanel, cp });

                if (fkNodeModelList.get(0).isVisible()) {
                    addTab(parentModel, fkNodeModelList, fkTypeModel, toolBar, pkViewBean, detailPanel, fkPanel, itemPanel, cp);
                }
            }

        }
    }

    private void addTab(final ItemNodeModel parentModel, final List<ItemNodeModel> fkNodeModelList, final TypeModel fkTypeModel,
            final ItemDetailToolBar toolBar, final ViewBean pkViewBean, final ItemsDetailPanel detailPanel,
            final ForeignKeyTablePanel fkPanel, final ItemPanel itemPanel, final ContentPanel cp) {

        final String concept = fkTypeModel.getForeignkey().split("/")[0]; //$NON-NLS-1$

        final Map<String, Field<?>> fieldMap;
        if (cp instanceof TreeDetail) {
            TreeDetail treeDetail = (TreeDetail) cp;
            fieldMap = treeDetail.getFieldMap();
        } else {
            ForeignKeyTreeDetail fkTreeDetail = (ForeignKeyTreeDetail) cp;
            fieldMap = fkTreeDetail.getFieldMap();
        }

        // '{***}' section to display title. for instance: 'Agency:{position()}' is an English dynamic label, Agency
        // FK Tab Title should be 'Agency', but not 'Agency:{position()}'. Only when Agency as an item to be
        // displayed in tree detail UI, its dynamic label can work.
        String xpathLabel = ForeignKeyUtil.transferXpathToLabel(parentModel)
                + LabelUtil.getFKTabLabel(fkTypeModel.getLabel(UrlUtil.getLanguage()));
        xpathLabel = xpathLabel.substring(xpathLabel.indexOf('/') + 1);

        ItemDetailTabPanelContentHandle handle = detailPanel.addTabItem(xpathLabel, itemPanel, ItemsDetailPanel.MULTIPLE,
                GWT.getModuleName() + DOM.createUniqueId());
        relationFk.put(parentModel, handle);
        // lazy render FK
        ForeignKeyHandler handler = new ForeignKeyHandler() {

            @Override
            public void onSelect() {
                BrowseRecordsMessages msg = MessagesFactory.getMessages();
                final MessageBox renderFkProgress = MessageBox.wait(msg.rendering_title(), msg.render_message(),
                        msg.rendering_progress());
                service.getEntityModel(concept, Locale.getLanguage(), new SessionAwareAsyncCallback<EntityModel>() {

                    @Override
                    public void onSuccess(EntityModel entityModel) {
                        fkPanel.initContent(entityModel, parentModel, fkNodeModelList, fkTypeModel, fieldMap, detailPanel,
                                pkViewBean);
                        itemPanel.layout(true);
                        renderFkProgress.close();
                    }

                    @Override
                    protected void doOnFailure(Throwable caught) {
                        renderFkProgress.close();
                        super.doOnFailure(caught);
                    }
                });
            }
        };

        detailPanel.addFkHandler(itemPanel, handler);
        HashMap<String, ItemPanel> cachedEntityMap = BrowseRecords.getSession().getCurrentCachedEntity();
        String ids = toolBar.getItemBean().getIds() != null ? toolBar.getItemBean().getIds() : ""; //$NON-NLS-1$
        String key = toolBar.getItemBean().getConcept() + ids + detailPanel.isOutMost();
        String panelKey = ((toolBar.getViewCode() == BrowseRecordsView.LINEAGE_VIEW_CODE) ? BrowseRecordsView.LINEAGE_ITEMVIEW
                : "") + key; //$NON-NLS-1$

        if (cachedEntityMap != null && cachedEntityMap.containsKey(panelKey)) {
            ForeignKeyTabModel fkTabModel = new ForeignKeyTabModel(parentModel, xpathLabel, itemPanel, handler);
            HashMap<String, LinkedHashMap<String, ForeignKeyTabModel>> cachedFkPanels = BrowseRecords.getSession()
                    .getCurrentCachedFKTabs();
            if (cachedFkPanels == null) {
                cachedFkPanels = new HashMap<String, LinkedHashMap<String, ForeignKeyTabModel>>();
            }
            if (!cachedFkPanels.containsKey(key)) {
                cachedFkPanels.put(key, new LinkedHashMap<String, ForeignKeyTabModel>());
            }
            cachedFkPanels.get(key).put(xpathLabel, fkTabModel);
            BrowseRecords.getSession().put(UserSession.CURRENT_CACHED_FKTABS, cachedFkPanels);
        }
    }

    @Override
    public void removeRelationFkPanel(ItemNodeModel parentModel) {
        ItemDetailTabPanelContentHandle tabItem = relationFk.get(parentModel);
        if (tabItem != null) {
            tabItem.deleteContent();
            relationFk.remove(parentModel);
        }
    }

    @Override
    public void setRelationFk(ItemNodeModel parentModel, ItemDetailTabPanelContentHandle handle) {
        relationFk.put(parentModel, handle);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void recrusiveSetItems(VisibleRuleResult visibleResult) {
        for (Map.Entry<ItemNodeModel, ItemPanel> fkItemEntry : relationFKItems.entrySet()) {
            if (fkItemEntry.getKey() != null) {
                String realPath = CommonUtil.getRealXPath(fkItemEntry.getKey());

                if (relationFKItems.containsKey(fkItemEntry.getKey())) {
                    if (visibleResult.getXpath().equals(realPath)) {
                        fkItemEntry.getKey().setVisible(visibleResult.isVisible() && !fkItemEntry.getKey().isHide());
                        if (fkItemEntry.getKey().isVisible()) {
                            Object[] parameters = tabPanelParameters.get(fkItemEntry.getValue());
                            addTab((ItemNodeModel) parameters[0], (List<ItemNodeModel>) parameters[1], (TypeModel) parameters[2],
                                    (ItemDetailToolBar) parameters[3], (ViewBean) parameters[4],
                                    (ItemsDetailPanel) parameters[5], (ForeignKeyTablePanel) parameters[6],
                                    (ItemPanel) parameters[7], (ContentPanel) parameters[8]);
                        }
                    }
                }
                if (!visibleResult.getXpath().startsWith(realPath)) {
                    return;
                }
            }
        }
    }
}
