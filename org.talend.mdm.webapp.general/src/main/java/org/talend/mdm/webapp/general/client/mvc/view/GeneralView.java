/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.general.client.mvc.view;

import org.talend.mdm.webapp.base.client.util.Cookies;
import org.talend.mdm.webapp.general.client.layout.AccordionMenus;
import org.talend.mdm.webapp.general.client.layout.BorderLayoutContainer;
import org.talend.mdm.webapp.general.client.layout.WorkSpace;
import org.talend.mdm.webapp.general.client.mvc.GeneralEvent;
import org.talend.mdm.webapp.general.model.MenuGroup;

import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.IncrementalCommand;
import com.google.gwt.user.client.ui.RootPanel;

public class GeneralView extends View {

    public static final String WELCOMECONTEXT = "welcomeportal", WELCOMEAPP = "WelcomePortal", WELCOMEID = "Welcome"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    public static final String DSCCONTEXT = "datastewardship", DSCAPP = "Datastewardship", DSCID = "tdscPanel"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    public GeneralView(Controller controller) {
        super(controller);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void handleEvent(AppEvent event) {
        EventType type = event.getType();
        if (type == GeneralEvent.InitFrame) {
            initFrame(event);
        } else if (type == GeneralEvent.LoadMenus) {
            AccordionMenus.getInstance().initMenus((MenuGroup) event.getData());
            Dispatcher dispatcher = Dispatcher.get();
            dispatcher.dispatch(GeneralEvent.LoadActions);
            if ((Boolean) Cookies.getValue("AccordionMenus")) { //$NON-NLS-1$
                AccordionMenus.getInstance().collapse();
            }
        } else if (type == GeneralEvent.LoadWelcome) {
            loadWelcome(event);
        }
    }

    private void loadWelcome(AppEvent event) {
        DeferredCommand.addCommand(new IncrementalCommand() {

            public boolean execute() {
                return !WorkSpace.getInstance().loadApp(WELCOMECONTEXT, WELCOMEAPP);
            }
        });
    }

    private void initFrame(AppEvent event) {
        BorderLayoutContainer mainLayout = new BorderLayoutContainer();
        RootPanel.get().add(mainLayout);
        Dispatcher dispatcher = Dispatcher.get();
        dispatcher.dispatch(GeneralEvent.LoadMenus);
        dispatcher.dispatch(GeneralEvent.LoadWelcome);
    }
}
