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

package com.amalto.core.save.context;

import com.amalto.core.history.MutableDocument;
import com.amalto.core.history.action.CreateAction;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import java.util.Date;
import java.util.Set;

class OverrideCreateAction extends CreateAction {

    private final MutableDocument userDocument;

    private final ComplexTypeMetadata type;

    public OverrideCreateAction(Date date, String source, String userName, MutableDocument userDocument) {
        this(date, source, userName, userDocument, null);
    }

    public OverrideCreateAction(Date date, String source, String userName, MutableDocument userDocument, ComplexTypeMetadata type) {
        super(date, source, userName, type);
        this.userDocument = userDocument;
        this.type = type;
    }

    @Override
    public MutableDocument perform(MutableDocument document) {
        // Copy the document because this action is applied on 2 documents (and one of them removes attributes that should
        // not be removed from the other).
        document.create(userDocument.copy());
        return document;
    }

    @Override
    public boolean isAllowed(Set<String> roles) {
        return type == null || super.isAllowed(roles);
    }
}
