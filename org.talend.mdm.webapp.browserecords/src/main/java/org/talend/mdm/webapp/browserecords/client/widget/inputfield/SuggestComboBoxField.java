/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield;

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.model.BasePagingLoadConfigImpl;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.widget.ComboBoxEx;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsServiceAsync;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.Locale;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyField;

import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.fx.Resizable;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.util.DelayedTask;
import com.extjs.gxt.ui.client.widget.BoxComponent;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;

public class SuggestComboBoxField extends ComboBoxEx<ForeignKeyBean> {

    private BrowseRecordsServiceAsync service = (BrowseRecordsServiceAsync) Registry.get(BrowseRecords.BROWSERECORDS_SERVICE);

    private ListStore<ForeignKeyBean> foreignKeyStore = new ListStore<ForeignKeyBean>();

    private ForeignKeyBean selectedBean;

    private String displayFieldName = "displayInfo"; //$NON-NLS-1$

    private ForeignKeyField foreignKeyField;

    private int getListDelay = 200;

    private final int listLimitCount = 10;

    public static List<Integer> keyCodeList = new ArrayList<Integer>();

    public SuggestComboBoxField() {
        super();
        setUserProperties(BrowseRecords.getSession().getAppHeader().getUserProperties());
        init();
    }

    public SuggestComboBoxField(ForeignKeyField foreignKeyField) {
        super();
        this.foreignKeyField = foreignKeyField;
        setUserProperties(BrowseRecords.getSession().getAppHeader().getUserProperties());
        init();
    }

    protected void init() {
        initKeyCodeList();
        setDisplayField(displayFieldName);
        setTypeAhead(true);
        setTriggerAction(TriggerAction.ALL);
        setFireChangeEventOnSetValue(true);
        setStore(foreignKeyStore);
        setListener();
    }

    private void initKeyCodeList() {
        keyCodeList.add(KeyCodes.KEY_UP);
        keyCodeList.add(KeyCodes.KEY_DOWN);
        keyCodeList.add(KeyCodes.KEY_ESCAPE);
        keyCodeList.add(KeyCodes.KEY_ENTER);
        keyCodeList.add(KeyCodes.KEY_SHIFT);
        keyCodeList.add(KeyCodes.KEY_HOME);
        keyCodeList.add(KeyCodes.KEY_END);
        keyCodeList.add(KeyCodes.KEY_TAB);
        keyCodeList.add(KeyCodes.KEY_CTRL);
    }

    private DelayedTask task = new DelayedTask(new Listener<BaseEvent>() {

        @Override
        public void handleEvent(BaseEvent be) {
            String inputValue = getInputValue();
            if (inputValue != null && inputValue.length() > 0) {
                BasePagingLoadConfigImpl config = new BasePagingLoadConfigImpl();
                config.setLimit(listLimitCount);
                config.set("currentXpath", foreignKeyField.getCurrentPath()); //$NON-NLS-1$
                config.set("language", Locale.getLanguage()); //$NON-NLS-1$

                final TypeModel fieldTypeModel = foreignKeyField.getDataType();
                fieldTypeModel.setFilterValue(inputValue);
                String dataCluster = foreignKeyField.getDataCluster();
                service.getForeignKeySuggestion(config, fieldTypeModel,
                        org.talend.mdm.webapp.base.shared.util.CommonUtil.unescapeXml(foreignKeyField.parseForeignKeyFilter()),
                        dataCluster, Locale.getLanguage(), new SessionAwareAsyncCallback<List<ForeignKeyBean>>() {

                            @Override
                            public void onSuccess(List<ForeignKeyBean> result) {
                                updateListStore(result);
                                updateSelectedBean();
                            }
                        });
            }
        }
    });

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        this.setMinChars(1000);
        getElement().setAttribute("style", "top:0px"); //$NON-NLS-1$ //$NON-NLS-2$
        getInputEl().dom.setAttribute("autocomplete", "off"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected void afterRender() {
        super.afterRender();
        if (this.isEnabled()) {
            getInputEl().dom.setAttribute("style", "width:83%"); //$NON-NLS-1$//$NON-NLS-2$
        }
    }

    protected void setListener() {

        addListener(Events.KeyUp, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent e) {
                String inputValue = getInputValue();
                if (!keyCodeList.contains(e.getKeyCode())) {

                    if (e.getKeyCode() == KeyCodes.KEY_BACKSPACE || e.getKeyCode() == KeyCodes.KEY_DELETE) {
                        if (inputValue.contains("[") || inputValue.contains("]")) { //$NON-NLS-1$ //$NON-NLS-2$
                            setInputValue(""); //$NON-NLS-1$
                        } else if (inputValue == null || inputValue.trim().length() == 0 ){
                            foreignKeyField.setSuggestBoxValue(null);
                            clear();
                        }
                    }
                    
                    if (inputValue != null && !"".equals(inputValue.trim()) && !"[".equals(inputValue.trim())) { //$NON-NLS-1$ //$NON-NLS-2$
                        task.delay(getListDelay);
                    }
                }
            }
        });

        addSelectionChangedListener(new SelectionChangedListener<ForeignKeyBean>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<ForeignKeyBean> se) {
                setFieldValue();
            }

        });
    }

    class Resize extends Resizable {

        public Resize(BoxComponent resize) {
            super(resize);
        }

        public native BoxComponent getBoxComponent() /*-{
			return this.@com.extjs.gxt.ui.client.fx.Resizable::resize;
        }-*/;

    }

    @Override
    protected void onFocus(ComponentEvent be) {
        super.onFocus(be);
    }

    @Override
    public void disable() {
        if (!foreignKeyField.isEnabled()) {
            super.disable();
            setEditable(false);
            if (input != null) {
                input.dom.setAttribute("contenteditable", "false"); //$NON-NLS-1$//$NON-NLS-2$
                input.dom.removeAttribute("tabIndex"); //$NON-NLS-1$
            }
            fireEvent(Events.Disable);
        }
    }

    @Override
    public void enable() {
        super.enable();
        setEditable(true);
        if (input != null) {
            input.dom.setAttribute("contenteditable", "true"); //$NON-NLS-1$//$NON-NLS-2$
            input.dom.removeAttribute("tabIndex"); //$NON-NLS-1$
        }
    }

    @Override
    public void onAttach() {
        super.onAttach();
        if (GXT.isIE && input != null) {
            input.dom.removeAttribute("disabled"); //$NON-NLS-1$
        }
    }

    public void updateListStore(List<ForeignKeyBean> suggestionList) {
        foreignKeyStore.removeAll();

        if (suggestionList != null && suggestionList.size() > 0) {
            for (int i = 0; i < suggestionList.size(); i++) {
                ForeignKeyBean bean = suggestionList.get(i);
                if (bean != null) {
                    CommonUtil.setForeignKeyDisplayInfo(bean);
                    foreignKeyStore.add(bean);
                }
            }
            this.setExpanded(hidden);
        } else {
            this.setExpanded(getForceSelection());
        }
        this.expand();
    }

    public void updateSelectedBean() {
        if (foreignKeyStore != null) {
            for (ForeignKeyBean bean : foreignKeyStore.getModels()) {
                if (bean.getDisplayInfo() == null || "".equals(bean.getDisplayInfo()) || "null".equals(bean.getDisplayInfo())) { //$NON-NLS-1$ //$NON-NLS-2$
                    if (bean.getId().equalsIgnoreCase(this.getRawValue())) {
                        selectedBean = bean;
                    } else {
                        selectedBean = null;
                    }
                } else {
                    if (bean.getDisplayInfo().equalsIgnoreCase(this.getRawValue())) {
                        selectedBean = bean;
                    } else {
                        selectedBean = null;
                    }
                }
            }
            foreignKeyField.setSuggestBoxValue(selectedBean);
        }
    }

    public void setFieldValue() {
        foreignKeyField.setSuggestBoxValue(getValue());
    }

    public String getInputValue() {
        return getRawValue();
    }

    public void setInputValue(String input) {
        this.setRawValue(input);
    }

    public ListStore<ForeignKeyBean> getForeignKeyStore() {
        return this.foreignKeyStore;
    }

    public void setForeignKeyStore(ListStore<ForeignKeyBean> foreignKeyStore) {
        this.foreignKeyStore = foreignKeyStore;
    }

    @Override
    public void setValue(ForeignKeyBean fk) {
        selectedBean = fk;
        if (fk != null) {
            super.setValue(fk);
            if (selectedBean.getDisplayInfo() == null
                    || "".equals(selectedBean.getDisplayInfo()) || "null".equals(selectedBean.getDisplayInfo())) { //$NON-NLS-1$ //$NON-NLS-2$
                setRawValue(selectedBean.getId());
            } else {
                setRawValue(selectedBean.getDisplayInfo());
            }
        }
    }

    @Override
    public ForeignKeyBean getValue() {
        return selectedBean;
    }

    @Override
    public void clear() {
        super.clear();
    }

}