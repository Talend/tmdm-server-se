// ============================================================================
//
// Copyright (C) 2006-2020 Talend Inc. - www.talend.com
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

import java.io.InputStream;

import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl;

public class StorageClassLoaderServiceImpl extends ClassLoaderServiceImpl {

    private static final long serialVersionUID = 6527413895139469352L;

    private final ClassLoader classLoaders;

    public StorageClassLoaderServiceImpl(ClassLoader classLoaders) {
        super();
        this.classLoaders = classLoaders;
    }

    @Override
    public InputStream locateResourceStream(String name) {
        final InputStream stream = classLoaders.getResourceAsStream(name);
        if (stream != null) {
            return stream;
        }
        return null;
    }
}