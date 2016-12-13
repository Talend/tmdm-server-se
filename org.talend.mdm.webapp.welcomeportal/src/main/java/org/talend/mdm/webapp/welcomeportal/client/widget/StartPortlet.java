/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package org.talend.mdm.webapp.welcomeportal.client.widget;

import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortal;
import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.welcomeportal.client.resources.icon.Icons;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;

public class StartPortlet extends BasePortlet {

    public StartPortlet(MainFramePanel portal) {
        super(PortletConstants.START_NAME, portal);
        setIcon(AbstractImagePrototype.create(Icons.INSTANCE.start()));
        setHeading(MessagesFactory.getMessages().start_title());
        label.setText(MessagesFactory.getMessages().useful_links_desc());
        this.getHeader().removeTool(refreshBtn);
        initPortlet();
    }

    @Override
    public void refresh() {
        // same content, no need to refresh
        return;
    }

    private void initPortlet() {
        StringBuilder browseRecordsItem = new StringBuilder(
                "<span id=\"ItemsBrowser\" style=\"padding-right:8px;cursor: pointer; width:150;\" class=\"labelStyle\" title=\"" + MessagesFactory.getMessages().browse_items() + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        browseRecordsItem.append("<IMG SRC=\"/talendmdm/secure/img/menu/browse.png\"/>&nbsp;"); //$NON-NLS-1$
        browseRecordsItem.append(MessagesFactory.getMessages().browse_items());
        browseRecordsItem.append("</span>"); //$NON-NLS-1$
        HTML browseHtml = new HTML(browseRecordsItem.toString());
        browseHtml.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                portal.itemClick(WelcomePortal.BROWSECONTEXT, WelcomePortal.BROWSEAPP);
            }
        });
        fieldSet.add(browseHtml);
        StringBuilder journalItem = new StringBuilder(
                "<span id=\"Journal\" style=\"padding-right:8px;cursor: pointer; width:150;\" class=\"labelStyle\" title=\"" + MessagesFactory.getMessages().journal() + "\">"); //$NON-NLS-1$ //$NON-NLS-2$
        journalItem.append("<IMG SRC=\"/talendmdm/secure/img/menu/updatereport.png\"/>&nbsp;"); //$NON-NLS-1$
        journalItem.append(MessagesFactory.getMessages().journal());
        journalItem.append("</span>"); //$NON-NLS-1$
        HTML journalHtml = new HTML(journalItem.toString());
        journalHtml.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                portal.itemClick(WelcomePortal.JOURNALCONTEXT, WelcomePortal.JOURNALAPP);
            }
        });
        fieldSet.add(journalHtml);
        fieldSet.layout(true);
    }
}
