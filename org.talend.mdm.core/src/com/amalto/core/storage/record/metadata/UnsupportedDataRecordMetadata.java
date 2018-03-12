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

package com.amalto.core.storage.record.metadata;

import java.util.*;

/**
 * A special implementation for {@link com.amalto.core.storage.record.DataRecord} that do not want to expose {@link DataRecordMetadata}.
 * This singleton might be used for projections of fields for instance.
 */
public class UnsupportedDataRecordMetadata implements DataRecordMetadata {

    public static final DataRecordMetadata INSTANCE = new UnsupportedDataRecordMetadata();

    private UnsupportedDataRecordMetadata() {
    }

    public long getLastModificationTime() {
        return 0;
    }

    public void setLastModificationTime(long lastModificationTime) {
    }

    public String getTaskId() {
        return null;
    }

    public void setTaskId(String taskId) {
    }

    public Map<String, String> getRecordProperties() {
        return new AbstractMap<String, String>() {

            @Override
            public Set<Entry<String, String>> entrySet() {
                return Collections.emptySet();
            }

            @Override
            public String put(String s, String s2) {
                return s;
            }

            @Override
            public String get(Object o) {
                return null;
            }

            @Override
            public boolean containsValue(Object o) {
                return false;
            }

            @Override
            public boolean containsKey(Object o) {
                return false;
            }

            @Override
            public int size() {
                return 0;
            }

            @Override
            public boolean isEmpty() {
                return true;
            }

            @Override
            public void putAll(Map<? extends String, ? extends String> map) {
            }

            @Override
            public void clear() {
            }

            @Override
            public Set<String> keySet() {
                return Collections.emptySet();
            }

            @Override
            public Collection<String> values() {
                return Collections.emptySet();
            }

            @Override
            public String remove(Object o) {
                return null;
            }
        };
    }

    @Override
    public DataRecordMetadata copy() {
        return this;
    }
}
