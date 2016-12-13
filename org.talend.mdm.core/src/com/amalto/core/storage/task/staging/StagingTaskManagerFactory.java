/*
 * Copyright (C) 2006-2016 Talend Inc. - www.talend.com
 * 
 * This source code is available under agreement available at
 * %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
 * 
 * You should have received a copy of the agreement along with this program; if not, write to Talend SA 9 rue Pages
 * 92150 Suresnes, France
 */
package com.amalto.core.storage.task.staging;

import javax.jms.ConnectionFactory;
import javax.jms.MessageListener;
import javax.jms.Topic;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.listener.DefaultMessageListenerContainer;
import org.talend.mdm.commmon.util.core.MDMConfiguration;


public class StagingTaskManagerFactory implements ApplicationContextAware, DisposableBean {
    
    private ApplicationContext applicationContext;
    
    private StagingTaskRepository repository;
    
    private DefaultMessageListenerContainer listener;
    
    public StagingTaskManager createStagingTaskManager(){
        if(isClusterEnabled()){
            ClusteredStagingTaskManager clusteredManager = new ClusteredStagingTaskManager();
            ConnectionFactory connectionFactory = this.getConnectionFactory();
            Topic topic = this.getTopic();
            clusteredManager.setJmsTemplate(createJmsTemplate(connectionFactory, topic));
            clusteredManager.setRepository(this.repository);
            this.startMessageListener(connectionFactory, topic, clusteredManager);
            return clusteredManager;
        }
        else {
            LocalStagingTaskManager localManagermanager = new LocalStagingTaskManager();
            localManagermanager.setRepository(this.repository);
            return localManagermanager;
        }
    }
    
    private void startMessageListener(ConnectionFactory connectionFactory, Topic topic, MessageListener l){
        listener = new DefaultMessageListenerContainer();
        listener.setSessionTransacted(false);
        listener.setConnectionFactory(connectionFactory);
        listener.setDestination(topic);
        listener.setMessageListener(l);
        listener.setConcurrentConsumers(1);
        listener.setMaxConcurrentConsumers(1);
        listener.setAutoStartup(false);
        listener.setAcceptMessagesWhileStopping(false);
        listener.afterPropertiesSet();
        listener.start();
    }
    
    private JmsTemplate createJmsTemplate(ConnectionFactory connectionFactory, Topic topic){
        
        JmsTemplate template = new JmsTemplate(connectionFactory);
        template.setDefaultDestination(this.getTopic());
        return template;
    }
    
    private ConnectionFactory getConnectionFactory(){
        return this.applicationContext.getBean("jmsConnectionFactory", ConnectionFactory.class);
    }
    
    private Topic getTopic(){
        return this.applicationContext.getBean("stagingTaskCancellationTopic", Topic.class);
    }
    
    private boolean isClusterEnabled(){
        return MDMConfiguration.isClusterEnabled();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    
    public StagingTaskRepository getRepository() {
        return repository;
    }

    
    public void setRepository(StagingTaskRepository repository) {
        this.repository = repository;
    }

    @Override
    public void destroy() throws Exception {
        if(this.listener != null && this.listener.isRunning()){
            this.listener.shutdown();
        }
    }

}
