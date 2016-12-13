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

import com.amalto.core.storage.StorageMetadataUtils;
import org.apache.commons.lang.NotImplementedException;
import org.talend.mdm.commmon.metadata.FieldMetadata;

import com.amalto.core.query.user.Predicate;
import com.amalto.core.storage.record.DataRecord;

public class CompareMatcher implements Matcher {

    private final FieldMetadata field;

    private final Predicate predicate;

    private final String value;

    public CompareMatcher(FieldMetadata field, Predicate predicate, String value) {
        this.field = field;
        this.predicate = predicate;
        this.value = value;
    }

    @Override
    public boolean match(DataRecord record) {
        Object recordValue = record.get(field);
        if (recordValue == null) {
            return false;
        }
        Object matchValue = StorageMetadataUtils.convert(value, field);
        if (predicate == Predicate.CONTAINS) {
            return recordValue.toString().indexOf(matchValue.toString()) > 0;
        } else if (predicate == Predicate.EQUALS) {
            return recordValue.toString().equals(matchValue.toString());
        } else if (predicate == Predicate.GREATER_THAN) {
            return Double.compare(((Double) recordValue), ((Double) matchValue)) > 0;
        } else if (predicate == Predicate.GREATER_THAN_OR_EQUALS) {
            return Double.compare(((Double) recordValue), ((Double) matchValue)) >= 0;
        } else if (predicate == Predicate.LOWER_THAN) {
            return Double.compare(((Double) recordValue), ((Double) matchValue)) < 0;
        } else if (predicate == Predicate.LOWER_THAN_OR_EQUALS) {
            return Double.compare(((Double) recordValue), ((Double) matchValue)) <= 0;
        } else if (predicate == Predicate.STARTS_WITH) {
            return recordValue.toString().indexOf(matchValue.toString()) == 0;
        } else {
            throw new NotImplementedException();
        }
    }
}
