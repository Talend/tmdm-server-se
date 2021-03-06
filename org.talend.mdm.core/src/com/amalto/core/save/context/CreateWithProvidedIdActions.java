/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.FieldMetadata;
import org.talend.mdm.commmon.util.core.EUUIDCustomType;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.history.action.FieldUpdateAction;
import com.amalto.core.save.UserAction;

public class CreateWithProvidedIdActions extends CreateActions {

    CreateWithProvidedIdActions(MutableDocument document, Date date, String source, String userName, String dataCluster,
            String dataModel, SaverSource saverSource, UserAction userAction) {
        super(document, date, source, userName, dataCluster, dataModel, saverSource, userAction);
    }

    @Override
    protected void handleField(FieldMetadata field, boolean doCreate, String currentPath, UserAction userAction) {
        if (field.isKey()) {
            Accessor accessor = document.createAccessor(currentPath);
            if (accessor.exist() && isValidField(field, accessor)) {
                actions.add(new FieldUpdateAction(date, source, userName, currentPath, StringUtils.EMPTY, accessor.get(), field, userAction));
                return;
            }
        }
        super.handleField(field, doCreate, currentPath, userAction);
    }

    private boolean isValidField(FieldMetadata keyField, Accessor accessor) {
        if (isServerProvidedValue(keyField.getType().getName())) {
            String value = accessor.get();
            if (StringUtils.isBlank(value)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isServerProvidedValue(String keyFieldTypeName) {
        return EUUIDCustomType.UUID.getName().equalsIgnoreCase(keyFieldTypeName)
                || EUUIDCustomType.AUTO_INCREMENT.getName().equalsIgnoreCase(keyFieldTypeName);
    }
}
