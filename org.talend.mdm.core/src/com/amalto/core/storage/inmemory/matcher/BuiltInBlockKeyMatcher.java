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

import org.apache.commons.lang.NotImplementedException;

import com.amalto.core.query.user.Predicate;
import com.amalto.core.storage.StagingStorage;
import com.amalto.core.storage.record.DataRecord;

public class BuiltInBlockKeyMatcher implements Matcher {

    private final Predicate predicate;

    private final String value;

    public BuiltInBlockKeyMatcher(Predicate predicate, String value) {
        this.predicate = predicate;
        this.value = value;
    }

    @Override
    public boolean match(DataRecord record) {
        Object recordValue = record.getRecordMetadata().getRecordProperties().get(StagingStorage.METADATA_STAGING_BLOCK_KEY);
        if (recordValue == null) {
            return false;
        }
        if (predicate == Predicate.CONTAINS) {
            return recordValue.toString().indexOf(value) > 0;
        } else if (predicate == Predicate.EQUALS) {
            return recordValue.toString().equals(value);
        } else if (predicate == Predicate.STARTS_WITH) {
            return recordValue.toString().indexOf(value) == 0;
        } else {
            throw new NotImplementedException();
        }
    }
}
