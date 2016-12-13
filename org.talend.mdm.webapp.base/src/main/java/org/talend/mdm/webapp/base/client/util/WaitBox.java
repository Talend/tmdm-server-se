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

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.MessageBox.MessageBoxType;


public class WaitBox {

    private static MessageBox waitBox; 

    public static void show(String title, String msg, String progressText) {
    	hide();
    	waitBox = new MessageBox();
    	waitBox.setType(MessageBoxType.WAIT);
        waitBox.setButtons(""); //$NON-NLS-1$
        waitBox.setClosable(false);
    	
        waitBox.setTitle(title);
        waitBox.setMessage(msg);
        waitBox.setProgressText(progressText);
        waitBox.show();
    }
    
    public static void hide() {
    	if (waitBox != null){
	        if (waitBox.getDialog() != null){
	            waitBox.close();
	        }
    	}
    }
}
