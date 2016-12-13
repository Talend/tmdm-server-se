/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.browserecords.client.widget;

import java.util.Iterator;

import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class BreadCrumbBar extends Composite {

    HorizontalPanel hp = new HorizontalPanel();

    Label more = new Label();

    Menu menu = new Menu();

    HorizontalPanel crumbBar = new HorizontalPanel();

    public BreadCrumbBar() {
        more.setVisible(false);
        more.addStyleName("x-tool-left"); //$NON-NLS-1$
        more.addStyleName("x-tool"); //$NON-NLS-1$
        hp.add(more);
        hp.add(crumbBar);

        initEvent();
        initWidget(hp);
    }

    private void initEvent() {
        more.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                menu.show(more);
            }
        });
    }

    public void add(Widget w) {
        crumbBar.add(w);
        adjust();
    }

    public Widget getWidget(int index) {
        return crumbBar.getWidget(index);
    }

    public int getWidgetCount() {
        return crumbBar.getWidgetCount();
    }
    
    public int getWidgetIndex(Widget w) {
        return crumbBar.getWidgetIndex(w);
    }

    public void removeNeedless(Widget clickedHtml) {
        int index = getWidgetIndex(clickedHtml);
        if (index > -1) {
            while (getWidgetCount() - 1 > index) {
                crumbBar.remove(getWidgetCount() - 1);
            }
        }
        adjust();
    }

    public void adjust() {
        if (isAttached()) {
            Iterator<Widget> iter = crumbBar.iterator();
            while (iter.hasNext()) {
                Widget w = iter.next();
                w.setVisible(true);
            }
            menu.removeAll();

            int index = 0;
            Element parent = this.getElement().getParentElement().cast();
            while (this.getElement().getOffsetWidth() > parent.getOffsetWidth()) {
                final Widget w = crumbBar.getWidget(index);
                w.setVisible(false);
                MenuItem item = new MenuItem(w.getElement().getAttribute("titleText")); //$NON-NLS-1$
                item.addSelectionListener(new SelectionListener<MenuEvent>() {

                    public void componentSelected(MenuEvent ce) {
                        w.fireEvent(new ClickEvent() {
                        });
                    }
                });
                menu.insert(item, 0);
                index++;
            }
            if (index > 0) {
                more.setVisible(true);
            } else {
                more.setVisible(false);
            }
        }
    }
}
