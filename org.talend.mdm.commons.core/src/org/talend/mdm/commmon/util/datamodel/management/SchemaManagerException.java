/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.commmon.util.datamodel.management;

/**
 * DOC HSHU  class global comment. Detailled comment
 */
public class SchemaManagerException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = -5514699054391385118L;

    public SchemaManagerException(String msg) {
        super(msg);
    }

    public SchemaManagerException(Exception e) {
        super(e);
    }
}
