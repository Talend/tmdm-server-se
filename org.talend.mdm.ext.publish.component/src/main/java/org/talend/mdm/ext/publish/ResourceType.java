/*
 * Copyright (C) 2006-2017 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.ext.publish;

public enum ResourceType {

    DATAMODELS("dataModels"), //$NON-NLS-1$
    DATAMODELSTYPES("dataModelsTypes"), //$NON-NLS-1$
    CUSTOMTYPESSETS("customTypesSets"), //$NON-NLS-1$
    PICTURES("pictures"), //$NON-NLS-1$
    DATACLUSTERBACKUPFILE("dataclusterBackupFile"), //$NON-NLS-1$
    BARFILE("barFile"); //$NON-NLS-1$

    ResourceType(String name) {
        this.name = name;
    }

    ResourceType() {
    };

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
