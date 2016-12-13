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


public class GlobalMessage {

    public static native void registerGlobalMessageHandler(String msgId, MessageHandler handler)/*-{
        $wnd.General.registerGlobalMessage(msgId, function(msgId, data){
        handler.@org.talend.mdm.webapp.general.client.message.MessageHandler::handlerMessage(Ljava/lang/String;Ljava/lang/Object;)(msgId, data);
        });
    }-*/;

    public static native void notifyMessage(String msgId, Object data)/*-{
        $wnd.General.notifyGlobalMessage(msgId, data);
    }-*/;

}
