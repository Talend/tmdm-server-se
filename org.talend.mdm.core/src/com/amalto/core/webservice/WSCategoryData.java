/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.webservice;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="WSCategoryData")
public class WSCategoryData {
    protected java.lang.String categorySchema;
    
    public WSCategoryData() {
    }
    
    public WSCategoryData(java.lang.String categorySchema) {
        this.categorySchema = categorySchema;
    }
    
    public java.lang.String getCategorySchema() {
        return categorySchema;
    }
    
    public void setCategorySchema(java.lang.String categorySchema) {
        this.categorySchema = categorySchema;
    }
}
