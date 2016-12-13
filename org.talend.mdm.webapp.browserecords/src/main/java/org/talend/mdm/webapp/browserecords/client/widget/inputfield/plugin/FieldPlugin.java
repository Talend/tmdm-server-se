/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget.inputfield.plugin;

import org.talend.mdm.webapp.browserecords.client.resources.icon.Icons;

import com.extjs.gxt.ui.client.Style.HideMode;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ComponentHelper;
import com.extjs.gxt.ui.client.widget.ComponentPlugin;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.form.Field;
import com.google.gwt.user.client.ui.Image;


public class FieldPlugin implements ComponentPlugin {

    public interface Add_Remove_Listener {
        void onAdd(Field field);
        void onRemove(Field field);
    }
    
    Add_Remove_Listener listener;
    
    Field field;
    WidgetComponent add = new WidgetComponent(new Image(Icons.INSTANCE.drop_add()));
    WidgetComponent remove = new WidgetComponent(new Image(Icons.INSTANCE.drop_no()));
    
    boolean rendered;
    boolean validated = true;
    
    public void init(Component component) {
        field = (Field) component;
        initEvent();
        
        field.addListener(Events.Render, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                add.render(field.el().getParent().dom);
                remove.render(field.el().getParent().dom);
                
                add.setHideMode(HideMode.VISIBILITY);
                add.setStyleAttribute("display", "block");//$NON-NLS-1$ //$NON-NLS-2$
                add.el().makePositionable(true);
                
                remove.setHideMode(HideMode.VISIBILITY);
                remove.setStyleAttribute("display", "block");//$NON-NLS-1$ //$NON-NLS-2$
                remove.el().makePositionable(true);
                
                
                adjust();
                ComponentHelper.doAttach(add);
                ComponentHelper.doAttach(remove);
            }
        });
    }
    

    private void adjust(){
        int space = 2;
        if (!validated){
            space = 18;
        }
        add.el().alignTo(field.getElement(), "tl-tr", new int[] {space + 2, 3});//$NON-NLS-1$ 
        remove.el().alignTo(field.getElement(), "tl-tr", new int[] {space + 18, 3});//$NON-NLS-1$
    }
    
    private void initEvent(){
        add.addListener(Events.OnClick, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                if (listener != null){
                    listener.onAdd(field);
                }
                
            }
        });
        remove.addListener(Events.OnClick, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                if (listener != null){
                    listener.onRemove(field);
                }
            }
        });
    }
    
    public Add_Remove_Listener getListener() {
        return listener;
    }
    
    public void setListener(Add_Remove_Listener listener) {
        this.listener = listener;
    }
}
