/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.webapp.core.bean;

public class ComboItemBean implements Comparable<ComboItemBean> {

    private String value;

    private String text;

    public ComboItemBean(String value, String text) {
        super();
        this.text = text;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getText() {
        return text;
    }

    public int compareTo(ComboItemBean bean) {
        if (this == bean)
            return 0;

        int result = this.getValue().compareTo(bean.getValue());
        if (result != 0) {
            return result;
        }

        return this.getText().compareTo(bean.getText());
    }

    public boolean equals(ComboItemBean bean) {
        if (this == bean)
            return true;

        return this.getValue().equals(bean.getValue()) && this.getText().equals(bean.getText());
    }
}
