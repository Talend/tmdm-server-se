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

package com.amalto.core.objects;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class EmptyMap implements Map {
    @Override
    public Set entrySet() {
        return Collections.emptySet();
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
    public boolean containsValue(Object value) {
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return false;
    }

    @Override
    public Object get(Object key) {
        return null;
    }

    @Override
    public Object put(Object key, Object value) {
        return value;
    }

    @Override
    public Object remove(Object key) {
        return null;
    }

    @Override
    public void putAll(Map m) {
    }

    @Override
    public void clear() {
    }

    @Override
    public Set keySet() {
        return Collections.emptySet();
    }

    @Override
    public Collection values() {
        return Collections.emptySet();
    }
}
