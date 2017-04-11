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
import org.talend.mdm.webapp.base.client.i18n.BaseMessagesFactory;
import org.talend.mdm.webapp.base.client.util.UrlUtil;
import org.talend.mdm.webapp.base.client.widget.PortletConstants;
import org.talend.mdm.webapp.base.shared.AppHeader;
import org.talend.mdm.webapp.base.shared.Constants;
import org.talend.mdm.webapp.welcomeportal.client.MainFramePanel;
import org.talend.mdm.webapp.welcomeportal.client.WelcomePortal;
import org.talend.mdm.webapp.welcomeportal.client.i18n.MessagesFactory;
import org.talend.mdm.webapp.welcomeportal.client.resources.icon.Icons;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.util.Format;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.HTML;

public class TaskPortlet extends BasePortlet {

    private AppHeader header = (AppHeader) Registry.get(WelcomePortal.APP_HEADER);
    
    private final String TASK_AMOUNT = "amount";

    private String tdsServiceBaseUrl = GWT.getHostPageBaseURL() + "services/rest/tds/";

    private static String DSCTASKTYPE_NEW = "new"; //$NON-NLS-1$

    private static String DSCTASKTYPE_PENDING = "pending"; //$NON-NLS-1$

    private static String WORKFLOWTASKS_PREFIX = "<span id=\"workflowtasks\" style=\"padding-right:8px;cursor: pointer;\" class=\"labelStyle\" title=\"" + MessagesFactory.getMessages().tasks_title() + "\">"; //$NON-NLS-1$ //$NON-NLS-2$

    private static String DSCTASKS_PREFIX = "<span id=\"dsctasks\" style=\"padding-right:8px;cursor: pointer;\" class=\"labelStyle\" title=\"" + MessagesFactory.getMessages().tasks_title() + "\">"; //$NON-NLS-1$ //$NON-NLS-2$

    private boolean isHiddenWorkFlowTask = true;

    private boolean isHiddenTask = true;

    private enum TASK_TYPE {
        WORKFLOW_TYPE,
        DSC_TYPE,
        TDS_TYPE
    };

    private Integer task_New_Count;

    private Integer task_Pending_Count;

    private Integer workflowTask_Count;

    private ClickHandler workflowClikcHanlder;

    private ClickHandler dscClikcHanlder;

    private ClickHandler tdsClikcHanlder;

    public TaskPortlet(final MainFramePanel portal) {
        super(PortletConstants.TASKS_NAME, portal);
        setIcon(AbstractImagePrototype.create(Icons.INSTANCE.task()));
        setHeading(MessagesFactory.getMessages().tasks_title());
        isHiddenWorkFlowTask = portal.isHiddenWorkFlowTask();
        isHiddenTask = portal.isHiddenTask();
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

        tdsClikcHanlder = new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                UrlUtil.openSingleWindow(header.getTdsBaseUrl() + "/#/mytasks", Constants.TDS_NAME);
            }
        };
    }

    @Override
    public void refresh() {
        updateTaskes();
    }

    private void updateTaskes() {
        if (!isHiddenWorkFlowTask && isHiddenTask) {
            service.getWorkflowTaskMsg(new SessionAwareAsyncCallback<Integer>() {

                @Override
                public void onSuccess(Integer workflowTaskCount) {
                    if (workflowTaskCount != null) {
                        if (workflowTask_Count == null || workflowTask_Count != workflowTaskCount) {
                            workflowTask_Count = workflowTaskCount;
                            updateTaskPanel(workflowTask_Count, null, 0, 0);
                        }
                    }
                }
            });
        }

        if (!isHiddenTask && isHiddenWorkFlowTask) {
            if (header.isTdsEnabled()) {
                service.getCurrentDataModel(new SessionAwareAsyncCallback<String>() {

                    @Override
                    public void onSuccess(String dataModel) {
                        String url = tdsServiceBaseUrl + TASK_AMOUNT + "?model=" + dataModel;
                        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
                        try {
                            builder.sendRequest("", new RequestCallback() {
                                @Override
                                public void onResponseReceived(Request request, Response response) {
                                    if (Response.SC_OK == response.getStatusCode()) {
                                        int taskCount = handleTaskResult(response.getText());
                                        if (task_New_Count == null || task_New_Count != taskCount) {
                                            task_New_Count = taskCount;
                                            updateTaskPanel(0, TASK_TYPE.TDS_TYPE, task_New_Count, 0);
                                        }
                                    } else if (Response.SC_INTERNAL_SERVER_ERROR == response.getStatusCode()) {
                                        int taskCount = handleTaskResult("");
                                        if (task_New_Count == null) {
                                            updateTaskPanel(0, TASK_TYPE.TDS_TYPE, taskCount, 0);
                                        }
                                    }
                                }

                                @Override
                                public void onError(Request request, Throwable exception) {
                                    handleServiceException(exception);

                                }

                            });
                        } catch (RequestException exception) {
                            handleServiceException(exception);
                        }
                    }
                });
            } else {
                service.getDSCTaskMsg(new SessionAwareAsyncCallback<Map<String, Integer>>() {

                    @Override
                    public void onSuccess(Map<String, Integer> dscTasksMap) {
                        if (dscTasksMap.get(DSCTASKTYPE_NEW) != null && dscTasksMap.get(DSCTASKTYPE_PENDING) != null) {
                            if ((task_New_Count == null || task_New_Count != dscTasksMap.get(DSCTASKTYPE_NEW))
                                    || (task_Pending_Count == null || task_Pending_Count != dscTasksMap.get(DSCTASKTYPE_PENDING))) {
                                task_New_Count = dscTasksMap.get(DSCTASKTYPE_NEW);
                                task_Pending_Count = dscTasksMap.get(DSCTASKTYPE_PENDING);
                                updateTaskPanel(0, TASK_TYPE.DSC_TYPE, task_New_Count, task_Pending_Count);
                            }
                        }
                    }
                });
            }
        }

        if (!isHiddenWorkFlowTask && !isHiddenTask) {
            service.getWorkflowTaskMsg(new SessionAwareAsyncCallback<Integer>() {

                @Override
                public void onSuccess(final Integer workflowTaskCount) {
                    final boolean workflowTaskChanged = workflowTaskCount != null
                            && (workflowTask_Count == null || workflowTask_Count != workflowTaskCount);

                    if (header.isTdsEnabled()) {
                        service.getCurrentDataModel(new SessionAwareAsyncCallback<String>() {

                            @Override
                            public void onSuccess(String dataModel) {
                                String url = tdsServiceBaseUrl + TASK_AMOUNT + "?model=" + dataModel;
                                RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);
                                try {
                                    builder.sendRequest("", new RequestCallback() {

                                        @Override
                                        public void onResponseReceived(Request request, Response response) {
                                            if (Response.SC_OK == response.getStatusCode()) {
                                                int taskCount = handleTaskResult(response.getText());
                                                boolean taskChanged = task_New_Count == null || task_New_Count != taskCount;
                                                if (workflowTaskChanged || taskChanged) {
                                                    workflowTask_Count = workflowTaskCount;
                                                    task_New_Count = taskCount;
                                                    updateTaskPanel(workflowTask_Count, TASK_TYPE.TDS_TYPE, task_New_Count, 0);
                                                }
                                            } else if (Response.SC_INTERNAL_SERVER_ERROR == response.getStatusCode()) {
                                                int taskCount = handleTaskResult("");
                                                if (workflowTaskChanged) {
                                                    updateTaskPanel(workflowTask_Count, TASK_TYPE.TDS_TYPE, taskCount, 0);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onError(Request request, Throwable exception) {
                                            handleServiceException(exception);
                                        }

                                    });
                                } catch (RequestException exception) {
                                    handleServiceException(exception);
                                }
                            }
                        });
                    } else {
                        service.getDSCTaskMsg(new SessionAwareAsyncCallback<Map<String, Integer>>() {

                            @Override
                            public void onSuccess(Map<String, Integer> dscTasksMap) {
                                boolean taskChanged = (dscTasksMap.get(DSCTASKTYPE_NEW) != null
                                        && (task_New_Count == null || task_New_Count != dscTasksMap
                                        .get(DSCTASKTYPE_NEW))
                                        || (dscTasksMap.get(DSCTASKTYPE_NEW) != null && task_Pending_Count != dscTasksMap
                                        .get(DSCTASKTYPE_PENDING)));
                                if (workflowTaskChanged || taskChanged) {
                                    workflowTask_Count = workflowTaskCount;
                                    task_New_Count = dscTasksMap.get(DSCTASKTYPE_NEW);
                                    task_Pending_Count = dscTasksMap.get(DSCTASKTYPE_PENDING);
                                    updateTaskPanel(workflowTask_Count, TASK_TYPE.DSC_TYPE, task_New_Count, task_Pending_Count);
                                }
                            }
                        });
                    }
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
        } else if (type == TASK_TYPE.DSC_TYPE) {
            taskStringBuilder = new StringBuilder(DSCTASKS_PREFIX);
            countString = buildMessage(MessagesFactory.getMessages().waiting_dsctask(count1, count2), MessagesFactory
                    .getMessages().waiting_dsctask_suffix());
            taskHtml.addClickHandler(dscClikcHanlder);
        } else if (type == TASK_TYPE.TDS_TYPE) {
            taskStringBuilder = new StringBuilder(DSCTASKS_PREFIX);
            countString = buildMessage(MessagesFactory.getMessages().waiting_task(count1), MessagesFactory.getMessages()
                    .waiting_dsctask_suffix());
            taskHtml.addClickHandler(tdsClikcHanlder);
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

    private void updateTaskPanel(int workflowTaskCount, TASK_TYPE taskType, int taskCount1, int taskCount2) {
        if ((workflowTaskCount + taskCount1 + taskCount2) == 0) {
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
            if (taskCount1 + taskCount2 > 0) {
                task_New_Count = taskCount1;
                task_Pending_Count = taskCount2;
                HTML taskHtml = buildTaskHTML(taskType, task_New_Count, task_Pending_Count);
                if (taskHtml != null) {
                    fieldSet.add(taskHtml);
                }
            }
        }
        fieldSet.layout(true);
    }

    private void handleServiceException(Throwable caught) {
        String errorMsg = caught.getLocalizedMessage();
        if (Log.isDebugEnabled()) {
            errorMsg = caught.toString();
        } else {
            errorMsg = BaseMessagesFactory.getMessages().unknown_error();
        }
        errorMsg = Format.htmlEncode(errorMsg);
        MessageBox.alert(BaseMessagesFactory.getMessages().error_title(), errorMsg, null);
    }

    private int handleTaskResult(String result) {
        try {
            Integer taskCount = Integer.valueOf(result);
            return taskCount.intValue();
        } catch (NumberFormatException exception) {
            if ("authentication_failure".equals(result)) {
                MessageBox.alert(BaseMessagesFactory.getMessages().warning_title(), MessagesFactory.getMessages()
                        .login_tds_fail(), null);
            } else if ("role_missing".equals(result)) {
                MessageBox.info(BaseMessagesFactory.getMessages().info_title(), MessagesFactory.getMessages()
                        .retrieve_campaign_fail(), null);
            } else {
                MessageBox.alert(BaseMessagesFactory.getMessages().error_title(), BaseMessagesFactory.getMessages()
                        .unknown_error(), null);
            }
            return 0;
        }
    }
}
