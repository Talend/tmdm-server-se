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
import org.apache.commons.lang.NotImplementedException;

public class BinaryMatcher implements Matcher {

    private final Matcher left;

    private final Predicate predicate;

    private final Matcher right;

    public BinaryMatcher(Matcher left, Predicate predicate, Matcher right) {
        this.left = left;
        this.predicate = predicate;
        this.right = right;
    }

    @Override
    public boolean match(DataRecord record) {
        if (predicate == Predicate.AND) {
            return left.match(record) && right.match(record);
        } else if (predicate == Predicate.OR) {
            return left.match(record) || right.match(record);
        } else {
            throw new NotImplementedException();
        }
    }
}
