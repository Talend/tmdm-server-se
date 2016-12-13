/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public class SearchTemplate implements IsSerializable {

    private String CriteriaName;

    private String ViewPK;

    private String owner;

    private Boolean shared;

    private String criteria;

    public String getCriteriaName() {
        return CriteriaName;
    }

    public void setCriteriaName(String criteriaName) {
        CriteriaName = criteriaName;
    }

    public String getViewPK() {
        return ViewPK;
    }

    public void setViewPK(String viewPK) {
        ViewPK = viewPK;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setShared(Boolean shared) {
        this.shared = shared;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public SearchTemplate() {
        super();
    }

    private String getOldCriteria() {
        return "<Criteria><Field>bookmark</Field><Operator>is</Operator><Value>null</Value></Criteria>";//$NON-NLS-1$
    }

    public String marshal2String() {

        String marshaledItem = "<BrowseItem>" + "<CriteriaName>" + this.CriteriaName + "</CriteriaName>" + "<ViewPK>" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                + this.ViewPK + "</ViewPK>" + "<Owner>" + this.owner + "</Owner>" + "<Shared>" + this.shared + "</Shared>"//$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                + "<WhereCriteria>" + getOldCriteria() + "</WhereCriteria><SearchCriteria>" + this.criteria//$NON-NLS-1$ //$NON-NLS-2$ 
                + "</SearchCriteria></BrowseItem>";//$NON-NLS-1$
        return marshaledItem;

    }
}
