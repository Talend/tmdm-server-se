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

import com.amalto.core.history.FieldAction;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import java.util.Date;

/**
 *
 */
abstract class AbstractFieldAction implements FieldAction {

    final Date date;

    final String source;

    final String userName;

    private final FieldMetadata field;

    AbstractFieldAction(Date date, String source, String userName, FieldMetadata field) {
        this.date = date;
        this.source = source;
        this.userName = userName;
        this.field = field;
    }

    public Date getDate() {
        return date;
    }

    public String getSource() {
        return source;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public FieldMetadata getField() {
        return field;
    }

    @Override
    public boolean isTransient() {
        return false;
    }
}
