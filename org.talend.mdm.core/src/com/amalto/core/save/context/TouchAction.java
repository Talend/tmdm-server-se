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

import com.amalto.core.history.Action;
import com.amalto.core.history.MutableDocument;

import java.util.Date;
import java.util.Set;

public class TouchAction implements Action {

    private final String path;

    private final Date date;

    private final String source;

    private final String userName;

    TouchAction(String path, Date date, String source, String userName) {
        this.path = path;
        this.date = date;
        this.source = source;
        this.userName = userName;
    }

    public MutableDocument perform(MutableDocument document) {
        document.createAccessor(path).touch();
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
        return source;
    }

    public String getUserName() {
        return userName;
    }

    public boolean isAllowed(Set<String> roles) {
        return true;
    }

    public String getDetails() {
        return "Accessing value"; //$NON-NLS-1$
    }

    @Override
    public boolean isTransient() {
        return true;
    }

    @Override
    public String toString() {
        return "TouchAction{" + //$NON-NLS-1$
                "path='" + path + '\'' + //$NON-NLS-1$
                '}';
    }
}
