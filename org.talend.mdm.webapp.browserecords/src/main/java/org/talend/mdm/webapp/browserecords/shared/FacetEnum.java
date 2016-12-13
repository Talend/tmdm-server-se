/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.shared;

import org.talend.mdm.webapp.base.client.widget.MultiLanguageField;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.extjs.gxt.ui.client.widget.form.NumberField;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.user.client.ui.Widget;

public enum FacetEnum {

    MIN_OCCURENCE("minOccurence"), //$NON-NLS-1$
    MAX_OCCURENCE("maxOccurence"), //$NON-NLS-1$
    MIN_INCLUSIVE("minInclusive"), //$NON-NLS-1$
    MAX_INCLUSIVE("maxInclusive"), //$NON-NLS-1$
    MIN_EXCLUSIVE("minExclusive"), //$NON-NLS-1$
    MAX_EXCLUSIVE("maxExclusive"), //$NON-NLS-1$

    LENGTH("length"), //$NON-NLS-1$
    MIN_LENGTH("minLength"), //$NON-NLS-1$
    MAX_LENGTH("maxLength"), //$NON-NLS-1$

    TOTAL_DIGITS("totalDigits"), //$NON-NLS-1$
    FRACTION_DIGITS("fractionDigits"), //$NON-NLS-1$

    ENUMERATION("enumeration"), //$NON-NLS-1$

    PATTERN("pattern"), //$NON-NLS-1$

    WHTE_SPACE("whiteSpace");//$NON-NLS-1$

    private String facetName;

    FacetEnum(String facetName) {
        this.facetName = facetName;
    }

    public String getFacetName() {
        return facetName;
    }

    public static void setFacetValue(String facet, Widget w, String value) {
        try {
            if (facet.equals(MIN_OCCURENCE.getFacetName())) {
                Field<?> field = (Field<?>) w;
                field.setData(MIN_OCCURENCE.getFacetName(), value);
            } else if (facet.equals(MAX_OCCURENCE.getFacetName())) {
                Field<?> field = (Field<?>) w;
                field.setData(MAX_OCCURENCE.getFacetName(), value);
            } else if (facet.equals(MIN_INCLUSIVE.getFacetName())) {
                NumberField field = (NumberField) w;
                // if ("integer".equals(field.getData("numberType"))){
                // field.setMinValue(Integer.parseInt(value));
                // } else {
                // field.setMinValue(Double.parseDouble(value));
                // }
                field.setData(MIN_INCLUSIVE.getFacetName(), value);
            } else if (facet.equals(MAX_INCLUSIVE.getFacetName())) {
                NumberField field = (NumberField) w;
                // if ("integer".equals(field.getData("numberType"))){
                // field.setMaxValue(Integer.parseInt(value));
                // } else {
                // field.setMaxValue(Double.parseDouble(value));
                // }
                field.setData(MAX_INCLUSIVE.getFacetName(), value);
            } else if (facet.equals(MIN_EXCLUSIVE.getFacetName())) {
                NumberField field = (NumberField) w;
                field.setData(MIN_EXCLUSIVE.getFacetName(), value);
            } else if (facet.equals(MAX_EXCLUSIVE.getFacetName())) {
                NumberField field = (NumberField) w;
                field.setData(MAX_EXCLUSIVE.getFacetName(), value);
            } else if (facet.equals(LENGTH.getFacetName())) {
                TextField<String> field = (TextField<String>) w;
                field.setData(LENGTH.getFacetName(), value);
            } else if (facet.equals(MIN_LENGTH.getFacetName())) {
                TextField<String> field = (TextField<String>) w;
                field.setData(MIN_LENGTH.getFacetName(), value);
            } else if (facet.equals(MAX_LENGTH.getFacetName())) {
                TextField<String> field = (TextField<String>) w;
                field.setData(MAX_LENGTH.getFacetName(), value);
            } else if (facet.equals(TOTAL_DIGITS.getFacetName())) {
                NumberField field = (NumberField) w;
                field.setData(TOTAL_DIGITS.getFacetName(), value);
            } else if (facet.equals(FRACTION_DIGITS.getFacetName())) {
                NumberField field = (NumberField) w;
                field.setData(FRACTION_DIGITS.getFacetName(), value);
            } else if (facet.equals(PATTERN.getFacetName())) {
                TextField<String> field = (TextField<String>) w;
                // MultiLanguageField did not need to check pattern(Field will setup value by [en:***][fr:***] format)
                // the pattern will be used on server-side to check
                if (!(field instanceof MultiLanguageField)) {
                    field.setData(PATTERN.getFacetName(), value);
                }
            } else if (facet.equals(WHTE_SPACE.getFacetName())) {
                TextField<String> field = (TextField<String>) w;
                field.setData(WHTE_SPACE.getFacetName(), value);
            }
        } catch (ClassCastException e) {
            Log.error(e.getMessage(), e);
        }
    }
}
