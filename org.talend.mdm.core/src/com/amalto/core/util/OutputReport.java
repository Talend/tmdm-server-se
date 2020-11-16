/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.util;

/**
 * beforeSaving process output report
 */
public class OutputReport {

    /**
     * output message that stored in variable $output_report
     */
    private String message;

    /**
     * item that stored in variable $output_item
     */
    private String item;

    private boolean withAdminPermissions;

    public OutputReport(String message, String item, boolean withAdminPermissions) {
        this.message = message;
        this.item = item;
        this.withAdminPermissions = withAdminPermissions;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public boolean isWithAdminPermissions() {
        return withAdminPermissions;
    }

    public void setWithAdminPermissions(boolean withAdminPermissions) {
        this.withAdminPermissions = withAdminPermissions;
    }

}
