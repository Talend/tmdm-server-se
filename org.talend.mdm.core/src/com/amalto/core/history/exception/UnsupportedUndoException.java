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

package com.amalto.core.history.exception;

import com.amalto.core.history.Action;

/**
 *
 */
public class UnsupportedUndoException extends RuntimeException {
    private final Action causeAction;

    public UnsupportedUndoException(String message, Action causeAction) {
        super(message);
        this.causeAction = causeAction;
    }

    public Action getAction() {
        return causeAction;
    }
}
