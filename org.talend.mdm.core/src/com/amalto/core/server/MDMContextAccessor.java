/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class MDMContextAccessor implements ApplicationContextAware {

    private static ApplicationContext CONTEXT;
    
    private static Log LOG = LogFactory.getLog(MDMContextAccessor.class);
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        synchronized(MDMContextAccessor.class){
            if(CONTEXT != null){
                LOG.info("Overriding current Spring context with a new one"); //$NON-NLS-1$
            }
            CONTEXT = applicationContext;
        }
    }
    
    public static ApplicationContext getApplicationContext(){
        return CONTEXT;
    }

}
