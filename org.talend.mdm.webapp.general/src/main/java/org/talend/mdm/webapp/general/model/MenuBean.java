/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.model;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class MenuBean implements Serializable, IsSerializable {


	/**
     * 
     */
    private static final long serialVersionUID = -7349094793298324333L;
    int id;
	int level;
	String context;
	String icon;
	String name;
	String desc;
	String url;
	String application;
	
    boolean disabled;

    String disabledDesc;

	public MenuBean(){}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }

    public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getDisabledDesc() {
        return disabledDesc;
    }

    public void setDisabledDesc(String disabledDesc) {
        this.disabledDesc = disabledDesc;
    }

}
