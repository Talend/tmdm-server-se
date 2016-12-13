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

import java.util.ArrayList;
import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.jms.core.JmsTemplate;

import com.amalto.core.storage.task.StagingTask;
import com.amalto.core.storage.task.staging.ClusteredStagingTaskManager.StagingJobCancellationMessage;


public class ClusteredStagingTaskManagerTestCase extends AbstractStagingTaskManagerTastCase {
    
    private ClusteredStagingTaskManager taskManager;
    
    private StagingTaskRepository repository;
    
    private ActiveMQConnectionFactory connectionFactory;
    
    private JmsTemplate jmsTemplate;
    
    private Topic testTopic;
    
    private InternalListener internalListener;
    
    @Before
    public void setUp() throws Exception {
        connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");
        testTopic = new ActiveMQTopic("testTopic");
        jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setDefaultDestination(testTopic);
        repository = Mockito.mock(StagingTaskRepository.class);
        taskManager = new ClusteredStagingTaskManager();
        taskManager.setRepository(repository);
        taskManager.setJmsTemplate(jmsTemplate);
        this.internalListener = new InternalListener(this.connectionFactory, this.testTopic);
        this.internalListener.startListening();
    }
    
    @After
    public void tearDown() throws Exception {
        this.internalListener.stopListening();
        connectionFactory = null;
    }
    
    @Test
    public void testNoCurrentTask() throws Exception {
        String container = "container";
        // no registered execution at all
        Assert.assertNull(taskManager.getCurrentTaskId(container));
        Mockito.verify(repository).getCurrentTaskId(container);
        Mockito.verifyNoMoreInteractions(repository);
        this.internalListener.assertNoMessageSent();
    }
    
    @Test
    public void testCurrentTaskFromStorage() throws Exception {
        String container = "container";
        String taskId = "ABCD";
        // no registered execution at all
        Assert.assertNull(taskManager.getCurrentTaskId(container));
        // current execution from repository
        Mockito.when(repository.getCurrentTaskId(container)).thenReturn(taskId);
        Assert.assertEquals(taskId, taskManager.getCurrentTaskId(container));
        this.internalListener.assertNoMessageSent();
    }
    
    @Test
    public void testCurrentTaskFromLocal() throws Exception {
        String container = "container";
        String taskId = "ABCD";
        long startDate = System.currentTimeMillis();
        StagingTask task = createTask(container, taskId, startDate);
        taskManager.taskStarted(task);
        Mockito.verify(repository).saveNewTask(container, taskId, startDate);
        Assert.assertEquals(taskId, taskManager.getCurrentTaskId(container));
        Mockito.verifyNoMoreInteractions(repository);
        this.internalListener.assertNoMessageSent();
    }
    
    @Test
    public void testCancelLocalTask() throws Exception {
        String container = "container";
        String taskId = "ABCD";
        long startDate = System.currentTimeMillis();
        StagingTask task = createTask(container, taskId, startDate);
        taskManager.taskStarted(task);
        taskManager.cancelTask(container, taskId);
        Mockito.verify(task).cancel();
        Assert.assertEquals(taskId, taskManager.getCurrentTaskId(container));
        this.internalListener.assertNoMessageSent();
    }
    
    @Test
    public void testCancelRemoteTask() throws Exception {
        String container = "container";
        String taskId = "ABCD";
        Mockito.when(repository.getCurrentTaskId(container)).thenReturn(taskId);
        taskManager.cancelTask(container, taskId);
        Assert.assertEquals(taskId, taskManager.getCurrentTaskId(container));
        this.internalListener.assertOneMessageSent();
        String receivedMessageText = this.internalListener.getLastMessageReceived();
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><stagingJobCancellationMessage><dataContainer>container</dataContainer><taskId>ABCD</taskId></stagingJobCancellationMessage>", receivedMessageText);
        StagingJobCancellationMessage receivedMessage = StagingJobCancellationMessage.fromString(receivedMessageText);
        Assert.assertNotNull(receivedMessage);
        Assert.assertEquals(taskId, receivedMessage.getTaskId());
        Assert.assertEquals(container, receivedMessage.getDataContainer());
    }
    
    @Test
    public void testReceiveEmptyMessage() throws Exception {
        TextMessage message = Mockito.mock(TextMessage.class);
        Mockito.when(message.getText()).thenReturn(StringUtils.EMPTY);
        taskManager.onMessage(message);
        Mockito.verifyNoMoreInteractions(this.repository);
        this.internalListener.assertNoMessageSent();
    }
    
    @Test
    public void testReceiveNotTextMessage() throws Exception {
        ObjectMessage message = Mockito.mock(ObjectMessage.class);
        taskManager.onMessage(message);
        Mockito.verifyNoMoreInteractions(this.repository);
        this.internalListener.assertNoMessageSent();
    }
    
    @Test
    public void testReceiveUnparsableTextMessage() throws Exception {
        TextMessage message = Mockito.mock(TextMessage.class);
        Mockito.when(message.getText()).thenReturn("THIS IS NOT XML");
        taskManager.onMessage(message);
        Mockito.verifyNoMoreInteractions(this.repository);
        this.internalListener.assertNoMessageSent();
    }
    
    @Test
    public void testReceiveMessageForLocalTask() throws Exception {
        String container = "container";
        String taskId = "ABCD";
        long startDate = System.currentTimeMillis();
        StagingTask task = createTask(container, taskId, startDate);
        taskManager.taskStarted(task);
        TextMessage message = Mockito.mock(TextMessage.class);
        Mockito.when(message.getText()).thenReturn("<?xml version=\"1.0\"?><stagingJobCancellationMessage><dataContainer>"+ container + "</dataContainer><taskId>"+taskId+"</taskId></stagingJobCancellationMessage>");
        taskManager.onMessage(message);
        Mockito.verify(task).cancel();
        this.internalListener.assertNoMessageSent();
    }
    
    @Test
    public void testReceiveMessageForUnknownTask() throws Exception {
        TextMessage message = Mockito.mock(TextMessage.class);
        Mockito.when(message.getText()).thenReturn("<?xml version=\"1.0\"?><stagingJobCancellationMessage><dataContainer>container</dataContainer><taskId>1234</taskId></stagingJobCancellationMessage>");
        taskManager.onMessage(message);
        Mockito.verifyNoMoreInteractions(this.repository);
        this.internalListener.assertNoMessageSent();
    }
    
    @Test
    public void testReceiveMessageForEmptyInfo() throws Exception {
        TextMessage message = Mockito.mock(TextMessage.class);
        Mockito.when(message.getText()).thenReturn("<?xml version=\"1.0\"?><stagingJobCancellationMessage><dataContainer>container</dataContainer<taskId/></stagingJobCancellationMessage>");
        taskManager.onMessage(message);
        Mockito.verifyNoMoreInteractions(this.repository);
        this.internalListener.assertNoMessageSent();
        
        Mockito.when(message.getText()).thenReturn("<?xml version=\"1.0\"?><stagingJobCancellationMessage><dataContainer/><taskId>ABCD</taskId></stagingJobCancellationMessage>");
        taskManager.onMessage(message);
        Mockito.verifyNoMoreInteractions(this.repository);
        this.internalListener.assertNoMessageSent();
        
        Mockito.when(message.getText()).thenReturn("<?xml version=\"1.0\"?><toto></toto>");
        taskManager.onMessage(message);
        Mockito.verifyNoMoreInteractions(this.repository);
        this.internalListener.assertNoMessageSent();
    }
    
    @Test(expected=RuntimeException.class)
    public void testExceptionWhenReadingMessage() throws Exception {
        TextMessage message = Mockito.mock(TextMessage.class);
        Mockito.when(message.getText()).thenThrow(new JMSException(""));
        taskManager.onMessage(message);
        Mockito.verifyNoMoreInteractions(this.repository);
        this.internalListener.assertNoMessageSent();
    }
    
    private static class InternalListener implements MessageListener {
        
        private ConnectionFactory connectionFactory;
        
        private Topic topic;
        
        private Connection connection;
        
        private Session session;
        
        private List<String> receivedMessages = new ArrayList<String>();
        
        public InternalListener(ConnectionFactory connectionFactory, Topic topic) {
            this.connectionFactory = connectionFactory;
            this.topic = topic;
        }

        public void startListening() throws Exception {
            connection = this.connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(topic);
            consumer.setMessageListener(this);
            connection.start();
        }
        
        public void stopListening() throws Exception {
            session.close();
            connection.close();
        }

        @Override
        public void onMessage(Message message) {
            if(message instanceof TextMessage){
                TextMessage txtMessage = (TextMessage)message;
                try {
                    receivedMessages.add(txtMessage.getText());
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        
        public void assertNoMessageSent() throws Exception {
            Assert.assertEquals(0, receivedMessages.size());
        }
        
        public void assertOneMessageSent() throws Exception {
            Assert.assertEquals(1, receivedMessages.size());
        }
        
        public String getLastMessageReceived() throws Exception {
            return this.receivedMessages.get(0);
        }
        
    }
}
