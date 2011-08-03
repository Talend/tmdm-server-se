// ============================================================================
//
// Copyright (C) 2006-2011 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.mdm.webapp.general.client.layout;

import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;

public class WorkSpace extends LayoutContainer {

    private static WorkSpace instance;

    private TabPanel workTabPanel = new TabPanel();

    private Map<String, JavaScriptObject> uiMap = new HashMap<String, JavaScriptObject>();

    private WorkSpace() {
        super();
        this.setLayout(new FitLayout());
        workTabPanel.setMinTabWidth(115);
        workTabPanel.setResizeTabs(true);
        workTabPanel.setAnimScroll(true);
        workTabPanel.setTabScroll(true);
        this.add(workTabPanel);
        initEvent();
    }


    public static WorkSpace getInstance() {
        if (instance == null) {
            instance = new WorkSpace();
        }
        return instance;
    }

    private void initEvent() {
        workTabPanel.addListener(Events.Resize, new Listener<BaseEvent>() {
            public void handleEvent(BaseEvent be) {
                resizeUIObjects();
            }
        });
    }

    public JavaScriptObject getItem(String itemId) {
        return uiMap.get(itemId);
    }


    public void addWorkTab(final String itemId, final JavaScriptObject uiObject) {

        TabItem item = workTabPanel.getItemByItemId(itemId);
        if (item == null) {
            item = new TabItem(itemId);

            item.addListener(Events.Select, new Listener<BaseEvent>() {
                public void handleEvent(BaseEvent be) {
                    resizeUIObjects();
                }
            });
            item.addListener(Events.Close, new Listener<BaseEvent>() {

                public void handleEvent(BaseEvent be) {
                    uiMap.remove(itemId);
                }
            });
            item.setItemId(itemId);
            item.setClosable(true);
            item.setLayout(new FitLayout());
            SimplePanel content = new SimplePanel() {

                protected void onLoad() {
                    renderUIObject(this.getElement(), uiObject);
                }
            };
            item.add(content);
            workTabPanel.add(item);
            uiMap.put(itemId, uiObject);
        }
        workTabPanel.setSelection(item);
    }

    private void resizeUIObjects() {
        TabItem item = workTabPanel.getSelectedItem();
        if (item != null) {
            JavaScriptObject uiObject = uiMap.get(item.getItemId());
            if (uiObject != null) {
                resizeUIObject(uiObject);
            }
        }
    }

    native void resizeUIObject(JavaScriptObject uiObject)/*-{
        var parentNode = uiObject.getEl().dom.parentNode;
        uiObject.setSize(parentNode.offsetWidth, parentNode.offsetHeight);
    }-*/;


    private native void renderUIObject(Element el, JavaScriptObject uiObject)/*-{
        uiObject.render(el);
        uiObject.setSize(el.offsetWidth, el.offsetHeight);
    }-*/;

    public void clearTabs(){
        workTabPanel.removeAll();
    }
}
