/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */

package com.amalto.core.jobox.util;

import java.net.URL;
import java.net.URLClassLoader;

public class JobClassLoader extends URLClassLoader {

    public JobClassLoader(URL[] urls) {
        // We had better use the same class loader hierarchy as the war package
        // fix bug 0022967, use current thread class loader
        super(urls, ClassLoader.getSystemClassLoader().getParent());
    }
 }
