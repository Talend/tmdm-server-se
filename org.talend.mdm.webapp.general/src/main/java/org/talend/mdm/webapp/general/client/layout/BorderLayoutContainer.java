/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client.layout;

import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Element;

public class BorderLayoutContainer extends Viewport {

    Html messageHtml = new Html();

    private static final int DEFAULT_WEST_SIZE = 250;

    private static final int DEFAULT_EAST_SIZE = 250;

    private static final String WEST_WIDTH = "WEST_WIDTH"; //$NON-NLS-1$

    private static final String EAST_WIDTH = "EAST_WIDTH"; //$NON-NLS-1$

    @Override
    protected void onRender(Element target, int index) {
        super.onRender(target, index);
        final BorderLayout layout = new BorderLayout();
        setLayout(layout);
        this.setBorders(false);

        ContentPanel north = BrandingBar.getInstance();
        AccordionMenus west = AccordionMenus.getInstance();
        west.addListener(Events.Resize, new Listener<BaseEvent>() {

            public void handleEvent(BaseEvent be) {
                Cookies.setCookie(WEST_WIDTH, AccordionMenus.getInstance().getWidth() + ""); //$NON-NLS-1$
            }
        });
        WorkSpace center = WorkSpace.getInstance();
        center.setId("MDMCenterWorkspace"); //$NON-NLS-1$
        center.setBorders(false);

        ActionsPanel east = ActionsPanel.getInstance();
        east.addListener(Events.Resize, new Listener<BaseEvent>() {

            public void handleEvent(BaseEvent be) {
                Cookies.setCookie(EAST_WIDTH, ActionsPanel.getInstance().getWidth() + ""); //$NON-NLS-1$
            }
        });
        east.setBorders(false);
        
        BorderLayoutData northData = new BorderLayoutData(LayoutRegion.NORTH, 40);
        northData.setCollapsible(false);
        northData.setFloatable(false);
        northData.setHideCollapseTool(true);

        BorderLayoutData westData;
        String westWidth = Cookies.getCookie(WEST_WIDTH);
        if (westWidth != null && westWidth.trim().length() > 0) {
            westData = new BorderLayoutData(LayoutRegion.WEST, Integer.parseInt(westWidth));
        } else {
            westData = new BorderLayoutData(LayoutRegion.WEST, DEFAULT_WEST_SIZE);
        }

        westData.setMaxSize(300);
        westData.setSplit(true);
        westData.setCollapsible(true);
        westData.setFloatable(true);
        westData.setMargins(new Margins(0, 5, 0, 0));

        BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
        centerData.setMargins(new Margins(0));

        BorderLayoutData eastData;
        String eastWidth = Cookies.getCookie(EAST_WIDTH);
        if (eastWidth != null && eastWidth.trim().length() > 0) {
            eastData = new BorderLayoutData(LayoutRegion.EAST, Integer.parseInt(eastWidth));
        } else {
            eastData = new BorderLayoutData(LayoutRegion.EAST, DEFAULT_EAST_SIZE);
        }

        eastData.setMaxSize(300);
        eastData.setSplit(true);
        eastData.setCollapsible(true);
        eastData.setFloatable(false);
        eastData.setMargins(new Margins(0, 0, 0, 5));

        add(north, northData);
        add(west, westData);
        add(center, centerData);
        add(east, eastData);
        registerWorkingMessage();
        this.setId("MDMGeneral"); //$NON-NLS-1$
        this.getElement().getStyle().setPosition(Position.ABSOLUTE);
        this.getElement().getStyle().setTop(0D, Unit.PX);
        this.getElement().getStyle().setLeft(0D, Unit.PX);
    }

    public void setMessage(String message) {
        messageHtml.setHtml(message);
    }

    private static native void registerWorkingMessage()/*-{
        var instance = this;
        $wnd.working = function(message){
        instance.@org.talend.mdm.webapp.general.client.layout.BorderLayoutContainer::setMessage(Ljava/lang/String;)(message);
        };
    }-*/;
}
