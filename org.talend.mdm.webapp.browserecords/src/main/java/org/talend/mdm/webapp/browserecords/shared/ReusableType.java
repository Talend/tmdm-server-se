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

import java.io.Serializable;
import java.util.List;

import org.talend.mdm.webapp.base.shared.ComplexTypeModel;
import org.talend.mdm.webapp.base.shared.TypeModel;
import org.talend.mdm.webapp.browserecords.client.model.ItemNodeModel;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ReusableType implements Serializable, IsSerializable {

    private static final long serialVersionUID = 1426489981587916045L;

    public static List<TypeModel> getDefaultReusableTypeChildren(ComplexTypeModel model, ItemNodeModel node) {
        List<TypeModel> children = model.getSubTypes();
        List<ComplexTypeModel> reusableTypeList = model.getReusableComplexTypes();
        if (reusableTypeList != null && reusableTypeList.size() > 0) {
            for (ComplexTypeModel reusableComplexTypeModel : reusableTypeList) {
                if (!reusableComplexTypeModel.isAbstract()) {
                    children = reusableComplexTypeModel.getSubTypes();
                    // set realType
                    node.setRealType(reusableComplexTypeModel.getName());
                    break;
                }
            }
        }

        return children;
    }

}
