/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
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
import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.welcomeportal.client.resources.icon.Icons;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class AlertPortlet extends BasePortlet {

    public AlertPortlet(MainFramePanel portal) {
        super(PortletConstants.ALERT_NAME, portal);
        setIcon(AbstractImagePrototype.create(Icons.INSTANCE.alert()));
        setHeading(MessagesFactory.getMessages().alerts_title());
        label.setText(MessagesFactory.getMessages().loading_alert_msg());
        initConfigSettings();

        updateLinks();
        autoRefresh(configModel.isAutoRefresh());
    }

    @Override
    public void refresh() {
        updateLinks();
    }

    private void updateLinks() {
        label.setText(MessagesFactory.getMessages().no_alerts());
        fieldSet.setVisible(false);
    }
}
