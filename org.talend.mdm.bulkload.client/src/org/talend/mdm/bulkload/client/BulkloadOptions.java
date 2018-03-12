/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package org.talend.mdm.bulkload.client;

public class BulkloadOptions {

    boolean smartpk;

    boolean validate;

    int arraySize = 500; // the performance become better when the arraySize is bigger

    boolean insertOnly = false;

    public BulkloadOptions(boolean smartpk, boolean validate, int arraySize) {
        this.smartpk = smartpk;
        this.validate = validate;
        this.arraySize = arraySize;
    }

    public BulkloadOptions(boolean smartpk, boolean validate, int arraySize, boolean insertOnly) {
        this.smartpk = smartpk;
        this.validate = validate;
        this.arraySize = arraySize;
        this.insertOnly = insertOnly;
    }

    public BulkloadOptions(int arraySize) {
        this.arraySize = arraySize;
    }

    public BulkloadOptions() {
    }

    public boolean isSmartpk() {
        return smartpk;
    }

    public void setSmartpk(boolean smartpk) {
        this.smartpk = smartpk;
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    public int getArraySize() {
        return arraySize;
    }

    public void setArraySize(int arraySize) {
        this.arraySize = arraySize;
    }

    public boolean isInsertOnly() {
        return insertOnly;
    }

    public void setInsertOnly(boolean insertOnly) {
        this.insertOnly = insertOnly;
    }
}
