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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.util.PostDataUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.model.ItemBean;
import org.talend.mdm.webapp.browserecords.client.model.QueryModel;
import org.talend.mdm.webapp.browserecords.client.model.RecordsPagingConfig;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.shared.Constants;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.GWT;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class DownloadFilePanel extends FormPanel {

    private TextField<String> fileName;

    private TextField<String> multipleValueSeparatorField;

    private CheckBox fkResovled;

    private ComboBox<ItemBaseModel> fkDisplayCombo;

    private QueryModel queryModel;

    private Button exportBtn;

    private Window window;

    private ViewBean viewBean = null;

    private Map<String, List<String>> inheritanceNodeMap = null;

    private int defaultMaxExportRecordsCount;

    public DownloadFilePanel(ViewBean viewBean, QueryModel queryModel, Window window) {
        defaultMaxExportRecordsCount =  BrowseRecords.getSession().getAppHeader().getExportRecordsDefaultCount();

        this.viewBean = viewBean;
        this.queryModel = queryModel;
        this.window = window;

        this.setFrame(false);
        this.setHeaderVisible(false);
        this.setWidth("100%"); //$NON-NLS-1$

        fileName = new TextField<String>();
        fileName.setFieldLabel(MessagesFactory.getMessages().picture_field_label());
        fileName.setAllowBlank(false);
        fileName.setValue(viewBean.getBindingEntityModel().getConceptName());
        this.add(fileName, new FormData("90%")); //$NON-NLS-1$

        multipleValueSeparatorField = new TextField<String>();
        multipleValueSeparatorField.setId("multipleValueSeparator"); //$NON-NLS-1$
        multipleValueSeparatorField.setName("multipleValueSeparator"); //$NON-NLS-1$
        multipleValueSeparatorField.setFieldLabel(MessagesFactory.getMessages().multiple_value_separator_field_label());
        multipleValueSeparatorField.setMaxLength(1);
        multipleValueSeparatorField.setValue("|"); //$NON-NLS-1$
        this.add(multipleValueSeparatorField, new FormData("67.3%")); //$NON-NLS-1$

        fkResovled = new CheckBox();
        fkResovled.setFieldLabel(MessagesFactory.getMessages().fkinfo_display_label());
        fkResovled.setLabelStyle("width:90px"); //$NON-NLS-1$
        fkResovled.addListener(Events.Change, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                if (fkResovled.getValue().booleanValue()) {
                    fkDisplayCombo.setEnabled(true);
                } else {
                    fkDisplayCombo.setEnabled(false);
                }
            }
        });

        this.add(fkResovled, new FormData("90%")); //$NON-NLS-1$

        List<ItemBaseModel> fkDisplayList = new ArrayList<ItemBaseModel>();
        ItemBaseModel idPlusInfo = new ItemBaseModel();
        idPlusInfo.set("label", "Id-FKInfo"); //$NON-NLS-1$ //$NON-NLS-2$
        idPlusInfo.set("key", "Id-FKInfo"); //$NON-NLS-1$ //$NON-NLS-2$
        fkDisplayList.add(idPlusInfo);

        ItemBaseModel infoOnly = new ItemBaseModel();
        infoOnly.set("label", "FKInfo"); //$NON-NLS-1$ //$NON-NLS-2$
        infoOnly.set("key", "FKInfo"); //$NON-NLS-1$ //$NON-NLS-2$
        fkDisplayList.add(infoOnly);

        ListStore<ItemBaseModel> fkDisplayStoreList = new ListStore<ItemBaseModel>();
        fkDisplayStoreList.add(fkDisplayList);

        fkDisplayCombo = new ComboBox<ItemBaseModel>();
        fkDisplayCombo.setId("fkDisplay");//$NON-NLS-1$
        fkDisplayCombo.setName("fkDisplay");//$NON-NLS-1$
        fkDisplayCombo.setFieldLabel(MessagesFactory.getMessages().fkinfo_display_type_label());
        fkDisplayCombo.setDisplayField("label"); //$NON-NLS-1$
        fkDisplayCombo.setValueField("key"); //$NON-NLS-1$
        fkDisplayCombo.setStore(fkDisplayStoreList);
        fkDisplayCombo.setTriggerAction(TriggerAction.ALL);
        fkDisplayCombo.setEnabled(false);
        fkDisplayCombo.setSelection(fkDisplayList);
        this.add(fkDisplayCombo, new FormData("90%")); //$NON-NLS-1$

        exportBtn = new Button(MessagesFactory.getMessages().export_btn());
        exportBtn.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (!DownloadFilePanel.this.isValid()) {
                    return;
                }

                try {
                    final Map<String, String> param = buildExportParameter();
                    if (inheritanceNodeMap.keySet().size() > 0) {
                        StringBuilder errorMessage = new StringBuilder();
                        Set<Entry<String, List<String>>> entrySet = inheritanceNodeMap.entrySet();
                        for (Entry<String, List<String>> entry : entrySet) {
                            List<String> xpathList = entry.getValue();
                            for (String xpath : xpathList) {
                                errorMessage.append(entry.getKey());
                                errorMessage.append(" ----> "); //$NON-NLS-1$
                                errorMessage.append(xpath);
                                errorMessage.append("\r\n"); //$NON-NLS-1$
                            }
                        }
                        MessageBox.alert(MessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                                .missing_attribute(errorMessage.toString()), new Listener<MessageBoxEvent>() {

                            @Override
                            public void handleEvent(MessageBoxEvent be) {
                                downloadWarning(param);
                            }
                        });
                    } else {
                        downloadWarning(param);
                    }
                } catch (Exception e) {
                    MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages().export_error(),
                            null);
                }
                DownloadFilePanel.this.window.hide();
            }
        });
        this.add(exportBtn);
        this.setLabelWidth(180);
    }

    private Map<String, String> buildExportParameter() {
        List<String> viewableXpaths = viewBean.getViewableXpaths();
        EntityModel entityModel = viewBean.getBindingEntityModel();
        Map<String, TypeModel> dataTypes = entityModel.getMetaDataTypes();
        List<String> inheritanceNodePathList = CommonUtil.findInheritanceNodePath(entityModel);
        inheritanceNodeMap = new HashMap<String, List<String>>();
        List<String> xsiTypeList = new LinkedList<String>();
        List<String> headerList = new ArrayList<String>();
        List<String> xPathList = new ArrayList<String>();
        List<String> fkColXPathList = new ArrayList<String>();
        List<String> fkInfoList = new ArrayList<String>();

        for (String xpath : viewableXpaths) {
            headerList.add(xpath.substring(xpath.indexOf("/") + 1, xpath.length())); //$NON-NLS-1$
            if (xpath.endsWith(Constants.XSI_TYPE_QUALIFIED_NAME)) {
                xsiTypeList.add(xpath);
            } else {
                for (String inheritanceNodePath : inheritanceNodePathList) {
                    if (xpath.startsWith(inheritanceNodePath + "/")) { //$NON-NLS-1$
                        String xsiType = inheritanceNodePath + "/@" + Constants.XSI_TYPE_QUALIFIED_NAME; //$NON-NLS-1$
                        if (inheritanceNodeMap.get(xsiType) == null) {
                            List<String> subPathList = new LinkedList<String>();
                            subPathList.add(xpath);
                            inheritanceNodeMap.put(xsiType, subPathList);
                        } else {
                            inheritanceNodeMap.get(xsiType).add(xpath);
                        }
                    }
                }
            }
            xPathList.add(xpath);
            TypeModel typeModel = dataTypes.get(xpath);
            if (typeModel != null) {
                if (typeModel.getForeignkey() != null) {
                    fkColXPathList.add(xpath + "," + typeModel.getForeignkey()); //$NON-NLS-1$
                    List<String> fkInfo = typeModel.getForeignKeyInfo();
                    if (fkInfo.size() == 0) {
                        fkInfoList.add(" "); //$NON-NLS-1$
                    } else {
                        StringBuilder sb = new StringBuilder(fkInfo.get(0));
                        for (int i = 1; i < fkInfo.size(); i++) {
                            sb.append(",").append(fkInfo.get(i)); //$NON-NLS-1$
                        }
                        fkInfoList.add(sb.toString());
                    }
                }
            }
        }

        for (String xsiType : xsiTypeList) {
            inheritanceNodeMap.remove(xsiType);
        }

        List<String> selectItemIdsList = new ArrayList<String>();
        Grid<ItemBean> grid = ItemsListPanel.getInstance().getGrid();
        if (grid != null) {
            List<ItemBean> selectItemList = grid.getSelectionModel().getSelectedItems();
            for (int i = 0; i < selectItemList.size(); i++) {
                selectItemIdsList.add(selectItemList.get(i).getIds());
            }
        }

        Map<String, String> param = new HashMap<String, String>();

        queryModel.getModel();
        queryModel.getCriteria();
        queryModel.getLanguage();

        param.put("fileName", fileName.getValue()); //$NON-NLS-1$
        param.put("multipleValueSeparator", multipleValueSeparatorField.getValue()); //$NON-NLS-1$
        param.put("fkResovled", fkResovled.getValue().toString()); //$NON-NLS-1$
        param.put("fkDisplay", fkDisplayCombo.getValue().get("key").toString()); //$NON-NLS-1$ //$NON-NLS-2$
        param.put("tableName", viewBean.getViewPK()); //$NON-NLS-1$
        param.put("header", CommonUtil.convertList2Xml(headerList, "header")); //$NON-NLS-1$ //$NON-NLS-2$
        param.put("xpath", CommonUtil.convertList2Xml(xPathList, "xpath")); //$NON-NLS-1$ //$NON-NLS-2$
        param.put("fkColXPath", CommonUtil.convertList2Xml(fkColXPathList, "fkColXPath")); //$NON-NLS-1$ //$NON-NLS-2$
        param.put("fkInfo", CommonUtil.convertList2Xml(fkInfoList, "fkInfo")); //$NON-NLS-1$ //$NON-NLS-2$
        if (selectItemIdsList.size() > 0) {
            param.put(
                    "itemIdsListString", org.talend.mdm.webapp.base.shared.util.CommonUtil.convertListToString(selectItemIdsList, Constants.FILE_EXPORT_IMPORT_SEPARATOR)); //$NON-NLS-1$
        } else {
            param.put("itemIdsListString", ""); //$NON-NLS-1$//$NON-NLS-2$
        }
        param.put("criteria", queryModel.getCriteria()); //$NON-NLS-1$
        param.put("language", queryModel.getLanguage()); //$NON-NLS-1$

        RecordsPagingConfig pagingLoad = queryModel.getPagingLoadConfig();
        String sortDir = null;
        if (SortDir.ASC.equals(SortDir.findDir(pagingLoad.getSortDir()))) {
            sortDir = "ascending"; //$NON-NLS-1$
        }
        if (SortDir.DESC.equals(SortDir.findDir(pagingLoad.getSortDir()))) {
            sortDir = "descending"; //$NON-NLS-1$
        }
        Map<String, TypeModel> types = queryModel.getModel().getMetaDataTypes();
        TypeModel typeModel = types.get(pagingLoad.getSortField());
        if (typeModel != null) {
            if (DataTypeConstants.INTEGER.getTypeName().equals(typeModel.getType().getBaseTypeName())
                    || DataTypeConstants.INT.getTypeName().equals(typeModel.getType().getBaseTypeName())
                    || DataTypeConstants.LONG.getTypeName().equals(typeModel.getType().getBaseTypeName())
                    || DataTypeConstants.DECIMAL.getTypeName().equals(typeModel.getType().getBaseTypeName())
                    || DataTypeConstants.FLOAT.getTypeName().equals(typeModel.getType().getBaseTypeName())
                    || DataTypeConstants.DOUBLE.getTypeName().equals(typeModel.getType().getBaseTypeName())) {
                sortDir = "NUMBER:" + sortDir; //$NON-NLS-1$
            }
        }

        if (pagingLoad.getSortField() != null) {
            param.put("sortField", pagingLoad.getSortField()); //$NON-NLS-1$
        }
        if (sortDir != null) {
            param.put("sortDir", sortDir); //$NON-NLS-1$
        }
        param.put("offset", Integer.toString(pagingLoad.getOffset())); //$NON-NLS-1$
        param.put("limit", Integer.toString(pagingLoad.getLimit())); //$NON-NLS-1$
        return param;
    }

    private void downloadWarning(final Map<String, String> param) {
        MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory.getMessages()
                .export_items_greater_than_defalut(String.valueOf(defaultMaxExportRecordsCount)),
                new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {
                            PostDataUtil.postData(getActionUrl(), param);
                        }
                    }
                });
    }

    protected String getActionUrl() {
        return GWT.getHostPageBaseURL() + "/browserecords/download"; //$NON-NLS-1$
    }
}
