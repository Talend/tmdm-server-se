/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.model;

public class SimpleCriterion implements Criteria {

    private static final long serialVersionUID = 1L;

    public static final String Simple_Criterion = "SimpleCriterion"; //$NON-NLS-1$

    private String key;

    private String operator;

    private String value;

    private String info;

    private String inputValue;

    public SimpleCriterion() {
        super();
    }

    public SimpleCriterion(String key, String operator, String value) {
        super();
        this.key = key;
        this.operator = operator;
        this.value = value;
    }

    public SimpleCriterion(String key, String operator, String value, String info) {
        this(key, operator, value);
        this.info = info;
    }

    @Override
    public String toString() {
        return (key == null ? "" : key) + " " + (operator == null ? "" : operator) + " " + (value == null ? "" : value); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    public String toAppearanceString() {
        return (key == null ? "" : key) + " " + (operator == null ? "" : operator) + " " + (info == null ? value : info); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    public String toXmlString() {
        StringBuilder simpleCriterionBuilder = new StringBuilder();
        simpleCriterionBuilder.append("<").append(SimpleCriterion.Simple_Criterion).append(">"); //$NON-NLS-1$ //$NON-NLS-2$
        simpleCriterionBuilder.append("<key>").append(key).append("</key>"); //$NON-NLS-1$ //$NON-NLS-2$
        simpleCriterionBuilder.append("<operator>").append(operator).append("</operator>"); //$NON-NLS-1$ //$NON-NLS-2$
        simpleCriterionBuilder.append("<value>").append(value).append("</value>"); //$NON-NLS-1$ //$NON-NLS-2$
        simpleCriterionBuilder.append("<info>").append(info).append("</info>"); //$NON-NLS-1$ //$NON-NLS-2$
        simpleCriterionBuilder.append("</").append(SimpleCriterion.Simple_Criterion).append(">"); //$NON-NLS-1$//$NON-NLS-2$
        return simpleCriterionBuilder.toString();
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInputValue() {
        return inputValue;
    }

    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }

    public boolean equal(SimpleCriterion criteria) {
        if (criteria == null) {
            return false;
        }

        if (key.equals(criteria.getKey()) && operator.equals(criteria.getOperator()) && value.equals(criteria.getValue())) {
            return true;
        }

        return false;
    }
}
