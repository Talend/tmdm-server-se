/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.ForeignKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.shared.OperatorValueConstants;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.ServiceFactory;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandler;
import org.talend.mdm.webapp.browserecords.client.handler.ItemTreeHandlingStatus;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.mvc.BrowseRecordsView;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKeyFieldList;
import org.talend.mdm.webapp.browserecords.client.widget.ItemPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;

public class ForeignKeySelector extends ForeignKeyField implements ReturnCriteriaFK {

    private String foreignKeyFilter;

    private ItemNodeModel itemNode;

    private Image addButton;

    private Image cleanButton;

    private Image relationButton;

    private ForeignKeyFieldList fkFieldList;

    private boolean isFkFieldList;

    private ItemsDetailPanel itemsDetailPanel;

    private boolean showAddButton;

    private boolean showCleanButton;

    private boolean showRelationButton;

    private boolean validateFlag;

    public ForeignKeySelector(TypeModel dataType, ItemsDetailPanel itemsDetailPanel, ItemNodeModel itemNode) {
        super(dataType);
        this.foreignKeyFilter = dataType.getForeignKeyFilter();
        this.itemsDetailPanel = itemsDetailPanel;
        this.itemNode = itemNode;
        this.isStaging = itemsDetailPanel.isStaging();
        this.addButton = new Image(Icons.INSTANCE.link_add());
        this.cleanButton = new Image(Icons.INSTANCE.link_delete());
        this.relationButton = new Image(Icons.INSTANCE.link_go());
        this.showAddButton = !dataType.isReadOnly();
        this.showCleanButton = !dataType.isReadOnly();
        this.showRelationButton = true;
    }

    public ForeignKeySelector(TypeModel dataType, ForeignKeyFieldList fkFieldList, ItemsDetailPanel itemsDetailPanel,
            ItemNodeModel itemNode) {
        this(dataType, itemsDetailPanel, itemNode);
        this.fkFieldList = fkFieldList;
        this.isFkFieldList = true;
    }

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
    }

    @Override
    protected void addButtonListener() {
        super.addButtonListener();
        addButton.setTitle(MessagesFactory.getMessages().fk_add_title());
        addButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                Dispatcher dispatch = Dispatcher.get();
                AppEvent event = new AppEvent(BrowseRecordsEvents.CreateForeignKeyView, foreignConceptName);
                event.setData(BrowseRecordsView.FK_SOURCE_WIDGET, ForeignKeySelector.this);
                event.setData(BrowseRecordsView.ITEMS_DETAIL_PANEL, itemsDetailPanel);
                event.setData(BrowseRecordsView.IS_STAGING, itemsDetailPanel.isStaging());
                dispatch.dispatch(event);
            }
        });
        cleanButton.setTitle(MessagesFactory.getMessages().fk_del_title());
        cleanButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                if (!isFkFieldList) {
                    clear();
                } else {
                    fkFieldList.removeForeignKeyWidget(ForeignKeySelector.this.getValue());
                }
            }
        });
        relationButton.setTitle(MessagesFactory.getMessages().fk_open_title());
        relationButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent arg0) {
                ForeignKeyBean fkBean = ForeignKeySelector.this.getValue();
                if (fkBean == null || fkBean.getId() == null || "".equals(fkBean.getId())) { //$NON-NLS-1$
                    return;
                }
                Dispatcher dispatch = Dispatcher.get();
                AppEvent event = new AppEvent(BrowseRecordsEvents.ViewForeignKey);
                event.setData("ids", ForeignKeySelector.this.getValue().getId().replaceAll("^\\[|\\]$", "").replace("][", ".")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                event.setData("concept", foreignConceptName); //$NON-NLS-1$
                event.setData("isStaging", itemsDetailPanel.isStaging()); //$NON-NLS-1$
                event.setData(BrowseRecordsView.ITEMS_DETAIL_PANEL, itemsDetailPanel);
                dispatch.dispatch(event);
            }
        });

    }

    @Override
    protected El renderField() {
        El wrap = new El(DOM.createTable());
        wrap.setElementAttribute("cellSpacing", "0"); //$NON-NLS-1$ //$NON-NLS-2$
        wrap.addStyleName("x-form-field-wrap"); //$NON-NLS-1$
        wrap.addStyleName("x-form-file-wrap"); //$NON-NLS-1$
        Element tbody = DOM.createTBody();
        Element foreignKeyTR = DOM.createTR();
        tbody.appendChild(foreignKeyTR);

        if (showInput) {
            Element inputTD = DOM.createTD();
            foreignKeyTR.appendChild(inputTD);
            suggestBox.render(inputTD);
        }

        Element iconTD = DOM.createTD();
        foreignKeyTR.appendChild(iconTD);

        Element iconBody = DOM.createTBody();
        Element iconTR = DOM.createTR();

        if (showSelectButton) {
            Element selectTD = DOM.createTD();
            iconTR.appendChild(selectTD);
            selectTD.appendChild(selectButton.getElement());
        }

        if (showAddButton) {
            Element addTD = DOM.createTD();
            iconTR.appendChild(addTD);
            addTD.appendChild(addButton.getElement());
        }

        if (showCleanButton) {
            Element cleanTD = DOM.createTD();
            iconTR.appendChild(cleanTD);
            cleanTD.appendChild(cleanButton.getElement());
        }

        if (showRelationButton) {
            Element relationTD = DOM.createTD();
            iconTR.appendChild(relationTD);
            relationTD.appendChild(relationButton.getElement());
        }

        iconBody.appendChild(iconTR);
        iconTD.setAttribute("align", "right"); //$NON-NLS-1$//$NON-NLS-2$
        iconTD.appendChild(iconBody);
        wrap.appendChild(tbody);
        return wrap;
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        addButton.setVisible(!readOnly);
        cleanButton.setVisible(!readOnly);
        relationButton.setVisible(true);
    }

    @Override
    public String parseForeignKeyFilter() {
        if (foreignKeyFilter != null) {
            String[] criterias = org.talend.mdm.webapp.base.shared.util.CommonUtil
                    .getCriteriasByForeignKeyFilter(foreignKeyFilter);
            List<Map<String, String>> conditions = new ArrayList<Map<String, String>>();
            for (String cria : criterias) {
                Map<String, String> conditionMap = org.talend.mdm.webapp.base.shared.util.CommonUtil
                        .buildConditionByCriteria(cria);
                if (OperatorValueConstants.EMPTY_NULL.equals(conditionMap.get("Operator"))) { //$NON-NLS-1$
                    return foreignKeyFilter;
                }
                String filterValue = conditionMap.get("Value"); //$NON-NLS-1$
                if (filterValue == null || this.foreignKeyPath == null) {
                    return ""; //$NON-NLS-1$
                }

                // cases handle
                filterValue = org.talend.mdm.webapp.base.shared.util.CommonUtil.unescapeXml(filterValue);
                if (org.talend.mdm.webapp.base.shared.util.CommonUtil.isFilterValue(filterValue)) {
                    filterValue = filterValue.substring(1, filterValue.length() - 1);
                } else if (org.talend.mdm.webapp.base.shared.util.CommonUtil.isRelativePath(filterValue)) {
                    if (conditionMap.get("Xpath") != null && conditionMap.get("Xpath").split("/").length > 0 //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                            && currentPath.split("/")[0].equals(conditionMap.get("Xpath").split("/")[0])) { //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
                        String[] rightPathArray = filterValue.split("/"); //$NON-NLS-1$
                        String relativeMark = rightPathArray[0];
                        String targetPath = itemNode.getTypePath();
                        ItemNodeModel parentNode = itemNode;
                        if (".".equals(relativeMark)) { //$NON-NLS-1$
                            targetPath = targetPath + filterValue.substring(filterValue.indexOf("/")); //$NON-NLS-1$
                        } else if ("..".equals(relativeMark)) { //$NON-NLS-1$
                            parentNode = (ItemNodeModel) parentNode.getParent();
                            targetPath = targetPath.substring(0, targetPath.lastIndexOf("/")); //$NON-NLS-1$
                            targetPath = targetPath + filterValue.substring(filterValue.indexOf("/")); //$NON-NLS-1$
                        }
                        ItemNodeModel targetNode = findTarget(targetPath, parentNode);
                        if (targetNode != null && targetNode.getObjectValue() != null) {
                            Object targetValue = targetNode.getObjectValue();
                            if (targetValue instanceof ForeignKeyBean) {
                                ForeignKeyBean targetForeignKeyBean = (ForeignKeyBean) targetValue;
                                filterValue = org.talend.mdm.webapp.base.shared.util.CommonUtil
                                        .unwrapFkValue(targetForeignKeyBean.getId());
                            } else {
                                filterValue = targetNode.getObjectValue().toString();
                            }
                        } else {
                            filterValue = ""; //$NON-NLS-1$
                        }
                    }
                } else {
                    String[] rightValueOrPathArray = filterValue.split("/"); //$NON-NLS-1$
                    if (rightValueOrPathArray.length > 0) {
                        String rightConcept = rightValueOrPathArray[0];
                        if (rightConcept.equals(currentPath.split("/")[0])) { //$NON-NLS-1$
                            List<String> duplicatedPathList = new ArrayList<String>();
                            List<String> leftPathNodeList = new ArrayList<String>();
                            List<String> rightPathNodeList = Arrays.asList(filterValue.split("/")); //$NON-NLS-1$
                            String[] leftValueOrPathArray = currentPath.split("/"); //$NON-NLS-1$
                            for (String element : leftValueOrPathArray) {
                                leftPathNodeList.add(element);
                            }
                            for (int i = 0; i < leftPathNodeList.size(); i++) {
                                if (i < rightPathNodeList.size() && leftPathNodeList.get(i).equals(rightPathNodeList.get(i))) {
                                    duplicatedPathList.add(rightPathNodeList.get(i));
                                } else {
                                    break;
                                }
                            }
                            leftPathNodeList.removeAll(duplicatedPathList);
                            ItemNodeModel parentNode = itemNode;
                            for (int i = 0; i < leftPathNodeList.size(); i++) {
                                parentNode = (ItemNodeModel) parentNode.getParent();
                            }
                            ItemNodeModel targetNode = findTarget(filterValue, parentNode);
                            if (targetNode != null && targetNode.getObjectValue() != null) {
                                Object targetValue = targetNode.getObjectValue();
                                if (targetValue instanceof ForeignKeyBean) {
                                    ForeignKeyBean targetForeignKeyBean = (ForeignKeyBean) targetValue;
                                    filterValue = org.talend.mdm.webapp.base.shared.util.CommonUtil
                                            .unwrapFkValue(targetForeignKeyBean.getId());
                                } else {
                                    filterValue = targetNode.getObjectValue().toString();
                                }
                            } else {
                                filterValue = ""; //$NON-NLS-1$
                            }
                        }
                    }
                }
                conditionMap.put("Value", filterValue); //$NON-NLS-1$
                conditions.add(conditionMap);
            }
            return org.talend.mdm.webapp.base.shared.util.CommonUtil.buildForeignKeyFilterByConditions(conditions);
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    protected ItemNodeModel findTarget(String targetPath, ItemNodeModel node) {
        List<ModelData> childrenList = node.getChildren();
        if (childrenList != null && childrenList.size() > 0) {
            for (int i = 0; i < childrenList.size(); i++) {
                ItemNodeModel child = (ItemNodeModel) childrenList.get(i);
                String typePath = child.getTypePath();
                if (typePath.contains(":")) { //$NON-NLS-1$
                    String[] pathArray = typePath.split("/"); //$NON-NLS-1$
                    for (int j = 0; j < pathArray.length; j++) {
                        String nodePath = pathArray[j];
                        if (nodePath.contains(":")) { //$NON-NLS-1$
                            String[] nodePathArray = nodePath.split(":");
                            if (targetPath.contains("xsi:type")) {
                                pathArray[j] = nodePathArray[0] + "[@xsi:type=\"" + nodePathArray[1] + "\"]"; //$NON-NLS-1$  //$NON-NLS-2$ 
                            } else {
                                pathArray[j] = nodePathArray[0];
                            }
                        }
                    }
                    typePath = transformPath(pathArray);
                }
                if (targetPath.contains(typePath)) {
                    if (targetPath.equals(typePath)) {
                        return child;
                    } else {
                        return findTarget(targetPath, child);
                    }
                }
            }
        }
        return null;
    }

    public void setShowAddButton(boolean showAddButton) {
        this.showAddButton = showAddButton;
    }

    public void setShowCleanButton(boolean showCleanButton) {
        this.showCleanButton = showCleanButton;
    }

    public void setShowRelationButton(boolean showRelationButton) {
        this.showRelationButton = showRelationButton;
    }

    public void setItemNode(ItemNodeModel itemNode) {
        this.itemNode = itemNode;
    }

    @Override
    protected void doAttachChildren() {
        super.doAttachChildren();
        ComponentHelper.doAttach(addButton);
        ComponentHelper.doAttach(cleanButton);
        ComponentHelper.doAttach(relationButton);
    }

    @Override
    protected void doDetachChildren() {
        super.doDetachChildren();
        ComponentHelper.doDetach(addButton);
        ComponentHelper.doDetach(cleanButton);
        ComponentHelper.doDetach(relationButton);
    }

    public void setCriteriaFK(final String foreignKeyIds) {
        ItemPanel itemPanel = (ItemPanel) itemsDetailPanel.getFirstTabWidget();
        ItemNodeModel root = itemPanel.getTree().getRootModel();
        String xml = (new ItemTreeHandler(root, itemPanel.getViewBean(), ItemTreeHandlingStatus.BeforeLoad)).serializeItem();
        ServiceFactory
                .getInstance()
                .getService(isStaging)
                .getForeignKeyBean(foreignKeyPath.split("/")[0], foreignKeyIds, xml, currentPath, foreignKeyPath, foreignKeyInfo, //$NON-NLS-1$
                        parseForeignKeyFilter(), isStaging, Locale.getLanguage(),
                        new SessionAwareAsyncCallback<ForeignKeyBean>() {

                            @Override
                            public void onSuccess(ForeignKeyBean foreignKeyBean) {
                                if (foreignKeyBean != null) {
                                    setValue(foreignKeyBean);
                                } else {
                                    MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                                            .foreignkey_filter_warning(), null);
                                }
                            }
                        });
    }

    @Override
    public void setCriteriaFK(final ForeignKeyBean fk) {
        setValue(fk);
    }

    @Override
    public boolean validateValue(String value) {
        if (!validateFlag) {
            return true;
        }
        return super.validateValue(value);
    }

    public void setValidateFlag(boolean validateFlag) {
        this.validateFlag = validateFlag;
    }

    @Override
    public void clear() {
        super.clear();
        this.validate();

        ForeignKeyBean bean = new ForeignKeyBean();
        bean.setId(""); //$NON-NLS-1$
        setValue(bean);

        if (suggestBox != null) {
            suggestBox.clear();
        }
    }

    protected String transformPath(String[] pathArray) {
        StringBuilder pathBuilder = new StringBuilder();
        for (int i = 0; i < pathArray.length; i++) {
            pathBuilder.append(pathArray[i]);
            if (i < pathArray.length - 1) {
                pathBuilder.append("/"); //$NON-NLS-1$
            }
        }
        return pathBuilder.toString();
    }
}
