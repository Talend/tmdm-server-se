/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.save.context;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.accessor.Accessor;
import com.amalto.core.history.action.FieldUpdateAction;
import org.apache.commons.lang.StringUtils;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import java.util.Date;

public class CreateWithProvidedIdActions extends CreateActions {

    CreateWithProvidedIdActions(MutableDocument document, Date date, String source, String userName, String dataCluster, String dataModel, SaverSource saverSource) {
        super(document, date, source, userName, dataCluster, dataModel, saverSource);
    }

    @Override
    protected void handleField(FieldMetadata field, boolean doCreate, String currentPath) {
        if (field.isKey()) {
            Accessor accessor = document.createAccessor(currentPath);
            if (accessor.exist()) {
                actions.add(new FieldUpdateAction(date, source, userName, currentPath, StringUtils.EMPTY, accessor.get(), field));
                return;
            }
        }
        super.handleField(field, doCreate, currentPath);
    }
}
