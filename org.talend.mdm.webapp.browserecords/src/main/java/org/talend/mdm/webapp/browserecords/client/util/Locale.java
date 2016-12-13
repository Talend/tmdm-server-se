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

import org.talend.mdm.webapp.base.client.util.UrlUtil;

public class Locale {

    public static String getLanguage() {
        return UrlUtil.getLanguage();
    }

    public static native String getExceptionMessageByLanguage(String language, String errorString)/*-{
        var reg = /\[(.*?):(.*?)\]/gi;
        var errorsArray = errorString.match(reg);
        if (errorsArray != null){
            for(var i=0;i<errorsArray.length;i++){
                if(errorsArray[i].indexOf("[")>=0){
                    errorsArray[i] = errorsArray[i].replace("[","").trim();
                }
                if(errorsArray[i].indexOf("]")>=0){
                    errorsArray[i] = errorsArray[i].replace("]"," ").trim();
                }
                if(language==errorsArray[i].split(":")[0].toLowerCase()&&errorsArray[i].split(":")[1]!=null&&errorsArray[i].split(":")[0].trim()!=""){
                    errorString=errorsArray[i].substr(errorsArray[i].indexOf(":")+1);
                    flag=true;
                    break;
                }
                if("en"==errorsArray[i].split(":")[0].toLowerCase()){
                    defualtErrorMsg=errorsArray[i].substr(errorsArray[i].indexOf(":")+1);
                }
            }
            if(!flag){
                return defualtErrorMsg;
            }
        }
        return errorString;
    }-*/;

    @Deprecated
    public static native String getExceptionString(String language, String result)/*-{
        var pattern = "([{]" + language.toUpperCase() + ":" + "(([^{]|[{][^{}]*[}])*)})";
        var resultArray = result.match(pattern);
        if (resultArray != null) {
            result = resultArray[2];
        }
        return result;
    }-*/;
}
