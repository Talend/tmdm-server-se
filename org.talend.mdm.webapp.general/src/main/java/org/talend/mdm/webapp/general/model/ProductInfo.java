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

public class ProductInfo implements Serializable, IsSerializable {

    private static final long serialVersionUID = 3239025568482047616L;

    private String productKey;

    private String productName;

    private String productEdition;

    public ProductInfo() {

    }

    public String getProductKey() {
        return this.productKey;
    }

    public void setProductKey(String productKey) {
        this.productKey = productKey;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductEdition() {
        return this.productEdition;
    }

    public void setProductEdition(String productEdition) {
        this.productEdition = productEdition;
    }

}
