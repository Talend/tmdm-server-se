/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.creator;

import org.talend.mdm.webapp.base.client.model.DataType;
import org.talend.mdm.webapp.base.client.model.DataTypeConstants;
import org.talend.mdm.webapp.base.client.model.DataTypeCustomized;

/**
 * DOC HSHU class global comment. Detailled comment
 */
public class DataTypeCreator {

    public static DataType getDataType(String typeName, String baseTypeName) {

        if ((typeName == null || typeName.trim().length() == 0) && "anyType".equals(baseTypeName)) //$NON-NLS-1$
            return DataTypeConstants.UNKNOW;

        DataTypeConstants[] values = DataTypeConstants.values();
        for (DataTypeConstants value : values) {
            if (value.getTypeName().equals(typeName)) {
                value.setBaseTypeName(baseTypeName);
                return value;
            }
        }
        if (typeName == null)
            typeName = "unknow"; //$NON-NLS-1$
        return new DataTypeCustomized(typeName, baseTypeName);
    }

}
