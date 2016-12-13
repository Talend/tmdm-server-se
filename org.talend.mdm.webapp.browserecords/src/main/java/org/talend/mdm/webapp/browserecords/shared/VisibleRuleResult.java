/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.shared;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public class VisibleRuleResult implements Serializable, IsSerializable {
    private String xpath;
    private boolean visible;

    public VisibleRuleResult() {

    }

    public VisibleRuleResult(String xpath, boolean visible) {
        this.xpath = xpath;
        this.visible = visible;
    }

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }
}
