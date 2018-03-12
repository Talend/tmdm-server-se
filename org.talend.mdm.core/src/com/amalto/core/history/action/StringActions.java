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
import com.amalto.core.history.FieldAction;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class StringActions {

    public static FieldAction concat(FieldUpdateAction action, String separator) {
        return new ConcatAction(action, separator);
    }

    public static Action longest(FieldUpdateAction action) {
        return new LongestAction(action);
    }

    public static Action shortest(FieldUpdateAction action) {
        return new ShortestAction(action);
    }

    public static Action mostCommon(Collection<FieldUpdateAction> actions) {
        if (actions == null || actions.isEmpty()) {
            return NoOpAction.instance();
        }
        return MostCommonAction.mostCommon(actions);
    }

    private static class ConcatAction extends FieldUpdateAction {

        private final String separator;

        private ConcatAction(FieldUpdateAction delegate, String separator) {
            super(delegate.date,
                    delegate.source,
                    delegate.userName,
                    delegate.path,
                    delegate.oldValue,
                    delegate.newValue,
                    delegate.updatedField);
            this.separator = separator;
        }

        @Override
        public String getNewValue() {
            if (StringUtils.isEmpty(separator)) {
                return super.getOldValue() + super.getNewValue();
            } else {
                return super.getOldValue() + separator + super.getNewValue();
            }
        }
    }

    private static class LongestAction extends FieldUpdateAction {
        private LongestAction(FieldUpdateAction delegate) {
            super(delegate.date,
                    delegate.source,
                    delegate.userName,
                    delegate.path,
                    delegate.oldValue,
                    delegate.newValue,
                    delegate.updatedField);
        }

        @Override
        public String getNewValue() {
            String newValue = super.getNewValue();
            String oldValue = super.getOldValue();
            if (newValue.length() >= oldValue.length()) {
                return newValue;
            } else {
                return oldValue;
            }
        }
    }

    private static class ShortestAction extends FieldUpdateAction {
        private ShortestAction(FieldUpdateAction delegate) {
            super(delegate.date,
                    delegate.source,
                    delegate.userName,
                    delegate.path,
                    delegate.oldValue,
                    delegate.newValue,
                    delegate.updatedField);
        }

        @Override
        public String getNewValue() {
            String newValue = super.getNewValue();
            String oldValue = super.getOldValue();
            if (newValue.length() < oldValue.length()) {
                return newValue;
            } else {
                return oldValue;
            }
        }
    }

    private static class MostCommonAction extends FieldUpdateAction {
        private MostCommonAction(String mostCommonValue, FieldUpdateAction delegate) {
            super(delegate.date,
                    delegate.source,
                    delegate.userName,
                    delegate.path,
                    delegate.oldValue,
                    mostCommonValue,
                    delegate.updatedField);
        }

        private static Action mostCommon(Collection<FieldUpdateAction> actions) {
            List<String> newValues = new ArrayList<String>(actions.size());
            for (FieldUpdateAction action : actions) {
                newValues.add(action.getNewValue());
            }
            String mostCommonValue = getMostCommonValue(newValues);
            return new MostCommonAction(mostCommonValue, actions.iterator().next());
        }

        // TODO Duplicated code with org.talend.dataquality.matchmerge -> bad
        public static String getMostCommonValue(Collection<String> values) {
            if (values.isEmpty()) {
                return null;
            }
            String[] strings = values.toArray(new String[values.size()]);
            Arrays.sort(strings); // Sorts items to ensure all similar strings are grouped together
            int occurrenceCount = 1;
            int maxOccurrenceCount = 0;
            String mostCommon = strings[0];
            String previousString = strings[0];
            for (int i = 1; i < strings.length; i++) {
                String current = strings[i];
                if (!areEquals(previousString, current)) {
                    if (occurrenceCount > maxOccurrenceCount) {
                        mostCommon = previousString;
                        maxOccurrenceCount = occurrenceCount;
                    }
                    occurrenceCount = 1;
                } else {
                    occurrenceCount++;
                }
                previousString = current;
            }
            if (occurrenceCount > maxOccurrenceCount) {
                mostCommon = previousString;
            }
            return mostCommon;
        }

        private static boolean areEquals(String previousString, String current) {
            if (previousString == null) {
                return current == null;
            }
            return previousString.equals(current);
        }
    }
}
