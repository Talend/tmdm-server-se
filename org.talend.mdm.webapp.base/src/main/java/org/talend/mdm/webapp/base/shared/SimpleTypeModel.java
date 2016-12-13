/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.shared;

import java.util.List;

import org.talend.mdm.webapp.base.client.model.DataType;

public class SimpleTypeModel extends TypeModel {

    private static final long serialVersionUID = 1L;

    private List<String> enumeration;

    private List<FacetModel> facets;

    public SimpleTypeModel() {
        super();
    }

    public SimpleTypeModel(String name, DataType typeName) {
        super(name, typeName);
    }

    public List<String> getEnumeration() {
        return enumeration;
    }

    public void setEnumeration(List<String> enumeration) {
        this.enumeration = enumeration;
    }

    public List<FacetModel> getFacets() {
        return facets;
    }

    public void setFacets(List<FacetModel> facets) {
        this.facets = facets;
    }

    @Override
    public boolean isSimpleType() {
        return true;
    }

    @Override
    public boolean hasEnumeration() {

        if (enumeration != null && enumeration.size() > 0)
            return true;

        return false;
    }
}
