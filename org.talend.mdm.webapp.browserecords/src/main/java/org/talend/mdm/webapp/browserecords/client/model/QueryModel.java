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

import org.talend.mdm.webapp.base.client.model.ItemBaseModel;
import org.talend.mdm.webapp.base.shared.EntityModel;
import org.talend.mdm.webapp.browserecords.shared.ViewBean;

public class QueryModel extends ItemBaseModel {

    private static final long serialVersionUID = 4315775494963149856L;

    RecordsPagingConfig pagingLoadConfig;

    String dataClusterPK;

    ViewBean view;
    
    EntityModel model;

    String criteria;

    String language;
    
    String errorValue;

    public QueryModel() {

    }

    public RecordsPagingConfig getPagingLoadConfig() {
        return pagingLoadConfig;
    }

    public void setPagingLoadConfig(RecordsPagingConfig pagingLoadConfig) {
        this.pagingLoadConfig = pagingLoadConfig;
    }

    public String getDataClusterPK() {
        return dataClusterPK;
    }

    public void setDataClusterPK(String dataClusterPK) {
        this.dataClusterPK = dataClusterPK;
    }

    public ViewBean getView() {
        return view;
    }

    public void setView(ViewBean view) {
        this.view = view;
    }
    
    public EntityModel getModel() {
        return model;
    }

    public void setModel(EntityModel model) {
        this.model = model;
    }

    public String getCriteria() {
        return criteria;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
    
    public String getErrorValue() {
        return errorValue;
    }
    
    public void setErrorValue(String errorValue) {
        this.errorValue = errorValue;
    }
}
