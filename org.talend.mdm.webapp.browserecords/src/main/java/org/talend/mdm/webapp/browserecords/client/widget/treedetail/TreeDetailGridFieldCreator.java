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

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.ForeignKeyBean;
import org.talend.mdm.webapp.base.client.widget.MultiLanguageField;
import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.FacetModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.BrowseRecords;
import org.talend.mdm.webapp.browserecords.client.BrowseRecordsEvents;
import org.talend.mdm.webapp.browserecords.client.model.ComboBoxModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;
import org.talend.mdm.webapp.browserecords.client.mvc.BrowseRecordsView;
import org.talend.mdm.webapp.browserecords.client.util.CommonUtil;
import org.talend.mdm.webapp.browserecords.client.util.MultiOccurrenceManager;
import org.talend.mdm.webapp.browserecords.client.widget.ItemDetailToolBar;
import org.talend.mdm.webapp.browserecords.client.widget.ItemsDetailPanel;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.FKPropertyEditor;
import org.talend.mdm.webapp.browserecords.client.widget.ForeignKey.ForeignKeySelector;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.BooleanField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.ComboBoxField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatDateField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatNumberField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatTextField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.PictureField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.SimpleComboBoxField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.UrlField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.validator.TextFieldValidator;
import org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldCreateContext;
import org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldCreator;
import org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldSource;
import org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldStyle;
import org.talend.mdm.webapp.browserecords.shared.FacetEnum;

import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.form.ComboBox.TriggerAction;
import com.extjs.gxt.ui.client.widget.form.DateField;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.SimpleComboBox;
import com.extjs.gxt.ui.client.widget.form.SimpleComboValue;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class TreeDetailGridFieldCreator {

    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
    public static Field<?> createField(ItemNodeModel node, final TypeModel dataType, String language,
            Map<String, Field<?>> fieldMap, String operation, final ItemsDetailPanel itemsDetailPanel) {
        // Field

        final Serializable value = node.getObjectValue();
        Field<?> field;
        boolean hasValue = value != null && !"".equals(value); //$NON-NLS-1$
        if (dataType.getForeignkey() != null) {
            ForeignKeySelector fkField = new ForeignKeySelector(dataType, itemsDetailPanel, node);
            if (value instanceof ForeignKeyBean) {
                ForeignKeyBean fkBean = (ForeignKeyBean) value;
                if (fkBean != null) {
                    // fkBean.setDisplayInfo(fkBean.getId());
                    fkField.setValue(fkBean);
                    fkField.setPropertyEditor(new FKPropertyEditor());
                }
            }
            fkField.setEnabled(!dataType.isReadOnly());
            fkField.setReadOnly(dataType.isReadOnly());
            field = fkField;
        } else if (dataType.hasEnumeration()) {
            SimpleComboBoxField comboBox = new SimpleComboBoxField();
            comboBox.setFireChangeEventOnSetValue(true);
            comboBox.setDisplayField("text"); //$NON-NLS-1$
            if (dataType.getMinOccurs() == 1 && dataType.getMaxOccurs() == 1) {
                if (BrowseRecords.getSession().getAppHeader().isAutoValidate()) {
                    comboBox.setAllowBlank(false);
                }
            }
            comboBox.setEditable(false);
            comboBox.setForceSelection(true);
            comboBox.setTriggerAction(TriggerAction.ALL);
            comboBox.setTemplate(getTemplate());
            setEnumerationValues(dataType, comboBox, node.isMandatory());
            if (hasValue && value != null && !value.toString().isEmpty()) {
                comboBox.setSimpleValue(value.toString());
            }
            field = comboBox;
        } else if (dataType instanceof ComplexTypeModel) {
            final ComboBoxField<ComboBoxModel> comboxField = new ComboBoxField<ComboBoxModel>();
            comboxField.setDisplayField("text"); //$NON-NLS-1$
            comboxField.setValueField("value"); //$NON-NLS-1$
            comboxField.setTypeAhead(true);
            comboxField.setTriggerAction(TriggerAction.ALL);
            comboxField.setTemplate(getTemplate());

            // final ComplexTypeModel complexTypeModel = (ComplexTypeModel) dataType;
            List<ComplexTypeModel> reusableTypes = ((ComplexTypeModel) dataType).getReusableComplexTypes();
            Collections.sort(reusableTypes, new Comparator<ComplexTypeModel>() {

                @Override
                public int compare(ComplexTypeModel o1, ComplexTypeModel o2) {
                    return o1.getOrderValue() - o2.getOrderValue();
                }
            });
            ListStore<ComboBoxModel> comboxStore = new ListStore<ComboBoxModel>();
            comboxField.setStore(comboxStore);
            for (int i = 0; i < reusableTypes.size(); i++) {
                if (reusableTypes.get(i).isAbstract()) {
                    continue;
                }
                ComboBoxModel cbm = new ComboBoxModel(reusableTypes.get(i).getLabel(language), reusableTypes.get(i).getName());
                cbm.set("reusableType", reusableTypes.get(i)); //$NON-NLS-1$
                comboxStore.add(cbm);
            }
            if (comboxStore.getCount() > 0) {
                comboxField.setValue(comboxStore.getAt(0));
            }

            if (node.getRealType() != null && node.getRealType().trim().length() > 0) {
                comboxField.setValue(comboxStore.findModel("value", node.getRealType())); //$NON-NLS-1$
            } else if (hasValue) {
                comboxField.setValue(comboxStore.findModel(value.toString(), value));
            }

            comboxField.addSelectionChangedListener(new SelectionChangedListener<ComboBoxModel>() {

                @Override
                public void selectionChanged(SelectionChangedEvent<ComboBoxModel> se) {
                    ComplexTypeModel reusableType;
                    if (se.getSelectedItem() == null) {
                        reusableType = comboxField.getStore().getAt(0).get("reusableType"); //$NON-NLS-1$
                    } else {
                        reusableType = se.getSelectedItem().get("reusableType"); //$NON-NLS-1$    
                    }
                    AppEvent app = new AppEvent(BrowseRecordsEvents.UpdatePolymorphism);
                    app.setData(reusableType);
                    app.setData(BrowseRecordsView.ITEMS_DETAIL_PANEL, itemsDetailPanel);
                    Dispatcher.forwardEvent(app);
                }

            });

            field = comboxField;
        } else {
            TypeFieldCreateContext context = new TypeFieldCreateContext(dataType);
            context.setLanguage(language);
            context.setMandatory(node.isMandatory());
            context.setBulkUpdate(node.isMassUpdate());
            TypeFieldCreator typeFieldCreator = new TypeFieldCreator(new TypeFieldSource(TypeFieldSource.FORM_INPUT), context);
            Map<String, TypeFieldStyle> sytles = new HashMap<String, TypeFieldStyle>();
            sytles.put(TypeFieldStyle.ATTRI_WIDTH, new TypeFieldStyle(TypeFieldStyle.ATTRI_WIDTH,
                    "400", TypeFieldStyle.SCOPE_BUILTIN_TYPEFIELD)); //$NON-NLS-1$
            field = typeFieldCreator.createFieldWithValueAndUpdateStyle(node, sytles);
        }

        field.setFieldLabel(dataType.getLabel(language));
        field.setName(dataType.getXpath());
        if (dataType.getType().equals(DataTypeConstants.UUID) || dataType.getType().equals(DataTypeConstants.AUTO_INCREMENT)) {
            field.setReadOnly(true);
            field.setEnabled(false);
        } else {
            if (node.isKey() && ItemDetailToolBar.DUPLICATE_OPERATION.equals(operation)) {
                field.setEnabled(true);
                field.setReadOnly(false);
            } else if (node.isKey() && ItemDetailToolBar.CREATE_OPERATION.equals(operation)) {
                field.setEnabled(true);
                field.setReadOnly(false);
            } else if (node.isKey() && hasValue) {
                field.setEnabled(false);
                field.setReadOnly(true);
            } else {
                boolean readOnly = dataType.isReadOnly();
                field.setReadOnly(readOnly);
                field.setEnabled(!readOnly);
            }
        }

        // facet set
        if (field instanceof TextField<?> && !(dataType instanceof ComplexTypeModel)) {
            buildFacets(dataType, field);

            String errorMsg = dataType.getFacetErrorMsgs().get(language);
            field.setData("facetErrorMsgs", errorMsg);//$NON-NLS-1$        
            FacetEnum.setFacetValue("maxOccurence", field, String.valueOf(dataType.getMaxOccurs())); //$NON-NLS-1$

            if (((TextField<?>) field).getValidator() == null) {
                ((TextField<?>) field).setValidator(TextFieldValidator.getInstance());
            }

            if (errorMsg != null && !errorMsg.equals("")) { //$NON-NLS-1$
                ((TextField<?>) field).getMessages().setBlankText(errorMsg);
            }

        }
        fieldMap.put(node.getId().toString(), field);
        updateMandatory(field, node, fieldMap);
        addFieldListener(dataType, field, node, fieldMap);
        return field;
    }

    public static Field<?> createField(ItemNodeModel node, final TypeModel dataType, String language,
            Map<String, Field<?>> fieldMap, final ItemsDetailPanel itemsDetailPanel) {
        return createField(node, dataType, language, fieldMap, null, itemsDetailPanel);
    }

    public static void deleteField(ItemNodeModel node, Map<String, Field<?>> fieldMap) {

        Field<?> updateField = fieldMap.get(node.getId().toString());
        node.setObjectValue(null);
        updateMandatory(updateField, node, fieldMap);
        fieldMap.remove(node.getId().toString());
    }

    private static void addFieldListener(final TypeModel typeModel, final Field<?> field, final ItemNodeModel node,
            final Map<String, Field<?>> fieldMap) {
        field.setFireChangeEventOnSetValue(true);
        field.addListener(Events.Change, new Listener<FieldEvent>() {

            @Override
            @SuppressWarnings("rawtypes")
            public void handleEvent(FieldEvent fe) {
                Serializable value;
                if (fe.getField().isValid()) {
                    if (fe.getField() instanceof ComboBoxField) {
                        value = ((ComboBoxModel) fe.getValue()).getValue();
                    } else if (fe.getField() instanceof CheckBox) {
                        value = fe.getValue().toString();
                    } else if (fe.getField() instanceof MultiLanguageField) {
                        value = ((MultiLanguageField) fe.getField()).getMultiLanguageStringValue();
                    } else {
                        if (fe.getField() instanceof ComboBox) {
                            SimpleComboValue simpleComboValue = (SimpleComboValue) fe.getValue();
                            value = (Serializable) (simpleComboValue == null ? null : simpleComboValue.getValue());
                        } else {
                            value = (Serializable) fe.getValue();
                            // TMDM-8470 trim pk textfield value
                            if (node.isKey() && field instanceof TextField<?> && value != null) {
                                value = value.toString().trim();
                            }
                        }
                    }
                    if (node.getObjectValue() != null) {
                        if (!node.getObjectValue().toString().equals(value)) {
                            node.setObjectValue(value);
                            node.setChangeValue(true);
                        }
                    } else {
                        if (value != null) {
                            node.setObjectValue(value);
                            node.setChangeValue(true);
                        }
                    }
                }
                validate(fe.getField(), node);
                updateMandatory(field, node, fieldMap);

                TreeDetail treeDetail = getCurrentTreeDetail(field);
                if (treeDetail != null) {
                    MultiOccurrenceManager multiManager = treeDetail.getMultiManager();
                    if (multiManager != null) {
                        multiManager.warningBrothers(node);
                    }
                }
            }
        });

        field.addListener(Events.Attach, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent fe) {
                setErrorIcon(field);
                validate(field, node);
                node.setRendered(true);
            }
        });

        field.addListener(Events.Blur, new Listener<FieldEvent>() {

            @Override
            public void handleEvent(FieldEvent fe) {
                // TMDM-3353 only when node is valid, call setObjectValue(); otherwise objectValue is changed to
                // original value
                if (fe.getField().isValid()) {
                    if (fe.getField() instanceof FormatTextField) {
                        if (BrowseRecords.getSession().getAppHeader().isAutoValidate()) {
                            node.setObjectValue(((FormatTextField) fe.getField()).getOjbectValue());
                            // TMDM-8470 trim pk textfield value
                            if (node.isKey() && node.getObjectValue() != null) {
                                node.setObjectValue(node.getObjectValue().toString().trim());
                            }
                        }
                    } else if (fe.getField() instanceof FormatNumberField) {
                        if (BrowseRecords.getSession().getAppHeader().isAutoValidate()) {
                            node.setObjectValue(((FormatNumberField) fe.getField()).getOjbectValue());
                        }
                    } else if (fe.getField() instanceof FormatDateField) {
                        if (node.getObjectValue() != null
                                && !node.getObjectValue().equals(((FormatDateField) fe.getField()).getRawValue())) {
                            node.setObjectValue(((FormatDateField) fe.getField()).getOjbectValue());
                            node.setChangeValue(true);
                        }
                        validate(fe.getField(), node);
                    }
                } else {
                    if (fe.getField() instanceof FormatDateField) {
                        if(node.getObjectValue() != null && !node.getObjectValue().equals(((FormatDateField) fe.getField()).getRawValue())){
                            node.setObjectValue(((FormatDateField) fe.getField()).getOjbectValue());
                            node.setChangeValue(true);
                        }
                        validate(fe.getField(), node);
                    }
                }
            }
        });
    }

    private static void setErrorIcon(Field<?> field) {
        WidgetComponent errorIcon = _getErrorIcon(field);
        if (errorIcon != null) {
            errorIcon.removeFromParent();
            Element errEl = errorIcon.getElement();
            if (errEl != null) {
                errEl.removeFromParent();
            }
        }

        errorIcon = new WidgetComponent(field.getImages().getInvalid().createImage()) {

            @Override
            public void setElement(Element elem) {
                _setEl(new El(elem) {

                    @Override
                    public El alignTo(Element align, String pos, int[] offsets) {
                        return this;
                    }
                });
                super.setElement(elem);
                if (!rendered) {
                    setElementRender = true;
                    render(null);
                }
            }

            private native void _setEl(El elem)/*-{
		this.@com.extjs.gxt.ui.client.widget.Component::el = elem;
    }-*/;
        };
        errorIcon.setStyleAttribute("display", "block"); //$NON-NLS-1$ //$NON-NLS-2$
        errorIcon.setStyleAttribute("float", "right"); //$NON-NLS-1$ //$NON-NLS-2$
        errorIcon.setStyleAttribute("marginTop", "-18px");//$NON-NLS-1$ //$NON-NLS-2$
        errorIcon.render(field.el().getParent().dom);
        errorIcon.setHideMode(HideMode.VISIBILITY);
        errorIcon.hide();

        _setErrorIcon(field, errorIcon);
    }

    private static native void _setErrorIcon(Field<?> field, WidgetComponent errorIcon)/*-{
		field.@com.extjs.gxt.ui.client.widget.form.Field::errorIcon = errorIcon;
    }-*/;

    private static native WidgetComponent _getErrorIcon(Field<?> field)/*-{
		return field.@com.extjs.gxt.ui.client.widget.form.Field::errorIcon;
    }-*/;

    public static native String getTemplate() /*-{
		return [
				'<tpl for=".">',
				'<tpl if="text == \'\'">',
				'<div role=\"listitem\" class="x-combo-list-item" title=""></br></div>',
				'</tpl>',
				'<tpl if="text != \'\'">',
				'<div role=\"listitem\" class="x-combo-list-item" title="{text}">{text}</div>',
				'</tpl>', '</tpl>' ].join("");
    }-*/;

    private static void buildFacets(TypeModel typeModel, Widget w) {
        if (typeModel instanceof SimpleTypeModel) {
            List<FacetModel> facets = ((SimpleTypeModel) typeModel).getFacets();
            if (facets != null) {
                for (FacetModel facet : facets) {
                    FacetEnum.setFacetValue(facet.getName(), w, facet.getValue());
                }
            }
        }
    }

    private static void setEnumerationValues(TypeModel typeModel, Widget w, boolean mandatory) {
        List<String> enumeration = ((SimpleTypeModel) typeModel).getEnumeration();
        if (enumeration != null && enumeration.size() > 0) {
            @SuppressWarnings("unchecked")
            SimpleComboBox<String> field = (SimpleComboBox<String>) w;
            if (!mandatory) {
                field.getStore().add(new SimpleComboValue<String>() {

                    {
                        this.set("text", ""); //$NON-NLS-1$ //$NON-NLS-2$
                        this.setValue(""); //$NON-NLS-1$
                    }
                });
            }
            for (final String value : enumeration) {
                field.getStore().add(new SimpleComboValue<String>() {

                    {
                        this.set("text", value); //$NON-NLS-1$
                        this.setValue(value);
                    }
                });
            }
        }
    }

    public static void updateMandatory(Field<?> field, ItemNodeModel node, Map<String, Field<?>> fieldMap) {
        boolean flag = false;
        boolean noBulkUpdateFlag = node.isMassUpdate() && !node.isEdited();
        ItemNodeModel parent = (ItemNodeModel) node.getParent();
        if (parent != null && parent.getParent() != null && !parent.isMandatory()) {
            List<ModelData> childs = parent.getChildren();
            for (int i = 0; i < childs.size(); i++) {
                ItemNodeModel child = (ItemNodeModel) childs.get(i);
                if (CommonUtil.hasChildrenValue(child)) {
                    flag = true;
                    break;
                }
            }

            autoFillValue4MandatoryBooleanField(flag, childs, fieldMap);
            for (int i = 0; i < childs.size(); i++) {
                ItemNodeModel mandatoryNode = (ItemNodeModel) childs.get(i);
                Field<?> updateField = fieldMap.get(mandatoryNode.getId().toString());
                if (updateField != null && mandatoryNode.isMandatory()) {
                    boolean noBulkUpdateChildFlag = mandatoryNode.isMassUpdate() && !mandatoryNode.isEdited();
                    boolean isFieldMandatory = flag ? mandatoryNode.isMandatory() : !mandatoryNode.isMandatory();
                    mandatoryNode.setFieldMandatory(isFieldMandatory);
                    setMandatory(updateField, noBulkUpdateChildFlag ? false : isFieldMandatory);
                    mandatoryNode.setValid(updateField.validate());
                }
            }
        } else {
            node.setFieldMandatory(node.isMandatory());
            setMandatory(field, noBulkUpdateFlag ? false : node.isMandatory());
        }
    }

    public static void autoFillValue4MandatoryBooleanField(boolean enable, List<ModelData> toUpdateNodes,
            Map<String, Field<?>> fieldMap) {

        if (toUpdateNodes == null) {
            return;
        }

        if (enable) {

            for (int i = 0; i < toUpdateNodes.size(); i++) {
                ItemNodeModel toUpdateNode = (ItemNodeModel) toUpdateNodes.get(i);
                Field<?> toUpdateField = fieldMap.get(toUpdateNode.getId().toString());

                if ((toUpdateField instanceof BooleanField || toUpdateField instanceof CheckBox) && toUpdateNode.isMandatory()
                        && (toUpdateNode.getObjectValue() == null || "".equals(toUpdateNode.getObjectValue()))) {

                    toUpdateNode.setObjectValue((Serializable) DataTypeConstants.BOOLEAN.getDefaultValue());

                }
            }// end for

        }

    }

    private static TreeDetail getCurrentTreeDetail(Widget child) {
        if (child == null) {
            return null;
        }
        Widget current = child;
        while (current != null) {
            if (current instanceof TreeDetail) {
                return (TreeDetail) current;
            }
            current = current.getParent();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public static void setMandatory(Field<?> field, boolean mandatory) {
        if (!BrowseRecords.getSession().getAppHeader().isAutoValidate()) {
            return;
        }
        if (field instanceof NumberField) {
            ((NumberField) field).setAllowBlank(!mandatory);
        } else if (field instanceof BooleanField) {
            ((BooleanField) field).setAllowBlank(!mandatory);
        } else if (field instanceof DateField) {
            ((DateField) field).setAllowBlank(!mandatory);
        } else if (field instanceof PictureField) {
            ((PictureField) field).setMandatory(mandatory);
            ((PictureField) field).setAllowBlank(!mandatory);
        } else if (field instanceof TextField) {
            ((TextField) field).setAllowBlank(!mandatory);
        } else if (field instanceof UrlField) {
            ((UrlField) field).setAllowBlank(!mandatory);
        }
    }

    public static void validate(Field<?> field, ItemNodeModel node) {
        boolean isEmpty = field.getRawValue().isEmpty();
        if (field instanceof NumberField) {
            node.setBlankValid(((NumberField) field).getAllowBlank() || !isEmpty);
        } else if (field instanceof BooleanField) {
            node.setBlankValid(((BooleanField) field).getAllowBlank() || !isEmpty);
        } else if (field instanceof DateField) {
            node.setBlankValid(((DateField) field).getAllowBlank() || !isEmpty);
        } else if (field instanceof TextField) {
            node.setBlankValid(((TextField) field).getAllowBlank() || !isEmpty);
        } else if (field instanceof UrlField) {
            node.setBlankValid(((UrlField) field).isAllowBlank() || !isEmpty);
        }
        node.setValid(field.isValid());
    }
}
