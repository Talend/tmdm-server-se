/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.ForeignKeyUtil;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.MultiOccurrenceChangeItem;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.MultiOccurrenceChangeItem.AddRemoveHandler;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.DynamicTreeItem;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetail.GhostTreeItem;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeDetailGridFieldCreator;
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.TreeItemEx;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.ui.Widget;

public class MultiOccurrenceManager {

    TreeDetail treeDetail;

    Map<String, TypeModel> metaDataTypes;

    private List<List<DynamicTreeItem>> brothersGroups = new ArrayList<List<DynamicTreeItem>>();

    public MultiOccurrenceManager(Map<String, TypeModel> metaDataTypes, TreeDetail treeDetail) {
        this.metaDataTypes = metaDataTypes;
        this.treeDetail = treeDetail;
    }

    private List<DynamicTreeItem> getBrothersGroup(String xpathWithoutLastIndex) {

        for (List<DynamicTreeItem> group : brothersGroups) {
            if (group.size() > 0) {
                DynamicTreeItem item = group.get(0);
                String xpath = CommonUtil.getRealXpathWithoutLastIndex(item.getItemNodeModel());
                if (xpathWithoutLastIndex.equals(xpath)) {
                    return group;
                }
            }
        }
        return null;
    }

    private List<DynamicTreeItem> createBrothersGroup() {
        brothersGroups.add(new ArrayList<TreeDetail.DynamicTreeItem>());
        return brothersGroups.get(brothersGroups.size() - 1);
    }

    public void addMultiOccurrenceNode(TreeItemEx treeItem) {
        if (treeItem instanceof DynamicTreeItem) {
            DynamicTreeItem item = (DynamicTreeItem) treeItem;
            ItemNodeModel nodeModel = item.getItemNodeModel();
            if (nodeModel != null) {
                TypeModel typeModel = metaDataTypes.get(nodeModel.getTypePath());
                boolean isLazyLoading = !typeModel.isAutoExpand() && nodeModel.getParent() != null
                        && nodeModel.getChildCount() > 0 && item.getChild(0) instanceof GhostTreeItem;
                if (typeModel.isAutoExpand() || isLazyLoading || nodeModel.getChildCount() == 0 || item.getChildCount() == 0) {
                    if (typeModel.getMaxOccurs() < 0 || typeModel.getMaxOccurs() > 1) {
                        String xpath = CommonUtil.getRealXpathWithoutLastIndex(nodeModel);
                        List<DynamicTreeItem> brothersGroup = getBrothersGroup(xpath);
                        if (brothersGroup == null) {
                            brothersGroup = createBrothersGroup();
                        }
                        setAddRemoveEvent(item);
                        int index = getIndexOfMultiItem(item);
                        brothersGroup.add(index, item);
                    }
                }

                if (isLazyLoading) {
                    return;
                }
            }
        }
        for (int i = 0; i < treeItem.getChildCount(); i++) {
            TreeItemEx childItem = treeItem.getChild(i);
            addMultiOccurrenceNode(childItem);
        }
    }

    private int getIndexOfMultiItem(DynamicTreeItem item) {
        int index = -1;
        ItemNodeModel itemModel = item.getItemNodeModel();
        TreeItemEx parent = item.getParentItem();
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            if (parent.getChild(i) instanceof DynamicTreeItem) {
                DynamicTreeItem childItem = (DynamicTreeItem) parent.getChild(i);
                ItemNodeModel childModel = childItem.getItemNodeModel();
                if (childModel.getTypePath().equals(itemModel.getTypePath())) {
                    index++;
                    if (childItem.equals(item)) {
                        return index;
                    }
                }
            }
        }
        return index;
    }

    public void removeMultiOccurrenceNode(DynamicTreeItem item) {
        ItemNodeModel nodeModel = item.getItemNodeModel();
        String xpath = CommonUtil.getRealXpathWithoutLastIndex(nodeModel);
        List<DynamicTreeItem> brothersGroup = getBrothersGroup(xpath);
        if (brothersGroup != null) {
            MultiOccurrenceChangeItem multiOccurrenceChangeItem = (MultiOccurrenceChangeItem) item.getWidget();
            brothersGroup.remove(item);
            if (multiOccurrenceChangeItem.isEditNodeButtonVisible()) {
                if (brothersGroup.get(0) != null) {
                    MultiOccurrenceChangeItem firstMultiOccurrenceChangeItem = (MultiOccurrenceChangeItem) brothersGroup.get(0)
                            .getWidget();
                    firstMultiOccurrenceChangeItem.setEditNodeButtonVisible(true);
                }
            }

            if (brothersGroup.size() == 0) {
                brothersGroups.remove(brothersGroup);
            }
            clearAddRemoveEvent(item);
        }

        for (int i = 0; i < item.getChildCount(); i++) {
            if (item.getChild(i) instanceof DynamicTreeItem) {
                DynamicTreeItem childItem = (DynamicTreeItem) item.getChild(i);
                removeMultiOccurrenceNode(childItem);
            }
        }
    }

    private void setAddRemoveEvent(DynamicTreeItem item) {
        Widget w = item.getWidget();
        if (w instanceof MultiOccurrenceChangeItem) {
            MultiOccurrenceChangeItem multiItem = (MultiOccurrenceChangeItem) w;
            multiItem.setAddRemoveHandler(new AddRemoveHandler() {

                @Override
                public void removedNode(DynamicTreeItem selectedItem) {
                    handleRemoveNode(selectedItem);
                }

                @Override
                public void addedNode(DynamicTreeItem selectedItem, String optId) {
                    handleAddNode(selectedItem, optId);
                }

                @Override
                public void clearNodeValue(DynamicTreeItem selectedItem) {
                    handleClearNodeValue(selectedItem);
                }

                @Override
                public void removeAllNode(DynamicTreeItem item) {
                    handleRemoveAllNode(item);
                }
            });
        }
    }

    private void clearAddRemoveEvent(DynamicTreeItem item) {
        Widget w = item.getWidget();
        if (w instanceof MultiOccurrenceChangeItem) {
            MultiOccurrenceChangeItem multiItem = (MultiOccurrenceChangeItem) w;
            multiItem.setAddRemoveHandler(null);
        }
    }

    public void warningBrothers(ItemNodeModel nodeModel) {
        ItemNodeModel parentNode = (ItemNodeModel) nodeModel.getParent();
        for (int i = 0; i < parentNode.getChildCount(); i++) {
            ItemNodeModel childNode = (ItemNodeModel) parentNode.getChild(i);
            if (childNode.isLeaf()) {
                warningItems(childNode);
            }
        }
    }

    public void warningItems(ItemNodeModel nodeModel) {
        ItemNodeModel current = nodeModel;
        while (current != null) {
            warningNode(current);
            current = (ItemNodeModel) current.getParent();
        }
    }

    private void warningNode(ItemNodeModel node) {
        String xpath = CommonUtil.getRealXpathWithoutLastIndex(node);
        List<DynamicTreeItem> brothersGroup = getBrothersGroup(xpath);
        if (brothersGroup == null) {
            return;
        }
        String mandatory = checkMandatory(brothersGroup);
        for (int i = 0; i < brothersGroup.size(); i++) {
            DynamicTreeItem childItem = brothersGroup.get(i);
            MultiOccurrenceChangeItem multiItem = (MultiOccurrenceChangeItem) childItem.getWidget();
            ItemNodeModel nodeModel = childItem.getItemNodeModel();
            if (!node.isBlankValid()) {
                multiItem.clearWarning();
                childItem.getElement().setTitle(null);
                nodeModel.setValid(true);
            }
            if (mandatory != null) {
                multiItem.warnMandatory();
                childItem.setTitle(mandatory);
                nodeModel.setValid(false);
            }

        }
    }

    public void warningAllItems() {
        for (List<DynamicTreeItem> group : brothersGroups) {
            for (DynamicTreeItem item : group) {
                warningNode(item.getItemNodeModel());
            }
        }
    }

    public void handleOptIcons() {
        for (List<DynamicTreeItem> group : brothersGroups) {
            for (DynamicTreeItem item : group) {
                ItemNodeModel nodeModel = item.getItemNodeModel();
                String realXpath = CommonUtil.getRealXPath(nodeModel);
                handleOptIcon(realXpath);
            }
        }
    }

    private void handleOptIcon(String realPath) {
        String xpath = CommonUtil.getRealXpathWithoutLastIndex(realPath);
        List<DynamicTreeItem> brothersGroup = getBrothersGroup(xpath);
        if (brothersGroup == null) {
            return;
        }
        if (brothersGroup.size() > 0) {

            TypeModel tm = metaDataTypes.get(brothersGroup.get(0).getItemNodeModel().getTypePath());
            boolean isFk = tm.getForeignkey() != null && tm.getForeignkey().trim().length() > 0;
            if (brothersGroup.size() == 1) {
                MultiOccurrenceChangeItem itemWidget = (MultiOccurrenceChangeItem) brothersGroup.get(0).getWidget();
                itemWidget.switchRemoveOpt(false, isFk);
            } else {
                MultiOccurrenceChangeItem itemWidget = (MultiOccurrenceChangeItem) brothersGroup.get(0).getWidget();
                itemWidget.switchRemoveOpt(true, isFk);
            }

            boolean canAdd;
            if (tm.getMaxOccurs() <= 0) {
                canAdd = true;
            } else {
                canAdd = brothersGroup.size() < tm.getMaxOccurs();
            }
            for (int i = 0; i < brothersGroup.size(); i++) {
                DynamicTreeItem item = brothersGroup.get(i);
                MultiOccurrenceChangeItem itemWidget = (MultiOccurrenceChangeItem) item.getWidget();
                itemWidget.setAddIconVisible(canAdd);
            }
        }
    }

    private String checkMandatory(List<DynamicTreeItem> items) {
        int validValueCount = 0;

        if (items.size() >= 1) {
            DynamicTreeItem firstItem = items.get(0);
            ItemNodeModel firstModel = firstItem.getItemNodeModel();
            ItemNodeModel parentModel = (ItemNodeModel) firstModel.getParent();
            TypeModel parentType = metaDataTypes.get(parentModel.getTypePath());
            boolean parentMandatory = true;
            if (parentModel.getParent() != null) {
                parentMandatory = parentType.getMinOccurs() == 1 && parentType.getMaxOccurs() == 1;
            }

            int minOccurs = metaDataTypes.get(firstModel.getTypePath()).getMinOccurs();
            for (DynamicTreeItem item : items) {
                ItemNodeModel nodeModel = item.getItemNodeModel();
                if (CommonUtil.hasChildrenValue(nodeModel)) {
                    validValueCount++;
                }
            }
            if (parentMandatory) {
                if (validValueCount < minOccurs) {

                    return MessagesFactory.getMessages().multiOccurrence_minimize_title(minOccurs,
                            firstItem.getItemNodeModel().getName());
                } else {
                    return null;
                }
            } else {
                for (int i = 0; i < parentModel.getChildCount(); i++) {
                    ItemNodeModel childModel = (ItemNodeModel) parentModel.getChild(i);
                    if (CommonUtil.hasChildrenValue(childModel)) {
                        if (validValueCount < minOccurs) {
                            return MessagesFactory.getMessages().multiOccurrence_minimize_title(minOccurs,
                                    firstItem.getItemNodeModel().getName());
                        }
                    }
                }
            }
        }
        return null;
    }

    private void handleAddNode(final DynamicTreeItem selectedItem, String optId) {

        final ItemNodeModel selectedModel = selectedItem.getItemNodeModel();
        TypeModel typeModel = metaDataTypes.get(selectedModel.getTypePath());
        int count = CommonUtil.getCountOfBrotherOfTheSameName(selectedModel);

        if (typeModel.getMaxOccurs() < 0 || count < typeModel.getMaxOccurs()) {
            ItemNodeModel model = null;
            if ("Clone".equals(optId)) { //$NON-NLS-1$
                model = selectedModel.clone(true);
            } else {
                List<ItemNodeModel> modelList = CommonUtil.getDefaultTreeModel(typeModel, Locale.getLanguage(), false, false,
                        false);
                if (modelList.size() > 0) {
                    model = modelList.get(0);
                }
            }
            if (model == null) {
                throw new IllegalStateException("Model is null"); //$NON-NLS-1$
            }

            model.setDynamicLabel(LabelUtil.getNormalLabel(model.getLabel()));
            model.setMandatory(selectedModel.isMandatory());
            ItemNodeModel parentModel = (ItemNodeModel) selectedModel.getParent();
            int selectModelIndex = parentModel.indexOf(selectedModel);
            parentModel.insert(model, selectModelIndex + 1);
            parentModel.setChangeValue(true);
            // if it has default value
            if (typeModel.getDefaultValue() != null) {
                model.setObjectValue(typeModel.getDefaultValue());
            }
            model.setEdited(selectedModel.isEdited());
            DynamicTreeItem treeItem = treeDetail.buildGWTTree(model, null, true,
                    selectedModel.isMassUpdate() ? ItemDetailToolBar.BULK_UPDATE_OPERATION : null);
            ((MultiOccurrenceChangeItem) treeItem.getWidget()).setEditNodeButtonVisible(false);
            ViewUtil.copyStyleToTreeItem(selectedItem, treeItem);

            DynamicTreeItem parentItem = (DynamicTreeItem) selectedItem.getParentItem();
            parentItem.insertItem(treeItem, parentItem.getChildIndex(selectedItem) + 1);
            treeDetail.adjustFieldWidget(treeItem);

            MultiOccurrenceManager multiManager = treeDetail.getMultiManager();
            multiManager.addMultiOccurrenceNode(treeItem);

            warningItems(treeItem.getItemNodeModel());

            handleOptIcon(CommonUtil.getRealXPath(selectedModel));

        } else {
            MessageBox.alert(MessagesFactory.getMessages().status(), MessagesFactory.getMessages()
                    .multiOccurrence_maximize(count), null);
        }
    }

    private void handleRemoveNode(final DynamicTreeItem selectedItem) {

        final DynamicTreeItem parentItem = (DynamicTreeItem) selectedItem.getParentItem();
        final ItemNodeModel selectedModel = selectedItem.getItemNodeModel();
        final ItemNodeModel parentModel = (ItemNodeModel) selectedModel.getParent();
        final TypeModel typeModel = metaDataTypes.get(selectedModel.getTypePath());
        final int count = CommonUtil.getCountOfBrotherOfTheSameName(selectedModel);

        final Map<String, Field<?>> fieldMap = treeDetail.getFieldMap();
        MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory.getMessages()
                .delete_occurrence_confirm(), new Listener<MessageBoxEvent>() {

            @Override
            public void handleEvent(MessageBoxEvent be) {
                if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                    if (count > 1 && count > typeModel.getMinOccurs()) {
                        TreeDetailGridFieldCreator.deleteField(selectedModel, fieldMap);

                        removeMultiOccurrenceNode(selectedItem);
                        selectedModel.setObjectValue(null);
                        warningItems(selectedModel);
                        String selectedXpath = CommonUtil.getRealXPath(selectedModel);
                        parentItem.removeItem(selectedItem);
                        parentModel.remove(selectedModel);
                        parentModel.setChangeValue(true);

                        Set<ItemNodeModel> fkContainers = ForeignKeyUtil.getAllForeignKeyModelParent(treeDetail.getViewBean(),
                                selectedModel);
                        for (ItemNodeModel fkContainer : fkContainers) {
                            treeDetail.getFkRender().removeRelationFkPanel(fkContainer);
                        }

                        handleOptIcon(selectedXpath);

                        if (parentModel.getChildCount() > 0) {
                            ItemNodeModel child = (ItemNodeModel) parentModel.getChild(0);
                            Field<?> field = fieldMap.get(child.getId().toString());
                            if (field != null) {
                                TreeDetailGridFieldCreator.updateMandatory(field, child, fieldMap);
                            }
                        }
                    } else {
                        MessageBox.alert(MessagesFactory.getMessages().status(), MessagesFactory.getMessages()
                                .multiOccurrence_minimize(count), null);
                    }
                }
            }
        });
    }

    private void handleClearNodeValue(final DynamicTreeItem selectedItem) {
        ItemNodeModel nodeModel = selectedItem.getItemNodeModel();
        // Clear all the leaf node's value when the current node isAutoExpand = false and children have not been
        // rendered
        TypeModel typeModel = metaDataTypes.get(nodeModel.getTypePath());
        if (!typeModel.isAutoExpand() && nodeModel.getChildCount() > 0 && selectedItem.getChild(0) instanceof GhostTreeItem) {
            nodeModel.clearNodeValue();
        } else {
            if (nodeModel.isLeaf()) {
                MultiOccurrenceChangeItem itemWidget = (MultiOccurrenceChangeItem) selectedItem.getWidget();
                itemWidget.clearValue();
                nodeModel.clearNodeValue();
            }
            for (int i = 0; i < selectedItem.getChildCount(); i++) {
                handleClearNodeValue((DynamicTreeItem) selectedItem.getChild(i));
            }
        }
    }

    public void handleRemoveAllNode(DynamicTreeItem item) {
        TreeItemEx parentItem = item.getParentItem();
        ItemNodeModel nodeModel = item.getItemNodeModel();
        String xpath = CommonUtil.getRealXpathWithoutLastIndex(nodeModel);
        List<DynamicTreeItem> brothersGroup = getBrothersGroup(xpath);
        List<DynamicTreeItem> items = new ArrayList<DynamicTreeItem>();
        if (brothersGroup != null) {
            for (int i = 1; i < brothersGroup.size(); i++) {
                items.add(brothersGroup.get(i));
            }
            for (int i = 0; i < items.size(); i++) {
                removeNodeFromParent((DynamicTreeItem) parentItem, items.get(i));
            }
            handleClearNodeValue(item);
        }
    }

    public void removeNodeFromParent(DynamicTreeItem parentItem, DynamicTreeItem selectedItem) {
        TreeDetailGridFieldCreator.deleteField(selectedItem.getItemNodeModel(), treeDetail.getFieldMap());
        removeMultiOccurrenceNode(selectedItem);
        selectedItem.getItemNodeModel().setObjectValue(null);
        warningItems(selectedItem.getItemNodeModel());
        String selectedXpath = CommonUtil.getRealXPath(selectedItem.getItemNodeModel());
        parentItem.removeItem(selectedItem);
        parentItem.getItemNodeModel().remove(selectedItem.getItemNodeModel());
        parentItem.getItemNodeModel().setChangeValue(true);
        Set<ItemNodeModel> fkContainers = ForeignKeyUtil.getAllForeignKeyModelParent(treeDetail.getViewBean(),
                selectedItem.getItemNodeModel());
        for (ItemNodeModel fkContainer : fkContainers) {
            treeDetail.getFkRender().removeRelationFkPanel(fkContainer);
        }
        handleOptIcon(selectedXpath);
        if (parentItem.getItemNodeModel().getChildCount() > 0) {
            ItemNodeModel child = (ItemNodeModel) parentItem.getItemNodeModel().getChild(0);
            Field<?> field = treeDetail.getFieldMap().get(child.getId().toString());
            if (field != null) {
                TreeDetailGridFieldCreator.updateMandatory(field, child, treeDetail.getFieldMap());
            }
        }
    }
}
