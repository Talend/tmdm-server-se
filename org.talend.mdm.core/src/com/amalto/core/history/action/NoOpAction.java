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

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.Set;

/**
 *
 */
public class NoOpAction implements Action {

    private static final Action INSTANCE = new NoOpAction();

    private final Date date;

    private NoOpAction() {
        this(Long.MIN_VALUE);
    }

    private NoOpAction(long time) {
        date = new Date(time);
    }

    public static Action instance() {
        return INSTANCE;
    }

    public static Action instance(long time) {
        return new NoOpAction(time);
    }

    public MutableDocument perform(MutableDocument document) {
        return document;
    }

    public MutableDocument undo(MutableDocument document) {
        return document;
    }

    public MutableDocument addModificationMark(MutableDocument document) {
        return document;
    }

    public MutableDocument removeModificationMark(MutableDocument document) {
        return document;
    }

    public Date getDate() {
        return date;
    }

    public String getSource() {
        return StringUtils.EMPTY;
    }

    public String getUserName() {
        return StringUtils.EMPTY;
    }

    public boolean isAllowed(Set<String> roles) {
        return true;
    }

    public String getDetails() {
        return StringUtils.EMPTY;
    }

    @Override
    public boolean isTransient() {
        return true;
    }
}
