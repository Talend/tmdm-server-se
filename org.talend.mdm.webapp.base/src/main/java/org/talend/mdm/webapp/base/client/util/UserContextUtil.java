/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.base.client.util;

import org.talend.mdm.webapp.base.client.model.UserContextModel;

public class UserContextUtil {

    static {
        init();
    }
    
    public native static void init()/*-{
        $wnd.mdm_ucx = $wnd.mdm_ucx || {};
    }-*/;

    public static UserContextModel getUserContext() {
        UserContextModel ucx = new UserContextModel();
        ucx.setDataContainer(getDataContainer());
        ucx.setDataModel(getDataModel());
        ucx.setLanguage(getLanguage());
        ucx.setDateTimeFormat(getDateTimeFormat());
        return ucx;
    }


    public static native String getDataContainer()/*-{
        return $wnd.mdm_ucx.dataContainer;
    }-*/;
    
    public static native void setDataContainer(String dataContainer)/*-{
        $wnd.mdm_ucx.dataContainer = dataContainer;
    }-*/;
    
    public static native String getDataModel()/*-{
        return $wnd.mdm_ucx.dataModel;
    }-*/;
    
    public static native void setDataModel(String dataModel)/*-{
        $wnd.mdm_ucx.dataModel = dataModel;
    }-*/;
    
    public static native String getLanguage()/*-{
         return $wnd.mdm_ucx.language;
    }-*/;
    
    public static native String setLanguage(String language)/*-{
        $wnd.mdm_ucx.language = language;
    }-*/;
    
    public static native String getDateTimeFormat()/*-{
        return $wnd.mdm_ucx.dateTimeFormat;
    }-*/;
    
    public static native String setDateTimeFormat(String dateTimeFormat)/*-{
        $wnd.mdm_ucx.dateTimeFormat = dateTimeFormat;
    }-*/;
}
