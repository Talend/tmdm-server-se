/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client.boundary;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.general.client.General;
import org.talend.mdm.webapp.general.client.GeneralServiceAsync;
import org.talend.mdm.webapp.general.client.layout.BrandingBar;
import org.talend.mdm.webapp.general.model.ProductInfo;

import com.extjs.gxt.ui.client.Registry;

public class PubService {

    public native static void registerLanguageService()/*-{
		$wnd.getLanguage = function() {
			return @org.talend.mdm.webapp.base.client.util.UrlUtil::getLanguage()();
		};
    }-*/;

    public native static void registerUpdateProductInfo()/*-{
		$wnd.refreshProductInfo = function() {
			@org.talend.mdm.webapp.general.client.boundary.PubService::updateProductInfo()();
		};
    }-*/;

    static void updateProductInfo() {
        GeneralServiceAsync service = (GeneralServiceAsync) Registry.get(General.OVERALL_SERVICE);
        service.getProductInfo(new SessionAwareAsyncCallback<ProductInfo>() {

            @Override
            public void onSuccess(ProductInfo info) {
                BrandingBar.getInstance().setProductInfo(info);
            }
        });
    }
}
