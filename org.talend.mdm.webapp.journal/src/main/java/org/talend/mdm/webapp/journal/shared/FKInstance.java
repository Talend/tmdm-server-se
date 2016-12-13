/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.shared;

import java.io.Serializable;

/**
 * DOC Administrator  class global comment. Detailled comment
 */
public class FKInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fkInfo;

    boolean retireveFKInfo;

    public FKInstance() {

    }

    public boolean isRetireveFKInfo() {
        return retireveFKInfo;
    }

    public void setRetireveFKInfo(boolean retireveFKInfo) {
        this.retireveFKInfo = retireveFKInfo;
    }

    public String getFkInfo() {
        return fkInfo;
    }

    public void setFkInfo(String fkInfo) {
        this.fkInfo = fkInfo;
    }
}