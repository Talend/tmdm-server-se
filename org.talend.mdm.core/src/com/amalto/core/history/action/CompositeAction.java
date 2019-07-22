/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import java.util.stream.Collectors;

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
        List<Action> reorderedAction = reorderDeleteContainedTypeActions(actions);
        for (Action action : reorderedAction) {
            FieldUpdateAction fieldUpdateAction = (FieldUpdateAction) action;
            if (fieldUpdateAction.getPath().indexOf('@') > 0 && fieldUpdateAction.getNewValue() == null
                    && fieldUpdateAction.getOldValue() != null) {
                continue;
            }
            mutableDocument = action.perform(document);
        }
        return mutableDocument;
    }

    public MutableDocument undo(MutableDocument document) {
        MutableDocument mutableDocument = document;
        List<Action> copyActions = reorderDeleteContainedTypeActions(reverseXSITypeActions(actions));
        for (Action action : copyActions) {
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

    public List<Action> getActions() {
        return actions;
    }

    /**
     * reorder the actions if the actions contains one which is a delete contained type
     * eg.:
     * actions[0] ="FieldUpdateAction{path='detail[2]/code', oldValue='s', newValue='null'}"
     * actions[1] ="FieldUpdateAction{path='detail[2]/features/actor', oldValue='sf', newValue='null'}"
     * actions[2] ="FieldUpdateAction{path='detail[2]/features/vendor[1]', oldValue='sd', newValue='null'}"
     * actions[3] ="FieldUpdateAction{path='detail[2]/features', oldValue='null', newValue='null'}"
     * <p>
     * return:
     * actions[0] ="FieldUpdateAction{path='detail[2]/code', oldValue='s', newValue='null'}"
     * actions[1] ="FieldUpdateAction{path='detail[2]/features', oldValue='null', newValue='null'}"
     * actions[2] ="FieldUpdateAction{path='detail[2]/features/actor', oldValue='sf', newValue='null'}"
     * actions[3] ="FieldUpdateAction{path='detail[2]/features/vendor[1]', oldValue='sd', newValue='null'}"
     *
     * @param actions the action list need to be reordered
     * @return reordered action list
     */
    protected List<Action> reorderDeleteContainedTypeActions(List<Action> actions) {
        List<Action> copyActions = new ArrayList<>(actions);
        int beginIndex = 0;
        String previousPath = StringUtils.EMPTY;
        for (int i = actions.size() - 1; i >= 0; i--) {
            FieldUpdateAction fieldUpdateAction = (FieldUpdateAction) actions.get(i);
            if (fieldUpdateAction.getOldValue() == null && fieldUpdateAction.getNewValue() == null) {
                beginIndex = i;
                previousPath = fieldUpdateAction.getPath();
            } else if (beginIndex > 0 && !StringUtils
                    .equals(previousPath, StringUtils.substringBeforeLast(fieldUpdateAction.getPath(), "/"))) { //$NON-NLS-1$
                copyActions.remove(actions.get(beginIndex));
                copyActions.add(i + 1, actions.get(beginIndex));
                beginIndex = 0;
                previousPath = StringUtils.EMPTY;
            }

        }
        return copyActions;
    }

    /**
     * reverse the actions if the actions contains <pre>xsi:type</pre>
     * eg.
     * action[0] = "FieldUpdateAction{path='User/first/Location', oldValue='J-Location-N', newValue='null'}"
     * action[1] = "FieldUpdateAction{path='User/first/@xsi:type', oldValue='JuniorSchool', newValue='SeniorSchool'}"
     * action[2] = "FieldUpdateAction{path='User/first/Name', oldValue='J-Name-N', newValue='J-Name-N-to-O'}"
     * action[3] = "FieldUpdateAction{path='User/first/Gaokao', oldValue='null', newValue='J-Location-N-O'}"
     * action[4] = "FieldUpdateAction{path='User/second/Gaokao', oldValue='S-Gaokao-N', newValue='null'}"
     * action[5] = "FieldUpdateAction{path='User/second/@xsi:type', oldValue='SeniorSchool', newValue='JuniorSchool'}"
     * action[6] = "FieldUpdateAction{path='User/second/Name', oldValue='S-Name-N', newValue='S-Name-N-O'}"
     * action[7] = "FieldUpdateAction{path='User/second/Location', oldValue='null', newValue='S-Gaokao-N-O'}"
     * <p>
     * finally return:
     * action[0] = "FieldUpdateAction{path='User/first/Gaokao', oldValue='null', newValue='J-Location-N-O'}"
     * action[1] = "FieldUpdateAction{path='User/first/Name', oldValue='J-Name-N', newValue='J-Name-N-to-O'}"
     * action[2] = "FieldUpdateAction{path='User/first/@xsi:type', oldValue='JuniorSchool', newValue='SeniorSchool'}"
     * action[3] = "FieldUpdateAction{path='User/first/Location', oldValue='J-Location-N', newValue='null'}"
     * action[4] = "FieldUpdateAction{path='User/second/Location', oldValue='null', newValue='S-Gaokao-N-O'}"
     * action[5] = "FieldUpdateAction{path='User/second/Name', oldValue='S-Name-N', newValue='S-Name-N-O'}"
     * action[6] = "FieldUpdateAction{path='User/second/@xsi:type', oldValue='SeniorSchool', newValue='JuniorSchool'}"
     * action[7] = "FieldUpdateAction{path='User/second/Gaokao', oldValue='S-Gaokao-N', newValue='null'}"
     *
     * @param actions the actions need to be reversed
     * @return reversed action list
     */
    protected List<Action> reverseXSITypeActions(List<Action> actions) {
        List<Action> copyActions = new ArrayList<>(actions);
        List<String> parents = copyActions.stream().filter(action -> ((FieldUpdateAction) action).getPath().contains("@xsi:type"))
                .map(action -> (StringUtils.substringBeforeLast(((FieldUpdateAction) action).getPath(), "@xsi:type")))
                .collect(Collectors.toList());
        if (parents.isEmpty()) {
            return copyActions;
        }
        int beginIndex = -1;
        List<Action> changeTypeActions = new ArrayList<>(actions.size());
        int i = 0;
        for (String parent : parents) {
            for (; i < actions.size(); i++) {
                FieldUpdateAction fieldUpdateAction = (FieldUpdateAction) actions.get(i);
                String path = fieldUpdateAction.getPath();
                if (path.indexOf(parent) == 0) {
                    changeTypeActions.add(fieldUpdateAction);
                    if (beginIndex < 0) {
                        beginIndex = i;
                    }
                } else {
                    if (changeTypeActions.size() > 1) {
                        resetActions(beginIndex, copyActions, changeTypeActions);
                        changeTypeActions.clear();
                        beginIndex = -1;
                        break;
                    }
                }
            }
        }

        if (changeTypeActions.size() > 1) {
            resetActions(beginIndex, copyActions, changeTypeActions);
        }
        return copyActions;
    }

    /**
     * remove the actions if the actions contains <pre>xsi:type</pre>
     * remove the action which the newValue and oldValue both are null
     *
     * below two actions will be removed
     * action[0] = "FieldUpdateAction{path='User/first/Gaokao', oldValue='null', newValue='John'}"
     * action[0] = "FieldUpdateAction{path='User/detail', oldValue='null', newValue='null'}"
     */
    public void removeXSITypeAndNullValueAction() {
        Iterator<Action> iterator = actions.iterator();
        while (iterator.hasNext()) {
            Action action = iterator.next();
            FieldUpdateAction fieldUpdateAction = (FieldUpdateAction) action;
            if (fieldUpdateAction.getNewValue() == null && fieldUpdateAction.getOldValue() == null) {
                iterator.remove();
            }
        }
    }

    private void resetActions(int beginIndex, List<Action> copyActions, List<Action> changeTypeActions) {
        Collections.reverse(changeTypeActions);
        int resetIndex = beginIndex;
        for (Action changeTypeAction : changeTypeActions) {
            copyActions.set(resetIndex++, changeTypeAction);
        }
    }
}
