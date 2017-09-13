/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.record;

import com.amalto.core.storage.hibernate.TypeMapping;
import com.amalto.core.storage.hibernate.Wrapper;
import com.amalto.core.storage.record.metadata.DataRecordMetadataImpl;

public class ObjectDataRecordReader {

    public DataRecord read(TypeMapping mapping, Wrapper input) {
        if (mapping == null) {
            throw new IllegalArgumentException("Mapping cannot be null.");
        }
        DataRecordMetadataImpl recordMetadata = new DataRecordMetadataImpl(input.timestamp(), input.taskId());
        DataRecord record = new DataRecord(mapping.getUser(), recordMetadata);
        mapping.setValues(input, record);
        return record;
    }
}
