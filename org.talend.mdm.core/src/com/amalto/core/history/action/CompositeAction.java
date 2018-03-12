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

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class CompositeAction implements Action {

    private final Date date;

    private final String source;

    private final String userName;

    private final List<Action> actions;

    public CompositeAction(Date date, String source, String userName, List<Action> actions) {
        this.date = date;
        this.source = source;
        this.userName = userName;
        this.actions = actions;
    }

    public MutableDocument perform(MutableDocument document) {
        MutableDocument mutableDocument = document;
        for (Action action : actions) {
            mutableDocument = action.perform(document);
        }
        return mutableDocument;
    }

    public MutableDocument undo(MutableDocument document) {
        MutableDocument mutableDocument = document;
        for (Action action : actions) {
            mutableDocument = action.undo(document);
        }
        return mutableDocument;
    }

    public MutableDocument addModificationMark(MutableDocument document) {
        MutableDocument mutableDocument = document;
        for (Action action : actions) {
            mutableDocument = action.addModificationMark(document);
        }
        return mutableDocument;
    }

    public MutableDocument removeModificationMark(MutableDocument document) {
        MutableDocument mutableDocument = document;
        for (Action action : actions) {
            mutableDocument = action.removeModificationMark(document);
        }
        return mutableDocument;
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

    public boolean isAllowed(Set<String> roles) {
        boolean isAllowed = true;
        for (Action action : actions) {
            isAllowed &= action.isAllowed(roles);
        }
        return isAllowed;
    }

    public String getDetails() {
        StringBuilder result = new StringBuilder();
        for (Action action : actions) {
            result.append(action.getDetails());
        }
        return result.toString();
    }

    @Override
    public boolean isTransient() {
        for (Action action : actions) {
            if(!action.isTransient()) {
                return false;
            }
        }
        return true;
    }
}
