/*
 * Copyright (C) 2006-2018 Talend Inc. - www.talend.com
 *
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 *
 * You should have received a copy of the agreement
 * along with this program; if not, write to Talend SA
 * 9 rue Pages 92150 Suresnes, France
 */

package com.amalto.core.storage.task;

import org.springframework.context.ApplicationContext;

import com.amalto.core.server.MDMContextAccessor;


public class TaskSubmitterFactory {

    private static TaskSubmitter submitter;

    private TaskSubmitterFactory() {
    }

    public static TaskSubmitter getSubmitter() {
        synchronized(TaskSubmitterFactory.class){
            if(submitter == null){
                ApplicationContext context = MDMContextAccessor.getApplicationContext();
                if(context != null){
                    submitter = context.getBean(TaskSubmitter.class);
                }
                else {
                    submitter = new DefaultTaskSubmitter();
                }
            }
        }
        return submitter;
    }
}
