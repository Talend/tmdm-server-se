// ============================================================================
//
// Copyright (C) 2006-2018 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package com.amalto.core.storage.hibernate;

/**
 * The ThreadLocal method use below scenarios.<br />
 * 1. during the Integrated Matching to except Golden record with state code 204. {@link MatchMergeUtils#getObsoleteTaskIds}<br />
 * 2. MatchingStatistics to bypass the sub condition that added in class StandardQueryHandler#visit(Compare), {@link MatchingStatistics#getMatchingStatistics}
 * Created by hwzhu on Dec 18, 2018
 *
 */
public final class MatchingIdentifier {

    private static ThreadLocal<Boolean> threadLocal = new ThreadLocal<Boolean>() {
        public Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    public static void set(boolean value) {
        threadLocal.set(Boolean.valueOf(value));
    }

    public static boolean get() {
        return ((Boolean) threadLocal.get()).booleanValue();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
