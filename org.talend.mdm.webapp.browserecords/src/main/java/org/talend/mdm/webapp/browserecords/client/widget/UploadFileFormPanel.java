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

import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.client.util.MultilanguageMessageParser;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.FileUtil;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.shared.Constants;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
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
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.extjs.gxt.ui.client.widget.form.MultiField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.core.client.GWT;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public class UploadFileFormPanel extends FormPanel implements Listener<FormEvent> {

    private ComboBox<ItemBaseModel> separatorCombo;

    private ComboBox<ItemBaseModel> textDelimiterCombo;

    private ComboBox<ItemBaseModel> encodingCombo;

    private FileUploadField file;

    private TextField<String> multipleValueSeparatorField;

    private CheckBox isPartialUpdate;
    
    private CheckBox headerLine;

    private HiddenField<String> conceptField;

    private HiddenField<String> headerField;

    private HiddenField<String> mandatoryField;

    private HiddenField<String> languageField;

    private HiddenField<String> viewableXpathField;

    private HiddenField<String> inheritanceNodePath;

    private MessageBox waitBar;

    private String type;

    private ViewBean viewBean;

    private Window window;

    private int importRecordsDefaultCount;

    public UploadFileFormPanel(ViewBean viewBean, Window window) {
        this.viewBean = viewBean;
        this.window = window;
        this.setFrame(false);
        this.setHeaderVisible(false);
        this.setEncoding(Encoding.MULTIPART);
        this.setMethod(Method.POST);
        this.setWidth("100%"); //$NON-NLS-1$
        this.setAction(getActionUrl());
        this.renderForm();
        importRecordsDefaultCount = BrowseRecords.getSession().getAppHeader().getImportRecordsDefaultCount();
    }

    protected String getHeaderString() {
        EntityModel entityModel = viewBean.getBindingEntityModel();
        Map<String, TypeModel> dataTypes = entityModel.getMetaDataTypes();
        List<String> viewableXpathList = viewBean.getViewableXpaths();
        StringBuilder headerStringBuilder = new StringBuilder();
        for (String viewableXpath : viewableXpathList) {
            TypeModel typeModel = null;
            if (viewableXpath.endsWith(Constants.XSI_TYPE_QUALIFIED_NAME)) {
                typeModel = dataTypes.get(viewableXpath.substring(0, viewableXpath.lastIndexOf("/"))); //$NON-NLS-1$
            } else {
                typeModel = dataTypes.get(viewableXpath);
            }
            if (!headerStringBuilder.toString().isEmpty()) {
                headerStringBuilder.append(Constants.FILE_EXPORT_IMPORT_SEPARATOR);
            }
            headerStringBuilder.append(org.talend.mdm.webapp.base.shared.util.CommonUtil.escape(viewableXpath
                    + Constants.HEADER_VISIBILITY_SEPARATOR + typeModel.isVisible()));
        }
        return headerStringBuilder.toString();
    }

    protected String getViewableXpathString() {
        return org.talend.mdm.webapp.base.shared.util.CommonUtil.convertListToString(viewBean.getViewableXpaths(),
                Constants.FILE_EXPORT_IMPORT_SEPARATOR);
    }

    private String getMandatoryStr() {
        EntityModel entityModel = viewBean.getBindingEntityModel();
        Map<String, TypeModel> dataTypes = entityModel.getMetaDataTypes();
        List<String> xpathList = viewBean.getViewableXpaths();
        StringBuilder mandatoryStringBuilder = new StringBuilder();

        for (String key : xpathList) {
            TypeModel typeModel = dataTypes.get(key);
            if (typeModel != null) {
                if (typeModel.getMinOccurs() == 1) {
                    if (!mandatoryStringBuilder.toString().isEmpty()) {
                        mandatoryStringBuilder.append(Constants.FILE_EXPORT_IMPORT_SEPARATOR);
                    }
                    mandatoryStringBuilder.append(typeModel.getTypePath());
                }
            }
        }
        return org.talend.mdm.webapp.base.shared.util.CommonUtil.escape(mandatoryStringBuilder.toString());
    }

    private void renderForm() {
        FormData fileData = new FormData("98%"); //$NON-NLS-1$
        FormData multipleValueData = new FormData(100, 20);
        FormData comboData = new FormData("84%"); //$NON-NLS-1$

        conceptField = new HiddenField<String>();
        conceptField.setName("concept");//$NON-NLS-1$
        conceptField.setValue(viewBean.getBindingEntityModel().getConceptName());
        this.add(conceptField);

        headerField = new HiddenField<String>();
        headerField.setName("header");//$NON-NLS-1$
        headerField.setValue(getHeaderString());
        this.add(headerField);

        mandatoryField = new HiddenField<String>();
        mandatoryField.setName("mandatoryField");//$NON-NLS-1$
        mandatoryField.setValue(getMandatoryStr());
        this.add(mandatoryField);

        languageField = new HiddenField<String>();
        languageField.setName("language");//$NON-NLS-1$
        languageField.setValue(UrlUtil.getLanguage());
        this.add(languageField);

        viewableXpathField = new HiddenField<String>();
        viewableXpathField.setName("viewableXpath");//$NON-NLS-1$
        viewableXpathField.setValue(getViewableXpathString());
        this.add(viewableXpathField);

        inheritanceNodePath = new HiddenField<String>();
        inheritanceNodePath.setName("inheritanceNodePath");//$NON-NLS-1$
        inheritanceNodePath.setValue(org.talend.mdm.webapp.base.shared.util.CommonUtil.convertListToString(
                org.talend.mdm.webapp.browserecords.client.util.CommonUtil.findInheritanceNodePath(viewBean
                        .getBindingEntityModel()), Constants.FILE_EXPORT_IMPORT_SEPARATOR));
        this.add(inheritanceNodePath);

        file = new FileUploadField();
        file.setAllowBlank(false);
        file.setName("file"); //$NON-NLS-1$
        file.setId("fileUpload");//$NON-NLS-1$   
        file.setFieldLabel(MessagesFactory.getMessages().label_field_file());
        file.addListener(Events.Change, new Listener<BaseEvent>() {

            @Override
            public void handleEvent(BaseEvent be) {
                type = FileUtil.getFileType(file.getValue());
                if (type.equalsIgnoreCase("CSV")) { //$NON-NLS-1$
                    separatorCombo.enable();
                    textDelimiterCombo.enable();
                    encodingCombo.enable();

                    separatorCombo.setAllowBlank(false);
                    textDelimiterCombo.setAllowBlank(false);
                    encodingCombo.setAllowBlank(false);
                } else {
                    separatorCombo.disable();
                    textDelimiterCombo.disable();
                    encodingCombo.disable();

                    separatorCombo.setAllowBlank(true);
                    separatorCombo.validate();
                    textDelimiterCombo.setAllowBlank(true);
                    textDelimiterCombo.validate();
                    encodingCombo.setAllowBlank(true);
                    encodingCombo.validate();
                }
            }
        });
        this.add(file, fileData);

        multipleValueSeparatorField = new TextField<String>();
        multipleValueSeparatorField.setId("multipleValueSeparator"); //$NON-NLS-1$
        multipleValueSeparatorField.setName("multipleValueSeparator"); //$NON-NLS-1$
        multipleValueSeparatorField.setMaxLength(1);
        multipleValueSeparatorField.setValue("|"); //$NON-NLS-1$
        MultiField<String> multipleValueField = new MultiField<String>();
        LabelField lf = new LabelField();
        multipleValueField.setFieldLabel(MessagesFactory.getMessages().multiple_value_separator_field_label());
        multipleValueField.add(multipleValueSeparatorField);
        multipleValueField.add(lf);
        this.add(multipleValueField, multipleValueData);

        List<ItemBaseModel> list = new ArrayList<ItemBaseModel>();
        ItemBaseModel excel = new ItemBaseModel();
        excel.set("label", "Excel"); //$NON-NLS-1$ //$NON-NLS-2$
        excel.set("key", "excel"); //$NON-NLS-1$ //$NON-NLS-2$
        list.add(excel);

        ItemBaseModel csv = new ItemBaseModel();
        csv.set("label", "CSV"); //$NON-NLS-1$ //$NON-NLS-2$
        csv.set("key", "csv"); //$NON-NLS-1$ //$NON-NLS-2$
        list.add(csv);
        ListStore<ItemBaseModel> typeList = new ListStore<ItemBaseModel>();
        typeList.add(list);

        isPartialUpdate = new CheckBox();
        isPartialUpdate.setId("isPartialUpdate"); //$NON-NLS-1$
        isPartialUpdate.setName("isPartialUpdate"); //$NON-NLS-1$
        isPartialUpdate.setValueAttribute("on"); //$NON-NLS-1$
        isPartialUpdate.setFieldLabel(MessagesFactory.getMessages().label_field_partial_update());
        isPartialUpdate.setValue(false);
        isPartialUpdate.setInputStyleAttribute("left", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
        isPartialUpdate.setInputStyleAttribute("position", "absolute"); //$NON-NLS-1$ //$NON-NLS-2$
        this.add(isPartialUpdate);
        
        headerLine = new CheckBox();
        headerLine.setId("headersOnFirstLine");//$NON-NLS-1$
        headerLine.setName("headersOnFirstLine");//$NON-NLS-1$
        headerLine.setValueAttribute("on"); //$NON-NLS-1$
        headerLine.setFieldLabel(MessagesFactory.getMessages().label_field_header_first());
        headerLine.setValue(true);
        headerLine.setInputStyleAttribute("left", "10px"); //$NON-NLS-1$ //$NON-NLS-2$
        headerLine.setInputStyleAttribute("position", "absolute"); //$NON-NLS-1$ //$NON-NLS-2$
        this.add(headerLine);

        List<ItemBaseModel> separatorList = new ArrayList<ItemBaseModel>();
        ItemBaseModel comma = new ItemBaseModel();
        comma.set("label", "comma"); //$NON-NLS-1$ //$NON-NLS-2$
        comma.set("key", "comma"); //$NON-NLS-1$ //$NON-NLS-2$
        separatorList.add(comma);

        ItemBaseModel semicolon = new ItemBaseModel();
        semicolon.set("label", "semicolon"); //$NON-NLS-1$ //$NON-NLS-2$
        semicolon.set("key", "semicolon"); //$NON-NLS-1$ //$NON-NLS-2$
        separatorList.add(semicolon);

        ListStore<ItemBaseModel> separatorStoreList = new ListStore<ItemBaseModel>();
        separatorStoreList.add(separatorList);

        separatorCombo = new ComboBox<ItemBaseModel>();
        separatorCombo.setId("sep");//$NON-NLS-1$
        separatorCombo.setName("sep");//$NON-NLS-1$
        separatorCombo.setFieldLabel(MessagesFactory.getMessages().label_field_separator());
        separatorCombo.setDisplayField("label"); //$NON-NLS-1$
        separatorCombo.setValueField("key"); //$NON-NLS-1$
        separatorCombo.setStore(separatorStoreList);
        separatorCombo.setTriggerAction(TriggerAction.ALL);
        this.add(separatorCombo, comboData);

        List<ItemBaseModel> textDelimiterList = new ArrayList<ItemBaseModel>();
        ItemBaseModel doubleDelimiter = new ItemBaseModel();
        doubleDelimiter.set("label", "\""); //$NON-NLS-1$ //$NON-NLS-2$
        doubleDelimiter.set("key", "\""); //$NON-NLS-1$ //$NON-NLS-2$
        textDelimiterList.add(doubleDelimiter);

        ItemBaseModel singleDelimiter = new ItemBaseModel();
        singleDelimiter.set("label", "\'"); //$NON-NLS-1$ //$NON-NLS-2$
        singleDelimiter.set("key", "\'"); //$NON-NLS-1$ //$NON-NLS-2$
        textDelimiterList.add(singleDelimiter);

        ListStore<ItemBaseModel> textDelimiterStoreList = new ListStore<ItemBaseModel>();
        textDelimiterStoreList.add(textDelimiterList);

        textDelimiterCombo = new ComboBox<ItemBaseModel>();
        textDelimiterCombo.setId("delimiter");//$NON-NLS-1$
        textDelimiterCombo.setName("delimiter");//$NON-NLS-1$
        textDelimiterCombo.setFieldLabel(MessagesFactory.getMessages().label_field_delimiter());
        textDelimiterCombo.setDisplayField("label"); //$NON-NLS-1$
        textDelimiterCombo.setValueField("key"); //$NON-NLS-1$
        textDelimiterCombo.setStore(textDelimiterStoreList);
        textDelimiterCombo.setTriggerAction(TriggerAction.ALL);
        this.add(textDelimiterCombo, comboData);

        List<ItemBaseModel> encodingList = new ArrayList<ItemBaseModel>();
        ItemBaseModel utf8 = new ItemBaseModel();
        utf8.set("label", "UTF-8"); //$NON-NLS-1$ //$NON-NLS-2$
        utf8.set("key", "utf-8"); //$NON-NLS-1$ //$NON-NLS-2$
        encodingList.add(utf8);

        ItemBaseModel iso88591 = new ItemBaseModel();
        iso88591.set("label", "ISO-8859-1"); //$NON-NLS-1$ //$NON-NLS-2$
        iso88591.set("key", "iso-8859-1"); //$NON-NLS-1$ //$NON-NLS-2$
        encodingList.add(iso88591);

        ItemBaseModel iso885915 = new ItemBaseModel();
        iso885915.set("label", "ISO-8859-15"); //$NON-NLS-1$ //$NON-NLS-2$
        iso885915.set("key", "iso-8859-15"); //$NON-NLS-1$ //$NON-NLS-2$
        encodingList.add(iso885915);

        ItemBaseModel cp1252 = new ItemBaseModel();
        cp1252.set("label", "cp1252"); //$NON-NLS-1$ //$NON-NLS-2$
        cp1252.set("key", "cp-1252"); //$NON-NLS-1$ //$NON-NLS-2$
        encodingList.add(cp1252);

        ItemBaseModel gbk = new ItemBaseModel();
        gbk.set("label", "GBK"); //$NON-NLS-1$ //$NON-NLS-2$
        gbk.set("key", "GBK"); //$NON-NLS-1$ //$NON-NLS-2$
        encodingList.add(gbk);

        ListStore<ItemBaseModel> encodingStoreList = new ListStore<ItemBaseModel>();
        encodingStoreList.add(encodingList);

        encodingCombo = new ComboBox<ItemBaseModel>();
        encodingCombo.setId("encodings");//$NON-NLS-1$
        encodingCombo.setName("encodings");//$NON-NLS-1$
        encodingCombo.setFieldLabel(MessagesFactory.getMessages().label_field_encoding());
        encodingCombo.setDisplayField("label"); //$NON-NLS-1$
        encodingCombo.setValueField("key"); //$NON-NLS-1$
        encodingCombo.setStore(encodingStoreList);
        encodingCombo.setTriggerAction(TriggerAction.ALL);
        this.add(encodingCombo, comboData);

        Button submit = new Button(MessagesFactory.getMessages().label_button_submit());
        submit.setId("btnSubmit");//$NON-NLS-1$
        submit.addSelectionListener(new SelectionListener<ButtonEvent>() {

            @Override
            public void componentSelected(ButtonEvent ce) {
                if (!UploadFileFormPanel.this.isValid()) {
                    return;
                }

                if (("".equalsIgnoreCase(type)) || !Constants.FileType_Imported.contains(type.toLowerCase())) { //$NON-NLS-1$
                    MessageBox.alert(MessagesFactory.getMessages().error_title(), MessagesFactory.getMessages()
                            .error_incompatible_file_type(), null);
                    return;
                }

                MessageBox.confirm(MessagesFactory.getMessages().confirm_title(), MessagesFactory.getMessages()
                        .import_items_greater_than_default(String.valueOf(importRecordsDefaultCount)), new Listener<MessageBoxEvent>() {

                    @Override
                    public void handleEvent(MessageBoxEvent be) {
                        if (be.getButtonClicked().getItemId().equals(Dialog.YES)) {

                            UploadFileFormPanel.this.submit();
                            waitBar = MessageBox.wait(MessagesFactory.getMessages().import_progress_bar_title(), MessagesFactory
                                    .getMessages().import_progress_bar_message(), MessagesFactory.getMessages()
                                    .import_progress_bar_laod());
                        }
                    }
                });
            }
        });
        this.addButton(submit);
        this.setButtonAlign(HorizontalAlignment.CENTER);

        separatorCombo.disable();
        textDelimiterCombo.disable();
        encodingCombo.disable();

        this.setLabelWidth(200);
        this.addListener(Events.Submit, this);
    }

    @Override
    public void handleEvent(final FormEvent formEvent) {
        waitBar.close();
        if (formEvent.getResultHtml().contains(Constants.IMPORT_SUCCESS)) {
            MessageBox.alert(MessagesFactory.getMessages().info_title(), MessagesFactory.getMessages().import_success_label(),
                    new Listener<MessageBoxEvent>() {

                        @Override
                        public void handleEvent(MessageBoxEvent be) {
                            window.hide();
                            ButtonEvent buttonEvent = new ButtonEvent(ItemsToolBar.getInstance().searchButton);
                            ItemsToolBar.getInstance().searchButton.fireEvent(Events.Select, buttonEvent);
                        }
                    });
        } else {
            String returnMessage = filterFormatTag(formEvent.getResultHtml());
            String displayMessage = MultilanguageMessageParser.pickOutISOMessage(returnMessage);
            if (!MultilanguageMessageParser.getLanguageValueMap(returnMessage).isEmpty()) {
                displayMessage = returnMessage.substring(0, returnMessage.indexOf("[")) + MultilanguageMessageParser.pickOutISOMessage(returnMessage); //$NON-NLS-1$
            }
            MessageBox.alert(MessagesFactory.getMessages().error_title(), displayMessage, null);
        }
    }

    protected String getActionUrl() {
        return GWT.getHostPageBaseURL() + "/browserecords/upload"; //$NON-NLS-1$
    }

    private String filterFormatTag(String message) {
        String msg = message;
        msg = msg.replace("<pre>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        msg = msg.replace("</pre>", ""); //$NON-NLS-1$ //$NON-NLS-2$
        return msg;
    }
}
