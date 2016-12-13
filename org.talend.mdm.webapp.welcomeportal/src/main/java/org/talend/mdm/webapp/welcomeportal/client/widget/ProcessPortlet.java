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

import java.util.HashMap;
import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.welcomeportal.client.resources.icon.Icons;

import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;

public class ProcessPortlet extends BasePortlet {

    private Map<String, String> processMap = new HashMap<String, String>();

    public ProcessPortlet(MainFramePanel portal) {
        super(PortletConstants.PROCESS_NAME, portal);
        setIcon(AbstractImagePrototype.create(Icons.INSTANCE.transformer()));
        setHeading(MessagesFactory.getMessages().process_title());
        initConfigSettings();
        label.setText(MessagesFactory.getMessages().process_desc());
        updateProcesses();
        autoRefresh(configModel.isAutoRefresh());
    }

    @Override
    public void refresh() {
        updateProcesses();
    }

    private void buildProcesses() {
        if (processMap.isEmpty()) {
            label.setText(MessagesFactory.getMessages().no_standalone_process());
            fieldSet.setVisible(false);
        } else {
            for (final String key : processMap.keySet()) {
                String description = processMap.get(key);
                HTML processHtml = new HTML();
                StringBuilder sb = new StringBuilder();
                sb.append("<span id=\"processes"); //$NON-NLS-1$
                sb.append(key);
                sb.append("\" style=\"padding-right:8px;cursor: pointer;\" class=\"labelStyle\">"); //$NON-NLS-1$
                sb.append("<IMG SRC=\"/talendmdm/secure/img/genericUI/runnable_bullet.png\"/>&nbsp;"); //$NON-NLS-1$
                sb.append(description != null ? description.replace("Runnable#", "") : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                sb.append("</span>"); //$NON-NLS-1$
                processHtml.setHTML(sb.toString());
                processHtml.addClickHandler(new ClickHandler() {

                    @Override
                    public void onClick(ClickEvent arg0) {
                        service.isExpired(UrlUtil.getLanguage(), new SessionAwareAsyncCallback<Boolean>() {

                            @Override
                            public void onSuccess(Boolean result1) {
                                final MessageBox box = MessageBox.wait(null, MessagesFactory.getMessages().waiting_msg(),
                                        MessagesFactory.getMessages().waiting_desc());
                                Timer t = new Timer() {

                                    @Override
                                    public void run() {
                                        box.close();
                                    }
                                };
                                t.schedule(600000);
                                service.runProcess(key, new SessionAwareAsyncCallback<String>() {

                                    @Override
                                    protected void doOnFailure(Throwable caught) {
                                        box.close();
                                        MessageBox.alert(MessagesFactory.getMessages().run_status(), MessagesFactory
                                                .getMessages().run_fail(), null);

                                    }

                                    @Override
                                    public void onSuccess(final String result2) {
                                        box.close();
                                        final MessageBox msgBox = new MessageBox();
                                        msgBox.setTitle(MessagesFactory.getMessages().run_status());
                                        msgBox.setMessage(MessagesFactory.getMessages().run_launched());
                                        msgBox.setButtons(""); //$NON-NLS-1$
                                        msgBox.setIcon(MessageBox.INFO);
                                        msgBox.show();
                                        Timer timer = new Timer() {

                                            public void run() {
                                                msgBox.close();
                                                if (result2.length() > 0) {
                                                    portal.openWindow(result2);
                                                }
                                            }
                                        };
                                        timer.schedule(700);
                                    }
                                });
                            }
                        });
                    }
                });
                fieldSet.add(processHtml);
                fieldSet.layout(true);

            }
        }
    }
    
    private void updateProcesses() {
        service.getStandaloneProcess(UrlUtil.getLanguage(), new SessionAwareAsyncCallback<Map<String, String>>() {

            @Override
            public void onSuccess(Map<String, String> newProcessMap) {
                if (!compareProcesses(newProcessMap)) {
                    processMap = newProcessMap;
                    fieldSet.removeAll();
                    buildProcesses();
                }
            }
        });

    }

    protected boolean compareProcesses(Map<String, String> newProcessMap) {
        boolean flag = true;
        if (processMap.size() == newProcessMap.size()) {
            for (String key : newProcessMap.keySet()) {
                if (!processMap.containsKey(key)) {
                    flag = false;
                    break;
                }
            }
        } else {
            flag = false;
        }
        return flag;
    }
}
