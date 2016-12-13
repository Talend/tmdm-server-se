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

import org.talend.mdm.webapp.base.client.model.DataType;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class DataTypeCustomized implements DataType {

    private String typeName = null;

    private String baseTypeName = null;

    /**
     * DOC HSHU DataTypeCustomized constructor comment.
     */

    public DataTypeCustomized() {
        super();
    }

    public DataTypeCustomized(String typeName,String baseTypeName) {
        this.typeName = typeName;
        this.baseTypeName = baseTypeName;
    }

    public String getBaseTypeName() {
        return baseTypeName;
    }

    public void setBaseTypeName(String baseTypeName) {
        this.baseTypeName = baseTypeName;
    }

    /*
     * (non-Jsdoc)
     * 
     * @see org.talend.mdm.webapp.itemsbrowser2.client.model.DataType#getDefaultValue()
     */
    public Object getDefaultValue() {
        return null;
    }

    /*
     * (non-Jsdoc)
     * 
     * @see org.talend.mdm.webapp.itemsbrowser2.client.model.DataType#getTypeName()
     */
    public String getTypeName() {
        return this.typeName;
    }

}
