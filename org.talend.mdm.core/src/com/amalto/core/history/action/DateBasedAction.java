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
import org.apache.commons.lang.NotImplementedException;

import java.util.Date;
import java.util.Set;

public class DateBasedAction implements Action {

    private final Action left;

    private final Action right;

    private final Type type;

    private static enum Type {
        MOST_RECENT,
        LEAST_RECENT
    }

    private DateBasedAction(Action left,
                            Action right,
                            Type type) {
        this.left = left;
        this.right = right;
        this.type = type;
    }

    public static Action mostRecent(Action left, Action right) {
        return new DateBasedAction(left, right, Type.MOST_RECENT);
    }

    public static Action leastRecent(Action left, Action right) {
        return new DateBasedAction(left, right, Type.LEAST_RECENT);
    }

    public Action getAction() {
        switch (type) {
            case MOST_RECENT:
                return right.getDate().getTime() > left.getDate().getTime() ? right : left;
            case LEAST_RECENT:
                return right.getDate().getTime() < left.getDate().getTime() ? right : left;
            default:
                throw new NotImplementedException("No support for '" + type + "'.");
        }
    }

    @Override
    public MutableDocument perform(MutableDocument document) {
        return getAction().perform(document);
    }

    @Override
    public MutableDocument undo(MutableDocument document) {
        return getAction().undo(document);
    }

    @Override
    public MutableDocument addModificationMark(MutableDocument document) {
        return getAction().addModificationMark(document);
    }

    @Override
    public MutableDocument removeModificationMark(MutableDocument document) {
        return getAction().removeModificationMark(document);
    }

    @Override
    public Date getDate() {
        return getAction().getDate();
    }

    @Override
    public String getSource() {
        return getAction().getSource();
    }

    @Override
    public String getUserName() {
        return getAction().getUserName();
    }

    @Override
    public boolean isAllowed(Set<String> roles) {
        return getAction().isAllowed(roles);
    }

    @Override
    public String getDetails() {
        return getAction().getDetails();
    }

    @Override
    public boolean isTransient() {
        return getAction().isTransient();
    }
}
