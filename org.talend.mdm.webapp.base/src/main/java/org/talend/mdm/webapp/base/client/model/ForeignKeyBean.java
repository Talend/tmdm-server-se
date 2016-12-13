/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.model;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class ForeignKeyBean extends ItemBaseModel {

    private static final long serialVersionUID = 1L;

    private String foreignKeyPath;

    private boolean showInfo = false;

    private String typeName;

    private String conceptName;

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public boolean isShowInfo() {
        return showInfo;
    }

    public void setShowInfo(boolean showInfo) {
        this.showInfo = showInfo;
    }

    private Map<String, String> foreignKeyInfo = new LinkedHashMap<String, String>();

    public Map<String, String> getForeignKeyInfo() {
        return foreignKeyInfo;
    }

    public void setForeignKeyInfo(Map<String, String> foreignKeyInfo) {
        this.foreignKeyInfo = foreignKeyInfo;
    }

    public String getId() {
        return get("id"); //$NON-NLS-1$
    }

    public void setId(String id) {
        set("id", id); //$NON-NLS-1$
    }

    public String getDisplayInfo() {
        return get("displayInfo"); //$NON-NLS-1$
    }

    public void setDisplayInfo(String displayInfo) {
        set("displayInfo", displayInfo); //$NON-NLS-1$
    }

    public String getForeignKeyPath() {
        return foreignKeyPath;
    }

    public void setForeignKeyPath(String foreignKeyPath) {
        this.foreignKeyPath = foreignKeyPath;
    }

    public String getFullString() {
        return foreignKeyPath + "-" + getId(); //$NON-NLS-1$
    }

    @Override
    public String toString() {
        if (foreignKeyInfo.size() != 0) {
            if (getDisplayInfo() != null && getDisplayInfo().trim().length() > 0)
                return getDisplayInfo();
            StringBuilder sb = new StringBuilder();
            Collection<String> fkInfoValues = foreignKeyInfo.values();
            int i = 0;
            for (String fkInfoValue : fkInfoValues) {
                if (fkInfoValue != null && fkInfoValue.trim().length() > 0) {
                    if (i > 0)
                        sb.append('-');
                    sb.append(fkInfoValue);
                    i++;
                }
            }
            String displayValue = sb.toString();
            if (displayValue != null && displayValue.trim().length() > 0) {
                return displayValue;
            } else {
                return getId();
            }
        } else {
            return this.getId();
        }
   	}
}
