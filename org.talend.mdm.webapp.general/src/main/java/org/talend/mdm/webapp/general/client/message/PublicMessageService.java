/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client.message;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.core.client.JavaScriptObject;

public class PublicMessageService {
	
    static Map<String, JavaScriptFunction> messages = new HashMap<String, JavaScriptFunction>();

	public static native void registerMessageService()/*-{
        $wnd.General = {
        	registerGlobalMessage : function(msgId, handler){//  msgConfig.id, msgConfig.handler
        		@org.talend.mdm.webapp.general.client.message.PublicMessageService::registerGlobalMessageHandler(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(msgId, handler);
        	},
        	notifyGlobalMessage : function(msgId, data){
        		@org.talend.mdm.webapp.general.client.message.PublicMessageService::notifyMessage(Ljava/lang/String;Ljava/lang/Object;)(msgId, data);
        	}
        };
    }-*/;
	
	static void registerGlobalMessageHandler(String msgId, JavaScriptObject handler){
		JavaScriptFunction fsFn = new JavaScriptFunction(handler);
        messages.put(msgId, fsFn);
	}
	
	static void notifyMessage(String msgId, Object data){
        JavaScriptFunction fn = messages.get(msgId);
        if (fn != null) {
            fn.executeFn(msgId, data);
        }
	}
	
}
