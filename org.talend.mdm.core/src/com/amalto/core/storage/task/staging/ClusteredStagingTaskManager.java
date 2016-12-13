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

import java.io.StringReader;
import java.io.StringWriter;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

/**
 * Clustered ready implementation of {@link StagingTaskManager} that supports situations where
 * a task is run in a different node that the current one: it queries the underlying storage 
 * to get current running task if there is no task running locally. Also sends a JMS message on a topic
 * for tasks's cancellation with this task is not running in the local node.
 */
public class ClusteredStagingTaskManager extends LocalStagingTaskManager implements MessageListener {
    
    private static final Logger LOGGER = Logger.getLogger(ClusteredStagingTaskManager.class);
    
    private JmsTemplate jmsTemplate;
    
    @Override
    public String getCurrentTaskId(String dataContainer) {
        // let's first check this node runs a task locally 
        String result = super.getCurrentTaskId(dataContainer);
        // if not found search in database
        if(result == null){
            result = this.getRepository().getCurrentTaskId(dataContainer);
        }
        return result;
    }

    @Override
    public void cancelTask(String dataContainer, String taskId) {
        this.cancelTask(dataContainer, taskId, true);
    }
    
    protected void cancelTask(String dataContainer, String taskId, boolean sendJmsMessage){
        String localTaskId = super.getCurrentTaskId(dataContainer);
        if(localTaskId != null && localTaskId.equals(taskId)){
            // simply cancel the task as it is local
            super.cancelTask(dataContainer, localTaskId);
        }
        else if(sendJmsMessage){
            // send JMS message to the cluster to ask for cancellation
            this.sendCancellationMessage(dataContainer, taskId);
        }
        else {
            // this node is not concerned by this message
        }
    }
    
    protected void sendCancellationMessage(final String dataContainer, final String taskId){
        jmsTemplate.send(new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                TextMessage message = session.createTextMessage();
                try {
                    String msg = StagingJobCancellationMessage.toString(new StagingJobCancellationMessage(dataContainer, taskId));
                    message.setText(msg);
                    return message;
                } catch (JAXBException e) {
                    LOGGER.error("Cannot unmarshall message", e);
                    throw new JMSException("Cannot unmarshall message");
                }
            }
        });
    }

    @Override
    public void onMessage(Message message) {
        if(message instanceof TextMessage){
            TextMessage txtMessage = (TextMessage)message;
            String txt = StringUtils.EMPTY;
            try {
                txt = txtMessage.getText();
                StagingJobCancellationMessage cancellationMessage = StagingJobCancellationMessage.fromString(txt);
                if(cancellationMessage != null && cancellationMessage.getDataContainer() != null && cancellationMessage.getTaskId() != null){
                    this.cancelTask(cancellationMessage.getDataContainer(), cancellationMessage.getTaskId(), false);
                }
                else {
                    LOGGER.error("Received an invalid JMS cancellation message " + txt + " forgetting it.");
                }
            } catch (JAXBException e) {
                LOGGER.error("Received an unparsable JMS text message [" + txt + "] forgetting it.");
            } catch (JMSException e) {
                LOGGER.error("JMS Exception when reading message. Resending it", e);
                throw new RuntimeException(e);
            }
        }
        else {
            LOGGER.error("Received an unsupported JMS message " + message + " forgetting it.");
        }
    }
    
    public JmsTemplate getJmsTemplate() {
        return jmsTemplate;
    }

    
    public void setJmsTemplate(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }
    
    /**
     * Internal message sent on the JMS topic for cancellation.  
     */
    @XmlRootElement(name="stagingJobCancellationMessage")
    @XmlAccessorType(XmlAccessType.FIELD)
    static class StagingJobCancellationMessage {
        
        @XmlElement(name="dataContainer")
        private String dataContainer;
        
        @XmlElement(name="taskId")
        private String taskId;
        
        private static final JAXBContext JAXBCONTEXT;
        
        static {
            try {
                JAXBCONTEXT = JAXBContext.newInstance(StagingJobCancellationMessage.class);
            } catch (JAXBException e) {
                throw new RuntimeException("Cannot instanciate JAXBContext", e);
            }
        }
        
        @SuppressWarnings("unused")
        public StagingJobCancellationMessage(){
            
        }
        
        public StagingJobCancellationMessage(String dataContainer, String taskId){
            this.dataContainer = dataContainer;
            this.taskId = taskId;
        }
        
        public String getDataContainer() {
            return dataContainer;
        }
        
        @SuppressWarnings("unused")
        public void setDataContainer(String dataContainer) {
            this.dataContainer = dataContainer;
        }
        
        public String getTaskId() {
            return taskId;
        }
        
        @SuppressWarnings("unused")
        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }
        
        public static StagingJobCancellationMessage fromString(final String content) throws JAXBException{
            JAXBElement<StagingJobCancellationMessage> element = JAXBCONTEXT.createUnmarshaller().unmarshal(new StreamSource(new StringReader(content)), StagingJobCancellationMessage.class);
            return element.getValue();
        }
        
        public static String toString(StagingJobCancellationMessage message) throws JAXBException{
            StringWriter writer = new StringWriter();
            JAXBCONTEXT.createMarshaller().marshal(message, writer);
            return writer.toString();
        }
    }

}
