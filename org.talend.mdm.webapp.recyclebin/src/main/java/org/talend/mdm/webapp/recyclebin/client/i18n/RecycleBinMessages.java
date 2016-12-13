/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.recyclebin.client.i18n;

import com.google.gwt.i18n.client.Messages;

/**
 * DOC Administrator class global comment. Detailled comment
 */
public interface RecycleBinMessages extends Messages {

    String dataClusterName();

    String conceptName();

    String Ids();

    String UserName();

    String Date();

    String delete();

    String restore();

    String title();

    String search();

    String serarch_tooltip();

    String delete_confirm();

    String restore_confirm();

    String overwrite_confirm();

    String name();

    String dataModelName();
    
    String restore_no_permissions();
    
    String delete_no_permissions();
    
    String restoreSelected();
    
    String restoreSelectedConfirm();
    
    String restoreSelectedError(int count, String ids);
    
    String restoreSelectedOverwriteConfirm(String id);
    
    String deleteSelected();

    String deleteSelectedConfirm();
    
    String deleteSelectedError(int count, String ids);

    String select_warning();
}
