/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.util;

import org.talend.mdm.webapp.browserecords.client.BrowseRecordsGWTTest;

import com.google.gwt.core.client.JavaScriptObject;

public class CallJsniGWTTest extends BrowseRecordsGWTTest {

    private native void initJsEnv()/*-{
        $wnd.amalto = {};
        $wnd.amalto.itemsbrowser = {}
        $wnd.amalto.itemsbrowser.ItemsBrowser = {};
    }-*/;

    @Override
    protected void gwtSetUp() throws Exception {
        super.gwtSetUp();
        initJsEnv();
        browseRecords.regItemDetails();
    }
    
    public void testEditItemDetails() {

        JavaScriptObject errors = editItemDetails();
        assertEquals(checkErrors(errors), true);
    }
    

    private native JavaScriptObject editItemDetails()/*-{
        var errors = [];
        try {
            $wnd.amalto.itemsbrowser.ItemsBrowser.editItemDetails();
        } catch (e){
            errors.push(e.message);
        }
        try {
            $wnd.amalto.itemsbrowser.ItemsBrowser.editItemDetails("Search->");
        } catch (e){
            errors.push(e.message);
        }
        try {
            $wnd.amalto.itemsbrowser.ItemsBrowser.editItemDetails("Search->","11.22".split("."));
        } catch (e){
            errors.push(e.message);
        }
        try {
            $wnd.amalto.itemsbrowser.ItemsBrowser.editItemDetails("Search->","11.22".split("."), "Product");
        } catch (e){
            errors.push(e.message);
        }
        try {
            $wnd.amalto.itemsbrowser.ItemsBrowser.editItemDetails("Search->","11.22".split("."), "Product", function(){});
        } catch (e){
            errors.push(e.message);
        }
        return errors;
    }-*/;

    private native boolean checkErrors(JavaScriptObject errors)/*-{
        var result = true;
        result = result && errors.length === 5;
        result = result && errors[0] === "argument format error!";
        result = result && errors[1] === "argument format error!";
        result = result && errors[2] === "argument format error!";
        result = result && errors[3] != "argument format error!";
        result = result && errors[4] != "argument format error!";
        return result;
    }-*/;

}
