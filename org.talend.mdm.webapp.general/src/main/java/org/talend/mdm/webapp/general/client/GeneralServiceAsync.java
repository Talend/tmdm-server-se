/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client;

import java.util.List;

import org.talend.mdm.webapp.general.model.ActionBean;
import org.talend.mdm.webapp.general.model.LanguageBean;
import org.talend.mdm.webapp.general.model.MenuGroup;
import org.talend.mdm.webapp.general.model.ProductInfo;
import org.talend.mdm.webapp.general.model.UserBean;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>GreetingService</code>.
 */
public interface GeneralServiceAsync {

    void getProductInfo(AsyncCallback<ProductInfo> callback);

    void getMenus(String language, AsyncCallback<MenuGroup> callback);

    void setClusterAndModel(String cluster, String model, AsyncCallback<Void> callback);

    void getUser(AsyncCallback<UserBean> callback);

    void getLanguages(String language, AsyncCallback<List<LanguageBean>> callback);

    void getAction(AsyncCallback<ActionBean> callback);

    void logout(AsyncCallback<Void> callback);

    void isExpired(String language, AsyncCallback<Boolean> callback);

    void supportStaging(String dataCluster, AsyncCallback<Boolean> callback);

    void setDefaultLanguage(String language, AsyncCallback<Void> callback);

    void isEnterpriseVersion(AsyncCallback<Boolean> callback);
}
