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

import java.util.ArrayList;
import java.util.List;

import org.talend.mdm.webapp.base.client.model.DataType;
import org.talend.mdm.webapp.base.shared.TypeModel;

/**
 * DOC chliu class global comment. Detailled comment
 */
public class ComplexTypeModel extends TypeModel {

    private int orderValue;

    private List<TypeModel> subTypes = new ArrayList<TypeModel>();

    private List<ComplexTypeModel> reusableTypes = new ArrayList<ComplexTypeModel>();

    /**
     * DOC HSHU ComplexTypeModel constructor comment.
     */
    public ComplexTypeModel() {
        super();
    }

    public ComplexTypeModel(String name, DataType dataType) {
        super(name, dataType);
    }

    public int getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(int orderValue) {
        this.orderValue = orderValue;
    }

    public List<TypeModel> getSubTypes() {
        return this.subTypes;
    }

    /**
     * DOC HSHU Comment method "addSubType".
     */
    public void addSubType(TypeModel subType) {
        if (this.subTypes != null) {
            this.subTypes.add(subType);
            subType.setParentTypeModel(this);
        }
    }

    public void setReadOnly(boolean readOnly) {
        super.setReadOnly(readOnly);
        for (ComplexTypeModel realType : reusableTypes) {
            realType.setReadOnly(this.isReadOnly());
        }
    }

    public void setParentTypeModel(TypeModel parentTypeModel) {
        super.setParentTypeModel(parentTypeModel);
        for (ComplexTypeModel realType : reusableTypes) {
            realType.setParentTypeModel(parentTypeModel);
        }
    }

    public void addComplexReusableTypes(ComplexTypeModel reusableType) {
        reusableTypes.add(reusableType);
        reusableType.setParentTypeModel(this.getParentTypeModel());
        reusableType.setReadOnly(this.isReadOnly());
    }

    public List<ComplexTypeModel> getReusableComplexTypes() {
        return reusableTypes;
    }

    public ComplexTypeModel getRealType(String typeName) {
        for (ComplexTypeModel realType : reusableTypes) {
            if (realType.getName().equals(typeName)) {
                return realType;
            }
        }
        return null;
    }

    public boolean isSimpleType() {
        return false;
    }

    public boolean hasEnumeration() {
        return false;
    }
}
