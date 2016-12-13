/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.util;

import com.extjs.gxt.ui.client.state.CookieProvider;

public class Cookies {

    private static CookieProvider provider = new CookieProvider(null, null, null, false);

    public static void setValue(String key, Object value) {
        provider.set(key, value);
    }

    public static Object getValue(String key) {
        return provider.get(key);
    }
}
