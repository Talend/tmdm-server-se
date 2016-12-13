/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.inmemory.matcher;

import com.amalto.core.query.user.Predicate;
import com.amalto.core.storage.record.DataRecord;

public class UnaryMatcher implements Matcher {

    public static enum Operator {
        NOT
    }

    private final Matcher matcher;

    private final Predicate predicate;

    public UnaryMatcher(Matcher matcher, Predicate predicate) {
        this.matcher = matcher;
        this.predicate = predicate;
    }

    @Override
    public boolean match(DataRecord record) {
        if (predicate == Predicate.NOT) {
            return !matcher.match(record);
        }
        return matcher.match(record);
    }
}
