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

@XmlType(name="WSExistsView")
public class WSExistsView {
    protected com.amalto.core.webservice.WSViewPK wsViewPK;

    public WSExistsView() {
    }

    public WSExistsView(com.amalto.core.webservice.WSViewPK wsViewPK) {
        this.wsViewPK = wsViewPK;
    }

    public com.amalto.core.webservice.WSViewPK getWsViewPK() {
        return wsViewPK;
    }

    public void setWsViewPK(com.amalto.core.webservice.WSViewPK wsViewPK) {
        this.wsViewPK = wsViewPK;
    }
}
