/*
 * Copyright (C) 2006-2019 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.webservice;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="WSExistsTransformerPluginV2")
public class WSExistsTransformerPluginV2 {
    protected java.lang.String jndiName;

    public WSExistsTransformerPluginV2() {
    }

    public WSExistsTransformerPluginV2(java.lang.String jndiName) {
        this.jndiName = jndiName;
    }

    public java.lang.String getJndiName() {
        return jndiName;
    }

    public void setJndiName(java.lang.String jndiName) {
        this.jndiName = jndiName;
    }
}
