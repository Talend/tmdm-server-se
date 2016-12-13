/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.journal.shared;

import java.io.Serializable;
import java.util.List;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * DOC Administrator  class global comment. Detailled comment
 */
public class JournalTreeModel extends BaseTreeModel implements Serializable, IsSerializable {

    private static final long serialVersionUID = 1L;
    
    private boolean isAuth = true;

    public JournalTreeModel() {

    }

    public JournalTreeModel(String name) {
        this.set("name", name); //$NON-NLS-1$
    }

    public JournalTreeModel(String id, String name,String path) {
        this(name);
        this.set("id", id); //$NON-NLS-1$
        this.set("path", path); //$NON-NLS-1$
    }

    public JournalTreeModel(String id, String name, String path,String cls) {
        this(id, name,path);
        this.set("cls", cls); //$NON-NLS-1$
        this.set("path", path); //$NON-NLS-1$
    }

    public JournalTreeModel(String name, List<JournalTreeModel> list) {
        this(name);
        for (JournalTreeModel model : list) {
            add(model);
        }
    }

    public String getName() {
        return (String) get("name"); //$NON-NLS-1$
    }
    
    public String getPath() {
        return (String) get("path"); //$NON-NLS-1$
    }
    
    public String getCls() {
        return (String) get("cls"); //$NON-NLS-1$
    }
    
    public void addChild(JournalTreeModel model) {
        add(model);
    }
    
    public void addChildren(List<JournalTreeModel> list) {
        for (JournalTreeModel model : list) {
            add(model);
        }
    }
 
    public boolean isAuth() {
        return isAuth;
    }
    
    public void setAuth(boolean isAuth) {
        this.isAuth = isAuth;
    }
    
    public String getId(){
        return this.get("id"); //$NON-NLS-1$
    }
}