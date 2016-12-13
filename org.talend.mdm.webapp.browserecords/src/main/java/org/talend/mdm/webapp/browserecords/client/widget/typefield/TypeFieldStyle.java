/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.typefield;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 * DOC Administrator  class global comment. Detailed comment
 */
public class TypeFieldStyle implements IsSerializable {

    public static final String SCOPE_BUILTIN_TYPEFIELD = "SCOPE_BUILTIN_TYPEFIELD";

    public static final String SCOPE_CUSTOM_TYPEFIELD = "SCOPE_CUSTOM_TYPEFIELD";

    public static final String ATTRI_WIDTH = "ATTRI_WIDTH";

    private String key;

    private String value;

    private String scope;

    /**
     * DOC Administrator TypeFieldStyle constructor comment.
     */
    public TypeFieldStyle() {

    }

    public TypeFieldStyle(String key, String value, String scope) {
        super();
        this.key = key;
        this.value = value;
        this.scope = scope;
    }

    public String getValue() {
        return value;
    }

    public String getScope() {
        return scope;
    }

}
