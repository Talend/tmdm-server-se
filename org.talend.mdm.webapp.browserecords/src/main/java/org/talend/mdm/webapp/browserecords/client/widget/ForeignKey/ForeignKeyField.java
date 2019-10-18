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

import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.base.shared.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.SuggestComboBoxField;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Image;

public class ForeignKeyField extends TextField<ForeignKeyBean> {

    private BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    protected TypeModel dataType;

    protected List<String> foreignKeyInfo;

    protected SuggestComboBoxField suggestBox;

    protected TextField<String> textField;

    protected String currentPath;

    protected String foreignKeyPath;

    private String usageField;

    protected boolean isStaging;

    protected Image selectButton;

    protected String foreignConceptName;

    protected boolean showInput;

    protected boolean showSelectButton;

    private Boolean editable = true;

    protected String foreignKeyFilter;

    protected String originForeignKeyFilter;

    protected boolean withTextInput; // Check if create input field or suggest box

    public ForeignKeyField(TypeModel dataType) {
        init(dataType);
        initField(false);
    }

    public ForeignKeyField(TypeModel dataType, boolean withTextInput) {
        init(dataType);
        initField(withTextInput);
    }

    private void init(TypeModel dataType) {
        this.dataType = dataType;
        this.foreignKeyPath = dataType.getForeignkey();
        this.foreignKeyInfo = dataType.getForeignKeyInfo();
        this.currentPath = dataType.getXpath();
        this.selectButton = new Image(Icons.INSTANCE.link());
        this.showSelectButton = !dataType.isReadOnly();
        this.setFireChangeEventOnSetValue(true);
        String[] foreignKeyPathArray = foreignKeyPath.split("/"); //$NON-NLS-1$
        if (foreignKeyPathArray.length > 0) {
            foreignConceptName = foreignKeyPathArray[0];
        }
    }

    private void initField(boolean withTextInput) {
        // If withTextInput=true, won't initialize suggestBox but will initialize textField
        if (withTextInput) {
            this.textField = new TextField<String>();
            this.withTextInput = withTextInput;
            this.showInput = false;
        } else {
            this.suggestBox = new SuggestComboBoxField(this);
            this.withTextInput = false;
            this.showInput = true;
        }
    }

    @Override
    protected void onRender(Element target, int index) {
        El wrap = renderField();
        addButtonListener();
        setAutoWidth(true);
        setElement(wrap.dom, target, index);

        super.onRender(target, index);
    }

    @Override
    protected void onResize(int width, int height) {
        if (withTextInput) {
            if ("SearchFieldCreator".equals(usageField)) { //$NON-NLS-1$
                textField.setWidth(width - selectButton.getWidth() - 20);
            } else {
                textField.setWidth(width);
            }
        } else {
            if ("SearchFieldCreator".equals(usageField)) { //$NON-NLS-1$
                suggestBox.setWidth(width - selectButton.getWidth() - 20);
            } else {
                suggestBox.setWidth(width);
            }
        }
    }

    @Override
    protected void doAttachChildren() {
        super.doAttachChildren();
        ComponentHelper.doAttach(selectButton);
        if (showInput) {
            ComponentHelper.doAttach(suggestBox);
        }
        if (withTextInput) {
            ComponentHelper.doAttach(textField);
        }
    }

    @Override
    protected void doDetachChildren() {
        super.doDetachChildren();
        ComponentHelper.doDetach(selectButton);
        if (showInput) {
            ComponentHelper.doDetach(suggestBox);
        }
        if (withTextInput) {
            ComponentHelper.doAttach(textField);
        }
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        if (suggestBox != null) {
            suggestBox.setReadOnly(readOnly);
        }
        if (textField != null) {
            textField.setReadOnly(readOnly);
        }
        selectButton.setVisible(!readOnly);
    }

    protected El renderField() {
        El wrap = new El(DOM.createTable());
        wrap.setElementAttribute("cellSpacing", "0"); //$NON-NLS-1$ //$NON-NLS-2$
        Element tbody = DOM.createTBody();
        Element foreignKeyTR = DOM.createTR();
        tbody.appendChild(foreignKeyTR);
        if (showInput) {
            Element inputTD = DOM.createTD();
            foreignKeyTR.appendChild(inputTD);
            suggestBox.render(inputTD);
        }
        if (withTextInput) {
            Element inputTD = DOM.createTD();
            foreignKeyTR.appendChild(inputTD);
            textField.render(inputTD);
            textField.setPosition(1, 1);
            textField.setValue("*"); //$NON-NLS-1$
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
        iconBody.appendChild(iconTR);
        iconTD.setAttribute("align", "right"); //$NON-NLS-1$//$NON-NLS-2$
        iconTD.appendChild(iconBody);
        wrap.appendChild(tbody);
        return wrap;
    }

    public TypeModel getDataType() {
        return this.dataType;
    }

    public void setDataType(TypeModel dataType) {
        this.dataType = dataType;
    }

    public void setUsageField(String usageField) {
        this.usageField = usageField;
    }

    public void setStaging(boolean isStaging) {
        this.isStaging = isStaging;
    }

    public List<String> getForeignKeyInfo() {
        return this.foreignKeyInfo;
    }

    public String getCurrentPath() {
        return this.currentPath;
    }

    public String parseForeignKeyFilter() {
        return CommonUtil.EMPTY;
    }

    public String getForeignKeyPath() {
        return this.foreignKeyPath;
    }

    public String getDataCluster() {
        if (isStaging) {
            return BrowseRecords.getSession().getAppHeader().getStagingDataCluster();
        } else {
            return BrowseRecords.getSession().getAppHeader().getMasterDataCluster();
        }
    }

    @Override
    public void setValue(ForeignKeyBean foreignKeyBean) {
        if (foreignKeyBean != null) {
            foreignKeyBean.setShowInfo(foreignKeyInfo.size() > 0);
        }
        if (suggestBox != null) {
            if (foreignKeyBean == null) {
                suggestBox.setRawValue(CommonUtil.EMPTY);
            } else {
                suggestBox.setValue(foreignKeyBean);
            }
        }
        if (withTextInput) {
            if (foreignKeyBean != null) {
                String value = foreignKeyBean.getId();
                // the value return by FK Picker wrapped by '[' and ']'
                // in Advance Search, the value from the input, only the FK id.
                // if the value wrapped by '[' and ']', will remove them
                if (value.startsWith("[") && value.endsWith("]")) { //$NON-NLS-1$//$NON-NLS-2$
                    value = value.substring(1, value.length() - 1);
                }
                textField.setRawValue(value);
                foreignKeyBean = null;
            }
        }
        super.setValue(foreignKeyBean);
    }

    public void setSuggestBoxValue(ForeignKeyBean foreignKeyBean) {
        if (foreignKeyBean != null) {
            foreignKeyBean.setShowInfo(foreignKeyInfo.size() > 0);
        }
        if (suggestBox != null) {
            suggestBox.setValue(foreignKeyBean);
        }
        super.setValue(foreignKeyBean);
    }

    @Override
    public ForeignKeyBean getValue() {
        if (suggestBox != null && suggestBox.getValue() != null) {
            return suggestBox.getValue();
        }
        return value;
    }

    @Override
    protected void onFocus(ComponentEvent be) {
        if (suggestBox != null) {
            suggestBox.focus();
        } else if (withTextInput) {
            textField.focus();
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        selectButton.setVisible(enabled);
        if (enabled) {
            enable();
            if (suggestBox != null) {
                suggestBox.enable();
            }
            if (withTextInput) {
                textField.enable();
            }
        } else {
            disable();
            if (suggestBox != null) {
                suggestBox.disable();
            }
            if (withTextInput) {
                textField.disable();
            }
        }
    }

    protected void addButtonListener() {
        selectButton.setTitle(MessagesFactory.getMessages().fk_select_title());
        final ForeignKeyField _this = this;
        selectButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent ce) {
                if (foreignConceptName != null) {

                    String foreignKeyFilter = getOriginForeignKeyFilter();
                    if (foreignKeyFilter.contains(CommonUtil.FN_PREFIX)) {
                        AppEvent event = new AppEvent(BrowseRecordsEvents.TransformFkFilterItem, foreignKeyFilter);
                        event.setData(BrowseRecords.FOREIGN_KEY_FIELD, _this);
                        event.setData(BrowseRecords.FOREIGN_KEY_FILTER, foreignKeyFilter);
                        Dispatcher.forwardEvent(event);
                    }

                    service.getEntityModel(foreignConceptName, Locale.getLanguage(),
                            new SessionAwareAsyncCallback<EntityModel>() {

                                @Override
                                public void onSuccess(EntityModel entityModel) {
                                    ForeignKeyListWindow foreignKeyListWindow = new ForeignKeyListWindow(foreignKeyPath,
                                            foreignKeyInfo, getDataCluster(), entityModel, ForeignKeyField.this);
                                    foreignKeyListWindow.setForeignKeyFilterValue(parseForeignKeyFilter());
                                    foreignKeyListWindow.setSize(550, 350);
                                    foreignKeyListWindow.setResizable(true);
                                    foreignKeyListWindow.setModal(true);
                                    foreignKeyListWindow.setBlinkModal(true);
                                    foreignKeyListWindow.setHeading(MessagesFactory.getMessages().fk_RelatedRecord());
                                    foreignKeyListWindow.show();
                                }
                            });
                }
            }
        });
    }

    @Override
    protected void onKeyDown(FieldEvent fe) {
        if (fe.getKeyCode() == 13 && !isEditable()) {
            fe.stopEvent();
            return;
        }
        super.onKeyDown(fe);
    }

    @Override
    public void onDisable() {
    }

    public String getTextInputValue() {
        if (withTextInput) {
            return textField.getRawValue();
        }
        return CommonUtil.EMPTY;
    }

    public void setShowInput(boolean showInput) {
        this.showInput = showInput;
    }

    public void setShowSelectButton(boolean showSelectButton) {
        this.showSelectButton = showSelectButton;
    }

    public Boolean isEditable() {
        return this.editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public SuggestComboBoxField getSuggestBox() {
        return this.suggestBox;
    }

    public void setForeignKeyFilter(String foreignKeyFilter) {
        this.foreignKeyFilter = foreignKeyFilter;
    }

    public String getForeignKeyFilter() {
        return this.foreignKeyFilter;
    }

    public String getOriginForeignKeyFilter() {
        return this.originForeignKeyFilter == null ? CommonUtil.EMPTY : this.originForeignKeyFilter;
    }

    public boolean isWithTextInput() {
        return withTextInput;
    }
    public void getOriginForeignKeyFilter(String originForeignKeyFilter) {
        this.originForeignKeyFilter = originForeignKeyFilter;
    }
}
