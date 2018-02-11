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

package com.amalto.core.history.accessor;

import org.apache.commons.lang.StringUtils;

public class NoOpAccessor implements Accessor {

    public static final Accessor INSTANCE = new NoOpAccessor();

    private NoOpAccessor() {
    }

    public void set(String value) {
    }

    public String get() {
        return StringUtils.EMPTY;
    }

    public void touch() {
    }

    public void create() {
    }

    @Override
    public void insert() {
    }

    public void createAndSet(String value) {
    }

    public void delete() {
    }

    public boolean exist() {
        return true;
    }

    public void markModified(Marker marker) {
    }

    public void markUnmodified() {
    }

    public int size() {
        return 0;
    }

    public String getActualType() {
        return StringUtils.EMPTY;
    }

    @Override
    public int compareTo(Accessor accessor) {
        if (exist() != accessor.exist()) {
            return -1;
        }
        if (exist()) {
            return get().equals(accessor.get()) ? 0 : -1;
        }
        return -1;
    }

}
