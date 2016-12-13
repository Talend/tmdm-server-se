/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.typefield;

import java.math.BigDecimal;
import java.util.List;

import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.shared.FacetModel;
import org.talend.mdm.webapp.base.shared.SimpleTypeModel;
import org.talend.mdm.webapp.browserecords.client.model.OperatorConstants;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.FormatNumberField;
import org.talend.mdm.webapp.browserecords.client.widget.inputfield.validator.NumberFieldValidator;
import org.talend.mdm.webapp.browserecords.shared.FacetEnum;

import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.TextField;


public class NumberTypeFieldFactory extends TypeFieldFactory {


    public NumberTypeFieldFactory() {

    }

    public NumberTypeFieldFactory(TypeFieldSource source, TypeFieldCreateContext context) {
        super(source, context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.base.client.widget.typefield.TypeFieldFactory#createField()
     */
    @Override
    public Field<?> createField() {

        FormatNumberField numberField = new FormatNumberField();

        if (!isEmpty(displayformatPattern))
            numberField.setFormatPattern(displayformatPattern);

        numberField.setValidator(NumberFieldValidator.getInstance());

        // exceptions
        if (DataTypeConstants.FLOAT.getBaseTypeName().equals(baseType)) {
            numberField.setData("numberType", DataTypeConstants.FLOAT.getBaseTypeName());//$NON-NLS-1$ 
            numberField.setPropertyEditorType(Float.class);
        } else if (DataTypeConstants.DOUBLE.getBaseTypeName().equals(baseType)) {
            numberField.setData("numberType", DataTypeConstants.DOUBLE.getBaseTypeName());//$NON-NLS-1$ 
            numberField.setPropertyEditorType(Double.class);
        } else if (DataTypeConstants.DECIMAL.getBaseTypeName().equals(baseType)) {
            numberField.setData("numberType", DataTypeConstants.DECIMAL.getBaseTypeName());//$NON-NLS-1$ 
            numberField.setPropertyEditorType(BigDecimal.class);

            List<FacetModel> facets = ((SimpleTypeModel) context.getDataType()).getFacets();
            if (facets != null) {
                for (FacetModel facet : facets) {
                    if (facet.getName().equals(FacetEnum.FRACTION_DIGITS.getFacetName())) {
                        numberField.setData(FacetEnum.FRACTION_DIGITS.getFacetName(), facet.getValue());
                        continue;
                    } else if (facet.getName().equals(FacetEnum.TOTAL_DIGITS.getFacetName())) {
                        numberField.setData(FacetEnum.TOTAL_DIGITS.getFacetName(), facet.getValue());
                        continue;
                    }
                }
            }
        } else {
            numberField.setData("numberType", DataTypeConstants.INTEGER.getBaseTypeName());//$NON-NLS-1$ 
            numberField.setPropertyEditorType(Integer.class);
        }

        if (context.isWithValue()) {

            Number toSetValue = null;
            if (hasValue()) {
                if (DataTypeConstants.FLOAT.getBaseTypeName().equals(baseType)) {
                    toSetValue = Float.parseFloat(getValue().toString());
                } else if (DataTypeConstants.DECIMAL.getBaseTypeName().equals(baseType)) {
                    toSetValue = numberField.getDecimalValue(getValue().toString()) ;
                } else if (DataTypeConstants.DOUBLE.getBaseTypeName().equals(baseType)) {
                    toSetValue = Double.parseDouble(getValue().toString());
                } else {
                    toSetValue = Long.parseLong(getValue().toString());
                }
            }

            numberField.setValue(toSetValue);
        }

        return numberField;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldFactory#createSearchField()
     */
    @Override
    public Field<?> createSearchField() {
        Field<?> field = null;
        if (baseType.equals(DataTypeConstants.INT.getBaseTypeName())
                || baseType.equals(DataTypeConstants.INTEGER.getBaseTypeName())
                || baseType.equals(DataTypeConstants.LONG.getBaseTypeName())
                || baseType.equals(DataTypeConstants.SHORT.getBaseTypeName())) {

            TextField<String> textField = new TextField<String>();
            textField.setWidth(80);
            textField.setValue("*"); //$NON-NLS-1$
            field = textField;
            source.setOperatorMap(OperatorConstants.numOperators);
        } else if (baseType.equals(DataTypeConstants.DOUBLE.getBaseTypeName())
                || baseType.equals(DataTypeConstants.DECIMAL.getBaseTypeName())
                || baseType.equals(DataTypeConstants.FLOAT.getBaseTypeName())) {
            TextField<String> textField = new TextField<String>();
            textField.setWidth(80);
            textField.setValue("*"); //$NON-NLS-1$
            field = textField;
            source.setOperatorMap(OperatorConstants.numOperators);
        } else {
            field = genTextSearchField();
        }
        return field;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.talend.mdm.webapp.browserecords.client.widget.typefield.TypeFieldFactory#updateStyle()
     */
    @Override
    public void updateStyle(Field<?> field) {
        updateBuiltInTypeFiledsStyle(field);
    }


}
