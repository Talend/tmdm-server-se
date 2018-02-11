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

package com.amalto.core.history.action;

import com.amalto.core.history.DeleteType;
import com.amalto.core.history.MutableDocument;
import org.talend.mdm.commmon.metadata.ComplexTypeMetadata;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class CreateAction extends AbstractAction {

    private final ComplexTypeMetadata createdType;

    public CreateAction(Date date, String source, String userName, ComplexTypeMetadata createdType) {
        super(date, source, userName);
        this.createdType = createdType;
    }

    public MutableDocument perform(MutableDocument document) {
        return document.create(document);
    }

    public MutableDocument undo(MutableDocument document) {
        return document.delete(DeleteType.PHYSICAL);
    }

    public MutableDocument addModificationMark(MutableDocument document) {
        return document;
    }

    public MutableDocument removeModificationMark(MutableDocument document) {
        return document;
    }

    public boolean isAllowed(Set<String> roles) {
        List<String> denyCreate = createdType.getDenyCreate();
        boolean isAllowed = true;
        for (String role : roles) {
            if (denyCreate.contains(role)) {
                isAllowed = false;
            }
        }
        return isAllowed;
    }

    public String getDetails() {
        return "create '" + createdType.getName() + "'";
    }

    @Override
    public boolean isTransient() {
        return false;
    }
}
