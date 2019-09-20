/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.ForeignKey;

import java.util.ArrayList;
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
import org.talend.mdm.webapp.browserecords.client.widget.treedetail.ForeignKeyUtil;

import com.extjs.gxt.ui.client.core.El;
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
        this.originForeignKeyFilter = dataType.getForeignKeyFilter();
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
                    conditions.add(conditionMap);
                    continue;
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
                    filterValue = ForeignKeyUtil.findRelativePath(filterValue, conditionMap.get("Xpath"), currentPath, itemNode);
                } else {
                    filterValue = ForeignKeyUtil.getXpathValue(filterValue, currentPath, itemNode);
                }
                conditionMap.put("Value", filterValue); //$NON-NLS-1$
                conditions.add(conditionMap);
            }
            return org.talend.mdm.webapp.base.shared.util.CommonUtil.buildForeignKeyFilterByConditions(conditions);
        } else {
            return ""; //$NON-NLS-1$
        }
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

    public ItemNodeModel getItemNode() {
        return itemNode;
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

}
