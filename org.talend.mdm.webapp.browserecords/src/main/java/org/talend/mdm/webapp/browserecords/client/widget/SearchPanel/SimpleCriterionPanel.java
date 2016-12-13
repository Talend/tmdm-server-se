/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.SearchPanel;

import java.util.HashMap;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.model.SimpleCriterion;
import org.talend.mdm.webapp.base.client.widget.MultiLanguageField;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;
import org.talend.mdm.webapp.browserecords.client.util.StagingConstant;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeyField;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ReturnCriteriaFK;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.ComboBoxField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.SpinnerField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.creator.SearchFieldCreator;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.extjs.gxt.ui.client.widget.form.RadioGroup;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;

/**
 * DOC stephane class global comment. Detailled comment
 */
public class SimpleCriterionPanel<T> extends HorizontalPanel implements ReturnCriteriaFK {

    private ComboBoxField<BaseModel> keyComboBox;

    private ComboBoxField<BaseModel> operatorComboBox;

    private Field field;

    private Button searchBut;

    private LayoutContainer content = new LayoutContainer();

    private ViewBean view;

    private Map<String, TypeModel> itemsPredicates = new HashMap<String, TypeModel>();

    private ListStore<BaseModel> list = new ListStore<BaseModel>();

    private ListStore<BaseModel> operatorlist = new ListStore<BaseModel>();

    private boolean staging;

    public SimpleCriterionPanel(final MultipleCriteriaPanel ancestor, final Panel parent, Button searchBut) {
        super();
        this.searchBut = searchBut;
        setSpacing(3);

        keyComboBox = new ComboBoxField<BaseModel>();
        keyComboBox.setDisplayField("name"); //$NON-NLS-1$
        keyComboBox.setValueField("value"); //$NON-NLS-1$
        keyComboBox.setStore(list);
        keyComboBox.setTriggerAction(TriggerAction.ALL);
        keyComboBox.setId("BrowseRecords_SimpleKeyComboBox"); //$NON-NLS-1$
        addKeyComboBoxListener(null);
        add(keyComboBox);

        operatorComboBox = new ComboBoxField<BaseModel>();
        operatorComboBox.setDisplayField("name"); //$NON-NLS-1$
        operatorComboBox.setValueField("value"); //$NON-NLS-1$
        operatorComboBox.setStore(operatorlist);
        operatorComboBox.setTriggerAction(TriggerAction.ALL);
        operatorComboBox.setId("BrowseRecords_SimpleOperatorComboBox"); //$NON-NLS-1$
        add(operatorComboBox);

        content.setId("BrowseRecords_ContentField"); //$NON-NLS-1$
        add(content);

        if (ancestor != null) {
            add(new Image(Icons.INSTANCE.Delete()) {

                {
                    addClickHandler(new ClickHandler() {

                        @Override
                        public void onClick(ClickEvent ce) {
                            parent.remove(SimpleCriterionPanel.this);
                            if (ancestor != null) {
                                ancestor.redraw();
                            }
                        }
                    });
                }
            });
        } else {
        }

    }

    public SimpleCriterionPanel(final MultipleCriteriaPanel ancestor, final Panel parent, Button searchBut, boolean staging) {
        this(ancestor, parent, searchBut);
        this.staging = staging;
    }

    public SimpleCriterionPanel(Button searchBut) {
        super();
        this.searchBut = searchBut;
        Grid sizeGrid = new Grid(3, 1);

        keyComboBox = new ComboBoxField<BaseModel>();
        keyComboBox.setDisplayField("name"); //$NON-NLS-1$
        keyComboBox.setValueField("value"); //$NON-NLS-1$
        keyComboBox.setStore(list);
        keyComboBox.setTriggerAction(TriggerAction.ALL);
        keyComboBox.setId("MenuItem_BrowseRecords_SimpleKeyComboBox"); //$NON-NLS-1$

        sizeGrid.setWidget(0, 0, keyComboBox);

        operatorComboBox = new ComboBoxField<BaseModel>();
        operatorComboBox.setDisplayField("name"); //$NON-NLS-1$
        operatorComboBox.setValueField("value"); //$NON-NLS-1$
        operatorComboBox.setStore(operatorlist);
        operatorComboBox.setTriggerAction(TriggerAction.ALL);
        operatorComboBox.setId("MenuItem_BrowseRecords_SimpleOperatorComboBox"); //$NON-NLS-1$
        sizeGrid.setWidget(1, 0, operatorComboBox);

        content.setId("MenuItem_BrowseRecords_ContentField"); //$NON-NLS-1$
        sizeGrid.setWidget(2, 0, content);

        sizeGrid.getElement().getStyle().setMarginLeft(24D, Unit.PX);
        add(sizeGrid);
    }

    public SimpleCriterionPanel(Button searchBut, boolean staging) {
        this(searchBut);
        this.staging = staging;
    }

    public void updateFields(ViewBean view) {
        this.view = view;
        // field combobox
        BaseModel field;
        list.removeAll();
        if (this.view.getSearchables() != null) {
            for (String key : this.view.getSearchables().keySet()) {
                field = new BaseModel();
                field.set("name", this.view.getSearchables().get(key)); //$NON-NLS-1$
                // TMDM-6441: fixed the parsing errors
                if (key.endsWith(StagingConstant.STAGING_TASKID)) {
                    key = StagingConstant.STAGING_TASKID.substring(1);
                }
                field.set("value", key); //$NON-NLS-1$
                for (BaseModel model : list.getModels()) {
                    if (model.get("name").equals(this.view.getSearchables().get(key))) { //$NON-NLS-1$
                        field = new BaseModel();
                        field.set("name", this.view.getSearchables().get(key) + "(" + key + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        // TMDM-6441: fixed the parsing errors
                        if (key.endsWith(StagingConstant.STAGING_TASKID)) {
                            key = StagingConstant.STAGING_TASKID.substring(1);
                        }
                        field.set("value", key); //$NON-NLS-1$
                    }
                }
                list.add(field);
            }
        } else {
            field = new BaseModel();
            field.set("", ""); //$NON-NLS-1$  //$NON-NLS-2$
            list.add(field);
        }

        if (this.view.getBindingEntityModel().getMetaDataTypes() != null) {
            itemsPredicates.clear();
            for (String key : this.view.getBindingEntityModel().getMetaDataTypes().keySet()) {
                itemsPredicates.put(key, this.view.getBindingEntityModel().getMetaDataTypes().get(key));
            }
        }

        keyComboBox.setValue(list.getAt(0));
    }

    private void setOperatorComboBox(Map<String, String> cons) {
        BaseModel field;

        operatorlist.removeAll();
        for (String curOper : cons.keySet()) {
            field = new BaseModel();
            field.set("name", cons.get(curOper)); //$NON-NLS-1$
            field.set("value", curOper); //$NON-NLS-1$
            operatorlist.add(field);
        }
        operatorComboBox.setValue(operatorlist.getAt(0));
    }

    private TypeModel adaptOperatorAndValue() {
        content.removeAll();
        TypeModel typeModel = itemsPredicates.get(getKey());
        field = SearchFieldCreator.createField(typeModel);
        if (field != null) {
            field.setId("SimpleSearchValueFiled"); //$NON-NLS-1$
            if (field instanceof ForeignKeyField) {
                // ((FKSearchField) field).Update(getKey(), this);
                ((ForeignKeyField) field).setStaging(staging);
            }
            content.add(field);
            field.addListener(Events.KeyDown, new Listener<FieldEvent>() {

                @Override
                public void handleEvent(FieldEvent be) {
                    if (be.getKeyCode() == KeyCodes.KEY_ENTER) {
                        if (searchBut != null) {
                            field.setValue(field.getValue());
                            searchBut.fireEvent(Events.Select);
                        }
                    }
                }
            });
        }
        setOperatorComboBox(SearchFieldCreator.cons);
        content.layout();
        return typeModel;
    }

    public void focusField() {
        if (field != null) {
            field.focus();
        }
    }

    private String getKey() {
        return keyComboBox.getValue().get("value"); //$NON-NLS-1$
    }

    public String getOperator() {
        return operatorComboBox.getValue().get("value"); //$NON-NLS-1$
    }

    public String getValue() {

        if (field != null) {

            if (field.getValue() == null) {
                return "";
            }

            if (field instanceof ForeignKeyField) {
                return ((ForeignKeyField) field).getValue().getId();
            } else if (field instanceof DateField) {
                return ((DateField) field).getPropertyEditor().getFormat().format(((DateField) field).getValue());
            } else if (field instanceof RadioGroup) {
                return ((RadioGroup) field).getValue().getValueAttribute();
            } else if (field instanceof SimpleComboBox) {
                return ((SimpleComboBox) field).getValue().get("value"); //$NON-NLS-1$
            } else if (field instanceof MultiLanguageField) {
                return ((MultiLanguageField) field).getMultiLanguageKeywordsValue(getOperator());
            }
            return field.getValue().toString();
        }

        return null;
    }

    private String getInfo() {
        String info = getValue();
        if (field != null && field instanceof ForeignKeyField && field.getValue() != null) {
            ForeignKeyBean fkField = ((ForeignKeyField) field).getValue();
            return fkField.getDisplayInfo() != null ? fkField.getDisplayInfo() : fkField.getId();
        }
        return info;
    }

    public SimpleCriterion getCriteria() {
        try {
            SimpleCriterion simpleCriterion = new SimpleCriterion(getKey(), getOperator(), getValue(), getInfo());
            if (field != null && field instanceof MultiLanguageField && field.getValue() != null) {
                simpleCriterion.setInputValue(field.getValue().toString());
            }
            return simpleCriterion;
        } catch (Exception e) {
            Log.error(e.getMessage());
            return null;
        }
    }

    public void setCriterion(SimpleCriterion criterion) {
        try {
            keyComboBox.setValue(list.findModel("value", criterion.getKey())); //$NON-NLS-1$
            TypeModel typeModel = adaptOperatorAndValue();
            operatorComboBox.setValue(operatorlist.findModel("value", criterion.getOperator())); //$NON-NLS-1$
            setField(criterion, typeModel);
        } catch (Exception e) {
            Log.error(e.getMessage(), e);
        }
    }

    private void setField(SimpleCriterion criterion, TypeModel typeModel) {
        if (field != null) {
            if (field instanceof DateField) {
                ((DateField) field).setValue(((DateField) field).getPropertyEditor().convertStringValue(criterion.getValue()));
            } else if (field instanceof SimpleComboBox) {
                ((SimpleComboBox) field).setValue(((SimpleComboBox) field).findModel(criterion.getValue()));
            } else if (field instanceof RadioGroup) {
                for (Field f : ((RadioGroup) field).getAll()) {
                    if (((Radio) f).getValueAttribute().equals(criterion.getValue())) {
                        ((Radio) f).setValue(true);
                        return;
                    }
                }
            } else if (field instanceof ForeignKeyField) {
                ForeignKeyBean fk = new ForeignKeyBean();
                fk.setId(criterion.getValue());
                fk.setDisplayInfo(criterion.getInfo());
                ((ForeignKeyField) field).setValue(fk);
            } else if (field instanceof SpinnerField) {
                String dataType = typeModel.getType().getBaseTypeName();
                String value = criterion.getValue();
                if (dataType.equals(DataTypeConstants.INT.getBaseTypeName())
                        || dataType.equals(DataTypeConstants.INTEGER.getBaseTypeName())) {
                    field.setValue(value != null ? Integer.parseInt(value.toString()) : null);
                } else if (dataType.equals(DataTypeConstants.SHORT.getBaseTypeName())) {
                    field.setValue(value != null ? Short.parseShort(value.toString()) : null);
                } else if (dataType.equals(DataTypeConstants.LONG.getBaseTypeName())) {
                    field.setValue(value != null ? Long.parseLong(value.toString()) : null);
                } else if (dataType.equals(DataTypeConstants.DOUBLE.getBaseTypeName())
                        || dataType.equals(DataTypeConstants.DECIMAL.getBaseTypeName())) {
                    field.setValue(value != null ? Double.parseDouble(value.toString()) : null);
                } else if (dataType.equals(DataTypeConstants.FLOAT.getBaseTypeName())) {
                    field.setValue(value != null ? Float.parseFloat(value.toString()) : null);
                }
            } else if (field instanceof CheckBox) {
                field.setValue(Boolean.valueOf(criterion.getValue()));
            } else if (field instanceof MultiLanguageField) {
                if (criterion.getInputValue() == null) {
                    criterion.setInputValue(((MultiLanguageField) field).getInputValue(criterion.getOperator(),
                            criterion.getValue()));
                }
                ((MultiLanguageField) field).setValue(criterion.getInputValue());
            } else {
                field.setValue(criterion.getValue());
            }
        }
    }

    @Override
    public void setCriteriaFK(ForeignKeyBean fk) {
        if (field != null) {
            if (field instanceof ForeignKeyField) {
                ((ForeignKeyField) field).setValue(fk);
            }
        }
    }

    public ViewBean getView() {
        return view;
    }

    public void setView(ViewBean view) {
        this.view = view;
    }

    public void addKeyComboBoxListener(final SimpleCriterionPanel<T> simpleCriterionPanel) {
        keyComboBox.addSelectionChangedListener(new SelectionChangedListener<BaseModel>() {

            @Override
            public void selectionChanged(SelectionChangedEvent<BaseModel> se) {
                adaptOperatorAndValue();

                if (simpleCriterionPanel != null) {
                    addOperatorAndFieldListener(simpleCriterionPanel, false);
                    simpleCriterionPanel.setKey(getKey());
                }
            }

        });
    }

    private void setKey(String value) {
        keyComboBox.setValue(list.findModel("value", value)); //$NON-NLS-1$
    }

    private void setOperator(String value) {
        operatorComboBox.setValue(operatorlist.findModel("value", value)); //$NON-NLS-1$
    }

    private void addOperatorAndFieldListener(final SimpleCriterionPanel<T> simpleCriterionPanel, boolean isAddOperatorListener) {
        if (isAddOperatorListener) {
            operatorComboBox.addSelectionChangedListener(new SelectionChangedListener<BaseModel>() {

                @Override
                public void selectionChanged(SelectionChangedEvent<BaseModel> se) {
                    if (simpleCriterionPanel != null) {
                        simpleCriterionPanel.setOperator(getOperator());
                    }
                }

            });
        }
        if (field != null) {
            field.addListener(Events.Change, new Listener<FieldEvent>() {

                @Override
                public void handleEvent(FieldEvent be) {
                    if (simpleCriterionPanel != null) {
                        TypeModel typeModel = itemsPredicates.get(getKey());
                        simpleCriterionPanel.setField(getCriteria(), typeModel);
                    }

                };
            });
        }
    }

    public SimpleCriterionPanel<?> clonePanel() {
        SimpleCriterionPanel<T> simpleCriterionPanel = new SimpleCriterionPanel<T>(searchBut, staging);
        if (view != null) {
            simpleCriterionPanel.updateFields(view);
            simpleCriterionPanel.adaptOperatorAndValue();
        }

        simpleCriterionPanel.addKeyComboBoxListener(this);
        simpleCriterionPanel.setCriterion(this.getCriteria());
        simpleCriterionPanel.addOperatorAndFieldListener(this, true);
        return simpleCriterionPanel;
    }
}