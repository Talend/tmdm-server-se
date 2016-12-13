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

import java.util.Map;

import org.talend.mdm.webapp.base.client.SessionAwareAsyncCallback;
import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortal;
import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.welcomeportal.client.resources.icon.Icons;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;

public class TaskPortlet extends BasePortlet {

    private static String DSCTASKTYPE_NEW = "new"; //$NON-NLS-1$

    private static String DSCTASKTYPE_PENDING = "pending"; //$NON-NLS-1$

    private static String WORKFLOWTASKS_PREFIX = "<span id=\"workflowtasks\" style=\"padding-right:8px;cursor: pointer;\" class=\"labelStyle\" title=\"" + MessagesFactory.getMessages().tasks_title() + "\">"; //$NON-NLS-1$ //$NON-NLS-2$

    private static String DSCTASKS_PREFIX = "<span id=\"dsctasks\" style=\"padding-right:8px;cursor: pointer;\" class=\"labelStyle\" title=\"" + MessagesFactory.getMessages().tasks_title() + "\">"; //$NON-NLS-1$ //$NON-NLS-2$

    private boolean isHiddenWorkFlowTask = true;

    private boolean isHiddenDSCTask = true;

    private enum TASK_TYPE {
        WORKFLOW_TYPE,
        DES_TYPE
    };

    private Integer dscTask_New_Count;

    private Integer dscTask_Pending_Count;

    private Integer workflowTask_Count;

    private ClickHandler workflowClikcHanlder;

    private ClickHandler dscClikcHanlder;

    public TaskPortlet(final MainFramePanel portal) {
        super(PortletConstants.TASKS_NAME, portal);
        setIcon(AbstractImagePrototype.create(Icons.INSTANCE.task()));
        setHeading(MessagesFactory.getMessages().tasks_title());
        isHiddenWorkFlowTask = portal.isHiddenWorkFlowTask();
        isHiddenDSCTask = portal.isHiddenDSCTask();
        initConfigSettings();
        label.setText(MessagesFactory.getMessages().loading_task_msg());
        updateTaskes();
        autoRefresh(configModel.isAutoRefresh());

        workflowClikcHanlder = new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                portal.itemClick(WelcomePortal.WORKFLOW_TASKCONTEXT, WelcomePortal.WORKFLOW_TASKAPP);
            }
        };

        dscClikcHanlder = new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                portal.itemClick(WelcomePortal.DSC_TASKCONTEXT, WelcomePortal.DSC_TASKAPP);
            }
        };
    }

    @Override
    public void refresh() {
        updateTaskes();
    }

    private void updateTaskes() {

        if (!isHiddenWorkFlowTask && isHiddenDSCTask) {
            service.getWorkflowTaskMsg(new SessionAwareAsyncCallback<Integer>() {

                @Override
                public void onSuccess(Integer workflowTaskCount) {
                    if (workflowTaskCount != null && workflowTaskCount != workflowTask_Count) {
                        workflowTask_Count = workflowTaskCount;
                        if (workflowTask_Count == 0) {
                            label.setText(MessagesFactory.getMessages().no_tasks());
                            fieldSet.setVisible(false);
                        } else {
                            HTML taskHtml = buildTaskHTML(TASK_TYPE.WORKFLOW_TYPE, workflowTaskCount, 0);
                            if (taskHtml != null) {
                                fieldSet.removeAll();
                                fieldSet.add(taskHtml);
                            }
                        }
                        fieldSet.layout(true);
                    }
                }
            });
        }

        if (!isHiddenDSCTask && isHiddenWorkFlowTask) {
            service.getDSCTaskMsg(new SessionAwareAsyncCallback<Map<String, Integer>>() {

                @Override
                public void onSuccess(Map<String, Integer> dscTasksMap) {
                    if (dscTasksMap.get(DSCTASKTYPE_NEW) != null && dscTasksMap.get(DSCTASKTYPE_PENDING) != null) {
                        if (dscTasksMap.get(DSCTASKTYPE_NEW) != dscTask_New_Count
                                || dscTasksMap.get(DSCTASKTYPE_PENDING) != dscTask_Pending_Count) {
                            dscTask_New_Count = dscTasksMap.get(DSCTASKTYPE_NEW);
                            dscTask_Pending_Count = dscTasksMap.get(DSCTASKTYPE_PENDING);
                            if ((dscTask_New_Count + dscTask_Pending_Count) == 0) {
                                label.setText(MessagesFactory.getMessages().no_tasks());
                                fieldSet.setVisible(false);
                            } else {
                                HTML taskHtml = buildTaskHTML(TASK_TYPE.DES_TYPE, dscTask_New_Count, dscTask_Pending_Count);
                                if (taskHtml != null) {
                                    fieldSet.removeAll();
                                    fieldSet.add(taskHtml);
                                }
                            }
                            fieldSet.layout(true);
                        }
                    }
                }
            });
        }

        if (!isHiddenWorkFlowTask && !isHiddenDSCTask) {
            service.getWorkflowTaskMsg(new SessionAwareAsyncCallback<Integer>() {

                @Override
                public void onSuccess(final Integer workflowTaskCount) {

                    service.getDSCTaskMsg(new SessionAwareAsyncCallback<Map<String, Integer>>() {

                        @Override
                        public void onSuccess(Map<String, Integer> dscTasksMap) {

                            if (workflowTaskCount != null && dscTasksMap.get(DSCTASKTYPE_NEW) != null
                                    && dscTasksMap.get(DSCTASKTYPE_PENDING) != null) {
                                boolean workflowTaskChanged = workflowTaskCount != workflowTask_Count;
                                boolean dscTasksChanged = (dscTasksMap.get(DSCTASKTYPE_NEW) != dscTask_New_Count || dscTasksMap
                                        .get(DSCTASKTYPE_PENDING) != dscTask_Pending_Count);
                                if (workflowTaskChanged || dscTasksChanged) {
                                    if ((workflowTaskCount + dscTasksMap.get(DSCTASKTYPE_NEW) + dscTasksMap
                                            .get(DSCTASKTYPE_PENDING)) == 0) {
                                        label.setText(MessagesFactory.getMessages().no_tasks());
                                        fieldSet.setVisible(false);
                                    } else {
                                        fieldSet.removeAll();
                                        if (workflowTaskCount > 0) {
                                            workflowTask_Count = workflowTaskCount;
                                            HTML taskHtml = buildTaskHTML(TASK_TYPE.WORKFLOW_TYPE, workflowTask_Count, 0);
                                            if (taskHtml != null) {
                                                fieldSet.add(taskHtml);
                                            }
                                        }
                                        if ((dscTasksMap.get(DSCTASKTYPE_NEW) + dscTasksMap.get(DSCTASKTYPE_PENDING)) > 0) {
                                            dscTask_New_Count = dscTasksMap.get(DSCTASKTYPE_NEW);
                                            dscTask_Pending_Count = dscTasksMap.get(DSCTASKTYPE_PENDING);
                                            HTML taskHtml = buildTaskHTML(TASK_TYPE.DES_TYPE, dscTask_New_Count,
                                                    dscTask_Pending_Count);
                                            if (taskHtml != null) {
                                                fieldSet.add(taskHtml);
                                            }
                                        }
                                    }
                                    fieldSet.layout(true);
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    private HTML buildTaskHTML(TASK_TYPE type, Integer count1, Integer count2) {
        HTML taskHtml = new HTML();
        StringBuilder taskStringBuilder;
        String countString;
        if (type == TASK_TYPE.WORKFLOW_TYPE) {
            taskStringBuilder = new StringBuilder(WORKFLOWTASKS_PREFIX);
            countString = buildMessage(String.valueOf(count1), MessagesFactory.getMessages().waiting_workflowtask_suffix());
            taskHtml.addClickHandler(workflowClikcHanlder);
        } else if (type == TASK_TYPE.DES_TYPE) {
            taskStringBuilder = new StringBuilder(DSCTASKS_PREFIX);
            countString = buildMessage(MessagesFactory.getMessages().waiting_dsctask(count1, count2), MessagesFactory
                    .getMessages().waiting_dsctask_suffix());
            taskHtml.addClickHandler(dscClikcHanlder);
        } else {
            return null;
        }
        taskStringBuilder.append(countString);
        label.setText(MessagesFactory.getMessages().tasks_desc());
        fieldSet.setVisible(true);
        taskStringBuilder.append("</span>"); //$NON-NLS-1$
        taskHtml.setHTML(taskStringBuilder.toString());
        return taskHtml;
    }

    private String buildMessage(String countString, String task_suffix) {
        StringBuilder message = new StringBuilder("<IMG SRC=\"secure/img/genericUI/task-list-icon.png\"/>&nbsp;"); //$NON-NLS-1$
        message.append(MessagesFactory.getMessages().waiting_task_prefix());
        message.append("&nbsp;<b style=\"color: red;\">"); //$NON-NLS-1$
        message.append(countString);
        message.append("</b>&nbsp;"); //$NON-NLS-1$
        message.append(task_suffix);
        return message.toString();
    }
}
